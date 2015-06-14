/* The java class containing the attributes to a cluster, like the analog Measurement
 * values in the cluster
 * The average of the analog measurement, the label and the description.
 * 
 * Author: Pratik Sonthalia, Radhakrishnan Natarajan
 * Date: 14 June' 15
 * 
 */

package com.caeps.loadDatabase;

import java.util.ArrayList;

// TODO: Auto-generated Javadoc
/**
 * The Class Cluster.
 */
public class Cluster {
	
	/** The cluster label. */
	private int clusterLabel;
	
	/** The voltage. */
	private double voltage;
	
	/** The angle. */
	private double angle;
	
	/** The descr. */
	private String descr;
	
	/** The analog measurements. */
	private ArrayList<AnalogMeasurement> analogMeasurements=new ArrayList<AnalogMeasurement>();
	
	/**
	 * Instantiates a new cluster.
	 *
	 * @param clusterLabel the cluster label
	 * @param analogMeasurements the analog measurements
	 */
	public Cluster(int clusterLabel, ArrayList<AnalogMeasurement> analogMeasurements){
		this.clusterLabel=clusterLabel;
		this.analogMeasurements=analogMeasurements;
	}
	
	/**
	 * Instantiates a new cluster.
	 *
	 * @param cluster the cluster
	 */
	public Cluster(Cluster cluster) {
		this.clusterLabel=cluster.getLabel();
		this.analogMeasurements=cluster.getAnalogMeasurements();
	}

	/**
	 * Gets the voltage.
	 *
	 * @return the voltage
	 */
	public double getVoltage(){
		double voltageSum=0;
		for(int i=0;i<analogMeasurements.size();i++){
			voltageSum+=analogMeasurements.get(i).getVoltageValue();
		}
		voltage=voltageSum/analogMeasurements.size();
		return voltage;
	}
	
	/**
	 * Gets the angle.
	 *
	 * @return the angle
	 */
	public double getAngle(){
		double angleSum=0;
		for(int i=0;i<analogMeasurements.size();i++){
			angleSum+=analogMeasurements.get(i).getAngleValue();
		}
		angle=angleSum/analogMeasurements.size();
		return angle;
	}

	/**
	 * Gets the label.
	 *
	 * @return the label
	 */
	public int getLabel(){
		return clusterLabel;
	}
	
	/**
	 * Gets the desc.
	 *
	 * @return the desc
	 */
	public String getDesc(){
		return descr;
	}
	
	/**
	 * Sets the desc.
	 *
	 * @param d the new desc
	 */
	public void setDesc(String d){
		descr=d;
	}
	
	/**
	 * Sets the analog measurements.
	 *
	 * @param am the new analog measurements
	 */
	public void setAnalogMeasurements(ArrayList<AnalogMeasurement> am){
		this.analogMeasurements=am;
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
