package com.teamface.custom.controller.colour;

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
import com.teamface.custom.service.colour.ColourAppService;

/** 
* @Description:  自动标识颜色控制器
* @author: liupan 
* @date: 2017年11月29日 上午11:03:42 
* @version: 1.0 
*/
@Controller
@RequestMapping("/colour")
public class ColourController {
	
	private static final Logger LOG = LogManager.getLogger(ColourController.class);

	@Autowired
	private ColourAppService colourAppService;
	
	/**
	 * 分配规则列表
	 * 
	 * @param request
	 * @return
	 * @Description:
	 */
	@RequestMapping(value = "/queryColourList", method = RequestMethod.GET)
	public @ResponseBody JSONObject queryAllotList(HttpServletRequest request,
			@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token) {
		List<Map<String, Object>> resultMap = null;
		try {
			String bean = request.getParameter("bean");
			if (StringUtils.isBlank(bean)) {
				return JsonResUtil.getResultJsonByIdent("common.req.param.error");
			}
			resultMap = colourAppService.queryColourList(token, bean);
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
	@RequestMapping(value = "/delColour", method = RequestMethod.POST)
	public @ResponseBody JSONObject editColour(@RequestBody(required = true) String reqJsonStr,
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
			serviceResult = colourAppService.delColour(map);
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
	 * 保存规则条件
	 * 
	 * @param reqJsonStr
	 * @param token
	 * @return
	 * @Description:
	 */
	@RequestMapping(value = "/saveColourRule", method = RequestMethod.POST)
	public @ResponseBody JSONObject saveColourRule(@RequestBody(required = true) String reqJsonStr,
			@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token) {
		ServiceResult<String> serviceResult = new ServiceResult<>();
		try {
			serviceResult = colourAppService.saveColourRule(token, reqJsonStr);
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
	@RequestMapping(value = "/queryInitColourDateil", method = RequestMethod.GET)
	public @ResponseBody JSONObject queryInitColourDateil(HttpServletRequest request,
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
			jsonObject = colourAppService.queryInitColourDateil(map);
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
	@RequestMapping(value = "/editColourRule", method = RequestMethod.POST)
	public @ResponseBody JSONObject editAllotRule(@RequestBody(required = true) String reqJsonStr,
			@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token) {
		ServiceResult<String> serviceResult = new ServiceResult<>();
		try {

			Map<String, Object> map = new HashMap<>();
			map.put("reqJsonStr", reqJsonStr);
			map.put("token", token);
			serviceResult = colourAppService.editColourRule(map);
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

    