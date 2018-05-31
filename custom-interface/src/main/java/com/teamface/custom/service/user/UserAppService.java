
package com.teamface.custom.service.user;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;
import com.teamface.custom.model.AccountVO;

/**
 * @Title:用户dubbo接口
 * @Description:提供用户注册、登录、获取等接口
 * @Author:caojianhua
 * @Since:2017年9月27日
 * @Version:1.1.0
 */
public interface UserAppService {

	/**
	 * @param accountVO
	 * @param clientFlag
	 * @return JSONObject
	 * @Description: 用户注册
	 */
	JSONObject register(AccountVO accountVO, String clientFlag);

	/**
	 * @param verifyJsonStr
	 * @return ServiceResult
	 * @Description:发送短信码
	 */
	ServiceResult<String> sendSmsCode(String verifyJsonStr);

	/**
	 * @param verifyJsonStr
	 * @return ServiceResult
	 * @Description:验证短信码
	 */
	ServiceResult<String> verifySmsCode(String verifyJsonStr);

	/**
	 * @param modifyJsonStr
	 * @param clientFlag
	 * @param ip
	 * @return ServiceResult
	 * @Description:忘记密码 > 修改密码
	 */
	JSONObject modifyPwd(String modifyJsonStr, String clientFlag, String ip);

	/**
	 * 完善信息
	 * 
	 * @param perfectJsonStr
	 * @param token
	 * @param clientFlag
	 * @return
	 * @Description:
	 */
	JSONObject perfectInfo(String perfectJsonStr, Long accountId, String clientFlag);

	/**
	 * 获取公司列表
	 * 
	 * @param perfectJsonStr
	 * @return
	 * @Description:
	 */
	List<Map<String, Object>> companyList(Map<String, Object> map);

	/**
	 * 修改完善度
	 * 
	 * @param perfectJsonStr
	 * @return
	 * @Description:
	 */
	ServiceResult<String> upPerfect(String perfectJsonStr);

	/**
	 * 完善个人
	 * 
	 * @param perfectJsonStr
	 * @param token
	 * @return
	 * @Description:
	 */
	ServiceResult<String> personalInfo(String perfectJsonStr, String token);

	/**
	 * 切换企业登录
	 * 
	 * @param map
	 * @return
	 * @Description:
	 */
	JSONObject companyLogin(Map<String, Object> map);

	/**
	 * 用户登录
	 * 
	 * @param map
	 * @param request
	 * @return
	 * @Description:
	 */
	JSONObject login(Map<String, Object> map);

	/**
	 * 获取标识
	 * 
	 * @return
	 * @Description:
	 */
	JSONObject queryCode();

	/**
	 * 扫码验证
	 * 
	 * @param map
	 * @return
	 * @Description:
	 */
	JSONObject valiLogin(Map<String, Object> map);

	/**
	 * 验证登录
	 * 
	 * @param map
	 * @return
	 * @Description:
	 */
	JSONObject scanCodeLogin(Map<String, Object> map);

	/**
	 * 获取详情
	 * 
	 * @param token
	 * @return
	 * @Description:
	 */
	JSONObject queryInfo(String token);

	/**
	 * 查询公司组织架构
	 * 
	 * @param token
	 * @return
	 * @Description:
	 */
	JSONArray findUsersByCompany(String token);

	/**
	 * 修改密码
	 * 
	 * @param perfectJsonStr
	 * @return
	 * @Description:
	 */
	ServiceResult<String> editPassWord(Map<String, Object> map);

	/**
	 * 新版注册
	 * 
	 * @param map
	 * @return
	 * @Description:
	 */
	JSONObject userRegister(Map<String, Object> map);

	/**
	 * 后台企业管理
	 * 
	 * @param map
	 * @return
	 * @Description:
	 */
	ServiceResult<String> editCompany(Map<String, Object> map);

	/**
	 * 转让企业
	 * 
	 * @param map
	 * @return
	 * @Description:
	 */
	ServiceResult<String> editCompanyOwner(Map<String, String> map);

	/**
	 * 公司组织架构模糊搜索
	 * 
	 * @param map
	 * @return
	 * @Description:
	 */
	List<JSONObject> findByUserName(Map<String, String> map);

	/**
	 * 后台安全管理 安全策略
	 * 
	 * @param map
	 * @return
	 * @Description:
	 */
	ServiceResult<String> saveCompanySafe(Map<String, Object> map);

	/**
	 * 后台安全管理 会话设置
	 * 
	 * @param map
	 * @return
	 * @Description:
	 */
	ServiceResult<String> saveLinkSetting(Map<String, Object> map);

	/**
	 * 后台安全管理IP白名单
	 * 
	 * @param map
	 * @return
	 * @Description:
	 */
	ServiceResult<String> saveCompanyWhite(Map<String, String> map);

	/**
	 * 后台安全管理IP
	 * 
	 * @param map
	 * @return
	 * @Description:
	 */
	ServiceResult<String> saveCompanyIp(JSONObject jsonObject, String token);

	/**
	 * ip设置列表
	 * 
	 * @param map
	 * @return
	 * @Description:
	 */
	JSONObject queryCompanyIpList(Map<String, String> map);

	/**
	 * 删除IP设置
	 * 
	 * @param map
	 * @return
	 * @Description:
	 */
	ServiceResult<String> delCompanyIp(Map<String, String> map);

	/**
	 * 获取密码策略设置
	 * 
	 * @param map
	 * @return
	 * @Description:
	 */
	JSONObject queryCompanySet(Map<String, String> map);

	/**
	 * 获取ip白名单列表
	 * 
	 * @param map
	 * @return
	 * @Description:
	 */
	List<JSONObject> queryCompanyWhiteList(Map<String, String> map);

	/**
	 * 添加个性偏好皮肤
	 * 
	 * @param map
	 * @return
	 * @Description:
	 */
	ServiceResult<String> savaBackg(Map<String, String> map);

	/**
	 * 后台安全管理 删除ip白名单设置
	 * 
	 * @param map
	 * @return
	 * @Description:
	 */
	ServiceResult<String> delCompanyWhite(Map<String, String> map);

	/**
	 * 不需要token获取最近公司密码策略
	 * 
	 * @param phone
	 * 
	 * @return
	 * @Description:
	 */
	JSONObject getCompanySet(String phone);

	/**
	 * @param companyId
	 * @param employeeId
	 * @return Map
	 * @Description:获取登录用户信息
	 */
	Map<String, Object> getUserInfo(String companyId, String employeeId);

	/**
	 * 
	 * @param modifyJsonStr
	 * @param token
	 * @return
	 */
	ServiceResult<String>  modifyPhone(String modifyJsonStr, String token);
	
	/**
	 * 验证IP是否存在
	 * @param map
	 * @return
	 */
	ServiceResult<String> vailCompanyIp(Map<String, String> map);

}
