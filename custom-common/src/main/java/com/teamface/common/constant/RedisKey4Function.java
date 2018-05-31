package com.teamface.common.constant;

/**
 * @Description:redis的功能标识key，用的是index。 Redis key命名规则，
 *                                      由以下五部分组成(缺失的用字符x替代)，每部分之间用"_"分隔，全采用小写：
 *                                      1.环境标识：已在JedisClusterHelper自动填充，开发者不用考虑这一块,见Constant里的REDIS_KEY_SUFFIX
 *                                      2.公司标识：公司编号
 *                                      3.模块标识:bean的英文名称,非自定义模块可用其它类似标识
 *                                      4.功能标识:在RedisKey4Function里定义，用的是index
 *                                      5.动态参数:可选项
 * @author: ZZH
 * @date: 2018年4月25日 下午5:34:49
 * @version: 1.0
 */
public enum RedisKey4Function
{
    /* key格式：公司编号_模块标识_功能标识（例：1_bean1523859282504_1） */
    LAYOUT_ENABLE("布局-已使用字段", 1),
    /* key格式： 公司编号_模块标识_功能标识（例：1_bean1523859282504_2） */
    LAYOUT_DISABLE("布局-未使用字段", 2),
    /* key格式： 公司编号_模块标识_功能标识（例：1_bean1523859282504_3） */
    LAYOUT_RELATION("布局-关联关系", 3),
    /* key格式：公司编号_模块标识_功能标识（例：1_bean1523859282504_4） */
    LAYOUT_SUBFORM_TABLES("布局-子表单表名", 4),
    /* key格式： 公司编号_模块标识_终端0pc1app_功能标识（例：1_bean1523859282504_4） */
    LAYOUT_LIST_FIELDS("布局-数据列表字段设置", 5),
    /* key格式： */
    PROCESS_MODULE_LAYOUT("流程表单布局", 6),
    /* key格式： 公司编号_模块标识_流程属性id_功能标识（例：1_bean1523859282504_1_7） */
    PROCESS_MODULE_FIELD_V("流程字段版本", 7),
    /* key格式： 公司标识_流程实例_功能标识（例：1_2402_8） */
    PROCESS_BEGIN_USER("流程发起者", 8),
    /* key格式： 公司标识_流程实例_功能标识（例：1_2402_9） */
    PROCESS_NAME("流程名称", 9),
    /* key格式：公司标识_流程实例_节点key_功能标识（例：1_2402_task151_9） */
    PROCESS_APPROVAL_USERS("已审批人ID集", 10),
    /* key格式： (例 x_time1_11)*/ 
    USER_LOGIN_INFO("登录用户相关信息", 11),
    /* key格式： */
    REPORT_DATALIST_FILTER_FIELDS("报表数据列表筛选字段", 12),
    /* key格式：x_cjob_13_className_mothodName_timer */
    JOB_TRIGGER_REQUEST_PARAM("触发任务请求参数", 13),
    /* key格式：公司标识_流程实例_功能标识（例：1_2402_14） */
    PROCESS_AGREE_USERS("流程审批通过人员集合", 14);
    
    // 注释
    private String name;
    
    // 索引，用于REDIS key
    private int index;
    
    private RedisKey4Function(String name, int index)
    {
        this.name = name;
        this.index = index;
    }
    
    public static String getName(int index)
    {
        for (RedisKey4Function c : RedisKey4Function.values())
        {
            if (c.getIndex() == index)
            {
                return c.name;
            }
        }
        return null;
    }
    
    public String getName()
    {
        return name;
    }
    
    public int getIndex()
    {
        return index;
    }
}