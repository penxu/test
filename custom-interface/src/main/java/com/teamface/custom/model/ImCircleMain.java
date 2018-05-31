package com.teamface.custom.model;

import java.util.List;

/**
 * 
 * @Title:
 * @Description:企业圈的主表Model类
 * @Author:dsl
 * @Since:2017年11月21日
 * @Version:1.1.0
 */
public class ImCircleMain extends BaseVo implements java.io.Serializable
{
    
    /** 类的版本号 */
    private static final long serialVersionUID = 2202783491885056L;
    
    /** 员工ID，谁发表的企业圈 */
    private Long fromId;
    
    /** 发表的地址 */
    private String address;
    
    /** 经度 */
    private String longitude;
    
    /** 维度 */
    private String latitude;
    
    /** 发表的内容 */
    private String info;
    
    /** 是否删除0表示未，1表示删除 */
    private Integer isDelete;
    
    /** 公司的ID */
    private Long companyId;
    
    /**
     * 冗余字段
     * 
     * @return
     */
    List<ImCirclePhoto> images;
    
    List<Long> peoples;
    
    public Long getCompanyId()
    {
        return companyId;
    }
    
    public void setCompanyId(Long companyId)
    {
        this.companyId = companyId;
    }
    
    public List<Long> getPeoples()
    {
        return peoples;
    }
    
    public void setPeoples(List<Long> peoples)
    {
        this.peoples = peoples;
    }
    
    public List<ImCirclePhoto> getImages()
    {
        return images;
    }
    
    public void setImages(List<ImCirclePhoto> images)
    {
        this.images = images;
    }
    
    public Long getFromId()
    {
        return this.fromId;
    }
    
    public void setFromId(Long fromId)
    {
        this.fromId = fromId;
    }
    
    public String getAddress()
    {
        return this.address;
    }
    
    public void setAddress(String address)
    {
        this.address = address;
    }
    
    public String getLongitude()
    {
        return this.longitude;
    }
    
    public void setLongitude(String longitude)
    {
        this.longitude = longitude;
    }
    
    public String getLatitude()
    {
        return this.latitude;
    }
    
    public void setLatitude(String latitude)
    {
        this.latitude = latitude;
    }
    
    public String getInfo()
    {
        return this.info;
    }
    
    public void setInfo(String info)
    {
        this.info = info;
    }
    
    public Integer getIsDelete()
    {
        return this.isDelete;
    }
    
    public void setIsDelete(Integer isDelete)
    {
        this.isDelete = isDelete;
    }
    
    @Override
    public String toString()
    {
        return "ImCircleMain [fromId=" + fromId + ", address=" + address
            + ", longitude=" + longitude + ", latitude=" + latitude + ", info="
            + info + ", isDelete=" + isDelete + ", companyId=" + companyId
            + ", images=" + images + ", peoples=" + peoples + "]";
    }
}