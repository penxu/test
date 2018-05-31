package com.teamface.common.util;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @Description:
 * @author: ZZH
 * @date: 2018年1月29日 上午11:01:08
 * @version: 1.0
 */
public class LogAspect
{
    Logger logger = Logger.getLogger(LogAspect.class);
    
    /**
     * 前置通知：在某连接点之前执行的通知，但这个通知不能阻止连接点前的执行
     * 
     * @param jp 连接点：程序执行过程中的某一行为
     */
    public void doBefore(JoinPoint jp)
    {
        StringBuilder logSB = new StringBuilder();
        logSB.append("start ! ").append(jp.getTarget().getClass().getName()).append(".").append(jp.getSignature().getName()).append("(");
        StringBuilder tmpSB = new StringBuilder();
        for (Object o : jp.getArgs())
        {
            if (tmpSB.length() > 0)
            {
                tmpSB.append(" , ");
            }
            tmpSB.append(o);
        }
        logSB.append(tmpSB).append(").");
        logger.warn(logSB.toString());
    }
    
    /**
     * 环绕通知：包围一个连接点的通知，可以在方法的调用前后完成自定义的行为，也可以选择不执行。
     * 类似web中SERVLET规范中Filter的doFilter方法。
     * 
     * @param pjp 当前进程中的连接点
     * @return
     * @throws Throwable 
     */
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable
    {
        StringBuilder logSB = new StringBuilder();
        StringBuilder tmpSB = new StringBuilder();
        long timeS = System.currentTimeMillis();
        Object result = pjp.proceed();
        long timeE = System.currentTimeMillis();
        logSB.append("times:").append((timeE-timeS)/1000).append(" , ").append(pjp.getTarget().getClass()).append(".").append(pjp.getSignature().getName()).append("().");
        logSB.append(tmpSB);
        logger.warn(logSB.toString());
        return result;
    }
    
    /**
     * 后置通知
     * 
     * @param jp
     */
    public void doAfter(JoinPoint jp)
    {
        StringBuilder logSB = new StringBuilder();
        logSB.append("end! ").append(jp.getTarget().getClass().getName()).append(".").append(jp.getSignature().getName()).append("().");
        logger.warn(logSB.toString());
    }
}
