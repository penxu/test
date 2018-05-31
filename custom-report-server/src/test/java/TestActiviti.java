import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;


/**
 * @Description:
 * @author: Administrator
 * @date: 2017年12月26日 上午9:48:06
 * @version: 1.0
 */

public class TestActiviti
{
    static void run(ProcessEngine processEngine)
        throws Exception
    {
        RepositoryService repositoryService = processEngine.getRepositoryService();
        
        repositoryService.createDeployment().addClasspathResource("process08.bpmn").deploy();
        
        RuntimeService runtimeService = processEngine.getRuntimeService();
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("userid", "10010");
        variables.put("pass", "1");
        List<String> assigneeList = new ArrayList<String>(); // 分配任务的人员
        assigneeList.add("tom");
        assigneeList.add("jeck");
        assigneeList.add("mary");
        variables.put("assigneeList", assigneeList);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process08", variables);
        
        TaskService taskService = processEngine.getTaskService();
        TaskQuery query = taskService.createTaskQuery();
        
        System.out.println(processInstance.getId());
        List<Task> tasks = query.processInstanceId(processInstance.getId()).list();
        
        for (Task task : tasks)
        {
            try
            {
                System.out.println(task.getId() + "," + task.getName() + "," + task.getAssignee());
                taskService.setVariable(task.getId(), "firster", "123");
                taskService.setVariableLocal(task.getId(), "myname", task.getName());
                taskService.complete(task.getId(), variables);
            }
            catch (Exception e)
            {
                break;
            }
            
        }
        
        System.out.println("--------------------");
        
        tasks = query.processInstanceId(processInstance.getId()).list();
        
        for (Task task : tasks)
        {
            try
            {
                System.out.println(task.getId() + "," + task.getName() + "," + task.getAssignee());
                
                taskService.complete(task.getId(), variables);
            }
            catch (Exception e)
            {
                break;
            }
        }
        
        System.out.println("--------------------");
        
        tasks = query.processInstanceId(processInstance.getId()).list();
        
        for (Task task : tasks)
        {
            try
            {
                System.out.println("firster:" + taskService.getVariable(task.getId(), "firster"));
                System.out.println("myname:" + taskService.getVariable(task.getId(), "myname"));
                System.out.println("myname:" + taskService.getVariableLocal(task.getId(), "myname"));
                System.out.println(task.getId() + "," + task.getName() + "," + task.getAssignee());
                
                taskService.complete(task.getId(), variables);
            }
            catch (Exception e)
            {
                break;
            }
        }
        
        System.out.println("--------------------");
        
        tasks = query.processInstanceId(processInstance.getId()).list();
        
        for (Task task : tasks)
        {
            try
            {
                
                System.out.println(task.getId() + "," + task.getName() + "," + task.getAssignee());
                
                taskService.complete(task.getId(), variables);
                
            }
            catch (Exception e)
            {
                break;
            }
        }
        
        System.out.println("--------------------");
        
        tasks = query.processInstanceId(processInstance.getId()).list();
        
        for (Task task : tasks)
        {
            try
            {
                System.out.println(task.getId() + "," + task.getName() + "," + task.getAssignee());
                
                taskService.complete(task.getId(), variables);
            }
            catch (Exception e)
            {
                break;
            }
        }
        
        System.out.println("--------------------");
        
        tasks = query.processInstanceId(processInstance.getId()).list();
        
        for (Task task : tasks)
        {
            try
            {
                System.out.println(task.getId() + "," + task.getName() + "," + task.getAssignee());
                
                taskService.complete(task.getId(), variables);
            }
            catch (Exception e)
            {
                break;
            }
        }
        
        System.out.println("--------------------");
        tasks = query.processInstanceId(processInstance.getId()).list();
        
        for (Task task : tasks)
        {
            try
            {
                System.out.println(task.getId() + "," + task.getName() + "," + task.getAssignee());
                
                taskService.complete(task.getId(), variables);
            }
            catch (Exception e)
            {
                break;
            }
        }
        
        System.out.println("--------------------");
        tasks = query.processInstanceId(processInstance.getId()).list();
        
        for (Task task : tasks)
        {
            try
            {
                System.out.println(task.getId() + "," + task.getName() + "," + task.getAssignee());
                
                taskService.complete(task.getId(), variables);
            }
            catch (Exception e)
            {
                break;
            }
        }
        
        System.out.println("--------------------");
        tasks = query.processInstanceId(processInstance.getId()).list();
        
        for (Task task : tasks)
        {
            try
            {
                System.out.println(task.getId() + "," + task.getName() + "," + task.getAssignee());
                
                taskService.complete(task.getId(), variables);
            }
            catch (Exception e)
            {
                break;
            }
        }
        
        System.out.println("--------------------");
        tasks = query.processInstanceId(processInstance.getId()).list();
        
        for (Task task : tasks)
        {
            try
            {
                System.out.println(task.getId() + "," + task.getName() + "," + task.getAssignee());
                
                taskService.complete(task.getId(), variables);
            }
            catch (Exception e)
            {
                break;
            }
        }
        
        System.out.println("--------------------");
        tasks = query.processInstanceId(processInstance.getId()).list();
        
        for (Task task : tasks)
        {
            try
            {
                System.out.println(task.getId() + "," + task.getName() + "," + task.getAssignee());
                
                taskService.complete(task.getId(), variables);
            }
            catch (Exception e)
            {
                break;
            }
        }
        
        System.out.println("--------------------");
        tasks = query.processInstanceId(processInstance.getId()).list();
        
        for (Task task : tasks)
        {
            try
            {
                System.out.println(task.getId() + "," + task.getName() + "," + task.getAssignee());
                
                taskService.complete(task.getId(), variables);
            }
            catch (Exception e)
            {
                break;
            }
        }
        
        System.out.println("--------------------");
    }
    
    static void run1(ProcessEngine processEngine)
        throws Exception
    {
        RepositoryService repositoryService = processEngine.getRepositoryService();
        
        repositoryService.createDeployment().addClasspathResource("leave.bpmn20.xml").deploy();
        
        RuntimeService runtimeService = processEngine.getRuntimeService();
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("userid", "10010");
        variables.put("day", 3);
        
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("leave_process", variables);
        
        TaskService taskService = processEngine.getTaskService();
        HistoryService historyService = processEngine.getHistoryService();
        TaskQuery query = taskService.createTaskQuery();
        
        System.out.println(processInstance.getId());
        List<Task> tasks = query.processInstanceId(processInstance.getId()).list();
        
        for (Task task : tasks)
        {
            System.out.println(task.getId() + "," + task.getName() + "," + task.getAssignee());
            taskService.complete(task.getId(), variables);
        }
        
        System.out.println("--------------------");
        
        tasks = query.processInstanceId(processInstance.getId()).list();
        
        for (Task task : tasks)
        {
            System.out.println(task.getId() + "," + task.getName() + "," + task.getAssignee());
            // taskService.complete(task.getId(), variables);
            List<HistoricTaskInstance> taskss =
                historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstance.getId()).finished().orderByTaskCreateTime().desc().list();
            System.out.println(taskss.size());
            System.out.println(taskss.get(0).getId() + "," + taskss.get(0).getName() + "," + taskss.get(0).getAssignee());
            
        }
        
        System.out.println("--------------------");
        
        // tasks = query.processInstanceId(processInstance.getId()).list();
        //
        // for(Task task : tasks)
        // {
        // System.out.println(task.getId() + "," +
        // task.getName()+","+task.getAssignee());
        // taskService.complete(task.getId(), variables);
        // }
    }
    
    public static ProcessEngine basic()
    {
        ProcessEngineConfiguration cfg = new StandaloneProcessEngineConfiguration().setTablePrefixIsSchema(true)
            .setJdbcUrl("jdbc:postgresql://192.168.1.223:5432/test4")
            .setJdbcUsername("hjhq")
            .setJdbcPassword("hjhq123")
            .setJdbcDriver("org.postgresql.Driver")
            
            .setJobExecutorActivate(false)
            .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        ProcessEngine processEngine = cfg.buildProcessEngine();
        String pName = processEngine.getName();
        String ver = ProcessEngine.VERSION;
        System.out.println("ProcessEngine [" + pName + "] Version: [" + ver + "]");
        
        return processEngine;
    }
    
    public static void main(String[] args)
    {
        String beanName = "bean1527673478016";
        Long companyId = 13L;
        String sql = "ALTER TABLE bean1527673478016_approval_13 ADD COLUMN phone_1527673630606 varchar(20);ALTER TABLE bean1527673478016_approval_13 ADD COLUMN text_1527673623743 varchar(255);ALTER TABLE bean1527673478016_approval_13 ADD COLUMN picklist_1527673615646_v varchar(100);ALTER TABLE bean1527673478016_approval_13 ADD COLUMN picklist_1527673615646 varchar(1000);COMMENT ON COLUMN bean1527673478016_approval_13.phone_1527673630606 is '电话+';COMMENT ON COLUMN bean1527673478016_approval_13.text_1527673623743 is '单行文本+';COMMENT ON COLUMN bean1527673478016_approval_13.picklist_1527673615646_v is '下拉选项+';COMMENT ON COLUMN bean1527673478016_approval_13.picklist_1527673615646 is '下拉选项+';create table bean1527673478016_subform_1527673584362_13 ( id serial not null,subform_location_1527673590503 varchar(300) ,subform_attachment_1527673596400 varchar(1000) ,del_status char DEFAULT '0',bean1527673478016_id int  ,CONSTRAINT  bean1527673478016_subform_1527673584362_13_pkey  PRIMARY KEY (id));COMMENT ON TABLE bean1527673478016_subform_1527673584362_13 is '子表单';COMMENT ON COLUMN bean1527673478016_subform_1527673584362_13.id is '主键id';COMMENT ON COLUMN bean1527673478016_subform_1527673584362_13.subform_location_1527673590503 is '定位+';COMMENT ON COLUMN bean1527673478016_subform_1527673584362_13.subform_attachment_1527673596400 is '附件+';COMMENT ON COLUMN bean1527673478016_subform_1527673584362_13.del_status is '删除状态';";
        if (sql.contains(beanName.concat("_subform_")))
        {
            String prefix = beanName.concat("_subform_");
            String companyStr = "_".concat(companyId.toString());
            String tmpStr = sql.substring(sql.indexOf(prefix), sql.length());
            String businessSubformBean = tmpStr.substring(tmpStr.indexOf(prefix), tmpStr.indexOf(companyStr));
            String businessSubformTable = businessSubformBean.concat(companyStr);
            String approvalSubformTable = businessSubformBean.concat("_approval").concat(companyStr);
            sql = sql.replace(businessSubformTable, approvalSubformTable);
        }
        
        String subField = "bean1527653692207_subform_1527661154455_approval_12_subform_number_1526634186775".replace("_approval_", "_");
        System.err.println(" >> "+(subField.length() > 64 ? subField.substring(0, 63) : subField));
        
        String jkl = "select * From bean1527644945683_subform_1527645508080_8 where jfkds_la jkldsja_f jkl ";
        
        System.err.println(jkl.substring(jkl.indexOf("bean1527644945683_subform_"), jkl.indexOf("_8")).concat("_8"));
        
        String t1 = "bean1527644945683_8";
        String t2 = "bean1527644945683_subform_1527645508080_8";
        System.err.println(t1.substring(0, t1.lastIndexOf("_") + 1).concat("approval"));
        System.err.println(t2.substring(0, t2.lastIndexOf("_") + 1).concat("approval"));
        
        String fd = "bean1526523981976_subform_1526524574767_2";
        System.err.println(fd.substring(0, fd.indexOf("_") + 1));
        
        String asField = "bean1526389720739_subform_1526615904913_4_subform_number_1526634186775";
        
        System.err.println(asField.length() > 64 ? asField.substring(0, 63) : asField);
        
        System.err
            .println(asField.length() > 64 ? asField.substring(0, asField.lastIndexOf("subform")) + asField.substring(asField.lastIndexOf("_") + 1, asField.length()) : asField);
        
        System.err.println(asField.length() > 64 ? "a".concat(asField.substring(asField.length() - 62, asField.length())) : asField);
        
        /*
         * StringBuilder sdf = new StringBuilder();
         * sdf.append("3421,321,dqw, 4321,ttttt,"); String[] dfsfds =
         * sdf.toString().split(","); for (String string : dfsfds) {
         * System.out.println(string); }
         */
        
        /*
         * ProcessEngine processEngine = basic();
         * 
         * try { run(processEngine); } catch (Exception e) {
         * e.printStackTrace(); }
         * 
         * processEngine.close();
         */}
}
