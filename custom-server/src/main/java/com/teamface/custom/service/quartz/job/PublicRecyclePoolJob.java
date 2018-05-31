package com.teamface.custom.service.quartz.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.Constant;
import com.teamface.common.util.dao.DAOUtil;

/**
 * 
 * @Title:定时器
 * @Description: 自动标识颜色
 * @Author:liupan
 * @Since:2017年11月27日
 * @Version:1.1.0
 */

public class PublicRecyclePoolJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		String companyId = dataMap.getString("companyId");
		String ruleName = DAOUtil.getTableName("rule_colour", companyId);
		String sql = "select * from " + ruleName + "    order by id asc";
		List<JSONObject> jsonObject = DAOUtil.executeQuery4JSON(sql);
		if (!jsonObject.isEmpty()) {
			for (int i = 0; i < jsonObject.size(); i++) {
				JSONObject data = jsonObject.get(i);
				if (data.getInteger("status") == Constant.CURRENCY_ONE) { // 判断规则是否删除
					String colourCenterTable = DAOUtil.getTableName("module_colour_center", companyId);
					StringBuilder delBuilder = new StringBuilder();// 删除设置生成的数据
					delBuilder.append("delete from ").append(colourCenterTable).append(" where rule_colour_id = ")
							.append(data.getLong("id"));
					int sum = DAOUtil.executeUpdate(delBuilder.toString());
				} else {
					    // 规则条件 0任何 1选配
					    String beanName = DAOUtil.getTableName(data.getString("bean"), companyId);
						if (data.getInteger("condition") == 0) {
							sql = "select * from " + beanName;
							List<JSONObject> jsonList = DAOUtil.executeQuery4JSON(sql);
							// 规则分配数据颜色
							commonAllot(jsonList, data, companyId);

						} else {

							String commSql = data.getString("query_condition");
							commSql = commSql.replace(Constant.VAR_QUOTES, "'");

							// 拼装sql 查询是否又符合语句
							if (StringUtils.isNotBlank(commSql) && !commSql.trim().equals("where")) {
								sql = "select *  from " + beanName + " where " + commSql;
								List<JSONObject> jsonList = DAOUtil.executeQuery4JSON(sql);
								if (!jsonList.isEmpty() && jsonList.size() > 0) {
									// 规则分配数据颜色
									commonAllot(jsonList, data, companyId);
						     }
						}
					}
				}
			}
		}
	}

	/**
	 * 规则分配数据颜色
	 * 
	 * @param jsonList
	 * @param data
	 * @param info
	 * @Description:
	 */
	private void commonAllot(List<JSONObject> jsonList, JSONObject data, String companyId) {
		// 规则分配数据颜色
		String colourName = DAOUtil.getTableName("module_colour_center", companyId);
		List<List<Object>> batchValue = new ArrayList<>();
		List<List<Object>> batchValues = new ArrayList<>();
		for (int i = 0; i < jsonList.size(); i++) {
			String sql = "select count(*) from " + colourName + " where bean='" + data.getString("bean")
					+ "' and data_id=" + jsonList.get(i).getLong("id");
			int count = DAOUtil.executeCount(sql);
			if (count <= 0) { // 匹配数据是否替换
				List<Object> model = new ArrayList<>();
				model.add(data.getLong("id")); // 规则ID
				model.add(data.getString("bean")); // bean
				model.add(data.getString("colour")); // 颜色
				model.add(jsonList.get(i).getLongValue("id")); // 数据id
				batchValue.add(model);
			}else {
				List<Object> model = new ArrayList<>();
				model.add(data.getString("colour")); // 颜色
				model.add(data.getString("bean"));// bean
				model.add(jsonList.get(i).getLongValue("id")); // 数据id
				batchValues.add(model);
			}

		}
		if (!batchValue.isEmpty()) {
			StringBuilder insertSql = new StringBuilder();
			insertSql.append("insert into ");
			insertSql.append(colourName);
			insertSql.append(" (rule_colour_id,bean,colour,data_id) values(?,?,?,?)");
			DAOUtil.executeUpdate(insertSql.toString(), batchValue, 1000);
		}
		if (!batchValues.isEmpty()) {
			StringBuilder editBuilder = new StringBuilder();
			editBuilder.append("update  ").append(colourName).append(" set colour = ?  where bean=?  and data_id = ?");
			DAOUtil.executeUpdate(editBuilder.toString(), batchValues, 1000);
		}

	}

}
