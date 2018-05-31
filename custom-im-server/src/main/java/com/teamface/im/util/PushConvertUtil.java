package com.teamface.im.util;


public class PushConvertUtil
{
    public static String getPushRabbitSetting(Long companyId, Integer pushType, Long id, String beanName)
    {
        StringBuilder parametersSB = new StringBuilder();
        parametersSB.append("{'company_id':'")
            .append(companyId)
            .append("','push_type':'")
            .append(pushType)
            .append("','id':'")
            .append(id)
            .append("','bean_name':'")
            .append(beanName)
            .append("'}");
        return parametersSB.toString();
    }
}
