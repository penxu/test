package com.teamface.custom.model;

/**
 * 
 * @Title:
 * @Description:企业圈的点赞列表Model类
 * @Author:dsl
 * @Since:2017年11月21日
 * @Version:1.1.0
 */
public class ImCircleComment extends BaseVo implements java.io.Serializable
{
    
    /** 类的版本号 */
    private static final long serialVersionUID = 2203069788997632L;
    
    /** 评论者的ID */
    private Long senderId;
    
    /** 评论的内容 */
    private String contentInfo;
    
    /** 企业圈的ID */
    private Long circleMainId;
    
    /** 回复者的ID */
    private Long receiverId;
    
    public Long getSenderId()
    {
        return this.senderId;
    }
    
    public void setSenderId(Long senderId)
    {
        this.senderId = senderId;
    }
    
    public String getContentInfo()
    {
        return this.contentInfo;
    }
    
    public void setContentInfo(String contentInfo)
    {
        this.contentInfo = contentInfo;
    }
    
    public Long getCircleMainId()
    {
        return this.circleMainId;
    }
    
    public void setCircleMainId(Long circleMainId)
    {
        this.circleMainId = circleMainId;
    }
    
    public Long getReceiverId()
    {
        return this.receiverId;
    }
    
    public void setReceiverId(Long receiverId)
    {
        this.receiverId = receiverId;
    }
    
    @Override
    public String toString()
    {
        return "[" + "senderId:" + getSenderId() + "," + "contentInfo:" + getContentInfo() + "," + "circleMainId:" + getCircleMainId() + "," + "receiverId:" + getReceiverId() + ","
            + "]";
    }
}