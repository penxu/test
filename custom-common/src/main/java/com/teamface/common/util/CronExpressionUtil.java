package com.teamface.common.util;

import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;

/**
 * Cron表达式
 * 
 * @Title:
 * @Description:
 * @Author:CZL
 * @Since:2018年5月22日
 * @Version:1.1.0
 */

public class CronExpressionUtil
{
    private static char blank = ' ';
    
    private static char star = '*';
    
    private static char question = '?';
    
    private static char slash = '/';
    
    public static final String CRON_TYPE_LAST_DAY = "CRON_TYPE_LAST_DAY";
    
    /**
     * 获取按年的cron表达式，周 和 天 只能2选一
     * 
     * @param minutes 分钟
     * @param hour 小时
     * @param day 天
     * @param week 周（星期） ，1代表周天，2代表周一 ..... 7代表周六，如果需要周一和周五表达为 2,6
     * @param month 月
     * @param frequency 年的频率
     * @return
     * @Description:
     */
    
    public static String getCronByYear(String minutes, String hour, String day, String week, String month, String frequency)
    {
        // * * * * * ? *
        StringBuilder sb = new StringBuilder();
        
        sb.append(0).append(blank); // 秒
        sb.append(minutes).append(blank); // 分钟
        sb.append(hour).append(blank); // 小时
        if (!StringUtils.isEmpty(day))
        {
            sb.append(day).append(blank);
        }
        else
        {
            if (!StringUtils.isEmpty(week))
            {
                sb.append(question).append(blank); // 天
            }
            else
            {
                sb.append(star).append(blank); // 天
            }
        }
        
        if (!StringUtils.isEmpty(month))
        {
            sb.append(month).append(blank); // 月
        }
        else
        {
            sb.append(star).append(blank);
        }
        
        if (!StringUtils.isEmpty(week))
        {
            sb.append(week).append(blank);
        }
        else
        {
            sb.append(question).append(blank);// 周
        }
        
        if (!"1".equals(frequency))
        {
            sb.append(star).append(slash).append(frequency);
        }
        else
        {
            sb.append(star); // 年
        }
        
        return sb.toString();
    }
    
    /**
     * 获取按月的cron表达式
     * 
     * @param minutes 分钟
     * @param hour 小时
     * @param day 天
     * @param frequency 月的频率
     * @return
     * @Description:
     */
    
    public static String getCronByMonth(String minutes, String hour, String day, String frequency)
    {
        // * * * * * ? *
        StringBuilder sb = new StringBuilder();
        
        sb.append(0).append(blank); // 秒
        sb.append(minutes).append(blank); // 分钟
        sb.append(hour).append(blank); // 小时
        
        String[] days = day.split(",");
        int last = days.length - 1;
        StringBuilder dayString = new StringBuilder();
        boolean lastDayFlag = false;
        String lastDay = null;
        StringBuilder dayTmpString = new StringBuilder();
        if (days.length == 1)
        {
            String d = days[0];
            if (checkLastDay(Integer.parseInt(d)))
            {
                dayString.append("L"); // 设置
                lastDay = d;
            }
            else
            {
                dayString.append(d);                
            }
        }
        else
        {
            for (int i = 0; i < days.length; i++)
            {
                String d = days[i];
                if (checkLastDay(Integer.parseInt(d)))
                {
                    dayString.append("28-31"); // 设置
                    lastDayFlag = true;
                    lastDay = d;
                }                
                else
                {
                    dayString.append(d);
                    if ("28".equals(d) || "29".equals(d) || "30".equals(d))
                    {
                        dayTmpString.append(d).append('-');
                    }
                }
                
                if (i < last)
                {
                    dayString.append(',');
                }
            }
        }
        
        sb.append(dayString.toString()).append(blank); // 天
        
        if ("1".equals(frequency))
        {
            sb.append(star).append(blank);
        }
        else
        {
            sb.append(star).append(slash).append(frequency).append(blank); // 月
        }
        
        sb.append(question).append(blank);// 周
        sb.append(star); // 年
        
        if (lastDayFlag)
        {
            sb.append(';').append(CRON_TYPE_LAST_DAY).append(',').append(lastDay);
            sb.append(',').append(dayTmpString.toString());
        }
        
        return sb.toString();
    }
    
    /**
     * 获取按周的cron表达式
     * 
     * @param minutes 分钟
     * @param hour 小时
     * @param week 周（星期） ，1代表周天，2代表周一 ..... 7代表周六，如果需要周一和周五表达为 2,6
     * @param frequency 周的频率
     * @return
     * @Description:
     */
    
    public static String getCronByWeek(String minutes, String hour, String week, String frequency)
    {
        // * * * * * ? *
        StringBuilder sb = new StringBuilder();
        
        sb.append(0).append(blank); // 秒
        sb.append(minutes).append(blank); // 分钟
        sb.append(hour).append(blank); // 小时
        sb.append(question).append(blank); // 天
        sb.append(star).append(blank); // 月
        sb.append(week).append(blank);// 周
        
        sb.append(star); // 年
        
        if (!"1".equals(frequency))
        {
            sb.append(";").append(frequency);
        }
        
        return sb.toString();
    }
    
    /**
     * 获取按天的cron表达式
     * 
     * @param minutes 分钟
     * @param hour 小时
     * @param frequency 天的频率
     * @return
     * @Description:
     */
    
    public static String getCronByDay(String minutes, String hour, String frequency)
    {
        // * * * * * ? *
        StringBuilder sb = new StringBuilder();
        
        sb.append(0).append(blank); // 秒
        sb.append(minutes).append(blank); // 分钟
        sb.append(hour).append(blank); // 小时
        if ("1".equals(frequency))
        {
            sb.append(star).append(blank);
        }
        else
        {
            sb.append(star).append(slash).append(frequency).append(blank); // 天
        }
        sb.append(star).append(blank); // 月
        sb.append(question).append(blank); // 周
        sb.append(star); // 年
        
        return sb.toString();
    }
    
    public static int getLastDayByCurrMonth()
    {
        Calendar calendar = Calendar.getInstance();
        
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        
        int lastday = calendar.get(Calendar.DATE);
        
        return lastday;
    }
    
    public static int getLastDayByMonth(int month)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        
        int lastday = calendar.get(Calendar.DATE);
        
        return lastday;
    }
    
    public static int getCurrDay()
    {
        Calendar calendar = Calendar.getInstance();
        
        int day = calendar.get(Calendar.DATE);
        
        return day;
    }
    
    public static boolean checkLastDayRange(int day)
    {
        if (day >= 28 && day <= 31)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public static boolean checkLastDay(int day)
    {
        int lastDay = getLastDayByCurrMonth();
        
        if (day == lastDay)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
