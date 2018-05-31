package com.teamface.custom.model.job.enums;

/**
 * 失败处理策略
 * 
 * @Title:
 * @Description:
 * @Author:CZL
 * @Since:2018年5月7日
 * @Version:1.1.0
 */

public enum FailStrategyEnum
{
    FAIL_ALARM("失败告警"),
    
    FAIL_RETRY("失败重试(只重试一次)");
    
    private String value;
    
    FailStrategyEnum(String value)
    {
        this.value = value;
    }
    
    public String getValue()
    {
        return value;
    }
    
}
