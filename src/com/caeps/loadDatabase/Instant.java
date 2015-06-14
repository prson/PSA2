/*
 * 
 */
package com.caeps.loadDatabase;

import java.util.ArrayList;

import com.caeps.loadDatabase.Cluster;

// TODO: Auto-generated Javadoc
/**
 * The Class Instant.
 */
public class Instant {
	
	/** The avg voltage. */
	private double avgVoltage;
	
	/** The avg angle. */
	private double avgAngle;
	
	/** The instant. */
	private int instant;
	
	/** The analog measurements. */
	private ArrayList<AnalogMeasurement> analogMeasurements=new ArrayList<AnalogMeasurement>();
	
	/** The belongs to clusters. */
	private Cluster belongsToClusters;
	
	/**
	 * Instantiates a new instant.
	 *
	 * @param instant the instant
	 * @param analogMeasurements the analog measurements
	 */
	public Instant(int instant, ArrayList<AnalogMeasurement> analogMeasurements){
		this.instant=instant;
		this.analogMeasurements=analogMeasurements;
	}
	
	/**
	 * Gets the avg voltage.
	 *
	 * @return the avg voltage
	 */
	public double getAvgVoltage(){
		double voltageSum=0;
		for(int i=0;i<analogMeasurements.size();i++){
			voltageSum+=analogMeasurements.get(i).getVoltageValue();
		}
		avgVoltage=voltageSum/analogMeasurements.size();
		return avgVoltage;
	}
	
	/**
	 * Gets the avg angle.
	 *
	 * @return the avg angle
	 */
	public double getAvgAngle(){
		double angleSum=0;
		for(int i=0;i<analogMeasurements.size();i++){
			angleSum+=analogMeasurements.get(i).getAngleValue();
		}
		avgAngle=angleSum/analogMeasurements.size();
		return avgAngle;
	}
	
	/**
	 * Gets the instant.
	 *
	 * @return the instant
	 */
	public int getInstant(){
		return instant;
	}
	
	/**
	 * Gets the cluster.
	 *
	 * @return the cluster
	 */
	public Cluster getCluster(){
		return belongsToClusters;
	}
	
	/**
	 * Sets the cluster.
	 *
	 * @param a the new cluster
	 */
	public void setCluster(Cluster a){
		belongsToClusters=a;
	}
	
	/**
	 * Gets the analog measurements.
	 *
	 * @return the analog measurements
	 */
	public ArrayList<AnalogMeasurement> getAnalogMeasurements(){
		return analogMeasurements;
	}

}
