package com.teamface.custom.controller.allot;

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

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.DataTypes;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.JsonResUtil;
import com.teamface.custom.service.allot.AllotAppService;

/**
 * @Description: 自动分配控制器
 * @author: liupan
 * @date: 2017年11月28日 上午9:26:26
 * @version: 1.0
 */
@Controller
@RequestMapping("/allot")
public class AllotController {

	private static final Logger LOG = LogManager.getLogger(AllotController.class);

	@Autowired
	private AllotAppService allotAppService;

	/**
	 * 分配规则列表
	 * 
	 * @param request
	 * @return
	 * @Description:
	 */
	@RequestMapping(value = "/queryAllotList", method = RequestMethod.GET)
	public @ResponseBody JSONObject queryAllotList(HttpServletRequest request,
			@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token) {
		List<JSONObject> resultMap = null;
		try {
			String bean = request.getParameter("bean");
			if (StringUtils.isBlank(bean)) {
				return JsonResUtil.getResultJsonByIdent("common.req.param.error");
			}
			resultMap = allotAppService.queryAllotList(token, bean);
		} catch (Exception e) {
			LOG.error(e);
			e.printStackTrace();
		}
		return JsonResUtil.getSuccessJsonObject(resultMap);
	}

	/**
	 * 修改分配规则状态
	 * 
	 * @param reqJsonStr
	 * @param token
	 * @return
	 * @Description:
	 */
	@RequestMapping(value = "/delAllot", method = RequestMethod.POST)
	public @ResponseBody JSONObject editAllot(@RequestBody(required = true) String reqJsonStr,
			@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token) {
		ServiceResult<String> serviceResult = new ServiceResult<>();
		try {
			JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
			String id = layoutJson.get("id").toString();
			String bean = layoutJson.get("bean").toString();
			if (StringUtils.isBlank(id) || StringUtils.isBlank(bean)) {
				return JsonResUtil.getResultJsonByIdent("common.req.param.error");
			}
			Map<String, Object> map = new HashMap<>();
			map.put("id", id);
			map.put("bean", bean);
			map.put("token", token);
			serviceResult = allotAppService.delAllot(map);
			if (!serviceResult.isSucces()) {
				return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
			}
		} catch (Exception e) {
			LOG.error(e);
			e.printStackTrace();
		}
		return JsonResUtil.getSuccessJsonObject();

	}


	/**
	 * 保存分配规则
	 * 
	 * @param reqJsonStr
	 * @param token
	 * @return
	 * @Description:
	 */
	@RequestMapping(value = "/saveAllotRule", method = RequestMethod.POST)
	public @ResponseBody JSONObject saveAllotRule(@RequestBody(required = true) String reqJsonStr,
			@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token) {
		ServiceResult<String> serviceResult = new ServiceResult<>();
		try {
			serviceResult = allotAppService.saveAllotRule(token, reqJsonStr);
			if (!serviceResult.isSucces()) {
				return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
			}
		} catch (Exception e) {
			LOG.error(e);
			e.printStackTrace();
		}
		return JsonResUtil.getSuccessJsonObject();
	}

	/**
	 * 根据ID获取相应规则数据
	 * 
	 * @param request
	 * @return
	 * @Description:
	 */
	@RequestMapping(value = "/queryInitAllotDateil", method = RequestMethod.GET)
	public @ResponseBody JSONObject queryInitAllotDateil(HttpServletRequest request,
			@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token) {
		JSONObject jsonObject = null;
		try {
			String id = request.getParameter("id");
			String bean = request.getParameter("bean");
			if (StringUtils.isBlank(id)||StringUtils.isBlank(bean)) {
				return JsonResUtil.getResultJsonByIdent("common.req.param.error");
			}
			Map<String, Object> map = new HashMap<>();
			map.put("id", id);
			map.put("bean", bean);
			map.put("token", token);
			jsonObject = allotAppService.queryInitAllotDateil(map);
		} catch (Exception e) {
			LOG.error(e);
			e.printStackTrace();
		}
		return JsonResUtil.getSuccessJsonObject(jsonObject);
	}

	/**
	 * 修改分配规则
	 * 
	 * @param reqJsonStr
	 * @param token
	 * @return
	 * @Description:
	 */
	@RequestMapping(value = "/editAllotRule", method = RequestMethod.POST)
	public @ResponseBody JSONObject editAllotRule(@RequestBody(required = true) String reqJsonStr,
			@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token) {
		ServiceResult<String> serviceResult = new ServiceResult<>();
		try {

			Map<String, Object> map = new HashMap<>();
			map.put("reqJsonStr", reqJsonStr);
			map.put("token", token);
			serviceResult = allotAppService.editAllotRule(map);
			if (!serviceResult.isSucces()) {
				return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
			}
		} catch (Exception e) {
			LOG.error(e);
			e.printStackTrace();
		}
		return JsonResUtil.getSuccessJsonObject();

	}
}
