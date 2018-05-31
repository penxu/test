package com.teamface.db;
import java.io.File;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;

import com.teamface.common.util.FileUtil;
import com.teamface.common.util.dao.DAOUtil;

/**
 * @Description:
 * @author: ZZH
 * @date: 2018年1月30日 上午11:08:36
 * @version: 1.0
 */

public class TestInitCompanyTables
{
    private static final Logger log = Logger.getLogger(TestInitCompanyTables.class);
    
    public static void main(String[] args)
    {
        TestInitCompanyTables.initCompanyTable();
    }
    
    public static Connection getConnection()
    {
        Connection con = null;
        String driver = "org.postgresql.Driver";
        String url = "jdbc:postgresql://192.168.1.223:5432/custom_test";
        String user = "hjhq";
        String password = "hjhq123";
        try
        {
            Class.forName(driver);
            con = DriverManager.getConnection(url, user, password);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return con;
    }
    
    public static class CreateTable extends Thread
    {
        private Long company;
        
        private List<String> sqls;
        
        public CreateTable(Long company, List<String> sqls)
        {
            this.company = company;
            this.sqls = sqls;
        }
        
        public void run()
        {
            try
            {
                executeSqlScript(sqls, "replace", company.toString(), 0);
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
        }
    }
    
    public static class CreateCompanyTable extends Thread
    {
        public void run()
        {
            createCompanyTables();
        }
    }
    
    private static void createEmployeeTables(long companyId)
    {
        StringBuilder sqlSB = new StringBuilder();
        // employee
        sqlSB.append("CREATE TABLE employee_")
        .append(companyId)
        .append("(")
        .append(
            "id  serial NOT NULL, employee_name  varchar(200), picture  varchar(2000), leader  char(1) DEFAULT '0'::bpchar, phone  varchar(20), mobile_phone  varchar(20),birth   int8, region  varchar(20), email  varchar(100), status  char(1) DEFAULT '0'::bpchar, account  varchar(255), is_enable  char(1) DEFAULT 0, post_id  int4, role_id  int4, del_status  char(1) DEFAULT 0, microblog_background  varchar(2000), sex  char(1), sign  varchar(255), mood  varchar(255), personnel_create_by  int4, datetime_create_time  int8,")
        .append("CONSTRAINT employee_")
        .append(companyId)
        .append("_pkey PRIMARY KEY (id));");
    // department
    sqlSB.append("CREATE TABLE department_")
        .append(companyId)
        .append("(")
        .append(" id  serial NOT NULL, department_name  varchar(200), parent_id  int4, status  char(1) DEFAULT '0'::bpchar,")
        .append("CONSTRAINT department_")
        .append(companyId)
        .append("_pkey PRIMARY KEY (id));");
    // department_center
    sqlSB.append("CREATE TABLE department_center_")
        .append(companyId)
        .append("(")
        .append(" id  serial NOT NULL, department_id  int4, employee_id  int4, status  char(1) DEFAULT 0, leader  char(1) DEFAULT 0, is_main  char(1) DEFAULT 0,")
        .append("CONSTRAINT department_center_")
        .append(companyId)
        .append("_pkey PRIMARY KEY (id));");
    // post_
    sqlSB.append("CREATE TABLE post_")
        .append(companyId)
        .append("(")
        .append(" id serial NOT NULL, name  varchar(200),status char(1) DEFAULT 0,")
        .append("CONSTRAINT post_")
        .append(companyId)
        .append("_pkey PRIMARY KEY (id));");
    // role_module_
    sqlSB.append("CREATE TABLE role_module_")
        .append(companyId)
        .append("(")
        .append(" id SERIAL NOT NULL,role_id int2,module_id int2,data_auth int2,")
        .append("CONSTRAINT role_module_")
        .append(companyId)
        .append("_pkey PRIMARY KEY (id));");
    // module_func_auth_
    sqlSB.append("CREATE TABLE module_func_auth_")
        .append(companyId)
        .append("(")
        .append(" id SERIAL NOT NULL, role_id int2,module_id int2,func_id int2,auth_code int2,")
        .append("CONSTRAINT module_func_auth_")
        .append(companyId)
        .append("_pkey PRIMARY KEY (id));");
    // application_module_
    sqlSB.append("CREATE TABLE application_module_")
        .append(companyId)
        .append("(")
        .append(
            "id serial NOT NULL,terminal_app char(1) DEFAULT 0,application_id int4,chinese_name varchar(200),topper int2,personnel_create_by varchar(100),datetime_create_time int8,personnel_modify_by varchar(100),datetime_modify_time int8,icon varchar(2048),icon_type char(1),icon_color varchar(255),icon_url text,edition char(1),english_name varchar(255),del_status char(1) DEFAULT 0,")
        .append("CONSTRAINT application_module_")
        .append(companyId)
        .append("_pkey PRIMARY KEY (id));");
    // employee_fabulous_
    sqlSB.append("CREATE TABLE employee_fabulous_")
        .append(companyId)
        .append("(")
        .append(" id serial NOT NULL,fabulous_id int4,employee_id int4,status char(1) DEFAULT '1'::bpchar,")
        .append("CONSTRAINT employee_fabulous_")
        .append(companyId)
        .append("_pkey PRIMARY KEY (id));");
    DAOUtil.executeUpdate(sqlSB.toString());
    }
    
    @Transactional
    public static void createCompanyTables()
    {
        log.error("createCompanyTables start!");
        long t1 = System.currentTimeMillis();
        int ecompanyCount = executeCount("select count(*) from company where company_name is null and status='1'");
        log.error("null company count: " + ecompanyCount);
        if (ecompanyCount < 5)
        {
            try
            {
                Long company = getNextval4Table("company", "");
                String insertSql = "insert into company(id,status) values(" + company + ",'1')";
                executeUpdate(insertSql);
                log.error("createCompanyTables,compayId:" + company);
                
                // 创建表
                createEmployeeTables(company);
                ClassPathResource rc = new ClassPathResource("execute/company-tables.sql");
                File file = rc.getFile();
                List<String> sqls = FileUtil.readFile(file, ";", "utf-8");
                CreateTable ct = new CreateTable(company, sqls);
                ct.start();
                while (ct.isAlive())
                {
                    sleep(100);
                }
                log.error("company-tables created!");
                
                // 添加字段注释
                ClassPathResource indexrc = new ClassPathResource("execute/company-index.sql");
                File indexfile = indexrc.getFile();
                List<String> indexsqls = FileUtil.readFile(indexfile, ";", "utf-8");
                CreateTable cit = new CreateTable(company, indexsqls);
                cit.start();
                log.error("company-index created!");
                
                // 流程表
                ClassPathResource prc = new ClassPathResource("execute/process-constraint.sql");
                File pfile = prc.getFile();
                List<String> psqls = FileUtil.readFile(pfile, ";", "utf-8");
                CreateTable pct = new CreateTable(company, psqls);
                pct.start();
                log.error("process-constraint created!");
                
                StringBuilder sqlSB = new StringBuilder();
                sqlSB.append("INSERT INTO post_" + company + " VALUES (nextval('post_" + company + "_id_seq'::regclass), '员工','0');");
                sqlSB.append("INSERT INTO role_group_" + company + " VALUES (nextval('role_group_" + company + "_id_seq'::regclass), '系统类角色', '1', '0');");
                sqlSB.append("INSERT INTO role_group_" + company + " VALUES (nextval('role_group_" + company + "_id_seq'::regclass), '职能类角色', '0', '0');");
                sqlSB.append("INSERT INTO role_" + company + " VALUES (nextval('role_" + company + "_id_seq'::regclass),'1','企业所有者','0', '');");
                sqlSB.append("INSERT INTO role_" + company + " VALUES (nextval('role_" + company + "_id_seq'::regclass), '1', '系统管理员', '0', '');");
                sqlSB.append("INSERT INTO role_" + company + " VALUES (nextval('role_" + company + "_id_seq'::regclass), '1', '成员', '0', '');");
                sqlSB.append("INSERT INTO catalog_table_" + company + " VALUES (nextval('catalog_table_" + company + "_id_seq'::regclass), '公司文件');");
                sqlSB.append("INSERT INTO catalog_table_" + company + " VALUES (nextval('catalog_table_" + company + "_id_seq'::regclass), '应用文件');");
                sqlSB.append("INSERT INTO catalog_table_" + company + " VALUES (nextval('catalog_table_" + company + "_id_seq'::regclass), '个人文件');");
                sqlSB.append("INSERT INTO catalog_table_" + company + " VALUES (nextval('catalog_table_" + company + "_id_seq'::regclass), '我共享的');");
                sqlSB.append("INSERT INTO catalog_table_" + company + " VALUES (nextval('catalog_table_" + company + "_id_seq'::regclass), '与我共享');");
                sqlSB.append("INSERT INTO project_management_role_" + company + " VALUES (nextval('project_management_role_" + company + "_id_seq'::regclass), '项目负责人','拥有当前负责项目所有权限，可修改权限范围',1,'1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24',1,").append(t1).append(");");
                sqlSB.append("INSERT INTO project_management_role_" + company + " VALUES (nextval('project_management_role_" + company + "_id_seq'::regclass), '项目成员','拥有参与项目享有的权限，可修改权限范围',2,'7,9,17,19,20,21',1,").append(t1).append(");");
                sqlSB.append("INSERT INTO project_management_role_" + company + " VALUES (nextval('project_management_role_" + company + "_id_seq'::regclass), '访客','只读角色，可修改权限范围',3,'19,20,21',1,").append(t1).append(");");
                
                sqlSB.append("INSERT INTO project_management_priviledge_" + company + " VALUES (nextval('project_management_priviledge_" + company + "_id_seq'::regclass), '修改项目信息');");
                sqlSB.append("INSERT INTO project_management_priviledge_" + company + " VALUES (nextval('project_management_priviledge_" + company + "_id_seq'::regclass), '设置项目偏好');");                
                sqlSB.append("INSERT INTO project_management_priviledge_" + company + " VALUES (nextval('project_management_priviledge_" + company + "_id_seq'::regclass), '设置项目状态变更');");
                sqlSB.append("INSERT INTO project_management_priviledge_" + company + " VALUES (nextval('project_management_priviledge_" + company + "_id_seq'::regclass), '添加标签');");
                sqlSB.append("INSERT INTO project_management_priviledge_" + company + " VALUES (nextval('project_management_priviledge_" + company + "_id_seq'::regclass), '移除标签');");
                sqlSB.append("INSERT INTO project_management_priviledge_" + company + " VALUES (nextval('project_management_priviledge_" + company + "_id_seq'::regclass), '导出项目任务');");
                sqlSB.append("INSERT INTO project_management_priviledge_" + company + " VALUES (nextval('project_management_priviledge_" + company + "_id_seq'::regclass), '邀请成员');");
                sqlSB.append("INSERT INTO project_management_priviledge_" + company + " VALUES (nextval('project_management_priviledge_" + company + "_id_seq'::regclass), '移除成员');");
                sqlSB.append("INSERT INTO project_management_priviledge_" + company + " VALUES (nextval('project_management_priviledge_" + company + "_id_seq'::regclass), '添加任务分组');");
                sqlSB.append("INSERT INTO project_management_priviledge_" + company + " VALUES (nextval('project_management_priviledge_" + company + "_id_seq'::regclass), '编辑任务分组');");
                sqlSB.append("INSERT INTO project_management_priviledge_" + company + " VALUES (nextval('project_management_priviledge_" + company + "_id_seq'::regclass), '删除任务分组');");
                sqlSB.append("INSERT INTO project_management_priviledge_" + company + " VALUES (nextval('project_management_priviledge_" + company + "_id_seq'::regclass), '移动任务分组');");
                sqlSB.append("INSERT INTO project_management_priviledge_" + company + " VALUES (nextval('project_management_priviledge_" + company + "_id_seq'::regclass), '新建列表');");
                sqlSB.append("INSERT INTO project_management_priviledge_" + company + " VALUES (nextval('project_management_priviledge_" + company + "_id_seq'::regclass), '编辑列表名称');");
                sqlSB.append("INSERT INTO project_management_priviledge_" + company + " VALUES (nextval('project_management_priviledge_" + company + "_id_seq'::regclass), '删除列表');");
                sqlSB.append("INSERT INTO project_management_priviledge_" + company + " VALUES (nextval('project_management_priviledge_" + company + "_id_seq'::regclass), '移动列表');");
                sqlSB.append("INSERT INTO project_management_priviledge_" + company + " VALUES (nextval('project_management_priviledge_" + company + "_id_seq'::regclass), '任务批量操作');");
                sqlSB.append("INSERT INTO project_management_priviledge_" + company + " VALUES (nextval('project_management_priviledge_" + company + "_id_seq'::regclass), '新建任务');");
                sqlSB.append("INSERT INTO project_management_priviledge_" + company + " VALUES (nextval('project_management_priviledge_" + company + "_id_seq'::regclass), '设置任务提醒');");
                sqlSB.append("INSERT INTO project_management_priviledge_" + company + " VALUES (nextval('project_management_priviledge_" + company + "_id_seq'::regclass), '点赞');");
                sqlSB.append("INSERT INTO project_management_priviledge_" + company + " VALUES (nextval('project_management_priviledge_" + company + "_id_seq'::regclass), '允许评论');");
                sqlSB.append("INSERT INTO project_management_priviledge_" + company + " VALUES (nextval('project_management_priviledge_" + company + "_id_seq'::regclass), '添加文件夹');");
                sqlSB.append("INSERT INTO project_management_priviledge_" + company + " VALUES (nextval('project_management_priviledge_" + company + "_id_seq'::regclass), '编辑文件夹');");
                sqlSB.append("INSERT INTO project_management_priviledge_" + company + " VALUES (nextval('project_management_priviledge_" + company + "_id_seq'::regclass), '删除文件');");
                DAOUtil.executeUpdate(sqlSB.toString());
                
                while (cit.isAlive() || pct.isAlive())
                {
                    sleep(100);
                }
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
        }
        long t2 = System.currentTimeMillis();
        log.error("createCompanyTables end! times:" + (t2 - t1) / 1000);
    }
    
    private static void sleep(long millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
    public static void initCompanyTable()
    {
        log.error("initCompanyTable start! ");
        CreateCompanyTable thread = new CreateCompanyTable();
        thread.start();
        log.error("initCompanyTable end! ");
    }
    
    public static int executeSqlScript(List<String> sqls, String target, String replacement, int batchCount)
    {
        if (sqls == null || sqls.isEmpty())
        {
            return 0;
        }
        int result = 0;
        Connection con = null;
        Statement stmt = null;
        try
        {
            con = getConnection();
            con.setAutoCommit(false);
            stmt = con.createStatement();
            for (int i = 0; i < sqls.size(); i++)
            {
                String sql = sqls.get(i);
                if (target != null && target.trim().length() > 0)
                {
                    sql = sql.replace(target, replacement);
                }
                stmt.addBatch(sql);
                if (batchCount > 0 && i % batchCount == 0)
                {
                    stmt.executeBatch();
                }
            }
            stmt.executeBatch();
            con.commit();
            con.setAutoCommit(true);
            result = 1;
        }
        catch (BatchUpdateException e)
        {
            result = -1;
            log.error(e.getMessage(), e);
            log.error(e.getNextException() == null ? "" : e.getNextException().getMessage(), e.getNextException());
        }
        catch (Exception e)
        {
            result = -1;
            log.error(e.getMessage(), e);
        }
        finally
        {
            closeStatement(stmt);
            closeConnection(con);
        }
        return result;
    }
    
    public static int executeUpdate(String sql)
    {
        if (sql == null || sql.trim().isEmpty())
        {
            return 0;
        }
        Connection con = null;
        Statement stmt = null;
        try
        {
            con = getConnection();
            stmt = con.createStatement();
            return stmt.executeUpdate(sql);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            closeStatement(stmt);
            closeConnection(con);
        }
        return 0;
    }
    
    public static void closeConnection(Connection conn)
    {
        try
        {
            if (conn != null)
            {
                conn.close();
            }
        }
        catch (SQLException e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
    public static void closeStatement(PreparedStatement stmt)
    {
        if (stmt != null)
        {
            try
            {
                stmt.close();
            }
            catch (SQLException e)
            {
                log.error(e.getMessage(), e);
            }
        }
    }
    
    public static void closeStatement(Statement stmt)
    {
        if (stmt != null)
        {
            try
            {
                stmt.close();
            }
            catch (SQLException e)
            {
                log.error(e.getMessage(), e);
            }
        }
    }
    
    public static int executeCount(String sql)
    {
        if (sql == null || sql.trim().isEmpty())
        {
            return 0;
        }
        Connection con = null;
        Statement stmt = null;
        try
        {
            con = getConnection();
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                return rs.getInt(1);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            closeStatement(stmt);
            closeConnection(con);
        }
        return 0;
    }
    
    public static long getNextval4Table(String bean, String companyId)
    {
        StringBuilder sqlSB = new StringBuilder();
        String tableName = getTableName(bean, companyId);
        sqlSB.append("select pa.adsrc from pg_attrdef pa join pg_class pc on pa.adrelid=pc.oid where pa.adnum=1 and pc.relname ='").append(tableName).append("'");
        List<Map<String, Object>> resultLS = executeQuery(sqlSB.toString());
        for (Map<String, Object> map : resultLS)
        {
            String seq = String.valueOf(map.get("adsrc"));
            sqlSB.setLength(0);
            sqlSB.append("select ").append(seq);
            return (long)(execute(sqlSB.toString()));
        }
        return 0;
    }
    
    public static String getTableName(String bean, String companyId)
    {
        if (companyId == null || companyId.trim().isEmpty())
            return bean;
        return bean + "_" + companyId;
    }
    
    public static List<Map<String, Object>> executeQuery(String sql)
    {
        List<Map<String, Object>> resultMapLS = new ArrayList<>();
        if (sql == null || sql.trim().isEmpty())
        {
            return resultMapLS;
        }
        Connection con = null;
        Statement stmt = null;
        try
        {
            con = getConnection();
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();
            while (rs.next())
            {
                Map<String, Object> resultMap = new LinkedHashMap<>();
                for (int i = 1; i <= rsmd.getColumnCount(); i++)
                {
                    resultMap.put(rsmd.getColumnName(i), rs.getObject(i) == null ? "" : rs.getObject(i));
                }
                resultMapLS.add(resultMap);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            closeStatement(stmt);
            closeConnection(con);
        }
        return resultMapLS;
    }
    
    public static Object execute(String sql)
    {
        if (sql == null || sql.trim().isEmpty())
        {
            return 0;
        }
        Connection con = null;
        Statement stmt = null;
        try
        {
            con = getConnection();
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                return rs.getObject(1);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            closeStatement(stmt);
            closeConnection(con);
        }
        return 0;
    }
}