package com.teamface.custom.model.job;

import java.io.Serializable;

import com.alibaba.fastjson.JSON;

/**
 * job任务存储参数对象
 * 
 * @Title:
 * @Description:
 * @Author:CZL
 * @Since:2018年5月7日
 * @Version:1.1.0
 */

public class JobSeriaParam implements Serializable
{
    
    /**
    *
    */
    private static final long serialVersionUID = -2074653633289725274L;
    
    private String className; // dubbo接口配置的名称(dubbo:reference)
    
    private String mothodName; // 调用的方法名称
    
    private Class<?>[] paramType; // 调用方法的参数类型，例如 ：Class<?>[] paramType = new
                                  // Class<?>[]{int.class,String.class}
    
    private Object[] args; // 调用方法的参数
    
    private int jobId;
    
    private String operType; // 操作类型- 指定日期、循环次数
    
    private String operCron; // 操作数据
    
    private String frequency; //频率
    
    public String getClassName()
    {
        return className;
    }
    
    public void setClassName(String className)
    {
        this.className = className;
    }
    
    public String getMothodName()
    {
        return mothodName;
    }
    
    public void setMothodName(String mothodName)
    {
        this.mothodName = mothodName;
    }
    
    public Class<?>[] getParamType()
    {
        return paramType;
    }
    
    public void setParamType(Class<?>[] paramType)
    {
        this.paramType = paramType;
    }
    
    public Object[] getArgs()
    {
        return args;
    }
    
    public void setArgs(Object[] args)
    {
        this.args = args;
    }
    
    public int getJobId()
    {
        return jobId;
    }
    
    public void setJobId(int jobId)
    {
        this.jobId = jobId;
    }
    
    public String getOperType()
    {
        return operType;
    }
    
    public void setOperType(String operType)
    {
        this.operType = operType;
    }

    public String getOperCron()
    {
        return operCron;
    }

    public void setOperCron(String operCron)
    {
        this.operCron = operCron;
    }

    public String getFrequency()
    {
        return frequency;
    }

    public void setFrequency(String frequency)
    {
        this.frequency = frequency;
    }

    public String toJson()
    {
        String json = JSON.toJSONString(this);
        return json;
    }
    
    public static JobSeriaParam toObj(String json)
    {
        JobSeriaParam obj = JSON.parseObject(json, JobSeriaParam.class);
        return obj;
    }
}
