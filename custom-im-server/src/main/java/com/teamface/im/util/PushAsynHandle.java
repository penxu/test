package com.teamface.im.util;

import com.teamface.common.constant.Constant;
import com.teamface.common.msg.WebSocketCustomPush;

public class PushAsynHandle
{
    
    public static void pushMsg(String pushContent, String[] pushPeople, Long account)
    {
        
        new Thread()
        {
            @Override
            public void run()
            {
                for (String people : pushPeople)
                {
                    String pricipal = account == null ? "" : account.toString();
                    if (!people.equals(pricipal))
                    {
                        WebSocketCustomPush.getInstance().singlePush(pushContent, Constant.PUSH_ACCOUNT, Long.valueOf(people), 1);
                    }
                }
                
            }
        }.start();
    }
    
    public static void pushGroupMsg(String pushContent, Long groupId)
    {
        
        new Thread()
        {
            @Override
            public void run()
            {
                WebSocketCustomPush.getInstance().groupPush(pushContent, Constant.PUSH_ACCOUNT, groupId, 1);
            }
        }.start();
    }
}
