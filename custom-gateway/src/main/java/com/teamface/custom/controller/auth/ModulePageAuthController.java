package com.teamface.custom.controller.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.JsonResUtil;
import com.teamface.custom.service.auth.ModulePageAuthAppService;

/**
 * 模块页面权限控制器
 * @author Administrator
 */
@Controller
@RequestMapping(value = "/modulePageAuth")
public class ModulePageAuthController {
	@Autowired
	private ModulePageAuthAppService modulePageAuthAppService;
	
	/**
     * @return JSONObject
     * @Description: 获取指定模块的页面列表
     */
    @RequestMapping(value = "/getModulePageList", method = RequestMethod.GET)
    public @ResponseBody JSONObject getModulePageList(@RequestHeader("TOKEN") String token, @RequestParam(required = true) String moduleName)
    {
        JSONArray result = modulePageAuthAppService.getModulePageList(token, moduleName);
        return JsonResUtil.getSuccessJsonObject(result);
    }
	
    /**
     * @return JSONObject
     * @Description: 对模块页面进行权限设置
     */
    @RequestMapping(value = "/modifyModulePageAuth", method = RequestMethod.POST)
    public @ResponseBody JSONObject modifyModulePageAuth(@RequestHeader("TOKEN") String token, @RequestBody(required = true) String reqJsonStr)
    {
    	ServiceResult<String> result = modulePageAuthAppService.modifyModulePageAuth(token, reqJsonStr);
    	if (!result.isSucces())
        {
            return JsonResUtil.getResultJsonObject(result.getCode(), result.getObj());
        }
        return JsonResUtil.getSuccessJsonObject();
    }
	
}
