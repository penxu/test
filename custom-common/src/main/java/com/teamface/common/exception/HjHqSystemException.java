package com.teamface.common.exception;

public class HjHqSystemException extends RuntimeException
{
    
    private static final long serialVersionUID = 19700101000000L;
    
    private int code = 0;// <10000就是正常的
                         // 2位模块代号+3位错误代号,如：10001用户crud[增加(Create)、查询(Retrieve)、更新(Update)和删除(Delete)]
    
    public int getCode()
    {
        return code;
    }
    
    public HjHqSystemException(int code, Throwable root)
    {
        super(root);
        this.code = code;
    }
    
    public HjHqSystemException(Throwable root)
    {
        super(root);
    }
    
    public HjHqSystemException(int code, String message, Throwable root)
    {
        super(message, root);
        this.code = code;
    }
    
    public HjHqSystemException(String message, Throwable root)
    {
        super(message, root);
    }
    
    public HjHqSystemException(int code, String message)
    {
        super(message);
    }
    
    public HjHqSystemException(String message)
    {
        super(message);
    }
    
    @Override
    public String toString()
    {
        return super.toString() + " [code=" + code + "]";
    }
}