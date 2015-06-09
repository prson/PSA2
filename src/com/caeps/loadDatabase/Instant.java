package com.caeps.loadDatabase;

import com.caeps.loadDatabase.Cluster;

public class Instant {
	
	private double avgVoltage;
	private double avgAngle;
	private int instant;
	private Cluster belongsToClusters;
	
	public Instant(int instant, double avgVoltage, double avgAngle){
		this.instant=instant;
		this.avgVoltage=avgVoltage;
		this.avgAngle=avgAngle;
	}
	
	public double getAvgVoltage(){
		return avgVoltage;
	}
	
	public double getAvgAngle(){
		return avgAngle;
	}
	
	public int getInstant(){
		return instant;
	}
	
	public Cluster getCluster(){
		return belongsToClusters;
	}
	
	public void setCluster(Cluster a){
		belongsToClusters=a;
	}
	

}
