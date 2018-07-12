package com.teamface.common.util.dao;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.teamface.common.constant.Constant;
import com.teamface.common.util.SerializableUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * @Description:类型(string-string,string-list,string-set,string-sortedset,string-map)
 * @author: zhangzhihua
 * @date: 2017年9月18日 上午10:48:03
 * @version: 1.0
 */

public class JedisClusterHelper
{
    static Logger log = Logger.getLogger(JedisClusterHelper.class);
    
    private static JedisPool jedisPool = null;
    
    private JedisClusterHelper()
    {
        
    }
    
    public static void hset(String key, String feild, String value)
    {
        Jedis jedis = null;
        try
        {
            jedis = getJedis();
            if (jedis != null)
            {
                jedis.hset(Constant.REDIS_KEY_SUFFIX + key, feild, value);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            close(jedis);
        }
    }
    
    public static void hdel(String key, String... feild)
    {
        Jedis jedis = null;
        try
        {
            jedis = getJedis();
            if (jedis != null)
            {
                jedis.hdel(Constant.REDIS_KEY_SUFFIX + key, feild);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            close(jedis);
        }
    }
    
    public static String hget(String key, String feild)
    {
        String value = null;
        Jedis jedis = null;
        try
        {
            jedis = getJedis();
            if (jedis != null)
            {
                value = jedis.hget(Constant.REDIS_KEY_SUFFIX + key, feild);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            close(jedis);
        }
        return value;
    }
    
    public static Long hincrBy(String key, String feild, long value)
    {
        Long result = -1l;
        Jedis jedis = null;
        try
        {
            jedis = getJedis();
            if (jedis != null)
            {
                result = jedis.hincrBy(Constant.REDIS_KEY_SUFFIX + key, feild, value);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            close(jedis);
        }
        return result;
    }
    
    public static void hmset(String key, Map<String, String> fieldValueMap)
    {
        Jedis jedis = null;
        try
        {
            jedis = getJedis();
            if (jedis != null)
            {
                jedis.hmset(Constant.REDIS_KEY_SUFFIX + key, fieldValueMap);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            close(jedis);
        }
    }
    
    public static void set(String key, Object value)
    {
        Jedis jedis = null;
        try
        {
            jedis = getJedis();
            if (jedis != null)
            {
                jedis.set((Constant.REDIS_KEY_SUFFIX + key).getBytes(), SerializableUtils.serialize(value));
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            close(jedis);
        }
    }
    
    public static void set(String key, Object value, int seconds)
    {
        Jedis jedis = null;
        try
        {
            jedis = getJedis();
            if (jedis != null)
            {
                jedis.setex((Constant.REDIS_KEY_SUFFIX + key).getBytes(), seconds, SerializableUtils.serialize(value));
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            close(jedis);
        }
    }
    
    public static void set(String key, String value)
    {
        Jedis jedis = null;
        try
        {
            jedis = getJedis();
            if (jedis != null)
            {
                jedis.set(Constant.REDIS_KEY_SUFFIX + key, value);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            close(jedis);
        }
    }
    
    public static void set(String key, String value, int seconds)
    {
        Jedis jedis = null;
        try
        {
            jedis = getJedis();
            if (jedis != null)
            {
                jedis.setex(Constant.REDIS_KEY_SUFFIX + key, seconds, value);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            close(jedis);
        }
    }
    
    /**
     * value以列表形式存储
     * 
     * @param key
     * @param members
     * @Description:
     */
    public static long lpush(String key, String... members)
    {
        long result = -1;
        Jedis jedis = null;
        try
        {
            jedis = getJedis();
            if (jedis != null)
            {
                result = jedis.lpush(Constant.REDIS_KEY_SUFFIX + key, members);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            close(jedis);
        }
        return result;
    }
    
    /**
     * 将值 value 插入到列表 key 的表头，当且仅当 key 存在并且是一个列表。 和 LPUSH 命令相反，当 key 不存在时，
     * LPUSHX 命令什么也不做。
     * 
     * @param key
     * @param members
     * @Description:
     */
    public static long lpushx(String key, String... members)
    {
        long result = -1;
        Jedis jedis = null;
        try
        {
            jedis = getJedis();
            if (jedis != null)
            {
                result = jedis.lpushx(Constant.REDIS_KEY_SUFFIX + key, members);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            close(jedis);
        }
        return result;
    }
    
    /**
     * 删除列表指定的值
     * 
     * @param key
     * @param member
     * @Description:
     */
    public static long lrem(String key, String member)
    {
        long result = -1;
        Jedis jedis = null;
        try
        {
            jedis = getJedis();
            if (jedis != null)
            {
                result = jedis.lrem(Constant.REDIS_KEY_SUFFIX + key, 1, member);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            close(jedis);
        }
        return result;
    }
    
    /**
     * 删除列表指定的值
     * 
     * @param key
     * @param member
     * @param count 个数（有重复值使用）
     * @Description:
     */
    public static long lrem(String key, String member, long count)
    {
        long result = -1;
        Jedis jedis = null;
        try
        {
            jedis = getJedis();
            if (jedis != null)
            {
                result = jedis.lrem(Constant.REDIS_KEY_SUFFIX + key, count, member);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            close(jedis);
        }
        return result;
    }
    
    /**
     * 向sets集合中加入元素
     * 
     * @param key
     * @param member
     * @Description:
     */
    public static long sadd(String key, String member)
    {
        long result = -1;
        Jedis jedis = null;
        try
        {
            jedis = getJedis();
            if (jedis != null)
            {
                result = jedis.sadd(Constant.REDIS_KEY_SUFFIX + key, member);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            close(jedis);
        }
        return result;
    }
    
    /**
     * 向sets集合中删除元素
     * 
     * @param key
     * @param member
     * @Description:
     */
    public static long srem(String key, String member)
    {
        long result = -1;
        Jedis jedis = null;
        try
        {
            jedis = getJedis();
            if (jedis != null)
            {
                result = jedis.srem(Constant.REDIS_KEY_SUFFIX + key, member);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            close(jedis);
        }
        return result;
    }
    
    /**
     * 判断成员元素是否是集合的成员
     * 
     * @param key
     * @param member
     * @return
     * @Description:
     */
    public static boolean sismember(String key, String member)
    {
        boolean result = false;
        Jedis jedis = null;
        try
        {
            jedis = getJedis();
            if (jedis != null)
            {
                result = jedis.sismember(Constant.REDIS_KEY_SUFFIX + key, member);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            close(jedis);
        }
        return result;
    }
    
    /**
     * 查看sets集合中的所有元素
     * 
     * @param key
     * @return
     * @Description:
     */
    public static Set<String> smembers(String key)
    {
        Set<String> result = null;
        Jedis jedis = null;
        try
        {
            jedis = getJedis();
            if (jedis != null)
            {
                result = jedis.smembers(Constant.REDIS_KEY_SUFFIX + key);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            close(jedis);
        }
        return result;
    }
    
    /**
     * 取出list
     * 
     * @param key
     * @param start
     * @param end -1代表倒数一个元素
     * @return
     * @Description:
     */
    public static List<String> lrange(String key, long start, long end)
    {
        List<String> result = null;
        Jedis jedis = null;
        try
        {
            jedis = getJedis();
            if (jedis != null)
            {
                result = jedis.lrange(Constant.REDIS_KEY_SUFFIX + key, start, end);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            close(jedis);
        }
        return result;
    }
    
    /**
     * 用于在Redis键中设置某些字符串值 (如果该键在Redis中不存在,若存在则无效果，结果还是最开始的值)
     * 
     * @param key
     * @param value
     * @return 如果键成功设置则返回 1,否则0
     * @Description:
     */
    public static long setnx(String key, String value)
    {
        long result = -1;
        Jedis jedis = null;
        try
        {
            jedis = getJedis();
            if (jedis != null)
            {
                result = jedis.setnx(Constant.REDIS_KEY_SUFFIX + key, value);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            close(jedis);
        }
        return result;
    }
    
    /**
     * 用于将键的整数值递增1
     * 
     * @param key
     * @return
     * @Description:
     */
    public static long incr(String key)
    {
        long result = -1;
        Jedis jedis = null;
        try
        {
            jedis = getJedis();
            if (jedis != null)
            {
                result = jedis.incr(Constant.REDIS_KEY_SUFFIX + key);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            close(jedis);
        }
        return result;
    }
    
    /**
     * 获取下一个序列编号
     * 
     * @param key 序列名称
     * @param fix 序列前缀
     * @param dateFormat 序列日期格式
     * @param index 序列起始下标
     * @return 获取下一个序列编号
     * @Description:
     */
    public static String getNextAutoNumber(String key, String fix, String dateFormat, String index)
    {
        String value = null;
        Jedis jedis = null;
        try
        {
            jedis = getJedis();
            if (jedis != null)
            {
                StringBuilder autoNumSB = new StringBuilder();
                SimpleDateFormat myfmt = new SimpleDateFormat(dateFormat);
                String date = myfmt.format(new Date());
                Long i = Long.parseLong(index);
                if (!exists(key))
                {
                    jedis.incrBy(Constant.REDIS_KEY_SUFFIX + key, i);
                }
                else
                {
                    i = incr(key);
                }
                String num = String.format("%0" + index.length() + "d", i);
                autoNumSB.append(fix).append(date).append(num);
                value = autoNumSB.toString();
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            close(jedis);
        }
        return value;
    }
    
    /**
     * 设置过期时间
     * 
     * @param key
     * @param seconds
     * @Description:
     */
    public static void expire(String key, int seconds)
    {
        Jedis jedis = null;
        try
        {
            jedis = getJedis();
            if (jedis != null)
            {
                jedis.expire(Constant.REDIS_KEY_SUFFIX + key, seconds);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            close(jedis);
        }
    }
    
    public static boolean exists(String key)
    {
        boolean result = false;
        Jedis jedis = null;
        try
        {
            jedis = getJedis();
            if (jedis != null)
            {
                result = jedis.exists(Constant.REDIS_KEY_SUFFIX + key);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            close(jedis);
        }
        return result;
    }
    
    public static String getValue(String key)
    {
        String value = null;
        Jedis jedis = null;
        try
        {
            jedis = getJedis();
            if (jedis != null)
            {
                value = jedis.get(Constant.REDIS_KEY_SUFFIX + key);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            close(jedis);
        }
        return value;
    }
    
    public static Map<String, String> hgetAll(String key)
    {
        Map<String, String> result = null;
        Jedis jedis = null;
        try
        {
            jedis = getJedis();
            if (jedis != null)
            {
                result = jedis.hgetAll(Constant.REDIS_KEY_SUFFIX + key);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            close(jedis);
        }
        return result;
    }
    
    public static Object get(String key)
    {
        Object value = null;
        Jedis jedis = null;
        try
        {
            jedis = getJedis();
            if (jedis != null)
            {
                value = SerializableUtils.deserialize(jedis.get((Constant.REDIS_KEY_SUFFIX + key).getBytes()));
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            close(jedis);
        }
        return value;
    }
    
    public static boolean del(String key)
    {
        boolean result = false;
        Jedis jedis = null;
        try
        {
            jedis = getJedis();
            if (jedis != null)
            {
                result = jedis.del(Constant.REDIS_KEY_SUFFIX + key) > 0;
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            close(jedis);
        }
        return result;
    }
    
    private static synchronized Jedis getJedis()
    {
        Jedis jedis = null;
        if (jedisPool == null)
        {
            initPool();
        }
        if (jedisPool != null)
        {
            for (int i = 0; i < Constant.REDIS_MAX_ATTEMPTS; i++)
            {
                try
                {
                    jedis = jedisPool.getResource();
                    jedis.set("test", "1");
                    break;
                }
                catch (JedisConnectionException e1)
                {
                    log.error(e1.getMessage(), e1);
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                    return null;
                }
            }
        }
        
        return jedis;
    }
    
    private static void close(final Jedis jedis)
    {
        if (jedis != null)
        {
            try
            {
                jedis.close();
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
        }
    }
    
    private static synchronized void initPool()
    {
        try
        {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxIdle(Constant.REDIS_MAX_IDLE);
            config.setMaxTotal(Constant.REDIS_MAX_TOTAL);
            config.setTestOnBorrow(Constant.REDIS_TEST_ON_BORROW == 1);
            config.setTestOnReturn(Constant.REDIS_TEST_ON_RETURN == 1);
            
            String host = Constant.REDIS_HOST;
            String password = Constant.REDIS_PASSWORD;
            int port = Constant.REDIS_PORT;
            int timeout = Constant.REDIS_TIMEOUT;
            jedisPool = new JedisPool(config, host, port, timeout, password);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
}