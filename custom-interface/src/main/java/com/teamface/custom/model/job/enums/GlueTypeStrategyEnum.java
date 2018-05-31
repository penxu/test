package com.teamface.custom.model.job.enums;

/**
 * GLUE类型
 * 
 * @Title:
 * @Description:
 * @Author:CZL
 * @Since:2018年5月7日
 * @Version:1.1.0
 */

public enum GlueTypeStrategyEnum
{
    BEAN("任务以JobHandler方式维护在执行器端；需要结合 JobHandler属性匹配执行器中任务");//默认
    
    private String value;
    
    GlueTypeStrategyEnum(String value)
    {
        this.value = value;
    }
    
    public String getValue()
    {
        return value;
    }
}
