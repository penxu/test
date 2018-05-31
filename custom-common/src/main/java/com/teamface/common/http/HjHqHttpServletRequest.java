package com.teamface.common.http;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.teamface.common.util.MathUtil;
import com.teamface.common.util.StringUtil;


/**
 * 请求参数包装器
 * 
 * @author huangym
 * 
 */
public class HjHqHttpServletRequest extends HttpServletRequestWrapper {

	private HttpServletRequest request;

	public HjHqHttpServletRequest(HttpServletRequest request) {
		super(request);
		this.request = request;
	}

	public HttpServletRequest getHttpServletRequest() {
		return this.request;
	}
	
	public String getParameter(String name) {
		return getStr(name);
	}
	
	public String[] getParameterValues(String name){
		return getStrs(name);
	}
	
	public List<String> getReqParams() {
		Enumeration<String> reqEnum = request.getParameterNames();
		List<String> reqParams = new ArrayList<String>();
		while (reqEnum.hasMoreElements()) {
			reqParams.add(reqEnum.nextElement());
		}
		return reqParams;
	}

	public String getStr(String reqParam) {
		return StringUtil.getParameter(request.getParameter(reqParam));
	}
	
	public String getStrNoEcode(String reqParam) {
		return request.getParameter(reqParam);
	}
	
	public String[] getStrs(String reqParam) {
		String[] reqParams = request.getParameterValues(reqParam);
		if (reqParams != null) {
			int len = reqParams.length;
			String[] newReqParams = new String[len];
			for (int i = 0; i < len; i++) {
				newReqParams[i] = StringUtil.getParameter(reqParams[i]);
			}
			return newReqParams;
		}
		return null;
	}

	public byte getByte(String reqParam) {
		return MathUtil.toByte(request.getParameter(reqParam));
	}

	public byte[] getBytes(String reqParam) {
		String[] reqParams = request.getParameterValues(reqParam);
		if (reqParams != null) {
			int len = reqParams.length;
			byte[] newReqParams = new byte[len];
			for (int i = 0; i < len; i++) {
				newReqParams[i] = MathUtil.toByte(reqParams[i]);
			}
			return newReqParams;
		}
		return null;
	}

	public int getShort(String reqParam) {
		return MathUtil.toShort(request.getParameter(reqParam));
	}

	public short[] getShorts(String reqParam) {
		String[] reqParams = request.getParameterValues(reqParam);
		if (reqParams != null) {
			int len = reqParams.length;
			short[] newReqParams = new short[len];
			for (int i = 0; i < len; i++) {
				newReqParams[i] = MathUtil.toShort(reqParams[i]);
			}
			return newReqParams;
		}
		return null;
	}

	public int getInt(String reqParam) {
		return MathUtil.toInt(request.getParameter(reqParam));
	}

	public int[] getInts(String reqParam) {
		String[] reqParams = request.getParameterValues(reqParam);
		if (reqParams != null) {
			int len = reqParams.length;
			int[] newReqParams = new int[len];
			for (int i = 0; i < len; i++) {
				newReqParams[i] = MathUtil.toInt(reqParams[i]);
			}
			return newReqParams;
		}
		return null;
	}

	public long getLong(String reqParam) {
		return MathUtil.toLong(request.getParameter(reqParam));
	}

	public long[] getLongs(String reqParam) {
		String[] reqParams = request.getParameterValues(reqParam);
		if (reqParams != null) {
			int len = reqParams.length;
			long[] newReqParams = new long[len];
			for (int i = 0; i < len; i++) {
				newReqParams[i] = MathUtil.toLong(reqParams[i]);
			}
			return newReqParams;
		}
		return null;
	}

	public float getFloat(String reqParam) {
		return MathUtil.toFloat(request.getParameter(reqParam));
	}

	public float[] getFloats(String reqParam) {
		String[] reqParams = request.getParameterValues(reqParam);
		if (reqParams != null) {
			int len = reqParams.length;
			float[] newReqParams = new float[len];
			for (int i = 0; i < len; i++) {
				newReqParams[i] = MathUtil.toFloat(reqParams[i]);
			}
			return newReqParams;
		}
		return null;
	}

	public double getDouble(String reqParam) {
		return MathUtil.toDouble(request.getParameter(reqParam));
	}

	public double[] getDoubles(String reqParam) {
		String[] reqParams = request.getParameterValues(reqParam);
		if (reqParams != null) {
			int len = reqParams.length;
			double[] newReqParams = new double[len];
			for (int i = 0; i < len; i++) {
				newReqParams[i] = MathUtil.toDouble(reqParams[i]);
			}
			return newReqParams;
		}
		return null;
	}
}
