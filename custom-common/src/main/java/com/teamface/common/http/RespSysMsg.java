package com.teamface.common.http;

import com.teamface.common.model.Response;

/**
 * 自定义系统返回消息
 * 
 * @author Liu
 * @date 2016-11-22
 * @version 1.0
 */
public class RespSysMsg {

	public static final Response SYS_SUCCESS = new Response(0, "success");
	/** 200服务器请求成功，且提供了请求的网页和数据 **/
	public static final Response SYS_SUCCESS_200 = new Response(200, "SUCCESS");
	/** 202服务器已接收请求，但尚未处理，服务器繁忙 **/
	public static final Response SYS_SUCCESS_201 = new Response(202, "SERVER_BUSY");
	/** 204服务处理成功，但不返回任何内容 **/
	public static final Response SYS_SUCCESS_204 = new Response(204, "SUCCCESS_NOCONTENT");

	/** 400请求错误，Bad Request **/
	public static final Response SYS_ERROR_400 = new Response(400, "BAD_REQUEST");
	/** 404找不到请求的资源 **/
	public static final Response SYS_ERROR_404 = new Response(404, "SOURCE_NOFIND");
	/** 405请求方法禁用 **/
	public static final Response SYS_ERROR_405 = new Response(405, "SOURCE_FORBIDDEN");
	/** 407上传文件大于10MB **/
	public static final Response SYS_ERROR_UPLAOD_FILE_OUTOF_SIZE = new Response(407, "上传文件大于10MB");
	/** 服务器异常 **/
	public static final Response SYS_SERVER_THROWS_EXCEPTION = new Response(500, "服务器出现繁忙，请稍后再请求.");
	/** 请求参数错误 **/
	public static final Response SYS_REQUEST_PARAMETER_ERROR = new Response(100, "请求参数错误。");
	/** 服务器超时 **/
	public static final Response SYS_TIMEOUT = new Response(101, "响应超时,服务器繁忙.");
	/** 用户未登录 **/
	public static final Response SYS_USER_NOT_LOGIN = new Response(102, "请登录.");
	/** token过期 **/
	public static final Response SYS_SESSION_OVERDUE = new Response(103, "请重新登录");
	/** 没有实现的功能 **/
	public static final Response SYS_NO_IMPLEMENTED_FUNCTION = new Response(104, "没有实现的功能！");
	/** 上传文件失败 **/
	public static final Response SYS_ERROR_UPLAOD_FILE = new Response(105, "上传文件失败");
	/** 记录不存在 **/
	public static final Response SYS_ERROR_RECORD_NOTEXIST = new Response(106, "记录不存在");

	public static Response getResponse(Integer code, String describe) {
		return new Response(code, describe);
	}

}
