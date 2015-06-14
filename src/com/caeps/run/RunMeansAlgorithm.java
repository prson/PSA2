/* The Class RunMeansAlgorithm, contains the necessary function to form the clusters given a set of mesurement instants.
 * 
 * Author: Pratik Sonthalia, Radhakrishnan Natarajan
 * Date: 14 June' 15.
 * 
 */
package com.caeps.run;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.caeps.loadDatabase.AnalogMeasurement;
import com.caeps.loadDatabase.Cluster;
import com.caeps.loadDatabase.Instant;

/**
 * The Class RunMeansAlgorithm, contains the necessary function to form the clusters given a set of mesurement instants.
 */
public class RunMeansAlgorithm {

	/** The logger. */
	private static Logger logger = Logger.getLogger(RunMeansAlgorithm.class);
	
	/** The clusters. */
	static ArrayList<Cluster> clusters = new ArrayList<Cluster>();
	
	/** The instants. */
	ArrayList<Instant> instants = new ArrayList<Instant>();
	
	/**
	 * Form clusters.
	 *
	 * @param connTraining the conn training
	 */
	public void formClusters(Connection connTraining) {
		try {
			String dataFile = "initial_data.csv";
			// Define buffer and split element
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(dataFile));
			} catch (FileNotFoundException e1) {
				logger.debug("Error while trying to load the initial data file"+e1);
			}// load a file into buffer;
			String line;

			String query = "Select * from substations";
			for (int i = 1; i <= 4; i++) {
				ArrayList<AnalogMeasurement> analogMeasurements = new ArrayList<AnalogMeasurement>();
				PreparedStatement stat = connTraining.prepareStatement(query);
				ResultSet rs = stat.executeQuery();
				while (rs.next()) {
					String rdfId = rs.getString("rdfid");
					try {

						if ((line = br.readLine()) != null) {
							// Read each line of file
							String a[] = line.split(",");
							AnalogMeasurement analogMeasurementObj = new AnalogMeasurement(0, Double.parseDouble(a[0]),Double.parseDouble(a[1]), rdfId);
							analogMeasurements.add(analogMeasurementObj);
						}
					} catch (NumberFormatException e) {
						logger.debug("Number format exception while forming initial clusters from the initial data file\n"+e);
					} catch (IOException e) {
						logger.debug("IO exception while forming initial clusters from the initial data file\n"+e);
					}
				}
				Cluster clusterObj = new Cluster(i, analogMeasurements);
				clusters.add(clusterObj);
			}
			displayClusters(clusters);

			query = "SELECT MAX(time) FROM analog_meas";
			PreparedStatement stat = connTraining.prepareStatement(query);
			ResultSet rs = stat.executeQuery();
			rs.next();
			int numOfTrainingData = rs.getInt("MAX(time)");
			logger.info("Number of training data"+numOfTrainingData);
			// Creating the instants from the database
			for (int i = 1; i <= numOfTrainingData; i++) {
				query = "Select * from substations";
				ArrayList<AnalogMeasurement> analogMeasurements = new ArrayList<AnalogMeasurement>();
				stat = connTraining.prepareStatement(query);
				rs = stat.executeQuery();
				while (rs.next()) {
					String rdfId = rs.getString("rdfid");
					query = "Select * from analog_meas where sub_rdfid='"
							+ rdfId + "' and time=" + i
							+ " and name like '%VOLT%'";
//					 logger.debug(query);
					stat = connTraining.prepareStatement(query);
					ResultSet rs1 = stat.executeQuery();
					rs1.next();
					double voltageValue = rs1.getDouble("value");
					query = "Select * from analog_meas where sub_rdfid='"
							+ rdfId + "' and time=" + i
							+ " and name like '%ANG%'";
//					 logger.debug(query);
					stat = connTraining.prepareStatement(query);
					ResultSet rs2 = stat.executeQuery();
					rs2.next();
					double angleValue = rs2.getDouble("value");
					AnalogMeasurement am = new AnalogMeasurement(i, angleValue,	voltageValue, rdfId);
					analogMeasurements.add(am);
				}
//				displayAnlogMeas(analogMeasurements);
				Instant instantObj = new Instant(i, analogMeasurements);
				instants.add(instantObj);
			}

		} catch (SQLException e) {
			logger.debug("SQL Error while retrieving measurement and substation from the database\n"+e);
		} finally {
			if (connTraining != null) {
				try {
					connTraining.close();
				} catch (SQLException e) {
					logger.debug("Error while closing the database connection\n"+e);
				}
			}

		}

		logger.debug("************************************Starting the KMeans Algorithm*******************************");
		logger.debug("************************************Initial clusters*******************************");
		displayClusters(clusters);
		logger.debug("************************************Initial clusters*******************************");
		clusters = calculateKMeans(instants, clusters);
		clusters = compareClusters(clusters);
		logger.debug("************************************Resulting clusters*****************************************************");
		displayClusters(clusters);
		logger.debug("************************************Resulting clusters*****************************************************");
		logger.debug("************************************Resulting learning instants*****************************************************");
		displayInstants(instants);
		logger.debug("************************************Resulting learning instants*****************************************************");
	}

	/**
	 * Calculate k means.
	 *
	 * @param instants the instants
	 * @param clusters the clusters
	 * @return the array list
	 */
	public ArrayList<Cluster> calculateKMeans(
			ArrayList<Instant> instants, ArrayList<Cluster> clusters) {
		ArrayList<Cluster> newClusters = new ArrayList<Cluster>();
		int iteration = 1;
		do {
			newClusters = new ArrayList<Cluster>();
			 logger.debug("Initial cluster at iteration "+iteration+":");
			for (int j = 0; j < clusters.size(); j++) {
				Cluster clusterObj = new Cluster(clusters.get(j).getLabel(),
						clusters.get(j).getAnalogMeasurements());
				newClusters.add(clusterObj);
			}
			 displayClusters(newClusters);
			newClusters = compareClusters(newClusters);

			for (int i = 0; i < instants.size(); i++) {
				Instant instantObj = instants.get(i);
				Cluster a = getCluster(instantObj, newClusters);
				instantObj.setCluster(a);
				instants.set(i, instantObj);
			}

			for (int j = 0; j < clusters.size(); j++) {
				Cluster clusterObj = clusters.get(j);
				ArrayList<AnalogMeasurement> analogMeasurementsForCluster = findClusterMean(
						instants, clusterObj);
				clusterObj.setAnalogMeasurements(analogMeasurementsForCluster);
				clusters.set(j, clusterObj);

			}
			 logger.debug("Cluster After iteration "+iteration+":");
			 displayClusters(clusters);
			iteration++;
		} while (!converge(clusters, newClusters));
		logger.debug("Clustering converges after "+iteration+" iterations");
		return clusters;
	}

	/**
	 * Find cluster mean.
	 *
	 * @param instants the instants
	 * @param clusterObj the cluster obj
	 * @return the array list
	 */
	public static ArrayList<AnalogMeasurement> findClusterMean(
			ArrayList<Instant> instants, Cluster clusterObj) {

		ArrayList<AnalogMeasurement> analogMeasurements = new ArrayList<AnalogMeasurement>();
		for (int j = 0; j < clusterObj.getAnalogMeasurements().size(); j++) {
			double voltageSum = 0;
			double angleSum = 0;
			int count = 0;
			for (int i = 0; i < instants.size(); i++) {
				if (instants.get(i).getCluster().getLabel() == clusterObj
						.getLabel()) {
					voltageSum = voltageSum
							+ instants.get(i).getAnalogMeasurements().get(j)
									.getVoltageValue();
					angleSum = angleSum
							+ instants.get(i).getAnalogMeasurements().get(j)
									.getAngleValue();
					count++;

				}
			}
			AnalogMeasurement analogMeasurementObj = null;
			if (count != 0) {
				 logger.info("Count ="+count+" for cluster"+clusterObj.getLabel());
				analogMeasurementObj = new AnalogMeasurement(0, angleSum/ count, voltageSum / count, clusterObj.getAnalogMeasurements().get(j).getSubstationRdfID());
			} else {
				 logger.info("Count becomes zero for cluster "+clusterObj.getLabel());
				analogMeasurementObj = clusterObj.getAnalogMeasurements()
						.get(j);
			}
			analogMeasurements.add(analogMeasurementObj);
		}
		return analogMeasurements;
	}

	/**
	 * Gets the cluster.
	 *
	 * @param instant the instant
	 * @param clusters the clusters
	 * @return the cluster
	 */
	public static Cluster getCluster(Instant instant,
			ArrayList<Cluster> clusters) {
		Cluster a = null;
		double minDist = Double.POSITIVE_INFINITY;
		for (int i = 0; i < clusters.size(); i++) {
			double distSum = 0;
			for (int j = 0; j < instant.getAnalogMeasurements().size(); j++) {
				distSum = distSum
						+ Math.pow(instant.getAnalogMeasurements().get(j)
								.getAngleValue()
								- clusters.get(i).getAnalogMeasurements()
										.get(j).getAngleValue(), 2)
						+ Math.pow(instant.getAnalogMeasurements().get(j)
								.getVoltageValue()
								- clusters.get(i).getAnalogMeasurements()
										.get(j).getVoltageValue(), 2);
			}

			double dist = Math.pow(distSum, 0.5);
			if (dist < minDist) {
				minDist = dist;
				a = clusters.get(i);
			}
		}
		return a;
	}

	/**
	 * Converge.
	 *
	 * @param clusters the clusters
	 * @param newClusters the new clusters
	 * @return true, if successful
	 */
	public static boolean converge(ArrayList<Cluster> clusters,
			ArrayList<Cluster> newClusters) {
		// displayClusters(newClusters);
		// displayClusters(clusters);
		boolean converge = true;
		for (int i = 0; i < clusters.size(); i++) {
			for (int j = 0; j < clusters.get(i).getAnalogMeasurements().size(); j++) {
				if (clusters.get(i).getAnalogMeasurements().get(j)
						.getAngleValue() != newClusters.get(i)
						.getAnalogMeasurements().get(j).getAngleValue()) {
					converge = false;
					break;
				}
				if (clusters.get(i).getAnalogMeasurements().get(j)
						.getVoltageValue() != newClusters.get(i)
						.getAnalogMeasurements().get(j).getVoltageValue()) {
					converge = false;
					break;
				}
			}
		}
		logger.debug("Converge? " + converge);
		return converge;
	}

	/**
	 * State.
	 *
	 * @param a the a
	 * @param trainingAnalogMeasurements the training analog measurements
	 * @return the cluster
	 */
	public static Cluster state(int a[],
			ArrayList<AnalogMeasurement> trainingAnalogMeasurements) {
		AnalogMeasurement analogMeasurementObj = null;
		double max = Double.NEGATIVE_INFINITY;
		int maxIndex = -1;
		for (int k = 0; k < a.length; k++) {
			if (max < a[k]) {
				maxIndex = k;
				max = a[k];
			}
		}
		maxIndex++;
		for (int i = 0; i < trainingAnalogMeasurements.size(); i++) {
			if (trainingAnalogMeasurements.get(i).getCluster().getLabel() == maxIndex) {
				analogMeasurementObj = trainingAnalogMeasurements.get(i);
			}
		}
		return analogMeasurementObj.getCluster();
	}

	/**
	 * Display clusters.
	 *
	 * @param clusters the clusters
	 */
	static public void displayClusters(ArrayList<Cluster> clusters) {
		for (int j = 0; j < clusters.size(); j++) {
			logger.debug("Cluster: " + clusters.get(j).getLabel());
			for (int i = 0; i < clusters.get(j).getAnalogMeasurements().size(); i++) {
				logger.debug("For substation "
						+ clusters.get(j).getAnalogMeasurements().get(i)
								.getSubstationRdfID()
						+ ": "
						+ clusters.get(j).getAnalogMeasurements().get(i)
								.getVoltageValue()
						+ ", "
						+ clusters.get(j).getAnalogMeasurements().get(i)
								.getAngleValue());
			}
			logger.debug("Average Voltage: " + clusters.get(j).getVoltage()
					+ ", Average Angle: " + clusters.get(j).getAngle());
			logger.debug("Cluster Description: " + clusters.get(j).getDesc());
		}
	}

	/**
	 * Display analog measurements
	 *
	 * @param analogMeasurements the analog measurements
	 */
	public void displayAnlogMeas(
			ArrayList<AnalogMeasurement> analogMeasurements) {
		for (int j = 0; j < analogMeasurements.size(); j++) {
			logger.debug(analogMeasurements.get(j).getInstant() + ":"
					+ analogMeasurements.get(j).getSubstationRdfID() + ":"
					+ analogMeasurements.get(j).getAngleValue() + ":"
					+ analogMeasurements.get(j).getVoltageValue());
		}
	}

	/**
	 * Display instants.
	 *
	 * @param instants the instants
	 */
	public void displayInstants(ArrayList<Instant> instants) {
		for (int j = 0; j < instants.size(); j++) {
			logger.debug(instants.get(j).getInstant() + ":"
					+ instants.get(j).getAvgAngle() + ":"
					+ instants.get(j).getAvgVoltage() + ":"
					+ instants.get(j).getCluster().getDesc());
		}
	}

	/**
	 * Compare clusters.
	 *
	 * @param clusters the clusters
	 * @return the array list
	 */
	public ArrayList<Cluster> compareClusters(ArrayList<Cluster> clusters) {
		double a[] = { clusters.get(0).getVoltage(),
				clusters.get(1).getVoltage(), clusters.get(2).getVoltage(),
				clusters.get(3).getVoltage() };
		a = bubblesort(a);
		for (int i = 0; i < clusters.size(); i++) {
			if (clusters.get(i).getVoltage() == a[0])
				clusters.get(i).setDesc("Day-time high load");
			else if (clusters.get(i).getVoltage() == a[1])
				clusters.get(i).setDesc("Line on maintenance");
			else if (clusters.get(i).getVoltage() == a[2])
				clusters.get(i).setDesc("Generator on Maintenance");
			else if (clusters.get(i).getVoltage() == a[3])
				clusters.get(i).setDesc("Night-time lower load");
		}
		return clusters;
	}

	/**
	 * Bubblesort.
	 *
	 * @param a the a
	 * @return the double[]
	 */
	static double[] bubblesort(double a[]) {
		for (int i = 0; i < a.length; i++) {
			for (int j = i + 1; j < a.length; j++) {
				if (a[i] > a[j]) {
					double temp = a[i];
					a[i] = a[j];
					a[j] = temp;
				}
			}
		}
		return a;
	}
	
	/**
	 * Gets the clusters.
	 *
	 * @return the clusters
	 */
	public ArrayList<Cluster> getClusters(){
		return clusters;
	}
	
	/**
	 * Gets the instants.
	 *
	 * @return the instants
	 */
	public ArrayList<Instant> getInstants(){
		return instants;
	}
}