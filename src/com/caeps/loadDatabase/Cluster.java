package com.caeps.loadDatabase;

public class Cluster {
	private String clusterLabel;
	private double voltage;
	private double angle;
	
	public Cluster(String clusterLabel){
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

	public String getLabel(){
		return clusterLabel;
	}
}
