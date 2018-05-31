package com.teamface.im.util;

/**
 * @Description:
 * @author: dsl
 * @date: 2017年12月15日 上午9:11:40
 * @version: 1.0
 */

public class QuartzCronExpressionUtil
{
    
    private static char blankChar = ' ';
    
    private static char starChar = '*';
    
    private static char questionChar = '?';
    
    private static char slashChar = '/';
    
    /**
     * 
     * @param json
     * @return
     * @Description:{秒数} {分钟} {小时} {日期} {月份} {星期} {年份(可为空)}
     */
    public static String generatePlainCronExpression(Short minutes, Short hour, Short day, Short week, Short month)
    {
        StringBuilder cronExpression = new StringBuilder("");
        cronExpression.append('0').append(blankChar);
        if (null != minutes)
        {
            cronExpression.append(minutes).append(blankChar);
        }
        else
        {
            cronExpression.append(starChar).append(blankChar);
        }
        if (null != hour)
        {
            cronExpression.append(hour).append(blankChar);
        }
        else
        {
            cronExpression.append(starChar).append(blankChar);
        }
        if (null != day)
        {
            cronExpression.append(day).append(blankChar);
        }
        else if (null == day && null != week)
        {
            cronExpression.append(questionChar).append(blankChar);
        }
        else
        {
            cronExpression.append(starChar).append(blankChar);
        }
        if (null != month)
        {
            cronExpression.append(month).append(blankChar);
        }
        else
        {
            cronExpression.append(starChar).append(blankChar);
        }
        if (null != week)
        {
            cronExpression.append(week).append(blankChar);
        }
        else if (null != day && null == week)
        {
            cronExpression.append(questionChar).append(blankChar);
        }
        else if (null == day && null == week)
        {
            cronExpression.append(questionChar).append(blankChar);
        }
        else
        {
            cronExpression.append(starChar).append(blankChar);
        }
        cronExpression.append(starChar).append(blankChar);
        return cronExpression.toString();
    }
    
    /**
     * 
     * @param minutes
     * @param hour
     * @param day
     * @param frequency
     * @return
     * @Description:获取设置天的表达式
     */
    public static String getDayCronExpression(Short minutes, Short hour, Short frequency)
    {
        StringBuilder cronExpression = new StringBuilder("");
        cronExpression.append('0').append(blankChar);
        if (null != minutes)
        {
            cronExpression.append(minutes).append(blankChar);
        }
        else
        {
            cronExpression.append(starChar).append(blankChar);
        }
        if (null != hour)
        {
            cronExpression.append(hour).append(blankChar);
        }
        else
        {
            cronExpression.append(starChar).append(blankChar);
        }
        if (null != frequency)
        {
            cronExpression.append(starChar).append(slashChar).append(frequency).append(blankChar);
        }
        
        cronExpression.append(starChar).append(blankChar).append(questionChar).append(blankChar).append(starChar).append(blankChar);
        return cronExpression.toString();
    }
    
    /**
     * 
     * @param minutes
     * @param hour
     * @param day
     * @param month
     * @param frequency
     * @return
     * @Description:获取设置月的表达式
     */
    public static String getMonthCronExpression(Short minutes, Short hour, Short day, Short frequency)
    {
        StringBuilder cronExpression = new StringBuilder("");
        cronExpression.append('0').append(blankChar);
        if (null != minutes)
        {
            cronExpression.append(minutes).append(blankChar);
        }
        else
        {
            cronExpression.append(starChar).append(blankChar);
        }
        if (null != hour)
        {
            cronExpression.append(hour).append(blankChar);
        }
        else
        {
            cronExpression.append(starChar).append(blankChar);
        }
        if (null != day)
        {
            cronExpression.append(day).append(blankChar);
        }
        else
        {
            cronExpression.append(starChar).append(blankChar);
        }
        if (null != frequency)
        {
            cronExpression.append(starChar).append(slashChar).append(frequency).append(blankChar);
        }
        else
        {
            cronExpression.append(starChar).append(blankChar);
        }
        cronExpression.append(questionChar).append(blankChar).append(starChar).append(blankChar);
        return cronExpression.toString();
    }
    
    /**
     * 
     * @param minutes
     * @param hour
     * @param week
     * @param frequency
     * @return
     * @Description:获取设置周的cron表达式
     */
    public static String getWeekCronExpression(Short minutes, Short hour, Short week, Short frequency)
    {
        StringBuilder cronExpression = new StringBuilder("");
        cronExpression.append('0').append(blankChar);
        if (null != minutes)
        {
            cronExpression.append(minutes).append(blankChar);
        }
        else
        {
            cronExpression.append(starChar).append(blankChar);
        }
        if (null != hour)
        {
            cronExpression.append(hour).append(blankChar);
        }
        else
        {
            cronExpression.append(starChar).append(blankChar);
        }
        
        cronExpression.append(questionChar).append(blankChar).append(starChar).append(blankChar);
        
        if (null != week && null != frequency)
        {
            cronExpression.append(week).append(slashChar).append(frequency * 7).append(blankChar);
        }
        else
        {
            cronExpression.append(starChar).append(blankChar);
        }
        cronExpression.append(starChar).append(blankChar);
        return cronExpression.toString();
    }
    
    /**
     * 
     * @param minutes
     * @param hour
     * @param day
     * @param week
     * @param month
     * @param frequency
     * @return
     * @Description:获取年的cron表达式
     */
    public static String getYearCronExpression(Short minutes, Short hour, Short day, Short week, Short month, Short frequency)
    {
        StringBuilder cronExpression = new StringBuilder("");
        cronExpression.append('0').append(blankChar);
        if (null != minutes)
        {
            cronExpression.append(minutes).append(blankChar);
        }
        else
        {
            cronExpression.append(starChar).append(blankChar);
        }
        if (null != hour)
        {
            cronExpression.append(hour).append(blankChar);
        }
        else
        {
            cronExpression.append(starChar).append(blankChar);
        }
        if (null != day)
        {
            cronExpression.append(day).append(blankChar);
        }
        else
        {
            cronExpression.append(starChar).append(blankChar);
        }
        if (null != month)
        {
            cronExpression.append(month).append(blankChar);
        }
        else
        {
            cronExpression.append(starChar).append(blankChar);
        }
        
        cronExpression.append(questionChar).append(blankChar);
        
        cronExpression.append(starChar).append(slashChar).append(frequency).append(blankChar);
        return cronExpression.toString();
    }
    
}
