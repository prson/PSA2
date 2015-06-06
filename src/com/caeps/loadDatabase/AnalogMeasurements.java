package com.caeps.loadDatabase;

public class AnalogMeasurements extends CIMObject{
	
	private int instant;
	private double voltage;
	private double angle;
	private Cluster cluster;
	
	public AnalogMeasurements(String rdfId, int instant, double voltage, double angle)
	{
		super(rdfId);
		this.instant=instant;
		this.voltage=voltage;
		this.angle=angle;
	}
	
	public int getInstant(){
		return instant;
	}
	
	public double getVoltage(){
		return voltage;
	}
	
	public double getAngle(){
		return angle;
	}
	
	public setCluster(Cluster a){
		cluster=a;
	}

}
