package com.teamface.custom.model.job.enums;

/**
 * 阻塞处理策略
 * 
 * @Title:
 * @Description:
 * @Author:CZL
 * @Since:2018年5月7日
 * @Version:1.1.0
 */

public enum BlockStrategyEnum
{
    SERIAL_EXECUTION("单机串行（默认）"),
    DISCARD_LATER("丢弃后续调度"),
    COVER_EARLY("覆盖之前调度");
    
    private String value;
    
    BlockStrategyEnum(String value)
    {
        this.value = value;
    }
    
    public String getValue()
    {
        return value;
    }
}
