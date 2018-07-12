package com.teamface.im.constant;

public enum AssisstantEnum
{
    ASSISTANT_TYPE_APPLICATION_MODULE("应用小助手", 1),
    
    ASSISTANT_TYPE_APPLICATION_CHAT("企信小助手", 2),
    
    ASSISTANT_TYPE_APPROVAL("审批小助手", 3),
    
    ASSISTANT_TYPE_LIB("文件库小助手", 4),
    
    ASSISTANT_TYPE_MEMO("备忘录小助手", 5),
    
    ASSISTANT_TYPE_EMAIL("邮件小助手", 6),
    
    ASSISTANT_TYPE_TASK("任务小助手", 7);
    
    // 注释
    private String name;
    
    // 索引
    private int index;
    
    private AssisstantEnum(String name, int index)
    {
        this.name = name;
        this.index = index;
    }
    
    public static String getName(int index)
    {
        for (AssisstantEnum c : AssisstantEnum.values())
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
