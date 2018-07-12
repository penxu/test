package com.teamface.email.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.util.StringUtil;

/**
 * @Description: 邮件接收规则处理类
 * @author: dsl
 * @date: 2018年4月10日 下午4:49:51
 * @version: 1.0
 */

public class RegulationOperationUtil
{
    /**
     * 
     * @param regulationJson
     * @param mail
     * @return
     * @Description:收件规则和逻辑条件处理
     */
    public static boolean processMail(JSONObject regulationJson, JSONObject mail)
    {
        if (null == regulationJson || StringUtils.isEmpty(regulationJson.getString("condition")))
        {
            return false;
        }
        JSONArray regulationArr = regulationJson.getJSONArray("condition");
        String seniorWhere = regulationJson.getString("high_where");
        List<Boolean> whereLS = new ArrayList<>();
        Boolean processStatus;
        boolean seniorResult = false;
        for (Object regulation : regulationArr)
        {
            JSONObject json = (JSONObject)regulation;
            processStatus = processSeniorConditon(json, mail);
            whereLS.add(processStatus);
        }
        if (StringUtil.isEmpty(seniorWhere) && whereLS.size() > 1)
        {
            boolean logicResult = true;
            for (Boolean b : whereLS)
            {
                logicResult = logicResult & b;
            }
            return logicResult;
        }
        else
        {
            seniorWhere = seniorWhere.toUpperCase().replace("AND", " AND ").replace("OR", " OR ");
            int index = 0;
            String r = "(\\d+)";
            Pattern p = Pattern.compile(r);
            Matcher m = p.matcher(seniorWhere);
            if (!StringUtils.isEmpty(seniorWhere))
            {
                while (m.find())
                {
                    int findNum = Integer.parseInt(m.group(1));
                    if (findNum > whereLS.size())
                    {
                        return whereLS.get(0);
                    }
                    Boolean newspace = whereLS.get(findNum - 1);
                    int index1 = seniorWhere.indexOf(String.valueOf(findNum));
                    String operator = seniorWhere.substring(index, index1);
                    if (operator.contains("AND"))
                    {
                        seniorResult = newspace & seniorResult;
                    }
                    if (operator.contains("OR"))
                    {
                        seniorResult = newspace | seniorResult;
                    }
                    if (!operator.contains("AND") || !operator.contains("OR"))
                    {
                        seniorResult = newspace;
                    }
                    index = index1 + 1;
                }
            }
            else
            {
                return whereLS.get(0);
            }
            
        }
        return seniorResult;
    }
    
    /**
     * 
     * @param regulation
     * @param mail
     * @return
     * @Description:单条规则处理
     */
    private static boolean processSeniorConditon(JSONObject regulation, JSONObject mail)
    {
        boolean resultStatus = false;
        String field = regulation.getString("field_value").split(":")[0];
        String operator = regulation.getString("operator_value").split(":")[0];
        String result = regulation.getString("result_value");
        if ("CONTAIN".equals(operator))
        {
            resultStatus = mail.getString(field).indexOf(result) >= 0 ? true : false;
        }
        if ("NCONTAIN".equals(operator))
        {
            resultStatus = mail.getString(field).indexOf(result) >= 0 ? false : true;
        }
        if ("GREATER".equals(operator))
        {
            resultStatus = Integer.valueOf(mail.getString(field)) > Integer.valueOf(result) ? true : false;
        }
        if ("LESS".equals(operator))
        {
            resultStatus = Integer.valueOf(mail.getString(field)) < Integer.valueOf(result) ? true : false;
        }
        if ("EQUALS".equals(operator))
        {
            resultStatus = Integer.valueOf(mail.getString(field)) == Integer.valueOf(result) ? true : false;
        }
        if ("GREATERE".equals(operator))
        {
            resultStatus = Integer.valueOf(mail.getString(field)) >= Integer.valueOf(result) ? true : false;
        }
        if ("LESSE".equals(operator))
        {
            resultStatus = Integer.valueOf(mail.getString(field)) <= Integer.valueOf(result) ? true : false;
        }
        return resultStatus;
    }
    
}
