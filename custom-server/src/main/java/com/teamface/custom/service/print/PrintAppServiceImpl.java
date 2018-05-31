package com.teamface.custom.service.print;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.Constant;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.FilePreviewUtil;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JSONParser4SQL;
import com.teamface.common.util.dao.LayoutUtil;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;

/**
 * 
 * @Title:
 * @Description:打印设置service实现类
 * @Author:liupan
 * @Since:2018年3月6日
 * @Version:1.1.0
 */
@Service("printAppService")
public class PrintAppServiceImpl implements PrintAppService {

	private static final Logger LOG = LogManager.getLogger(PrintAppServiceImpl.class);

	private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();

	/**
	 * 添加打印设置
	 */
	@Override
	public ServiceResult<String> sava(Map<String, Object> map) {
		ServiceResult<String> serviceResult = new ServiceResult<>();
		try {
			StringBuilder buf = new StringBuilder();
			InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
			String printTable = DAOUtil.getTableName("bean_print", info.getCompanyId());
			buf.append("insert into ");
			buf.append(printTable);
			buf.append("(bean,print_type,template_name,url,create_by,create_time,remark) values");
			buf.append("('");
			buf.append(map.get("bean"));
			buf.append("','");
			buf.append(map.get("type"));
			buf.append("',");
			if (null == map.get("template_name")) {
				buf.append("null");
			} else {
				buf.append("'").append(map.get("template_name").toString().replace("'", "''")).append("'");
			}
			buf.append(",'");
			buf.append(map.get("url"));
			buf.append("',");
			buf.append(info.getEmployeeId());
			buf.append(",");
			buf.append(System.currentTimeMillis());
			buf.append(",'");
			buf.append(map.get("remark") + "')");
			int count = DAOUtil.executeUpdate(buf.toString());
			if (count <= 0) {
				serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
				return serviceResult;
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
		return serviceResult;
	}

	/**
	 * 编辑打印设置
	 */
	@Override
	public ServiceResult<String> edit(Map<String, Object> map) {
		ServiceResult<String> serviceResult = new ServiceResult<>();
		try {
			StringBuilder buf = new StringBuilder();
			InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
			String printTable = DAOUtil.getTableName("bean_print", info.getCompanyId());
			buf.append("update   ");
			buf.append(printTable);
			buf.append(" set  ");
			buf.append("bean = '");
			buf.append(map.get("bean"));
			buf.append("',print_type='");
			buf.append(map.get("type"));
			buf.append("',template_name=");
			if (null == map.get("template_name")) {
				buf.append("null");
			} else {
				buf.append("'").append(map.get("template_name").toString().replace("'", "''")).append("'");
			}
			buf.append(",url='");
			buf.append(map.get("url"));
			buf.append("',create_by = ");
			buf.append(info.getEmployeeId());
			buf.append(",create_time = '");
			buf.append(System.currentTimeMillis());
			buf.append("',remark = '");
			buf.append(map.get("remark"));
			buf.append("' where id = ");
			buf.append(map.get("id"));
			int count = DAOUtil.executeUpdate(buf.toString());
			if (count <= 0) {
				serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
				return serviceResult;
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
		return serviceResult;
	}

	/**
	 * 删除打印设置
	 */
	@Override
	public ServiceResult<String> delete(Map<String, String> map) {
		ServiceResult<String> serviceResult = new ServiceResult<>();
		try {
			StringBuilder buf = new StringBuilder();
			InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
			String printTable = DAOUtil.getTableName("bean_print", info.getCompanyId());
			buf.append("update   ").append(printTable).append(" set  ").append("del_status =")
					.append(Constant.CURRENCY_ONE).append(" where id in (").append(map.get("id")).append(")");
			int count = DAOUtil.executeUpdate(buf.toString());
			if (count <= 0) {
				serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
				return serviceResult;
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
		return serviceResult;
	}

	/**
	 * 根据ID获取打印设置信息
	 */
	@Override
	public JSONObject queryById(Map<String, String> map) {
		JSONObject jsonObject = new JSONObject();
		try {
			StringBuilder buf = new StringBuilder();
			InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
			String printTable = DAOUtil.getTableName("bean_print", info.getCompanyId());
			buf.append("select * from ").append(printTable).append(" where id = ").append(map.get("id"))
					.append(" and del_status =").append(Constant.CURRENCY_ZERO);
			jsonObject = DAOUtil.executeQuery4FirstJSON(buf.toString(),null);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return jsonObject;
	}

	/**
	 * 打印设置列表
	 */
	@Override
	public JSONObject queryList(Map<String, String> map) {
		StringBuilder buf = new StringBuilder();
		JSONObject result = new JSONObject();
		try {
			InfoVo info = TokenMgr.obtainInfo(map.get("token"));
			String printTable = DAOUtil.getTableName("bean_print", info.getCompanyId());
			buf.append("select id,template_name,remark,url from ").append(printTable).append(" where del_status =")
					.append(Constant.CURRENCY_ZERO).append(" and bean = '").append(map.get("bean")).append("'")
					.append(" order by id desc");
			int pageSize = Integer.parseInt(map.get("pageSize"));
			int pageNum = Integer.parseInt(map.get("pageNum"));
			List<String> fields   = new ArrayList<>();
			fields.add("url");
			result = BusinessDAOUtil.getTableDataListAndPageInfo(buf.toString(), pageSize, pageNum,fields);
		} catch (NumberFormatException e) {
			LOG.error(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * 选择版本打印替换预览
	 */
	@Override
	public JSONObject preview(Map<String, String> map) {
		JSONObject jsonReult = new JSONObject();
		StringBuilder buf = new StringBuilder();
		StringBuilder updateBuf = new StringBuilder();
		try {
			InfoVo info = TokenMgr.obtainInfo(map.get("token"));
			String printTable = DAOUtil.getTableName("bean_print", info.getCompanyId());
			// String beanTable = DAOUtil.getTableName(map.get("bean"),
			// info.getCompanyId());
			// 获取文件模版路径
			buf.append("select url from ").append(printTable).append(" where  id  = ").append(map.get("template_id"))
					.append("  and del_status =").append(Constant.CURRENCY_ZERO);
			Object object = DAOUtil.executeQuery4Object(buf.toString());

			JSONObject jsonResult = JSONObject.parseObject(object.toString());

			// 获取查询sql字段名
			Map<String, Object> commentMap = JSONParser4SQL.getFields(info.getCompanyId().toString(),
					map.get("bean").toString(), "0", "0", true);
			List<String> stringList = (List<String>) commentMap.get("fields");
			// 条件
			String str = "{\"where\":{\"id\":" + map.get("id") + "}}";
			JSONObject json = JSONObject.parseObject(str);
			String querySql = JSONParser4SQL.getSimpleQuerySql(info.getCompanyId().toString(),
					map.get("bean").toString(), stringList, json);
			JSONObject jsonObject = DAOUtil.executeQuery4FirstJSON(querySql);

			String url = commonPingjie(jsonResult.getString("file_url"), jsonObject, info, map.get("bean"));

			commonSql(map.get("bean"), info, map.get("id"), url);

			DAOUtil.executeUpdate(updateBuf.toString());

			String strReult = FilePreviewUtil.convertExceltoHtml(url);
			strReult = strReult.replace("<h2>Sheet1</h2>", " ");
			strReult = strReult.replace("<h2>Sheet2</h2>", " ");
			strReult = strReult.replace("<h2>Sheet3</h2>", " ");
			jsonReult.put("html", strReult);
		} catch (InvalidFormatException e) {
			LOG.error(e.getMessage(), e);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		} catch (ParserConfigurationException e) {
			LOG.error(e.getMessage(), e);
		} catch (TransformerException e) {
			LOG.error(e.getMessage(), e);
		}
		return jsonReult;
	}

	/**
	 * 数据保存
	 * 
	 * @param string
	 * @param info
	 * @param url
	 * @param string2
	 * @Description:
	 */
	private void commonSql(String bean, InfoVo info, String id, String url) {
		StringBuilder buf = new StringBuilder();
		String printTable = DAOUtil.getTableName("bean_print_url", info.getCompanyId());
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("select count(*) from ");
		queryBuilder.append(printTable);
		queryBuilder.append(" where bean = '");
		queryBuilder.append("' and bean_id = ");
		queryBuilder.append(id);
		int count = DAOUtil.executeCount(queryBuilder.toString());
		if (count <= 0) {
			buf.append("insert into ").append(printTable).append(" (bean,bean_id,print_url) ").append(" values('")
					.append(bean).append("',").append(id).append(",").append("'").append(url).append("')");
			int sum = DAOUtil.executeUpdate(buf.toString());
			LOG.error("打印添加" + sum);
		} else {
			StringBuilder editBuf = new StringBuilder();
			editBuf.append("update ").append(printTable).append(" set print_url = '").append(url).append("'")
					.append(" where id = ").append(id).append(" and bean = '").append(bean).append("'");
			int num = DAOUtil.executeUpdate(editBuf.toString());
			LOG.error(num);
			LOG.error("打印编辑" + num);
		}

	}

	/**
	 * 模版文件替换形成正式打印模版
	 * 
	 * @param url
	 * @param jsonObject
	 * @param info
	 * @param bean
	 * @return
	 * @Description:
	 */
	private String commonPingjie(String url, JSONObject jsonObject, InfoVo info, String bean) {
		String previewUrl = "";
		try {
			JSONArray arrayList = new JSONArray();
			FileInputStream fs = new FileInputStream(url);
			Workbook wb = null;
			if (url.toLowerCase().endsWith("xlsx")) {
				wb = new XSSFWorkbook(fs);
			} else if (url.toLowerCase().endsWith("xls")) {
				wb = new HSSFWorkbook(fs);
			}
			Sheet sheet = wb.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.iterator();
			Map<String, String> array = new HashMap<>();

			int record = 0;
			int number = 0;
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				Iterator<Cell> cellIterator = row.cellIterator();
				if (row != null) {
					// 循环每一列
					while (cellIterator.hasNext()) {
						JSONObject object = new JSONObject();

						Cell cell = cellIterator.next();
						if (cell != null) {
							cell.setCellType(HSSFCell.CELL_TYPE_STRING);
						}
						if (cell == null || cell.getStringCellValue() == null) {
							continue;
						}
						String value = cell.getStringCellValue();
						if (!"".equals(value)) { // 值不为空 并不存在字表单的特殊字符
							if (!"".equals(jsonObject.getString(value)) && value.indexOf("$") == -1) {
								String valueName = "";

								if (null == jsonObject.getString(value)) {
									if (jsonObject.containsKey(value)) {
										valueName = "";
									} else {
										valueName = value;
										// 没有替换的字段则用原有字符
									}
								} else {

									// 是否是下框
									if (value.contains(Constant.TYPE_PICKLIST)) {
										JSONArray jsonRerult = JSONArray.parseArray(jsonObject.getString(value));
										for (int k = 0; k < jsonRerult.size(); k++) {
											JSONObject result = (JSONObject) jsonRerult.get(k);
											valueName = result.getString("label");
										}
									}
									// 是否是多级下拉
									else if (value.contains(Constant.TYPE_MUTLI_PICKLIST)) {
										JSONArray jsonRerult = JSONArray.parseArray(jsonObject.getString(value));
										String name = "";
										for (int k = 0; k < jsonRerult.size(); k++) {
											JSONObject result = (JSONObject) jsonRerult.get(k);
											name += result.getString("label");
										}
										valueName = name;
									}
									// 是否是时间
									else if (value.contains(Constant.TYPE_DATETIME)) {
										SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
										valueName = sdf.format(Long.parseLong(jsonObject.getString(value)));
									} else {
										// 存在替换字段则替换真正值
										valueName = jsonObject.getString(value);
									}

								}
								if (record == Constant.CURRENCY_ZERO) {
									object.put("RowIndex", cell.getRowIndex());
								} else {
									object.put("RowIndex", cell.getRowIndex() + record);
								}
								object.put("type", Constant.CURRENCY_ZERO);
								object.put("ColumnIndex", cell.getColumnIndex());
								object.put("fieldName", valueName);
								arrayList.add(object);
							} else {
								if (jsonObject.containsKey(value)) {
									int rowIndex = 0;
									if (record == Constant.CURRENCY_ZERO) {
										rowIndex = cell.getRowIndex();
									} else {
										rowIndex = cell.getRowIndex() + record;
									}
									object.put("RowIndex", rowIndex);
									object.put("type", Constant.CURRENCY_ZERO);
									object.put("ColumnIndex", cell.getColumnIndex());
									object.put("fieldName", "");
									arrayList.add(object);
								} else {
									int rowIndex = 0;
									if (record == Constant.CURRENCY_ZERO) {
										rowIndex = cell.getRowIndex();
									} else {
										rowIndex = cell.getRowIndex() + record;
									}

									// 存在子表单特殊字符则记录

									String str = value.substring(0, value.indexOf("$")); // 截取目标
									String content = value.substring(value.indexOf("$") + 1, value.length()); // 目标字段
									object.put("type", Constant.CURRENCY_ONE);
									object.put("RowIndex", rowIndex);
									object.put("ColumnIndex", cell.getColumnIndex());
									object.put("style", cell.getCellStyle());
									object.put("bean", str);
									object.put("fieldName", content);
									arrayList.add(object);

									String surface = bean + "_" + str;
									String handleTable = DAOUtil.getTableName(surface, info.getCompanyId()); // bean

									String FieldName = bean + "_id"; // 子表单关联字段
									StringBuilder builder = new StringBuilder();
									builder.append("select count(*)  from " + handleTable)
											.append(" where " + FieldName + " = ").append(jsonObject.get("id"));
									int num = DAOUtil.executeCount(builder.toString());

									number = num;
								}
							}
						} else {
							cell.setCellValue("");
						}

					}
					record = number;
				}
			}
			if (arrayList.size() > 0) { // 填写数据插入
				Row rows;
				for (int i = 0; i < arrayList.size(); i++) {
					JSONObject objectList = arrayList.getJSONObject(i);
					// 正常
					if (objectList.getInteger("type") == Constant.CURRENCY_ZERO) {
						int rowIndex = objectList.getInteger("RowIndex");
						int columnIndex = objectList.getInteger("ColumnIndex");
						String fieldName = objectList.getString("fieldName");
						if (array.containsKey(rowIndex + "")) {
							rows = sheet.getRow(rowIndex);
							// 是否存在相同行 存在则创建
						} else {
							rows = sheet.createRow(rowIndex);
							array.put(rowIndex + "", rowIndex + "");
						}
						Cell cells = rows.createCell(columnIndex);
						cells.setCellValue(fieldName);

					} else {
						int num = 0;

						String surface = bean + "_" + objectList.getString("bean");
						String FieldName = bean + "_id"; // 子表单关联字段

						List<String> stringList = LayoutUtil.jsonParser4Map2(info.getCompanyId().toString(), bean, "0");
						String str = "{\"where\":{" + FieldName + ":" + jsonObject.get("id") + "}}";
						JSONObject json = JSONObject.parseObject(str);
						String querySql = JSONParser4SQL.getSimpleQuerySql(info.getCompanyId().toString(), surface,
								stringList, json);

						List<JSONObject> list = DAOUtil.executeQuery4JSON(querySql);
						int rowIndex = objectList.getInteger("RowIndex");
						int columnIndex = objectList.getInteger("ColumnIndex");
						String fieldName = objectList.getString("fieldName");
						for (int j = 0; j < list.size(); j++) {

							int index = rowIndex + num;
							JSONObject object = list.get(j);
							// 是否存在相同行 存在则获取
							if (array.containsKey(index + "")) {
								rows = sheet.getRow(index);
								// 是否存在相同行 存在则创建
							} else {
								rows = sheet.createRow(index);
								array.put(index + "", index + "");
							}
							Cell cells = rows.createCell(columnIndex);
							if (!"".equals(object.getString(fieldName)) && null != object.getString(fieldName)) {
								// 是否是下框
								if (fieldName.contains(Constant.TYPE_PICKLIST)) {
									JSONArray jsonRerult = JSONArray.parseArray(object.getString(fieldName));
									for (int k = 0; k < jsonRerult.size(); k++) {
										JSONObject result = (JSONObject) jsonRerult.get(k);
										cells.setCellValue(result.getString("label"));
									}
								}
								// 是否是多级下拉
								else if (fieldName.contains(Constant.TYPE_MUTLI_PICKLIST)) {
									JSONArray jsonRerult = JSONArray.parseArray(object.getString(fieldName));
									String name = "";
									for (int k = 0; k < jsonRerult.size(); k++) {
										JSONObject result = (JSONObject) jsonRerult.get(k);
										name += result.getString("label");
									}
									cells.setCellValue(name);
								}
								// 是否是时间
								else if (fieldName.contains(Constant.TYPE_DATETIME)) {
									SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
									cells.setCellValue(sdf.format(Long.parseLong(object.getString(fieldName))));
								}
								// 省市区
								else if (fieldName.contains(Constant.TYPE_AREA)) {
									String area = object.getString(fieldName);
									String[] areaList = area.split(",");
									String nameValue = null;
									for (String name : areaList) {
										String strName = name.substring(name.indexOf(":"), name.length());
										nameValue += strName;
									}
									cells.setCellValue(nameValue);
								} else {
									cells.setCellValue(object.getString(fieldName));
								}
							} else {
								cells.setCellValue("");
							}
							num++;
						}
					}

				}
			}
			File savedir = new File(Constant.PRINT_PREVIEW_URL);

			if (!savedir.exists()) {
				savedir.mkdirs();
			}
			Long timeMillis = System.currentTimeMillis();
			// 如果文件存在则删除
			File imageFile = new File(savedir, timeMillis + ".xls");
			if (imageFile.exists()) {
				imageFile.delete();
			} // 输出文件
			previewUrl = Constant.PRINT_PREVIEW_URL + timeMillis + ".xls";
			FileOutputStream fOut = new FileOutputStream(previewUrl);
			wb.write(fOut);
			fOut.flush();
			// 操作结束，关闭文件
			fOut.close();

		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return previewUrl;
	}

}
