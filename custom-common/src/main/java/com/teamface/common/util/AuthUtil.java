package com.teamface.common.util;

import java.util.Iterator;

import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * @Description:
 * @author: Administrator
 * @date: 2018年5月25日 下午2:16:06
 * @version: 1.0
 */

public class AuthUtil
{
    
    /**
     * 判断是否是管理员
     * 
     * @param shareArray
     * @param departmentIds
     * @param roleObj
     * @param employeeId
     * @param companyId
     * @return
     * @Description:
     */
    public static boolean isAdmin(JSONArray shareArray, String departmentIds, JSONObject roleObj, Long employeeId, Long companyId)
    {
        boolean isAdmin = false;
        int roleId = roleObj.getInteger("id");
        // 如果是系统管理员或者企业所有者 则直接返回
        if (roleId == 1 || roleId == 2)
        {
            return true;
        }
        Iterator<Object> iterator = shareArray.iterator();
        while (iterator.hasNext())
        {
            JSONObject obj = (JSONObject)iterator.next();
            String type = obj.getString("type");
            String id = obj.getString("id");
            // 0部门 1成员 2角色 3 动态成员 4 公司
            if ("0".equals(type))
            {
                if (departmentIds != null && (departmentIds.concat(",")).contains(id.concat(",")))
                {
                    isAdmin = true;
                    break;
                }
                
            }
            else if ("1".equals(type) && id.equals(employeeId.toString()))
            {
                isAdmin = true;
                break;
                
            }
            else if ("2".equals(type) && !StringUtils.isEmpty(roleObj) && roleObj.getString("id").equals(id))
            {
                isAdmin = true;
                break;
                
            }
            else if ("4".equals(type) && id.equals(companyId.toString()))
            {
                isAdmin = true;
                break;
            }
        }
        return isAdmin;
    }
}
