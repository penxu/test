package com.teamface.email.util;

import java.util.Calendar;

public class MailCronExpressionUtil
{
    private static char blankChar = ' ';
    
    private static char starChar = '*';
    
    private static char questionChar = '?';
    
    /**
     * 
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minutes
     * @return
     * @Description:获取邮件cron表达式
     */
    public static String getMailCronExpression(Integer year, Integer month, Integer day, Integer hour, Integer minutes)
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
        if (null != year)
        {
            cronExpression.append(year).append(blankChar);
        }
        else
        {
            cronExpression.append(starChar).append(blankChar);
        }
        return cronExpression.toString();
    }
    
    public static String getMailCronExpressionByTime(long time)
    {
        Calendar timeDate = Calendar.getInstance();
        timeDate.setTimeInMillis(time);
        StringBuilder cronExpression = new StringBuilder("");
        cronExpression.append('0').append(blankChar);
        cronExpression.append(timeDate.get(Calendar.MINUTE)).append(blankChar);
        cronExpression.append(timeDate.get(Calendar.HOUR_OF_DAY)).append(blankChar);
        cronExpression.append(timeDate.get(Calendar.DAY_OF_MONTH)).append(blankChar);
        cronExpression.append(timeDate.get(Calendar.MONTH) + 1).append(blankChar);
        cronExpression.append(questionChar).append(blankChar);
        cronExpression.append(timeDate.get(Calendar.YEAR)).append(blankChar);
        return cronExpression.toString();
    }
}
