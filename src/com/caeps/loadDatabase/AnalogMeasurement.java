package com.caeps.loadDatabase;

public class AnalogMeasurement{
	
	private int instant;
	private double angleValue;
	private double voltageValue;
	private Cluster cluster;
	private String substationID;
	
	public AnalogMeasurement(int instant, double angleValue, double voltageValue, String substationID)
	{
		this.instant=instant;
		this.voltageValue=voltageValue;
		this.angleValue=angleValue;
		this.substationID=substationID;
	}
	
	public int getInstant(){
		return instant;
	}
	
	public double getVoltageValue(){
		return voltageValue;
	}
	
	public double getAngleValue(){
		return angleValue;
	}

	public String getSubstationRdfID(){
		return substationID;
	}
	
	public Cluster getCluster(){
		return cluster;
	}

	public void setCluster(Cluster a){
		cluster=a;
	}

}