package com.teamface.common.model;

/**
 * @Description:
 * @author: Administrator
 * @date: 2017年10月11日 下午6:22:27
 * @version: 1.0
 */

public class InfoVo
{
    private Long companyId;
    
    private Long accountId;
    
    private Long employeeId;
    
    public InfoVo(Long accountId, Long employeeId, Long companyId)
    {
        super();
        this.companyId = companyId;
        this.accountId = accountId;
        this.employeeId = employeeId;
    }
    
    public Long getCompanyId()
    {
        
        return companyId;
    }
    
    public void setCompanyId(Long companyId)
    {
        
        this.companyId = companyId;
    }
    
    public Long getAccountId()
    {
        
        return accountId;
    }
    
    public void setAccountId(Long accountId)
    {
        
        this.accountId = accountId;
    }
    
    public Long getEmployeeId()
    {
        
        return employeeId;
    }
    
    public void setEmployeeId(Long employeeId)
    {
        
        this.employeeId = employeeId;
    }
}
