package com.teamface.im.util;

/**
 * @Description:
 * @author: dsl
 * @date: 2017年12月15日 上午10:49:46
 * @version: 1.0
 */

public class PushJobUtil
{
    public static String getJobName(Short pushTypeId)
    {
        String jobName = "";
        switch (pushTypeId)
        {
            case (1):
                jobName = "PUSH_ADD_MESSAGE";
                break;
            case (2):
                jobName = "PUSH_SHARE_MESSAGE";
                break;
            case (3):
                jobName = "PUSH_REVERT_MESSAGE";
                break;
            case (4):
                jobName = "PUSH_TRANSFER_MESSAGE";
                break;
            case (5):
                jobName = "PUSH_DELETE_MESSAGE";
                break;
            case (6):
                jobName = "PUSH_EDIT_MESSAGE";
                break;
            case (7):
                jobName = "PUSH_COMMENT_MESSAGE";
                break;
            case (8):
                jobName = "PUSH_ALERT_MESSAGE";
                break;
        }
        return jobName;
    }
    
    public static String getJobTrigger(Short pushTypeId)
    {
        String triggerName = "";
        switch (pushTypeId)
        {
            case (1):
                triggerName = "PUSH_ADD_TRRIGER";
                break;
            case (2):
                triggerName = "PUSH_SHARE_TRRIGER";
                break;
            case (3):
                triggerName = "PUSH_REVERT_TRRIGER";
                break;
            case (4):
                triggerName = "PUSH_TRANSFER_TRRIGER";
                break;
            case (5):
                triggerName = "PUSH_DELETE_TRRIGER";
                break;
            case (6):
                triggerName = "PUSH_EDIT_TRRIGER";
                break;
            case (7):
                triggerName = "PUSH_COMMENT_TRRIGER";
                break;
            case (8):
                triggerName = "PUSH_ALERT_TRRIGER";
                break;
        }
        
        return triggerName;
    }
}
