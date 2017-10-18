package com.nj.simba;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.imageio.ImageIO;
import com.nj.simba.MapPointLoader.MapPoint;
import com.nj.simba.RoadMap.RoadSeg;

public class DrawRoadMap {
	public static final int ZOOM = 100;
	public static final int ADJ_E = 0;
	public static final int ADJ_N = 0;
	
	 public static void main(String[] args) throws Exception {
		 System.out.println("读取数据库: ret2000.mdb");
		 RoadMap roadMap = new RoadMap("ret2000.mdb");
			
		 int width = roadMap.getMapWidth()/ZOOM;
		 int height = roadMap.getMapHeight()/ZOOM;
		 
		 System.out.println("Gps轨迹图生成中...");

		 File file = new File("Gps轨迹图.png");
		 BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		 Graphics2D g2 = (Graphics2D)bi.getGraphics(); 
		 g2.setBackground(Color.WHITE);
		 g2.clearRect(0, 0, width, height);
		 g2.setPaint(Color.BLACK);
		 
		 ArrayList roadSegs = roadMap.getRoadSegs();
		 final int top_e = roadMap.getMapTopGpsE();
		 final int top_n = roadMap.getMapTopGpsN();
		 
		 for(int i = 0; i < roadSegs.size(); i++) {
			 RoadSeg seg = (RoadSeg) roadSegs.get(i);
			 drawSeg(g2, seg, top_e, top_n);
		 }

		 BufferedImage biFlip = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		 biFlip.getGraphics().drawImage(bi, 0, 0, width, height, 0, height, width, 0, null);
		 ImageIO.write(biFlip, "png", file);
		 System.out.println("Gps轨迹图生成中完成\n生成互联网+版本...");
		 
		 drawWithBaidu(roadSegs, top_e, top_n, roadMap.getMapBottomGpsE(), roadMap.getMapBottomGpsN());
		 System.out.println("生成互联网+版本完成");
	}

	public static void drawSeg(Graphics2D g2, RoadSeg seg, int top_e, int top_n) {
		ArrayList points = seg.segPoints;
		Collections.sort(points, new SortByDistance(seg));

		MapPoint p1 = (MapPoint) points.get(0);

		for(int i = 1; i < points.size(); i++) {
			MapPoint p2 = (MapPoint) points.get(i);
			int e1 = p1.gps_e-top_e;
			int n1 = p1.gps_n-top_n;
			int e2 = p2.gps_e-top_e;
			int n2 = p2.gps_n-top_n;
					
			g2.drawLine(e1/ZOOM, n1/ZOOM, e2/ZOOM, n2/ZOOM);
			p1 = p2;
		}
	}
	
	public static void drawWithBaidu(ArrayList roadSegs, int top_e, int top_n, int bottom_e, int bottom_n) throws IOException, URISyntaxException {
		InputStream is = DrawRoadMap.class.getResourceAsStream("/GpsGuiji.html");
		InputStreamReader isr = new InputStreamReader(is, "UTF-8");
		BufferedReader reader = new BufferedReader(isr);
		
		StringBuffer sbHead   = new StringBuffer();
		StringBuffer sbBottom = new StringBuffer();
		StringBuffer sbSegs   = new StringBuffer();
		
		double top_e_real    = (double)(top_e+ADJ_E)/1000000;
		double top_n_real    = (double)(top_n+ADJ_N)/1000000;
		double bottom_e_real = (double)(bottom_e+ADJ_E)/1000000;
		double bottom_n_real = (double)(bottom_n+ADJ_N)/1000000;
		double center_e      = (top_e_real + bottom_e_real)/2;
		double center_n      = (top_n_real + bottom_n_real)/2;

		String line;
		while((line = reader.readLine()) != null) {
			if (line.contains("gps-guiji-start")) {
				break;
			}
			
			if (line.contains("gps-guiji-center-e0")) {
				line = line.replaceAll("gps-guiji-center-e0", String.valueOf(top_e_real))
				.replaceAll("gps-guiji-center-n0", String.valueOf(top_n_real))
				.replaceAll("gps-guiji-center-e1", String.valueOf(bottom_e_real))
				.replaceAll("gps-guiji-center-n1", String.valueOf(bottom_n_real));
			} else if (line.contains("gps-guiji-center-e")) {
				line = line.replaceAll("gps-guiji-center-e", String.valueOf(center_e))
				.replaceAll("gps-guiji-center-n", String.valueOf(center_n));
			}
			
			sbHead.append(line+"\n");
		}
		
		while((line = reader.readLine()) != null) {
			if (line.contains("gps-guiji-end")) {
				break;
			}
		}
		
		while((line = reader.readLine()) != null) {
			sbBottom.append(line + "\n");
		}
		
		reader.close();
		
		for(int iSeg = 0; iSeg < roadSegs.size(); iSeg++) {
			 RoadSeg seg = (RoadSeg) roadSegs.get(iSeg);
			 ArrayList points = seg.segPoints;

			 sbSegs.append("\tvar seg" + iSeg + " = [\n");
			 int size = points.size();
			 
			 for(int i = 0; i < size-1; i++) {
				MapPoint p = (MapPoint) points.get(i);
				sbSegs.append("\t\tnew BMap.Point(");
				sbSegs.append((double)(p.gps_e+ADJ_E)/1000000);
				sbSegs.append(",");
				sbSegs.append((double)(p.gps_n+ADJ_N)/1000000);
				sbSegs.append("),\n");
			}
			
			MapPoint p = (MapPoint) points.get(size-1);
			sbSegs.append("\t\tnew BMap.Point(");
			sbSegs.append((double)(p.gps_e+ADJ_E)/1000000);
			sbSegs.append(",");
			sbSegs.append((double)(p.gps_n+ADJ_N)/1000000);
			
			sbSegs.append(")\n");
			sbSegs.append("\t];\n");
			
			sbSegs.append("\tvar curve" + iSeg
					+ " = new BMap.Polyline(seg" + iSeg 
					+ ", {strokeColor:\"red\", strokeWeight:4, strokeOpacity:0.5});\n"
			        + "\tmap.addOverlay(curve" + iSeg
			        + ");\n");
		}
		
		
		File htaOut = new File("Gps轨迹互联网+.hta");
		OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(htaOut), "UTF-8");
		BufferedWriter writer = new BufferedWriter(osw);
		writer.write(sbHead.toString());
		writer.write(sbSegs.toString());
		writer.write(sbBottom.toString());
		writer.close();
	}

	
	static class SortByDistance implements Comparator {
		RoadSeg roadSeg; 

		public SortByDistance(RoadSeg seg) {
			roadSeg = seg;
		}

		public int compare(Object p1, Object p2) {
			MapPoint point1 = (MapPoint) p1;
			MapPoint point2 = (MapPoint) p2;
			
			int e1 = point1.gps_e - roadSeg.start.gps_e;
			int n1 = point1.gps_n - roadSeg.start.gps_n;
			int e2 = point2.gps_e - roadSeg.start.gps_e;
			int n2 = point2.gps_n - roadSeg.start.gps_n;
			
			int disPower1 = e1*e1 + n1*n1;
			int disPower2 = e2*e2 + n2*n2;
			
			return disPower1 - disPower2;
		}

	}

}
