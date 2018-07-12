package com.teamface.common.exception;

import com.teamface.common.model.Response;

/**
 * teamface项目Service层自定义的异常
 * 
 * @author Liu
 * @date 2016-11-23
 * @version 1.0
 */
public class ServiceException extends Exception
{
    
    private static final long serialVersionUID = -6518885832782981810L;
    
    // 错误信息
    private String errorMessage = null;
    
    // 概括错误信息
    private String errorTitle = null;
    
    // 错误类型，缺省为0
    private int errorType = 0;
    
    private Response response = null;
    
    private String errorCode = null;
    
    /**
     * ServiceException构造函数
     */
    public ServiceException()
    {
        super();
        errorMessage = "Service layer is Error!!";
    }
    
    /**
     * ServiceException构造函数根据传递的异常信息
     * 
     * @param argMessage
     */
    public ServiceException(String argMessage)
    {
        super(argMessage);
        errorMessage = argMessage;
    }
    
    /**
     * ServiceException构造函数根据传递的异常信息
     * 
     * @param argMessage
     */
    public ServiceException(int errorType, String errorTitle)
    {
        super(errorTitle);
        this.errorTitle = errorTitle;
        this.errorMessage = errorTitle;
        this.errorType = errorType;
    }
    
    /**
     * ServiceException构造函数根据传递的异常信息
     * 
     * @param argMessage
     */
    public ServiceException(String errorCode, String errorMsg)
    {
        super(errorMsg);
        this.errorCode = errorCode;
        this.errorMessage = errorMsg;
    }
    
    public ServiceException(Response response)
    {
        super(response.getDescribe());
        this.errorTitle = response.getDescribe();
        this.errorMessage = response.getDescribe();
        this.errorType = response.getCode();
        this.response = response;
    }
    
    /**
     * ServiceException构造函数根据传递的异常信息
     * 
     * @param argMessage
     */
    public ServiceException(int errorType, String errorTitle, String argMessage)
    {
        super(argMessage);
        this.errorMessage = argMessage;
        this.errorType = errorType;
        this.errorTitle = errorTitle;
    }
    
    /**
     * ServiceException构造函数根据传递的异常信息
     * 
     * @param argMessage
     * @param argThr
     */
    public ServiceException(String argMessage, Throwable argThr)
    {
        super(argMessage, argThr);
    }
    
    /**
     * ServiceException构造函数根据传递的异常信息
     * 
     * @param argMessage
     * @param argThr
     */
    public ServiceException(int errorType, String argMessage, Throwable argThr)
    {
        super(argMessage, argThr);
        this.errorType = errorType;
    }
    
    /**
     * ServiceException构造函数根据传递的异常信息
     * 
     * @param argMessage
     * @param argThr
     */
    public ServiceException(int errorType, String errorTitle, String argMessage, Throwable argThr)
    {
        super(argMessage, argThr);
        this.errorType = errorType;
        this.errorTitle = errorTitle;
    }
    
    /**
     * ServiceException构造函数通过传递异常对象
     * 
     * @param argThr
     */
    public ServiceException(Throwable argThr)
    {
        super(argThr);
        this.errorMessage = argThr.getLocalizedMessage();
    }
    
    public int getErrorType()
    {
        return errorType;
    }
    
    public String getErrorMessage()
    {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }
    
    public void setErrorType(int errorType)
    {
        this.errorType = errorType;
    }
    
    public String getErrorTitle()
    {
        return errorTitle;
    }
    
    public void setErrorTitle(String errorTitle)
    {
        this.errorTitle = errorTitle;
    }
    
    public Response getResponse()
    {
        return response;
    }
    
    public void setResponse(Response response)
    {
        this.response = response;
    }
    
    public String getErrorCode()
    {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode)
    {
        this.errorCode = errorCode;
    }
}