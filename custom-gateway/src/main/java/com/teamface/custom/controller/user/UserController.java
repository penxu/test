
package com.teamface.custom.controller.user;

import java.util.ArrayList;
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
import com.teamface.common.constant.DataTypes;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.JsonResUtil;
import com.teamface.custom.model.AccountVO;
import com.teamface.custom.service.user.UserAppService;

/**
 * @Title:用户控制器
 * @Description:提供用户注册、登录、获取等接口
 * @Author:liupan
 * @Since:2017年11月27日
 * @Version:1.1.0
 */
@Controller
@RequestMapping("/user")
public class UserController {
	private static final Logger LOG = LogManager.getLogger(UserController.class);

	@Autowired
	private UserAppService userAppService;

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public @ResponseBody JSONObject login(@RequestBody(required = true) String reqJsonStr, HttpServletRequest request,
			@RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag) {
		Map<String, Object> map = new HashMap<>();
		LOG.error("进入方法++++++++++++++++++++++++++");
		JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
		String userName = layoutJson.getString("userName");
		String passWord = layoutJson.getString("passWord");
		String userCode = layoutJson.getString("userCode");
		if (StringUtils.isEmpty(userName) || StringUtils.isEmpty(passWord)) {
			return JsonResUtil.getResultJsonByIdent("common.req.param.error");
		}
		LOG.error("userName++++++++++++++++++++++++++" + userName);
		LOG.error("passWord方法++++++++++++++++++++++++++" + passWord);
		map.put("userName", userName);
		map.put("clientFlag", clientFlag);
		map.put("passWord", passWord);
		map.put("userCode", userCode);
		map.put("ip", request.getRemoteAddr());
		JSONObject obj = userAppService.login(map);
		if (null == obj.get("perfect")) {
			return obj;
		}
		return JsonResUtil.getSuccessJsonObject(obj);
	}

	/**
	 * 生成唯一标识
	 * 
	 * @param req
	 * @return
	 * @Description:
	 */
	@RequestMapping(value = "/queryCode", method = RequestMethod.GET)
	public @ResponseBody JSONObject queryCode() {
		JSONObject obj = null;
		try {
			obj = userAppService.queryCode();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return JsonResUtil.getFailJsonObject();
		}
		return JsonResUtil.getSuccessJsonObject(obj);
	}

	/**
	 * 扫码验证
	 * 
	 * @param req
	 * @return
	 * @Description:
	 */
	@RequestMapping(value = "/valiLogin", method = RequestMethod.POST)
	public @ResponseBody JSONObject valiLogin(@RequestBody(required = true) String reqJsonStr) {
		JSONObject obj = null;
		try {
			JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
			Map<String, Object> map = new HashMap<>();
			String id = reqJson.getString("id");
			String userName = reqJson.getString("userName");
			map.put("id", id);
			map.put("userName", userName);
			obj = userAppService.valiLogin(map);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return JsonResUtil.getFailJsonObject();
		}
		return JsonResUtil.getSuccessJsonObject(obj);
	}

	/**
	 * 验证扫码登录
	 * 
	 * @param req
	 * @return
	 * @Description:
	 */
	@RequestMapping(value = "/scanCodeLogin", method = RequestMethod.GET)
	public @ResponseBody JSONObject scanCodeLogin(HttpServletRequest request,
			@RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag) {
		JSONObject obj = null;
		try {
			Map<String, Object> map = new HashMap<>();
			String id = request.getParameter("id");
			if (StringUtils.isBlank(id)) {
				return JsonResUtil.getResultJsonByIdent("common.req.param.error");
			}
			map.put("id", id);
			map.put("clientFlag", clientFlag);
			obj = userAppService.scanCodeLogin(map);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return JsonResUtil.getFailJsonObject();
		}
		return JsonResUtil.getSuccessJsonObject(obj);
	}

	/** 注册 */
	@RequestMapping(value = "/register", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody JSONObject register(@RequestBody(required = true) AccountVO accountVO,
			@RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag) {
		LOG.error("121212131231321321++++++++++++++++++++++++++++++++++++++++++++++++++++");
		JSONObject result = userAppService.register(accountVO, clientFlag);
		if (null == result) {
			return JsonResUtil.getFailJsonObject();
		}
		return JsonResUtil.getSuccessJsonObject(result);
	}

	/** 发送短信码 */
	@RequestMapping(value = "/sendSmsCode", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody JSONObject sendSmsCode(@RequestBody(required = true) String verifyJsonStr) {
		ServiceResult<String> serviceResult = userAppService.sendSmsCode(verifyJsonStr);
		if (!serviceResult.isSucces()) {
			return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
		}
		return JsonResUtil.getSuccessJsonObject();
	}

	/** 验证短信码 */
	@RequestMapping(value = "/verifySmsCode", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody JSONObject verifySmsCode(@RequestBody(required = true) String verifyJsonStr) {
		ServiceResult<String> serviceResult = userAppService.verifySmsCode(verifyJsonStr);
		if (!serviceResult.isSucces()) {
			return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
		}
		return JsonResUtil.getSuccessJsonObject();
	}

	/** 修改密码 */
	@RequestMapping(value = "/modifyPwd", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody JSONObject modifyPwd(@RequestBody(required = true) String modifyJsonStr,
			HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag) {
		JSONObject jsonObject = userAppService.modifyPwd(modifyJsonStr, clientFlag, request.getRemoteAddr());
		if (null == jsonObject.get("perfect")) {
			return jsonObject;
		}
		return JsonResUtil.getSuccessJsonObject(jsonObject);
	}

	/** 获取可切换公司列表 **/
	@RequestMapping(value = "/companyList", method = RequestMethod.GET)
	public @ResponseBody JSONObject companyList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token) {
		if (StringUtils.isBlank(token)) {
			return JsonResUtil.getResultJsonByIdent("common.req.param.error");
		}
		Map<String, Object> map = new HashMap<>();
		map.put("token", token);
		List<Map<String, Object>> listMap = userAppService.companyList(map);
		return JsonResUtil.getSuccessJsonObject(listMap);
	}

	/** 完善个人资料 **/
	@RequestMapping(value = "/personalInfo", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody JSONObject personalInfo(@RequestBody(required = true) String perfectJsonStr,
			@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
			@RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag) {
		ServiceResult<String> serviceResult = userAppService.personalInfo(perfectJsonStr, token);
		if (!serviceResult.isSucces()) {
			return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
		}
		return JsonResUtil.getSuccessJsonObject();
	}

	/** 公司列表 **/
	@RequestMapping(value = "/companyLogin", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody JSONObject companyLogin(@RequestBody(required = true) String perfectJsonStr,
			@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
			@RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag) {
		Map<String, Object> map = new HashMap<>();
		JSONObject layoutJson = JSONObject.parseObject(perfectJsonStr);
		String companyId = layoutJson.get("company_id").toString();
		if (StringUtils.isEmpty(companyId)) {
			return JsonResUtil.getResultJsonByIdent("common.req.param.error");
		}
		map.put("companyId", companyId);
		map.put("clientFlag", clientFlag);
		map.put("token", token);
		JSONObject obj = userAppService.companyLogin(map);
		return JsonResUtil.getSuccessJsonObject(obj);
	}

	/** 修改个人资料 **/
	@RequestMapping(value = "/updateInfo", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody JSONObject upPerfect(@RequestBody(required = true) String perfectJsonStr) {
		ServiceResult<String> serviceResult = userAppService.upPerfect(perfectJsonStr);
		if (!serviceResult.isSucces()) {
			return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
		}
		return JsonResUtil.getSuccessJsonObject();
	}

	/** 获取个人资料 **/
	@RequestMapping(value = "/queryInfo", method = RequestMethod.GET)
	public @ResponseBody JSONObject queryInfo(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token) {
		JSONObject object = userAppService.queryInfo(token);
		return JsonResUtil.getSuccessJsonObject(object);
	}

	/**
	 * @param map
	 * @return JSONObject
	 * @Description:查询公司组织架构
	 */
	@RequestMapping(value = "/findUsersByCompany", method = RequestMethod.GET)
	public @ResponseBody JSONObject findUsersByCompany(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token) {
		JSONArray companyFramework = userAppService.findUsersByCompany(token);
		return JsonResUtil.getSuccessJsonObject(companyFramework);
	}

	/**
	 * 修改密码
	 * 
	 * @param perfectJsonStr
	 * @param token
	 * @return
	 * @Description:
	 */
	@RequestMapping(value = "/editPassWord", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody JSONObject editPassWord(@RequestBody(required = true) String perfectJsonStr,
			@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token) {

		Map<String, Object> map = new HashMap<>();
		JSONObject layoutJson = JSONObject.parseObject(perfectJsonStr);
		String passWord = layoutJson.get("passWord").toString();
		String newPassWord = layoutJson.get("newPassWord").toString();
		if (StringUtils.isEmpty(newPassWord) || StringUtils.isEmpty(passWord)) {
			return JsonResUtil.getResultJsonByIdent("common.req.param.error");
		}
		map.put("passWord", passWord);
		map.put("newPassWord", newPassWord);
		map.put("token", token);
		ServiceResult<String> serviceResult = userAppService.editPassWord(map);
		if (!serviceResult.isSucces()) {
			return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
		}
		return JsonResUtil.getSuccessJsonObject();
	}

	/** 注册 */
	@RequestMapping(value = "/userRegister", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody JSONObject userRegister(@RequestBody(required = true) String perfectJsonStr,
			@RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag) {
		JSONObject jsonObject = null;
		try {
			JSONObject layoutJson = JSONObject.parseObject(perfectJsonStr);
			Object companyName = layoutJson.getString("company_name");
			Object userName = layoutJson.getString("user_name");
			Object userPwd = layoutJson.getString("user_pwd");
			Object phone = layoutJson.getString("phone");
			Object inviteCode = layoutJson.getString("invite_code");
			if (null == companyName || null == userName | null == userPwd || null == phone) {
				return JsonResUtil.getResultJsonByIdent("common.req.param.error");
			}
			Map<String, Object> map = new HashMap<>();
			map.put("companyName", companyName);
			map.put("userName", userName);
			map.put("phone", phone);
			map.put("passWord", userPwd);
			map.put("inviteCode", inviteCode);
			map.put("clientFlag", clientFlag);
			jsonObject = userAppService.userRegister(map);
			if (null == jsonObject.get("perfect")) {
				return jsonObject;
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return JsonResUtil.getSuccessJsonObject(jsonObject);
	}

	/**
	 * 后台企业管理
	 * 
	 * @param perfectJsonStr
	 * @param token
	 * @return
	 * @Description:
	 */
	@RequestMapping(value = "/editCompany", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody JSONObject editCompany(@RequestBody(required = true) String perfectJsonStr,
			@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token) {

		JSONObject layoutJson = JSONObject.parseObject(perfectJsonStr);
		String data = layoutJson.get("data").toString();
		Map<String, Object> map = new HashMap<>();
		map.put("data", data);
		map.put("token", token);

		ServiceResult<String> serviceResult = userAppService.editCompany(map);
		if (!serviceResult.isSucces()) {
			return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
		}

		return JsonResUtil.getSuccessJsonObject();
	}

	/**
	 * 后台企业管理
	 * 
	 * @param perfectJsonStr
	 * @param token
	 * @return
	 * @Description:
	 */
	@RequestMapping(value = "/editCompanyOwner", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody JSONObject editCompanyOwner(@RequestBody(required = true) String perfectJsonStr,
			@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token) {

		JSONObject layoutJson = JSONObject.parseObject(perfectJsonStr);
		Map<String, String> map = new HashMap<>();
		map.put("user_pwd", layoutJson.getString("user_pwd"));
		map.put("user_id", layoutJson.getString("user_id"));
		map.put("token", token);

		ServiceResult<String> serviceResult = userAppService.editCompanyOwner(map);
		if (!serviceResult.isSucces()) {
			return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
		}

		return JsonResUtil.getSuccessJsonObject();
	}

	/**
	 * @param map
	 * @return JSONObject
	 * @Description:公司组织架构模糊搜索
	 */
	@RequestMapping(value = "/findByUserName", method = RequestMethod.GET)
	public @ResponseBody JSONObject findByUser(HttpServletRequest request,
			@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token) {
		List<JSONObject> resultMap = new ArrayList<>();
		try {
			String departmentId = request.getParameter("department_id");
			String employeeName = request.getParameter("employee_name");
			if (StringUtils.isBlank(employeeName)) {
				return JsonResUtil.getResultJsonByIdent("common.req.param.error");
			}
			Map<String, String> map = new HashMap<>();
			map.put("departmentId", departmentId);
			map.put("employeeName", employeeName);
			map.put("token", token);
			resultMap = userAppService.findByUserName(map);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return JsonResUtil.getFailJsonObject();
		}
		return JsonResUtil.getSuccessJsonObject(resultMap);
	}

	/**
	 * 后台安全管理 密码策略设置
	 * 
	 * @param perfectJsonStr
	 * @param token
	 * @return
	 * @Description:
	 */
	@RequestMapping(value = "/saveCompanySafe", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody JSONObject saveCompanySafe(@RequestBody(required = true) String perfectJsonStr,
			@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token) {

		try {
			JSONObject layoutJson = JSONObject.parseObject(perfectJsonStr);
			Map<String, Object> map = new HashMap<>();
			map.put("pwd_term", layoutJson.getInteger("pwd_term"));
			map.put("pwd_length", layoutJson.getInteger("pwd_length"));
			map.put("pwd_complex", layoutJson.getInteger("pwd_complex"));
			map.put("pwd_phone", layoutJson.getInteger("pwd_phone"));
			map.put("pwd_lock", layoutJson.getInteger("pwd_lock"));
			map.put("pwd_reset", layoutJson.getInteger("pwd_reset"));
			map.put("token", token);

			ServiceResult<String> serviceResult = userAppService.saveCompanySafe(map);
			if (!serviceResult.isSucces()) {
				return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return JsonResUtil.getFailJsonObject();
		}

		return JsonResUtil.getSuccessJsonObject();
	}

	/**
	 * 后台安全管理 会话设置
	 * 
	 * @param perfectJsonStr
	 * @param token
	 * @return
	 * @Description:
	 */
	@RequestMapping(value = "/saveLinkSetting", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody JSONObject saveLinkSetting(@RequestBody(required = true) String perfectJsonStr,
			@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token) {

		try {
			JSONObject layoutJson = JSONObject.parseObject(perfectJsonStr);
			Map<String, Object> map = new HashMap<>();
			map.put("link_set", layoutJson.getInteger("link_set"));
			map.put("token", token);

			ServiceResult<String> serviceResult = userAppService.saveLinkSetting(map);
			if (!serviceResult.isSucces()) {
				return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return JsonResUtil.getFailJsonObject();
		}

		return JsonResUtil.getSuccessJsonObject();
	}

	/**
	 * 后台安全管理 ip白名单设置
	 * 
	 * @param perfectJsonStr
	 * @param token
	 * @return
	 * @Description:
	 */
	@RequestMapping(value = "/saveCompanyWhite", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody JSONObject saveCompanyWhite(@RequestBody(required = true) String perfectJsonStr,
			@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token) {

		try {
			JSONObject layoutJson = JSONObject.parseObject(perfectJsonStr);
			Map<String, String> map = new HashMap<>();
			map.put("employee_id", layoutJson.getString("employee_id"));
			map.put("token", token);

			ServiceResult<String> serviceResult = userAppService.saveCompanyWhite(map);
			if (!serviceResult.isSucces()) {
				return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return JsonResUtil.getFailJsonObject();
		}

		return JsonResUtil.getSuccessJsonObject();
	}

	/**
	 * 后台安全管理 删除ip白名单设置
	 * 
	 * @param perfectJsonStr
	 * @param token
	 * @return
	 * @Description:
	 */
	@RequestMapping(value = "/delCompanyWhite", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody JSONObject delCompanyWhite(@RequestBody(required = true) String perfectJsonStr,
			@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token) {

		try {
			JSONObject layoutJson = JSONObject.parseObject(perfectJsonStr);
			Map<String, String> map = new HashMap<>();
			map.put("id", layoutJson.getString("id"));
			map.put("token", token);

			ServiceResult<String> serviceResult = userAppService.delCompanyWhite(map);
			if (!serviceResult.isSucces()) {
				return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return JsonResUtil.getFailJsonObject();
		}

		return JsonResUtil.getSuccessJsonObject();
	}

	/**
	 * 获取IP白名单列表数据
	 * 
	 * @param request
	 * @return
	 * @Description:
	 */
	@RequestMapping(value = "/queryCompanyWhiteList", method = RequestMethod.GET)
	public @ResponseBody JSONObject queryCompanyWhiteList(HttpServletRequest request,
			@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token) {
		List<JSONObject> resultMap = new ArrayList<>();
		try {
			Map<String, String> map = new HashMap<>();
			map.put("token", token);
			resultMap = userAppService.queryCompanyWhiteList(map);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return JsonResUtil.getFailJsonObject();
		}
		return JsonResUtil.getSuccessJsonObject(resultMap);
	}

	/**
	 * 后台安全管理 ip
	 * 
	 * @param perfectJsonStr
	 * @param token
	 * @return
	 * @Description:
	 */
	@RequestMapping(value = "/saveCompanyIp", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody JSONObject saveCompanyIp(@RequestBody(required = true) String perfectJsonStr,
			@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token) {

		try {
			JSONObject jsonObject = JSONObject.parseObject(perfectJsonStr);
			ServiceResult<String> serviceResult = userAppService.saveCompanyIp(jsonObject, token);
			if (!serviceResult.isSucces()) {
				return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return JsonResUtil.getFailJsonObject();
		}
		return JsonResUtil.getSuccessJsonObject();
	}

	/**
	 * 验证IP是否存在
	 * 
	 * @param perfectJsonStr
	 * @param token
	 * @return
	 */
	@RequestMapping(value = "/vailCompanyIp", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody JSONObject vailCompanyIp(@RequestBody(required = true) String perfectJsonStr,
			@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token) {

		try {
			JSONObject layoutJson = JSONObject.parseObject(perfectJsonStr);
			Map<String, String> map = new HashMap<>();
			map.put("ip", layoutJson.getString("ip"));
			map.put("token", token);

			ServiceResult<String> serviceResult = userAppService.vailCompanyIp(map);
			if (!serviceResult.isSucces()) {
				return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return JsonResUtil.getFailJsonObject();
		}
		return JsonResUtil.getSuccessJsonObject();
	}

	/**
	 * 获取限制IP列表数据
	 * 
	 * @param request
	 * @return
	 * @Description:
	 */
	@RequestMapping(value = "/queryCompanyIpList", method = RequestMethod.GET)
	public @ResponseBody JSONObject queryList(HttpServletRequest request,
			@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token) {
		JSONObject resultMap = new JSONObject();
		try {
			String pageSize = request.getParameter("pageSize");
			String pageNum = request.getParameter("pageNum");
			if (StringUtils.isBlank(pageSize) || StringUtils.isBlank(pageNum)) {
				return JsonResUtil.getResultJsonByIdent("common.req.param.error");
			}
			Map<String, String> map = new HashMap<>();
			map.put("pageSize", pageSize);
			map.put("pageNum", pageNum);
			map.put("token", token);
			resultMap = userAppService.queryCompanyIpList(map);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return JsonResUtil.getFailJsonObject();
		}
		return JsonResUtil.getSuccessJsonObject(resultMap);
	}

	/**
	 * 后台安全管理 ip
	 * 
	 * @param perfectJsonStr
	 * @param token
	 * @return
	 * @Description:
	 */
	@RequestMapping(value = "/delCompanyIp", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody JSONObject delCompanyIp(@RequestBody(required = true) String perfectJsonStr,
			@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token) {

		JSONObject layoutJson = JSONObject.parseObject(perfectJsonStr);
		Map<String, String> map = new HashMap<>();
		map.put("id", layoutJson.getString("id"));
		map.put("token", token);

		ServiceResult<String> serviceResult = userAppService.delCompanyIp(map);
		if (!serviceResult.isSucces()) {
			return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
		}

		return JsonResUtil.getSuccessJsonObject();
	}

	/**
	 * 获取密码策略设置
	 * 
	 * @param request
	 * @return
	 * @Description:
	 */
	@RequestMapping(value = "/queryCompanySet", method = RequestMethod.GET)
	public @ResponseBody JSONObject queryCompanySet(HttpServletRequest request,
			@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token) {
		JSONObject resultMap = new JSONObject();
		try {
			Map<String, String> map = new HashMap<>();
			map.put("token", token);
			resultMap = userAppService.queryCompanySet(map);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return JsonResUtil.getFailJsonObject();
		}
		return JsonResUtil.getSuccessJsonObject(resultMap);
	}

	/**
	 * 获取密码策略设置
	 * 
	 * @param request
	 * @return
	 * @Description:
	 */
	@RequestMapping(value = "/getCompanySet", method = RequestMethod.GET)
	public @ResponseBody JSONObject queryCompanySetting(HttpServletRequest request) {
		String phone = request.getParameter("phone");
		JSONObject resultMap = new JSONObject();
		try {
			resultMap = userAppService.getCompanySet(phone);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return JsonResUtil.getFailJsonObject();
		}
		return JsonResUtil.getSuccessJsonObject(resultMap);
	}

	/**
	 * 后台安全管理 ip
	 * 
	 * @param perfectJsonStr
	 * @param token
	 * @return
	 * @Description:
	 */
	@RequestMapping(value = "/savaBackg", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody JSONObject savaBackg(@RequestBody(required = true) String perfectJsonStr,
			@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token) {

		JSONObject layoutJson = JSONObject.parseObject(perfectJsonStr);
		Map<String, String> map = new HashMap<>();
		map.put("background_color", layoutJson.getString("background_color"));
		map.put("custom_color", layoutJson.getString("custom_color"));
		map.put("token", token);

		ServiceResult<String> serviceResult = userAppService.savaBackg(map);
		if (!serviceResult.isSucces()) {
			return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
		}

		return JsonResUtil.getSuccessJsonObject();
	}

	/** 修改密码 */
	@RequestMapping(value = "/modifyPhone", method = RequestMethod.POST, consumes = "application/json")
	public @ResponseBody JSONObject modifyPhone(@RequestBody(required = true) String modifyJsonStr,
			@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token) {
		try {
			ServiceResult<String> serviceResult = userAppService.modifyPhone(modifyJsonStr, token);
			if (!serviceResult.isSucces()) {
				return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return JsonResUtil.getFailJsonObject();
		}
		return JsonResUtil.getSuccessJsonObject();
	}
}
