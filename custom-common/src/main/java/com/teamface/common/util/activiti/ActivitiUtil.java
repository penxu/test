package com.teamface.common.util.activiti;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.BpmnAutoLayout;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.persistence.entity.CommentEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.ReadOnlyProcessDefinition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.Constant;
import com.teamface.common.constant.RedisKey4Function;
import com.teamface.common.util.StringUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JedisClusterHelper;

/**
 * @Description:Activiti工具类
 * @author: ZZH
 * @date: 2017年12月19日 上午11:20:36
 * @version: 1.0
 */

public class ActivitiUtil
{
    public static class AutoTask
    {
        public long assignee;
        
        public long nextAssignee;
        
        public String taskId;
        
        public String taskDefinitionKey;
        
        public String taskDefinitionName;
        
        public boolean auto;
        
        public AutoTask(String taskId, String taskDefinitionKey, String taskDefinitionName, long assignee, long nextAssignee, boolean auto)
        {
            this.taskId = taskId;
            this.taskDefinitionKey = taskDefinitionKey;
            this.taskDefinitionName = taskDefinitionName;
            this.assignee = assignee;
            this.nextAssignee = nextAssignee;
            this.auto = auto;
        }
    }
    
    static Logger log = Logger.getLogger(ActivitiUtil.class);
    
    private static final String MAX_LOOPCARDINALITY = "100";
    
    private static final String PROCESS_FILE_TYPE = ".bpmn";
    
    // 任务类型：同意
    private static final String TASK_CATEGORY_AGREE = "agree";
    
    // 任务类型：同意
    private static final String TASK_CATEGORY_SUBMIT = "submit";
    
    // 任务类型：拒绝、驳回
    private static final String TASK_CATEGORY_REFUSE = "refuse";
    
    // 变量-去重类型 0：不启用 ，1：多次出现去重，2：连续出现去重
    public static final String VAR_DISTINCTTYPE = "distinctType";
    
    // 变量-当前业务数据的主键
    public static final String VAR_DATA_ID = "dataId";
    
    // 去重，自动通过相关批注
    public static final String AUTO_MESSAGE = "自动通过";
    
    // 同一个流程在同一节点，若其中之一是在执行驳回操作则须等驳回操作完毕其它实例才能执行相关操作
    public static volatile List<String> processes = new ArrayList<>();
    
    private ActivitiUtil()
    {
        
    }
    
    /**
     * 
     * @param companyId 公司编号
     * @return String
     * @Description:获取流程表前缀
     */
    public static String getDatabaseTablePrefix(long companyId)
    {
        return new StringBuilder("A").append(companyId).append("_").toString();
    }
    
    /**
     * 
     * @param companyId 公司编号
     * @return ProcessEngine
     * @Description: 获取工作流引擎
     */
    public static ProcessEngine getProcessEngine(long companyId)
    {
        try
        {
            String databaseTablePrefix = companyId < 0 ? "" : getDatabaseTablePrefix(companyId);
            
            ActivitiServer server = ActivitiServer.getInstance(databaseTablePrefix);
            return server.getProcessEngine();
        }
        catch (Exception e)
        {
            log.error(e.toString(), e);
            return null;
        }
    }
    
    /**
     * 
     * @param id 流程编号
     * @param name 流程名称
     * @return Process
     * @Description: 获取流程对象
     */
    public static Process getProcess(String id, String name)
    {
        Process process = new Process();
        process.setId(id);
        process.setName(name);
        return process;
    }
    
    /**
     * 
     * @param from 源节点
     * @param to 目标节点
     * @param name 连线对象名称
     * @param conditionExpression 条件表达式
     * @return SequenceFlow
     * @Description:创建连线对象
     */
    public static SequenceFlow createSequenceFlow(String from, String to, String name, String conditionExpression)
    {
        SequenceFlow flow = new SequenceFlow();
        flow.setSourceRef(from);
        flow.setTargetRef(to);
        flow.setName(name);
        if (StringUtils.isNotEmpty(conditionExpression))
        {
            flow.setConditionExpression(conditionExpression);
        }
        return flow;
    }
    
    /**
     * 
     * @param from 源节点
     * @param to 目标节点
     * @param name 连线对象名称
     * @param sql 查询语句（若执行该语句有值则为true）
     * @return SequenceFlow
     * @Description:创建连线对象
     */
    public static SequenceFlow createSequenceFlow4Sql(String from, String to, String name, String sql)
    {
        SequenceFlow flow = new SequenceFlow();
        flow.setSourceRef(from);
        flow.setTargetRef(to);
        flow.setName(name);
        if (StringUtils.isNotEmpty(sql))
        {
            flow.setConditionExpression("${assigneeServer.executeConditionExpression('" + sql.replace("'", Constant.VAR_QUOTES) + "',dataId)}");
        }
        return flow;
    }
    
    /**
     * 
     * @param id 编号
     * @return ExclusiveGateway
     * @Description: 排他网关
     */
    public static ExclusiveGateway createExclusiveGateway(String id)
    {
        ExclusiveGateway exclusiveGateway = new ExclusiveGateway();
        exclusiveGateway.setId(id);
        return exclusiveGateway;
    }
    
    /**
     * 
     * @return StartEvent
     * @Description: 开始节点
     */
    public static StartEvent createStartEvent()
    {
        StartEvent startEvent = new StartEvent();
        startEvent.setId(Constant.PROCESS_FIELD_TASK_START);
        return startEvent;
    }
    
    /**
     * 
     * @return EndEvent
     * @Description: 结束节点
     */
    public static EndEvent createEndEvent()
    {
        EndEvent endEvent = new EndEvent();
        endEvent.setId(Constant.PROCESS_FIELD_TASK_END);
        return endEvent;
    }
    
    /**
     * 
     * @param id 任务节点编号
     * @param name 任务节点名称
     * @param assigneeListName 执行者集合名称（变量名）
     * @param isSequential 是否串行（执行者同时获取任务）
     * @param isSingleAssign 是否只需要单个执行者执行
     * @return UserTask
     * @Description:创建多实例任务节点
     */
    public static UserTask createUserTask4Multi(String id, String name, String assigneeListName, boolean isSequential, boolean isSingleAssign)
    {
        UserTask userTask = new UserTask();
        userTask.setName(name);
        userTask.setId(id);
        userTask.setAssignee("${assignee}");
        
        MultiInstanceLoopCharacteristics loopCharacteristics = new MultiInstanceLoopCharacteristics();
        loopCharacteristics.setInputDataItem(assigneeListName);
        loopCharacteristics.setElementVariable("assignee");
        loopCharacteristics.setSequential(isSequential);
        if (isSingleAssign)
        {
            loopCharacteristics.setCompletionCondition("${nrOfCompletedInstances == 1 }");
        }
        else
        {
            loopCharacteristics.setCompletionCondition("${nrOfInstances>0 && nrOfCompletedInstances/nrOfInstances == 1 }");
        }
        userTask.setLoopCharacteristics(loopCharacteristics);
        return userTask;
    }
    
    /**
     * 
     * @param id 任务节点编号
     * @param name 任务节点名称
     * @param assignees 执行者集合(以','隔开)
     * @param isSequential 是否并行（执行者同时获取任务）
     * @param isSingleAssign 是否只需要单个执行者执行
     * @return UserTask
     * @Description:创建多实例任务节点
     */
    public static UserTask createUserTask4Multier(String id, String name, String assignees, boolean isSequential, boolean isSingleAssign)
    {
        UserTask userTask = new UserTask();
        userTask.setName(name);
        userTask.setId(id);
        userTask.setAssignee("${assignee}");
        
        MultiInstanceLoopCharacteristics loopCharacteristics = new MultiInstanceLoopCharacteristics();
        loopCharacteristics.setInputDataItem("${assigneeServer.getMultipleUser('" + assignees + "')}");
        loopCharacteristics.setElementVariable("assignee");
        loopCharacteristics.setSequential(isSequential);
        if (isSingleAssign)
        {
            loopCharacteristics.setCompletionCondition("${nrOfCompletedInstances == 1 }");
        }
        else
        {
            loopCharacteristics.setCompletionCondition("${nrOfInstances>0 && nrOfCompletedInstances/nrOfInstances == 1 }");
        }
        userTask.setLoopCharacteristics(loopCharacteristics);
        return userTask;
    }
    
    /**
     * 
     * @param id 任务节点编号
     * @param name 任务节点名称
     * @param assigneeList 执行者集合
     * @param isSequential 是否串行（执行者同时获取任务）
     * @param isSingleAssign 是否只需要单个执行者执行
     * @return UserTask
     * @Description:创建多实例任务节点
     */
    public static UserTask createUserTask4CandidateUsers(String id, String name, List<String> assigneeList, boolean isSequential, boolean isSingleAssign)
    {
        UserTask userTask = new UserTask();
        userTask.setName(name);
        userTask.setId(id);
        userTask.setCandidateUsers(assigneeList);
        MultiInstanceLoopCharacteristics loopCharacteristics = new MultiInstanceLoopCharacteristics();
        loopCharacteristics.setLoopCardinality(String.valueOf(assigneeList.size()));
        loopCharacteristics.setSequential(isSequential);
        if (isSingleAssign)
        {
            loopCharacteristics.setCompletionCondition("${nrOfCompletedInstances == 1 }");
        }
        else
        {
            loopCharacteristics.setCompletionCondition("${nrOfInstances>0 && nrOfCompletedInstances/nrOfInstances == 1 }");
        }
        userTask.setLoopCharacteristics(loopCharacteristics);
        return userTask;
    }
    
    /**
     * 
     * @param id 任务节点编号
     * @param name 任务节点名称
     * @param assignee 执行人编号
     * @return UserTask
     * @Description:创建任务节点
     */
    public static UserTask createUserTask(String id, String name, String assignee)
    {
        UserTask userTask = new UserTask();
        userTask.setName(name);
        userTask.setId(id);
        userTask.setAssignee(assignee);
        
        return userTask;
    }
    
    /**
     * 
     * @param id 任务节点编号
     * @param name 任务节点名称
     * @param assignee 执行人编号
     * @return UserTask
     * @Description:创建发起人节点
     */
    public static UserTask createStarterTask(String id, String name)
    {
        UserTask userTask = new UserTask();
        userTask.setName(name);
        userTask.setId(id);
        userTask.setAssignee("${starter}");
        
        return userTask;
    }
    
    /**
     * 
     * @param id 任务节点编号
     * @param name 任务节点名称
     * @param level 部门级别
     * @param userUpper 若该审批人空缺，由其在通讯录中的上级部门负责人代审批
     * @return UserTask
     * @Description:创建任务节点(部门负责人-单级)
     */
    public static UserTask createUserTask4DeparmentPrincipal(String id, String name, int level, boolean userUpper)
    {
        UserTask userTask = new UserTask();
        userTask.setName(name);
        userTask.setId(id);
        userTask.setAssignee("${assigneeServer.getDeparmentPrincipal(companyId,starter," + level + "," + userUpper + ")}");
        
        return userTask;
    }
    
    /**
     * 
     * @param id 任务节点编号
     * @param name 任务节点名称
     * @param finalDeparmentId 终止审批部门
     * @return UserTask
     * @Description:创建任务节点(部门负责人-多级)
     */
    public static UserTask createUserTask4MulitDeparmentPrincipal(String id, String name, long finalDeparmentId)
    {
        return createUserTask4Multi(id, name, "${assigneeServer.getDeparmentPrincipals(companyId," + finalDeparmentId + ",starter)}", true, false);
    }
    
    /**
     * 
     * @param id 任务节点编号
     * @param name 任务节点名称
     * @param roleId 角色编号
     * @param isSequential 是否串行（执行者同时获取任务）
     * @param isSingleAssign 是否只需要单个执行者执行
     * @return UserTask
     * @Description:创建任务节点(指定角色)
     */
    public static UserTask createUserTask4Role(String id, String name, int roleId, boolean isSequential, boolean isSingleAssign)
    {
        return createUserTask4Multi(id, name, "${assigneeServer.getEmployees4Role(companyId," + roleId + ")}", isSequential, isSingleAssign);
    }
    
    /**
     * 
     * @param id 任务节点编号
     * @param name 任务节点名称
     * @return UserTask
     * @Description: 创建自由任务节点（适合无流程处理，当assigneeName的value为空时，任务结束）
     */
    public static UserTask createUserTask4Free(String id, String name)
    {
        
        MultiInstanceLoopCharacteristics loopCharacteristics = new MultiInstanceLoopCharacteristics();
        loopCharacteristics.setLoopCardinality(MAX_LOOPCARDINALITY);
        loopCharacteristics.setSequential(true);
        loopCharacteristics.setCompletionCondition("${nextAssignee==0}");
        
        UserTask userTask = new UserTask();
        userTask.setName(name);
        userTask.setId(id);
        userTask.setAssignee("${nextAssignee}");
        userTask.setLoopCharacteristics(loopCharacteristics);
        return userTask;
    }
    
    /**
     * 
     * @param id 任务节点编号
     * @param name 任务节点名称
     * @return UserTask
     * @Description: 创建任务节点（审批人根据上一节点传入）
     */
    public static UserTask createUserTask4Var(String id, String name)
    {
        UserTask userTask = new UserTask();
        userTask.setName(name);
        userTask.setId(id);
        userTask.setAssignee("${nextAssignee}");
        return userTask;
    }
    
    /**
     * 
     * @param companyId 公司编号
     * @param process 流程对象
     * @return Deployment
     * @Description:部署流程
     */
    public static Deployment deployProcess(long companyId, Process process)
    {
        try
        {
            String processId = process.getId();
            BpmnModel model = new BpmnModel();
            model.addProcess(process);
            new BpmnAutoLayout(model).execute();
            ProcessEngine engine = getProcessEngine(companyId);
            if (engine != null)
            {
                return engine.getRepositoryService().createDeployment().addBpmnModel(processId + PROCESS_FILE_TYPE, model).name(processId + "_deployment").deploy();
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        
        return null;
    }
    
    /**
     * 
     * @param companyId 公司编号
     * @param processId 流程id
     * @param employeeId 发起人
     * @param processVars 流程变量
     * @param filePath 流程对象存储路径
     * @param event 事件（流程状态）
     * @param message 批注
     * @param nextAssignee 下一个节点审批,-1:不设，0:自由流程结束，>0:设一个节点审批人
     * @return Map {firstTaskId,processInstanceId,processDefinitionId}
     * @Description: 启动流程，获取流程实例编号
     */
    public static Map<String, String> startProcess(long companyId, String processId, long employeeId, Map<String, Object> processVars, String filePath, String event,
        String message, long nextAssignee)
    {
        log.info(String.format("parameters{%s,%s,%s,%s,%s,%s,%s} start!", companyId, processId, employeeId, processVars, filePath, event, message));
        Map<String, String> resultMap = new HashMap<>();
        try
        {
            ProcessEngine engine = getProcessEngine(companyId);
            if (engine != null)
            {
                if (processVars == null)
                {
                    processVars = new HashMap<>();
                }
                processVars.put("companyId", String.valueOf(companyId));
                processVars.put("starter", String.valueOf(employeeId));
                processVars.put("currentOperator", String.valueOf(employeeId));
                ProcessInstance processInstance = engine.getRuntimeService().startProcessInstanceByKey(processId, processVars);
                String processDefinitionId = processInstance.getProcessDefinitionId();
                if (!StringUtil.isEmpty(filePath))
                {
                    ProcessDefinition processDefinition = engine.getRepositoryService().getProcessDefinition(processDefinitionId);
                    String deploymentId = processDefinition.getDeploymentId();
                    
                    InputStream processDiagram = engine.getRepositoryService().getProcessDiagram(processDefinitionId);
                    FileUtils.copyInputStreamToFile(processDiagram, new File(filePath + processId + ".png"));
                    
                    InputStream processBpmn = engine.getRepositoryService().getResourceAsStream(deploymentId, processId + PROCESS_FILE_TYPE);
                    FileUtils.copyInputStreamToFile(processBpmn, new File(filePath + processId + PROCESS_FILE_TYPE));
                }
                String processInstanceId = processInstance.getId();
                List<Task> tasks = getTasks(companyId, processInstanceId);
                if (!tasks.isEmpty())
                {
                    Task task = tasks.get(0);
                    String taskId = task.getId();
                    String taskKey = task.getTaskDefinitionKey();
                    resultMap.put("firstTaskId", taskId);
                    TaskService taskService = engine.getTaskService();
                    taskService.setVariable(taskId, "firstTaskId", taskId);
                    taskService.setVariable(taskId, "currentTaskId", taskId);
                    taskService.setVariable(taskId, "currentTaskKey", taskKey);
                    AutoTask autoComplete = completeTask(companyId, processInstanceId, taskId, taskKey, event, message, employeeId, nextAssignee, TASK_CATEGORY_SUBMIT);
                    if (!StringUtil.isEmpty(autoComplete.taskDefinitionKey) && autoComplete.taskDefinitionKey.equals("noOutgoing"))
                    {
                        resultMap.put("noOutgoing", "1");
                        return resultMap;
                    }
                }
                resultMap.put("processInstanceId", processInstanceId);
                resultMap.put("processDefinitionId", processDefinitionId);
                return resultMap;
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        
        return resultMap;
    }
    
    /**
     * 
     * @param companyId 公司编号
     * @param processId 流程id
     * @param employeeId 发起人
     * @param processVars 流程变量
     * @param event 事件（流程状态）
     * @param message 批注
     * @param nextAssignee 下一个节点审批,-1:不设，0:自由流程结束，>0:设一个节点审批人
     * @return @return Map {firstTaskId,processInstanceId,processDefinitionId}
     * @Description: 启动流程，获取流程实例编号
     */
    public static Map<String, String> startProcess(long companyId, String processId, long employeeId, Map<String, Object> processVars, String event, String message,
        long nextAssignee)
    {
        return startProcess(companyId, processId, employeeId, processVars, null, event, message, nextAssignee);
    }
    
    /**
     * 
     * @param companyId 公司编号
     * @param processInstanceId 流程实例编号
     * @return List<TaskComment>
     * @Description:获取流程所有批注信息
     */
    public static List<TaskComment> getTaskComments(long companyId, String processInstanceId)
    {
        log.info(String.format("parameters{%s,%s} start!", companyId, processInstanceId));
        List<TaskComment> taskComments = new ArrayList<>();
        try
        {
            ProcessEngine engine = getProcessEngine(companyId);
            if (engine != null)
            {
                Map<String, TaskComment> taskCommentMap = new HashMap<>();
                TaskService taskService = engine.getTaskService();
                HistoryService historyService = engine.getHistoryService();
                HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
                List<Comment> comments = taskService.getProcessInstanceComments(processInstance.getId());
                for (Comment comment : comments)
                {
                    String id = comment.getId();
                    TaskComment taskComment = taskCommentMap.get(id);
                    if (taskComment == null)
                    {
                        taskComment = new TaskComment();
                        taskComment.setId(comment.getId());
                        taskComment.setProcessInstanceId(comment.getProcessInstanceId());
                        taskComment.setTaskId(comment.getTaskId());
                        taskComment.setTime(comment.getTime());
                        
                        taskCommentMap.put(id, taskComment);
                    }
                    if (CommentEntity.TYPE_EVENT.equals(comment.getType()))
                    {
                        taskComment.setEvent(comment.getFullMessage());
                    }
                    else
                    {
                        taskComment.setMessage(comment.getFullMessage());
                    }
                    taskComments.add(taskComment);
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return taskComments;
    }
    
    /**
     * 
     * @param companyId 公司编号
     * @param processInstanceId 流程实例编号
     * @return List<HistoricTaskInstance>
     * @Description:获取当前流程已完成的任务集合
     */
    public static List<HistoricTaskInstance> getHistoricTasks(long companyId, String processInstanceId)
    {
        log.info(String.format("parameters{%s,%s} start!", companyId, processInstanceId));
        List<HistoricTaskInstance> resultLS = new ArrayList<>();
        try
        {
            ProcessEngine engine = getProcessEngine(companyId);
            if (engine != null)
            {
                HistoryService historyService = engine.getHistoryService();
                resultLS = historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstanceId).finished().orderByTaskCreateTime().asc().list();
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return resultLS;
    }
    
    /**
     * 
     * @param companyId 公司编号
     * @param processInstanceId 流程实例编号
     * @param assignee 执行者编号
     * @return List<HistoricTaskInstance>
     * @Description:获取执行者在当前流程已完成的任务集合
     */
    public static List<HistoricTaskInstance> getHistoricTasks4Assignee(long companyId, String processInstanceId, String assignee)
    {
        log.info(String.format("parameters{%s,%s,%s} start!", companyId, processInstanceId, assignee));
        List<HistoricTaskInstance> resultLS = new ArrayList<>();
        try
        {
            ProcessEngine engine = getProcessEngine(companyId);
            if (engine != null)
            {
                HistoryService historyService = engine.getHistoryService();
                resultLS = historyService.createHistoricTaskInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .taskAssignee(assignee)
                    .taskCategory(TASK_CATEGORY_AGREE)
                    .finished()
                    .orderByTaskCreateTime()
                    .asc()
                    .list();
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        
        return resultLS;
    }
    
    /**
     * 
     * @param companyId 公司编号
     * @param taskId 当前任务节点编号
     * @param employeeId 当前审批人
     * @param event 审批事件（状态）
     * @param message 批注
     * @Description:自由流程（撤回、驳回结束）
     */
    public static void refuseEnd4Free(long companyId, String taskId, long employeeId, String event, String message)
    {
        log.info(String.format("parameters{%s,%s,%s,%s,%s} start!", companyId, taskId, employeeId, event, message));
        try
        {
            ProcessEngine engine = getProcessEngine(companyId);
            if (engine != null)
            {
                TaskService taskService = engine.getTaskService();
                Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
                task.setCategory(TASK_CATEGORY_REFUSE);
                taskService.setVariable(taskId, "nextAssignee", "0");
                taskService.complete(taskId);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
    /**
     * 
     * @param companyId 公司编号
     * @param taskId 当前任务节点编号
     * @param employeeId 当前审批人
     * @param event 审批事件（状态）
     * @param message 批注
     * @Description:挂起流程（撤回、驳回结束）
     */
    public static void suspendProcess(long companyId, String taskId, long employeeId, String event, String message)
    {
        log.info(String.format("parameters{%s,%s,%s,%s,%s} start!", companyId, taskId, employeeId, event, message));
        try
        {
            ProcessEngine engine = getProcessEngine(companyId);
            if (engine != null)
            {
                TaskService taskService = engine.getTaskService();
                Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
                String processInstanceId = task.getProcessInstanceId();
                task.setCategory(TASK_CATEGORY_REFUSE);
                taskService.complete(taskId);
                engine.getRuntimeService().suspendProcessInstanceById(processInstanceId);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
    public static void suspendProcess(long companyId, String processInstanceId)
    {
        log.info(String.format("parameters{%s,%s} start!", companyId, processInstanceId));
        try
        {
            ProcessEngine engine = getProcessEngine(companyId);
            if (engine != null)
            {
                engine.getRuntimeService().suspendProcessInstanceById(processInstanceId);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
    /**
     * 
     * @param companyId 公司编号
     * @param taskId 当前任务节点编号
     * @param employeeId 转办人
     * @param currentEmployeeId 当前审批人
     * @param event 审批事件（状态）
     * @param message 批注
     * @Description:转办任务
     */
    public static void turnTask(long companyId, String taskId, long employeeId, long currentEmployeeId, String event, String message)
    {
        log.info(String.format("parameters{%s,%s} start!", companyId, taskId, employeeId));
        try
        {
            ProcessEngine engine = getProcessEngine(companyId);
            if (engine != null)
            {
                TaskService taskService = engine.getTaskService();
                taskService.setAssignee(taskId, String.valueOf(employeeId));
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
    /**
     * 
     * @param engine 流程引擎，如果没有可以设为null
     * @param companyId 公司编号
     * @param processInstanceId 流程实例编号
     * @param taskId 任务编号
     * @param taskDefinitionKey 任务key
     * @param event 事件（流程状态）
     * @param message 批注
     * @param employeeId 当前执行者编号
     * @param nextAssignee 下一个节点审批,-1:不设，0:自由流程结束，>0:设一个节点审批人
     * @Description:完成任务,若有自动审批任务则返回待自动审批的任务相关信息,没有则返回null
     */
    public static AutoTask completeTask(long companyId, String processInstanceId, String taskId, String taskDefinitionKey, String event, String message, long employeeId,
        long nextAssignee)
    {
        return completeTask(companyId, processInstanceId, taskId, taskDefinitionKey, event, message, employeeId, nextAssignee, null);
    }
    
    /**
     * 
     * @param engine 流程引擎，如果没有可以设为null
     * @param companyId 公司编号
     * @param processInstanceId 流程实例编号
     * @param taskId 任务编号
     * @param taskDefinitionKey 任务key
     * @param event 事件（流程状态）
     * @param message 批注
     * @param employeeId 当前执行者编号
     * @param nextAssignee 下一个节点审批,-1:不设，0:自由流程结束，>0:设一个节点审批人
     * @param auto 是否是自动审批
     * @Description:完成任务,若有自动审批任务则返回待自动审批的任务相关信息,没有则返回null
     */
    private static AutoTask completeTask(long companyId, String processInstanceId, String taskId, String taskDefinitionKey, String event, String message, long employeeId,
        long nextAssignee, String category)
    {
        if (employeeId <= 0 || StringUtils.isEmpty(taskId) || StringUtils.isEmpty(processInstanceId) || StringUtils.isEmpty(taskDefinitionKey))
        {
            return null;
        }
        try
        {
            ProcessEngine engine = getProcessEngine(companyId);
            if (engine != null)
            {
                TaskService taskService = engine.getTaskService();
                // 当前节点
                Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
                String taskKey = task.getTaskDefinitionKey();
                String processTaskFlag = task.getProcessDefinitionId() + "_" + task.getTaskDefinitionKey();
                while (processes.contains(processTaskFlag))
                {
                    Thread.sleep(1000);
                }
                String theCategory = StringUtils.isEmpty(category) ? TASK_CATEGORY_AGREE : category;
                task.setCategory(theCategory);
                taskService.saveTask(task);
                Object lastTaskId = taskService.getVariable(taskId, "currentTaskId");
                Object lastTaskKey = taskService.getVariable(taskId, "currentTaskKey");
                if (lastTaskKey == null || !taskKey.equals(lastTaskKey.toString()))
                {
                    taskService.setVariable(taskId, "lastTaskId", lastTaskId);
                    taskService.setVariable(taskId, "lastTaskKey", lastTaskKey);
                }
                taskService.setVariable(taskId, "currentTaskId", taskId);
                taskService.setVariable(taskId, "currentTaskKey", taskKey);
                taskService.setVariable(taskId, "currentOperator", String.valueOf(employeeId));
                
                if (nextAssignee >= 0)
                {
                    taskService.setVariable(taskId, "nextAssignee", String.valueOf(nextAssignee));
                }
                String nextTaskId = null;
                String nextTaskKey = null;
                String nextTaskName = null;
                String assignee = null;
                long nextNextAssignee = nextAssignee;
                boolean distinctFlag = false;
                Object distinctType = taskService.getVariable(taskId, VAR_DISTINCTTYPE);
                try
                {
                    taskService.complete(taskId);
                }
                catch (Exception e)
                {
                    if (e.getMessage().startsWith("No outgoing sequence flow of the exclusive gateway "))
                    {
                        return new AutoTask(null, "noOutgoing", null, 0, 0, false);
                    }
                }
                String redisKey = companyId + "_" + processInstanceId + "_" + RedisKey4Function.PROCESS_AGREE_USERS.getIndex();
                JedisClusterHelper.sadd(redisKey, theCategory + employeeId);
                // 判断是否要处理下一节点
                if (distinctType != null && (Integer)distinctType != 0)
                {
                    int type = Integer.parseInt(distinctType.toString());
                    if (type > 0)
                    {
                        List<Task> tasks = getTasks(companyId, processInstanceId);
                        if (!tasks.isEmpty())
                        {
                            Task nextTask = tasks.get(0);
                            if (nextTask != null)
                            {
                                nextTaskId = nextTask.getId();
                                nextTaskKey = nextTask.getTaskDefinitionKey();
                                nextTaskName = nextTask.getName();
                                assignee = nextTask.getAssignee();
                                if (nextAssignee > 0)
                                {
                                    nextNextAssignee = -1;
                                    assignee = String.valueOf(nextAssignee);
                                }
                                if ((nextAssignee > 0 && nextAssignee == employeeId) || String.valueOf(employeeId).equals(assignee))
                                {
                                    distinctFlag = true;
                                }
                                if (!distinctFlag && type == 1)
                                {
                                    if (JedisClusterHelper.exists(redisKey))
                                    {
                                        distinctFlag = JedisClusterHelper.sismember(redisKey, TASK_CATEGORY_AGREE + assignee);
                                    }
                                    else
                                    {
                                        distinctFlag = !getHistoricTasks4Assignee(companyId, processInstanceId, assignee).isEmpty();
                                    }
                                }
                            }
                        }
                    }
                }
                else
                {
                    assignee = String.valueOf(employeeId);
                }
                return new AutoTask(nextTaskId, nextTaskKey, nextTaskName, Long.parseLong(assignee), nextNextAssignee, (distinctFlag && nextTaskId != null));
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * 流程结束后清除缓存
     * 
     * @param companyId
     * @param processInstanceId
     * @Description:
     */
    public static void clearCache(long companyId, String processInstanceId)
    {
        JedisClusterHelper.del(companyId + "_" + processInstanceId + "_" + RedisKey4Function.PROCESS_AGREE_USERS.getIndex());
    }
    
    /**
     * 
     * @param companyId 公司编号
     * @param currentTaskId 当前任务编号
     * @return int
     * @Description:当前节点还有几个任务未处理
     */
    public static int notCompletedInstancesCount(long companyId, String currentTaskId)
    {
        try
        {
            ProcessEngine engine = getProcessEngine(companyId);
            if (engine != null)
            {
                TaskService taskService = engine.getTaskService();
                Task currentTask = taskService.createTaskQuery().taskId(currentTaskId).singleResult();
                Object nrOfCompletedInstances = taskService.getVariable(currentTask.getId(), "nrOfCompletedInstances");
                Object nrOfInstances = taskService.getVariable(currentTask.getId(), "nrOfInstances");
                if (nrOfCompletedInstances == null || nrOfInstances == null)
                {
                    return 1;
                }
                else
                {
                    return (int)nrOfInstances - (int)nrOfCompletedInstances;
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return 1;
    }
    
    /**
     * 
     * @param companyId 公司编号
     * @param employeeId 当前审批人
     * @param currentTaskId 当前任务编号
     * @param desTaskKey 目标任务key,无则传null
     * @param event 审批事件（状态）
     * @param message 批注
     * @param type 跳转/驳回类型，0：上一节点，1：发起人节点,3:驳回结束
     * @Description:
     */
    public static void jumpTask(long companyId, long employeeId, String currentTaskId, String desTaskKey, String event, String message, int type)
    {
        try
        {
            ProcessEngine engine = getProcessEngine(companyId);
            if (engine != null)
            {
                HistoryService historyService = engine.getHistoryService();
                RepositoryService repositoryService = engine.getRepositoryService();
                TaskService taskService = engine.getTaskService();
                Task currentTask = taskService.createTaskQuery().taskId(currentTaskId).singleResult();
                currentTask.setCategory(TASK_CATEGORY_REFUSE);
                String processDefinitionId = currentTask.getProcessDefinitionId();
                ReadOnlyProcessDefinition processDefinitionEntity = (ReadOnlyProcessDefinition)repositoryService.getProcessDefinition(currentTask.getProcessDefinitionId());
                String desTaskId = null;
                
                String taskId = currentTask.getId();
                String taskKey = currentTask.getTaskDefinitionKey();
                Object lastTaskId = taskService.getVariable(taskId, "currentTaskId");
                Object lastTaskKey = taskService.getVariable(taskId, "currentTaskKey");
                if (lastTaskKey == null || !taskKey.equals(lastTaskKey.toString()))
                {
                    taskService.setVariable(taskId, "lastTaskId", lastTaskId);
                    taskService.setVariable(taskId, "lastTaskKey", lastTaskKey);
                }
                taskService.setVariable(taskId, "currentTaskId", taskId);
                taskService.setVariable(taskId, "currentTaskKey", taskKey);
                taskService.setVariable(taskId, "currentOperator", String.valueOf(employeeId));
                
                // 目标节点
                // 上一节点
                if (type == 0)
                {
                    if (lastTaskKey != null && taskKey.equals(lastTaskKey.toString()))
                    {
                        lastTaskId = taskService.getVariable(currentTaskId, "lastTaskId");
                    }
                    desTaskId = lastTaskId.toString();
                }
                // 发起人节点
                else if (type == 1)
                {
                    desTaskId = taskService.getVariable(currentTaskId, "firstTaskId").toString();
                }
                else if (type == 3)
                {
                    desTaskKey = Constant.PROCESS_FIELD_TASK_END;
                }
                if (desTaskId != null && desTaskKey == null)
                {
                    HistoricTaskInstance historyTask = historyService.createHistoricTaskInstanceQuery().taskId(desTaskId).singleResult();
                    desTaskKey = historyTask.getTaskDefinitionKey();
                }
                
                // 目标节点
                ActivityImpl destinationActivity = (ActivityImpl)processDefinitionEntity.findActivity(desTaskKey);
                
                // 当前节点
                ActivityImpl currentActivity = (ActivityImpl)processDefinitionEntity.findActivity(currentTask.getTaskDefinitionKey());
                String processTaskFlag = processDefinitionId + "_" + currentTask.getTaskDefinitionKey();
                processes.add(processTaskFlag);
                jumpTask(currentTask.getProcessInstanceId(), taskService, currentTaskId, currentActivity, destinationActivity);
                processes.remove(processTaskFlag);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
    private static void jumpTask(String processInstanceId, TaskService taskService, String currentTaskId, ActivityImpl currentActivity, ActivityImpl destinationActivity)
    {
        String taskKey = currentActivity.getId();
        // 清空当前流向
        List<PvmTransition> oriPvmTransitionList = new ArrayList<>();
        List<PvmTransition> pvmTransitionList = currentActivity.getOutgoingTransitions();
        for (PvmTransition pvmTransition : pvmTransitionList)
        {
            oriPvmTransitionList.add(pvmTransition);
        }
        pvmTransitionList.clear();
        
        // 创建新流向
        TransitionImpl newTransition = currentActivity.createOutgoingTransition();
        
        // 设置新流向的目标节点
        newTransition.setDestination(destinationActivity);
        
        // 执行转向任务
        taskService.complete(currentTaskId);
        TaskQuery query = taskService.createTaskQuery();
        List<Task> tasks = query.processInstanceId(processInstanceId).orderByTaskCreateTime().asc().list();
        for (Task task : tasks)
        {
            if (taskKey.equals(task.getTaskDefinitionKey()))
            {
                task.setCategory(TASK_CATEGORY_REFUSE);
                taskService.complete(task.getId());
            }
        }
        
        // 删除目标节点新流入
        destinationActivity.getIncomingTransitions().remove(newTransition);
        pvmTransitionList.clear();
        
        // 还原以前流向
        for (PvmTransition pvmTransition : oriPvmTransitionList)
        {
            pvmTransitionList.add(pvmTransition);
        }
    }
    
    /**
     * 
     * @param companyId 公司编号
     * @param taskId 当前任务编号
     * @param processDefinitionId 流程定义编号
     * @return Task
     * @Description:获取下一节点
     */
    public static TaskDefinition getNextTask(long companyId, String taskId)
    {
        try
        {
            ProcessEngine engine = getProcessEngine(companyId);
            if (engine != null)
            {
                RepositoryService repositoryService = engine.getRepositoryService();
                TaskService taskService = engine.getTaskService();
                Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
                String taskDefinitionKey = task.getTaskDefinitionKey();
                ReadOnlyProcessDefinition processDefinitionEntity = (ReadOnlyProcessDefinition)repositoryService.getProcessDefinition(task.getProcessDefinitionId());
                // 当前节点
                ActivityImpl currentActivity = (ActivityImpl)processDefinitionEntity.findActivity(taskDefinitionKey);
                // 获取该节点下一个节点信息
                TaskDefinition taskDefinition = ((UserTaskActivityBehavior)currentActivity.getActivityBehavior()).getTaskDefinition();
                return taskDefinition;
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * 
     * @param companyId 公司编号
     * @param taskId 任务编号
     * @return String
     * @Description:获取流程定义id
     */
    public static String getProcessDefinitionId(long companyId, String taskId)
    {
        try
        {
            ProcessEngine engine = getProcessEngine(companyId);
            if (engine != null)
            {
                TaskService taskService = engine.getTaskService();
                Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
                return task.getProcessDefinitionId();
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * 
     * @param companyId 公司编号
     * @param processDefinitionId 流程定义id
     * @param taskDefinitionKey 当前节点key
     * @return TaskDefinition
     * @Description:获取下一节点定义
     */
    public static Object getNextTaskDefinition(long companyId, String processDefinitionId, String taskDefinitionKey, String dataId)
    {
        try
        {
            ProcessEngine engine = getProcessEngine(companyId);
            if (engine != null)
            {
                RepositoryService repositoryService = engine.getRepositoryService();
                ReadOnlyProcessDefinition processDefinitionEntity = (ReadOnlyProcessDefinition)repositoryService.getProcessDefinition(processDefinitionId);
                // 当前节点
                ActivityImpl currentActivity = (ActivityImpl)processDefinitionEntity.findActivity(taskDefinitionKey);
                // 获取当前对象后续路径
                List<PvmTransition> transitions = currentActivity.getOutgoingTransitions();
                if (null != transitions && transitions.size() == 1)
                {
                    ActivityImpl ai = (ActivityImpl)transitions.get(0).getDestination();
                    if (ai.getId().indexOf(Constant.PROCESS_FIELD_GATEWAYTASK) > -1)
                    {
                        currentActivity = (ActivityImpl)processDefinitionEntity.findActivity(ai.getId());
                        transitions = currentActivity.getOutgoingTransitions();
                    }
                }
                for (PvmTransition trans : transitions)
                {
                    ActivityImpl ai = (ActivityImpl)trans.getDestination();
                    Object conditionObj = trans.getProperty(BpmnParse.PROPERTYNAME_CONDITION_TEXT);
                    if (null != conditionObj)
                    {
                        String executeSql = conditionObj.toString().substring(conditionObj.toString().indexOf("'") + 1, conditionObj.toString().lastIndexOf("'"));
                        executeSql = executeSql.replaceAll(Constant.VAR_QUOTES, "'");
                        if (!StringUtil.isEmpty(dataId))
                        {
                            executeSql = executeSql.concat(" and id = ").concat(dataId);
                        }
                        List<JSONObject> result = DAOUtil.executeQuery4JSON(executeSql);
                        if (transitions.size() > 1)
                        {// 多分支
                            if (!result.isEmpty())
                            {
                                UserTaskActivityBehavior utab = (UserTaskActivityBehavior)ai.getActivityBehavior();
                                return utab;// .getTaskDefinition()
                            }
                        }
                        else
                        {// 单分支
                            if (!result.isEmpty())
                            {
                                UserTaskActivityBehavior utab = (UserTaskActivityBehavior)ai.getActivityBehavior();
                                return utab;// .getTaskDefinition()
                            }
                            else
                            {
                                return "noAssignee";
                            }
                        }
                    }
                    else
                    {
                        if (ai.getId().equals(Constant.PROCESS_FIELD_TASK_END))
                        {
                            return null;
                        }
                        return ai.getActivityBehavior();
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * 
     * @param companyId 公司编号
     * @param processInstanceId 流程实例编号
     * @param assignee 执行者
     * @return List<Task>
     * @Description:根据执行者获取要执行的任务
     */
    public static List<Task> getTaskByAssignee(long companyId, String processInstanceId, String assignee)
    {
        try
        {
            ProcessEngine engine = getProcessEngine(companyId);
            if (engine != null)
            {
                TaskService taskService = engine.getTaskService();
                return taskService.createTaskQuery().processInstanceId(processInstanceId).taskAssignee(assignee).orderByTaskCreateTime().asc().list();
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return new ArrayList<>();
    }
    
    /**
     * 
     * @param companyId 公司编号
     * @param processInstanceId 流程实例编号
     * @return List<Task>
     * @Description:当前任务列表
     */
    public static List<Task> getTasks(long companyId, String processInstanceId)
    {
        try
        {
            ProcessEngine engine = getProcessEngine(companyId);
            if (engine != null)
            {
                TaskService taskService = engine.getTaskService();
                return taskService.createTaskQuery().processInstanceId(processInstanceId).orderByTaskCreateTime().asc().list();
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return new ArrayList<>();
    }
    
    /**
     * 
     * @param companyId 公司编号
     * @param activiTaskId 当前流程实例活跃任务编号
     * @param expression el表达式 ${...}
     * @return Object
     * @Description:跟进表达式获取值（AssigneeServer类代理）
     */
    public static Object parseExpression(long companyId, String activiTaskId, String expression)
    {
        if (StringUtils.isEmpty(expression) || !expression.startsWith("$"))
        {
            return expression;
        }
        try
        {
            ProcessEngine engine = getProcessEngine(companyId);
            if (engine != null)
            {
                TaskService taskService = engine.getTaskService();
                AssigneeServer assigneeServer = new AssigneeServer();
                ExpressionParser parser = new SpelExpressionParser();
                StandardEvaluationContext itemContext = new StandardEvaluationContext(assigneeServer);
                
                boolean flag = false;
                int dindex = expression.indexOf(".");
                int ksindex = expression.indexOf("(");
                int keindex = expression.lastIndexOf(")");
                if (dindex < 0 || ksindex < 0 || keindex < 0)
                {
                    String parameter = expression.substring(2, expression.length() - 1);
                    return taskService.getVariable(activiTaskId, parameter);
                }
                String parameters = expression.substring(ksindex + 1, keindex);
                for (String parameter : parameters.split(","))
                {
                    if (parameter.contains("'"))
                    {
                        flag = !flag;
                        continue;
                    }
                    if (flag)
                    {
                        continue;
                    }
                    Object object = taskService.getVariable(activiTaskId, parameter);
                    if (object != null)
                    {
                        expression = expression.replace(parameter, "'" + object + "'");
                    }
                }
                String exp = expression.substring(dindex + 1, expression.length() - 1);
                Expression exp4 = parser.parseExpression(exp);
                return exp4.getValue(itemContext);
                
            }
            
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}