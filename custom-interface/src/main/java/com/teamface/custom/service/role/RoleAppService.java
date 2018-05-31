package com.teamface.custom.service.role;

import com.alibaba.fastjson.JSONArray;
import com.teamface.common.model.ServiceResult;

/** 
* @Description:  角色接口类
* @author: liupan 
* @date: 2017年11月30日 上午10:25:00 
* @version: 1.0 
*/

public interface RoleAppService {

	/**
	 * 新增角色组
	* @param reqJsonStr
	 * @param reqJsonStr2 
	* @return
	* @Description:
	 */
	ServiceResult<String> addRoleGroup(String token, String reqJsonStr);
	
	/**
	 * 重命名角色组
	* @param reqJsonStr
	* @return
	* @Description:
	 */
	ServiceResult<String> renameRoleGroup(String token,String reqJsonStr);
	
	/**
	 * 删除角色组
	* @param reqJsonStr
	* @return
	* @Description:
	 */
	ServiceResult<String> deleteRoleGroup(String token,String reqJsonStr);
	
	/**
	 * 新增角色
	* @param reqJsonStr
	* @return
	* @Description:
	 */
	ServiceResult<String> addRole(String token,String reqJsonStr);
	
	/**
	 * 修改角色
	* @param reqJsonStr
	* @return
	* @Description:
	 */
	ServiceResult<String> modifyRole(String token,String reqJsonStr);
	
	/**
	 * 删除角色
	* @param reqJsonStr
	* @return
	* @Description:
	 */
	ServiceResult<String> deleteRole(String token,String reqJsonStr);
	
	/**
	 * 获取角色组和角色列表
	* @return
	* @Description:
	 */
	JSONArray getRoleGroupList(String token);

}

    