package com.teamface.custom.model;

/**
 * 
 * @Title:
 * @Description:工作汇报的点赞列表Model类
 * @Author:dsl
 * @Since:2017年11月21日
 * @Version:1.1.0
 */
public class ImCircleUp extends BaseVo implements java.io.Serializable
{
    
    /** 类的版本号 */
    private static final long serialVersionUID = 2202798437091328L;
    
    /** 点赞人的ID */
    private Long employeeId;
    
    /** 企业圈的ID */
    private Long circleMainId;
    
    public Long getEmployeeId()
    {
        return this.employeeId;
    }
    
    public void setEmployeeId(Long employeeId)
    {
        this.employeeId = employeeId;
    }
    
    public Long getCircleMainId()
    {
        return this.circleMainId;
    }
    
    public void setCircleMainId(Long circleMainId)
    {
        this.circleMainId = circleMainId;
    }
    
    @Override
    public String toString()
    {
        return "[" + "employeeId:" + getEmployeeId() + "," + "circleMainId:" + getCircleMainId() + "," + "]";
    }
}