package com.teamface.common.util.sendMessageUtil;

import java.util.HashMap;

import com.teamface.common.constant.Constant;
import com.teamface.common.constant.DataTypes;

public class SendMessage {

	/**
	 * 发送短信
	 * 
	 * @author MaLingCong
	 * @param telphone
	 * @param inviteCode
	 * @param min
	 * @param type
	 * @param companyName
	 * @param employeeName
	 * @return
	 */
	public static boolean sendMessage(String telephone, Integer code, Integer min, Integer type, String companyName,
			String employeeName,String password,String customerNumber) {
		HashMap<String, Object> result = null;
		// 初始化SDK
		CCPRestSmsSDK restAPI = new CCPRestSmsSDK();
		restAPI.init(Constant.SERVERIP, Constant.SERVERPORT);
		restAPI.setAccount(Constant.ACCOUNTSID, Constant.ACCOUNTTOKEN);
		restAPI.setAppId(Constant.APPID);
		// 发送的模板类型
		String templateId = "";
		if (type == 0) {
			templateId = DataTypes.MESSAGE_TEMPLATE_USUAL_ID;
			result = restAPI.sendTemplateSMS(telephone.trim(), templateId, new String[] { code + "", min + "" });
		} else if (type == 1) {
			templateId = DataTypes.MESSAGE_TEMPLATE_REGISTER_ID;
			result = restAPI.sendTemplateSMS(telephone.trim(), templateId, new String[] { code + "", min + "" });
		} else if (type == 2) {
			templateId = DataTypes.MESSAGE_TEMPLATE_INVITE_ID;
			result = restAPI.sendTemplateSMS(telephone.trim(), templateId, new String[] {employeeName, telephone, code + ""});
		} else if (type == 3) {
			templateId = DataTypes.MESSAGE_TEMPLATE_UPDATEPASSWORD_ID;
			result = restAPI.sendTemplateSMS(telephone.trim(), templateId,
					new String[] {password, min + "" });
		}
		else if (type == 4) {
			templateId = DataTypes.MESSAGE_TEMPLATE_ACTIVE_ID;
			result = restAPI.sendTemplateSMS(telephone.trim(), templateId,
					new String[] { employeeName, telephone, password, min + "" });
		}else if (type == 5) {
			templateId = DataTypes.MESSAGE_TEMPLATE_APPROVE_ID;
			result = restAPI.sendTemplateSMS(telephone.trim(), templateId,
					new String[] { employeeName, password, customerNumber});
		}else if (type == 6) {
			templateId = DataTypes.MESSAGE_TEMPLATE_INVITATION_ID;
			result = restAPI.sendTemplateSMS(telephone.trim(), templateId,
					new String[] { employeeName,customerNumber});
		}

		 if (!"000000".equals(result.get("statusCode")))
			 return Boolean.FALSE;
		 
		return Boolean.TRUE;
	}
}