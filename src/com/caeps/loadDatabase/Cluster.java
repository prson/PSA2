package com.caeps.loadDatabase;

import java.util.ArrayList;

public class Cluster {
	private int clusterLabel;
	private double voltage;
	private double angle;
	private String descr;
	private ArrayList<AnalogMeasurement> analogMeasurements=new ArrayList<AnalogMeasurement>();
	
	public Cluster(int clusterLabel, ArrayList<AnalogMeasurement> analogMeasurements){
		this.clusterLabel=clusterLabel;
		this.analogMeasurements=analogMeasurements;
	}
	
	public Cluster(Cluster cluster) {
		this.clusterLabel=cluster.getLabel();
		this.analogMeasurements=cluster.getAnalogMeasurements();
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
	
	public void setAnalogMeasurements(ArrayList<AnalogMeasurement> am){
		this.analogMeasurements=am;
	}
	
	public ArrayList<AnalogMeasurement> getAnalogMeasurements(){
		return analogMeasurements;
	}
	
}
