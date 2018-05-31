package com.teamface.custom.service.employee;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.Constant;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.model.TreeNode;
import com.teamface.common.util.JsonResUtil;
import com.teamface.common.util.MD5Util;
import com.teamface.common.util.RandomUtil;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JSONParser4SQL;
import com.teamface.common.util.dao.RedisUtil;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.common.util.sendMessageUtil.SendMessage;
import com.teamface.custom.async.CustomAsyncHandle;
import com.teamface.custom.service.im.ImChatService;
import com.teamface.custom.service.im.ImCircleAppService;
import com.teamface.custom.service.library.FileLibraryAppService;
import com.teamface.custom.service.push.MessagePushService;

/**
 * @Description:
 * @author: liupan
 * @date: 2017年9月19日 上午11:58:41
 * @version: 1.0
 */

@Service("employeeAppService")
public class EmployeeAppServiceImpl implements EmployeeAppService
{
    
    private static Logger log = Logger.getLogger(EmployeeAppServiceImpl.class);
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    @Autowired
    private ImCircleAppService imCircleAppService;
    
    @Autowired
    private ImChatService imChatService;
    
    @Autowired
    private MessagePushService messagePushService;
    
    @Autowired
    FileLibraryAppService fileLibraryAppService;
    
    /**
     * 增加员工资料
     */
    @Override
    public ServiceResult<String> savaEmployee(Map<String, Object> map)
    {
        // 先隐藏 后期需要放开
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            Long companyId = info.getCompanyId();
            String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
            
            JSONObject obj = new JSONObject();
            obj.put("bean", "employee");
            obj.put("data", map.get("data"));
            StringBuilder builder = new StringBuilder("select count(*) from ").append(employeeTable)
                .append(" where phone = '")
                .append(obj.getJSONObject("data").getString("phone").trim())
                .append("' and del_status=")
                .append(Constant.CURRENCY_ZERO);
            int countNum = DAOUtil.executeCount(builder.toString());
            if (countNum > 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), "当前手机已存在");
                return serviceResult;
            }
            String insertSql = JSONParser4SQL.getInsertSql(obj, companyId == null ? "" : String.valueOf(companyId));
            int sum = DAOUtil.executeUpdate(insertSql); // 添加员工资料
            if (sum <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            
            Long id = BusinessDAOUtil.geCurrval4Table("employee", companyId == null ? "" : String.valueOf(companyId));
            String departmentName = DAOUtil.getTableName("department_center", info.getCompanyId());
            String departmentId = map.get("departmentId").toString(); // 部门
            String[] idArray = departmentId.split(",");
            List<List<Object>> batchValues = new ArrayList<>();
            if (idArray.length > 0)
            {
                for (String deparId : idArray)
                {
                    List<Object> model = new ArrayList<>();
                    model.add(Long.parseLong(deparId)); // 部门id
                    model.add(id); // 员工id
                    if (map.get("mainId").equals(deparId))
                    {
                        model.add(Constant.CURRENCY_ONE);
                    }
                    else
                    {
                        model.add(Constant.CURRENCY_ZERO);
                    }
                    batchValues.add(model);
                }
                StringBuilder buil = new StringBuilder("insert into ").append(departmentName).append("(department_id,employee_id,is_main) values(?,?,?)");
                int count = DAOUtil.executeUpdate(buil.toString(), batchValues, 10);
                if (count <= 0)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                    return serviceResult;
                }
            }
            
            String accountName = DAOUtil.getTableName("account", "");
            String acountinfoName = DAOUtil.getTableName("acountinfo", "");
            String companyName = DAOUtil.getTableName("company", "");
            String employeeBean = DAOUtil.getTableName("employee", info.getCompanyId());
            JSONObject dataJson = obj.getJSONObject("data");
            StringBuilder buf = new StringBuilder("select * from ").append(accountName).append(" where mobile = '").append(dataJson.getString("phone")).append("'");
            List<JSONObject> listMap = DAOUtil.executeQuery4JSON(buf.toString());
            StringBuilder bufBuilder = new StringBuilder("select employee_name from ").append(employeeBean).append(" where id = ").append(info.getEmployeeId());
            Object employeeName = DAOUtil.executeQuery4Object(bufBuilder.toString());
            if (listMap.isEmpty())
            {
                String passWord = RandomUtil.genRandomNum(); // 生成随机密码
                StringBuilder savaBuf = new StringBuilder();
                savaBuf.append("insert into ")
                    .append(accountName)
                    .append("(login_name, login_pwd, mobile,create_time,is_company) values('")
                    .append(dataJson.getString("phone"))
                    .append("','")
                    .append(MD5Util.MD5(MD5Util.MD5(passWord + "hjhq2017Teamface")))
                    .append("','")
                    .append(dataJson.getString("phone"))
                    .append("',")
                    .append(System.currentTimeMillis())
                    .append(",'1')");
                DAOUtil.executeUpdate(savaBuf.toString()); // 添加中间表信息
                Long objDa = BusinessDAOUtil.geCurrval4Table("account", "");
                StringBuilder addBuf = new StringBuilder("insert into ").append(acountinfoName)
                    .append(" (company_id, employee_id, account_id) values('")
                    .append(info.getCompanyId())
                    .append("','")
                    .append(id)
                    .append("','")
                    .append(objDa)
                    .append("')");
                DAOUtil.executeUpdate(addBuf.toString()); // 添加中间表信息
                Long acountinfoId = BusinessDAOUtil.geCurrval4Table("acountinfo", "");
                // Redis
                RedisUtil.commonRedis(acountinfoId.toString());
                
                StringBuilder editBuilder = new StringBuilder();
                editBuilder.append(" update   ").append(accountName).append(" set is_white = ").append(Constant.CURRENCY_ONE).append(" where id =").append(info.getAccountId());
                DAOUtil.executeUpdate(editBuilder.toString());
                
                // 群加人
                serviceResult = imChatService.updateBigGroup(map.get("token").toString(), acountinfoId, 0l);
                int type = 4;
                // 发送短信
                SendMessage.sendMessage(dataJson.getString("phone"), null, null, type, null, employeeName.toString(), passWord, null);
            }
            else
            {
                
                StringBuilder saveBuf = new StringBuilder("insert into acountinfo (company_id, employee_id, account_id) values('").append(info.getCompanyId())
                    .append("','")
                    .append(id)
                    .append("','")
                    .append(listMap.get(0).get("id"))
                    .append("')");
                DAOUtil.executeUpdate(saveBuf.toString()); // 添加中间表信息
                
                StringBuilder queryBuf = new StringBuilder("select company_name from ").append(companyName).append(" where id = ").append(companyId);
                Object object = DAOUtil.executeQuery4Object(queryBuf.toString());
                Long acountinfoId = BusinessDAOUtil.geCurrval4Table("acountinfo", "");
                
                // Redis
                RedisUtil.commonRedis(acountinfoId.toString());
                // 群加人
                imChatService.updateBigGroup(map.get("token").toString(), acountinfoId, 0L);
                
                int type = 6;
                // 发送短信
                SendMessage.sendMessage(dataJson.getString("phone"), null, null, type, null, employeeName.toString(), null, object.toString());
            }
            fileLibraryAppService.savaFileMember(map.get("token").toString(), id);
            messagePushService.pushCatalogMessage(companyId, 1000);
        }
        catch (NumberFormatException e)
        {
            log.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 编辑员工信息
     * 
     */
    @Override
    public ServiceResult<String> editEmployee(Map<String, Object> map)
    {
        // 先隐藏 后期需要放开
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            Long companyId = info.getCompanyId();
            JSONObject obj = new JSONObject();
            obj.put("bean", "employee");
            obj.put("data", map.get("data"));
            obj.put("id", map.get("id"));
            String updateSql = JSONParser4SQL.getUpdateSql(obj, companyId == null ? "" : String.valueOf(companyId));
            int number = DAOUtil.executeUpdate(updateSql); // 修改员工资料
            if (number <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            String departmentName = DAOUtil.getTableName("department_center", companyId);
            StringBuilder buf = new StringBuilder("delete  from ").append(departmentName).append(" where employee_id =").append(map.get("id")).append(" and status = 0 ");
            int sum = DAOUtil.executeUpdate(buf.toString()); // 修改员工部门中间表
            if (sum <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            String departmentId = map.get("departmentId").toString();
            String[] idArray = departmentId.split(",");
            List<List<Object>> batchValues = new ArrayList<>();
            if (idArray.length > 0)
            {
                for (String deparId : idArray)
                {
                    List<Object> model = new ArrayList<>();
                    model.add(Long.parseLong(deparId)); // //部门id
                    model.add(Long.parseLong(map.get("id").toString())); // 员工id
                    if (map.get("mainId").equals(deparId))
                    {
                        model.add(Constant.CURRENCY_ONE);
                    }
                    else
                    {
                        model.add(Constant.CURRENCY_ZERO);
                    }
                    batchValues.add(model);
                    
                }
                StringBuilder builder = new StringBuilder("insert into ").append(departmentName).append("(department_id,employee_id,is_main) values(?,?,?)");
                int count = DAOUtil.executeUpdate(builder.toString(), batchValues, 100000);
                if (count <= 0)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                    return serviceResult;
                }
            }
            messagePushService.pushCatalogMessage(companyId, 1000);
            
            JSONObject reqJSON = new JSONObject();
            reqJSON.put("companyId", companyId);
            reqJSON.put("employeeId", info.getEmployeeId());
            CustomAsyncHandle customHandle = new CustomAsyncHandle(null, reqJSON);
            customHandle.modifyUserInfo();
        }
        catch (NumberFormatException e)
        {
            log.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 查询 未启用的员工
     * 
     */
    @Override
    public JSONObject queryEmployee(Map<String, Object> map)
    {
        // 先隐藏 后期需要放开
        JSONObject result = new JSONObject();
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String departmentName = DAOUtil.getTableName("department_center", info.getCompanyId());
        String postName = DAOUtil.getTableName("post", info.getCompanyId());
        String roleName = DAOUtil.getTableName("role", info.getCompanyId());
        String employeeName = DAOUtil.getTableName("employee", info.getCompanyId());
        StringBuilder builder = new StringBuilder();
        String str = commSql(map);
        builder.append("SELECT    e.id,e.picture,d.department_id,d.leader,e.employee_name,e.account,e.phone,e.is_enable,p.name as post_name,r.name  as role_name,e.role_id  FROM ")
            .append(employeeName)
            .append("  e  LEFT JOIN ")
            .append(postName)
            .append(" P ON e.post_id = p.id LEFT JOIN ")
            .append(roleName)
            .append("  r  on e.role_id  = r.id  left join ")
            .append(departmentName)
            .append("  d on e.id = d.employee_id   WHERE d.status = 0 and e.status=0 and e.del_status = 0  ")
            .append(str);
        int pageSize = Integer.parseInt(map.get("pageSize").toString());
        int pageNum = Integer.parseInt(map.get("pageNum").toString());
        result = BusinessDAOUtil.getTableDataListAndPageInfo(builder.toString(), pageSize, pageNum);
        return result;
    }
    
    /**
     * 拼接查询条件
     * 
     * @param map
     * @return
     * @Description:
     */
    private String commSql(Map<String, Object> map)
    {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotBlank(map.get("roleId").toString()))
        {
            builder.append(" and r.id = ").append(map.get("roleId").toString());
        }
        if (StringUtils.isNotBlank(map.get("status").toString()))
        {
            builder.append(" and e.status = ").append(map.get("status").toString());
        }
        if (StringUtils.isNotBlank(map.get("departmentId").toString()))
        {
            builder.append(" and d.department_id  = ").append(map.get("departmentId").toString());
        }
        if (StringUtils.isNotBlank(map.get("employeeName").toString()))
        {
            builder.append(" and e.employee_name   LIKE  '%").append(map.get("employeeName").toString()).append("%'");
        }
        if (StringUtils.isNotBlank(map.get("name").toString()))
        {
            builder.append(" and (e.employee_name  LIKE  '%")
                .append(map.get("name").toString())
                .append("%' or e.phone LIKE '%")
                .append(map.get("name").toString())
                .append("%' or e.account LIKE '%")
                .append(map.get("name").toString())
                .append("%')");
        }
        builder.append(" order by id asc");
        return builder.toString();
    }
    
    /**
     * 添加职位
     * 
     */
    @Override
    public ServiceResult<String> savaPost(Map<String, Object> map)
    {
        // 先隐藏 后期需要放开
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String postName = DAOUtil.getTableName("post", info.getCompanyId());
        ServiceResult<String> serviceResult = new ServiceResult<>();
        StringBuilder builder = new StringBuilder("insert into ").append(postName).append(" (name) values('").append(map.get("name")).append("')");
        int count = DAOUtil.executeUpdate(builder.toString());
        if (count <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 修改职位
     * 
     */
    @Override
    public ServiceResult<String> editPost(Map<String, Object> map)
    {
        // 先隐藏 后期需要放开
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String postName = DAOUtil.getTableName("post", info.getCompanyId());
        ServiceResult<String> serviceResult = new ServiceResult<>();
        StringBuilder builder = new StringBuilder("update ").append(postName).append(" set name = '").append(map.get("name")).append("' where id = ").append(map.get("id"));
        int count = DAOUtil.executeUpdate(builder.toString());
        if (count <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        JSONObject reqJSON = new JSONObject();
        reqJSON.put("companyId", info.getCompanyId());
        reqJSON.put("employeeId", info.getEmployeeId());
        CustomAsyncHandle customHandle = new CustomAsyncHandle(null, reqJSON);
        customHandle.modifyUserInfo();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 删除职务
     * 
     */
    @Override
    public ServiceResult<String> delPost(Map<String, Object> map)
    {
        // 先隐藏 后期需要放开
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String postName = DAOUtil.getTableName("post", info.getCompanyId());
            String employeeName = DAOUtil.getTableName("employee", info.getCompanyId());
            StringBuilder builder = new StringBuilder("update ").append(postName).append(" set status='").append(Constant.CURRENCY_ONE).append("' where id=").append(map.get("id"));
            int count = DAOUtil.executeUpdate(builder.toString());
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            StringBuilder buil = new StringBuilder("update ").append(employeeName).append(" set post_id = null where post_id=").append(map.get("id"));
            DAOUtil.executeUpdate(buil.toString());
            JSONObject reqJSON = new JSONObject();
            reqJSON.put("companyId", info.getCompanyId());
            reqJSON.put("employeeId", info.getEmployeeId());
            CustomAsyncHandle customHandle = new CustomAsyncHandle(null, reqJSON);
            customHandle.modifyUserInfo();
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
     * 职位列表
     * 
     */
    @Override
    public List<Map<String, Object>> queryPost(Map<String, Object> map)
    {
        // 先隐藏 后期需要放开
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String postName = DAOUtil.getTableName("post", info.getCompanyId());
        StringBuilder builder = new StringBuilder("select * from  ").append(postName).append(" where status = ").append(Constant.CURRENCY_ZERO);
        if (StringUtils.isNotBlank(map.get("name").toString()))
        {
            builder.append(" and name = '").append(map.get("name")).append("'");
        }
        builder.append(" order by id desc");
        return DAOUtil.executeQuery(builder.toString());
    }
    
    /**
     * 编辑部门
     * 
     */
    @Override
    public ServiceResult<String> editDepartment(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String departmentName = DAOUtil.getTableName("department", info.getCompanyId());
            StringBuilder builder =
                new StringBuilder("update ").append(departmentName).append(" set department_name ='").append(map.get("departmentName")).append("' where id=").append(map.get("id"));
            int count = DAOUtil.executeUpdate(builder.toString());
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            messagePushService.pushCatalogMessage(info.getCompanyId(), 1000);
            
            JSONObject reqJSON = new JSONObject();
            reqJSON.put("companyId", info.getCompanyId());
            reqJSON.put("employeeId", info.getEmployeeId());
            CustomAsyncHandle customHandle = new CustomAsyncHandle(null, reqJSON);
            customHandle.modifyUserInfo();
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
     * 删除部门
     * 
     */
    @Override
    public ServiceResult<String> delDepartment(Map<String, Object> map)
    {
        // 先隐藏 后期需要放开
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String departmentName = DAOUtil.getTableName("department", info.getCompanyId());
            String centerName = DAOUtil.getTableName("department_center", info.getCompanyId());
            StringBuilder builder = new StringBuilder();
            String sb = BusinessDAOUtil.getDepments(info.getCompanyId(), Long.parseLong(map.get("id").toString()), 1);
            if ("".equals(sb) || null == sb)
            {
                builder.append(map.get("id").toString());
            }
            else
            {
                builder.append(sb).append(",").append(map.get("id").toString());
            }
            StringBuilder buil =
                new StringBuilder("select count(*) from ").append(centerName).append(" where department_id in (").append(builder.toString()).append(")  and status=0");
            int count = DAOUtil.executeCount(buil.toString());
            if (count > 0)
            { // 部门下是否存在员工
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("postprocess.department.employee.error"));
                return serviceResult;
            }
            else
            {
                StringBuilder buf = new StringBuilder("update ").append(centerName).append(" set status = 1  where department_id=").append(map.get("id"));
                DAOUtil.executeUpdate(buf.toString());
                if (count > 0)
                { // 修改部门中间表
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                    return serviceResult;
                }
                StringBuilder editBuf = new StringBuilder("update ").append(departmentName).append(" set status = 1  where id=").append(map.get("id"));
                DAOUtil.executeUpdate(editBuf.toString());
                if (count > 0)
                { // 修改部门表
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                    return serviceResult;
                }
            }
            messagePushService.pushCatalogMessage(info.getCompanyId(), 1000);
            JSONObject reqJSON = new JSONObject();
            reqJSON.put("companyId", info.getCompanyId());
            reqJSON.put("employeeId", info.getEmployeeId());
            CustomAsyncHandle customHandle = new CustomAsyncHandle(null, reqJSON);
            customHandle.modifyUserInfo();
        }
        catch (NumberFormatException e)
        {
            log.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 查询公司部门架构
     * 
     */
    @Override
    public JSONArray findCompanyDepartment(String token)
    {
        // 先隐藏 后期需要放开
        InfoVo info = TokenMgr.obtainInfo(token);
        String departmentTable = DAOUtil.getTableName("department", info.getCompanyId());
        String centerTable = DAOUtil.getTableName("department_center", info.getCompanyId());
        String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
        String postTable = DAOUtil.getTableName("post", info.getCompanyId());
        StringBuilder builder = new StringBuilder(" select * from ").append(departmentTable).append(" where status=").append(Constant.CURRENCY_ZERO).append(" order by id asc");
        List<JSONObject> o = DAOUtil.executeQuery4JSON(builder.toString());
        StringBuilder queryBuilde = new StringBuilder();
        queryBuilde.append(" select department_id as department_id,employee_name as employee_name,name as name,e.id as id from ")
            .append(employeeTable)
            .append(" e LEFT JOIN ")
            .append(postTable)
            .append(" p   ON   e.post_id = p.id  left join ")
            .append(centerTable)
            .append(" d on e.id =d.employee_id  where d.status = 0 and e.status = 0  and e.del_status = 0 ");
        // users
        List<JSONObject> users = DAOUtil.executeQuery4JSON(queryBuilde.toString());
        
        List<TreeNode> dataList = new ArrayList<>();
        for (JSONObject j : o)
        {
            String parentId = j.getString("parent_id");
            if (parentId == null || parentId.equals(""))
            {
                TreeNode tl = new TreeNode();
                tl.setId(j.getLong("id"));
                tl.setName(j.getString("department_name"));
                tl.setValue("0-" + j.getLong("id"));// 0部门 1成员 2角色 3 动态成员 4 公司
                dataList.add(tl);
                this.getChildDept(o, users, tl);
            }
        }
        String jsonString = JSON.toJSONString(dataList);
        JSONArray jsonArray = JSONArray.parseArray(jsonString);
        return jsonArray;
        
    }
    
    private void getChildDept(List<JSONObject> o, List<JSONObject> users, TreeNode tl)
    {
        tl.setChildList(new ArrayList<TreeNode>());
        for (JSONObject j : o)
        {
            Long parentId = j.getLong("parent_id");
            if (parentId != null && null != tl.getId() && parentId.equals(tl.getId()))
            {
                TreeNode t = new TreeNode();
                t.setId(j.getLong("id"));
                t.setName(j.getString("department_name"));
                t.setValue("0-" + j.getLong("id"));// 0部门 1成员 2角色 3 动态成员 4 公司
                t.setParentId(parentId);
                tl.getChildList().add(t);
                getChildDept(o, users, t);
            }
        }
    }
    
    /**
     * 添加部门信息
     * 
     */
    @Override
    public JSONObject savaDepartment(Map<String, Object> map)
    {
        JSONObject  jsonObject = new JSONObject();
        try
        {
            String token = map.get("token").toString();
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            String departmentTable = DAOUtil.getTableName("department", info.getCompanyId());
            StringBuilder builder = new StringBuilder("insert into ").append(departmentTable)
                .append(" (department_name,parent_id) values('")
                .append(map.get("departmentName"))
                .append("','")
                .append(map.get("parentId"))
                .append("')");
            int count = DAOUtil.executeUpdate(builder.toString());
            if (count <= 0)
            {
                return JsonResUtil.getResultJsonByIdent("common.fail");
            }
            
            Long id = BusinessDAOUtil.geCurrval4Table("department", info.getCompanyId().toString());
            jsonObject.put("departmentName", map.get("departmentName"));
            jsonObject.put("departmentId", id);
            jsonObject.put("parentId", map.get("parentId"));
            messagePushService.pushCatalogMessage(companyId, 1000);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            return JsonResUtil.getResultJsonByIdent("common.fail");
        }
        return jsonObject;
    }
    
    /**
     * 批量修改员工
     * 
     */
    @Override
    public ServiceResult<String> betchEditEmployee(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        // 先隐藏 后期需要放开
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            Long companyId = info.getCompanyId();
            String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
            String centerTable = DAOUtil.getTableName("department_center", info.getCompanyId());
            String acountinfoTable = DAOUtil.getTableName("acountinfo", "");
            StringBuilder builder = new StringBuilder();
            StringBuilder buil = new StringBuilder();
            StringBuilder queryBuilder = new StringBuilder();
            StringBuilder editBuilder = new StringBuilder();
            // 0 禁用 1启用
            if (Integer.parseInt(map.get("status").toString()) == Constant.CURRENCY_ONE)
            {
                // 修改员工状态
                builder.append("update  ").append(employeeTable).append(" set status = ").append(Constant.CURRENCY_ZERO).append(" where id in (").append(map.get("id")).append(")");
                // 修改账户信息中间表信息
                buil.append("update ")
                    .append(acountinfoTable)
                    .append(" set status = ")
                    .append(Constant.CURRENCY_ZERO)
                    .append(" where company_id =")
                    .append(companyId)
                    .append(" and employee_id in (")
                    .append(map.get("id"))
                    .append(")");
                queryBuilder.append("select * from ").append(acountinfoTable).append("  where employee_id in (").append(map.get("id")).append(") and company_id = ").append(
                    info.getCompanyId());
                List<JSONObject> json = DAOUtil.executeQuery4JSON(queryBuilder.toString());
                
                for (int i = 0; i < json.size(); i++)
                {
                    JSONObject object = json.get(i);
                    RedisUtil.commonRedis(object.getString("id"));// 企信Redis
                    serviceResult = imChatService.updateBigGroup(map.get("token").toString(), object.getLong("id"), 1L); // 群人员维护
                }
                
            }
            else if (Integer.parseInt(map.get("status").toString()) == Constant.CURRENCY_ZERO)
            {
                // 修改员工状态
                builder.append("update  ").append(employeeTable).append(" set  status = ").append(Constant.CURRENCY_ONE).append(" where id in (").append(map.get("id")).append(")");
                // 修改账户信息中间表信息
                buil.append("update acountinfo set status = ")
                    .append(Constant.CURRENCY_ONE)
                    .append(" where company_id =")
                    .append(companyId)
                    .append(" and employee_id in (")
                    .append(map.get("id"))
                    .append(")");
                queryBuilder.append("select *  from ").append(acountinfoTable).append("  where employee_id in (").append(map.get("id")).append(") and company_id = ").append(
                    info.getCompanyId());
                editBuilder.append("UPDATE ")
                    .append(centerTable)
                    .append(" set leader = ")
                    .append(Constant.CURRENCY_ZERO)
                    .append(" WHERE employee_id in (")
                    .append(map.get("id"))
                    .append(")");
                List<JSONObject> json = DAOUtil.executeQuery4JSON(queryBuilder.toString());
                for (int i = 0; i < json.size(); i++)
                {
                    JSONObject object = json.get(i);
                    RedisUtil.delRedis(object.getLong("id"));// 企信Redis
                    serviceResult = imChatService.removePeopleFromGroup(map.get("token").toString(), object.getLong("id"));// 群人员维护
                }
                int sumCount = DAOUtil.executeUpdate(editBuilder.toString());
                log.debug("主部门============" + sumCount);
            }
            int count = DAOUtil.executeUpdate(builder.toString());
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            int sum = DAOUtil.executeUpdate(buil.toString());
            if (sum <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            messagePushService.pushCatalogMessage(info.getCompanyId(), 1000);
            JSONObject reqJSON = new JSONObject();
            reqJSON.put("companyId", info.getCompanyId());
            reqJSON.put("employeeId", info.getEmployeeId());
            CustomAsyncHandle customHandle = new CustomAsyncHandle(null, reqJSON);
            customHandle.modifyUserInfo();
        }
        catch (NumberFormatException e)
        {
            log.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 查询父级部门
     * 
     */
    @Override
    public JSONObject queryEmployeeDepartment(String id, String token)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        String departmentName = DAOUtil.getTableName("department", info.getCompanyId());
        StringBuilder builder = new StringBuilder("select * from ").append(departmentName).append(" where id = ").append(Long.parseLong(id));
        return DAOUtil.executeQuery4FirstJSON(builder.toString());
    }
    
    /**
     * 批量调整部门
     * 
     */
    @Override
    public ServiceResult<String> betchEditDepartment(Map<String, Object> map)
    {
        // 先隐藏 后期需要放开
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String ids = map.get("employeeId").toString();
            String departmentName = DAOUtil.getTableName("department_center", info.getCompanyId());
            String[] idArray = ids.split(",");
            StringBuilder buf = new StringBuilder();
            // 删除之前部门中间表信息
            buf.append(" delete  FROM ").append(departmentName).append(" where ").append(" employee_id in (").append(ids).append(")");
            int sum = DAOUtil.executeUpdate(buf.toString());
            if (sum <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            List<List<Object>> batchValues = new ArrayList<>();
            if (idArray.length > 0)
            {
                for (String id : idArray)
                {
                    List<Object> model = new ArrayList<>();
                    model.add(Long.parseLong(map.get("departmentId").toString())); // 部门id
                    model.add(Long.parseLong(id)); // 员工id
                    model.add(Constant.CURRENCY_ONE);
                    batchValues.add(model);
                }
                // 添加部门中间表信息
                StringBuilder buil = new StringBuilder("insert into ").append(departmentName).append("(department_id,employee_id,is_main) values(?,?,?)");
                int count = DAOUtil.executeUpdate(buil.toString(), batchValues, 10);
                if (count <= 0)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                    return serviceResult;
                }
            }
            JSONObject reqJSON = new JSONObject();
            reqJSON.put("companyId", info.getCompanyId());
            reqJSON.put("employeeId", info.getEmployeeId());
            CustomAsyncHandle customHandle = new CustomAsyncHandle(null, reqJSON);
            customHandle.modifyUserInfo();
        }
        catch (NumberFormatException e)
        {
            log.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 激活提醒
     * 
     */
    @Override
    public JSONObject activateRemind(Map<String, Object> map)
    {
        // 先隐藏 后期需要放开
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        JSONObject obj = new JSONObject();
        try
        {
            String ids = map.get("id").toString();
            String[] idArray = ids.split(",");
            if (idArray.length > 0)
            {
                for (String id : idArray)
                {
                    commonActivateRemind(id, info);
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return obj;
        
    }
    
    /**
     * 公共激活提醒处理
     * 
     * @param id
     * @param info
     * @return
     * @throws ParseException
     * @Description:
     */
    private JSONObject commonActivateRemind(String id, InfoVo info)
    {
        JSONObject obj = new JSONObject();
        Long companyId = info.getCompanyId();
        String activateTable = DAOUtil.getTableName("activate_record", info.getCompanyId());
        String companyTable = DAOUtil.getTableName("company", "");
        String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
        StringBuilder builder =
            new StringBuilder("select * from ").append(activateTable).append(" where employee_id=").append(Long.parseLong(id)).append(" order by datetime_time desc limit 1");
        List<JSONObject> objData = DAOUtil.executeQuery4JSON(builder.toString());
        if (null != objData && objData.size() > 0)
        {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String da = format.format(objData.get(0).getLongValue("datetime_time"));
            String data = format.format(System.currentTimeMillis());
            Date date = null;
            Date now = null;
            try
            {
                date = format.parse(da);
                now = format.parse(data);
                int days = (int)((now.getTime() - date.getTime()) / (1000 * 3600 * 24));
                if (days <= 7)
                { // 7天才能提醒
                    return JsonResUtil.getResultJsonByIdent("上次提醒未超过7天");
                }
            }
            catch (ParseException e)
            {
                log.error(e.getMessage(), e);
            }
            
        }
        StringBuilder buil = new StringBuilder("insert into ").append(activateTable)
            .append(" (employee_id,datetime_time) values('")
            .append(Long.parseLong(id))
            .append("','")
            .append(System.currentTimeMillis())
            .append("')");
        DAOUtil.executeUpdate(buil.toString()); // 激活记录表添加
        
        StringBuilder queryBuilder = new StringBuilder("select * from ").append(employeeTable).append(" where id=").append(Long.parseLong(id)); // 查询员工信息
        List<JSONObject> queryMap = DAOUtil.executeQuery4JSON(queryBuilder.toString());
        if (queryMap.isEmpty())
        {
            return obj;
        }
        StringBuilder buf = new StringBuilder("select employee_name from ").append(employeeTable).append(" where id = ").append(info.getEmployeeId());
        Object employeeName = DAOUtil.executeQuery4Object(buf.toString());
        StringBuilder queryBuf = new StringBuilder("select company_name from ").append(companyTable).append(" where id = ").append(companyId);
        Object object = DAOUtil.executeQuery4Object(queryBuf.toString());
        int type = 6;
        // 发送短信
        SendMessage.sendMessage(queryMap.get(0).getString("phone"), null, null, type, null, employeeName.toString(), null, object.toString());
        
        return obj;
    }
    
    /**
     * 编辑负责人
     * 
     */
    @Override
    public ServiceResult<String> editLeader(Map<String, Object> map)
    {
        // 先隐藏 后期需要放开
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String departmentTable = DAOUtil.getTableName("department_center", info.getCompanyId());
            StringBuilder builder = new StringBuilder("UPDATE ").append(departmentTable).append(" set leader = 0 where department_id=").append(map.get("departmentId"));
            int num = DAOUtil.executeUpdate(builder.toString());
            if (num <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            StringBuilder editBuilder = new StringBuilder("update ").append(departmentTable)
                .append(" set leader = 1 where department_id=")
                .append(map.get("departmentId"))
                .append(" and employee_id=")
                .append(map.get("employeeId"));
            int count = DAOUtil.executeUpdate(editBuilder.toString());
            if (count <= 0)
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
     * 已停用员工列表
     * 
     */
    @Override
    public JSONObject queryEmployeeList(Map<String, Object> map)
    {
        JSONObject result = new JSONObject();
        // 先隐藏 后期需要放开
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
        String postTable = DAOUtil.getTableName("post", info.getCompanyId());
        String roleTable = DAOUtil.getTableName("role", info.getCompanyId());
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT   e.id,e.employee_name,e.account,e.phone,e.is_enable,p.name as post_name,r.name  as role_name  FROM ")
            .append(employeeTable)
            .append("  e  LEFT JOIN ")
            .append(postTable)
            .append(" P ON e.post_id = p.id LEFT JOIN ")
            .append(roleTable)
            .append("  r  on e.role_id  = r.id  WHERE   e.status=1 and e.del_status = 0  ");
        if (StringUtils.isNotBlank(map.get("roleId").toString()))
        {
            builder.append(" and r.id = ").append(map.get("roleId").toString());
        }
        int pageSize = Integer.parseInt(map.get("pageSize").toString());
        int pageNum = Integer.parseInt(map.get("pageNum").toString());
        result = BusinessDAOUtil.getTableDataListAndPageInfo(builder.toString(), pageSize, pageNum);
        return result;
    }
    
    /**
     * 获取员工
     * 
     */
    @Override
    public Map<String, Object> getEmployeeDetail(Map<String, Object> map)
    {
        // 先隐藏 后期需要放开
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
        String postTable = DAOUtil.getTableName("post", info.getCompanyId());
        String roleTable = DAOUtil.getTableName("role", info.getCompanyId());
        String departmentTable = DAOUtil.getTableName("department", info.getCompanyId());
        String centerTable = DAOUtil.getTableName("department_center", info.getCompanyId());
        Map<String, Object> resultMap = new HashMap<>();
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT e.ID,e.employee_name,e.account,e.phone,e.post_id ,e.role_id ,P.NAME AS post_name,r.NAME AS role_name FROM ")
            .append(employeeTable)
            .append(" e LEFT JOIN ")
            .append(postTable)
            .append(" P ON e.post_id = P.ID LEFT JOIN ")
            .append(roleTable)
            .append(" r ON e.role_id = r. ID  where e.id=")
            .append(map.get("id"));
        resultMap.put("detil", DAOUtil.executeQuery4JSON(builder.toString()).get(0));
        StringBuilder buf = new StringBuilder();
        buf.append("SELECT d.department_id,t.department_name,d.is_main  from ")
            .append(centerTable)
            .append("  d LEFT JOIN ")
            .append(departmentTable)
            .append(" t on d.department_id = t.id where   employee_id = ")
            .append(map.get("id"))
            .append(" and d.status = 0 and t.status = 0");
        resultMap.put("department", DAOUtil.executeQuery4JSON(buf.toString()));
        return resultMap;
    }
    
    /**
     * 获取员工ID集合和公司ID获取信息
     * 
     */
    @Override
    public List<JSONObject> queryEmployeeDetail(String str, Long companyId)
    {
        if (StringUtils.isBlank(str))
        {
            return null;
        }
        String employeeTable = DAOUtil.getTableName("employee", companyId);
        String postTable = DAOUtil.getTableName("post", companyId);
        StringBuilder builder = new StringBuilder();
        builder.append(
            "select e.id,e.employee_name,e.employee_name as name,e.picture,e.leader,e.phone,e.mobile_phone,e.birth,e.region,e.email,e.status,e.account,e.is_enable,e.post_id,e.role_id,e.del_status,e.microblog_background,e.sex,e.sign,e.mood,e.personnel_create_by,e.datetime_create_time,p.name as post_name from ")
            .append(employeeTable)
            .append(" e left join ")
            .append(postTable)
            .append(" p on e.post_id=p.id  where e.id in(")
            .append(str)
            .append(")");
        return DAOUtil.executeQuery4JSON(builder.toString());
    }
    
    /**
     * 根据ID集合获取员工职务信息
     * 
     */
    @Override
    public List<JSONObject> queryEmployeeSomeFields(String str, Long companyId)
    {
        if (StringUtils.isBlank(str))
        {
            return null;
        }
        String employeeTable = DAOUtil.getTableName("employee", companyId);
        String postTable = DAOUtil.getTableName("post", companyId);
        StringBuilder sql = new StringBuilder();
        sql.append("select e.id,e.employee_name,e.picture from ")
            .append(employeeTable)
            .append(" e left join ")
            .append(postTable)
            .append(" p on e.post_id=p.id where e.id in(")
            .append(str)
            .append(")");
        return DAOUtil.executeQuery4JSON(sql.toString());
    }
    
    /**
     * 批量删除员工
     * 
     */
    @Override
    public ServiceResult<String> betchDelEmployee(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            // 先隐藏 后期需要放开
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            Long companyId = info.getCompanyId();
            String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
            String acountinfoble = DAOUtil.getTableName("acountinfo", info.getCompanyId());
            
            // 修改员工状态
            StringBuilder builder = new StringBuilder("update ").append(employeeTable)
                .append(" set  del_status =")
                .append(Constant.CURRENCY_ONE)
                .append(" where  id in (")
                .append(map.get("id"))
                .append(")");
            int count = DAOUtil.executeUpdate(builder.toString());
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            // 修改账户信息中间表状态
            StringBuilder buil = new StringBuilder("update ").append(acountinfoble)
                .append(" set status =")
                .append(Constant.CURRENCY_ONE)
                .append(" where company_id =")
                .append(companyId)
                .append(" and employee_id in (")
                .append(map.get("id"))
                .append(")");
            int num = DAOUtil.executeUpdate(buil.toString());
            if (num <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            StringBuilder queryBuf = new StringBuilder("select * from " + acountinfoble + "  where id in (" + map.get("id") + ")");
            List<JSONObject> json = DAOUtil.executeQuery4JSON(queryBuf.toString());
            for (int i = 0; i < json.size(); i++)
            {
                JSONObject object = json.get(i);
                RedisUtil.delRedis(object.getLong("id"));// 企信Redis
                serviceResult = imChatService.removePeopleFromGroup(map.get("token").toString(), object.getLong("id"));// 群人员维护
            }
            messagePushService.pushCatalogMessage(info.getCompanyId(), 1000);
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
     * 批量修改角色
     * 
     */
    @Override
    public ServiceResult<String> betchEditRole(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            // 先隐藏 后期需要放开
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
            StringBuilder queryBuf =
                new StringBuilder("update ").append(employeeTable).append(" set role_id = ").append(map.get("roleId")).append(" where id in (").append(map.get("id")).append(")");
            int count = DAOUtil.executeUpdate(queryBuf.toString());
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            JSONObject reqJSON = new JSONObject();
            reqJSON.put("companyId", info.getCompanyId());
            reqJSON.put("employeeId", info.getEmployeeId());
            CustomAsyncHandle customHandle = new CustomAsyncHandle(null, reqJSON);
            customHandle.modifyUserInfo();
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
     * 根据员工名称查询员工信息（导入用）
     * 
     */
    @Override
    public JSONObject getEmployeeByName(String employeeName, String companyId)
    {
        JSONObject result = null;
        try
        {
            // 拼装sql
            String customerTable = DAOUtil.getTableName("employee", companyId);
            StringBuilder querySql = new StringBuilder();
            querySql.append("select * from ");
            querySql.append(customerTable);
            querySql.append(" where employee_name = '");
            querySql.append(employeeName);
            querySql.append("' order by id desc");
            
            // 执行sql
            result = DAOUtil.executeQuery4FirstJSON(querySql.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return result;
        
    }
    
    /**
     * 根据iD查询员工信息
     * 
     */
    @Override
    public JSONObject queryEmployee(Long userId, Long companyId)
    {
        JSONObject result = null;
        try
        {
            String employeeTable = DAOUtil.getTableName("employee", companyId);
            String roleTable = DAOUtil.getTableName("role", companyId);
            String postTable = DAOUtil.getTableName("post", companyId);
            // 刘攀查询备份：e.*,p.*,r.*,p.name as duty_name
            StringBuilder queryBuf = new StringBuilder(
                "select e.id, e.employee_name, e.picture, e.leader, e.phone, e.mobile_phone, e.birth, e.region, e.email, e.status, e.account, e.is_enable, e.post_id, e.role_id, e.del_status, e.microblog_background, e.sex, e.sign, e.mood, e.personnel_create_by, e.datetime_create_time, p.id p_id, p.name duty_name, p.status p_status, r.id r_id, r.role_group_id, r.name r_name, r.status r_status, r.remark from ")
                    .append(employeeTable)
                    .append(" e left join ")
                    .append(postTable)
                    .append(" p on e.post_id = p.id left join ")
                    .append(roleTable)
                    .append(" r on e.role_id = r.id  where e.id=")
                    .append(userId);
            result = DAOUtil.executeQuery4FirstJSON(queryBuf.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return result;
    }
    
    /**
     * 员工列表
     * 
     */
    @Override
    public List<Map<String, Object>> selectEmployeeList(Map<String, Object> map)
    {
        // 先隐藏 后期需要放开
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        Long companyId = info.getCompanyId();
        String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
        String postTable = DAOUtil.getTableName("post", info.getCompanyId());
        String roleTable = DAOUtil.getTableName("role", info.getCompanyId());
        StringBuilder queryBuf = new StringBuilder();
        queryBuf.append(
            "SELECT   e.id,e.employee_name,e.account,e.phone,e.is_enable,p.name as post_name,r.name  as role_name,e.picture,t.id as sign_id,  '0' || ':' || e.ID AS value FROM ")
            .append(employeeTable)
            .append("  e  LEFT JOIN ")
            .append(postTable)
            .append(" P ON e.post_id = p.id LEFT JOIN ")
            .append(roleTable)
            .append("  r  on e.role_id  = r.id left join acountinfo t on t.employee_id =e.id WHERE   e.status=0 and e.del_status = 0  and t.company_id=")
            .append(companyId);
        if (StringUtils.isNotBlank(map.get("roleId").toString()))
        {
            queryBuf.append(" and r.id = ").append(map.get("roleId").toString());
        }
        return DAOUtil.executeQuery(queryBuf.toString());
    }
    
    /**
     * 修改员工信息
     * 
     */
    @Override
    public ServiceResult<String> editEmployeeDetail(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            JSONObject obj = new JSONObject();
            obj.put("bean", "employee");
            obj.put("data", map.get("data"));
            obj.put("id", info.getEmployeeId());
            String updateSql = JSONParser4SQL.getUpdateSql(obj, info.getCompanyId().toString());
            int number = DAOUtil.executeUpdate(updateSql); // 修改员工资料
            if (number <= 0)
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
     * 获取个人信息
     * 
     */
    @Override
    public JSONObject queryEmployeeInfo(Map<String, Object> map)
    {
        
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
        String postTable = DAOUtil.getTableName("post", info.getCompanyId());
        String departmentTable = DAOUtil.getTableName("department", info.getCompanyId());
        String centerTable = DAOUtil.getTableName("department_center", info.getCompanyId());
        String fabulousTable = DAOUtil.getTableName("employee_fabulous", info.getCompanyId());
        String acountinfoTable = DAOUtil.getTableName("acountinfo", "");
        JSONObject resultJsonObject = new JSONObject();
        Long employeeId;
        if (!"".equals(map.get("sign_id")) && map.get("sign_id") != null)
        {
            StringBuilder queryBuf = new StringBuilder("select * from ").append(acountinfoTable).append(" where  id = ").append(map.get("sign_id"));
            JSONObject jsonObject = DAOUtil.executeQuery4FirstJSON(queryBuf.toString());
            employeeId = jsonObject.getLong("employee_id");
        }
        else
        {
            employeeId = Long.valueOf(map.get("employee_id").toString());
        }
        
        // 基本信息
        StringBuilder buf = new StringBuilder();
        buf.append(
            "select e.id  as id,e.sign,e.email,e.mobile_phone,e.birth,e.region,e.mood,e.phone,e.employee_name,p.name post_name,e.role_id,e.picture,t.id as sign_id,e.sign,sex,microblog_background from ")
            .append(employeeTable)
            .append(" e left join ")
            .append(postTable)
            .append("  p on e.post_id = p.id left join acountinfo t on t.employee_id =e.id   where e.id = ")
            .append(employeeId)
            .append(" and company_id=")
            .append(info.getCompanyId());
        resultJsonObject.put("employeeInfo", DAOUtil.executeQuery4FirstJSON(buf.toString()));
        // 公司信息
        StringBuilder builder = new StringBuilder("select * from  company where id = ").append(info.getCompanyId());
        resultJsonObject.put("companyInfo", DAOUtil.executeQuery4FirstJSON(builder.toString()));
        // 部门信息
        StringBuilder buil = new StringBuilder("select d.*,c.is_main from ").append(centerTable)
            .append(" c left join ")
            .append(departmentTable)
            .append(" d on c.department_id=d.id where c.employee_id=")
            .append(employeeId)
            .append("  and c.status=")
            .append(Constant.CURRENCY_ZERO);
        resultJsonObject.put("departmentInfo", DAOUtil.executeQuery4JSON(buil.toString()));
        Map<String, Object> mapPhoto = new HashMap<>();
        mapPhoto.put("fromId", employeeId);
        // 最近相册
        List<Map<String, Object>> photo = imCircleAppService.findLastPhoto(map.get("token").toString(), mapPhoto);
        resultJsonObject.put("photo", photo);
        
        StringBuilder countBuil =
            new StringBuilder("select count(*) from ").append(fabulousTable).append(" where fabulous_id =  ").append(employeeId).append(" and status=").append(
                Constant.CURRENCY_ONE);
        resultJsonObject.put("fabulous_count", DAOUtil.executeCount(countBuil.toString())); // 被赞数量
        
        StringBuilder queryBuil = new StringBuilder("select count(*) from ").append(fabulousTable)
            .append(" where fabulous_id =  ")
            .append(employeeId)
            .append(" and employee_id = ")
            .append(info.getEmployeeId())
            .append(" and status=")
            .append(Constant.CURRENCY_ONE);
        int sum = DAOUtil.executeCount(queryBuil.toString());
        if (sum <= 0) // 自己是否点赞
        {
            resultJsonObject.put("fabulous_status", Constant.CURRENCY_ZERO);
        }
        else
        {
            
            resultJsonObject.put("fabulous_status", Constant.CURRENCY_ONE);
        }
        return resultJsonObject;
    }
    
    /**
     * 查询部门级数
     * 
     */
    @Override
    public List<JSONObject> queryDepartmentLevel(String token)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        String departmentTable = DAOUtil.getTableName("department", info.getCompanyId());
        StringBuilder builder = new StringBuilder("select max(parent_id) from ").append(departmentTable).append(" where status = ").append(Constant.CURRENCY_ZERO);
        Object object = DAOUtil.executeQuery4Object(builder.toString());
        List<JSONObject> json = new ArrayList<>();
        if (object == null)
        {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("value", "0");
            jsonObject.put("label", "第" + 1 + "级部门负责人");
            json.add(jsonObject);
            return json;
        }
        String departmentId = BusinessDAOUtil.getDepments(info.getCompanyId(), Long.parseLong(object.toString()), Constant.CURRENCY_ZERO);
        String[] idArray = departmentId.split(",");
        int num = idArray.length + 1;
        for (int i = 0; i < num; i++)
        {
            JSONObject jsonObject = new JSONObject();
            int sum = i + 1;
            jsonObject.put("value", String.valueOf(i));
            jsonObject.put("label", "第" + sum + "级部门负责人");
            json.add(jsonObject);
        }
        return json;
    }
    
    /**
     * 获取企业所有者
     * 
     */
    @Override
    public JSONObject queryCompanyOwner(String token)
    {
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        String employeeTable = DAOUtil.getTableName("employee", companyId);
        String roleTable = DAOUtil.getTableName("role", companyId);
        StringBuilder sql = new StringBuilder();
        sql.append(" select e. id, e.role_id, e.employee_name, e.account, e.phone, e.is_enable, r. name as role_name from ")
            .append(employeeTable)
            .append(" e ")
            .append(" left join ")
            .append(roleTable)
            .append(" r on e.role_id = r. id ")
            .append(" where e.status = 0 and e.del_status = 0 and r.id=1");
        return DAOUtil.executeQuery4FirstJSON(sql.toString());
        
    }
    
    /**
     * 是否点赞取消
     */
    @Override
    public ServiceResult<String> whetherFabulous(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String fabulousTable = DAOUtil.getTableName("employee_fabulous", info.getCompanyId());
            StringBuilder builder = new StringBuilder();
            if (Integer.parseInt(map.get("status").toString()) == 1)
            {
                builder.append("insert into ")
                    .append(fabulousTable)
                    .append(" (fabulous_id,employee_id) values(")
                    .append(map.get("id"))
                    .append(",")
                    .append(info.getEmployeeId())
                    .append(")");
            }
            else if (Integer.parseInt(map.get("status").toString()) == 0)
            {
                builder.append("update  ")
                    .append(fabulousTable)
                    .append(" set status='")
                    .append(Constant.CURRENCY_ZERO)
                    .append("' where fabulous_id=")
                    .append(map.get("id"))
                    .append(" and employee_id=")
                    .append(info.getEmployeeId());
            }
            int count = DAOUtil.executeUpdate(builder.toString());
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
        }
        catch (NumberFormatException e)
        {
            log.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 获取主部门架构
     */
    @Override
    public JSONObject queryDepartmentFramework(Map<String, Object> map)
    {
        JSONObject jsonObject = new JSONObject();
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
        String postTable = DAOUtil.getTableName("post", info.getCompanyId());
        String departmentTable = DAOUtil.getTableName("department", info.getCompanyId());
        String centerTable = DAOUtil.getTableName("department_center", info.getCompanyId());
        String sb = BusinessDAOUtil.getDepments(info.getCompanyId(), Long.parseLong(map.get("id").toString()), 0);
        StringBuilder sql =
            new StringBuilder("select * from  ").append(departmentTable).append(" where  id in (").append(sb).append(") and status = ").append(Constant.CURRENCY_ZERO);
        jsonObject.put("departmentLevel", DAOUtil.executeQuery4JSON(sql.toString()));
        log.debug("查询当前主部门层级==============");
        StringBuilder builder = new StringBuilder("SELECT  array_to_string(array_agg(s.employee_id),',')  from (select *  from ").append(centerTable)
            .append(" where department_id  = ")
            .append(Long.parseLong(map.get("id").toString()))
            .append(" and status = 0) s ");
        Object object = DAOUtil.executeQuery4Object(builder.toString());
        log.debug("查询当前主部门人员==============");
        StringBuilder buf = new StringBuilder();
        buf.append("select e.id  as id,e.sign,e.email,e.mood,e.phone,e.employee_name,p.name post_name,e.role_id,e.picture,t.id as sign_id,e.sign,sex,microblog_background from ")
            .append(employeeTable)
            .append(" e left join ")
            .append(postTable)
            .append("  p on e.post_id = p.id left join acountinfo t on t.employee_id =e.id   where e.id in (")
            .append(object.toString())
            .append(") and company_id=")
            .append(info.getCompanyId());
        jsonObject.put("employee", DAOUtil.executeQuery4JSON(buf.toString()));
        log.debug("查询当前主部门人员详情==============");
        return jsonObject;
    }
    
    /**
     * 获取部门人员详细
     */
    @Override
    public JSONObject queryDepartmentEmployee(Map<String, Object> map)
    {
        JSONObject jsonObject = new JSONObject();
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
        String postTable = DAOUtil.getTableName("post", info.getCompanyId());
        String centerTable = DAOUtil.getTableName("department_center", info.getCompanyId());
        StringBuilder buf = new StringBuilder("SELECT  array_to_string(array_agg(s.employee_id),',')  from (select *  from ").append(centerTable)
            .append(" where department_id  = ")
            .append(Long.parseLong(map.get("id").toString()))
            .append(" and status = 0) s ");
        Object object = DAOUtil.executeQuery4Object(buf.toString());
        log.debug("查询当前主部门人员==============" + object);
        StringBuilder builder = new StringBuilder();
        builder
            .append("select e.id  as id,e.sign,e.email,e.mood,e.phone,e.employee_name,p.name post_name,e.role_id,e.picture,t.id as sign_id,e.sign,sex,microblog_background from ")
            .append(employeeTable)
            .append(" e left join ")
            .append(postTable)
            .append("  p on e.post_id = p.id left join acountinfo t on t.employee_id =e.id   where e.id in (")
            .append(object.toString())
            .append(") and company_id=")
            .append(info.getCompanyId());
        jsonObject.put("employee", DAOUtil.executeQuery4JSON(builder.toString()));
        log.debug("查询当前主部门人员详情==============");
        return jsonObject;
    }
    
    /**
     * 获取员工所有的部门编码
     */
    @Override
    public String getdepartmentIds(String employeeId, String companyId)
    {
        StringBuilder sqlSB = new StringBuilder();
        String departmentTable = DAOUtil.getTableName("department_center", companyId);
        sqlSB.append("select string_agg(department_id,',') from ").append(departmentTable).append(" where employee_id=").append(employeeId).append(" and status=").append(
            Constant.CURRENCY_ZERO);
        return DAOUtil.executeQuery4Object(sqlSB.toString(), String.class);
    }
    
    /**
     * 获取可选范围人员列表 需要整理
     */
    @Override
    public List<JSONObject> queryRangeEmployeeList(Map<String, String> map)
    {
        JSONArray arrayList = JSONArray.parseArray(map.get("chooseRange"));
        InfoVo info = TokenMgr.obtainInfo(map.get("token"));
        StringBuilder buf = new StringBuilder();
        String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
        String departmentTable = DAOUtil.getTableName("department_center", info.getCompanyId());
        String postTable = DAOUtil.getTableName("post", info.getCompanyId());
        for (int i = 0; i < arrayList.size(); i++)
        {
            JSONObject jsonObject = (JSONObject)arrayList.get(i);
            Integer tmpType = jsonObject.getInteger("type");
            switch (tmpType)
            { // 0部门 1成员 2角色 4全公司
                case Constant.CURRENCY_ZERO:
                    
                    StringBuilder builder = new StringBuilder();
                    String sb = BusinessDAOUtil.getDepments(info.getCompanyId(), jsonObject.getLong("id"), 1);
                    if (!"".equals(sb) && null != sb)
                    {
                        builder.append(sb).append(",").append(jsonObject.getLong("id").toString());
                    }
                    else
                    {
                        builder.append(jsonObject.getLong("id").toString());
                    }
                    StringBuilder queryBuilder = new StringBuilder();
                    queryBuilder.append("select array_to_string(array_agg(distinct(employee_id)),',') from ")
                        .append(departmentTable)
                        .append(" where department_id in (")
                        .append(builder.toString())
                        .append(") and status = ")
                        .append(Constant.CURRENCY_ZERO);
                    Object json = DAOUtil.executeQuery4Object(queryBuilder.toString());
                    if (null != json)
                    {
                        if (buf.length() <= 0)
                        {
                            buf.append(json.toString());
                        }
                        else
                        {
                            buf.append(",").append(json.toString());
                        }
                    }
                    break;
                case Constant.CURRENCY_ONE:
                    
                    StringBuilder selectBuilder = new StringBuilder();
                    selectBuilder.append("select id from ")
                        .append(employeeTable)
                        .append(" where id = ")
                        .append(jsonObject.getLong("id"))
                        .append(" and  del_status = ")
                        .append(Constant.CURRENCY_ZERO)
                        .append(" and  status = ")
                        .append(Constant.CURRENCY_ZERO);
                    Object object = DAOUtil.executeQuery4Object(selectBuilder.toString());
                    if (null != object)
                    {
                        if (buf.length() <= 0)
                        {
                            buf.append(object.toString());
                        }
                        else
                        {
                            buf.append(",").append(object.toString());
                        }
                    }
                    break;
                case Constant.CURRENCY_TWO:
                    StringBuilder roleBuilder = new StringBuilder();
                    roleBuilder.append(" select array_to_string(array_agg(distinct(id)),',')  from ")
                        .append(employeeTable)
                        .append(" where role_id = ")
                        .append(jsonObject.getLong("id"))
                        .append(" and  del_status = ")
                        .append(Constant.CURRENCY_ZERO)
                        .append(" and  status = ")
                        .append(Constant.CURRENCY_ZERO);
                    Object roleObject = DAOUtil.executeQuery4Object(roleBuilder.toString());
                    if (null != roleObject)
                    {
                        if (buf.length() <= 0)
                        {
                            buf.append(roleObject.toString());
                        }
                        else
                        {
                            buf.append(",").append(roleObject.toString());
                        }
                    }
                    break;
                case 4:
                    StringBuilder companyBuilder = new StringBuilder();
                    companyBuilder.append(" select array_to_string(array_agg(distinct(id)),',')  from ")
                        .append(employeeTable)
                        .append("  where  del_status = ")
                        .append(Constant.CURRENCY_ZERO)
                        .append(" and  status = ")
                        .append(Constant.CURRENCY_ZERO);
                    Object companyObject = DAOUtil.executeQuery4Object(companyBuilder.toString());
                    if (null != companyObject)
                    {
                        if (buf.length() <= 0)
                        {
                            buf.append(companyObject.toString());
                        }
                        else
                        {
                            buf.append(",").append(companyObject.toString());
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        
        StringBuilder listBuilder = new StringBuilder();
        listBuilder.append(" select DISTINCT e.id,e.employee_name,e.picture,p.name post_name  from ")
            .append(employeeTable)
            .append(" e left join  ")
            .append(postTable)
            .append("  p  on e.post_id = p.id where e.id in (")
            .append(buf.toString())
            .append(")")
            .append(" and  e.del_status = ")
            .append(Constant.CURRENCY_ZERO)
            .append(" and  e.status = ")
            .append(Constant.CURRENCY_ZERO);
        List<JSONObject> list = DAOUtil.executeQuery4JSON(listBuilder.toString());
        return list;
    }
    
    /**
     * 获取可选范围部门列表 需要整理
     */
    @Override
    public List<JSONObject> queryRangeDepartmentList(Map<String, String> map)
    {
        JSONArray arrayList = JSONArray.parseArray(map.get("chooseRange"));
        InfoVo info = TokenMgr.obtainInfo(map.get("token"));
        StringBuilder builder = new StringBuilder();
        String departmentTable = DAOUtil.getTableName("department", info.getCompanyId());
        for (int i = 0; i < arrayList.size(); i++)
        {
            JSONObject jsonObject = (JSONObject)arrayList.get(i);
            Integer tmpType = jsonObject.getInteger("type");
            // 0部门 4全公司
            if (tmpType == Constant.CURRENCY_ZERO)
            {
                String sb = BusinessDAOUtil.getDepments(info.getCompanyId(), jsonObject.getLong("id"), 1);
                if (!"".equals(sb) && null != sb)
                {
                    builder.append(sb).append(",").append(jsonObject.getLong("id").toString());
                }
                else
                {
                    builder.append(jsonObject.getLong("id").toString());
                }               
            }else {
                StringBuilder companyBuilder = new StringBuilder();
                companyBuilder.append(" select array_to_string(array_agg(distinct(id)),',')  from ");
                companyBuilder.append(departmentTable).append(" where  status = ").append(Constant.CURRENCY_ZERO);
                Object companyObject = DAOUtil.executeQuery4Object(companyBuilder.toString());
                if (null != companyObject)
                {
                    if (builder.length() <= 0)
                    {
                        builder.append(companyObject.toString());
                    }
                    else
                    {
                        builder.append(",").append(companyObject.toString());
                    }
                }
            }
        }
        
        StringBuilder listDepartmentBuilder = new StringBuilder();
        listDepartmentBuilder.append(" select DISTINCT id,department_name,parent_id  from ");
        listDepartmentBuilder.append(departmentTable).append(" where id in (");
        listDepartmentBuilder.append(builder.toString()).append(")");
        listDepartmentBuilder.append(" and status = ").append(Constant.CURRENCY_ZERO);
        listDepartmentBuilder.append(" order by id DESC ");
        
        return DAOUtil.executeQuery4JSON(listDepartmentBuilder.toString());
    }
    
    /**
     * 根据员工id 获取企信 signId
     */
    @Override
    public String queryEmployeeSignId(String companyId, String employeeId)
    {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(" SELECT array_to_string(array_agg(distinct(id)),',') from  acountinfo where  ")
            .append("employee_id  in (")
            .append(employeeId)
            .append(") and company_id = ")
            .append(companyId)
            .append(" and  status = ")
            .append(Constant.CURRENCY_ZERO);
        Object object = DAOUtil.executeQuery4Object(queryBuilder.toString());
        return object.toString();
    }
    
    /**
     * 获取员工所属部门及子级部门所有员工ID
     */
    @Override
    public List<JSONObject> queryDepartmentAuthEmployee(String companyId, String employeeId)
    {
        
        StringBuilder queryBuilder = new StringBuilder();
        String centerTable = DAOUtil.getTableName("department_center", companyId);
        queryBuilder.append(" select * from ");
        queryBuilder.append(centerTable);
        queryBuilder.append(" where status = ");
        queryBuilder.append(Constant.CURRENCY_ZERO);
        queryBuilder.append(" and  employee_id = ");
        queryBuilder.append(employeeId);
        // 获取所有所属部门
        List<JSONObject> jsonList = DAOUtil.executeQuery4JSON(queryBuilder.toString());
        StringBuilder builder = new StringBuilder();
        // 拼接所有本部门及子级部门ID
        for (int i = 0; i < jsonList.size(); i++)
        {
            String str = BusinessDAOUtil.getDepments(Long.valueOf(companyId), jsonList.get(i).getLong("department_id"), 1, "department");
            if (str.isEmpty())
            {
                builder.append(jsonList.get(i).getString("department_id"));
            }
            else
            {
                if (i > 1)
                {
                    builder.append(",".concat(str).concat(",").concat(jsonList.get(i).getString("department_id")));
                }
                else
                {
                    builder.append(str.concat(",").concat(jsonList.get(i).getString("department_id")));
                }
            }
        }
        StringBuilder queryListBuilder = new StringBuilder();
        queryListBuilder.append("select DISTINCT employee_id from ");
        queryListBuilder.append(centerTable);
        queryListBuilder.append(" where department_id in (");
        queryListBuilder.append(builder.toString());
        queryListBuilder.append(") and status = ");
        queryListBuilder.append(Constant.CURRENCY_ZERO);
        // 获取部门下所有员工ID
        List<JSONObject> list = DAOUtil.executeQuery4JSON(queryListBuilder.toString());
        if (list == null)
        {
            return new ArrayList<>();
        }
        return list;
    }
    
}
