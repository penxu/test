package com.teamface.custom.model;

import java.io.Serializable;

/**
 * 
 * @Title:
 * @Description:VO的父类
 * @Author:dsl
 * @Since:2017年11月21日
 * @Version:1.1.0
 */
public class BaseVo implements Serializable, Comparable<BaseVo>
{
    
    private static final long serialVersionUID = -4853932607203164421L;
    
    /** 主键ID */
    protected Long id;
    
    /** 创建时期 **/
    protected Long createDate;
    
    /** 记录是否有效 **/
    protected Integer disabled;
    
    public Long getId()
    {
        return id;
    }
    
    public void setId(Long id)
    {
        this.id = id;
    }
    
    public Long getCreateDate()
    {
        return createDate;
    }
    
    public void setCreateDate(Long createDate)
    {
        this.createDate = createDate;
    }
    
    public Integer getDisabled()
    {
        return disabled;
    }
    
    public void setDisabled(Integer disabled)
    {
        this.disabled = disabled;
    }
    
    @Override
    public String toString()
    {
        return "BaseVo [id=" + id + ", createDate=" + createDate + ", disabled="
            + disabled + "]";
    }
    
    @Override
    public int compareTo(BaseVo o)
    {// 按创建时间倒序排列
        // TODO Auto-generated method stub
        if (this == null || o == null)
            return -1;
        if (this.getCreateDate() == null && o.getCreateDate() == null)
            return 0;
        if (this.getCreateDate() == null)
            return 1;
        if (o.getCreateDate() == null)
            return -1;
        if (this.getCreateDate() < o.getCreateDate())
            return 1;
        if (this.getCreateDate() > o.getCreateDate())
            return -1;
        return 0;
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BaseVo other = (BaseVo)obj;
        if (id == null)
        {
            if (other.id != null)
                return false;
        }
        else if (id.longValue() != other.id.longValue())
            return false;
        return true;
    }
    
}
