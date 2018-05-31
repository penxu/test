package com.teamface.custom.service.im;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;
import com.teamface.custom.model.ImCircleComment;
import com.teamface.custom.model.ImCircleMain;
import com.teamface.custom.model.ImCircleUp;
import com.teamface.custom.model.PageVo;

/**
 * 
 * @Title:
 * @Description:同事圈服务接口
 * @Author:dsl
 * @Since:2017年11月21日
 * @Version:1.1.0
 */
public interface ImCircleAppService
{
    
    /**
     * 企业圈的添加
     * 
     * @param employeeId
     * @param imCircleMain
     * @return
     */
    ServiceResult<String> imCircleAdd(String token, ImCircleMain imCircleMain);
    
    /**
     * 企业圈中的点赞
     * 
     * @param employeeId
     * @param id
     * @return
     */
    ServiceResult<String> imCircleUp(String token, Long circleMainId);
    
    /**
     * 评论
     * 
     * @param employeeId
     * @param circleMainId
     * @param contentInfo
     * @return
     */
    JSONObject imCircleComment(String token, Long circleMainId, String contentInfo, Long receiverId);
    
    /**
     * 删除企业圈
     * 
     * @param employeeId
     * @param circleMainId
     * @return
     */
    ServiceResult<String> imCircleDelete(String token, Long circleMainId);
    
    /**
     * 删除评论
     * 
     * @param employeeId
     * @param commentId
     * @return
     */
    ServiceResult<String> imCircleCommentDelete(String token, Long commentId);
    
    /**
     * 查询个人或者全公司的企业圈的列表
     * 
     * @param employeeId
     * @param isPerson
     * @return
     */
    PageVo<Map<String, Object>> imCircleList(String token, Long isPerson, Long startTime, Long endTime, Integer pageNo, Integer pageSize);
    
    /**
     * 
     * @param token
     * @param paramMap
     * @return
     * @Description:查询用户最近图片
     */
    List<Map<String, Object>> findLastPhoto(String token, Map<String, Object> paramMap);
    
    /**
     * 
     * @param token
     * @param paramMap
     * @return
     * @Description:查询用户发布信息图片
     */
    List<Map<String, Object>> findVoListPhoto(String token, Map<String, Object> paramMap);
    
    /**
     * 
     * @param token
     * @param paramMap
     * @return
     * @Description:查询用户评论列表
     */
    List<ImCircleComment> findVoListComment(String token, Map<String, Object> paramMap);
    
    /**
     * 
     * @param token
     * @param paramMap
     * @return
     * @Description:查询用户点赞列表
     */
    List<ImCircleUp> findVoListUp(String token, Map<String, Object> paramMap);
    
    /**
     * 
     * @param token
     * @param url
     * @return
     * @Description:修改同事圈背景图(暂取消)
     */
    ServiceResult<String> backGroundMod(String token, String url);
    
    /**
     * 
     * @param token
     * @param url
     * @return
     * @Description:获取同事圈发布信息列表
     */
    PageVo<Map<String, Object>> getAllInfo(String token, Long isPerson, Long startTime, Long endTime, Integer pageNo, Integer pageSize);
    
}