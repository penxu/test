package com.teamface.custom.service.quartz;

/**
 * @Description:
 * @author: dsl
 * @date: 2017年11月13日 下午6:41:35
 * @version: 1.0
 */

public interface PublicPoolJobService
{
    /**
     * 
     * @return
     * @Description:自动分配颜色
     */
    public boolean publicPoolRecycle(String token);
    
    /**
     *
     * @return
     * @Description: 邀请码
     */
    public boolean publicInvite();
    
    
    /**
     * 公司套餐结束定时器
     * @return
     * @Description:
     */
    public boolean publicCompanyEndTime();
    
    
    /**
     * 公司套餐结束定时器
     * @return
     * @Description:
     */
    public boolean publicCompanyStartTime();
}
