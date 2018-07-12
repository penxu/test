package com.teamface.custom.service.report;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.QueryOperators;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.Constant;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.ReportUtil;
import com.teamface.common.util.StringUtil;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JSONParser4SQL;
import com.teamface.common.util.dao.JedisClusterHelper;
import com.teamface.common.util.dao.LayoutUtil;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.async.AsyncHandle;
import com.teamface.custom.async.thread.GenerateDataListFilterFields;
import com.teamface.custom.service.auth.ModuleDataAuthAppService;
import com.teamface.custom.service.workflow.WorkflowAppService;

/**
 * @Description:报表接口实现类
 * @author: Administrator
 * @date: 2018年2月28日 下午4:08:26
 * @version: 1.0
 */
@Service("reportAppService")
public class ReportAppServiceImpl implements ReportAppService
{
    private static Logger log = Logger.getLogger(ReportAppServiceImpl.class);
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    @Autowired
    ModuleDataAuthAppService moduleDataAuthAppService;
    
    @Autowired
    WorkflowAppService workflowAppService;
    
    @Autowired
    private InstrumentPanelAppService instrumentPanelAppService;
    
    /**
     * @param token
     * @param clientFlag
     * @return List
     * @Description:获取所有有权限的模块列表
     */
    @Override
    public JSONArray getModuleList(String token, String clientFlag)
    {
        try
        {
            JSONObject roleJson = moduleDataAuthAppService.getRoleByUser(token);
            if (roleJson == null || roleJson.get("id") == null)
            {
                return new JSONArray();
            }
            // 角色id.
            Integer roleId = roleJson.getInteger("id");
            // 获取模块列表
            JSONArray result = moduleDataAuthAppService.getModuleAuthByRole(token, roleId, null, clientFlag);
            return result;
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return new JSONArray();
    }
    
    /**
     * @param token
     * @return
     * @Description:获取模块数据源
     */
    @Override
    public List<JSONObject> getSourceModule(String token, String sourceModuleBean, String sourceModuleTitle)
    {
        try
        {
            // token解析
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            // 获取关联模块
            List<JSONObject> relationModule = LayoutUtil.getRelationsByBean(companyId.toString(), sourceModuleBean, sourceModuleTitle, null);
            
            // 当前模块选项
            JSONObject currentModule = new JSONObject();
            currentModule.put("bean", sourceModuleBean);
            currentModule.put("title", sourceModuleTitle);
            relationModule.add(0, currentModule);
            
            JSONObject processAttr = workflowAppService.getProcessAttributeByBean(sourceModuleBean, token);
            if (null != processAttr)
            {
                // 当前模块和审批中的模块选项
                JSONObject currentModuleAndNoApproval = new JSONObject();
                currentModuleAndNoApproval.put("bean", new StringBuilder(sourceModuleBean).append(",").append(sourceModuleBean).append("_approval").toString());
                currentModuleAndNoApproval.put("title", new StringBuilder(sourceModuleTitle).append("和审核中的").append(sourceModuleTitle).toString());
                relationModule.add(1, currentModuleAndNoApproval);
            }
            return relationModule;
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    
    /**
     * @param token
     * @param sourceModuleBean
     * @return
     * @Description:获取字段数据源
     */
    @Override
    public JSONObject getSourceField(String token, String relationModuleBeans)
    {
        try
        {
            // token解析
            InfoVo info = TokenMgr.obtainInfo(token);
            return LayoutUtil.getReportModulesFields(info.getCompanyId().toString(), info.getEmployeeId().toString(), relationModuleBeans, null);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return new JSONObject();
    }
    
    /**
     * @param params
     * @return ServiceResult
     * @Description:保存报表定义
     */
    @Override
    public ServiceResult<String> saveReport(Map<String, String> params)
    {
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        String reqJsonStr = params.get("reqJsonStr");
        String token = params.get("token");
        
        try
        {
            // token解析
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            Long employeeId = info.getEmployeeId();
            String repotTable = DAOUtil.getTableName("report", companyId);
            
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            
            boolean modifyData = false;
            Long dataId = reqJson.getLong("reportId");
            if (dataId != null)
            {// 编辑功能，先删后插
                Map<String, String> checkParams = new HashMap<String, String>();
                checkParams.put("token", token);
                checkParams.put("reportLabel", reqJson.getString("reportLabel"));
                checkParams.put("reportId", reqJson.getString("reportId"));
                int checkResult = checkReportNameExist(checkParams, true);
                if (checkResult > 0)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), "报表名称已存在！");
                    return serviceResult;
                }
                
                // 删除定义
                Document queryDoc = new Document();
                queryDoc.put("reportId", dataId.toString());
                queryDoc.put("companyId", companyId.toString());
                queryDoc.put("styleType", reqJson.getString("styleType"));
                LayoutUtil.removeDoc(queryDoc, Constant.REPORT_COLLECTION);
                
                modifyData = true;
            }
            else
            {
                dataId = BusinessDAOUtil.getNextval4Table("report", companyId.toString());
            }
            reqJson.put("reportId", dataId.toString());
            reqJson.put("companyId", companyId.toString());
            reqJson.remove("_id");
            
            // 保存报表定义
            Document saveDoc = new Document();
            saveDoc.putAll(reqJson);
            boolean saveReportLayout = LayoutUtil.saveDoc(saveDoc, Constant.REPORT_COLLECTION);
            if (!saveReportLayout)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            
            // 可见性
            JSONArray visibilityArr = reqJson.getJSONArray("visibility");
            StringBuilder shareUserSB = new StringBuilder();
            StringBuilder shareRoleSB = new StringBuilder();
            StringBuilder shareDepartmentSB = new StringBuilder();
            boolean companyFlag = false;
            for (Object shareObj : visibilityArr)
            {
                JSONObject shareJson = (JSONObject)shareObj;
                String shareId = shareJson.getString("id");
                String shareType = shareJson.getString("type");// 0部门，1人员，2角色，4全公司
                if ("0".equals(shareType))
                {
                    shareDepartmentSB.append(shareId).append(",");
                }
                else if ("1".equals(shareType))
                {
                    shareUserSB.append(shareId).append(",");
                }
                else if ("2".equals(shareType))
                {
                    shareRoleSB.append(shareId).append(",");
                }
                else if ("4".equals(shareType))
                {
                    companyFlag = true;
                }
            }
            
            if (modifyData)
            {
                StringBuilder modifySql = new StringBuilder();
                modifySql.append("update ").append(repotTable);
                modifySql.append(" set report_label = '").append(reqJson.getString("reportLabel").replace("'", "''"));
                modifySql.append("', share_company = '").append(companyFlag ? "1" : "0");
                modifySql.append("', report_type = '").append(reqJson.getString("reportType"));
                modifySql.append("', share_user = '");
                modifySql.append(shareUserSB.length() == 0 ? "" : shareUserSB.substring(0, shareUserSB.lastIndexOf(",")));
                modifySql.append("', share_role = '");
                modifySql.append(shareRoleSB.length() == 0 ? "" : shareRoleSB.substring(0, shareRoleSB.lastIndexOf(",")));
                modifySql.append("', share_department = '");
                modifySql.append(shareDepartmentSB.length() == 0 ? "" : shareDepartmentSB.substring(0, shareDepartmentSB.lastIndexOf(",")));
                modifySql.append("', modify_time = ").append(System.currentTimeMillis());
                modifySql.append(" where id = ").append(dataId);
                int mdfResult = DAOUtil.executeUpdate(modifySql.toString());
                if (mdfResult < 1)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                }
            }
            else
            {
                List<Object> values = new ArrayList<>();
                values.add(dataId);
                values.add(reqJson.getString("reportName"));
                values.add(reqJson.getString("reportLabel"));
                values.add(reqJson.getString("dataSourceName"));
                values.add(reqJson.getString("dataSourceLabel"));
                values.add(reqJson.getString("reportType"));
                values.add(companyFlag ? "1" : "0");
                values.add(shareUserSB.length() == 0 ? "" : shareUserSB.substring(0, shareUserSB.lastIndexOf(",")));
                values.add(shareRoleSB.length() == 0 ? "" : shareRoleSB.substring(0, shareRoleSB.lastIndexOf(",")));
                values.add(shareDepartmentSB.length() == 0 ? "" : shareDepartmentSB.substring(0, shareDepartmentSB.lastIndexOf(",")));
                values.add("0");
                values.add(employeeId);
                values.add(System.currentTimeMillis());
                values.add(employeeId);
                values.add(System.currentTimeMillis());
                // 保存报表列表数据
                StringBuilder insertSql = new StringBuilder();
                insertSql.append("insert into ").append(repotTable);
                insertSql.append("(id, report_name, report_label, data_source_name, data_source_label, report_type, share_company, share_user, share_role, share_department, ");
                insertSql.append(Constant.FIELD_DEL_STATUS).append(", ");
                insertSql.append("create_by, create_time, ");
                insertSql.append("modify_by, modify_time) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                int intResult = DAOUtil.executeUpdate(insertSql.toString(), values.toArray());
                if (intResult < 1)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                }
                
                values = new ArrayList<>();
                values.add(dataId);
                values.add(employeeId);
                values.add(System.currentTimeMillis());
                // 保存点击记录
                saveClickHist(companyId, values);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return serviceResult;
    }
    
    /**
     * @param token
     * @param menuId
     * @return List
     * @Description:获取报表列表
     */
    @Override
    public JSONObject getReportList(Map<String, String> params)
    {
        JSONObject resultJSON = new JSONObject();
        // 报表风格
        String styleType = params.get("styleType");
        try
        {
            if (styleType.equals(Constant.REPORT_TYPE))
            {// 报表列表
                resultJSON = this.queryReportList(params);
            }
            else
            {// 仪表盘列表(仪表盘前端暂未调用)
                instrumentPanelAppService.findAll(params.get("token"));
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return resultJSON;
    }
    
    /**
     * @param params
     * @return LinkedList
     * @Description:获取编辑报表高级筛选条件字段
     */
    @Override
    public LinkedList<JSONObject> getReportEditLayoutFilterFields(Map<String, String> params)
    {
        LinkedList<JSONObject> filterFieldList = new LinkedList<JSONObject>();
        
        try
        {
            String token = params.get("token");
            String chooseBean = params.get("chooseBean");
            String pageNum = params.get("pageNum");// 预留
            if (StringUtil.isEmpty(chooseBean))
            {
                return filterFieldList;
            }
            
            // token解析
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            String[] beanNameArr = chooseBean.split(",");
            /*
             * List<String> beanNameList = new ArrayList<String>(); for (String
             * tmpName : beanNameArr) { if (tmpName.indexOf("approval") == -1) {
             * beanNameList.add(tmpName); } }
             */
            BasicDBObject basicCondition = new BasicDBObject();
            basicCondition.append(QueryOperators.AND,
                new BasicDBObject[] {new BasicDBObject("companyId", companyId.toString()), new BasicDBObject("pageNum", pageNum == null ? "0" : pageNum),
                    new BasicDBObject("layoutState", Constant.LAYOUT_TYPE_ENABLE), new BasicDBObject("bean", new BasicDBObject(QueryOperators.IN, beanNameArr))});
            List<JSONObject> beanJSONList = LayoutUtil.findDocs(basicCondition, Constant.CUSTOMIZED_COLLECTION);
            
            // 获取运算符
            JSONObject operator = LayoutUtil.getOperator();
            JSONArray operatorArray = operator.getJSONArray("operator");
            Map<String, JSONArray> typeMap = new HashMap<>();
            for (int i = 0; i < operatorArray.size(); i++)
            {
                JSONObject object = operatorArray.getJSONObject(i);
                JSONArray typeArray = object.getJSONArray("type");
                JSONArray typeOperatorArray = object.getJSONArray("operator");
                for (int j = 0; j < typeArray.size(); j++)
                {
                    String type = typeArray.getString(j);
                    typeMap.put(type, typeOperatorArray);
                }
            }
            
            int beanSize = beanNameArr.length;// beanNameList.size();
            int moduleSize = beanJSONList.size();
            for (int i = 0; i < moduleSize; i++)
            {
                JSONObject tmpModuleJSON = beanJSONList.get(i);
                JSONArray layoutArr = tmpModuleJSON.getJSONArray("layout");
                int layoutSize = layoutArr.size();
                for (int j = 0; j < layoutSize; j++)
                {
                    JSONObject layoutJSON = (JSONObject)layoutArr.get(j);
                    JSONArray rowsArr = layoutJSON.getJSONArray("rows");
                    int rowsSize = rowsArr.size();
                    for (int k = 0; k < rowsSize; k++)
                    {
                        JSONObject rowsSON = (JSONObject)rowsArr.get(k);
                        String type = rowsSON.getString("type");
                        String label = rowsSON.getString("label");
                        if (type.equals(Constant.TYPE_ATTACHMENT) || type.equals(Constant.TYPE_PICTURE) || type.equals(Constant.TYPE_SUBFORM))
                        {
                            continue;
                        }
                        
                        JSONObject fieldJson = new JSONObject();
                        fieldJson.put("bean", tmpModuleJSON.getString("bean"));
                        fieldJson.put("label", beanSize == 1 ? label : new StringBuilder(tmpModuleJSON.getString("title")).append(".").append(label).toString());
                        fieldJson.put("value", rowsSON.getString("name"));
                        fieldJson.put("type", type);
                        if (rowsSON.getJSONArray("entrys") != null)
                        {
                            fieldJson.put("entrys", rowsSON.getJSONArray("entrys"));
                        }
                        fieldJson.put("operator", typeMap.get(type));
                        
                        filterFieldList.add(fieldJson);
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        
        return filterFieldList;
    }
    
    /**
     * @param beanName
     * @param token
     * @param clientFlag
     * @return
     * @Description:获取筛选列表条件字段
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<JSONObject> getReportDataListFilterFields(String beanName, String token, String clientFlag)
    {
        List<JSONObject> result = new ArrayList<JSONObject>();
        // token解析
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        try
        {
            Object object = JedisClusterHelper.get(new StringBuilder("report_datalist_filterfields_").append(companyId).append("_").append(employeeId).toString());
            if (null != object)
            {
                result = (List<JSONObject>)object;
            }
            else
            {
                String reportTable = DAOUtil.getTableName("report", companyId);
                StringBuilder query = new StringBuilder();
                String departmentCenterTable = DAOUtil.getTableName("department_center", companyId);
                String employeeTable = DAOUtil.getTableName("employee", companyId);
                
                StringBuilder querySql = new StringBuilder();
                querySql.append("SELECT t1.data_source_name \"value\", t1.data_source_label label, t1.create_by, t1.modify_by FROM ");
                querySql.append(reportTable);
                querySql.append(" t1 WHERE NOT EXISTS(SELECT 1 FROM ");
                querySql.append(reportTable);
                querySql.append(
                    " WHERE t1.data_source_name=data_source_name AND t1.ID>ID) and t1.del_status = 0 and ((t1.share_company = 1 OR string_to_array(t1.share_user, ',') @> ARRAY [ '");
                querySql.append(employeeId);
                querySql.append("' ] OR 1 IN (SELECT ID FROM ");
                querySql.append(employeeTable);
                querySql.append(" WHERE string_to_array(ROLE_ID, ',') IN (string_to_array(t1.SHARE_ROLE, ',')) UNION SELECT EMPLOYEE_ID FROM ");
                querySql.append(departmentCenterTable);
                querySql.append(" WHERE string_to_array(DEPARTMENT_ID, ',') IN (string_to_array(t1.SHARE_DEPARTMENT, ',')))) OR t1.create_by = ");
                querySql.append(employeeId);
                querySql.append(")");
                List<JSONObject> reportDatas = DAOUtil.executeQuery4JSON(querySql.toString());
                
                List<JSONObject> dataSourceList = new ArrayList<JSONObject>();
                StringBuilder createByList = new StringBuilder();
                StringBuilder modifyByList = new StringBuilder();
                createByList.append("'");
                modifyByList.append("'");
                Map<String, String> valueMap = new HashMap<String, String>();
                
                for (JSONObject jsonObject : reportDatas)
                {
                    String createBy = jsonObject.getString("create_by");
                    String modifyBy = jsonObject.getString("modify_by");
                    String value = jsonObject.getString("value");
                    if (createByList.indexOf(createBy) == -1)
                    {
                        createByList.append(createBy).append("', '");
                    }
                    if (modifyByList.indexOf(modifyBy) == -1)
                    {
                        modifyByList.append(modifyBy).append("', '");
                    }
                    if (null == valueMap.get(value))
                    {
                        jsonObject.remove("create_by");
                        jsonObject.remove("modify_by");
                        dataSourceList.add(jsonObject);
                        valueMap.put(value, "Y");
                    }
                }
                
                JSONObject reportName = new JSONObject();
                reportName.put("id", "reportLabel");
                reportName.put("name", "报表名称");
                reportName.put("type", "text");
                result.add(reportName);
                
                JSONObject dataSourceName = new JSONObject();
                dataSourceName.put("id", "dataSourceName");
                dataSourceName.put("name", "数据源");
                dataSourceName.put("type", "picklist");
                dataSourceName.put("entrys", dataSourceList);
                result.add(dataSourceName);
                
                JSONObject createBy = new JSONObject();
                createBy.put("id", "createBy");
                createBy.put("name", "创建人");
                createBy.put("type", "personnel");
                query.setLength(0);
                query.append("select t1.id, t1.employee_name as \"name\", t1.picture, t2.name as post_name from employee_").append(companyId);
                query.append(" t1, post_").append(companyId);
                query.append(" t2 where t1.id in(").append(createByList.substring(0, modifyByList.lastIndexOf(",")));
                query.append(" ) and t1.post_id = t2.id");
                createBy.put("member", DAOUtil.executeQuery4JSON(query.toString()));
                result.add(createBy);
                
                JSONObject createTime = new JSONObject();
                createTime.put("id", "createTime");
                createTime.put("name", "创建时间");
                createTime.put("type", "datetime");
                result.add(createTime);
                
                JSONObject modifyBy = new JSONObject();
                modifyBy.put("id", "modifyBy");
                modifyBy.put("name", "修改人");
                modifyBy.put("type", "personnel");
                query.setLength(0);
                query.append("select t1.id, t1.employee_name as name, t1.picture, t2.name as post_name from employee_").append(companyId);
                query.append(" t1, post_").append(companyId);
                query.append(" t2 where t1.id in(").append(modifyByList.substring(0, modifyByList.lastIndexOf(",")));
                query.append(" ) and t1.post_id = t2.id");
                modifyBy.put("member", DAOUtil.executeQuery4JSON(query.toString()));
                result.add(modifyBy);
                
                JSONObject modifyTime = new JSONObject();
                modifyTime.put("id", "modifyTime");
                modifyTime.put("name", "修改时间");
                modifyTime.put("type", "datetime");
                result.add(modifyTime);
            }
            
        }
        catch (Exception e)
        {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        log.debug("end !");
        return result;
    }
    
    @Override
    public LinkedList<JSONObject> getReportDataDetailFilterFields(Map<String, String> params)
    {
        LinkedList<JSONObject> filterFieldList = new LinkedList<JSONObject>();
        // 获取报表定义布局
        JSONObject reportLayout = this.getReportLayoutDetail(params);
        if (null == reportLayout)
        {
            return filterFieldList;
        }
        
        // 字段集合
        JSONArray allFieldList = new JSONArray();
        // 报表类型：0列表式、1汇总式、2矩阵式
        String reportType = reportLayout.getString("reportType");
        if (reportType.equals("0"))
        {
            // 列显示字段
            allFieldList = reportLayout.getJSONArray("column");
        }
        else if (reportType.equals("1"))
        {
            // 获取分组字段
            allFieldList = reportLayout.getJSONArray("group");
            // 获取汇总字段
            allFieldList.addAll(reportLayout.getJSONArray("summary"));
        }
        else if (reportType.equals("2"))
        {
            // 获取行分组字段
            allFieldList = reportLayout.getJSONArray("rowGroup");
            // 获取列分组字段
            allFieldList.addAll(reportLayout.getJSONArray("columnGroup"));
            // 获取汇总字段
            allFieldList.addAll(reportLayout.getJSONArray("summary"));
        }
        
        // 获取运算符
        JSONObject operator = LayoutUtil.getOperator();
        JSONArray operatorArray = operator.getJSONArray("operator");
        Map<String, JSONArray> typeMap = new HashMap<>();
        for (int i = 0; i < operatorArray.size(); i++)
        {
            JSONObject object = operatorArray.getJSONObject(i);
            JSONArray typeArray = object.getJSONArray("type");
            JSONArray typeOperatorArray = object.getJSONArray("operator");
            for (int j = 0; j < typeArray.size(); j++)
            {
                String type = typeArray.getString(j);
                typeMap.put(type, typeOperatorArray);
            }
        }
        
        List<String> beans = new ArrayList<String>();
        int mainCount = 0;
        String mainBean = "";
        for (Object allFieldObj : allFieldList)
        {
            JSONObject allFieldJSON = (JSONObject)allFieldObj;
            String bean = allFieldJSON.getString("bean");
            String title = allFieldJSON.getString("title");
            if (StringUtil.isEmpty(mainBean))
            {
                mainBean = bean;
            }
            if (bean.equals(mainBean))
            {
                mainCount++;
            }
            StringBuilder bt = new StringBuilder(bean).append("-").append(title);
            if (!beans.contains(bt.toString()))
            {
                beans.add(bt.toString());
            }
        }
        for (Object allFieldObj : allFieldList)
        {
            JSONObject allFieldJSON = (JSONObject)allFieldObj;
            String name = allFieldJSON.getString("name");
            String type = allFieldJSON.getString("type");
            String label = allFieldJSON.getString("label");
            String bean = allFieldJSON.getString("bean");
            
            if (name.equals(Constant.FIELD_CREATE_TIME) || name.equals(Constant.FIELD_MODIFY_TIME))
            {// 过滤： 创建时间、修改时间
                continue;
            }
            
            JSONObject fieldJson = new JSONObject();
            fieldJson.put("bean", bean);
            fieldJson.put("label", label);
            fieldJson.put("value", name);
            fieldJson.put("type", type);
            if (allFieldJSON.getJSONArray("entrys") != null)
            {
                fieldJson.put("entrys", allFieldJSON.getJSONArray("entrys"));
            }
            fieldJson.put("operator", typeMap.get(type));
            
            filterFieldList.add(fieldJson);
        }
        
        for (int j = 0; j < beans.size(); j++)
        {// 添加： 创建时间、修改时间
            String[] beanArr = beans.get(j).split("-");
            JSONObject createTime = new JSONObject();
            createTime.put("bean", beanArr[0]);
            createTime.put("label", beans.size() > 1 ? beanArr[1].concat(".创建时间") : "创建时间");
            createTime.put("value", Constant.FIELD_CREATE_TIME);
            createTime.put("type", Constant.TYPE_DATETIME);
            createTime.put("operator", typeMap.get(Constant.TYPE_DATETIME));
            if (beanArr[0].equals(mainBean))
            {
                if (mainCount < filterFieldList.size())
                {
                    filterFieldList.add(mainCount, createTime);
                }
                else
                {
                    filterFieldList.add(createTime);
                }
            }
            else
            {
                filterFieldList.addLast(createTime);
            }
            
            JSONObject modifyTime = new JSONObject();
            modifyTime.put("bean", beanArr[0]);
            modifyTime.put("label", beans.size() > 1 ? beanArr[1].concat(".修改时间") : "修改时间");
            modifyTime.put("value", Constant.FIELD_MODIFY_TIME);
            modifyTime.put("type", Constant.TYPE_DATETIME);
            modifyTime.put("operator", typeMap.get(Constant.TYPE_DATETIME));
            if (beanArr[0].equals(mainBean))
            {
                mainCount += 1;
                if (mainCount < filterFieldList.size())
                {
                    filterFieldList.add(mainCount, modifyTime);
                }
                else
                {
                    filterFieldList.add(modifyTime);
                }
            }
            else
            {
                filterFieldList.addLast(modifyTime);
            }
        }
        
        return filterFieldList;
    }
    
    /**
     * @param token
     * @param reportId
     * @return JSONObject
     * @Description:根据id获取数据
     */
    @Override
    public JSONObject getReportById(String token, Long reportId)
    {
        // token解析
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        
        String reportTable = DAOUtil.getTableName("report", companyId);
        StringBuilder querySql = new StringBuilder();
        querySql.append("select * from ").append(reportTable).append(" where id = ").append(reportId);
        
        return DAOUtil.executeQuery4FirstJSON(querySql.toString());
    }
    
    /**
     * @param params
     * @return JSONObject
     * @Description:获取报表定义
     */
    @Override
    public JSONObject getReportLayoutDetail(Map<String, String> params)
    {
        // token解析
        InfoVo info = TokenMgr.obtainInfo(params.get("token"));
        Long companyId = info.getCompanyId();
        
        Document queryDoc = new Document();
        queryDoc.put("reportId", params.get("reportId"));
        queryDoc.put("companyId", companyId.toString());
        queryDoc.put("styleType", Constant.REPORT_TYPE);
        
        return LayoutUtil.findDoc(queryDoc, Constant.REPORT_COLLECTION);
    }
    
    /**
     * @param params
     * @return JSONObject
     * @Description:获取报表数据详情
     */
    @Override
    public String getReportDataDetail(Map<String, String> params)
    {
        List<JSONObject> sourceModueDataList = new ArrayList<JSONObject>();
        String token = params.get("token");
        JSONObject reqJson = JSONObject.parseObject(params.get("reqJsonStr"));
        String reportId = reqJson.getString("reportId");
        JSONArray seniorWhereArr = reqJson.getJSONArray("seniorWhere");
        try
        {
            // token解析
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            Long employeeId = info.getEmployeeId();
            
            JSONObject reportLayout = new JSONObject();
            // 报表类型：0列表式、1汇总式、2矩阵式
            String reportType = "";
            // 数据源
            String dataSourceBean = "";
            if (StringUtil.isEmpty(reportId))
            {// 预览详情
                String previewReportLayout = reqJson.getString("reportLayout");
                JSONObject previewReportLayoutJson = JSONObject.parseObject(previewReportLayout);
                reportType = previewReportLayoutJson.getString("reportType");
                dataSourceBean = previewReportLayoutJson.getString("dataSourceName");
                reportLayout = previewReportLayoutJson;
                JSONArray conditionArr = previewReportLayoutJson.getJSONArray("condition");
                if (null != conditionArr)
                {
                    if (null == seniorWhereArr)
                    {
                        seniorWhereArr = new JSONArray();
                    }
                    // 报表定义高级筛选条件
                    seniorWhereArr.addAll(conditionArr);
                }
            }
            else
            {// 数据详情
                JSONObject reportJson = this.getReportById(token, Long.parseLong(reportId));
                if (null == reportJson)
                {
                    return sourceModueDataList.toString();
                }
                reportType = reportJson.getString("report_type");
                dataSourceBean = reportJson.getString("data_source_name");
                
                // 获取报表定义布局
                Map<String, String> getReportLayoutParams = new HashMap<String, String>();
                getReportLayoutParams.put("companyId", companyId.toString());
                getReportLayoutParams.put("reportId", reportId);
                getReportLayoutParams.put("reportType", reportType);
                getReportLayoutParams.put("token", token);
                reportLayout = this.getReportLayoutDetail(getReportLayoutParams);
                if (null == reportLayout)
                {
                    return sourceModueDataList.toString();
                }
                JSONArray conditionArr = reportLayout.getJSONArray("condition");
                if (null != conditionArr)
                {
                    if (null == seniorWhereArr)
                    {
                        seniorWhereArr = new JSONArray();
                    }
                    // 报表定义高级筛选条件
                    seniorWhereArr.addAll(conditionArr);
                }
                
                List<Object> values = new ArrayList<>();
                values.add(Integer.valueOf(reportId));
                values.add(employeeId);
                values.add(System.currentTimeMillis());
                // 保存点击记录
                saveClickHist(companyId, values);
            }
            
            String chooseBean = reportLayout.getString("chooseBean");
            String mainModule = dataSourceBean.split(",")[0];
            if (reportType.equals("0"))
            {
                // 列显示字段
                JSONArray columnFields = reportLayout.getJSONArray("column");
                String pageNum = reqJson.getString("pageNum");
                String pageSize = reqJson.getString("pageSize");
                JSONObject pageData = this.queryReportDataListByList(chooseBean,
                    mainModule,
                    columnFields,
                    seniorWhereArr,
                    companyId,
                    employeeId,
                    StringUtil.isEmpty(pageNum) ? 1 : Integer.valueOf(pageNum),
                    StringUtil.isEmpty(pageSize) ? 20 : Integer.valueOf(pageSize));
                return pageData.toString();
            }
            else if (reportType.equals("1"))
            {
                // 获取分组字段
                JSONArray groupFields = reportLayout.getJSONArray("group");
                // 获取汇总字段
                JSONArray summaryFields = reportLayout.getJSONArray("summary");
                
                sourceModueDataList = this.queryReportDataListBySummary(chooseBean, groupFields, summaryFields, dataSourceBean, seniorWhereArr, companyId, employeeId);
            }
            else if (reportType.equals("2"))
            {
                // 获取行分组字段
                JSONArray rowGroupFields = reportLayout.getJSONArray("rowgroup");
                // 获取列分组字段
                JSONArray columnGroupFields = reportLayout.getJSONArray("colgroup");
                // 获取汇总字段
                JSONArray summaryFields = reportLayout.getJSONArray("summary");
                
                JSONObject sourceModueData =
                    this.queryReportDataListByMatrix(chooseBean, mainModule, rowGroupFields, columnGroupFields, summaryFields, seniorWhereArr, companyId, employeeId);
                return sourceModueData.toString();
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return sourceModueDataList.toString();
    }
    
    /**
     * @param params
     * @return ServiceResult
     * @Description: 删除报表
     */
    @Override
    public ServiceResult<String> removeReport(Map<String, String> params)
    {
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        try
        {
            // 请求参数
            String token = params.get("token");
            String reqJsonStr = params.get("reqJsonStr");
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            String reportId = reqJson.getString("reportId");
            String styleType = reqJson.getString("styleType");
            
            // token解析
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            Long employeeId = info.getEmployeeId();
            
            // 软删除数据记录
            String reportTable = DAOUtil.getTableName("report", companyId);
            StringBuilder delSql = new StringBuilder();
            delSql.append("update ").append(reportTable);
            delSql.append(" set ").append(Constant.FIELD_DEL_STATUS).append(" = '1' where id = ").append(reportId);
            int delResult = DAOUtil.executeUpdate(delSql.toString());
            if (delResult < 1)
            {
                log.error("delete report operation fail!!!");
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            
            // 删除定义
            Document queryDoc = new Document();
            queryDoc.put("reportId", reportId);
            queryDoc.put("companyId", companyId.toString());
            queryDoc.put("styleType", styleType);
            LayoutUtil.removeDoc(queryDoc, Constant.REPORT_COLLECTION);
            
            // 删除最近记录
            String clickHistTable = DAOUtil.getTableName("report_click_hist", companyId);
            StringBuilder insertSql = new StringBuilder();
            insertSql.append("delete from ").append(clickHistTable).append(" where report_id = ").append(reportId).append(" and create_by = ").append(employeeId);
            int delClickHistResult = DAOUtil.executeUpdate(insertSql.toString());
            if (delClickHistResult < 1)
            {
                log.error("delete click report history operation fail!!!");
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        return serviceResult;
    }
    
    /**
     * 获取报表所有字段
     */
    @Override
    public LinkedList<JSONObject> getReportFilterFields(Map<String, String> params)
    {
        LinkedList<JSONObject> filterFieldList = new LinkedList<JSONObject>();
        // 获取报表定义布局
        JSONObject reportLayout = this.getReportLayoutDetail(params);
        if (null == reportLayout)
        {
            return filterFieldList;
        }
        
        // 字段集合
        JSONArray allFieldList = new JSONArray();
        // 报表类型：0列表式、1汇总式、2矩阵式
        String reportType = reportLayout.getString("reportType");
        if (reportType.equals("0"))
        {
            // 列显示字段
            allFieldList = reportLayout.getJSONArray("column");
        }
        else if (reportType.equals("1"))
        {
            // 获取分组字段
            allFieldList = reportLayout.getJSONArray("group");
            // 获取汇总字段
            allFieldList.addAll(reportLayout.getJSONArray("summary"));
        }
        else if (reportType.equals("2"))
        {
            // 获取行分组字段
            allFieldList = reportLayout.getJSONArray("rowgroup");
            // 获取列分组字段
            allFieldList.add(reportLayout.getJSONArray("colgroup"));
            // 获取汇总字段
            allFieldList.add(reportLayout.getJSONArray("summary"));
        }
        
        // 获取运算符
        JSONObject operator = LayoutUtil.getOperator();
        JSONArray operatorArray = operator.getJSONArray("operator");
        Map<String, JSONArray> typeMap = new HashMap<>();
        for (int i = 0; i < operatorArray.size(); i++)
        {
            JSONObject object = operatorArray.getJSONObject(i);
            JSONArray typeArray = object.getJSONArray("type");
            JSONArray typeOperatorArray = object.getJSONArray("operator");
            for (int j = 0; j < typeArray.size(); j++)
            {
                String type = typeArray.getString(j);
                typeMap.put(type, typeOperatorArray);
            }
        }
        
        List<String> beans = new ArrayList<String>();
        int mainCount = 0;
        String mainBean = "";
        for (Object allFieldObj : allFieldList)
        {
            JSONObject allFieldJSON = null;
            if (allFieldObj instanceof List)
            {
                JSONArray allFieldArr = (JSONArray)allFieldObj;
                allFieldJSON = (JSONObject)allFieldArr.get(0);
            }
            else
            {
                allFieldJSON = (JSONObject)allFieldObj;
            }
            String bean = allFieldJSON.getString("bean");
            String title = allFieldJSON.getString("title");
            if (StringUtil.isEmpty(mainBean))
            {
                mainBean = bean;
            }
            if (bean.equals(mainBean))
            {
                mainCount++;
            }
            
            StringBuilder bt = new StringBuilder(bean).append("-").append(title);
            if (!beans.contains(bt.toString()))
            {
                beans.add(bt.toString());
            }
        }
        for (Object allFieldObj : allFieldList)
        {
            JSONObject allFieldJSON = null;
            if (allFieldObj instanceof List)
            {
                JSONArray allFieldArr = (JSONArray)allFieldObj;
                allFieldJSON = (JSONObject)allFieldArr.get(0);
            }
            else
            {
                allFieldJSON = (JSONObject)allFieldObj;
            }
            String name = allFieldJSON.getString("name");
            String type = allFieldJSON.getString("type");
            String label = allFieldJSON.getString("label");
            String bean = allFieldJSON.getString("bean");
            
            if (name.equals(Constant.FIELD_CREATE_TIME) || name.equals(Constant.FIELD_MODIFY_TIME))
            {// 过滤： 创建时间、修改时间
                continue;
            }
            
            JSONObject fieldJson = new JSONObject();
            fieldJson.put("bean", bean);
            fieldJson.put("label", label);
            fieldJson.put("value", name);
            fieldJson.put("type", type);
            if (allFieldJSON.getJSONArray("entrys") != null)
            {
                fieldJson.put("entrys", allFieldJSON.getJSONArray("entrys"));
            }
            fieldJson.put("operator", typeMap.get(type));
            
            filterFieldList.add(fieldJson);
        }
        
        for (int j = 0; j < beans.size(); j++)
        {// 添加： 创建时间、修改时间
            String[] beanArr = beans.get(j).split("-");
            JSONObject createTime = new JSONObject();
            createTime.put("bean", beanArr[0]);
            createTime.put("label", beans.size() > 1 ? beanArr[1].concat(".创建时间") : "创建时间");
            createTime.put("value", Constant.FIELD_CREATE_TIME);
            createTime.put("type", Constant.TYPE_DATETIME);
            createTime.put("operator", typeMap.get(Constant.TYPE_DATETIME));
            if (beanArr[0].equals(mainBean))
            {
                if (mainCount < filterFieldList.size())
                {
                    filterFieldList.add(mainCount, createTime);
                }
                else
                {
                    filterFieldList.add(createTime);
                }
            }
            else
            {
                filterFieldList.addLast(createTime);
            }
            
            JSONObject modifyTime = new JSONObject();
            modifyTime.put("bean", beanArr[0]);
            modifyTime.put("label", beans.size() > 1 ? beanArr[1].concat(".修改时间") : "修改时间");
            modifyTime.put("value", Constant.FIELD_MODIFY_TIME);
            modifyTime.put("type", Constant.TYPE_DATETIME);
            modifyTime.put("operator", typeMap.get(Constant.TYPE_DATETIME));
            if (beanArr[0].equals(mainBean))
            {
                mainCount += 1;
                if (mainCount < filterFieldList.size())
                {
                    filterFieldList.add(mainCount, modifyTime);
                }
                else
                {
                    filterFieldList.add(modifyTime);
                }
            }
            else
            {
                filterFieldList.addLast(modifyTime);
            }
        }
        
        return filterFieldList;
    }
    
    /**
     * @param params
     * @return int
     * @Description:验证报表名称是否存在
     */
    @Override
    public int checkReportNameExist(Map<String, String> params, boolean modifyFlag)
    {
        String reportLabel = params.get("reportLabel");
        String token = params.get("token");
        
        // token解析
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        
        StringBuilder querySql = new StringBuilder();
        String reportTable = DAOUtil.getTableName("report", companyId);
        
        querySql.append("select count(id) from ").append(reportTable).append(" where report_label = '").append(reportLabel).append("'");
        if (modifyFlag)
        {
            querySql.append(" and id != ").append(params.get("reportId"));
        }
        JSONObject firstResult = DAOUtil.executeQuery4FirstJSON(querySql.toString());
        if (null != firstResult && firstResult.getInteger("count") > 0)
        {
            return 1;
        }
        return 0;
        
    }
    
    /**
     * @param params
     * @return List
     * @Description: 获取报表类型数据列表
     */
    private JSONObject queryReportList(Map<String, String> params)
    {
        // 请求参数
        String token = params.get("token");
        String menuId = params.get("menuId");
        String reportLabel = params.get("reportLabel");
        String dataSourceName = params.get("dataSourceName");
        String createBy = params.get("createBy");
        String createTime = params.get("createTime");
        String modifyBy = params.get("modifyBy");
        String modifyTime = params.get("modifyTime");
        String queryType = params.get("queryType");
        int pageNum = StringUtil.isEmpty(params.get("pageNum")) ? 1 : Integer.parseInt(params.get("pageNum"));
        int pageSize = StringUtil.isEmpty(params.get("pageSize")) ? 20 : Integer.parseInt(params.get("pageSize"));
        
        // token解析
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        
        String reportTable = DAOUtil.getTableName("report", companyId);
        String employeeTable = DAOUtil.getTableName("employee", companyId);
        StringBuilder querySql = new StringBuilder();
        querySql.append("select t1.id, t1.report_name, t1.report_label, t1.report_type, t1.data_source_name, t1.data_source_label, t1.create_time, t1.create_by create_by_id, ");
        querySql.append("(select employee_name from ").append(employeeTable).append(" where id = t1.create_by)").append(" as create_by_name, ");
        querySql.append("t1.modify_by modify_by_id ");
        querySql.append(" from ").append(reportTable).append(" t1 ");
        
        // 菜单条件
        if (menuId.equals("0"))
        {// 最近报表（30天内点击的报表）
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.DATE, -29);
            
            String reportClickTable = DAOUtil.getTableName("report_click_hist", companyId);
            querySql.append(" where t1.id in(select tt.report_id from ").append(reportClickTable).append(" tt");
            querySql.append(" where tt.create_time between ").append(calendar.getTimeInMillis()).append(" and ").append(System.currentTimeMillis());
            querySql.append(" and tt.create_by = ").append(employeeId).append(")");
        }
        else if (menuId.equals("1"))
        {// 全部报表（共享给我的+我创建的）
            String departmentCenterTable = DAOUtil.getTableName("department_center", companyId);
            JSONObject userRoleJSON = moduleDataAuthAppService.getRoleByUser(token);
            if (null != userRoleJSON)
            {
                Integer roleId = userRoleJSON.getInteger("id");
                if (roleId == 1 || roleId == 2)
                {// 1,2角色可查看所有数据
                    querySql.append(" where 1 = 1 ");
                }
                else
                {// 其他角色需判断权限
                    querySql.append(" where ((t1.share_company = 1 or string_to_array(t1.share_user,',') @> array['").append(employeeId);
                    querySql.append("'] or ").append(employeeId);
                    querySql.append(" in(SELECT ID FROM ").append(employeeTable);
                    querySql.append(" WHERE array[ROLE_ID] && string_to_array(t1.SHARE_ROLE, ',')::int[] UNION SELECT EMPLOYEE_ID FROM ").append(departmentCenterTable);
                    querySql.append(" WHERE array[DEPARTMENT_ID] && string_to_array(t1.SHARE_DEPARTMENT, ',')::int[])) or t1.create_by=").append(employeeId).append(")");
                }
            }
        }
        else if (menuId.equals("2"))
        {// 共享给我的报表
            String departmentCenterTable = DAOUtil.getTableName("department_center", companyId);
            querySql.append(" where (t1.share_company = 1 or string_to_array(t1.share_user,',') @> array['").append(employeeId);
            querySql.append("'] or ").append(employeeId);
            querySql.append(" in(SELECT ID FROM ").append(employeeTable);
            querySql.append(" WHERE array[ROLE_ID] && string_to_array(t1.SHARE_ROLE, ',')::int[] UNION SELECT EMPLOYEE_ID FROM ").append(departmentCenterTable);
            querySql.append(" WHERE array[DEPARTMENT_ID] && string_to_array(t1.SHARE_DEPARTMENT, ',')::int[]))");
        }
        else if (menuId.equals("3"))
        {// 我创建的报表
            querySql.append(" where 1 = 1 and t1.create_by = ").append(employeeId);
        }
        
        // 筛选条件
        if (!StringUtil.isEmpty(reportLabel))
        {// 报表名称
            querySql.append(" and t1.report_label like '%").append(reportLabel).append("%'");
        }
        if (!StringUtil.isEmpty(dataSourceName))
        {// 数据源
            dataSourceName = new StringBuilder("'").append(dataSourceName.replace("#", "','")).append("'").toString();
            querySql.append(" and t1.data_source_name in(").append(dataSourceName).append(")");
        }
        if (!StringUtil.isEmpty(createBy))
        {// 创建人
            querySql.append(" and t1.create_by in(").append(createBy).append(")");
        }
        if (!StringUtil.isEmpty(createTime))
        {// 创建时间
            JSONObject createTimeJSON = JSONObject.parseObject(createTime);
            querySql.append(" and t1.create_time between ").append(createTimeJSON.get("startTime")).append(" and ").append(createTimeJSON.get("endTime"));
        }
        if (!StringUtil.isEmpty(modifyBy))
        {// 修改人
            querySql.append(" and t1.modify_by in(").append(modifyBy).append(")");
        }
        if (!StringUtil.isEmpty(modifyTime))
        {// 修改时间
            JSONObject modifyTimeJSON = JSONObject.parseObject(modifyTime);
            querySql.append(" and t1.modify_time between ").append(modifyTimeJSON.get("startTime")).append(" and ").append(modifyTimeJSON.get("endTime"));
        }
        
        querySql.append(" and t1.").append(Constant.FIELD_DEL_STATUS).append("=0 order by t1.create_time desc");
        List<JSONObject> dataList = DAOUtil.executeQuery4JSON(querySql.toString());
        querySql.append(" limit ").append(pageSize).append(" OFFSET ").append((pageNum - 1) * pageSize);
        List<JSONObject> pageList = DAOUtil.executeQuery4JSON(querySql.toString());
        
        if (StringUtil.isEmpty(queryType) || !"filter".equals(queryType))
        {
            AsyncHandle asyncHandle = new AsyncHandle();
            GenerateDataListFilterFields gdlff = new GenerateDataListFilterFields(token, dataList);
            gdlff.setName("GenerateDataListFilterFields-Thread");
            asyncHandle.commitJob(gdlff);
        }
        
        int totalRows = dataList.size();
        int totalPages = totalRows / pageSize;
        if (totalRows % pageSize > 0)
        {
            totalPages++;
        }
        JSONObject result = new JSONObject();
        /***** 封装返回前端的数据结构 *****/
        int curPageSize = pageList.size();
        result.put("list", pageList);
        
        JSONObject pageInfo = new JSONObject();
        pageInfo.put("totalRows", totalRows);
        pageInfo.put("totalPages", totalPages);
        pageInfo.put("pageNum", pageNum);
        pageInfo.put("pageSize", pageSize);
        pageInfo.put("curPageSize", curPageSize);
        result.put("pageInfo", pageInfo);
        
        return result;
    }
    
    /**
     * @param columnFields 列显示字段
     * @param mainModule 当前模块bean
     * @param companyId 公司id
     * @return List
     * @Description: 获取列表式报表数据
     */
    private JSONObject queryReportDataListByList(String chooseBean, String mainModule, JSONArray columnFields, JSONArray seniorWhereArr, Long companyId, Long employeeId,
        int pageNum, int pageSize)
    {
        JSONObject result = new JSONObject();
        List<JSONObject> sourceModueDataList = new ArrayList<JSONObject>();
        if (columnFields == null || columnFields.size() == 0)
        {
            return result;
        }
        JSONObject titleJson = new JSONObject();
        JSONObject dataJson = new JSONObject();
        titleJson.put("dataType", "title");
        dataJson.put("dataType", "data");
        
        // 报表的title
        List<String> titleList = new ArrayList<String>();
        // 查询sql
        StringBuilder querySourceModuleSql = new StringBuilder();
        // 表名
        StringBuilder querySourceModules = new StringBuilder();
        // 字段名
        StringBuilder querySourceModuleFileds = new StringBuilder();
        // 条件
        StringBuilder querySourceModuleWhere = new StringBuilder();
        // 审批bean
        String approvalBean = null;
        // 审批条件
        StringBuilder approvalWhere = new StringBuilder();
        // 审批标志
        boolean approvalBeanFlag = true;
        try
        {
            /***** 模块数据权限 *****/
            Map<String, String> dataAuthMap = ReportUtil.getModuleCreatersByDataAuth(companyId, employeeId, chooseBean);
            if (null == dataAuthMap || dataAuthMap.size() == 0)
            {// 无模块权限，不允许查数据
                return result;
            }
            
            /***** 模块数据共享（含：单条共享+规则共享） *****/
            Map<String, String> dataShareMap = ReportUtil.getDataShareIds(employeeId, companyId, chooseBean);
            
            // 主表
            String mainTable = DAOUtil.getTableName(mainModule, companyId);
            String[] chooseBeanArr = chooseBean.split(",");
            List<String> chooseBeanList = new ArrayList<String>();
            for (String chooseBeanStr : chooseBeanArr)
            {
                if (chooseBeanStr.contains(Constant.TYPE_SUBFORM))
                {// 子表单
                    continue;
                }
                if (chooseBeanStr.contains("_approval"))
                {// 审核中的数据
                    approvalBean = chooseBeanStr;
                    continue;
                }
                chooseBeanList.add(chooseBeanStr);
            }
            // 添加初始化条件
            querySourceModuleWhere.append(" where 1 = 1");
            
            boolean refFlag = false;
            Map<String, String> subformFlag = new HashMap<>();
            // 循环列显示字段
            for (Object columnField : columnFields)
            {
                JSONObject columnFieldJson = (JSONObject)columnField;
                String fieldBean = columnFieldJson.getString("bean");
                String fieldType = columnFieldJson.getString("type");
                String fieldName = columnFieldJson.getString("name");
                String otherTable = DAOUtil.getTableName(fieldBean, companyId);
                String employeeTable = DAOUtil.getTableName("employee", companyId);
                String queryField = new StringBuilder(otherTable).append(".").append(fieldName).toString();
                String asField = new StringBuilder(otherTable).append("_").append(fieldName).toString();
                // postgre字段名长度限制64
                asField = asField.replace("_approval_", "_").replace("_subform_", "_");
                asField = asField.length() > 64 ? asField.substring(0, 63) : asField;
                
                /***** 构造返回数据的title *****/
                titleList.add(columnFieldJson.getString("label"));
                
                /***** 添加表（如已存在就不添加了） *****/
                if (querySourceModules.indexOf(otherTable) == -1)
                {// 主表、关联关系表、子表单表
                    if (!StringUtil.isEmpty(approvalBean) && approvalBeanFlag)
                    {// 审核中的数据
                        StringBuilder existApproval = new StringBuilder();
                        existApproval.append("(select tmp1.* from ").append(otherTable);
                        existApproval.append(" tmp1 union all select apr.* from ").append(DAOUtil.getTableName(approvalBean, companyId));
                        existApproval.append(" apr where apr.id in(select pcs.approval_data_id from ").append(DAOUtil.getTableName("process_approval", companyId));
                        existApproval.append(" pcs where pcs.module_bean = '")
                            .append(mainModule)
                            .append("' and pcs.process_status in(0, 1) and apr.del_status = 0)) ")
                            .append(otherTable);
                        approvalBeanFlag = false;
                        querySourceModules.append(existApproval).append(", ");
                        approvalWhere.append(" order by ").append(otherTable).append(".id ");
                    }
                    else
                    {
                        querySourceModules.append(otherTable).append(", ");
                        querySourceModuleWhere.append(" and ").append(otherTable).append(".del_status = 0");
                        if (fieldType.indexOf(Constant.TYPE_SUBFORM.concat(".")) != -1 && null == subformFlag.get(otherTable))
                        {// 子表单条件
                            querySourceModuleWhere.append(" and ").append(otherTable.substring(0, otherTable.indexOf("_") + 1)).append(companyId).append(".id = ");
                            querySourceModuleWhere.append(otherTable).append(".").append(otherTable.substring(0, otherTable.indexOf("_"))).append("_id");
                            subformFlag.put(otherTable, "Y");
                        }
                    }
                    if (!StringUtil.isEmpty(dataAuthMap.get(fieldBean)))
                    {
                        querySourceModuleWhere.append(" and (").append(otherTable).append(".personnel_create_by in(").append(dataAuthMap.get(fieldBean)).append("))");
                        if (null != dataShareMap && !StringUtil.isEmpty(dataShareMap.get(fieldBean)))
                        {
                            querySourceModuleWhere.append(" or ").append(otherTable).append(".id in(").append(dataShareMap.get(fieldBean)).append("))");
                        }
                    }
                }
                
                /***** 添加关联关系条件 *****/
                if (chooseBeanList.size() > 1 && !refFlag)
                {
                    JSONObject refDescribe = columnFieldJson.getJSONObject("refDescribe");
                    if (null != refDescribe)
                    {
                        querySourceModuleWhere.append(" and ").append(otherTable).append(".").append(refDescribe.getString("field"));
                        querySourceModuleWhere.append(" = ").append(mainTable).append(".id");
                        refFlag = true;
                    }
                }
                
                /***** 添加条件（含：关联关系条件、子表单条件、人员组件条件） *****/
                /*
                 * if ((!fieldBean.equals(mainModule) ||
                 * fieldType.equals(Constant.TYPE_PERSONNEL)) &&
                 * querySourceModules.indexOf(mainModule) != -1) { if
                 * (fieldType.indexOf(Constant.TYPE_SUBFORM.concat(".")) != -1
                 * && null == subformFlag.get(otherTable)) {// 子表单条件
                 * querySourceModuleWhere.append(" and ").append(mainTable).
                 * append(".id = ");
                 * querySourceModuleWhere.append(otherTable).append(".").append(
                 * mainModule).append("_id"); subformFlag.put(otherTable, "Y");
                 * } }
                 */
                
                /***** 添加字段 *****/
                if (fieldType.equals(Constant.TYPE_PERSONNEL))
                {// 人员
                    String personnelFormat = columnFieldJson.getString("format");// 0按人、1按部门
                    if (personnelFormat.equals("0"))
                    {
                        querySourceModuleFileds.append("(select employee_name from ").append(employeeTable);
                        querySourceModuleFileds.append(" where ").append(employeeTable).append(".id = ").append(queryField);
                        querySourceModuleFileds.append(") as ").append(employeeTable).append("_").append(fieldName).append(", ");
                    }
                    else
                    {
                        String departmentTable = DAOUtil.getTableName("department", companyId);
                        String departmentCenterTable = DAOUtil.getTableName("department_center", companyId);
                        querySourceModuleFileds.append("(select t1.department_name from ").append(departmentTable);
                        querySourceModuleFileds.append(" t1 join ").append(departmentCenterTable);
                        querySourceModuleFileds.append(" t2 on t1.id = t2.department_id and t1.status = 0 and t2.is_main = 1 and t2.status = 0 and t2.employee_id = ");
                        querySourceModuleFileds.append(queryField).append(") as ").append(employeeTable).append("_").append(fieldName).append(", ");
                    }
                }
                else if (fieldType.contains(Constant.TYPE_DATETIME))
                {// 0：按年 1：按半年 2： 按季度 3：按月 4：按日
                    String dateTimeFormat = columnFieldJson.getString("format");// 0按人、1按部门
                    dateTimeFormat = StringUtil.isEmpty(dateTimeFormat) ? "4" : dateTimeFormat;
                    if (dateTimeFormat.equals("0"))
                    {// 年
                        querySourceModuleFileds.append("(to_char(to_timestamp(").append(queryField);
                        querySourceModuleFileds.append(" / 1000), 'YYYY') || '年') AS ");
                    }
                    else if (dateTimeFormat.equals("1"))
                    {// 半年-暂时不处理
                    
                    }
                    else if (dateTimeFormat.equals("2"))
                    {// 季度
                        querySourceModuleFileds.append("(to_char(to_timestamp(").append(queryField);
                        querySourceModuleFileds.append(" / 1000), 'YYYY') || '年' || EXTRACT(QUARTER from to_timestamp(").append(queryField);
                        querySourceModuleFileds.append(" / 1000)) || '季度') AS ");
                    }
                    else if (dateTimeFormat.equals("3"))
                    {// 月
                        querySourceModuleFileds.append("(to_char(to_timestamp(").append(queryField);
                        querySourceModuleFileds.append(" / 1000), 'YYYY-MM')) AS ");
                    }
                    else if (dateTimeFormat.equals("4"))
                    {// 日
                        querySourceModuleFileds.append("(to_char(to_timestamp(").append(queryField);
                        querySourceModuleFileds.append(" / 1000), 'YYYY-MM-DD')) AS ");
                    }
                    querySourceModuleFileds.append(asField).append(", ");
                }
                else if (fieldType.equals(Constant.TYPE_REFERENCE))
                {// 关联关系
                    JSONObject refDescribe = columnFieldJson.getJSONObject("refDescribe");
                    querySourceModuleFileds.append("(select ").append(refDescribe.getString("referenceField"));
                    querySourceModuleFileds.append(" from ").append(refDescribe.getString("referenceBean")).append("_").append(companyId);
                    querySourceModuleFileds.append(" where ")
                        .append(refDescribe.getString("referenceBean"))
                        .append("_")
                        .append(companyId)
                        .append(".id = ")
                        .append(queryField)
                        .append(") as ")
                        .append(otherTable)
                        .append("_")
                        .append(fieldName)
                        .append(", ");
                }
                else if (fieldType.equals(Constant.TYPE_SUBFORM.concat(".").concat(Constant.TYPE_REFERENCE)))
                {// 关联关系
                    JSONObject refDescribe = columnFieldJson.getJSONObject("refDescribe");
                    querySourceModuleFileds.append("(select ").append(refDescribe.getString("referenceField"));
                    querySourceModuleFileds.append(" from ").append(refDescribe.getString("referenceBean")).append("_").append(companyId);
                    querySourceModuleFileds.append(" where id = ").append(otherTable);
                    querySourceModuleFileds.append(".").append(fieldName).append(") as ").append(asField).append(", ");
                }
                else if (fieldType.equals(Constant.TYPE_AREA))
                {// 省市区
                    int idx = 0;
                    if (fieldName.contains(Constant.TYPE_AREA_PROVINCE))
                    {
                        idx = 1;
                    }
                    else if (fieldName.contains(Constant.TYPE_AREA_CITY))
                    {
                        idx = 2;
                    }
                    else if (fieldName.contains(Constant.TYPE_AREA_DISTRICT))
                    {
                        idx = 3;
                    }
                    String newFiledName = fieldName.replace("_".concat(Constant.TYPE_AREA_PROVINCE), "")
                        .replace("_".concat(Constant.TYPE_AREA_CITY), "")
                        .replace("_".concat(Constant.TYPE_AREA_DISTRICT), "");
                    querySourceModuleFileds.append("split_part(split_part(").append(otherTable).append(".").append(newFiledName).append(", ',', ").append(idx);
                    querySourceModuleFileds.append("),':',2)").append(" as ").append(otherTable).append("_").append(fieldName).append(", ");
                }
                else if (fieldType.equals(Constant.TYPE_MUTLI_PICKLIST))
                {// 多级下拉
                    int idx = 0;
                    if (fieldName.contains(Constant.TYPE_MUTLI_PICKLIST_LEVEL_1))
                    {
                        idx = 1;
                    }
                    else if (fieldName.contains(Constant.TYPE_MUTLI_PICKLIST_LEVEL_2))
                    {
                        idx = 2;
                    }
                    else if (fieldName.contains(Constant.TYPE_MUTLI_PICKLIST_LEVEL_3))
                    {
                        idx = 3;
                    }
                    String newFiledName = fieldName.replace("_".concat(Constant.TYPE_MUTLI_PICKLIST_LEVEL_1), "")
                        .replace("_".concat(Constant.TYPE_MUTLI_PICKLIST_LEVEL_2), "")
                        .replace("_".concat(Constant.TYPE_MUTLI_PICKLIST_LEVEL_3), "");
                    querySourceModuleFileds.append("split_part(replace(substring(").append(newFiledName).append(", 2, length(").append(newFiledName);
                    querySourceModuleFileds.append(")-2), '},{', '}${'),'$', ").append(idx).append(")");
                    querySourceModuleFileds.append(" as ").append(otherTable).append("_").append(fieldName).append(", ");
                }
                else if (fieldType.equals(Constant.TYPE_LOCATION))
                {// 定位0：无 1：按省 2： 按市 3：按区
                    String locationFormat = columnFieldJson.getString("format");
                    if (locationFormat.equals("0"))
                    {// 不拆
                        querySourceModuleFileds.append("split_part(replace(replace(split_part(").append(queryField);
                        querySourceModuleFileds.append(", ',', 3),'\"value\":\"', ''), '\"', ''), '#', 1)");
                    }
                    else
                    {// 拆分查询
                        querySourceModuleFileds.append(" CASE WHEN length(split_part(replace(replace(split_part(")
                            .append(queryField)
                            .append(", ',', 1), '\"', ''), '{area:', ''), '#', ")
                            .append(locationFormat)
                            .append(")) = 0");
                        querySourceModuleFileds.append(" THEN split_part(replace(replace(split_part(").append(queryField).append(", ',', 1), '\"', ''), '{area:', ''), '#', 1)");
                        querySourceModuleFileds.append(" ELSE split_part(replace(replace(split_part(")
                            .append(queryField)
                            .append(", ',', 1), '\"', ''), '{area:', ''), '#', ")
                            .append(locationFormat)
                            .append(") END");
                    }
                    querySourceModuleFileds.append(" as ").append(asField).append(", ");
                }
                else
                {// 其他
                    querySourceModuleFileds.append(queryField).append(" as ").append(asField).append(", ");
                }
            }
            ReportUtil ru = new ReportUtil();
            /***** 高级条件连接 *****/
            JSONObject relevanceWhereJSON = ru.parseSeniorWhereByReport(seniorWhereArr, mainTable, querySourceModules.substring(0, querySourceModules.lastIndexOf(",")), null);
            if (null != relevanceWhereJSON)
            {
                String recordNumberWhere = relevanceWhereJSON.getString("recordNumberWhere");
                if (!StringUtil.isEmpty(recordNumberWhere))
                {
                    querySourceModuleWhere.append(" and ").append(recordNumberWhere);
                    relevanceWhereJSON.remove("recordNumberWhere");
                }
                else
                {
                    String seinorWhereStr = JSONParser4SQL.getSeniorWhere4Relation(relevanceWhereJSON);
                    querySourceModuleWhere.append(" and ").append(seinorWhereStr.replaceAll(Constant.VAR_QUOTES, "'"));
                }
            }
            
            /***** 构建完整查询sql语句 *****/
            querySourceModuleSql.append("select ");
            querySourceModuleSql.append(querySourceModuleFileds.substring(0, querySourceModuleFileds.lastIndexOf(",")));
            querySourceModuleSql.append(" from ").append(querySourceModules.substring(0, querySourceModules.lastIndexOf(",")));
            querySourceModuleSql.append(querySourceModuleWhere);
            if (!StringUtil.isEmpty(approvalBean))
            {
                querySourceModuleSql.append(approvalWhere);
            }
            String[] moduleArr = querySourceModules.toString().split(",");
            StringBuilder orderSB = new StringBuilder();
            for (String module : moduleArr)
            {
                if (module.contains(Constant.TYPE_SUBFORM) || StringUtil.isEmpty(module.trim()))
                {
                    continue;
                }
                orderSB.append(module).append(".").append(Constant.FIELD_CREATE_TIME).append(",");
            }
            querySourceModuleSql.append(" order by ").append(orderSB.substring(0, orderSB.lastIndexOf(","))).append(" desc");
            List<JSONObject> totalList = DAOUtil.executeQuery4JSON(querySourceModuleSql.toString());
            int totalRows = totalList.size();
            int totalPages = totalRows / pageSize;
            if (totalRows % pageSize > 0)
            {
                totalPages++;
            }
            querySourceModuleSql.append(" limit ").append(pageSize).append(" OFFSET ").append((pageNum - 1) * pageSize);
            List<JSONObject> pageList = DAOUtil.executeQuery4JSON(querySourceModuleSql.toString());
            for (JSONObject pateData : pageList)
            {
                Set<String> keys = pateData.keySet();
                for (String key : keys)
                {
                    if (key.contains(Constant.TYPE_MUTLI_PICKLIST))
                    {
                        JSONObject keyJSON = pateData.getJSONObject(key);
                        if (null != keyJSON)
                        {
                            pateData.put(key, keyJSON.getString("label"));
                        }
                    }
                }
            }
            
            /***** 封装返回前端的数据结构 *****/
            int curPageSize = pageList.size();
            titleJson.put("dataObj", titleList);
            dataJson.put("dataObj", pageList);
            sourceModueDataList.add(titleJson);
            sourceModueDataList.add(dataJson);
            result.put("list", sourceModueDataList);
            
            JSONObject pageInfo = new JSONObject();
            pageInfo.put("totalRows", totalRows);
            pageInfo.put("totalPages", totalPages);
            pageInfo.put("pageNum", pageNum);
            pageInfo.put("pageSize", pageSize);
            pageInfo.put("curPageSize", curPageSize);
            result.put("pageInfo", pageInfo);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return result;
    }
    
    /**
     * @param groupFields 分组字段
     * @param summaryFields 汇总字段
     * @param mainModule 当前模块bean
     * @param companyId 公司id
     * @return List
     * @Description:获取汇总式报表数据
     */
    private List<JSONObject> queryReportDataListBySummary(String chooseBean, JSONArray groupFields, JSONArray summaryFields, String dataSourceBean, JSONArray seniorWhereArr,
        long companyId, long employeeId)
    {
        List<JSONObject> sourceModueDataList = new ArrayList<JSONObject>();
        if (groupFields == null || groupFields.size() == 0 || summaryFields == null || summaryFields.size() == 0)
        {
            return sourceModueDataList;
        }
        try
        {
            ReportUtil ru = new ReportUtil();
            // 获取基础数据列表
            Map<String, Double> queryDataList = ru.queryReportDatas(chooseBean, groupFields, summaryFields, dataSourceBean, seniorWhereArr, companyId, employeeId, false);
            // 获取汇总数据
            List<JSONObject> dataList = new ArrayList<JSONObject>();
            if (null != queryDataList && queryDataList.size() != 0)
            {
                dataList = ru.getReportData4Table(queryDataList, true, true, true);
            }
            
            JSONObject summaryDatas = new JSONObject();
            summaryDatas.put("reportType", "data");
            summaryDatas.put("reportObj", dataList);
            sourceModueDataList.add(summaryDatas);
            
            // 标题
            List<JSONObject> titleList = new ArrayList<JSONObject>();
            for (Object groupFieldObj : groupFields)
            {
                JSONObject groupFieldJSON = (JSONObject)groupFieldObj;
                JSONObject titleJSON = new JSONObject();
                ;
                titleJSON.put("name",
                    new StringBuilder(groupFieldJSON.getString("bean")).append("_").append(companyId).append("_").append(groupFieldJSON.getString("name")).toString());
                titleJSON.put("label", groupFieldJSON.getString("label"));
                titleList.add(titleJSON);
            }
            for (Object summaryFieldObj : summaryFields)
            {
                JSONObject summaryFieldJSON = (JSONObject)summaryFieldObj;
                String fieldName = summaryFieldJSON.getString("name");
                JSONObject titleJSON = new JSONObject();
                if (fieldName.equals("recordNumber"))
                {
                    titleJSON.put("name", ru.FIELD_COUNT);
                }
                else
                {
                    String filedName = new StringBuilder(summaryFieldJSON.getString("bean")).append("_").append(companyId).append("_").append(fieldName).toString();
                    filedName = filedName.replace("_approval_", "_").replace("_subform_", "_");
                    filedName = filedName.length() > 64 ? filedName.substring(0, 63) : filedName;
                    titleJSON.put("name", filedName);
                }
                titleJSON.put("label", summaryFieldJSON.getString("label"));
                titleList.add(titleJSON);
            }
            JSONObject titleJson = new JSONObject();
            titleJson.put("reportType", "title");
            titleJson.put("reportObj", titleList);
            sourceModueDataList.add(titleJson);
            return sourceModueDataList;
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * @param chooseBean 所选模块
     * @param mainModule 主模块
     * @param rowGroupFields 行分组字段
     * @param colGroupFields 列分组字段
     * @param summaryFields 汇总字段
     * @param seniorWhereArr 高级筛选条件
     * @param companyId 公司id
     * @return
     * @Description:获取矩阵式报表数据
     */
    private JSONObject queryReportDataListByMatrix(String chooseBean, String mainModule, JSONArray rowGroupFields, JSONArray colGroupFields, JSONArray summaryFields,
        JSONArray seniorWhereArr, long companyId, long employeeId)
    {
        JSONObject dataJSON = new JSONObject();
        if (rowGroupFields == null || rowGroupFields.size() == 0 || colGroupFields == null || colGroupFields.size() == 0 || summaryFields == null || summaryFields.size() == 0)
        {
            return new JSONObject();
        }
        try
        {
            ReportUtil ru = new ReportUtil();
            dataJSON = ru.queryReportDatasForMatrix(chooseBean, mainModule, rowGroupFields, colGroupFields, summaryFields, seniorWhereArr, companyId, employeeId);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return dataJSON;
    }
    
    private boolean saveClickHist(long companyId, List<Object> values)
    {
        String clickHistTable = DAOUtil.getTableName("report_click_hist", companyId);
        StringBuilder insertSql = new StringBuilder();
        insertSql.append("insert into ").append(clickHistTable).append("(report_id, create_by, create_time) values(?,?,?)");
        int insertClickHistResult = DAOUtil.executeUpdate(insertSql.toString(), values.toArray());
        if (insertClickHistResult < 1)
        {
            log.warn("insert click report history operation fail!!!");
            return false;
        }
        return true;
    }
}