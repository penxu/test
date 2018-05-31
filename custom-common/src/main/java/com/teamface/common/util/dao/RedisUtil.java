package com.teamface.common.util.dao;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * 
 * @Title:
 * @Description:企信公共 Redis 操作
 * @Author: liupan
 * @Since:2018年4月11日
 * @Version:1.1.0
 */
public class RedisUtil
{
    private static final Logger LOG = Logger.getLogger(RedisUtil.class);
    
    /**
     * 企信公共存入Redis信息
     * 
     * @param signId
     * @return
     * @Description:
     */
    public static boolean commonRedis(String signId)
    {
        LOG.debug("插入redis数据" + signId);
        Map<String, String> signMap = new HashMap<>();
        signMap.put("socket", "0");
        signMap.put("client_addr", "0");
        signMap.put("logintime", "0");
        signMap.put("logouttime", "0");
        signMap.put("server_addr", "0");
        String pcKey = "cpp:hash:imid:pc:" + signId;
        String appKey = "cpp:hash:imid:phone:" + signId;
        JedisClusterHelper.hmset(pcKey, signMap);
        JedisClusterHelper.hmset(appKey, signMap);
        LOG.debug("插入redis数据结束" + signId);
        return true;
    }
    
    /**
     * 企信公共删除Redis信息
     * 
     * @param signId
     * @Description:
     */
    public static boolean delRedis(Long signId)
    {
        LOG.debug("删除redis数据" + signId);
        String pcKey = "cpp:hash:imid:pc:" + signId;
        String appKey = "cpp:hash:imid:phone:" + signId;
        JedisClusterHelper.del(pcKey);
        JedisClusterHelper.del(appKey);
        LOG.debug("删除redis数据结束" + signId);
        return true;
    }
    
}
