package com.caeps.loadDatabase;

public class AnalogMeasurements extends CIMObject{
	
	private int instant;
	private double value;
//	private Substation substation;
	private Cluster cluster;
	
	public AnalogMeasurements(String rdfId, String name, int instant, double value)
	{
		super(rdfId, name);
		this.instant=instant;
		this.value=value;
//		this.substation=substation;
	}
	
	public int getInstant(){
		return instant;
	}
	
	public double getValue(){
		return value;
	}

//	public Substation getSubstation(){
//		return substation;
//	}
	
	public Cluster getCluster(){
		return cluster;
	}

	public void setCluster(Cluster a){
		cluster=a;
	}

}
