package com.teamface.custom.controller.employee;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.DataTypes;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.JsonResUtil;
import com.teamface.custom.service.employee.EmployeeAppService;

/**
 * @Description:员工模块控制器
 * @author: liupan
 * @date: 2017年9月19日 上午11:48:16
 * @version: 1.0
 */

@Controller
@RequestMapping(value = "/employee")
public class EmployeeController
{
    private static final Logger LOG = LogManager.getLogger(EmployeeController.class);
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    @Autowired
    private EmployeeAppService employeeAppService;
    
    /**
     * 添加员工信息
     * 
     * @param reqJsonStr
     * @return
     * @Description:
     */
    @RequestMapping(value = "/savaEmployee", method = RequestMethod.POST)
    public @ResponseBody JSONObject savaEmployee(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String data = layoutJson.get("data").toString();
            String departmentId = layoutJson.get("department_id").toString();
            String mainId = layoutJson.get("main_id").toString();
            if (StringUtils.isBlank(data) || StringUtils.isBlank(departmentId) || StringUtils.isBlank(mainId))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("data", data);
            map.put("token", token);
            map.put("mainId", mainId);
            map.put("departmentId", departmentId);
            serviceResult = employeeAppService.savaEmployee(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 编辑员工信息
     * 
     * @param reqJsonStr
     * @return
     * @Description:
     */
    @RequestMapping(value = "editEmployee", method = RequestMethod.POST)
    public @ResponseBody JSONObject editEmployee(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String data = layoutJson.get("data").toString();
            String id = layoutJson.get("id").toString();
            String departmentId = layoutJson.get("department_id").toString();
            String mainId = layoutJson.get("main_id").toString();
            if (StringUtils.isBlank(id) || StringUtils.isBlank(data) || StringUtils.isBlank(departmentId))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("data", data);
            map.put("id", id);
            map.put("mainId", mainId);
            map.put("token", token);
            map.put("departmentId", departmentId);
            serviceResult = employeeAppService.editEmployee(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 
     * @param req
     * @return
     * @Description:获取员工详情数据
     */
    @RequestMapping(value = "/getEmployeeDetail", method = RequestMethod.GET)
    public @ResponseBody JSONObject getEmployeeDetail(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            String id = request.getParameter("id");
            if (StringUtils.isBlank(id))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("token", token);
            Map<String, Object> resultMap = employeeAppService.getEmployeeDetail(map);
            return JsonResUtil.getSuccessJsonObject(resultMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        
    }
    
    /**
     * 编辑职务信息
     * 
     * @param reqJsonStr
     * @return
     * @Description:
     */
    @RequestMapping(value = "editLeader", method = RequestMethod.POST)
    public @ResponseBody JSONObject editLeader(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String employeeId = layoutJson.get("employee_id").toString();
            String departmentId = layoutJson.get("department_id").toString();
            if (StringUtils.isBlank(employeeId) || StringUtils.isBlank(departmentId))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("departmentId", departmentId);
            map.put("employeeId", employeeId);
            map.put("token", token);
            serviceResult = employeeAppService.editLeader(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 员工列表
     * 
     * @param request
     * @return
     * @Description:
     */
    @RequestMapping(value = "queryEmployee", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryEmployee(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            String roleId = request.getParameter("roleId");
            String departmentId = request.getParameter("departmentId");
            String status = request.getParameter("status");
            String name = request.getParameter("name");
            String pageSize = request.getParameter("pageSize");
            String pageNum = request.getParameter("pageNum");
            String employeeName = request.getParameter("employeeName");
            if (StringUtils.isBlank(pageSize) || StringUtils.isBlank(pageNum))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("roleId", roleId);
            map.put("status", status);
            map.put("departmentId", departmentId);
            map.put("name", name);
            map.put("token", token);
            map.put("pageSize", pageSize);
            map.put("pageNum", pageNum);
            map.put("employeeName", employeeName);
            JSONObject listMap = employeeAppService.queryEmployee(map);
            return JsonResUtil.getSuccessJsonObject(listMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    /**
     * 已停用员工列表
     * 
     * @param request
     * @return
     * @Description:
     */
    @RequestMapping(value = "queryEmployeeList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryEmployeeList(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            String pageSize = request.getParameter("pageSize");
            String pageNum = request.getParameter("pageNum");
            String roleId = request.getParameter("roleId");
            if (StringUtils.isBlank(pageSize) || StringUtils.isBlank(pageNum))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("roleId", roleId);
            map.put("token", token);
            map.put("pageSize", pageSize);
            map.put("pageNum", pageNum);
            JSONObject listMap = employeeAppService.queryEmployeeList(map);
            return JsonResUtil.getSuccessJsonObject(listMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    /**
     * 添加职务信息
     * 
     * @param reqJsonStr
     * @return
     * @Description:
     */
    @RequestMapping(value = "/savaPost", method = RequestMethod.POST)
    public @ResponseBody JSONObject savaPost(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String name = layoutJson.get("name").toString();
            if (StringUtils.isBlank(name))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("name", name);
            map.put("token", token);
            serviceResult = employeeAppService.savaPost(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 编辑职务信息
     * 
     * @param reqJsonStr
     * @return
     * @Description:
     */
    @RequestMapping(value = "editPost", method = RequestMethod.POST)
    public @ResponseBody JSONObject editPost(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String name = layoutJson.get("name").toString();
            String id = layoutJson.get("id").toString();
            if (StringUtils.isBlank(id) || StringUtils.isBlank(name))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("name", name);
            map.put("id", id);
            map.put("token", token);
            serviceResult = employeeAppService.editPost(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 职务列表
     * 
     * @param request
     * @return
     * @Description:
     */
    @RequestMapping(value = "queryPost", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryPost(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            String name = request.getParameter("name");
            Map<String, Object> map = new HashMap<>();
            map.put("name", name);
            map.put("token", token);
            List<Map<String, Object>> listMap = employeeAppService.queryPost(map);
            return JsonResUtil.getSuccessJsonObject(listMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    /**
     * 删除职务信息
     * 
     * @param reqJsonStr
     * @return
     * @Description:
     */
    @RequestMapping(value = "delPost", method = RequestMethod.POST)
    public @ResponseBody JSONObject delPost(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String id = layoutJson.get("id").toString();
            if (StringUtils.isBlank(id))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("token", token);
            serviceResult = employeeAppService.delPost(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 添加部门信息
     * 
     * @param reqJsonStr
     * @return
     * @Description:
     */
    @RequestMapping(value = "/savaDepartment", method = RequestMethod.POST)
    public @ResponseBody JSONObject savaDepartment(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        JSONObject  jsonObject = new JSONObject();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String parentId = layoutJson.get("parent_id").toString();
            String departmentName = layoutJson.get("department_name").toString();
            
            if (StringUtils.isBlank(departmentName) || StringUtils.isBlank(parentId))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("departmentName", departmentName);
            map.put("parentId", parentId);
            map.put("token", token);
            jsonObject = employeeAppService.savaDepartment(map);
            if(null != jsonObject.get("departmentId")) {
                return JsonResUtil.getSuccessJsonObject(jsonObject);
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 编辑部门信息
     * 
     * @param reqJsonStr
     * @return
     * @Description:
     */
    @RequestMapping(value = "editDepartment", method = RequestMethod.POST)
    public @ResponseBody JSONObject editDepartment(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String departmentName = layoutJson.get("department_name").toString();
            String id = layoutJson.get("id").toString();
            if (StringUtils.isBlank(id) || StringUtils.isBlank(departmentName))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("departmentName", departmentName);
            map.put("id", id);
            map.put("token", token);
            serviceResult = employeeAppService.editDepartment(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 删除部门信息
     * 
     * @param reqJsonStr
     * @return
     * @Description:
     */
    @RequestMapping(value = "delDepartment", method = RequestMethod.POST)
    public @ResponseBody JSONObject delDepartment(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String id = layoutJson.get("id").toString();
            if (StringUtils.isBlank(id))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("token", token);
            serviceResult = employeeAppService.delDepartment(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 查询公司组织架构
     * 
     * @param map
     * @return
     * @Description:
     */
    @RequestMapping(value = "/findCompanyDepartment", method = RequestMethod.GET)
    public @ResponseBody JSONObject findCompanyDepartment(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            JSONArray companyFramework = employeeAppService.findCompanyDepartment(token);
            return JsonResUtil.getSuccessJsonObject(companyFramework);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    /**
     * 编辑员工信息
     * 
     * @param reqJsonStr
     * @return
     * @Description:
     */
    @RequestMapping(value = "betchEditEmployee", method = RequestMethod.POST)
    public @ResponseBody JSONObject betchEditEmployee(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String id = layoutJson.get("id").toString();
            String status = layoutJson.get("status").toString();
            if (StringUtils.isBlank(id) || StringUtils.isBlank(status))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("token", token);
            map.put("status", status);
            serviceResult = employeeAppService.betchEditEmployee(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 编辑员工信息
     * 
     * @param reqJsonStr
     * @return
     * @Description:
     */
    @RequestMapping(value = "betchEditRole", method = RequestMethod.POST)
    public @ResponseBody JSONObject betchEditRole(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String id = layoutJson.get("id").toString();
            String roleId = layoutJson.get("role_id").toString();
            if (StringUtils.isBlank(id) || StringUtils.isBlank(roleId))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("roleId", roleId);
            map.put("token", token);
            serviceResult = employeeAppService.betchEditRole(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 编辑员工部门信息
     * 
     * @param reqJsonStr
     * @return
     * @Description:
     */
    @RequestMapping(value = "betchDelEmployee", method = RequestMethod.POST)
    public @ResponseBody JSONObject betchDelEmployee(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String id = layoutJson.get("id").toString();
            if (StringUtils.isBlank(id))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("token", token);
            employeeAppService.betchDelEmployee(map);
            serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
        
    }
    
    /**
     * 批量调整部门
     * 
     * @param reqJsonStr
     * @return
     * @Description:
     */
    @RequestMapping(value = "betchEditDepartment", method = RequestMethod.POST)
    public @ResponseBody JSONObject betchEditDepartment(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String departmentId = layoutJson.get("department_id").toString();
            String employeeId = layoutJson.get("employee_id").toString();
            String currentId = layoutJson.get("current_id").toString();
            if (StringUtils.isBlank(departmentId) || StringUtils.isBlank(employeeId) || StringUtils.isBlank(currentId))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("employeeId", employeeId);
            map.put("departmentId", departmentId);
            map.put("currentId", currentId);
            map.put("token", token);
            serviceResult = employeeAppService.betchEditDepartment(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
        
    }
    
    /**
     * 查询公司组织架构
     * 
     * @param map
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryEmployeeDepartment", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryEmployeeDepartment(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            String id = request.getParameter("parent_id");
            if (StringUtils.isBlank(id))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            JSONObject jsonObject = employeeAppService.queryEmployeeDepartment(id, token);
            return JsonResUtil.getSuccessJsonObject(jsonObject);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    /**
     * 激活提醒
     * 
     * @param reqJsonStr
     * @return
     * @Description:
     */
    @RequestMapping(value = "/activateRemind", method = RequestMethod.POST)
    public @ResponseBody JSONObject activateRemind(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        JSONObject result;
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String id = layoutJson.get("id").toString();
            if (StringUtils.isBlank(id))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("token", token);
            result = employeeAppService.activateRemind(map);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        if (null == result)
        {
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject(result);
        
    }
    
    /**
     * 员工列表
     * 
     * @param request
     * @return
     * @Description:
     */
    @RequestMapping(value = "selectEmployeeList", method = RequestMethod.GET)
    public @ResponseBody JSONObject selectEmployeeList(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            String roleId = request.getParameter("roleId");
            Map<String, Object> map = new HashMap<>();
            map.put("roleId", roleId);
            map.put("token", token);
            List<Map<String, Object>> listMap = employeeAppService.selectEmployeeList(map);
            return JsonResUtil.getSuccessJsonObject(listMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    /**
     * 修改员工个人中心信息
     * 
     * @param reqJsonStr
     * @return
     * @Description:
     */
    @RequestMapping(value = "/editEmployeeDetail", method = RequestMethod.POST)
    public @ResponseBody JSONObject editEmployeeDetail(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult;
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String data = layoutJson.get("data").toString();
            Map<String, Object> map = new HashMap<>();
            map.put("data", data);
            map.put("token", token);
            serviceResult = employeeAppService.editEmployeeDetail(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 员工列表
     * 
     * @param request
     * @return
     * @Description:
     */
    @RequestMapping(value = "queryEmployeeInfo", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryEmployeeInfo(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            Object signId = request.getParameter("sign_id");
            Object employeeId = request.getParameter("employee_id");
            Map<String, Object> map = new HashMap<>();
            map.put("sign_id", signId);
            map.put("employee_id", employeeId);
            map.put("token", token);
            JSONObject jsonObject = employeeAppService.queryEmployeeInfo(map);
            return JsonResUtil.getSuccessJsonObject(jsonObject);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    /**
     * 个人中心点赞取消
     * 
     * @param req
     * @return
     * @Description:
     */
    @RequestMapping(value = "/whetherFabulous", method = RequestMethod.POST)
    public @ResponseBody JSONObject whetherFabulous(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            Map<String, Object> map = new HashMap<>();
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String id = layoutJson.get("id").toString();
            String status = layoutJson.get("status").toString();
            if (StringUtils.isBlank(id))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            map.put("token", token);
            map.put("id", id);
            map.put("status", status);
            serviceResult = employeeAppService.whetherFabulous(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 查询部门级数
     * 
     * @param request
     * @param token
     * @return
     */
    @RequestMapping(value = "/queryDepartmentLevel", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryDepartmentLevel(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        List<JSONObject> resultMap = null;
        try
        {
            resultMap = employeeAppService.queryDepartmentLevel(token);
        }
        catch (Exception e)
        {
            LOG.error(e);
            e.printStackTrace();
        }
        return JsonResUtil.getSuccessJsonObject(resultMap);
    }
    
    /**
     * 查询部门级数
     * 
     * @param request
     * @param token
     * @return
     */
    @RequestMapping(value = "/queryDepartmentFramework", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryDepartmentFramework(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        JSONObject resultMap = null;
        try
        {
            Object id = request.getParameter("id");
            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            map.put("id", id);
            resultMap = employeeAppService.queryDepartmentFramework(map);
        }
        catch (Exception e)
        {
            LOG.error(e);
            e.printStackTrace();
        }
        return JsonResUtil.getSuccessJsonObject(resultMap);
    }
    
    @RequestMapping(value = "/queryDepartmentEmployee", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryDepartmentEmployee(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        JSONObject resultMap = null;
        try
        {
            Object id = request.getParameter("id");
            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            map.put("id", id);
            resultMap = employeeAppService.queryDepartmentEmployee(map);
        }
        catch (Exception e)
        {
            LOG.error(e);
            e.printStackTrace();
        }
        return JsonResUtil.getSuccessJsonObject(resultMap);
    }
    
    /**
     * 获取可选范围人员列表
     * 
     * @param request
     * @return
     * @Description:
     */
    @RequestMapping(value = "queryRangeEmployeeList", method = RequestMethod.POST)
    public @ResponseBody JSONObject queryRangeEmployeeList(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            JSONObject object = JSONObject.parseObject(reqJsonStr);
            String chooseRange = object.getString("chooseRange");
            if (StringUtils.isBlank(chooseRange))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, String> map = new HashMap<>();
            map.put("chooseRange", chooseRange);
            map.put("token", token);
            List<JSONObject> listMap = employeeAppService.queryRangeEmployeeList(map);
            return JsonResUtil.getSuccessJsonObject(listMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    /**
     * 获取可选范围部门列表
     * 
     * @param request
     * @return
     * @Description:
     */
    @RequestMapping(value = "queryRangeDepartmentList", method = RequestMethod.POST)
    public @ResponseBody JSONObject queryRangeDepartmentList(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            JSONObject object = JSONObject.parseObject(reqJsonStr);
            String chooseRange = object.getString("chooseRange");
            if (StringUtils.isBlank(chooseRange))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, String> map = new HashMap<>();
            map.put("chooseRange", chooseRange);
            map.put("token", token);
            List<JSONObject> listMap = employeeAppService.queryRangeDepartmentList(map);
            return JsonResUtil.getSuccessJsonObject(listMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
    }
    
}
