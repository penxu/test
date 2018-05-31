package com.teamface.common.util.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.teamface.common.util.PropertiesConfigObject;

/**
 * @Description: MongoDB操作工具类
 * @author: zhangzhihua
 * @date: 2017年9月27日 上午10:02:31
 * @version: 1.0
 */

public class MongoDBUtil
{
    private static final Logger log = LogManager.getLogger(MongoDBUtil.class);
    
    private static MongoDBUtil instance = null;
    
    private MongoClient client = null;
    
    private MongoDatabase db = null;
    
    public static synchronized MongoDBUtil getInstance(String database)
    {
        if (instance == null)
        {
            instance = new MongoDBUtil(database);
        }
        return instance;
    }
    
    private MongoDBUtil(String database)
    {
        try
        {
            PropertiesConfigObject properties = PropertiesConfigObject.getInstance();
            Configuration config = properties.getConfig();
            
            String user = config.getString("mongoDB.user");
            String password = config.getString("mongoDB.password");
            String address = config.getString("mongoDB.server.address");
            String ip = config.getString("mongoDB.ip");
            int port = config.getInt("mongoDB.port");
            int connectionsPerHost = config.getInt("mongoDB.connectionsPerHost");
            int connectTimeout = config.getInt("mongoDB.connectTimeout");
            int maxWaitTime = config.getInt("mongoDB.maxWaitTime");
            int socketTimeout = config.getInt("mongoDB.socketTimeout");
            int threadsAllowedToBlockForConnectionMultiplier = config.getInt("mongoDB.threadsAllowedToBlockForConnectionMultiplier");
            
            List<ServerAddress> sends = new ArrayList<>();
            if (address != null && address.trim().length() > 0)
            {
                String[] addresses = address.split(",");
                for (String addr : addresses)
                {
                    String[] addrPort = addr.split(":");
                    if (addrPort.length > 1)
                    {
                        sends.add(new ServerAddress(addrPort[0], Integer.parseInt(addrPort[1])));
                    }
                }
            }
            Builder options = new MongoClientOptions.Builder();
            if (connectionsPerHost > 0)
            {
                // 连接池设置为300个连接,默认为100
                options.connectionsPerHost(connectionsPerHost);
            }
            if (connectTimeout > 0)
            {
                // 连接超时，推荐>3000毫秒
                options.connectTimeout(connectTimeout);
            }
            if (maxWaitTime > 0)
            {
                // 连接等待时间
                options.maxWaitTime(maxWaitTime);
            }
            if (socketTimeout > 0)
            {
                // 套接字超时时间，0无限制
                options.socketTimeout(socketTimeout);
            }
            if (threadsAllowedToBlockForConnectionMultiplier > 0)
            {
                // 线程队列数，如果连接线程排满了队列就会抛出"Out of semaphores to get db"错误。
                options.threadsAllowedToBlockForConnectionMultiplier(threadsAllowedToBlockForConnectionMultiplier);
            }
            List<MongoCredential> mongoCredentialList = new ArrayList<>();
            if (user != null && user.length() > 0 && password != null && password.length() > 0 && database.length() > 0)
            {
                mongoCredentialList.add(MongoCredential.createMongoCRCredential(user, database, password.toCharArray()));
            }
            if (!sends.isEmpty() && !mongoCredentialList.isEmpty())
            {
                client = new MongoClient(sends, mongoCredentialList);
            }
            else if (!sends.isEmpty())
            {
                client = new MongoClient(sends);
            }
            else
            {
                client = new MongoClient(ip, port);
            }
            db = client.getDatabase(database);
        }
        catch (Exception e)
        {
            log.error(e, e);
        }
    }
    
    /**
     * 创建collection,可设置索引
     * 
     * @param collName
     * @param indexField
     * @Description:
     */
    public void createCollection(String collName, String indexField)
    {
        if (StringUtils.isEmpty(collName))
        {
            return;
        }
        db.createCollection(collName);
        if (!StringUtils.isEmpty(indexField))
        {
            db.getCollection(collName).createIndex(new BasicDBObject(indexField, 1));
        }
    }
    
    /**
     * 获取collection对象 - 指定Collection
     * 
     * @param collName
     * @return
     */
    public MongoCollection<Document> getCollection(String collName)
    {
        if (collName == null || collName.trim().isEmpty())
        {
            return null;
        }
        return db.getCollection(collName);
    }
    
    /**
     * 查询DB下的所有表名
     */
    public List<String> getAllCollections(String dbName)
    {
        List<String> tables = new ArrayList<>();
        if (dbName != null && dbName.trim().length() > 0)
        {
            MongoIterable<String> colls = db.listCollectionNames();
            for (String coll : colls)
            {
                tables.add(coll);
            }
        }
        return tables;
    }
    
    /**
     * 获取所有数据库名称列表
     * 
     * @return
     */
    public MongoIterable<String> getAllDBNames()
    {
        return client.listDatabaseNames();
    }
    
    /**
     * 查找对象 - 根据主键_id
     * 
     * @param collection
     * @param id
     * @return
     */
    public Document findById(String collName, String id)
    {
        return getCollection(collName).find(Filters.eq("_id", new ObjectId(id))).first();
    }
    
    /** 统计数 */
    public long getCount(String collName)
    {
        return getCollection(collName).count();
    }
    
    /** 条件查询 */
    public MongoCursor<Document> find(String collName, Bson filter)
    {
        return getCollection(collName).find(filter).iterator();
    }
    
    /** 条件查询 */
    public Document find(String collName, Bson filter, Bson sort)
    {
        return getCollection(collName).find(filter).sort(sort).limit(1).first();
    }
    
    /** 条件查询 */
    public List<JSONObject> find4JSONObject(String collName, Bson filter)
    {
        MongoCursor<Document> docs = find(collName, filter);
        List<JSONObject> result = new ArrayList<JSONObject>();
        // 组装json对象
        while (docs.hasNext())
        {
            Document doc = docs.next();
            JSONObject findInfo = JSONObject.parseObject(doc.toJson());
            result.add(findInfo);
        }
        return result;
    }
    
    /** 条件查询 */
    public JSONObject find4FirstJSONObject(String collName, Bson filter)
    {
        MongoCursor<Document> docs = find(collName, filter);
        // 组装json对象
        while (docs.hasNext())
        {
            Document doc = docs.next();
            return JSONObject.parseObject(doc.toJson());
        }
        return null;
    }
    
    /** 分页查询 */
    public MongoCursor<Document> findByPage(String collName, Bson filter, int pageNo, int pageSize)
    {
        return getCollection(collName).find(filter).sort(new BasicDBObject("_id", 1)).skip((pageNo - 1) * pageSize).limit(pageSize).iterator();
    }
    
    /**
     * 通过ID删除
     * 
     * @param coll
     * @param id
     * @return
     */
    public long deleteById(String collName, String id)
    {
        Bson filter = Filters.eq("_id", new ObjectId(id));
        DeleteResult deleteResult = getCollection(collName).deleteOne(filter);
        return deleteResult.getDeletedCount();
    }
    
    /**
     * 更新文档
     * 
     * @param coll
     * @param id
     * @param newdoc
     * @return
     */
    public Document updateById(String collName, String id, Document newdoc)
    {
        Bson filter = Filters.eq("_id", new ObjectId(id));
        // coll.replaceOne(filter, newdoc); // 完全替代
        getCollection(collName).updateOne(filter, new Document("$set", newdoc));
        return newdoc;
    }
    
    /**
     * 查询集合所有文档
     * 
     * @param dbName
     * @param collName
     * @return
     */
    public List<Document> findAll(String collName)
    {
        List<Document> results = new ArrayList<>();
        FindIterable<Document> iterables = db.getCollection(collName).find();
        MongoCursor<Document> cursor = iterables.iterator();
        while (cursor.hasNext())
        {
            results.add(cursor.next());
        }
        return results;
    }
    
    /**
     * 插入一个文档
     * 
     * @param dbName
     * @param collName
     * @param document
     */
    public void insert(String collName, Document document)
        throws DataAccessException
    {
        try
        {
            db.getCollection(collName).insertOne(document);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            new SQLStateSQLExceptionTranslator().translate("PreparedStatementCallback", " mongoDB insert", new SQLException());
        }
        
    }
    
    /**
     * 根据条件删除一个文档
     * 
     * @param dbName
     * @param collName
     * @param filter
     */
    public void deleteOne(String collName, Bson filter)
    {
        db.getCollection(collName).deleteOne(filter);
    }
    
    /**
     * 根据条件删除多个文档
     * 
     * @param dbName
     * @param collName
     * @param filter
     */
    public void deleteMany(String collName, Bson filter)
    {
        db.getCollection(collName).deleteMany(filter);
    }
    
    /**
     * 关闭Mongodb
     */
    public void close()
    {
        if (client != null)
        {
            client.close();
            client = null;
            instance = null;
        }
    }
}
