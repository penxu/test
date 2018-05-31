package com.teamface.custom.service.job;

import java.util.List;

import com.teamface.common.model.ServiceResult;
import com.teamface.custom.model.job.CJobInfo;

/**
 * CJob分布式任务调度接口
 * 
 * @Title:
 * @Description:
 * @Author:CZL
 * @Since:2018年5月7日
 * @Version:1.1.0
 */

public interface CJobService
{
    
    /**
     * 任务暂停状态
     */
    String JOB_PAUSED_STATUS = "PAUSED";
    
    /**
     * 执行任务运行类型，dateStop 指定日期停止
     */
    
    String JOB_RUN_OPER_TYPE_DATE_STOP = "dateStop";
    
    /**
     * 执行任务运行类型， numStop 指定次数停止
     */
    
    String JOB_RUN_OPER_TYPE_NUM_STOP = "numStop";
    
    /**
     * 添加通用任务，必须使用CJobInfo任务配置对象，任务会通过className、mothodName、paramType、args进行反射调用
     * 
     * @param cjobInfo 任务配置对象
     * @param className dubbo接口配置的名称(dubbo:reference)，通过spring读取dubbo配置名称得到应用对象
     * @param mothodName 调用的方法名称
     * @param paramType 调用方法的参数类型，如果是无参数方法传人null，对应args输入参数的类型，与args的个数对应，例如
     *            ：Class<?>[] paramType = new Class<?>[]{int.class,String.class}
     * @param args 调用方法的参数
     * @return 成功会返回 jobId
     * @Description:
     */
    ServiceResult<String> addCommonJob(CJobInfo cjobInfo, String className, String mothodName, Class<?>[] paramType, Object... args);
    
    /**
     * 添加简单任务，使用默认的任务配置对象，任务会通过className、mothodName、paramType、args进行反射调用
     * 
     * @param jobDesc 任务描述
     * @param author 负责人
     * @param alarmEmail 报警邮件，如果没有输入系统会使用默认的报警邮件
     * @param jobCron 任务执行CRON表达式 【base on quartz】
     * @param className dubbo接口配置的名称(dubbo:reference)，通过spring读取dubbo配置名称得到应用对象
     * @param mothodName 调用的方法名称
     * @param paramType 调用方法的参数类型，如果是无参数方法传人null，对应args输入参数的类型，与args的个数对应，例如
     *            ：Class<?>[] paramType = new Class<?>[]{int.class,String.class}
     * @param args 调用方法的参数
     * @return
     * @Description:
     */
    
    ServiceResult<String> addSimpleJob(String jobDesc, String author, String alarmEmail, String jobCron, String className, String mothodName, Class<?>[] paramType, Object... args);
    
    /**
     * 添加通用任务，必须使用CJobInfo任务配置对象，任务会通过className、mothodName、paramType、args进行反射调用
     * 
     * @param cjobInfo 任务配置对象
     * @param className dubbo接口配置的名称(dubbo:reference)，通过spring读取dubbo配置名称得到应用对象
     * @param mothodName 调用的方法名称
     * @param paramType 调用方法的参数类型，如果是无参数方法传人null，对应args输入参数的类型，与args的个数对应，例如
     *            ：Class<?>[] paramType = new Class<?>[]{int.class,String.class}
     * @param args 调用方法的参数
     * @return 成功会返回 jobId
     * @Description:
     */
    ServiceResult<String> addCommonJob(CJobInfo cjobInfo, String operType, String operCron, String className, String mothodName, Class<?>[] paramType, Object... args);
    
    /**
     * 添加简单任务，使用默认的任务配置对象，任务会通过className、mothodName、paramType、args进行反射调用
     * 
     * @param jobDesc 任务描述
     * @param author 负责人
     * @param alarmEmail 报警邮件，如果没有输入系统会使用默认的报警邮件
     * @param jobCron 任务执行CRON表达式 【base on quartz】
     * @param operType 执行任务运行类型，dateStop 指定日期停止 , numStop 指定次数停止
     * @param operCron 指定任务运行特殊调整，对应运行类型
     * @param className dubbo接口配置的名称(dubbo:reference)，通过spring读取dubbo配置名称得到应用对象
     * @param mothodName 调用的方法名称
     * @param paramType 调用方法的参数类型，如果是无参数方法传人null，对应args输入参数的类型，与args的个数对应，例如
     *            ：Class<?>[] paramType = new Class<?>[]{int.class,String.class}
     * @param args 调用方法的参数
     * @return
     * @Description:
     */
    
    ServiceResult<String> addSimpleJob(String jobDesc, String author, String alarmEmail, String jobCron, String operType, String operCron, String className, String mothodName,
        Class<?>[] paramType, Object... args);
    
    /**
     * 一次性任务，任务只会运行一次，运行后系统会删除该任务。任务会通过className、mothodName、paramType、args进行反射调用
     * 
     * @param jobDesc 任务描述
     * @param author 负责人
     * @param alarmEmail 报警邮件，如果没有输入系统会使用默认的报警邮件
     * @param className dubbo接口配置的名称(dubbo:reference)，通过spring读取dubbo配置名称得到应用对象
     * @param operType 执行任务运行类型，dateStop 指定日期停止 , numStop 指定次数停止
     * @param operCron 指定任务运行特殊调整，对应运行类型
     * @param mothodName 调用的方法名称
     * @param paramType 调用方法的参数类型，如果是无参数方法传人null，对应args输入参数的类型，与args的个数对应，例如
     *            ：Class<?>[] paramType = new Class<?>[]{int.class,String.class}
     * @param args 调用方法的参数
     * @return
     * @Description:
     */
    
    ServiceResult<String> triggerJob(String jobDesc, String author, String alarmEmail, String className, String mothodName, Class<?>[] paramType, Object... args);
    
    /**
     * 更新任务执行方式
     * 
     * @param jobId
     * @param jobCron
     * @param operType
     * @param operCron
     * @return
     * @Description:
     */
    
    ServiceResult<String> updateCronJob(int jobId, String jobCron, String operType, String operCron);
    
    /**
     * 删除任务
     * 
     * @param jobId 任务id
     * @return
     * @Description:
     */
    
    ServiceResult<String> removeJob(int jobId);
    
    /**
     * 暂停任务
     * 
     * @param jobId 任务id
     * @return
     * @Description:
     */
    
    ServiceResult<String> pauseJob(int jobId);
    
    /**
     * 获取任务状态
     * 
     * @param jobId
     * @return
     * @Description:
     */
    
    ServiceResult<String> getJobStatus(int jobId);
    
    /**
     * 清除失效暂停任务及清除该任务日志
     * 
     * @return
     * @Description:
     */
    
    ServiceResult<String> clearPauseJob();
    
    /**
     * 获取暂停任务
     * 
     * @return
     * @Description:
     */
    
    List<Object> getPauseJobList();
    
    /**
     * 获取该任务运行次数
     * 
     * @param jobId
     * @return
     * @Description:
     */
    
    int getJobRunCount(int jobId);
}