package com.teamface.custom.controller.memo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.DataTypes;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.JsonResUtil;
import com.teamface.custom.service.memo.MemoAppService;

@Controller
@RequestMapping(value = "/memo")
public class MemoController
{
    @Autowired
    MemoAppService memoAppService;
    
    /**
     * @param token
     * @param jsonStr
     * @return
     * @Description:新增备忘录
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject save(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = memoAppService.sava(token, jsonStr);
        return JsonResUtil.getSuccessJsonObject(JSONObject.parseObject(returnMSG.getObj()));
    }
    
    /**
     * @param token
     * @param jsonStr
     * @return
     * @Description:更新备忘录
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject update(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = memoAppService.update(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * @param token
     * @param ids
     * @param type
     * @return
     * @Description:删除、彻底删除、恢复、退出共享 type=1、2、3、4
     */
    @RequestMapping(value = "/del", method = RequestMethod.POST)
    public @ResponseBody JSONObject del(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) String ids,
        @RequestParam(required = true) Integer type)
    {
        ServiceResult<String> returnMSG = memoAppService.del(token, ids, type);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * @param token
     * @param id
     * @return
     * @Description:根据ID查询备忘录详情
     */
    @RequestMapping(value = "/findMemoDetail", method = RequestMethod.GET)
    public @ResponseBody JSONObject findMemoDetail(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id)
    {
        JSONObject memoDetail = memoAppService.findMemoDetail(token, id);
        return JsonResUtil.getSuccessJsonObject(memoDetail);
    }
    
    /**
     * @param token
     * @param type
     * @param keyword
     * @param pageNum
     * @param pageSize
     * @return
     * @Description:查询备忘录列表
     */
    @RequestMapping(value = "/findMemoList", method = RequestMethod.GET)
    public @ResponseBody JSONObject findMemoList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = false) Integer type,
        @RequestParam(required = false) String keyword, @RequestParam(required = false) Integer pageNum, @RequestParam(required = false) Integer pageSize)
    {
        JSONObject memoList = memoAppService.findMemoList(token, type, keyword, pageNum, pageSize);
        return JsonResUtil.getSuccessJsonObject(memoList);
    }
    
    /**
     * @param token
     * @param type
     * @param pageSize
     * @return
     * @Description:查询备忘录列表(web端不分页)
     */
    @RequestMapping(value = "/findMemoWebList", method = RequestMethod.GET)
    public @ResponseBody JSONObject findMemoWebList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = false) Integer type,
        @RequestParam(required = false) String keyword)
    {
        JSONObject memoList = memoAppService.findMemoList(token, type, keyword);
        return JsonResUtil.getSuccessJsonObject(memoList);
    }
    
    /**
     * @param token
     * @param id
     * @return
     * @Description:获取数据权限
     */
    @RequestMapping(value = "/findDataAuth", method = RequestMethod.GET)
    public @ResponseBody JSONObject findDataAuth(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id)
    {
        JSONObject memoDetail = memoAppService.findDataAuth(token, id);
        return JsonResUtil.getSuccessJsonObject(memoDetail);
    }
    
    /**
     * @param token
     * @param list
     * @return
     * @Description:查询备忘录详情列表
     */
    @RequestMapping(value = "/findMemoDetailForProject", method = RequestMethod.GET)
    public @ResponseBody JSONObject findMemoDetailForProject(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) List<JSONObject> dataList)
    {
        List<JSONObject> memoList = memoAppService.findMemoDetail(token, dataList);
        return JsonResUtil.getSuccessJsonObject(memoList);
    }
    
}
