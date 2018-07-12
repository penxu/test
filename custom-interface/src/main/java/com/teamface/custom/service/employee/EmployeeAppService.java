package com.teamface.custom.service.employee;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * @Description:
 * @author: liupan
 * @date: 2017年9月19日 上午11:55:29
 * @version: 1.0
 */

public interface EmployeeAppService
{
    /**
     * 增加员工资料
     * 
     * @param map
     * @Description:
     */
    public ServiceResult<String> savaEmployee(Map<String, Object> map);
    
    /**
     * 编辑员工信息
     * 
     * @param map
     * @Description:
     */
    public ServiceResult<String> editEmployee(Map<String, Object> map);
    
    /**
     * 查询 未启用的员工
     * 
     * @param map
     * @return
     * @Description:
     */
    public JSONObject queryEmployee(Map<String, Object> map);
    
    /**
     * 添加职位
     * 
     * @param map
     * @Description:
     */
    public ServiceResult<String> savaPost(Map<String, Object> map);
    
    /**
     * 修改职位
     * 
     * @param map
     * @Description:
     */
    public ServiceResult<String> editPost(Map<String, Object> map);
    
    /**
     * 职位列表
     * 
     * @param map
     * @return
     * @Description:
     */
    public List<Map<String, Object>> queryPost(Map<String, Object> map);
    
    /**
     * 编辑部门
     * 
     * @param map
     * @Description:
     */
    public ServiceResult<String> editDepartment(Map<String, Object> map);
    
    /**
     * 删除部门
     * 
     * @param map
     * @Description:
     */
    public ServiceResult<String> delDepartment(Map<String, Object> map);
    
    /**
     * 查询公司部门架构
     * 
     * @param map
     * @return
     * @Description:
     */
    public JSONArray findCompanyDepartment(String token);
    
    /**
     * 添加部门信息
     * 
     * @param map
     * @Description:
     */
    public JSONObject savaDepartment(Map<String, Object> map);
    
    /**
     * 批量修改员工
     * 
     * @param map
     * @Description:
     */
    public ServiceResult<String> betchEditEmployee(Map<String, Object> map);
    
    /**
     * 批量调整部门
     * 
     * @param map
     * @Description:
     */
    public ServiceResult<String> betchEditDepartment(Map<String, Object> map);
    
    /**
     * 激活提醒
     * 
     * @param map
     * @Description:
     */
    public JSONObject activateRemind(Map<String, Object> map);
    
    /**
     * 删除职务
     * 
     * @param map
     * @return
     * @Description:
     */
    public ServiceResult<String> delPost(Map<String, Object> map);
    
    /**
     * 编辑负责人
     * 
     * @param map
     * @return
     * @Description:
     */
    public ServiceResult<String> editLeader(Map<String, Object> map);
    
    /**
     * 已停用员工列表
     * 
     * @param map
     * @return
     * @Description:
     */
    public JSONObject queryEmployeeList(Map<String, Object> map);
    
    /**
     * 获取员工
     * 
     * @param map
     * @return
     * @Description:
     */
    public Map<String, Object> getEmployeeDetail(Map<String, Object> map);
    
    /**
     * 获取员工ID集合和公司ID获取信息
     * 
     * @param str
     * @param companyId
     * @return
     * @Description:
     */
    public List<JSONObject> queryEmployeeDetail(String str, Long companyId);
    
    /**
     * 根据ID集合获取员工职务信息
     * 
     * @param str
     * @param companyId
     * @return
     * @Description:
     */
    public List<JSONObject> queryEmployeeSomeFields(String str, Long companyId);
    
    /**
     * 批量删除员工
     * 
     * @param map
     * @Description:
     */
    public ServiceResult<String> betchDelEmployee(Map<String, Object> map);
    
    /**
     * 批量修改角色
     * 
     * @param map
     * @return
     * @Description:
     */
    public ServiceResult<String> betchEditRole(Map<String, Object> map);
    
    /**
     * 根据员工名称查询员工信息（导入用）
     * 
     * @param employeeName
     * @param companyId
     * @return
     * @Description:
     */
    public JSONObject getEmployeeByName(String employeeName, String companyId);
    
    /**
     * 根据iD查询员工信息
     * 
     * @return
     * @Description:
     */
    JSONObject queryEmployee(Long userId, Long companyId);
    
    /**
     * 员工列表
     * 
     * @return
     * @Description:
     */
    public List<Map<String, Object>> selectEmployeeList(Map<String, Object> map);
    
    /**
     * 修改员工信息
     * 
     * @param map
     * @return
     * @Description:
     */
    public ServiceResult<String> editEmployeeDetail(Map<String, Object> map);
    
    /**
     * 获取个人信息
     * 
     * @param map
     * @return
     * @Description:
     */
    public JSONObject queryEmployeeInfo(Map<String, Object> map);
    
    /**
     * 查询父级部门
     * 
     * @param id
     * @param token
     * @return
     * @Description:
     */
    public JSONObject queryEmployeeDepartment(String id, String token);
    
    /**
     * 查询部门级数
     * 
     * @param token
     * @return
     */
    public List<JSONObject> queryDepartmentLevel(String token);
    
    /**
     * 获取企业所有者
     * 
     * @param token
     * @return
     * @Description:
     */
    public JSONObject queryCompanyOwner(String token);
    
    /**
     * 是否点赞取消
     * 
     * @param map
     * @return
     * @Description:
     */
    public ServiceResult<String> whetherFabulous(Map<String, Object> map);
    
    /**
     * 获取主部门架构
     * 
     * @param token
     * @return
     * @Description:
     */
    public JSONObject queryDepartmentFramework(Map<String, Object> map);
    
    /**
     * 获取部门人员详细
     * 
     * @param map
     * @return
     * @Description:
     */
    public JSONObject queryDepartmentEmployee(Map<String, Object> map);
    
    /**
     * 获取员工所有的部门编码
     * 
     * @param employeeId
     * @param companyId
     * @return
     * @Description:
     */
    public String getdepartmentIds(String employeeId, String companyId);
    
    /**
     * 获取可选范围人员列表
     * 
     * @param map
     * @return
     * @Description:
     */
    public List<JSONObject> queryRangeEmployeeList(Map<String, String> map);
    
    /**
     * 获取可选范围部门列表
     * 
     * @param map
     * @return
     * @Description:
     */
    public List<JSONObject> queryRangeDepartmentList(Map<String, String> map);
    
    /**
     * 根据员工id 获取企信 signId
     * 
     * @param employeeId
     * @param companyId
     * @return
     * @Description:
     */
    public String queryEmployeeSignId(String companyId, String employeeId);
    
    /**
     * 获取员工所属部门及子级部门所有员工ID
     * 
     * @param token
     * @return
     * @Description:
     */
    public List<JSONObject> queryDepartmentAuthEmployee(String companyId, String employeeId);
    
    /**
     * 项目员工卡片信息
     * 
     * @return
     * @Description:
     */
    public JSONObject queryProjectEmployee(String employeeId, String token);
}
