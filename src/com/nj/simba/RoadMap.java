package com.nj.simba;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import com.nj.simba.MapPointLoader.MapPoint;

public class RoadMap {
	private ArrayList roadSegs = new ArrayList();
	private int top_e;
	private int top_n;
	private int bottom_e;
	private int bottom_n;
	
	public RoadMap(String dbPath) {
		MapPointLoader loader = new MapPointLoader(dbPath);
		try {
			loader.loadDb();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		top_e = loader.min_gps_e;
		top_n = loader.min_gps_n;
		bottom_e = loader.max_gps_e;
		bottom_n = loader.max_gps_n;

		buildRoadSegs(loader.points);
	}
	
	void buildRoadSegs(HashMap points) {
		HashMap segsTmp = new LinkedHashMap();
		
		Iterator iterPoints = points.entrySet().iterator();
		while (iterPoints.hasNext()) {
			Map.Entry entry = (Map.Entry) iterPoints.next();
			
			//Object key = entry.getKey();
			MapPoint point = (MapPoint) entry.getValue();
			RoadSeg roadSeg = (RoadSeg)segsTmp.get(point.road_code);
			
			if (roadSeg == null) {
				String[] ids = point.road_code.split("-");
				MapPoint start = (MapPoint) points.get(Integer.valueOf(ids[0]));
				roadSeg = new RoadSeg(point.road_code, start);
			}

			roadSeg.addPoint(point);
			segsTmp.put(point.road_code, roadSeg);
		}
		
		Iterator iterSegs = segsTmp.entrySet().iterator();
		while (iterSegs.hasNext()) {
			Map.Entry entry = (Map.Entry) iterSegs.next();
			roadSegs.add(entry.getValue());
		}

	}
	
	public int getMapWidth() {
		return bottom_e - top_e;
	}
	
	public int getMapHeight() {
		return bottom_n - top_n;
	}
	
	public ArrayList getRoadSegs() {
		return roadSegs;
	}
	
	public int getMapTopGpsE() {
		return top_e;
	}
	
	public int getMapTopGpsN() {
		return top_n;
	}
	
	public int getMapBottomGpsE() {
		return bottom_e;
	}
	
	public int getMapBottomGpsN() {
		return bottom_n;
	}
	
	static class RoadSeg {
		String road_code;
		MapPoint start;
		MapPoint end;
		
		public RoadSeg(String road_code, MapPoint start) {
			this.road_code = road_code;
			this.start = start;
			segPoints.add(start);
		}

		public int getPointCount() {
			return segPoints.size();
		}
		
		void addPoint(MapPoint point) {
			String id2str = String.valueOf(point.id);
			if (point.road_code.startsWith(id2str)) {
				start = point;
			}
			
			if (point.road_code.endsWith(id2str)) {
				 end = point;
			}
			
			segPoints.add(point);
			
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder(road_code);
			sb.append(":");
			
			for(int i = 0; i < segPoints.size(); i++) {
				MapPoint point = (MapPoint) segPoints.get(i);	
				sb.append(point.id);
				sb.append(",");
			}
			return sb.toString();
		}
		
		ArrayList segPoints = new ArrayList();
	}
	
	public static void main(String[] args) {
		RoadMap rm = new RoadMap("ret2000.mdb");
		
		for(int i = 0; i < rm.roadSegs.size(); i++) {
			System.out.println(rm.roadSegs.get(i));
		}
	}
}
