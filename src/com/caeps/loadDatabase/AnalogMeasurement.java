/* The java class containing the analog measurements pertaining to a particular substation 
 * It contains the voltage and angle values measures at the respective substation
 * The measurment belongs to a particular cluster and substation
 * 
 * Author: Pratik Sonthalia, Radhakrishnan Natarajan
 * Date: 14 June' 15
 * 
 */
package com.caeps.loadDatabase;

/**
 * The Class AnalogMeasurement.
 */
public class AnalogMeasurement {

	/** The instant at which the measurment is done (corresponding to the time column in database) */
	private int instant;

	/** The angle value of the measurement. */
	private double angleValue;

	/** The voltage value of the measurement */
	private double voltageValue;

	/** The cluster to which the measurement belongs to */
	private Cluster cluster;

	/** The substation id to which the measurement belongs to*/
	private String substationID;

	/**
	 * Instantiates a new analog measurement.
	 *
	 * @param instant
	 *            the instant
	 * @param angleValue
	 *            the angle value
	 * @param voltageValue
	 *            the voltage value
	 * @param substationID
	 *            the substation id
	 */
	public AnalogMeasurement(int instant, double angleValue,
			double voltageValue, String substationID) {
		this.instant = instant;
		this.voltageValue = voltageValue;
		this.angleValue = angleValue;
		this.substationID = substationID;
	}

	/**
	 * Gets the instant.
	 *
	 * @return the instant
	 */
	public int getInstant() {
		return instant;
	}

	/**
	 * Gets the voltage value.
	 *
	 * @return the voltage value
	 */
	public double getVoltageValue() {
		return voltageValue;
	}

	/**
	 * Gets the angle value.
	 *
	 * @return the angle value
	 */
	public double getAngleValue() {
		return angleValue;
	}

	/**
	 * Gets the substation rdf id.
	 *
	 * @return the substation rdf id
	 */
	public String getSubstationRdfID() {
		return substationID;
	}

	/**
	 * Gets the cluster.
	 *
	 * @return the cluster
	 */
	public Cluster getCluster() {
		return cluster;
	}

	/**
	 * Sets the cluster.
	 *
	 * @param a
	 *            the new cluster
	 */
	public void setCluster(Cluster a) {
		cluster = a;
	}

}