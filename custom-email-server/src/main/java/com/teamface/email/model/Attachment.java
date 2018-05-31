package com.teamface.email.model;

/**
 * @Description:
 * @author: dsl
 * @date: 2018年3月9日 上午10:50:02
 * @version: 1.0
 */

public class Attachment
{
    private String file_name;
    
    private String file_type;
    
    private String file_size;
    
    private String file_url;
    
    private String attachment_type;

    public String getFile_name()
    {
        
        return file_name;
    }

    public void setFile_name(String file_name)
    {
        
        this.file_name = file_name;
    }

    public String getFile_type()
    {
        
        return file_type;
    }

    public void setFile_type(String file_type)
    {
        
        this.file_type = file_type;
    }

    public String getFile_size()
    {
        
        return file_size;
    }

    public void setFile_size(String file_size)
    {
        
        this.file_size = file_size;
    }

    public String getFile_url()
    {
        
        return file_url;
    }

    public void setFile_url(String file_url)
    {
        
        this.file_url = file_url;
    }

    public String getAttachment_type()
    {
        return attachment_type;
    }

    public void setAttachment_type(String attachment_type)
    {
        this.attachment_type = attachment_type;
    }
    
    
    
}
