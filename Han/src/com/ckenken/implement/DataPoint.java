package com.ckenken.implement;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import com.ckenken.Main.Main_v2;

public class DataPoint {
	
	public static int MAX_G_NUMBER = 127;
	
	public static int max_symbol = 0;
	
	public int seqid;
	public int sameid;
	public double lat;
	public double lng;
	public int G;
	public String cate;
	public Date startTime;
	public Date endTime;
	public double [] timeDistribution; 
	public int [] timeCounter;
	public double [] gDistribution;
	public int [] gCounter;
	public int symbol;
	
	public DataPoint()
	{
		this.timeCounter = new int [24];
		this.timeDistribution = new double [24];
		this.gDistribution = new double[MAX_G_NUMBER];
		this.gCounter = new int [MAX_G_NUMBER];		
	}
	
	@SuppressWarnings("deprecation")
	public DataPoint(int a1, int a2, double a3, double a4, int a5, String a6, String a7, String a8) throws ParseException, FileNotFoundException 
	{
		seqid = a1;
		sameid = a2;
		lat = a3;
		lng = a4;
		G = a5;
		cate = a6;
		startTime = Main_v2.parseDate(a7);
		endTime = Main_v2.parseDate(a8);	
		timeDistribution = new double [24];
		timeCounter = new int[24];
		gDistribution = new double[MAX_G_NUMBER];
		gCounter = new int[MAX_G_NUMBER];
	//	gDistribution[a5] = 1;
	//	gCounter[a5] = 1;
		symbol = -1;
		
		int s = startTime.getHours();
		int e = endTime.getHours();
		
		Google_Cate_List cateList = new Google_Cate_List();
		
		String [] SP = cate.split(",");
		
		gDistribution[cateList.cateMap.get(SP[0])] = 0.5;
		gDistribution[cateList.cateMap.get(SP[1])] = 0.5;

		gCounter[cateList.cateMap.get(SP[0])] = 1;
		gCounter[cateList.cateMap.get(SP[1])] = 1;
		
		if(e < s) { // over a day
			for(int i = s; i<24; i++) {
				timeDistribution[i] = 1;
			}
			for(int i = 0; i<=e; i++) {
				timeDistribution[i] = 1;
			}
		}
		else {
			for(int i = s; i<=e; i++) {
				timeDistribution[i] = 1;
			}
		}
		
		for(int i = 0; i<24; i++) {
			timeCounter[i] = (int)timeDistribution[i];
		}
		
		// normalize
		int counter = 0;
		for(int i = 0; i<24; i++) {
			if (timeDistribution[i] != 0) {
				counter++;
			}
		}
		for(int i = 0; i<24; i++) {
			timeDistribution[i] /= (double)counter;
		} 
	}
	
	public DataPoint(DataPoint a) {
		
		this.seqid = a.seqid;
		this.sameid = a.sameid;
		this.lat = a.lat;
		this.lng = a.lng;
		this.G = a.G;
		this.cate = a.cate;
		this.startTime = a.startTime;
		this.endTime = a.endTime;
		this.timeCounter = new int [24];
		this.timeDistribution = new double [24];
		this.gDistribution = new double[MAX_G_NUMBER];
		this.gCounter = new int [MAX_G_NUMBER];
		
		for(int i = 0; i<24; i++) {
			this.timeCounter[i] = a.timeCounter[i];
			this.timeDistribution[i] = a.timeDistribution[i];
		}
		
		for(int i = 0; i<a.gCounter.length; i++) {
			this.gCounter[i] = a.gCounter[i];
			this.gDistribution[i] = a.gDistribution[i];
		}
		
		this.symbol = a.symbol;		
	}
	
	public static DataPoint copy(DataPoint a) {
		
		DataPoint temp = new DataPoint();
		
		temp.seqid = a.seqid;
		temp.sameid = a.sameid;
		temp.lat = a.lat;
		temp.lng = a.lng;
		temp.G = a.G;
		temp.cate = a.cate;
		temp.startTime = a.startTime;
		temp.endTime = a.endTime;
		temp.timeCounter = new int [24];
		temp.timeDistribution = new double [24];
		temp.gDistribution = new double[MAX_G_NUMBER];
		temp.gCounter = new int [MAX_G_NUMBER];
		
		for(int i = 0; i<24; i++) {
			temp.timeCounter[i] = a.timeCounter[i];
			temp.timeDistribution[i] = a.timeDistribution[i];
		}
		
		for(int i = 0; i<a.gCounter.length; i++) {
			temp.gCounter[i] = a.gCounter[i];
			temp.gDistribution[i] = a.gDistribution[i];
		}
		
		temp.symbol = a.symbol;		
		return temp;
	}	
	
	public static boolean same(DataPoint a, DataPoint b)
	{
		if(a.G == b.G) {
			if(overlap(a, b)) {
				return true;
			}
		}	
		
		return false;
	}
	
	@SuppressWarnings("deprecation")
	private static int timeToInt(Date d) 
	{
		int hour = d.getHours();
		int min = d.getMinutes();
		int sec = d.getSeconds();
		
		return (hour * 3600 + min * 60 + sec); 
	}
	
	private static boolean overlap(DataPoint a, DataPoint b)
	{
		if(a.startTime.getTime() > b.startTime.getTime()) {
			DataPoint temp = a;
			a = b;
			b = temp;
		}
		
		int startA = timeToInt(a.startTime);
		int startB = timeToInt(b.startTime);
		
		int endA = timeToInt(a.endTime);
		int endB = timeToInt(b.endTime);
				
		int lengthA = endA - startA;
		int lengthB = endB - startB;
		
		int overLength = 0;
		
		if(startB >= startA && endB <= endA) { // whole B are indside A 	
			overLength = lengthB;
		}
		else if (endA >= startB && endB >= endA) { // normal situation
			overLength = endA - startB;
		}
		else { // no overlap
			overLength = 0;
		}
		
		if(overLength >= (lengthA*0.5) && overLength >= (lengthB*0.5) && overLength != 0)
			return true;
		else 
			return false;
	}
	
	public static double similarity(DataPoint a, DataPoint b) 
	{
		double tsum1 = 0.0;
		// time distance
		for(int i = 0; i<24; i++) {
			double son = a.timeDistribution[i];
			double mother = b.timeDistribution[i];
			
			if(son == 0.0)
				continue;
			
			if(mother == 0.0)
				mother = 0.001;
			
			tsum1 += son * Math.log(son/mother);
		}
		
		double tsum2 = 0.0;
		for(int i = 0; i<24; i++) {
			double son = b.timeDistribution[i];
			double mother = a.timeDistribution[i];

			if(son == 0.0)
				continue;
			
			if(mother == 0.0)
				mother = 0.001;
			
			tsum2 += son * Math.log(son/mother);
		}
		
		double timeDis = 0.0;
		if(tsum1 < tsum2)
			timeDis = tsum1;
		else 
			timeDis = tsum2;
		
		// G distance
		double gsum1 = 0.0;
		for(int i = 0; i<a.gDistribution.length; i++) {
			double son = b.gDistribution[i];
			double mother = a.gDistribution[i];
			
			if(son == 0.0)
				continue;			
		
			if(mother == 0.0) {
				mother = 0.001;
			}
			
			gsum1 += son * Math.log(son/mother);
		}
		
		double gsum2 = 0.0;
		for(int i = 0; i<a.gDistribution.length; i++) {
			double son = a.gDistribution[i];
			double mother = b.gDistribution[i];
			
			if(son == 0.0)
				continue;
			
			if(mother == 0.0) {
				mother = 0.001;
			}
			
			gsum2 += son * Math.log(son/mother);
		}
				
		double gDis = 0.0;
		if (gsum1 < gsum2)
			gDis = gsum1;
		else 
			gDis = gsum2;
		
//		return timeDis + gDis;
		
//		System.out.println(a.seqid + "<->" + b.seqid);
//		
//		System.out.print("timeDis = " + timeDis + ", ");
//		System.out.print("gDis = " + gDis + ", ");
//		System.out.println(a.G == b.G);
		
		if(a.G == b.G)
			return (0.6 * timeDis + 0.4 * gDis) * 2;
		else 
			return 9999;
	}
	
	public static DataPoint merge(DataPoint a, DataPoint b)
	{
		DataPoint temp = new DataPoint();
		
		temp.seqid = -1; // must modify
		temp.sameid = -1; // must modify
		temp.lat = (a.lat + b.lat)/2;
		temp.lng = (a.lng + b.lng)/2;
		temp.G = -1; // must modify
	//	temp.cate = a.cate; // must modify
		temp.startTime = a.startTime; // must modify
		temp.endTime = a.endTime; // must modify
		
		int timeSum = 0;
		for(int i = 0; i<24; i++) {
			temp.timeCounter[i] = a.timeCounter[i] + b.timeCounter[i];
			timeSum += temp.timeCounter[i];
		}
		
		for(int i = 0; i<24; i++) {
			temp.timeDistribution[i] = (double)((double)temp.timeCounter[i] / (double)timeSum); 
		}

		for(int i = 0; i<24; i++) {
			if(temp.timeCounter[i] > 10000)
			{
				System.out.print("");
			}
		}		
		
		String [] SPA = a.cate.split(",");
		String [] SPB = b.cate.split(",");
		
		ArrayList<String> tempCates = new ArrayList<String>();
		
		for(int i = 0; i<SPA.length; i++) {
			tempCates.add(SPA[i]);
		}
		
		for(int i = 0; i<SPB.length; i++) {
			for(int j = 0; j<tempCates.size(); j++) {
				if(SPB[i].equals(tempCates.get(j))) {
					continue;
				}
				else {
					tempCates.add(SPB[i]);
					break;
				}
			}
		}
		
		for(int i = 0; i<tempCates.size(); i++) {
			if(i == 0)
				temp.cate += tempCates.get(i);
			else 
				temp.cate += "," + tempCates.get(i);
		}
		
		
		int gSum = 0;
		for(int i = 0; i<MAX_G_NUMBER; i++) {
			temp.gCounter[i] = a.gCounter[i] + b.gCounter[i];
			gSum += temp.gCounter[i];
		}
		
		for(int i = 0; i<MAX_G_NUMBER; i++) {
			temp.gDistribution[i] = (double)((double)temp.gCounter[i] / (double)gSum); 
		}		

		if(a.symbol != -1)
			temp.symbol = a.symbol;
		else if (b.symbol != -1)
			temp.symbol = b.symbol;
		else 
			temp.symbol = DataPoint.max_symbol++;
		
		return temp;
	}
	
	public void extendTheSame(DataPoint grouper)
	{
		this.lat = grouper.lat;
		this.lng = grouper.lng;
		
		for(int i = 0; i<24; i++) {
			this.timeCounter[i] = grouper.timeCounter[i];
			this.timeDistribution[i] = grouper.timeDistribution[i];
		}
		
		for(int i = 0; i<MAX_G_NUMBER; i++) {
			this.gCounter[i] = grouper.gCounter[i];
			this.gDistribution[i] = grouper.gDistribution[i];
		}
		this.symbol = grouper.symbol;  // not necessary 
	}
	
	public void setTimeDistribution(String input)
	{
		String [] SP = input.split("\n");
		
		double sum = 0.0;
		for(int i = 0; i<24; i++) {
			String [] SP2 = SP[i].split(":");
			this.timeCounter[i] = Integer.parseInt(SP2[1]);
			sum += Integer.parseInt(SP2[1]);
		}
		
		for(int i = 0; i<24; i++) {
			this.timeDistribution[i] = (double)((double)this.timeCounter[i]/sum);
		}
	}

	public void setDistributions(String gDis, String tDis)
	{
		String [] SP = tDis.split("\n");
		
		for(int i = 0; i<24; i++) {
			String [] SP2 = SP[i].split(":");
			this.timeDistribution[i] = Double.parseDouble(SP2[1]);
		}
		
		SP = gDis.split("\n");
		
		for(int i = 0; i<MAX_G_NUMBER; i++) {
			String [] SP2 = SP[i].split(":");
			this.gDistribution[i] = Double.parseDouble(SP2[1]);			
		}
	}	
	
}