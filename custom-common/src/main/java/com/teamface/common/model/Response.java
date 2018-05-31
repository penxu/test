package com.teamface.common.model;

/**
 * 自定义错误返回类
 * 
 * @author Liu
 * @date 2016-11-22
 * @version 1.0
 */
public class Response implements java.io.Serializable
{
    private static final long serialVersionUID = 7922902048100814093L;
    
    private Integer code;
    
    private String describe;
    
    public Response(Integer code, String describe)
    {
        super();
        this.code = code;
        this.describe = describe;
    }
    
    public Integer getCode()
    {
        return code;
    }
    
    public void setCode(Integer code)
    {
        this.code = code;
    }
    
    public String getDescribe()
    {
        return describe;
    }
    
    public void setDescribe(String describe)
    {
        this.describe = describe;
    }
    
    @Override
    public String toString()
    {
        return "Response [code=" + code + ", describe=" + describe + "]";
    }
    
}
