package com.teamface.common.util.dao;

import com.alibaba.fastjson.JSONObject;

/**
 * @Description: 提供给工具类使用的bean
 * @author: zhangzhihgua
 * @date: 2017年11月7日 上午9:58:10
 * @version: 1.0
 */

public class UtilDTO
{
    
    public static class Field
    {
        // 字段名
        public String name;
        
        // 字段注释
        public String label;
        
        // 字段类型
        public String type;
        
        // 组件类型
        public String layoutType;
        
        // 组件结构
        public JSONObject layoutJson;
        
        public Field(String name, String label, String type, String layoutType, JSONObject layoutJson)
        {
            this.name = name;
            this.label = label;
            this.type = type;
            this.layoutType = layoutType;
            this.layoutJson = layoutJson;
        }
    }
    
    public static class Relation
    {
        // 关联bean
        public String bean;
        
        // 当前字段
        public String field;
        
        // 关联字段
        public String referenceField;
        
        // 关系字段(显示字段)
        public String relationField;
        
        // 关联条件
        public String where;
        
        public Relation(String field, String referenceField, String bean, String relationField, String where)
        {
            this.field = field;
            this.bean = bean;
            this.referenceField = referenceField;
            this.relationField = relationField;
            this.where = where;
        }
    }
}
