package com.dwl.rep.common.DB;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

import com.dwl.rep.common.DB.impl.MySQLBase;
import com.dwl.rep.common.DB.impl.OracleBase;
import com.dwl.rep.common.DB.impl.PostgreSQLBase;
import com.dwl.rep.common.DB.impl.SQLServerBase;
import com.dwl.rep.common.DB.impl.SQLiteBase;
import com.dwl.rep.pojo.DbInfo;

public class DataBaseFactory {
	
	private DataBaseFactory(){
		
	}
	
	private static DataBaseFactory dataBaseFactory = null;
	
	private static Map<String, BasicDataSource> dbMap = new HashMap<>();
	
	private static Logger logger = Logger.getLogger(DataBaseFactory.class);
	
	public enum BaseType{
		
		MySQL,Oracle,PostgreSQL,SQLite,SQLServer
		
	}
	
	/**
	 * 获取单例
	 * @return
	 */
	public static DataBaseFactory getInstance(){
		if(dataBaseFactory == null){
			synchronized(DataBaseFactory.class){
				if(dataBaseFactory == null)
					dataBaseFactory = new DataBaseFactory();
			} 
		}
		return dataBaseFactory;
	}
	
	
	/**
	 * 获取数据库信息
	 * @param dbInfo
	 * @return
	 */
	public DataBase getDataBase(DbInfo dbInfo){
		DataBase dataBase = null;
		BaseType type =BaseType.valueOf(dbInfo.getDbType());
		switch (type) {
		case MySQL:
			dataBase = new MySQLBase();
			break;
		case Oracle:
			dataBase = new OracleBase();
			break;
		case PostgreSQL:
			dataBase = new PostgreSQLBase();
			break;
		case SQLite:
			dataBase = new SQLiteBase();
			break;
		case SQLServer:
			dataBase = new SQLServerBase();
			break;
		default:
			break;
		}
		return dataBase;
	}
	
	/**
	 * 获取数据库连接
	 * @param dbInfo
	 * @return
	 * @throws SQLException
	 */
	public Connection getConnection(DbInfo dbInfo) throws SQLException{
		if(!dbMap.containsKey(dbInfo.getId())){
			addDataSource(dbInfo);
		}
		return dbMap.get(dbInfo.getId()).getConnection();
	}
	
	/**
	 * 添加数据源
	 * @param dbInfo
	 */
	public void addDataSource(DbInfo dbInfo){
		synchronized (DataBaseFactory.class) {
			if(!dbMap.containsKey(dbInfo.getId())){
				dbMap.put(dbInfo.getId(), createDataSource(dbInfo));
			}
		}
	}
	
	/**
	 * 创建数据源
	 * @param dbInfo
	 * @return
	 */
	public BasicDataSource createDataSource(DbInfo dbInfo){
		logger.info("创建数据源："+dbInfo.getDbName());
		BasicDataSource dataSource =getDataBase(dbInfo).getDataSource(dbInfo);
		logger.info(dbInfo.getDbName()+" - 创建成功");
		return dataSource;
		
	}
	
	
	/**
	 * 移除数据源
	 * @param dbInfo
	 */
	public void removeDataSource(DbInfo dbInfo){
		BasicDataSource dataSource = dbMap.get(dbInfo.getId());
		if(dataSource != null){
			try {
				dataSource.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		dbMap.remove(dbInfo.getId());
		logger.info(dbInfo.getDbName()+" - 移除成功");
	}
	
	
	/**
	 * 测试连接
	 * @param dbInfo
	 * @throws SQLException
	 */
	public void testConnection(DbInfo dbInfo) throws SQLException{
		Connection conn = getConnection(dbInfo);
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(getDataBase(dbInfo).getLinkSql());
		if(rs.next())
			logger.info(getDataBase(dbInfo).getLinkSql() + " ----> " + rs.getString(1));
		rs.close();
		stmt.close();
		conn.close();
		
	}
	

}
