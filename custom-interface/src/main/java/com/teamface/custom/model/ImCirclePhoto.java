package com.teamface.custom.model;

/**
 * 
 * @Title:
 * @Description:工作汇报的点赞列表Model类
 * @Author:dsl
 * @Since:2017年11月21日
 * @Version:1.1.0
 */
public class ImCirclePhoto extends BaseVo implements java.io.Serializable
{
    
    /** 类的版本号 */
    private static final long serialVersionUID = 2235327132207104L;
    
    /** 所关联的审批id */
    private Long circle_main_id;
    
    /** 审批附件的url */
    private String file_url;
    
    /** 上传时间 */
    private Long upload_time;
    
    /** 文件名称 */
    private String file_name;
    
    /** 文件大小 */
    private Long file_size;
    
    /** 文件后缀 */
    private String file_type;

    public Long getCircle_main_id()
    {
        
        return circle_main_id;
    }

    public void setCircle_main_id(Long circle_main_id)
    {
        
        this.circle_main_id = circle_main_id;
    }

    public String getFile_url()
    {
        
        return file_url;
    }

    public void setFile_url(String file_url)
    {
        
        this.file_url = file_url;
    }

    public Long getUpload_time()
    {
        
        return upload_time;
    }

    public void setUpload_time(Long upload_time)
    {
        
        this.upload_time = upload_time;
    }

    public String getFile_name()
    {
        
        return file_name;
    }

    public void setFile_name(String file_name)
    {
        
        this.file_name = file_name;
    }

    public Long getFile_size()
    {
        
        return file_size;
    }

    public void setFile_size(Long file_size)
    {
        
        this.file_size = file_size;
    }

    public String getFile_type()
    {
        
        return file_type;
    }

    public void setFile_type(String file_type)
    {
        
        this.file_type = file_type;
    }
    

    

}