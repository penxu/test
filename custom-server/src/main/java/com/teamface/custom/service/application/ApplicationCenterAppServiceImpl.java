package com.teamface.custom.service.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.QueryOperators;
import com.mongodb.client.MongoCursor;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.Constant;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.StringUtil;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JSONParser4SQL;
import com.teamface.common.util.dao.LayoutUtil;
import com.teamface.common.util.dao.MongoDBUtil;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.auth.ModuleDataAuthAppService;
import com.teamface.custom.service.library.FileLibraryAppService;

/**
 * @Description:应用中心dubbo接口实现
 * @author: Administrator
 * @date: 2018年1月18日 下午3:29:44
 * @version: 1.0
 */
@Service("applicationCenterAppService")
public class ApplicationCenterAppServiceImpl implements ApplicationCenterAppService
{
    private static final Logger log = LogManager.getLogger(ApplicationCenterAppServiceImpl.class);
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    private static final MongoDBUtil mongoDB = MongoDBUtil.getInstance(Constant.DB_NAME);
    
    @Autowired
    ModuleDataAuthAppService moduleDataAuthAppService;
    
    @Autowired
    FileLibraryAppService fileLibraryAppService;
    
    /**
     * @param token
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:发布应用模板
     */
    
    @Override
    public ServiceResult<String> uploadTemplate(String token, String reqJsonStr)
    {
        log.debug("start !");
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        log.info("开始上传应用模版！！！");
        Long startTime = System.currentTimeMillis();
        try
        {
            // 请求参数
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            Long applicationId = reqJson.getLong("applicationId");
            
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            Long employeeId = info.getEmployeeId();
            
            // 获取应用模版记录
            JSONObject applicationTemplate = this.queryApplicationTemplate(applicationId, info);
            if (null != applicationTemplate)
            {
                log.info("发现历史版本，将进行模版数据覆盖");
                JSONObject templateJson = new JSONObject();
                templateJson.put("templateId", applicationTemplate.getLong("id"));
                // 删除旧版本
                ServiceResult<String> removeTemplateResult = this.removeTemplate(token, templateJson.toString());
                if (!removeTemplateResult.isSucces())
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), "覆盖应用模板数据失败！");
                    return serviceResult;
                }
            }
            
            // 保存应用模板数据
            long applicationTmplateId = this.saveTemplateData(reqJson, info);
            if (applicationTmplateId < 1)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), "保存应用模板数据失败！");
                return serviceResult;
            }
            
            // 复制应用下的模块
            List<JSONObject> copyModuleList = this.copyApplicationModule(applicationId, applicationTmplateId, companyId, employeeId);
            if (copyModuleList.size() < 1)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), "复制应用模块失败！");
                return serviceResult;
            }
            
            for (JSONObject moduleJson : copyModuleList)
            {
                String moduleBean = moduleJson.getString("english_name");
                Long moduleTemplateId = moduleJson.getLong("template_id");
                // 复制模块菜单（含菜单规则）
                boolean copyModuleMenuResult = this.copyModuleMenu(moduleJson.getInteger("module_id"), moduleTemplateId, companyId, employeeId);
                if (!copyModuleMenuResult)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), "复制模块菜单失败！");
                    break;
                }
                
                BasicDBObject mappingCondition = new BasicDBObject();
                BasicDBObject relyonCondition = new BasicDBObject();
                BasicDBObject convertCondition = new BasicDBObject();
                
                StringBuilder relationCondition = new StringBuilder();
                // 复制页面布局（含已使用字段和未使用字段，生成新模块数据表）
                boolean copyModuleLayoutResult = this.copyModuleLayout(applicationId,
                    moduleBean,
                    moduleTemplateId.toString(),
                    companyId.toString(),
                    mappingCondition,
                    relyonCondition,
                    convertCondition,
                    relationCondition);
                if (!copyModuleLayoutResult)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), moduleBean.concat("复制布局"));
                    break;
                }
                
                // 复制列表显示字段(含PC端和APP端)
                boolean copyModuleListFieldLayoutResult = this.copyModuleListFieldLayout(moduleBean, moduleTemplateId.toString(), companyId.toString(), relationCondition);
                if (!copyModuleListFieldLayoutResult)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), moduleBean.concat("复制列表显示字段失败！"));
                    break;
                }
                
                // 复制关联关系布局
                boolean copyRelationLayoutResult = this.copyFieldRelationLayout(moduleBean, moduleTemplateId.toString(), companyId.toString(), relationCondition);
                if (!copyRelationLayoutResult)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), moduleBean.concat("复制关联关系布局！"));
                    break;
                }
                // 复制字段依赖(含关联映射、关联依赖、选项字段依赖、选项字段控制)
                boolean copyFieldRelyonLayoutResult = this.copyFieldRelyonLayout(moduleBean, moduleTemplateId.toString(), companyId.toString(), mappingCondition, relyonCondition);
                if (!copyFieldRelyonLayoutResult)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), moduleBean.concat("复制字段依赖失败！"));
                    break;
                }
                
                // 复制字段转换
                boolean copyFieldConvertLayoutResult = this.copyFieldConvertLayout(moduleBean, moduleTemplateId.toString(), companyId.toString(), convertCondition);
                if (!copyFieldConvertLayoutResult)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), moduleBean.concat("复制字段转换失败！"));
                    break;
                }
                
                // 复制数据共享(含共享规则和规则详情)
                boolean copyModuleDataShareResult = this.copyModuleDataShare(moduleTemplateId, moduleBean, companyId, employeeId);
                if (!copyModuleDataShareResult)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), moduleBean.concat("复制数据共享失败！"));
                    break;
                }
                
                // 复制自动分配 改成自动化
                boolean copyAutoAllotResult = this.copyAutoAllot(moduleTemplateId, moduleBean, companyId, employeeId, applicationId);
                if (!copyAutoAllotResult)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), moduleBean.concat("复制自动分配失败！"));
                    break;
                }
                
                // 复制自动标记颜色
                boolean copyAutoColorResult = this.copyAutoColor(moduleTemplateId, moduleBean, companyId, employeeId);
                if (!copyAutoColorResult)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), moduleBean.concat("复制自动标记颜色失败！"));
                    break;
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        log.info("上传应用模版成功！！！总耗时：" + (System.currentTimeMillis() - startTime) / 1000);
        log.debug("end !");
        return serviceResult;
    }
    
    /**
     * @param token
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:安装应用模板
     */
    @Override
    public ServiceResult<String> installTemplate(String token, String reqJsonStr)
    {
        log.debug("start !");
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        
        try
        {
            // 请求参数
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            Integer templateId = reqJson.getInteger("templateId");
            
            StringBuilder queryBilder = new StringBuilder();
            queryBilder.append(" select count(*) from application_template").append(" where  id =  ").append(templateId).append(" and upload_status = ").append(
                Constant.CURRENCY_TWO);
            int count = DAOUtil.executeCount(queryBilder.toString());
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.application.download.error"), resultCode.getMsgZh("postprocess.application.download.error"));
                return serviceResult;
            }
            
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            Long employeeId = info.getEmployeeId();
            
            // 生成应用数据
            Long applicationId = this.generateApplication(templateId, companyId, employeeId, token);
            if (null == applicationId)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), "生成应用失败！");
                return serviceResult;
            }
            
            // 存储所有源bean为 key 新 bean做值 为做bean替换
            Map<String, String> map = new HashMap<>();
            
            // 生成模块数据
            List<JSONObject> newModuleList = this.generateModule(templateId, applicationId, companyId, employeeId, token, map);
            
            for (JSONObject newModuleJson : newModuleList)
            {
                Long moduleTemplateId = newModuleJson.getLong("id");
                Integer newModuleId = newModuleJson.getInteger("moduleId");
                String newModuleBean = newModuleJson.getString("moduleBean");
                
                // 生成模块菜单（含菜单规则）
                boolean generateModuleMenuResult = this.generateModuleMenu(newModuleId, moduleTemplateId, companyId, employeeId);
                if (!generateModuleMenuResult)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), "生成模块菜单失败！");
                    break;
                }
                
                // 生成页面布局（含已使用字段和未使用字段）
                boolean generateModuleLayoutResult = this.generateModuleLayout(newModuleBean, moduleTemplateId.toString(), companyId.toString(), map, applicationId.toString());
                if (!generateModuleLayoutResult)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), "生成页面布局（含已使用字段和未使用字段）失败");
                    break;
                }
                
                // 初始化模块权限
                moduleDataAuthAppService.addAuthAndBut(token, "", newModuleJson.getLong("moduleId"), true);
                
                // 生成列表显示字段(含PC端和APP端)
                boolean generateModuleListFieldLayoutResult = this.generateModuleListFieldLayout(newModuleBean, moduleTemplateId.toString(), companyId.toString());
                if (!generateModuleListFieldLayoutResult)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), "生成列表显示字段失败！");
                    break;
                }
                
                // 生成关联关系
                boolean generateRelationFieldLayoutResult = this.generateRelationFieldLayout(newModuleBean, moduleTemplateId.toString(), companyId.toString(), map);
                if (!generateRelationFieldLayoutResult)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), "生成列表显示字段失败！");
                    break;
                }
                
                // 生成字段依赖(含关联映射、关联依赖、选项字段依赖、选项字段控制)
                boolean generateFieldRelyonLayoutResult = this.generateFieldRelyonLayout(newModuleBean, moduleTemplateId.toString(), companyId.toString(), map);
                if (!generateFieldRelyonLayoutResult)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), "生成字段依赖失败！");
                    break;
                }
                
                // 生成字段转换
                boolean generateFieldConvertLayoutResult = this.generateFieldConvertLayout(newModuleBean, moduleTemplateId.toString(), companyId.toString(), map);
                if (!generateFieldConvertLayoutResult)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), "生成字段转换失败！");
                    break;
                }
                
                // 生成数据共享(含共享规则和规则详情)
                boolean generateModuleDataShareResult = this.generateModuleDataShare(moduleTemplateId, newModuleBean, companyId, employeeId);
                if (!generateModuleDataShareResult)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), "生成数据共享失败！");
                    break;
                }
                
                // 生成销售自动化
                boolean generateAutoAllotResult = this.generateAutoAllot(moduleTemplateId, newModuleBean, companyId, employeeId, map);
                if (!generateAutoAllotResult)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), "生成自动分配失败！");
                    break;
                }
                
                // 生成自动标记颜色
                boolean generateAutoColorResult = this.generateAutoColor(moduleTemplateId, newModuleBean, companyId, employeeId);
                if (!generateAutoColorResult)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), "生成自动标记颜色失败！");
                    break;
                }
                
                boolean generateBusinessTableResult = this.generateBusinessTable();
                if (!generateBusinessTableResult)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), "生成业务表失败！");
                    break;
                }
            }
            
            // 应用模版安装记录
            boolean saveInstallResult = this.saveInstallRecord(templateId, employeeId);
            if (!saveInstallResult)
            {
                log.debug("保存应用模版安装记录失败！");
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        log.debug("end !");
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * @param token
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:移除应用模板
     */
    
    @Override
    public ServiceResult<String> removeTemplate(String token, String reqJsonStr)
    {
        log.debug("start !");
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        try
        {
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            // 请求参数
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            Long templateId = reqJson.getLong("templateId");
            
            List<JSONObject> moduleList = this.queryModuleTemplate(templateId);
            if (null == moduleList || moduleList.size() == 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            
            // 移除应用模版
            boolean applicationTemplate = this.removeApplicationTemplate(templateId);
            if (!applicationTemplate)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), "移除模块模版异常！");
                return serviceResult;
            }
            
            // 移出模版评论
            boolean applicationTemplateComment = this.removeApplicationTemplateComment(templateId);
            if (!applicationTemplateComment)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), "移出模版评论异常！");
                return serviceResult;
            }
            for (JSONObject moduleJson : moduleList)
            {
                Long moduleTemplateId = moduleJson.getLong("id");
                // 移除自动标记颜色模版
                boolean autoColorTemplate = this.removeAutoColorTemplate(moduleTemplateId);
                if (!autoColorTemplate)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), "移除自动标记颜色模版异常！");
                    break;
                }
                
                // 移除自动分配模版
                
                boolean autoAllotTemplate = this.removeAutoAllotTemplate(moduleTemplateId);
                if (!autoAllotTemplate)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), "移除自动分配模版异常！");
                    break;
                }
                
                // 移除数据共享模版(含共享规则和规则详情)
                boolean dataShareTemplate = this.removeDataShareTemplate(moduleTemplateId);
                if (!dataShareTemplate)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), "移除数据共享异常！");
                    break;
                }
                
                // 移除字段转换模版
                boolean fieldConvertTemplate = this.removeFieldConvertLayoutTemplate(moduleTemplateId.toString(), companyId.toString());
                if (!fieldConvertTemplate)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), "移除字段转换模版异常！");
                    break;
                }
                
                // 移除字段依赖模版(含关联映射、关联依赖、选项字段依赖、选项字段控制)
                boolean fieldRelyonTemplate = this.removeFieldRelyonLayoutTemplate(moduleTemplateId.toString(), companyId.toString());
                if (!fieldRelyonTemplate)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), "移除字段依赖模版异常！");
                    break;
                }
                
                // 移除列表显示字段模版(含PC端和APP端)
                boolean listFieldsTemplate = this.removeListFieldsLayoutTemplate(moduleTemplateId.toString(), companyId.toString());
                if (!listFieldsTemplate)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), "移除列表显示字段模版异常！");
                    break;
                }
                
                // 移除页面布局模版（含已使用字段和未使用字段）
                boolean formLayoutTemplate = this.removeFormLayoutTemplate(moduleTemplateId.toString(), companyId.toString());
                if (!formLayoutTemplate)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), "移除页面布局模版异常！");
                    break;
                }
                
                // 移除模块菜单模版（含菜单规则）
                boolean menuTemplate = this.removeMenuTemplate(moduleTemplateId);
                if (!menuTemplate)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), "移除模块菜单模版异常！");
                    break;
                }
                
                // 移除模块模版
                boolean moduleTemplate = this.removeModuleTemplate(moduleTemplateId);
                if (!moduleTemplate)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), "移除模块模版异常！");
                    break;
                }
            }
            
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        log.debug("end !");
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 移出模版评论
     * 
     * @param templateId
     * @return
     * @Description:
     */
    
    private boolean removeApplicationTemplateComment(Long templateId)
    {
        try
        {
            StringBuilder removeSql = new StringBuilder();
            // 移除应用模版
            removeSql.append("delete from application_template_comment where template_id = ").append(templateId);
            DAOUtil.executeUpdate(removeSql.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * @param reqMap
     * @return List
     * @Description:获取应用模版列表
     */
    @Override
    public JSONObject queryTemplateList(Map<String, Object> reqMap)
    {
        log.debug("start !");
        JSONObject resultList = new JSONObject();
        try
        {
            // 请求参数
            Object listFlag = reqMap.get("listFlag");
            Object fitIndustry = reqMap.get("fitIndustry");
            Object funcType = reqMap.get("funcType");
            Object templateName = reqMap.get("templateName");
            Object chargeType = reqMap.get("chargeType");
            
            // 解析token
            
            StringBuilder querySql = new StringBuilder();
            querySql.append(
                "select t1.id template_id,t1.fit_industry,t1.func_type,t1.icon_color,t1.icon_url,t1.icon_type,t1.template_name,t1.price, t1.icon, t1.create_time,t1.upload_user,t1.upload_status,t1.upload_describe,t1.view_content,COALESCE (c.star_level,0) as star_level from application_template t1  left join  (SELECT  template_id,AVG(star_level) as star_level from application_template_comment  GROUP BY  template_id)  c  ON t1.id = C .template_id  where 1=1 ");
            if (null != listFlag)
            {
                if (listFlag.toString().equals("0"))
                {// 精品应用
                    querySql.append(" and t1.order_index is not null ");
                }
                else if (listFlag.toString().equals("1"))
                {// 行业应用（所有数据）
                    querySql.append(" and t1.fit_industry in(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13) ");
                }
                else if (listFlag.toString().equals("2"))
                {// 功能分类（所有数据）
                    querySql.append(" and t1.func_type in(1, 2, 3, 4, 5, 6, 7, 8) ");
                }
            }
            if (null != fitIndustry)
            {// 适用行业
                querySql.append(" and t1.fit_industry = ").append(fitIndustry);
            }
            if (null != funcType)
            {// 功能分类
                querySql.append(" and t1.func_type = ").append(funcType);
            }
            if (null != chargeType)
            {// 付费类型
                querySql.append(" and t1.charge_type = ").append(chargeType);
            }
            if (null != templateName)
            {// 搜索应用
                querySql.append(" and (t1.template_name like '%").append(templateName).append("%'  or  upload_user like '%").append(templateName).append("%')");
            }
            querySql.append(" and t1.del_status = " + Constant.CURRENCY_ZERO).append(" and upload_status = ").append(Constant.CURRENCY_TWO);
            if (null != listFlag && listFlag.toString().equals("0"))
            {
                querySql.append(" order by   t1.order_index asc");
            }
            else
            {
                querySql.append(" order by   t1.download_number desc,t1.create_time desc");
            }
            int pageSize = Integer.parseInt(reqMap.get("pageSize").toString());
            int pageNum = Integer.parseInt(reqMap.get("pageNum").toString());
            resultList = BusinessDAOUtil.getTableDataListAndPageInfo(querySql.toString(), pageSize, pageNum);
            
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        log.debug("end !");
        return resultList;
    }
    
    /**
     * @param reqJson
     * @param employeeId
     * @return
     * @Description:保存模板数据
     */
    private long saveTemplateData(JSONObject reqJson, InfoVo info)
    {
        log.debug("start !");
        try
        {
            long dataId = BusinessDAOUtil.getNextval4Table("application_template", "");
            StringBuilder insertSql = new StringBuilder();
            insertSql.append(
                "insert into application_template(id, application_id, template_name, fit_industry, func_type, icon, icon_type,icon_color,icon_url,upload_user, upload_describe, create_by, create_time,customized,charge_type,payment_type,price,receiv_account,function_remark,app_picture,web_picture,introduce,company_id) values(");
            insertSql.append(dataId).append(", ");
            insertSql.append(reqJson.getLong("applicationId")).append(", '");
            insertSql.append(reqJson.getString("template_name")).append("', ");
            insertSql.append(reqJson.getInteger("fit_industry")).append(", ");
            insertSql.append(reqJson.getInteger("func_type")).append(", '");
            insertSql.append(reqJson.getString("icon")).append("', ");
            insertSql.append(reqJson.getString("icon_type")).append(", '");
            insertSql.append(reqJson.getString("icon_color")).append("', '");
            insertSql.append(reqJson.getString("icon_url")).append("', '");
            insertSql.append(reqJson.getString("upload_user")).append("',");
            if (reqJson.getString("upload_describe") == null || reqJson.getString("upload_describe").isEmpty() || "[]".equals(reqJson.getString("upload_describe")))
            {
                insertSql.append("null");
            }
            else
            {
                insertSql.append("'").append(reqJson.getString("upload_describe").replace("'", "''")).append("'");
            }
            insertSql.append(",");
            insertSql.append(info.getEmployeeId()).append(", ");
            insertSql.append(System.currentTimeMillis() + ",'");
            insertSql.append(reqJson.getString("customized")).append("', '");
            insertSql.append(reqJson.getString("charge_type")).append("', '");
            insertSql.append(reqJson.getString("payment_type").trim()).append("', '");
            insertSql.append(reqJson.getString("price")).append("', '");
            insertSql.append(reqJson.getString("receiv_account")).append("', '");
            insertSql.append(reqJson.getString("function_remark")).append("', '");
            insertSql.append(reqJson.getString("app_picture")).append("', '");
            insertSql.append(reqJson.getString("web_picture")).append("', ");
            if (reqJson.getString("introduce") == null || reqJson.getString("introduce").isEmpty() || "[]".equals(reqJson.getString("introduce")))
            {
                insertSql.append("null");
            }
            else
            {
                insertSql.append("'").append(reqJson.getString("introduce").replace("'", "''")).append("'");
            }
            insertSql.append(",");
            insertSql.append(info.getCompanyId());
            insertSql.append(")");
            
            int result = DAOUtil.executeUpdate(insertSql.toString());
            if (result > 0)
            {
                log.debug("end !");
                return dataId;
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        log.debug("end !");
        return 0L;
    }
    
    /**
     * @param applicationId
     * @param tmplateId
     * @param companyId
     * @param employeeId
     * @return
     * @Description:复制模块数据
     */
    private List<JSONObject> copyApplicationModule(Long applicationId, Long applicationTmplateId, Long companyId, Long employeeId)
    {
        log.debug("start !");
        List<JSONObject> resultList = new ArrayList<JSONObject>();
        try
        {
            String applicationModuleTable = DAOUtil.getTableName("application_module", companyId);
            StringBuilder copySql = new StringBuilder();
            copySql.append(
                "insert into application_template_module(template_id, chinese_name, english_name, topper, terminal_app, icon,icon_type,icon_url,icon_color, edition,create_by, create_time)");
            copySql.append("select ");
            copySql.append(applicationTmplateId);
            copySql.append(", chinese_name, english_name, topper, terminal_app, icon,icon_type,icon_url,icon_color,edition, ");
            copySql.append(employeeId).append(", ");
            copySql.append(System.currentTimeMillis());
            copySql.append(" from ").append(applicationModuleTable);
            copySql.append(" where application_id = ").append(applicationId);
            copySql.append(" and del_status = 0");
            
            int copyResult = DAOUtil.executeUpdate(copySql.toString());
            if (copyResult < 1)
            {
                return new ArrayList<JSONObject>();
            }
            
            copySql.setLength(0);
            copySql.append("select t1.id template_id, t2.id module_id, t1.english_name from application_template_module t1, ");
            copySql.append(applicationModuleTable).append(" t2");
            copySql.append(" where t1.template_id = ").append(applicationTmplateId);
            copySql.append(" and t1.del_status = 0 and t1.english_name = t2.english_name");
            resultList = DAOUtil.executeQuery4JSON(copySql.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        log.debug("end !");
        return resultList;
    }
    
    /**
     * @param moduleId
     * @param moduleTmplateId
     * @param companyId
     * @param employeeId
     * @return
     * @Description:复制模块菜单数据
     */
    private boolean copyModuleMenu(Integer moduleId, Long moduleTmplateId, Long companyId, Long employeeId)
    {
        try
        {
            String applicationModuleTable = DAOUtil.getTableName("application_module_submenu", companyId);
            
            // 根据模块查询模块菜单模版数据
            StringBuilder copySql = new StringBuilder();
            copySql.append("select id module_menu_id from ").append(applicationModuleTable).append(" where module_id = ").append(moduleId);
            copySql.append(" and del_status = 0");
            List<JSONObject> moduleMenuList = DAOUtil.executeQuery4JSON(copySql.toString());
            
            if (null != moduleMenuList && moduleMenuList.size() > 0)
            {
                for (JSONObject jsonObject : moduleMenuList)
                {
                    copySql.setLength(0);
                    long templateModuleMenuId = BusinessDAOUtil.getNextval4Table("application_template_module_menu", "");
                    long moduleMenuId = jsonObject.getLongValue("module_menu_id");
                    // 复制模块菜单模版数据
                    copySql.append(
                        "insert into application_template_module_menu(id, template_module_id, name, high_where, type, topper, employee_id, allot_employee, allot_employee_v, target_lable, create_by, create_time)");
                    copySql.append("select ");
                    copySql.append(templateModuleMenuId).append(", ");
                    copySql.append(moduleTmplateId);
                    copySql.append(", name, high_where, type, topper, employee_id, allot_employee, allot_employee_v, target_lable, ");
                    copySql.append(employeeId).append(", ");
                    copySql.append(System.currentTimeMillis());
                    copySql.append(" from ").append(applicationModuleTable);
                    copySql.append(" where id = ").append(moduleMenuId);
                    copySql.append(" and del_status = 0");
                    int copyResult = DAOUtil.executeUpdate(copySql.toString());
                    if (copyResult < 1)
                    {
                        return false;
                    }
                    // 复制模块菜单规则模版数据
                    this.copyModuleMenuRule(templateModuleMenuId, moduleMenuId, companyId, employeeId);
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * @param moduleMenuList
     * @param companyId
     * @param employeeId
     * @return
     * @Description:复制菜单规则数据
     */
    private boolean copyModuleMenuRule(Long moduleMenueTmplateId, Long moduleMenuId, Long companyId, Long employeeId)
    {
        try
        {
            String submenuRuleTable = DAOUtil.getTableName("submenu_rule", companyId);
            
            // 复制菜单数据
            StringBuilder copySql = new StringBuilder();
            copySql.append(
                "insert into application_template_submenu_rule(template_module_menu_id, field_label, field_value, operator_label, operator_value, result_label, result_value, show_type, operators, entrys, sel_list, sel_time, value_field, create_by, create_time)");
            copySql.append("select ");
            copySql.append(moduleMenueTmplateId);
            copySql
                .append(", field_label, field_value, operator_label, operator_value, result_label, result_value, show_type, operators, entrys, sel_list, sel_time, value_field, ");
            copySql.append(employeeId).append(", ");
            copySql.append(System.currentTimeMillis());
            copySql.append(" from ").append(submenuRuleTable);
            copySql.append(" where submenu_id = ").append(moduleMenuId);
            DAOUtil.executeUpdate(copySql.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * @param moduleBean
     * @param companyId
     * @param relationCondition
     * @return
     * @Description:复制页面布局（已使用字段、未使用字段）
     */
    private boolean copyModuleLayout(Long applicationId, String moduleBean, String moduleTemplateId, String companyId, BasicDBObject mappingCondition,
        BasicDBObject relyonCondition, BasicDBObject convertCondition, StringBuilder relationCondition)
    {
        try
        {
            // 组装复制条件
            Document copyWhere = new Document();
            copyWhere.put("bean", moduleBean);
            copyWhere.put("companyId", companyId);
            
            // 获取需复制的布局
            List<JSONObject> fromModuleLayouts = LayoutUtil.findDocs(copyWhere, Constant.CUSTOMIZED_COLLECTION);
            if (null != fromModuleLayouts && fromModuleLayouts.size() > 0)
            {
                // 查询条件
                StringBuilder excludeFields = new StringBuilder();
                for (JSONObject nextFromModule : fromModuleLayouts)
                {
                    // 设置模块模版id
                    nextFromModule.put("moduleTemplateId", moduleTemplateId);
                    if (nextFromModule.getString("layoutState").equals(Constant.LAYOUT_TYPE_DISABLE))
                    {
                        Document saveDoc = new Document();
                        saveDoc.putAll(nextFromModule);
                        LayoutUtil.saveDoc(saveDoc, Constant.CUSTOMIZED_COLLECTION_CENTER);
                        continue;
                    }
                    
                    // 处理布局逻辑
                    JSONArray layouts = nextFromModule.getJSONArray("layout");
                    JSONArray newLayouts = new JSONArray();
                    for (Object layout : layouts)
                    {
                        JSONObject layoutJson = (JSONObject)layout;
                        if (layoutJson.getString("name").equals("systemInfo"))
                        {
                            newLayouts.add(layoutJson);
                            continue;
                        }
                        
                        JSONArray rows = layoutJson.getJSONArray("rows");
                        JSONArray newRowsArr = new JSONArray();
                        for (Object row : rows)
                        {
                            JSONObject rowJson = (JSONObject)row;
                            if (rowJson.getString("type").equals(Constant.TYPE_REFERENCE))
                            {
                                // 关联模块
                                String refModuleName = rowJson.getJSONObject("relevanceModule").getString("moduleName");
                                JSONObject moduleInfo = this.queryModuleByAppIdAndBean(applicationId, refModuleName, Long.parseLong(companyId));
                                // 1、页面布局中关联关系组件关联了非本应用内的模块则将当前字段属性中的关联模块、关联字段、列表搜索字段清空
                                if (null != moduleInfo)
                                {// 过滤非本应用中的关联组件
                                    newRowsArr.add(row);
                                }
                                else
                                {
                                    excludeFields.append(refModuleName).append(",");
                                    relationCondition.append(rowJson.getString("name")).append(",");
                                }
                            }
                            else
                            {
                                newRowsArr.add(row);
                            }
                            /**
                             * else if
                             * (rowJson.getString("type").equals(Constant.TYPE_PERSONNEL)){}
                             * 
                             * 人员组件中默认值固定为当前用户 -> 前端拉取布局时处理（所有人员组件默认为当前人员）
                             * 可选范围默认为全公司 -> 现默认就是从全公司选（不支持从其他范围内选择）
                             */
                        }
                        layoutJson.put("rows", newRowsArr);
                        newLayouts.add(layoutJson);
                    }
                    nextFromModule.put("layout", newLayouts);
                    Document saveDoc = new Document();
                    saveDoc.putAll(nextFromModule);
                    LayoutUtil.saveDoc(saveDoc, Constant.CUSTOMIZED_COLLECTION_CENTER);
                }
                // 关联映射条件
                mappingCondition.append("controlField.name", new BasicDBObject(QueryOperators.NIN, excludeFields.toString().split(",")));
                // 关联依赖条件
                relyonCondition.append(QueryOperators.AND,
                    new BasicDBObject[] {new BasicDBObject("controlField.name", new BasicDBObject(QueryOperators.NIN, excludeFields.toString().split(","))),
                        new BasicDBObject("relyonField.name", new BasicDBObject(QueryOperators.NIN, excludeFields.toString().split(",")))});
                // 字段转换条件
                convertCondition.append("basics.target_bean", new BasicDBObject(QueryOperators.NIN, excludeFields.toString().split(",")));
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * @param moduleBean
     * @param companyId
     * @param relationCondition
     * @return
     * @Description:复制列表显示字段（含PC列表字段、APP列表字段）
     */
    private boolean copyModuleListFieldLayout(String moduleBean, String moduleTemplateId, String companyId, StringBuilder relationCondition)
    {
        try
        {
            // 组装复制条件
            Document copyWhere = new Document();
            copyWhere.put("bean", moduleBean);
            copyWhere.put("companyId", companyId);
            boolean copyResult =
                LayoutUtil.copyModuleListFieldDocment(Constant.LIST_FIELDS_COLLECTION, Constant.LIST_FIELDS_COLLECTION_CENTER, moduleTemplateId, copyWhere, relationCondition);
            if (!copyResult)
            {
                return false;
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * 
     * @param moduleBean
     * @param moduleTemplateId
     * @param companyId
     * @param relyonCondition
     * @return
     * @Description:复制模块关联关系布局
     */
    private boolean copyFieldRelationLayout(String moduleBean, String moduleTemplateId, String companyId, StringBuilder relyonCondition)
    {
        try
        {
            // 组装复制条件
            Document copyWhere = new Document();
            copyWhere.put("bean", moduleBean);
            copyWhere.put("companyId", companyId);
            boolean copyResult =
                LayoutUtil.copyRelationFieldDocment(Constant.RELATION_COLLECTION, Constant.RELATION_COLLECTION_CENTER, moduleTemplateId, copyWhere, relyonCondition);
            if (!copyResult)
            {
                return false;
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * @param moduleBean
     * @param companyId
     * @return
     * @Description:复制字段依赖布局
     */
    private boolean copyFieldRelyonLayout(String moduleBean, String moduleTemplateId, String companyId, BasicDBObject mappingCondition, BasicDBObject relyonCondition)
    {
        try
        {
            // 组装复制条件
            mappingCondition.append(QueryOperators.AND, new BasicDBObject[] {new BasicDBObject("bean", moduleBean), new BasicDBObject("companyId", companyId)});
            relyonCondition.append(QueryOperators.AND, new BasicDBObject[] {new BasicDBObject("bean", moduleBean), new BasicDBObject("companyId", companyId)});
            // 复制关联映射
            boolean copyRelationMappedResult = LayoutUtil.copyDocment(Constant.MAPPING_COLLECTION, Constant.MAPPING_COLLECTION_CENTER, moduleTemplateId, mappingCondition);
            // 复制关联依赖
            boolean copyRelationRelyonResult = LayoutUtil.copyDocment(Constant.RELYON_COLLECTION, Constant.RELYON_COLLECTION_CENTER, moduleTemplateId, relyonCondition);
            
            // 组装复制条件
            Document copyWhere = new Document();
            copyWhere.put("bean", moduleBean);
            copyWhere.put("companyId", companyId);
            
            // 复制选项字段依赖
            boolean copyOptionFieldRelyResult =
                LayoutUtil.copyDocment(Constant.PICKUPLIST_RELYON_COLLECTION, Constant.PICKUPLIST_RELYON_COLLECTION_CENTER, moduleTemplateId, copyWhere);
            // 复制选项字段控制
            boolean copyOptionFieldControlResult =
                LayoutUtil.copyDocment(Constant.PICKUPLIST_CONTROL_COLLECTION, Constant.PICKUPLIST_CONTROL_COLLECTION_CENTER, moduleTemplateId, copyWhere);
            
            if (!copyRelationMappedResult || !copyRelationRelyonResult || !copyOptionFieldRelyResult || !copyOptionFieldControlResult)
            {
                return false;
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * @param moduleBean
     * @param companyId
     * @return
     * @Description:复制字段转换布局
     */
    private boolean copyFieldConvertLayout(String moduleBean, String moduleTemplateId, String companyId, BasicDBObject convertCondition)
    {
        try
        {
            // 组装复制条件
            convertCondition.append(QueryOperators.AND, new BasicDBObject[] {new BasicDBObject("bean", moduleBean), new BasicDBObject("companyId", companyId)});
            // 复制字段转换
            boolean copyResult = LayoutUtil.copyDocment(Constant.FIELD_COLLECTION, Constant.FIELD_COLLECTION_CENTER, moduleTemplateId, convertCondition);
            if (!copyResult)
            {
                return false;
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * @param moduleTemplateId
     * @param moduleBean
     * @param companyId
     * @param employeeId
     * @return
     * @Description:复制数据共享规则数据
     */
    private boolean copyModuleDataShare(Long moduleTemplateId, String moduleBean, Long companyId, Long employeeId)
    {
        try
        {
            String shareTable = DAOUtil.getTableName("module_share_setting", companyId);
            StringBuilder copySql = new StringBuilder();
            copySql.append(
                "insert into application_template_share(template_module_id, title, bean_name, employee_id, condition, high_where, access_permissions, create_by, create_time)");
            copySql.append("select ");
            copySql.append(moduleTemplateId);
            copySql.append(", title, bean_name, employee_id, condition, high_where, access_permissions, ");
            copySql.append(employeeId).append(", ");
            copySql.append(System.currentTimeMillis());
            copySql.append(" from ").append(shareTable);
            copySql.append(" where bean_name = '").append(moduleBean).append("'");
            int shareResult = DAOUtil.executeUpdate(copySql.toString());
            if (shareResult < 1)
            {
                return true;
            }
            
            // 复制数据共享规则详情数据
            copySql.setLength(0);
            copySql.append("select t1.id template_share_id, t2.id share_id from application_template_share t1, ");
            copySql.append(shareTable).append(" t2");
            copySql.append(" where t1.template_module_id = ").append(moduleTemplateId);
            copySql.append(" and t1.del_status = 0 and t1.bean_name = t2.bean_name");
            List<JSONObject> shareList = DAOUtil.executeQuery4JSON(copySql.toString());
            this.copyModuleDataShareDetail(shareList, companyId, employeeId);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * @param shareList
     * @param companyId
     * @param employeeId
     * @return
     * @Description:复制共享规则详情数据
     */
    private boolean copyModuleDataShareDetail(List<JSONObject> shareList, Long companyId, Long employeeId)
    {
        try
        {
            String shareDtailTable = DAOUtil.getTableName("module_share_setting_detail", companyId);
            
            for (JSONObject jsonObject : shareList)
            {
                Integer tmplateShareId = jsonObject.getInteger("template_share_id");
                Long shareId = jsonObject.getLong("share_id");
                
                // 复制共享规则详情数据
                StringBuilder copySql = new StringBuilder();
                copySql.append(
                    "insert into application_template_share_detail(template_share_id, field_value, field_label, operator_value, operator_label, result_value, result_label, show_type, operators, entrys, sel_list, sel_time, value_field, create_by, create_time)");
                copySql.append("select ");
                copySql.append(tmplateShareId);
                copySql.append(
                    ", field_value, field_label, operator_value, operator_label, result_value, result_label, show_type, operators, entrys, sel_list, sel_time, value_field, ");
                copySql.append(employeeId).append(", ");
                copySql.append(System.currentTimeMillis());
                copySql.append(" from ").append(shareDtailTable);
                copySql.append(" where share_id = ").append(shareId);
                DAOUtil.executeUpdate(copySql.toString());
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * @param moduleTemplateId
     * @param moduleBean
     * @param companyId
     * @param employeeId
     * @return
     * @Description:复制自动分配规则
     */
    private boolean copyAutoAllot(Long moduleTemplateId, String moduleBean, Long companyId, Long employeeId, Long applicationId)
    {
        try
        {
            String ruleTable = DAOUtil.getTableName("automation", companyId);
            // 根据自动分配规则数据
            StringBuilder copySql = new StringBuilder();
            copySql.append("select *  from ").append(ruleTable).append(" where bean = '").append(moduleBean);
            copySql.append("' and del_status = " + Constant.CURRENCY_ZERO);
            List<JSONObject> ruleList = DAOUtil.executeQuery4JSON(copySql.toString());
            
            if (null != ruleList && ruleList.size() > 0)
            {
                List<List<Object>> batchValues = new ArrayList<>();
                for (JSONObject jsonObject : ruleList)
                {
                    List<Object> model = new ArrayList<>();
                    long templateAutomationId = BusinessDAOUtil.getNextval4Table("application_template_automation", "");
                    model.add(templateAutomationId);
                    model.add(moduleTemplateId);
                    model.add(jsonObject.getString("bean"));
                    model.add(jsonObject.getString("title"));
                    model.add(jsonObject.getString("triggers"));
                    model.add(jsonObject.getString("remark"));
                    model.add(jsonObject.getString("condition"));
                    model.add(jsonObject.getString("query_condition"));
                    
                    JSONObject array = JSONObject.parseObject(jsonObject.getString("query_parameter"));
                    if (null != array.get("operation"))
                    {
                        // 解析条件
                        JSONArray arrList = JSONArray.parseArray(array.getString("operation"));
                        for (int i = 0; i < arrList.size(); i++)
                        {
                            JSONObject json = arrList.getJSONObject(i);
                            if (json.getInteger("type") == 0)// 更新处理
                            {
                                JSONObject jsonObject1 = JSONObject.parseObject(json.getString("module"));
                                boolean flag = commonQuery(applicationId, jsonObject1.getString("bean"), companyId);
                                if (!flag)
                                {
                                    // 若更新模块属于非本应用则该字段更新删除
                                    arrList.remove(json);
                                    i = i - 1;
                                }
                                JSONObject jsonObject2 = JSONObject.parseObject(json.getString("field"));
                                if (jsonObject2.getString("name").indexOf("personnel") != -1)
                                {
                                    // 若更新模块属于人员字段则该字段更新删除
                                    arrList.remove(json);
                                    i = i - 1;
                                }
                            }
                            else if (json.getInteger("type") == 1)// 若目标模块属于非本应用模块则将该设置删除
                            {
                                JSONArray objectList = JSONArray.parseArray(json.getString("list"));
                                for (int j = 0; j < objectList.size(); j++)
                                {
                                    String jsonObject1 = (String)objectList.get(j);
                                    // 获取转换布局
                                    JSONObject obj = LayoutUtil.getById(Constant.FIELD_COLLECTION, jsonObject1);
                                    // 获取转换目标bean
                                    JSONObject targetJson = JSONObject.parseObject(obj.getString("basics"));
                                    boolean flag = commonQuery(applicationId, targetJson.getString("target_bean"), companyId);
                                    if (!flag)
                                    {
                                        json.replace("remark", "");
                                        // 不是则剔除
                                        objectList.remove(jsonObject1);
                                    }
                                }
                                // 没有符合的则都剔除
                                if (objectList.size() <= 0)
                                {
                                    arrList.remove(json);
                                    i = i - 1;
                                }
                            }
                            else if (json.getInteger("type") == 2) // 自动分配
                            {
                                // 被分配人值清空
                                json.replace("allot_employee", "");
                                json.replace("remark", "");
                            }
                            
                        }
                        
                        array.replace("operation", arrList);
                    }
                    model.add(array.toString());
                    model.add(employeeId);
                    model.add(System.currentTimeMillis());
                    batchValues.add(model);
                    
                    // 复制自动分配规则详情数据
                    this.copyAutoAllotDetail(applicationId, jsonObject.getLong("id"), companyId, employeeId, templateAutomationId);
                }
                // 判断是否有需要插入值
                if (batchValues.size() > 0)
                {
                    String automationTable = DAOUtil.getTableName("application_template_automation", "");
                    StringBuilder buil = new StringBuilder("insert into " + automationTable
                        + "(id,template_module_id,bean,title,triggers,remark,condition,query_condition,query_parameter,create_by,create_time) values(?,?,?,?,?,?,?,?,?,?,?)");
                    int count = DAOUtil.executeUpdate(buil.toString(), batchValues, 1000);
                    if (count <= 0)
                    {
                        return false;
                    }
                }
                
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }
    
    /**
     * 查询判断是否是当前应用模块
     * 
     * @param applicationId
     * @param bean
     * @param companyId
     * @return
     * @Description:
     */
    private boolean commonQuery(Long applicationId, String bean, Long companyId)
    {
        String moduleTable = DAOUtil.getTableName("application_module", companyId);
        StringBuilder buf = new StringBuilder(" select count(*) from ").append(moduleTable).append(" where english_name = '" + bean + "' and application_id = " + applicationId);
        // 是否是本模块的更新
        int sum = DAOUtil.executeCount(buf.toString());
        if (sum <= 0)
        {
            return false;
        }
        return true;
        
    }
    
    /**
     * @param autoAllotList
     * @param companyId
     * @param employeeId
     * @param templateAutomationId
     * @return
     * @Description:复制自动分配规则详情
     */
    private boolean copyAutoAllotDetail(Long applicationId, Long automationId, Long companyId, Long employeeId, long templateAutomationId)
    {
        try
        {
            String handleTable = DAOUtil.getTableName("automation_handle", companyId);
            
            StringBuilder builder = new StringBuilder();
            builder.append(" select  * from  ").append(handleTable).append(" where automation_id = " + automationId);
            List<JSONObject> jsonList = DAOUtil.executeQuery4JSON(builder.toString());
            if (null != jsonList && jsonList.size() > 0)
            {
                int num = 0;
                List<List<Object>> batchValues = new ArrayList<>();
                for (JSONObject jsonObject : jsonList)
                {
                    List<Object> model = new ArrayList<>();
                    model.add(templateAutomationId);
                    model.add(jsonObject.getString("type"));
                    
                    JSONObject json = JSONObject.parseObject(jsonObject.getString("content"));
                    if (jsonObject.getInteger("type") == Constant.CURRENCY_ZERO)// 更新处理
                                                                                // 若更新模块属于非本应用则该字段更新删除
                    {
                        JSONObject jsonObject1 = JSONObject.parseObject(json.getString("module"));
                        boolean flag = commonQuery(applicationId, jsonObject1.getString("bean"), companyId);
                        if (!flag)
                        {
                            // 不是则剔除
                            num = 1;
                        }
                    }
                    else if (jsonObject.getInteger("type") == Constant.CURRENCY_ONE)// 若目标模块属于非本应用模块则将该设置删除
                    {
                        JSONArray objectList = JSONArray.parseArray(json.getString("list"));
                        for (int j = 0; j < objectList.size(); j++)
                        {
                            String jsonObject1 = (String)objectList.get(j);
                            // 获取转换布局
                            JSONObject obj = LayoutUtil.getById(Constant.FIELD_COLLECTION, jsonObject1);
                            // 获取转换目标bean
                            JSONObject targetJson = JSONObject.parseObject(obj.getString("basics"));
                            boolean flag = commonQuery(applicationId, targetJson.getString("target_bean"), companyId);
                            if (!flag)
                            {
                                // 不是则剔除
                                objectList.remove(jsonObject1);
                            }
                        }
                        // 没有符合的则都剔除
                        if (objectList.size() <= 0)
                        {
                            num = 1;
                        }
                    }
                    else if (jsonObject.getInteger("type") == Constant.CURRENCY_TWO) // 自动分配
                    { // 将被分配人值清空
                        
                        json.replace("allot_employee", "");
                    }
                    model.add(json.toString());
                    model.add(employeeId);
                    model.add(System.currentTimeMillis());
                    // 是否存在值
                    if (num == 0)
                    {
                        batchValues.add(model);
                    }
                    
                }
                // 判断是否有需要插入值
                if (batchValues.size() > 0)
                {
                    String detailTable = DAOUtil.getTableName("application_template_automation_handle_detail", "");
                    StringBuilder insertBuilder = new StringBuilder("insert into " + detailTable + "(template_automation_id,type,content,create_by,create_time) values(?,?,?,?,?)");
                    int count = DAOUtil.executeUpdate(insertBuilder.toString(), batchValues, 10);
                    if (count <= 0)
                    {
                        return false;
                    }
                }
                
            }
            
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * @param moduleTemplateId
     * @param moduleBean
     * @param companyId
     * @param employeeId
     * @return
     * @Description:复制自动标记颜色规则
     */
    private boolean copyAutoColor(Long moduleTemplateId, String moduleBean, Long companyId, Long employeeId)
    {
        try
        {
            String ruleColorTable = DAOUtil.getTableName("rule_colour", companyId);
            // 查询自动标记颜色数据
            StringBuilder copySql = new StringBuilder();
            copySql.append("select id rule_color_id from ").append(ruleColorTable).append(" where bean = '").append(moduleBean);
            copySql.append("' and status = 0");
            List<JSONObject> ruleColorList = DAOUtil.executeQuery4JSON(copySql.toString());
            
            if (null != ruleColorList && ruleColorList.size() > 0)
            {
                for (JSONObject jsonObject : ruleColorList)
                {
                    copySql.setLength(0);
                    long templateAutoColorId = BusinessDAOUtil.getNextval4Table("application_template_auto_color", "");
                    long ruleColorId = jsonObject.getLongValue("rule_color_id");
                    // 复制自动标记颜色规则数据
                    copySql.append("insert into application_template_auto_color(template_module_id, title, high_where, bean, condition, colour, create_by, create_time)");
                    copySql.append("select ");
                    copySql.append(moduleTemplateId);
                    copySql.append(", title, high_where, bean, condition, colour, ");
                    copySql.append(employeeId).append(", ");
                    copySql.append(System.currentTimeMillis());
                    copySql.append(" from ").append(ruleColorTable);
                    copySql.append(" where id = ").append(ruleColorId);
                    copySql.append(" and status = 0");
                    int copyResult = DAOUtil.executeUpdate(copySql.toString());
                    if (copyResult < 1)
                    {
                        return false;
                    }
                    // 复制自动标记颜色规则详情数据
                    this.copyAutoColorDetail(templateAutoColorId, ruleColorId, companyId, employeeId);
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * @param autoAllotList
     * @param companyId
     * @param employeeId
     * @return
     * @Description:复制自动标记颜色规则详情
     */
    private boolean copyAutoColorDetail(Long templateAutoColorId, Long ruleColorId, Long companyId, Long employeeId)
    {
        try
        {
            String autoColorDtailTable = DAOUtil.getTableName("rule_colour_detail", companyId);
            // 复制自动标记颜色规则详情数据
            StringBuilder copySql = new StringBuilder();
            copySql.append(
                "insert into application_template_auto_color_detail(template_auto_color_id, rule_colour_id, field_value, field_label, operator_value, operator_label, result_value, result_label, show_type, operators, entrys, sel_list, sel_time, value_field, create_by, create_time)");
            copySql.append("select ");
            copySql.append(templateAutoColorId);
            copySql.append(
                ", rule_colour_id, field_value, field_label, operator_value, operator_label, result_value, result_label, show_type, operators, entrys, sel_list, sel_time, value_field, ");
            copySql.append(employeeId).append(", ");
            copySql.append(System.currentTimeMillis());
            copySql.append(" from ").append(autoColorDtailTable);
            copySql.append(" where rule_colour_id = ").append(ruleColorId);
            DAOUtil.executeUpdate(copySql.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * @param templateId
     * @param companyId
     * @param employeeId
     * @return
     * @Description:生成应用
     */
    private Long generateApplication(Integer templateId, Long companyId, Long employeeId, String token)
    {
        long applicationId = BusinessDAOUtil.getNextval4Table("application", companyId.toString());
        try
        {
            String applicationTable = DAOUtil.getTableName("application", companyId);
            
            StringBuilder insertSql = new StringBuilder();
            insertSql.append("insert into ").append(applicationTable);
            insertSql.append(
                "(id, name, icon,icon_type,icon_color,icon_url, company_id, topper, personnel_create_by, datetime_create_time, personnel_modify_by, datetime_modify_time, del_status)");
            insertSql.append("select ").append(applicationId);
            insertSql.append(", template_name, icon,icon_type,icon_color,icon_url, ").append(companyId);
            insertSql.append(", 1, ").append(employeeId);
            insertSql.append(", ").append(System.currentTimeMillis());
            insertSql.append(", ").append(employeeId);
            insertSql.append(", ").append(System.currentTimeMillis());
            insertSql.append(", 0 from application_template where id = ").append(templateId);
            
            int insertResult = DAOUtil.executeUpdate(insertSql.toString());
            if (insertResult < 1)
            {
                return null;
            }
            else
            {
                JSONObject applicationJson = this.queryApplicationByAppIdAndBean(applicationId, companyId);
                log.error("时间" + System.currentTimeMillis() + " 名称 " + applicationJson.getString("name"));
                // 创建文件夹
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("type", "0");
                jsonObject.put("chinese_name", null == applicationJson ? "No application name!" : applicationJson.getString("name"));// 应用的名称
                jsonObject.put("english_name", "");
                jsonObject.put("id", applicationId);// 应用id
                jsonObject.put("parent_id", "");
                fileLibraryAppService.savaLibrary(token, jsonObject);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return applicationId;
    }
    
    /**
     * @param tmplateId
     * @param applicationId
     * @param companyId
     * @param employeeId
     * @param map
     * @return
     * @Description:生成模块
     */
    private List<JSONObject> generateModule(Integer tmplateId, Long applicationId, Long companyId, Long employeeId, String token, Map<String, String> map)
    {
        List<JSONObject> result = new ArrayList<JSONObject>();
        try
        {
            String applicationModuleTable = DAOUtil.getTableName("application_module", companyId);
            StringBuilder querySql = new StringBuilder();
            querySql.append("select * from application_template_module where template_id = ").append(tmplateId);
            querySql.append(" order by create_time desc");
            List<JSONObject> templateModule = DAOUtil.executeQuery4JSON(querySql.toString());
            
            // 循环生成模块
            StringBuilder generateSql = new StringBuilder();
            for (JSONObject jsonObject : templateModule)
            {
                long moduleId = BusinessDAOUtil.getNextval4Table("application_module", companyId.toString());
                String moduleBean = "bean" + System.currentTimeMillis();
                
                generateSql.append("insert into ");
                generateSql.append(applicationModuleTable);
                generateSql.append(
                    "(id, application_id, chinese_name, topper, personnel_create_by, datetime_create_time, personnel_modify_by, datetime_modify_time, icon, icon_type,icon_color,icon_url ,edition,english_name, del_status,terminal_app)");
                generateSql.append("select ");
                generateSql.append(moduleId).append(", ");
                generateSql.append(applicationId).append(", chinese_name , topper, ");
                generateSql.append(employeeId).append(", ");
                generateSql.append(System.currentTimeMillis()).append(", ");
                generateSql.append(employeeId).append(", ");
                generateSql.append(System.currentTimeMillis()).append(", icon,icon_type,icon_color,icon_url,edition,'");
                generateSql.append(moduleBean);
                generateSql.append("', 0,terminal_app  from application_template_module where del_status = 0 and template_id = ");
                generateSql.append(tmplateId).append(" and id = ").append(jsonObject.getIntValue("id"));
                int updateResult = DAOUtil.executeUpdate(generateSql.toString());
                if (updateResult > 0)
                {
                    JSONObject moduleJson = this.queryModuleByAppIdAndBean(applicationId, moduleBean, companyId);
                    log.error("时间" + System.currentTimeMillis() + " 名称 " + moduleJson.getString("chinese_name"));
                    // 创建文件夹
                    JSONObject filesGenerate = new JSONObject();
                    filesGenerate.put("type", "1");
                    filesGenerate.put("chinese_name", null == moduleJson ? "No module name!" : moduleJson.getString("chinese_name"));// 模块名称
                    filesGenerate.put("english_name", moduleBean);// 模块chinesename
                    filesGenerate.put("id", moduleId);// 模块id
                    filesGenerate.put("parent_id", applicationId);// 应用id
                    fileLibraryAppService.savaLibrary(token, filesGenerate);
                }
                generateSql.setLength(0);
                map.put(jsonObject.getString("english_name"), moduleBean);
                jsonObject.put("moduleId", moduleId);
                jsonObject.put("moduleBean", moduleBean);
                result.add(jsonObject);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return result;
    }
    
    /**
     * @param moduleId
     * @param moduleTemplateId
     * @param companyId
     * @param employeeId
     * @return
     * @Description:生成菜单
     */
    private boolean generateModuleMenu(Integer moduleId, Long moduleTemplateId, Long companyId, Long employeeId)
    {
        try
        {
            String applicationModuleSubmenuTable = DAOUtil.getTableName("application_module_submenu", companyId);
            // 根据模块查询菜单模版数据
            StringBuilder generateSql = new StringBuilder();
            generateSql.append("select id template_module_menu_id from application_template_module_menu where template_module_id = ").append(moduleTemplateId);
            generateSql.append(" and del_status = 0");
            List<JSONObject> moduleMenuList = DAOUtil.executeQuery4JSON(generateSql.toString());
            
            if (null != moduleMenuList && moduleMenuList.size() > 0)
            {
                for (JSONObject jsonObject : moduleMenuList)
                {
                    long moduleMenueTemplateId = jsonObject.getLongValue("template_module_menu_id");
                    long moduleMenuId = BusinessDAOUtil.getNextval4Table("application_module_submenu", companyId.toString());
                    generateSql.setLength(0);
                    // 生成菜单数据
                    generateSql.append("insert into ");
                    generateSql.append(applicationModuleSubmenuTable);
                    generateSql.append(
                        "(id, module_id, name, high_where, type, del_status, topper, employee_id, allot_employee, allot_employee_v, target_lable, personnel_create_by, datetime_create_time, personnel_modify_by, datetime_modify_time)");
                    generateSql.append("select ");
                    generateSql.append(moduleMenuId).append(", ");
                    generateSql.append(moduleId);
                    generateSql.append(", name, high_where, type, 0, topper, employee_id, allot_employee, allot_employee_v, target_lable, ");
                    generateSql.append(employeeId).append(", ");
                    generateSql.append(System.currentTimeMillis()).append(", ");
                    generateSql.append(employeeId).append(", ");
                    generateSql.append(System.currentTimeMillis());
                    generateSql.append(" from application_template_module_menu where template_module_id = ").append(moduleTemplateId);
                    generateSql.append(" and del_status = 0 and id = ").append(moduleMenueTemplateId);
                    int copyResult = DAOUtil.executeUpdate(generateSql.toString());
                    if (copyResult < 1)
                    {
                        return false;
                    }
                    // 生成菜单规则数据
                    this.generateModuleMenuRule(moduleMenueTemplateId, moduleMenuId, companyId, employeeId);
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * @param moduleMenuList
     * @param companyId
     * @param employeeId
     * @return
     * @Description:生成菜单规则数据
     */
    private void generateModuleMenuRule(Long moduleMenueTemplateId, Long moduleMenuId, Long companyId, Long employeeId)
    {
        try
        {
            String submenuRuleTable = DAOUtil.getTableName("submenu_rule", companyId);
            // 生成菜单规则数据
            StringBuilder copySql = new StringBuilder();
            copySql.append("insert into ");
            copySql.append(submenuRuleTable);
            copySql.append(
                "(submenu_id, field_label, field_value, operator_label, operator_value, result_label, result_value, personnel_create_by, datetime_create_time, personnel_modify_by, datetime_modify_time)");
            copySql.append("select ");
            copySql.append(moduleMenuId);
            copySql.append(", field_label, field_value, operator_label, operator_value, result_label, result_value, ");
            copySql.append(employeeId).append(", ");
            copySql.append(System.currentTimeMillis()).append(", ");
            copySql.append(employeeId).append(", ");
            copySql.append(System.currentTimeMillis());
            copySql.append(" from application_template_submenu_rule where template_module_menu_id = ").append(moduleMenueTemplateId);
            
            int copyResult = DAOUtil.executeUpdate(copySql.toString());
            if (copyResult < 1)
            {
                System.err.println("该菜单下无规则");
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
    /**
     * @param moduleBean
     * @param moduleTemplateId
     * @param companyId
     * @param map
     * @param applicationId
     * @return
     * @Description:生成页面布局
     */
    private boolean generateModuleLayout(String newModuleBean, String moduleTemplateId, String companyId, Map<String, String> map, String applicationId)
    {
        try
        {
            // 组装生成条件
            Document generateWhere = new Document();
            generateWhere.put("companyId", companyId);
            generateWhere.put("moduleTemplateId", moduleTemplateId);
            boolean generateResult = LayoutUtil
                .generateModuleLayoutDocment(Constant.CUSTOMIZED_COLLECTION_CENTER, Constant.CUSTOMIZED_COLLECTION, newModuleBean, generateWhere, map, companyId, applicationId);
            if (!generateResult)
            {
                return false;
            }
            
            generateWhere.put("layoutState", Constant.LAYOUT_TYPE_ENABLE);
            JSONObject moduleJson = LayoutUtil.findDoc(generateWhere, Constant.CUSTOMIZED_COLLECTION_CENTER);
            if (null != moduleJson)
            {
                moduleJson.put("bean", newModuleBean);
                // 创建业务表,生成新模块数据表
                String sql = JSONParser4SQL.getCreateSql(moduleJson, StringUtil.isEmpty(companyId) ? "" : companyId);
                DAOUtil.executeUpdate(sql);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * @param moduleBean
     * @param moduleTemplateId
     * @param companyId
     * @return
     * @Description:生成列表显示字段(含PC端和APP端)
     */
    private boolean generateModuleListFieldLayout(String moduleBean, String moduleTemplateId, String companyId)
    {
        try
        {
            // 组装生成条件
            Document generateWhere = new Document();
            generateWhere.put("companyId", companyId);
            generateWhere.put("moduleTemplateId", moduleTemplateId);
            boolean generateResult = LayoutUtil.generateDocment(Constant.LIST_FIELDS_COLLECTION_CENTER, Constant.LIST_FIELDS_COLLECTION, moduleBean, generateWhere, companyId);
            if (!generateResult)
            {
                return false;
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * 
     * @param moduleBean
     * @param moduleTemplateId
     * @param companyId
     * @param map
     * @param applicationId
     * @return
     * @Description: 生成模块关联关系布局
     */
    private boolean generateRelationFieldLayout(String moduleBean, String moduleTemplateId, String companyId, Map<String, String> map)
    {
        try
        {
            // 组装生成条件
            Document generateWhere = new Document();
            generateWhere.put("companyId", companyId);
            generateWhere.put("moduleTemplateId", moduleTemplateId);
            boolean generateResult =
                LayoutUtil.generateRelationDocment(Constant.RELATION_COLLECTION_CENTER, Constant.RELATION_COLLECTION, moduleBean, generateWhere, map, companyId);
            if (!generateResult)
            {
                return false;
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * @param moduleBean
     * @param moduleTemplateId
     * @param companyId
     * @param map
     * @return
     * @Description:生成字段依赖(含关联映射、关联依赖、选项字段依赖、选项字段控制)
     */
    private boolean generateFieldRelyonLayout(String moduleBean, String moduleTemplateId, String companyId, Map<String, String> map)
    {
        try
        {
            // 组装生成条件
            Document generateWhere = new Document();
            generateWhere.put("companyId", companyId);
            generateWhere.put("moduleTemplateId", moduleTemplateId);
            
            // 生成关联映射
            boolean generateRelationMappedResult =
                LayoutUtil.generateMappingDocment(Constant.MAPPING_COLLECTION_CENTER, Constant.MAPPING_COLLECTION, moduleBean, generateWhere, map);
            // 生成关联依赖
            boolean generateRelationRelyResult = LayoutUtil.generateDocment(Constant.RELYON_COLLECTION_CENTER, Constant.RELYON_COLLECTION, moduleBean, generateWhere, companyId);
            // 生成选项字段依赖
            boolean generateOptionFieldRelyResult =
                LayoutUtil.generateDocment(Constant.PICKUPLIST_RELYON_COLLECTION_CENTER, Constant.PICKUPLIST_RELYON_COLLECTION, moduleBean, generateWhere, companyId);
            // 生成选项字段控制
            boolean generateOptionFieldControlResult =
                LayoutUtil.generateDocment(Constant.PICKUPLIST_CONTROL_COLLECTION_CENTER, Constant.PICKUPLIST_CONTROL_COLLECTION, moduleBean, generateWhere, companyId);
            
            if (!generateRelationMappedResult || !generateRelationRelyResult || !generateOptionFieldRelyResult || !generateOptionFieldControlResult)
            {
                return false;
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * @param moduleBean
     * @param moduleTemplateId
     * @param companyId
     * @param map
     * @return
     * @Description:生成字段转换
     */
    private boolean generateFieldConvertLayout(String moduleBean, String moduleTemplateId, String companyId, Map<String, String> map)
    {
        try
        {
            // 组装生成条件
            Document generateWhere = new Document();
            generateWhere.put("companyId", companyId);
            generateWhere.put("moduleTemplateId", moduleTemplateId);
            boolean generateResult = LayoutUtil.generateFieldDocment(Constant.FIELD_COLLECTION_CENTER, Constant.FIELD_COLLECTION, moduleBean, generateWhere, map, companyId);
            if (!generateResult)
            {
                return false;
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * @param moduleTemplateId
     * @param moduleBean
     * @param companyId
     * @param employeeId
     * @return
     * @Description:生成数据共享(含共享规则和规则详情)
     */
    private boolean generateModuleDataShare(Long moduleTemplateId, String moduleBean, Long companyId, Long employeeId)
    {
        try
        {
            String moduleShareSettingTable = DAOUtil.getTableName("module_share_setting", companyId);
            // 根据模块查询共享模版数据
            StringBuilder generateSql = new StringBuilder();
            generateSql.append("select id template_share_id from application_template_share where template_module_id = ").append(moduleTemplateId);
            generateSql.append(" and del_status = 0");
            List<JSONObject> moduleMenuList = DAOUtil.executeQuery4JSON(generateSql.toString());
            
            if (null != moduleMenuList && moduleMenuList.size() > 0)
            {
                for (JSONObject jsonObject : moduleMenuList)
                {
                    long shareTemplateId = jsonObject.getLongValue("template_share_id");
                    long moduleShareId = BusinessDAOUtil.getNextval4Table("module_share_setting", companyId.toString());
                    generateSql.setLength(0);
                    // 生成共享数据
                    generateSql.append("insert into ");
                    generateSql.append(moduleShareSettingTable);
                    generateSql.append(
                        "(id, title, bean_name, employee_id, condition, high_where, access_permissions, allot_employee, allot_employee_v, target_lable, personnel_create_by, datetime_create_time, personnel_modify_by, datetime_modify_time, del_status)");
                    generateSql.append("select ");
                    generateSql.append(moduleShareId);
                    generateSql.append(", title, '");
                    generateSql.append(moduleBean).append("', ");
                    generateSql.append(employeeId);
                    generateSql.append(", condition, high_where, access_permissions, allot_employee, allot_employee_v, target_lable, ");
                    generateSql.append(employeeId).append(", ");
                    generateSql.append(System.currentTimeMillis()).append(", ");
                    generateSql.append(employeeId).append(", ");
                    generateSql.append(System.currentTimeMillis()).append(", 0");
                    generateSql.append(" from application_template_share where template_module_id = ").append(moduleTemplateId);
                    generateSql.append(" and del_status = 0 and id = ").append(shareTemplateId);
                    int copyResult = DAOUtil.executeUpdate(generateSql.toString());
                    if (copyResult < 1)
                    {
                        return false;
                    }
                    // 生成共享详情数据
                    this.generateModuleDataShareDetail(shareTemplateId, moduleShareId, companyId, employeeId);
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * @param moduleMenueTemplateId
     * @param moduleMenuId
     * @param companyId
     * @param employeeId
     * @Description:生成共享详情数据
     */
    private void generateModuleDataShareDetail(Long shareTemplateId, Long moduleShareId, Long companyId, Long employeeId)
    {
        try
        {
            String shareDetailTable = DAOUtil.getTableName("module_share_setting_detail", companyId);
            // 生成共享详情数据
            StringBuilder copySql = new StringBuilder();
            copySql.append("insert into ");
            copySql.append(shareDetailTable);
            copySql.append("(share_id, field_value, field_label, operator_value, operator_label, result_value, result_label)");
            copySql.append("select ");
            copySql.append(moduleShareId);
            copySql.append(", field_value, field_label, operator_value, operator_label, result_value, result_label ");
            copySql.append(" from application_template_share_detail where template_share_id = ").append(shareTemplateId);
            
            int copyResult = DAOUtil.executeUpdate(copySql.toString());
            if (copyResult < 1)
            {
                System.err.println("该模块下无共享规则详情");
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
    /**
     * @param moduleTemplateId
     * @param moduleBean
     * @param companyId
     * @param employeeId
     * @param map
     * @return
     * @Description:生成自动分配
     */
    private boolean generateAutoAllot(Long moduleTemplateId, String moduleBean, Long companyId, Long employeeId, Map<String, String> map)
    {
        try
        {
            String automationTable = DAOUtil.getTableName("automation", companyId);
            // 根据模块查询自动分配规则模版数据
            StringBuilder generateSql = new StringBuilder();
            generateSql.append("select   * from application_template_automation where template_module_id = ").append(moduleTemplateId);
            generateSql.append(" and del_status = 0");
            List<JSONObject> autoAllotList = DAOUtil.executeQuery4JSON(generateSql.toString());
            
            if (null != autoAllotList && autoAllotList.size() > 0)
            {
                List<List<Object>> batchValues = new ArrayList<>();
                for (JSONObject jsonObject : autoAllotList)
                {
                    List<Object> model = new ArrayList<>();
                    long automationTemplateId = jsonObject.getLongValue("id");
                    long automationId = BusinessDAOUtil.getNextval4Table("automation", companyId.toString());
                    model.add(automationId);
                    model.add(moduleBean);
                    model.add(jsonObject.getString("title"));
                    model.add(jsonObject.getString("triggers"));
                    model.add(jsonObject.getString("remark"));
                    model.add(jsonObject.getString("condition"));
                    model.add(jsonObject.getString("query_condition"));
                    
                    JSONObject array = JSONObject.parseObject(jsonObject.getString("query_parameter"));
                    if (null != array.get("operation"))
                    {
                        // 解析条件
                        JSONArray arrList = JSONArray.parseArray(array.getString("operation"));
                        for (int i = 0; i < arrList.size(); i++)
                        {
                            JSONObject json = arrList.getJSONObject(i);
                            if (json.getInteger("type") == Constant.CURRENCY_ZERO)// 更新处理
                            {
                                JSONObject jsonObject1 = JSONObject.parseObject(json.getString("module"));
                                // 替换更新目标新bean
                                jsonObject1.put("bean", map.get(jsonObject1.getString("bean")));
                                json.put("module", jsonObject1);
                            }
                        }
                        array.replace("operation", arrList);
                    }
                    model.add(array.toString());
                    model.add(employeeId);
                    model.add(System.currentTimeMillis());
                    batchValues.add(model);
                    
                    // 生成工作流自动化规则详情数据
                    this.generateAutoAllotDetail(automationTemplateId, automationId, companyId, employeeId, map);
                }
                if (batchValues.size() > 0)
                {
                    StringBuilder insertBuilder = new StringBuilder();
                    insertBuilder.append("insert into ")
                        .append(automationTable)
                        .append("(id,bean,title,triggers,remark,condition,query_condition,query_parameter,create_by,create_time)")
                        .append(" values(?,?,?,?,?,?,?,?,?,?)");
                    int count = DAOUtil.executeUpdate(insertBuilder.toString(), batchValues, 1000);
                    if (count <= 0)
                    {
                        return false;
                    }
                }
            }
            
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * @param shareTemplateId
     * @param moduleShareId
     * @param companyId
     * @param employeeId
     * @param map
     * @Description:生成工作流自动化规则详情数据
     */
    private void generateAutoAllotDetail(Long automationTemplateId, Long automationId, Long companyId, Long employeeId, Map<String, String> map)
    {
        try
        {
            String handleTable = DAOUtil.getTableName("automation_handle", companyId);
            StringBuilder queryBulider = new StringBuilder();
            queryBulider.append("select * from application_template_automation_handle_detail where template_automation_id =").append(automationTemplateId);
            List<JSONObject> jsonList = DAOUtil.executeQuery4JSON(queryBulider.toString());
            if (null != jsonList && jsonList.size() > 0)
            {
                List<List<Object>> batchValues = new ArrayList<>();
                for (JSONObject jsonObject : jsonList)
                {
                    List<Object> model = new ArrayList<>();
                    model.add(automationId);
                    model.add(jsonObject.getString("type"));
                    JSONObject json = JSONObject.parseObject(jsonObject.getString("content"));
                    if (jsonObject.getInteger("type") == Constant.CURRENCY_ZERO)// 更新处理
                    {
                        JSONObject jsonObject1 = JSONObject.parseObject(json.getString("module"));
                        // 替换更新目标新bean
                        jsonObject1.replace("bean", map.get(jsonObject1.getString("bean")));
                        json.put("module", jsonObject1);
                    }
                    
                    model.add(json.toString());
                    batchValues.add(model);
                }
                
                if (batchValues.size() > 0)
                {
                    // 生成工作流自动化规则详情数据
                    StringBuilder copySql = new StringBuilder();
                    copySql.append("insert into ").append(handleTable).append("(automation_id, type, content)").append(" values(?,?,?)");
                    int count = DAOUtil.executeUpdate(copySql.toString(), batchValues, 1000);
                    if (count <= 0)
                    {
                        System.err.println("该模块下无工作流自动化规则详情");
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
    /**
     * @param moduleTemplateId
     * @param moduleBean
     * @param companyId
     * @param employeeId
     * @return
     * @Description:生成自动标记颜色数据
     */
    private boolean generateAutoColor(Long moduleTemplateId, String moduleBean, Long companyId, Long employeeId)
    {
        try
        {
            // 根据模块查询自动标记颜色规则模版数据
            StringBuilder generateSql = new StringBuilder();
            generateSql.append("select id template_auto_color_id from application_template_auto_color where template_module_id = ").append(moduleTemplateId);
            generateSql.append(" and del_status = 0");
            List<JSONObject> autoColorList = DAOUtil.executeQuery4JSON(generateSql.toString());
            
            if (null != autoColorList && autoColorList.size() > 0)
            {
                String ruleColorTable = DAOUtil.getTableName("rule_colour", companyId);
                for (JSONObject jsonObject : autoColorList)
                {
                    long autoColorTemplateId = jsonObject.getLongValue("template_auto_color_id");
                    long autoColorId = BusinessDAOUtil.getNextval4Table("rule_colour", companyId.toString());
                    generateSql.setLength(0);
                    // 生成自动标记颜色规则数据
                    generateSql.append("insert into ");
                    generateSql.append(ruleColorTable);
                    generateSql.append("(id, title, high_where, bean, condition, colour, personnel_create_by, datetime_create_time)");
                    generateSql.append("select ");
                    generateSql.append(autoColorId).append(", title, high_where, '");
                    generateSql.append(moduleBean).append("', condition, colour, ");
                    generateSql.append(employeeId).append(", ");
                    generateSql.append(System.currentTimeMillis());
                    generateSql.append(" from application_template_auto_color where template_module_id = ").append(moduleTemplateId);
                    generateSql.append(" and del_status = 0 and id = ").append(autoColorTemplateId);
                    int copyResult = DAOUtil.executeUpdate(generateSql.toString());
                    if (copyResult < 1)
                    {
                        return false;
                    }
                    // 生成自动标记颜色规则详情数据
                    this.generateAutoColorDetail(autoColorTemplateId, autoColorId, companyId, employeeId);
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * @param autoAllotTemplateId
     * @param autoAllotId
     * @param companyId
     * @param employeeId
     * @Description:生成自动标记颜色规则详情数据
     */
    private void generateAutoColorDetail(Long autoColorTemplateId, Long autoColorId, Long companyId, Long employeeId)
    {
        try
        {
            String ruleColorDetailTable = DAOUtil.getTableName("rule_colour_detail", companyId);
            // 生成自动分配规则详情数据
            StringBuilder copySql = new StringBuilder();
            copySql.append("insert into ");
            copySql.append(ruleColorDetailTable);
            copySql.append("(rule_colour_id, field_value, field_label, operator_value, operator_label, result_value, result_label)");
            copySql.append("select ");
            copySql.append(autoColorId);
            copySql.append(
                ", field_value, field_label, operator_value, operator_label, result_value, result_label from application_template_auto_color_detail where template_auto_color_id = ");
            copySql.append(autoColorTemplateId);
            
            int copyResult = DAOUtil.executeUpdate(copySql.toString());
            if (copyResult < 1)
            {
                System.err.println("该模块下无自动标记颜色规则详情");
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
    /**
     * @param applicationTemplateId
     * @param employeeId
     * @return
     * @Description:保存安装记录
     */
    private boolean saveInstallRecord(Integer applicationTemplateId, Long employeeId)
    {
        try
        {
            StringBuilder insertSql = new StringBuilder();
            insertSql.append("insert into application_template_install(template_id, install_by, install_time) values(");
            insertSql.append(applicationTemplateId).append(", ");
            insertSql.append(employeeId).append(", ");
            insertSql.append(System.currentTimeMillis()).append(")");
            
            int insertResult = DAOUtil.executeUpdate(insertSql.toString());
            if (insertResult < 1)
            {
                return false;
            }
            
            StringBuilder editSql = new StringBuilder();
            editSql.append(" update application_template set  download_number = download_number+1 where id = " + applicationTemplateId);
            int count = DAOUtil.executeUpdate(editSql.toString());
            if (count < 1)
            {
                return false;
            }
            
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * @param applicationId
     * @param info
     * @return
     * @Description:获取应用模版记录
     */
    private JSONObject queryApplicationTemplate(Long applicationId, InfoVo info)
    {
        try
        {
            StringBuilder querySql = new StringBuilder();
            querySql.append("select * from application_template where application_id = ").append(applicationId).append(" and company_id = ").append(info.getCompanyId());
            return DAOUtil.executeQuery4FirstJSON(querySql.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * @param templateId
     * @return
     * @Description:获取应用模版记录
     */
    private List<JSONObject> queryModuleTemplate(Long templateId)
    {
        List<JSONObject> resultList = null;
        try
        {
            StringBuilder querySql = new StringBuilder();
            querySql.append("select * from application_template_module where template_id = ").append(templateId);
            resultList = DAOUtil.executeQuery4JSON(querySql.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return resultList;
    }
    
    /**
     * @param moduleTemplateId
     * @return
     * @Description:移除自动标记颜色模版
     */
    
    private boolean removeAutoColorTemplate(Long moduleTemplateId)
    {
        try
        {
            StringBuilder removeSql = new StringBuilder();
            // 移除自动标记颜色详情模版
            removeSql.append("delete from application_template_auto_color_detail where template_auto_color_id in(");
            removeSql.append("select id from application_template_auto_color where template_module_id = ").append(moduleTemplateId);
            removeSql.append(")");
            
            DAOUtil.executeUpdate(removeSql.toString());
            
            // 移除自动标记颜色模版
            removeSql.setLength(0);
            removeSql.append("delete from application_template_auto_color where template_module_id = ").append(moduleTemplateId);
            DAOUtil.executeUpdate(removeSql.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * @param moduleTemplateId
     * @return
     * @Description:移除自动分配模版
     */
    
    private boolean removeAutoAllotTemplate(Long moduleTemplateId)
    {
        try
        {
            StringBuilder removeSql = new StringBuilder();
            // 移除自动分配详情模版
            removeSql.append("delete from application_template_automation_handle_detail where template_automation_id in(");
            removeSql.append("select id from application_template_automation where template_module_id = ").append(moduleTemplateId);
            removeSql.append(")");
            DAOUtil.executeUpdate(removeSql.toString());
            
            // 移除自动分配模版
            removeSql.setLength(0);
            removeSql.append("delete from application_template_automation where template_module_id = ").append(moduleTemplateId);
            DAOUtil.executeUpdate(removeSql.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * @param moduleTemplateId
     * @return
     * @Description:移除数据共享模版
     */
    
    private boolean removeDataShareTemplate(Long moduleTemplateId)
    {
        try
        {
            StringBuilder removeSql = new StringBuilder();
            // 移除自动分配详情模版
            removeSql.append("delete from application_template_auto_allot_detail where template_auto_allot_id in(");
            removeSql.append("select id from application_template_share where template_module_id = ").append(moduleTemplateId);
            removeSql.append(")");
            DAOUtil.executeUpdate(removeSql.toString());
            
            // 移除自动分配模版
            removeSql.setLength(0);
            removeSql.append("delete from application_template_auto_allot where template_module_id = ").append(moduleTemplateId);
            DAOUtil.executeUpdate(removeSql.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * @param moduleBean
     * @param moduleTemplateId
     * @param companyId
     * @return
     * @Description:移除字段转换模版
     */
    
    private boolean removeFieldConvertLayoutTemplate(String moduleTemplateId, String companyId)
    {
        try
        {
            // 组装移除条件
            Document removeWhere = new Document();
            removeWhere.put("moduleTemplateId", moduleTemplateId);
            removeWhere.put("companyId", companyId);
            boolean copyResult = LayoutUtil.removeDoc(removeWhere, Constant.FIELD_COLLECTION_CENTER);
            if (!copyResult)
            {
                return false;
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * @param moduleTemplateId
     * @param companyId
     * @return
     * @Description:移除字段依赖模版(含关联映射、关联依赖、选项字段依赖、选项字段控制)
     */
    
    private boolean removeFieldRelyonLayoutTemplate(String moduleTemplateId, String companyId)
    {
        try
        {
            // 组装移除条件
            Document removeWhere = new Document();
            removeWhere.put("moduleTemplateId", moduleTemplateId);
            removeWhere.put("companyId", companyId);
            
            // 移除关联映射
            boolean removeRelationMappedResult = LayoutUtil.removeDoc(removeWhere, Constant.MAPPING_COLLECTION_CENTER);
            // 移除关联依赖
            boolean removeRelationRelyResult = LayoutUtil.removeDoc(removeWhere, Constant.RELYON_COLLECTION_CENTER);
            // 移除选项字段依赖
            boolean removeOptionFieldRelyResult = LayoutUtil.removeDoc(removeWhere, Constant.PICKUPLIST_RELYON_COLLECTION_CENTER);
            // 移除选项字段控制
            boolean removeOptionFieldControlResult = LayoutUtil.removeDoc(removeWhere, Constant.PICKUPLIST_CONTROL_COLLECTION_CENTER);
            
            if (!removeRelationMappedResult || !removeRelationRelyResult || !removeOptionFieldRelyResult || !removeOptionFieldControlResult)
            {
                return false;
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * @param moduleTemplateId
     * @param companyId
     * @return
     * @Description:移除列表显示字段模版(含PC端和APP端)
     */
    
    private boolean removeListFieldsLayoutTemplate(String moduleTemplateId, String companyId)
    {
        boolean removeResult = false;
        try
        {
            // 组装移除条件
            Document removeWhere = new Document();
            removeWhere.put("moduleTemplateId", moduleTemplateId);
            removeWhere.put("companyId", companyId);
            removeResult = LayoutUtil.removeDoc(removeWhere, Constant.LIST_FIELDS_COLLECTION_CENTER);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return removeResult;
    }
    
    /**
     * @param moduleTemplateId
     * @param companyId
     * @return
     * @Description:移除页面布局模版（含已使用字段和未使用字段）
     */
    
    private boolean removeFormLayoutTemplate(String moduleTemplateId, String companyId)
    {
        boolean removeResult = false;
        try
        {
            // 组装移除条件
            Document removeWhere = new Document();
            removeWhere.put("moduleTemplateId", moduleTemplateId);
            removeWhere.put("companyId", companyId);
            removeResult = LayoutUtil.removeDoc(removeWhere, Constant.CUSTOMIZED_COLLECTION_CENTER);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return removeResult;
    }
    
    /**
     * @param moduleTemplateId
     * @return
     * @Description:移除模块菜单模版（含菜单规则）
     */
    
    private boolean removeMenuTemplate(Long moduleTemplateId)
    {
        try
        {
            StringBuilder removeSql = new StringBuilder();
            // 移除模菜单规则模版
            removeSql.append("delete from application_template_submenu_rule where template_module_menu_id in(");
            removeSql.append("select id from application_template_module_menu where template_module_id = ").append(moduleTemplateId);
            removeSql.append(")");
            DAOUtil.executeUpdate(removeSql.toString());
            
            // 移除模块菜单模版
            removeSql.setLength(0);
            removeSql.append("delete from application_template_module_menu where template_module_id = ").append(moduleTemplateId);
            DAOUtil.executeUpdate(removeSql.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * @param moduleTemplateId
     * @return
     * @Description:移除模块模版
     */
    
    private boolean removeModuleTemplate(Long moduleTemplateId)
    {
        try
        {
            StringBuilder removeSql = new StringBuilder();
            // 移除模块模版
            removeSql.append("delete from application_template_module where id = ").append(moduleTemplateId);
            DAOUtil.executeUpdate(removeSql.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * @param templateId
     * @return
     * @Description:移除应用模版
     */
    
    private boolean removeApplicationTemplate(Long templateId)
    {
        try
        {
            StringBuilder removeSql = new StringBuilder();
            // 移除应用模版
            removeSql.append("delete from application_template where id = ").append(templateId);
            DAOUtil.executeUpdate(removeSql.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * @param applicationId
     * @param companyId
     * @return JSONObject
     * @Description:根据应用id获取应用信息
     */
    private JSONObject queryApplicationByAppIdAndBean(Long applicationId, Long companyId)
    {
        try
        {
            String applicationTable = DAOUtil.getTableName("application", companyId);
            
            StringBuilder querySql = new StringBuilder();
            querySql.append("select * from ").append(applicationTable);
            querySql.append(" where id = ").append(applicationId);
            return DAOUtil.executeQuery4FirstJSON(querySql.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * @param applicationId
     * @param moduleBean
     * @param companyId
     * @return
     * @Description:根据应用id、模块bean获取模块信息
     */
    private JSONObject queryModuleByAppIdAndBean(Long applicationId, String moduleBean, Long companyId)
    {
        try
        {
            String moduleTable = DAOUtil.getTableName("application_module", companyId);
            
            StringBuilder querySql = new StringBuilder();
            querySql.append("select * from ").append(moduleTable);
            querySql.append(" where application_id = ").append(applicationId);
            querySql.append(" and english_name = '").append(moduleBean).append("'");
            return DAOUtil.executeQuery4FirstJSON(querySql.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    
    private boolean generateBusinessTable()
    {
        
        return true;
    }
    
    /**
     * 根据模板应用ID获取模板模块
     */
    @Override
    public List<JSONObject> queryTemplateModuleByApplicationId(Map<String, String> reqMap)
    {
        if (!reqMap.containsKey("template_application_id"))
        {
            return null;
        }
        try
        {
            StringBuilder queryModuleSql = new StringBuilder();
            queryModuleSql.append("select * from application_template_module where template_id = ").append(reqMap.get("template_application_id"));
            return DAOUtil.executeQuery4JSON(queryModuleSql.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * 根据上传应用模板ID获取模板详情
     */
    @Override
    public JSONObject getTemplateById(Map<String, String> reqMap)
    {
        StringBuilder queryModuleSql = new StringBuilder();
        JSONObject object = new JSONObject();
        InfoVo info = TokenMgr.obtainInfo(reqMap.get("token"));
        try
        {
            queryModuleSql.append(
                "select t1.*,COALESCE(t2.count, 0) as install_totals  from application_template t1 left join (select template_id, count(id) from application_template_install group by template_id) t2 on t1.id = t2.template_id  left join application_template_comment c on t1.id = c.template_id    where t1.id= ")
                .append(reqMap.get("template_id"));
            object = DAOUtil.executeQuery4FirstJSON(queryModuleSql.toString());
            StringBuilder querySql =
                new StringBuilder(" select  count(*) from application_template_install  where template_id = " + reqMap.get("template_id")).append(" and install_by = ")
                    .append(info.getEmployeeId());
            int num = DAOUtil.executeCount(querySql.toString());
            StringBuilder countSql = new StringBuilder(" select COALESCE(AVG(star_level), 0)  from application_template_comment  where template_id = " + reqMap.get("template_id"));
            Object objectAvg = DAOUtil.executeQuery4Object(countSql.toString());
            object.put("is_install", num);
            object.put("star_level", objectAvg);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return object;
    }
    
    /**
     * 保存应用评论
     */
    @Override
    public ServiceResult<String> savaApplicationComment(Map<String, Object> reqMap)
    {
        
        StringBuilder builder = new StringBuilder();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(reqMap.get("token").toString());
            String commentTable = DAOUtil.getTableName("application_template_comment", "");
            builder.append("insert into ")
                .append(commentTable)
                .append(" (template_id,star_level,content,create_by,create_time) values (")
                .append(reqMap.get("template_id"))
                .append(",")
                .append(reqMap.get("number"))
                .append(",");
            if (reqMap.get("content") == null || reqMap.get("content").toString().isEmpty() || "[]".equals(reqMap.get("content")))
            {
                builder.append("null");
            }
            else
            {
                builder.append("'").append(reqMap.get("content").toString().replace("'", "''")).append("'");
            }
            builder.append(",").append(info.getEmployeeId()).append(",").append(System.currentTimeMillis()).append(")");
            int sum = DAOUtil.executeUpdate(builder.toString());
            if (sum <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 获取当前上传应用评论列表
     */
    @Override
    public JSONObject queryApplicationCommentById(Map<String, String> reqMap)
    {
        StringBuilder builder = new StringBuilder();
        JSONObject jsonList = new JSONObject();
        InfoVo info = TokenMgr.obtainInfo(reqMap.get("token"));
        
        String commentTable = DAOUtil.getTableName("application_template_comment", "");
        String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
        
        try
        {
            builder.append("select c.*,e.employee_name from  ")
                .append(commentTable)
                .append(" c join ")
                .append(employeeTable)
                .append(" e on c.create_by = e.id where c.template_id = ")
                .append(reqMap.get("template_id"))
                .append(" order by  c.create_time desc ");
            int pageSize = Integer.parseInt(reqMap.get("pageSize").toString());
            int pageNum = Integer.parseInt(reqMap.get("pageNum").toString());
            jsonList = BusinessDAOUtil.getTableDataListAndPageInfo(builder.toString(), pageSize, pageNum);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        
        return jsonList;
    }
    
    /**
     * 获取当前预览布局
     */
    @Override
    public List<JSONObject> queryApplicationLayoutById(Map<String, String> reqMap)
    {
        List<JSONObject> list = new ArrayList<JSONObject>();
        Document doc = new Document();
        doc.put("moduleTemplateId", reqMap.get("template_id"));
        MongoCursor<Document> mcDoc = mongoDB.find(Constant.CUSTOMIZED_COLLECTION_CENTER, doc);
        while (mcDoc.hasNext())
        {
            JSONObject findJsonInfo = JSONObject.parseObject(mcDoc.next().toJson());
            list.add(findJsonInfo);
        }
        return list;
    }
    
    /**
     * 我的应用删除
     */
    @Override
    public ServiceResult<String> delApplication(Map<String, Object> reqMap)
    {
        StringBuilder builder = new StringBuilder();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            String templateTable = DAOUtil.getTableName("application_template", "");
            builder.append(" update  ")
                .append(templateTable)
                .append(" set by_status = ")
                .append(Constant.CURRENCY_ONE)
                .append(" where id in (")
                .append(reqMap.get("template_id"))
                .append(")  and upload_status = ")
                .append(Constant.CURRENCY_ONE);
            int sum = DAOUtil.executeUpdate(builder.toString());
            if (sum <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 我的上传应用列表
     */
    @Override
    public JSONObject queryOwnTemplateList(Map<String, Object> reqMap)
    {
        StringBuilder querySql = new StringBuilder();
        JSONObject resultList = new JSONObject();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(reqMap.get("token").toString());
            Long employeeId = info.getEmployeeId();
            querySql.append(
                "select t1.id template_id, t1.template_name,t1.price, t1.icon, t1.create_time,t1.upload_user,t1.upload_status,t1.upload_describe,t1.view_content from application_template t1  where 1=1 ");
            querySql.append(" and t1.create_by  = ").append(employeeId).append(" and t1.by_status = ").append(Constant.CURRENCY_ZERO);
            querySql.append(" order by   t1.create_time desc");
            int pageSize = Integer.parseInt(reqMap.get("pageSize").toString());
            int pageNum = Integer.parseInt(reqMap.get("pageNum").toString());
            resultList = BusinessDAOUtil.getTableDataListAndPageInfo(querySql.toString(), pageSize, pageNum);
        }
        catch (NumberFormatException e)
        {
            log.error(e.getMessage(), e);
        }
        return resultList;
    }
    
}
