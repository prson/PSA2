package com.caeps.loadDatabase;

import java.util.ArrayList;

import com.caeps.loadDatabase.Cluster;

public class Instant {
	
	private double avgVoltage;
	private double avgAngle;
	private int instant;
	private ArrayList<AnalogMeasurement> analogMeasurements=new ArrayList<AnalogMeasurement>();
	private Cluster belongsToClusters;
	
	public Instant(int instant, ArrayList<AnalogMeasurement> analogMeasurements){
		this.instant=instant;
		this.analogMeasurements=analogMeasurements;
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
	
	public ArrayList<AnalogMeasurement> getAnalogMeasurements(){
		return analogMeasurements;
	}

}
