package com.teamface.custom.service.im.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.Constant;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.JsonResUtil;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JSONParser4SQL;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.model.ImCircleComment;
import com.teamface.custom.model.ImCircleMain;
import com.teamface.custom.model.ImCirclePhoto;
import com.teamface.custom.model.ImCircleUp;
import com.teamface.custom.model.PageVo;
import com.teamface.custom.service.employee.EmployeeAppService;
import com.teamface.custom.service.im.ImCircleAppService;
import com.teamface.custom.service.push.MessagePushService;

/**
 * 
 * @Title:
 * @Description:同事圈列表实现类
 * @Author:dsl
 * @Since:2017年11月21日
 * @Version:1.1.0
 */
@Service("imCircleAppService")
public class ImCircleAppServiceImpl implements ImCircleAppService
{
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    public static final char UNDERLINE = '_';
    
    @Autowired
    MessagePushService messagePushService;
    
    @Autowired
    public EmployeeAppService employeeAppService;
    
    @Override
    public ServiceResult<String> imCircleAdd(String token, ImCircleMain imCircleMain)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long employeeId = info.getEmployeeId();
        Long companyId = info.getCompanyId();
        Long signId = info.getSignId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        ImCircleMain circleMainLater = new ImCircleMain();
        List<Object[]> objList = new ArrayList<>();
        if (imCircleMain != null)
        {
            imCircleMain.setFromId(employeeId);
            imCircleMain.setCompanyId(companyId);
            imCircleMain.setCreateDate(System.currentTimeMillis());
            String contentInfo = imCircleMain.getInfo().replace("'", "''");
            JSONObject data = new JSONObject();
            data.put("from_id", imCircleMain.getFromId());
            if (null == imCircleMain.getAddress())
                data.put("address", "");
            else
                data.put("address", imCircleMain.getAddress());
            if (null == imCircleMain.getLongitude())
                data.put("longitude", "");
            else
                data.put("longitude", imCircleMain.getLongitude());
            if (null == imCircleMain.getLatitude())
                data.put("latitude", "");
            else
                data.put("latitude", imCircleMain.getLatitude());
                
            data.put("info", contentInfo);
            data.put("datetime_create_date", imCircleMain.getCreateDate());
            data.put("is_delete", '0');
            JSONObject obj = new JSONObject();
            obj.put("bean", "im_circle_main");
            obj.put("data", data);
            String insertSql = JSONParser4SQL.getInsertSql(obj, companyId == null ? "" : String.valueOf(companyId));
            int sum = DAOUtil.executeUpdate(insertSql); // 添加同事信息
            if (sum <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            long circleMainId = BusinessDAOUtil.geCurrval4Table("im_circle_main", companyId.toString());
            List<ImCirclePhoto> images = imCircleMain.getImages();
            JSONObject photoJson = new JSONObject();
            photoJson.put("bean", "im_circle_photo");
            long currentTime = System.currentTimeMillis();
            if (images != null && images.size() > 0)
            {
                for (ImCirclePhoto imCirclePhoto : images)
                {
                    List<Object> circleData = new ArrayList<>();
                    imCirclePhoto.setCircle_main_id(circleMainLater.getId());
                    imCirclePhoto.setUpload_time(currentTime);
                    circleData.add(circleMainId);
                    circleData.add(imCirclePhoto.getFile_url());
                    circleData.add(currentTime);
                    circleData.add(imCirclePhoto.getFile_name());
                    circleData.add(imCirclePhoto.getFile_size());
                    circleData.add(imCirclePhoto.getFile_size());
                    objList.add(circleData.toArray());
                    
                }
                // 插入头像数据
                StringBuilder insertPhotoSB = new StringBuilder().append("insert into im_circle_photo_")
                    .append(companyId)
                    .append(" (circle_main_id, file_url, datetime_upload_time,file_name,file_size,file_type) values(?, ?, ?, ?, ?, ?)");
                int res = DAOUtil.executeBatchUpdate(insertPhotoSB.toString(), objList);
                if (res <= 0)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                    return serviceResult;
                }
            }
            List<Long> peoples = imCircleMain.getPeoples();
            String content = imCircleMain.getInfo();
            String employeeName = queryEmployeeNameBySignId(signId, companyId);
            String pushContent = alertTemplate(employeeName, content);
            // 获取当前公司的助手企信小助手的ID
            StringBuilder queryAssistantSqlSB = new StringBuilder().append("select id from im_assistant where company_id = ").append(companyId).append(" and type = 2");
            JSONObject assistantObj = DAOUtil.executeQuery4FirstJSON(queryAssistantSqlSB.toString());
            Long assistantId = 0l;
            JSONObject pushContentObj = new JSONObject();
            pushContentObj.put("type", Constant.PUSH_MESSAGE_CIRCLE);
            pushContentObj.put("push_content", pushContent);
            pushContentObj.put("create_time", System.currentTimeMillis());
            if (null != assistantObj)
            {
                assistantId = assistantObj.getLongValue("id");
                pushContentObj.put("assistant_id", assistantId);
            }
            StringBuilder contentInsertSqlSB = new StringBuilder().append("insert into push_message_content_")
                .append(companyId)
                .append(" (assistant_id,push_content,bean_name,bean_name_chinese,datetime_create_time,type) values(")
                .append(assistantId)
                .append(",'")
                .append(pushContent)
                .append("','")
                .append("")
                .append("','")
                .append("")
                .append("',")
                .append(System.currentTimeMillis())
                .append(",")
                .append(6)
                .append(")");
            DAOUtil.executeUpdate(contentInsertSqlSB.toString());
            messagePushService.pushCircleMessage(pushContent, peoples, companyId);
            
        }
        return serviceResult;
        
    }
    
    @Override
    public ServiceResult<String> imCircleUp(String token, Long circleMainId)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long employeeId = info.getEmployeeId();
        Long companyId = info.getCompanyId();
        
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        List<JSONObject> objArr = new ArrayList<>();
        // 1.查询是否有点赞记录
        StringBuilder querySql = new StringBuilder("select id,employee_id,circle_main_id from im_circle_up_" + companyId);
        if (circleMainId != null && employeeId != null)
        {
            querySql.append(" where ");
            if (circleMainId != null)
            {
                querySql.append(" circle_main_id = " + circleMainId);
            }
            if (circleMainId != null)
            {
                querySql.append(" and employee_id = " + employeeId);
            }
        }
        querySql.append(";");
        objArr = DAOUtil.executeQuery4JSON(querySql.toString());
        if (objArr.size() > 0)
        {
            // 2.如果有则删除
            StringBuilder deleteSqlSB = new StringBuilder().append("delete from im_circle_up_")
                .append(companyId)
                .append(" where circle_main_id = ")
                .append(circleMainId)
                .append(" and employee_id = ")
                .append(employeeId);
            int res = DAOUtil.executeUpdate(deleteSqlSB.toString());
            if (res <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
        }
        else
        {
            // 3.没有则添加点赞信息
            JSONObject obj = new JSONObject();
            obj.put("bean", "im_circle_up");
            ImCircleUp circleUp = new ImCircleUp();
            circleUp.setCircleMainId(circleMainId);
            circleUp.setEmployeeId(employeeId);
            JSONObject dataObj = new JSONObject();
            dataObj.put("circle_main_id", circleUp.getCircleMainId());
            dataObj.put("employee_id", circleUp.getEmployeeId());
            obj.put("data", dataObj);
            String insertSql = JSONParser4SQL.getInsertSql(obj, companyId == null ? "" : String.valueOf(companyId));
            int res = DAOUtil.executeUpdate(insertSql);
            if (res <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
        }
        return serviceResult;
        
    }
    
    @Override
    public JSONObject imCircleComment(String token, Long circleMainId, String contentInfo, Long receiverId)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long employeeId = info.getEmployeeId();
        Long companyId = info.getCompanyId();
        JSONObject jsonObject = new JSONObject();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        long createTime = System.currentTimeMillis();
        String content = contentInfo.replace("'", "''");
        JSONObject dataObj = new JSONObject();
        dataObj.put("sender_id", employeeId);
        dataObj.put("content_info", content);
        dataObj.put("circle_main_id", circleMainId);
        dataObj.put("datetime_create_date", createTime);
        if (null != receiverId)
        {
            dataObj.put("receiver_id", receiverId);
        }
        JSONObject obj = new JSONObject();
        obj.put("bean", "im_circle_comment");
        obj.put("data", dataObj);
        // 添加评论信息
        String insertSql = JSONParser4SQL.getInsertSql(obj, companyId == null ? "" : String.valueOf(companyId));
        int result = DAOUtil.executeUpdate(insertSql);
        if (result <= 0)
        {
            return JsonResUtil.getFailJsonObject();
        }
        long commentId = BusinessDAOUtil.geCurrval4Table("im_circle_comment", companyId.toString());
        jsonObject.put("commentId", commentId);
        jsonObject.put("contentinfo", content);
        // 查询员工信息
        JSONObject employeeObj = employeeAppService.queryEmployee(employeeId, companyId);
        if (employeeObj != null)
        {
            jsonObject.put("senderName", employeeObj.get("employee_name"));
            jsonObject.put("senderPhotograph", employeeObj.get("picture"));
            jsonObject.put("senderId", employeeId);
        }
        else
        {
            jsonObject.put("senderName", "");
            jsonObject.put("senderPhotograph", "");
            jsonObject.put("senderId", "");
        }
        // 查询员工信息
        JSONObject receiverObj = employeeAppService.queryEmployee(receiverId, companyId);
        if (receiverObj != null)
        {
            jsonObject.put("receiverName", receiverObj.get("employee_name"));
            jsonObject.put("receiverPhotograph", receiverObj.get("picture"));
            jsonObject.put("receiverId", receiverId);
        }
        else
        {
            jsonObject.put("receiverName", "");
            jsonObject.put("receiverPhotograph", "");
            jsonObject.put("receiverId", "");
        }
        jsonObject.put("createDate", createTime);
        return JsonResUtil.getSuccessJsonObject(jsonObject);
        
    }
    
    @Override
    public ServiceResult<String> imCircleDelete(String token, Long circleMainId)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        if (circleMainId != null)
        {
            ImCircleMain circleMain = new ImCircleMain();
            circleMain.setId(circleMainId);
            // 删除同事圈点赞信息
            StringBuilder circleMainSqlSB = new StringBuilder().append("delete from im_circle_main_").append(companyId).append(" where id = ").append(circleMainId);
            int res = DAOUtil.executeUpdate(circleMainSqlSB.toString());
            if (res <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            // 删除同事圈点赞信息
            StringBuilder circleUpSqlSB = new StringBuilder().append("delete from im_circle_up_").append(companyId).append(" where circle_main_id = ").append(circleMainId);
            DAOUtil.executeUpdate(circleUpSqlSB.toString());
            // 删除同事圈头像信息
            StringBuilder circlePhotoSqlSB = new StringBuilder().append("delete from im_circle_photo_").append(companyId).append(" where id = ").append(circleMainId);
            DAOUtil.executeUpdate(circlePhotoSqlSB.toString());
            // 删除同事圈评论信息
            StringBuilder circleCommentSqlSB =
                new StringBuilder().append("delete from im_circle_comment_").append(companyId).append(" where circle_main_id = ").append(circleMainId);
            DAOUtil.executeUpdate(circleCommentSqlSB.toString());
        }
        return serviceResult;
        
    }
    
    @Override
    public ServiceResult<String> imCircleCommentDelete(String token, Long commentId)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        if (commentId != null)
        {
            StringBuilder circleCommentSqlSB = new StringBuilder().append("delete from im_circle_comment_").append(companyId).append(" where id = ").append(commentId);
            int res = DAOUtil.executeUpdate(circleCommentSqlSB.toString());
            if (res <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
        }
        return serviceResult;
        
    }
    
    @Override
    public ServiceResult<String> backGroundMod(String token, String url)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long employeeId = info.getEmployeeId();
        Long companyId = info.getCompanyId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        Map<String, Object> paramMap = new HashMap<String, Object>();
        Integer count = 0;
        paramMap.put("employeeMainId", employeeId);
        // 查询员工信息
        JSONObject empObj = employeeAppService.queryEmployee(employeeId, companyId);
        if (empObj != null)
        {
            StringBuilder sqlSB = new StringBuilder().append("update employee_").append(companyId).append(" set microblog_background='").append(url).append("'");
            count = DAOUtil.executeCount(sqlSB.toString());
        }
        if ((count + "").equals("0"))
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        return serviceResult;
        
    }
    
    /**
     * 查找个人的企业圈或者是全公司的
     */
    @Override
    public PageVo<Map<String, Object>> imCircleList(String token, Long isPerson, Long startTime, Long endTime, Integer pageNo, Integer pageSize)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        List<Map<String, Object>> result = new ArrayList<>();
        int offset = (pageNo - 1) * pageSize;
        String applicationTable = DAOUtil.getTableName("im_circle_main", companyId);
        StringBuilder countSqlSB = new StringBuilder();
        StringBuilder querySqlSB = new StringBuilder();
        countSqlSB.append("select count(*) from ");
        querySqlSB.append(
            "SELECT icm.ID,icm.from_id,icm.address,icm.longitude,icm.latitude,icm.info,icm.datetime_create_date,icm.is_delete,e.employee_name as \"employeeName\",e.id as \"employeeId\",e.picture as photograph FROM ");
        countSqlSB.append(applicationTable);
        querySqlSB.append(applicationTable).append(" icm left join employee_").append(companyId).append(" e on icm.from_id = e.id");
        if (isPerson != null)
        {
            countSqlSB.append(" where from_id = ");
            countSqlSB.append(isPerson);
            querySqlSB.append(" where icm.from_id = ");
            querySqlSB.append(isPerson);
        }
        countSqlSB.append(";");
        querySqlSB.append(" ORDER BY icm.datetime_create_date DESC OFFSET ");
        querySqlSB.append(offset);
        querySqlSB.append(" limit ");
        querySqlSB.append(pageSize);
        querySqlSB.append(";");
        int num = DAOUtil.executeCount(countSqlSB.toString());
        int pages = num / pageSize + 1;
        result = DAOUtil.executeQuery(querySqlSB.toString());
        List<Map<String, Object>> resultTransfer = new ArrayList<>();
        for (Map<String, Object> map : result)
        {
            Map<String, Object> tem = transferMap(map);
            resultTransfer.add(tem);
        }
        PageVo<Map<String, Object>> pageVo = new PageVo<>(resultTransfer);
        pageVo.setCurPageSize(result.size());
        pageVo.setPageNum(pageNo);
        pageVo.setPageSize(pageSize);
        pageVo.setTotalPages(pages);
        pageVo.setTotalRows((long)num);
        return pageVo;
    }
    
    @Override
    public List<Map<String, Object>> findLastPhoto(String token, Map<String, Object> paramMap)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        List<Map<String, Object>> circlePhotos = new ArrayList<>();
        StringBuilder querySqlSB = new StringBuilder().append("SELECT a.file_url,a.file_size,a.file_type,a.datetime_upload_time,a.id FROM im_circle_photo_")
            .append(companyId)
            .append(" a LEFT JOIN im_circle_main_")
            .append(companyId)
            .append(" b ON a.circle_main_id = b.id WHERE b.from_id=")
            .append(paramMap.get("fromId"))
            .append(" ORDER BY a.datetime_upload_time DESC limit 4");
        circlePhotos = DAOUtil.executeQuery(querySqlSB.toString());
        
        return circlePhotos;
    }
    
    @Override
    public List<Map<String, Object>> findVoListPhoto(String token, Map<String, Object> paramMap)
    {
        List<Map<String, Object>> circleMap = new ArrayList<>();
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        StringBuilder querySqlSB = new StringBuilder().append("SELECT a.file_url,a.file_size,a.file_type,a.datetime_upload_time,a.id FROM im_circle_photo_")
            .append(companyId)
            .append(" a WHERE a.circle_main_id=")
            .append(paramMap.get("circleMainId"));
        circleMap = DAOUtil.executeQuery(querySqlSB.toString());
        return circleMap;
    }
    
    @Override
    public List<ImCircleComment> findVoListComment(String token, Map<String, Object> paramMap)
    {
        List<ImCircleComment> imCircleComments = new ArrayList<ImCircleComment>();
        List<Map<String, Object>> imCircles = new ArrayList<>();
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        StringBuilder querySqlSB =
            new StringBuilder().append("select * from im_circle_comment_").append(companyId).append(" where circle_main_id = ").append(paramMap.get("circleMainId"));
        imCircles = DAOUtil.executeQuery(querySqlSB.toString());
        for (Map<String, Object> map : imCircles)
        {
            ImCircleComment imCircle = new ImCircleComment();
            imCircle.setCircleMainId(Long.parseLong(String.valueOf(map.get("circle_main_id"))));
            imCircle.setSenderId((Long)map.get("sender_id"));
            imCircle.setContentInfo((String)map.get("content_info"));
            imCircle.setReceiverId(null == map.get("receiver_id") ? null : Long.valueOf(String.valueOf(map.get("receiver_id"))));
            imCircle.setCreateDate((Long)map.get("datetime_create_date"));
            imCircle.setId(Long.parseLong(String.valueOf(map.get("id"))));
            imCircleComments.add(imCircle);
        }
        return imCircleComments;
    }
    
    @Override
    public List<ImCircleUp> findVoListUp(String token, Map<String, Object> paramMap)
    {
        List<ImCircleUp> ImCircleUps = new ArrayList<>();
        List<Map<String, Object>> imCircleUpMaps = new ArrayList<>();
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        StringBuilder querySqlSB =
            new StringBuilder().append("select * from im_circle_up_").append(companyId).append(" where circle_main_id =").append(paramMap.get("circleMainId"));
        imCircleUpMaps = DAOUtil.executeQuery(querySqlSB.toString());
        for (Map<String, Object> map : imCircleUpMaps)
        {
            ImCircleUp imCircle = new ImCircleUp();
            imCircle.setCircleMainId(Long.parseLong(String.valueOf(map.get("circle_main_id"))));
            
            imCircle.setEmployeeId(Long.parseLong(String.valueOf(map.get("employee_id"))));
            ImCircleUps.add(imCircle);
        }
        return ImCircleUps;
    }
    
    /**
     * 
     * @param mapBefore
     * @return
     * @Description:将map中的数据中的下划线数据转换成驼峰结构
     */
    Map<String, Object> transferMap(Map<String, Object> mapBefore)
    {
        Map<String, Object> mapTem = new HashMap<>();
        Set<Entry<String, Object>> entrySet = mapBefore.entrySet();
        for (Entry<String, Object> entry : entrySet)
        {
            String key = entry.getKey();
            String transferKey = underlineToCamel(key);
            mapTem.put(transferKey, entry.getValue());
            
        }
        return mapTem;
    }
    
    /**
     * 
     * @param param
     * @return
     * @Description:下划线转驼峰结构
     */
    String underlineToCamel(String param)
    {
        if (param == null || "".equals(param.trim()))
        {
            return "";
        }
        int len = param.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
        {
            char c = param.charAt(i);
            if (c == UNDERLINE)
            {
                if (++i < len)
                {
                    sb.append(Character.toUpperCase(param.charAt(i)));
                }
            }
            else
            {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    
    private String alertTemplate(String employeeName, String content)
    {
        StringBuilder template = new StringBuilder().append(employeeName).append(" 发表新动态：").append(content);
        return template.toString();
    }
    
    private String queryEmployeeNameBySignId(Long signId, Long companyId)
    {
        StringBuilder queryEmployeeNameSqlSB = new StringBuilder().append("select e.employee_name from employee_")
            .append(companyId)
            .append(" e join (select * from acountinfo a where a.id = ")
            .append(signId)
            .append(") ac on ac.employee_id = e.id ");
        JSONObject empObj = DAOUtil.executeQuery4FirstJSON(queryEmployeeNameSqlSB.toString());
        return empObj.getString("employee_name");
    }
    
    @Override
    public PageVo<Map<String, Object>> getAllInfo(String token, Long isPerson, Long startTime, Long endTime, Integer pageNo, Integer pageSize)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        PageVo<Map<String, Object>> pageVo = imCircleList(token, isPerson, startTime, endTime, pageNo, pageSize);
        List<Map<String, Object>> mapList = pageVo.getList();
        List<Map<String, Object>> circlePhotos = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> comments = new ArrayList<>();
        List<Map<String, Object>> circleUps = new ArrayList<>();
        StringBuilder circleMainIds = new StringBuilder();
        if (mapList.size() > 0)
        {
            for (Map<String, Object> vo : mapList)
            {
                circleMainIds.append(circleMainIds.length() > 0 ? "," : "").append(vo.get("id"));
            }
            // 查看照片
            
            circlePhotos = findListPhoto(token, circleMainIds.toString());
            comments = findListComment(companyId, circleMainIds.toString());
            circleUps = findListUp(companyId, circleMainIds.toString());
            for (Map<String, Object> vo : mapList)
            {
                List<Map<String, Object>> temp = new ArrayList<>();
                for (int i = 0; i < circlePhotos.size(); i++)
                {
                    if (vo.get("id").equals(circlePhotos.get(i).get("circle_main_id")))
                    {
                        temp.add(circlePhotos.get(i));
                    }
                }
                vo.put("images", temp);
                temp = new ArrayList<>();
                for (int i = 0; i < comments.size(); i++)
                {
                    if (vo.get("id").equals(comments.get(i).get("circleMainId")))
                    {
                        temp.add(comments.get(i));
                    }
                }
                // 查看评论
                vo.put("commentList", temp);
                temp = new ArrayList<>();
                for (int i = 0; i < circleUps.size(); i++)
                {
                    if (vo.get("id").equals(circleUps.get(i).get("circleMainId")))
                    {
                        temp.add(circleUps.get(i));
                    }
                }
                // 点赞的数据
                vo.put("praiseList", temp);
                if (temp != null && temp.size() > 0)
                {
                    vo.put("isPraise", 1);
                }
                else
                {
                    vo.put("isPraise", 0);
                }
            }
            
        }
        return pageVo;
    }
    
    public List<Map<String, Object>> findListComment(Long companyId, String circleMainIds)
    {
        List<Map<String, Object>> imCircleComments = new ArrayList<>();
        StringBuilder querySqlSB = new StringBuilder().append(
            "SELECT icc.id as \"commentId\",icc.circle_main_id as \"circleMainId\",icc.content_info as contentinfo,icc.datetime_create_date as \"createDate\",es.id as \"senderId\",es.employee_name as \"senderName\",es.picture as \"senderPhotograph\",er.id as \"receiverId\",er.employee_name as \"receiverName\",er.picture as \"receiverPhotograph\" FROM im_circle_comment_");
        querySqlSB.append(companyId).append(" icc left join employee_").append(companyId).append(" es on icc.sender_id = es.id ");
        querySqlSB.append(" left join employee_").append(companyId).append(" er on icc.receiver_id = er.id WHERE icc.circle_main_id in (");
        querySqlSB.append(circleMainIds).append(") order by icc.datetime_create_date desc");
        imCircleComments = DAOUtil.executeQuery(querySqlSB.toString());
        return imCircleComments;
    }
    
    public List<Map<String, Object>> findListUp(Long companyId, String circleMainIds)
    {
        List<Map<String, Object>> imCircleUpMaps = new ArrayList<>();
        StringBuilder querySqlSB = new StringBuilder()
            .append(
                "SELECT icu.circle_main_id as \"circleMainId\",icu.employee_id as \"employeeId\",e.employee_name as \"employeeName\",e.picture as \"photograph\" from im_circle_up_")
            .append(companyId);
        querySqlSB.append(" icu left join employee_").append(companyId).append(" e on icu.employee_id = e.id");
        querySqlSB.append(" where icu.circle_main_id in (").append(circleMainIds).append(")");
        imCircleUpMaps = DAOUtil.executeQuery(querySqlSB.toString());
        return imCircleUpMaps;
    }
    
    public List<Map<String, Object>> findListPhoto(String token, String circleMainIds)
    {
        List<Map<String, Object>> circleMap = new ArrayList<>();
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        StringBuilder querySqlSB = new StringBuilder().append("SELECT a.circle_main_id,a.file_url,a.file_size,a.file_type,a.datetime_upload_time,a.id FROM im_circle_photo_")
            .append(companyId)
            .append(" a WHERE a.circle_main_id in (")
            .append(circleMainIds)
            .append(")");
        circleMap = DAOUtil.executeQuery(querySqlSB.toString());
        return circleMap;
    }
}
