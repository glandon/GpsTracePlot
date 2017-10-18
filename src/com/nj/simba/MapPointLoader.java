package com.nj.simba;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class MapPointLoader {
	HashMap points;
	String dbPath;
	int min_gps_e = Integer.MAX_VALUE;
	int max_gps_e = 0;
	int min_gps_n = Integer.MAX_VALUE;
	int max_gps_n = 0;
	
	public MapPointLoader(String dbPath) {
		this.points = new LinkedHashMap();
		this.dbPath = dbPath;
	}
	
	public void loadDb() throws SQLException {
		Connection conn=DriverManager.getConnection("jdbc:ucanaccess://" + dbPath);
		//System.out.println(conn);
		
		Statement s = conn.createStatement();
		ResultSet rs = s.executeQuery("select * from map_point");
		int min_e_id = 0;
		int max_e_id = 0;
		int min_n_id = 0;
		int max_n_id = 0;
		
		while(rs.next()) {
			int id = rs.getInt("point_no");
			int gps_e = rs.getInt("gps_e");
			int gps_n = rs.getInt("gps_n");
			int point_no_f = rs.getInt("point_no_f");
			int point_type = rs.getInt("point_type");
			String road_code = rs.getString("road_code");
			
			if (road_code == null || id == 0) {
				continue;
			}
			
			if (min_gps_e > gps_e) {
				min_gps_e = gps_e;
				min_e_id = id;
			}
			
			if (max_gps_e < gps_e) {
				max_gps_e = gps_e;
				max_e_id = id;
			}
			
			if (min_gps_n > gps_n) {
				min_gps_n = gps_n;
				min_n_id = id;
			}
			
			if (max_gps_n < gps_n) {
				max_gps_n = gps_n;
				max_n_id = id;
			}
			
			MapPoint mp = new MapPoint(id, gps_e, gps_n, point_type, road_code, point_no_f);
			points.put(Integer.valueOf(id), mp);
		}

		System.out.println("加载数据库完成!");

		System.out.println(String.format("(%d=%d, %d=%d)-(%d=%d, %d=%d)", 
			new Integer[]{
			Integer.valueOf(min_e_id), Integer.valueOf(min_gps_e), 
			Integer.valueOf(min_n_id), Integer.valueOf(min_gps_n), 
			Integer.valueOf(max_e_id), Integer.valueOf(max_gps_e),
			Integer.valueOf(max_n_id), Integer.valueOf(max_gps_n)})
		);
	}
	
	static class MapPoint {
		public MapPoint(int id, int gps_e, int gps_n, int type, boolean visible, boolean passed, String road_code, int id_f) {
			this.id = id;
			this.gps_e = gps_e;
			this.gps_n = gps_n;
			this.type = type;
			this.visible = visible;
			this.passed = passed;
			this.id_f = id_f;
			this.road_code = road_code;
		}
		
		public MapPoint(int id, int gps_e, int gps_n, int type, String road_code, int id_f) {
			this.id = id;
			this.gps_e = gps_e;
			this.gps_n = gps_n;
			this.type = type;
			this.visible = true;
			this.passed = false;
			this.id_f = id_f;
			this.road_code = road_code;
		}
		
		public String toString() {
			return "id =" + id + ", id_f =" + id_f  + ", type =" + type
					+ ", gps_e=" + gps_e  + ", gps_n=" + gps_n;
		}

		int id;
		int id_f;
		int gps_e;
		int gps_n;
		int type;
		boolean visible;
		boolean passed;
		String road_code;
	}
	
	public static void main(String[] args) {
		try {
			new MapPointLoader("d:/ret2000.mdb").loadDb();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}


