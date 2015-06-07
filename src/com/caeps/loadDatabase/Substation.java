package com.caeps.loadDatabase;

import java.util.ArrayList;

public class Substation extends CIMObject{
	
	private ArrayList<AnalogMeasurements> analogMeasurements=new ArrayList<AnalogMeasurements>();
	private String region;
	
	public Substation(String rdfId, String name, ArrayList<AnalogMeasurements> analogMeasurements, String region)
//	public Substation(String rdfId, String name, String region)
	{
		super(rdfId, name);
		this.analogMeasurements=analogMeasurements;
		this.region=region;
	}
	
	public ArrayList<AnalogMeasurements> getAnalogMeasurements(){
		return analogMeasurements;
	}

	public String getRegion(){
		return region;
	}
}
