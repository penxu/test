package com.teamface.common.model;

import java.util.List;

/**
 * @Description:
 * @author: Administrator
 * @date: 2017年9月18日 下午2:30:02
 * @version: 1.0
 */
public class TreeNode
{
    private Long id;
    
    private String name;
    
    private String value;
    
    private Long parentId;
    
    private Long roleId;
    
    private List<TreeNode> childList;
    
    private List<TreeNode> users;
    
    private boolean checked = false;
    
    private String expandFlag;
    
    private int count;
    
    private String post_name;
    
    private Long departmentId;
    
    private String employee_name;
    
    private String picture;
    
    private String sign;
    
    private Long sign_id;
    
    private int company_count;
    
    public int getCompany_count()
    {
        return company_count;
    }
    
    public void setCompany_count(int company_count)
    {
        this.company_count = company_count;
    }
    
    public Long getSign_id()
    {
        
        return sign_id;
    }
    
    public void setSign_id(Long sign_id)
    {
        
        this.sign_id = sign_id;
    }
    
    public String getSign()
    {
        
        return sign;
    }
    
    public void setSign(String sign)
    {
        
        this.sign = sign;
    }
    
    public String getPicture()
    {
        
        return picture;
    }
    
    public void setPicture(String picture)
    {
        
        this.picture = picture;
    }
    
    public String getEmployee_name()
    {
        
        return employee_name;
    }
    
    public void setEmployee_name(String employee_name)
    {
        
        this.employee_name = employee_name;
    }
    
    public String getPost_name()
    {
        return post_name;
    }
    
    public void setPost_name(String post_name)
    {
        this.post_name = post_name;
    }
    
    public Long getDepartmentId()
    {
        return departmentId;
    }
    
    public void setDepartmentId(Long departmentId)
    {
        
        this.departmentId = departmentId;
    }
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getValue()
    {
        return value;
    }
    
    public void setValue(String value)
    {
        this.value = value;
    }
    
    public List<TreeNode> getChildList()
    {
        return childList;
    }
    
    public void setChildList(List<TreeNode> childList)
    {
        this.childList = childList;
    }
    
    public Long getId()
    {
        return id;
    }
    
    public void setId(Long id)
    {
        this.id = id;
    }
    
    public Long getParentId()
    {
        return parentId;
    }
    
    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
    }
    
    public int getCount()
    {
        
        return count;
    }
    
    public void setCount(int count)
    {
        
        this.count = count;
    }
    
    public List<TreeNode> getUsers()
    {
        return users;
    }
    
    public void setUsers(List<TreeNode> users)
    {
        this.users = users;
    }
    
    public boolean isChecked()
    {
        return checked;
    }
    
    public void setChecked(boolean checked)
    {
        this.checked = checked;
    }
    
    public String getExpandFlag()
    {
        return expandFlag;
    }
    
    public void setExpandFlag(String expandFlag)
    {
        this.expandFlag = expandFlag;
    }
    
    public Long getRoleId()
    {
        return roleId;
    }
    
    public void setRoleId(Long roleId)
    {
        this.roleId = roleId;
    }
}
