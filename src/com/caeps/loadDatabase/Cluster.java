package com.caeps.loadDatabase;

public class Cluster {
	private int clusterLabel;
	private double voltage;
	private double angle;
	private String descr;
	
	public Cluster(int clusterLabel){
		this.clusterLabel=clusterLabel;
	}
	
	public void setVoltage(double newVoltage){
		voltage=newVoltage;
	}
	
	public void setAngle(double newAngle){
		angle=newAngle;
	}
	
	public double getVoltage(){
		return voltage;
	}
	
	public double getAngle(){
		return angle;
	}

	public int getLabel(){
		return clusterLabel;
	}
	
	public String getDesc(){
		return descr;
	}
	
	public void setDesc(String d){
		descr=d;
	}
}
