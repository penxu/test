package com.teamface.common.util.activiti;

import java.util.Date;

/**
 * @Description:
 * @author: ZZH
 * @date: 2018年1月8日 下午3:21:00
 * @version: 1.0
 */

public class TaskComment implements Comparable<TaskComment>
{
    public String id;
    
    public String userId;
    
    public String taskId;
    
    public String processInstanceId;
    
    public String event;
    
    public String message;
    
    public Date time;
    
    public TaskComment()
    {
        
    }
    
    public String getId()
    {
        
        return id;
    }
    
    public void setId(String id)
    {
        
        this.id = id;
    }
    
    public String getUserId()
    {
        
        return userId;
    }
    
    public void setUserId(String userId)
    {
        
        this.userId = userId;
    }
    
    public String getTaskId()
    {
        
        return taskId;
    }
    
    public void setTaskId(String taskId)
    {
        
        this.taskId = taskId;
    }
    
    public String getProcessInstanceId()
    {
        
        return processInstanceId;
    }
    
    public void setProcessInstanceId(String processInstanceId)
    {
        
        this.processInstanceId = processInstanceId;
    }
    
    public String getEvent()
    {
        
        return event;
    }
    
    public void setEvent(String event)
    {
        
        this.event = event;
    }
    
    public String getMessage()
    {
        
        return message;
    }
    
    public void setMessage(String message)
    {
        
        this.message = message;
    }
    
    public Date getTime()
    {
        
        return time;
    }
    
    public void setTime(Date time)
    {
        
        this.time = time;
    }
    
    @Override
    public int compareTo(TaskComment o)
    {
        if (this.time.before(o.time))
        {
            return 1;
        }
        else if (this.time.after(o.time))
        {
            return -1;
        }
        else
        {
            return 0;
        }
    }
}
