package com.teamface.im.exception;

public class ImException extends Exception
{
    private static final long serialVersionUID = 6700697376100628474L;
    
    public ImException() {
        super();
    }
    
    public ImException(String msg) {
        super(msg);
    }
    
    public ImException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
    public ImException(Throwable cause) {
        super(cause);
    }
}
