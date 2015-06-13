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

public class RunMeansAlgorithm {

	private static Logger logger = Logger.getLogger(RunMeansAlgorithm.class);
	static ArrayList<Cluster> clusters = new ArrayList<Cluster>();

	public static void main(Connection connTraining, Connection connTest) {
		int k = 7;
		ConnectToDB connectDBObj = new ConnectToDB();
		connTraining = connectDBObj.establishConnection(
				"jdbc:mysql://localhost:3306/powersystemassignment2", "root",
				"root");
		ArrayList<Instant> instants = new ArrayList<Instant>();

		try {
			String dataFile = "initial_data.csv";
			// Define buffer and split element
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(dataFile));
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}// load a file into buffer;
			String line;
			int countSubst = 0;

			String query = "Select * from substations";
			for (int i = 1; i <= 4; i++) {
				ArrayList<AnalogMeasurement> analogMeasurements = new ArrayList<AnalogMeasurement>();
				PreparedStatement stat = connTraining.prepareStatement(query);
				ResultSet rs = stat.executeQuery();
				countSubst = 0;
				while (rs.next()) {
					String rdfId = rs.getString("rdfid");
					try {

						if ((line = br.readLine()) != null) {
							// Read each line of file
							String a[] = line.split(",");
							AnalogMeasurement analogMeasurementObj = new AnalogMeasurement(
									0, Double.parseDouble(a[0]),
									Double.parseDouble(a[1]), rdfId);
							analogMeasurements.add(analogMeasurementObj);
						}
						countSubst++;
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				Cluster clusterObj = new Cluster(i, analogMeasurements);
				clusters.add(clusterObj);
			}

			query = "SELECT MAX(time) FROM analog_meas";
			PreparedStatement stat = connTraining.prepareStatement(query);
			ResultSet rs = stat.executeQuery();
			rs.next();
			int numOfTrainingData = rs.getInt("MAX(time)");

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
					// logger.debug(query);
					stat = connTraining.prepareStatement(query);
					ResultSet rs1 = stat.executeQuery();
					rs1.next();
					double voltageValue = rs1.getDouble("value");
					query = "Select * from analog_meas where sub_rdfid='"
							+ rdfId + "' and time=" + i
							+ " and name like '%ANG%'";
					// logger.debug(query);
					stat = connTraining.prepareStatement(query);
					ResultSet rs2 = stat.executeQuery();
					rs2.next();
					double angleValue = rs2.getDouble("value");
					AnalogMeasurement am = new AnalogMeasurement(i, angleValue,
							voltageValue, rdfId);
					analogMeasurements.add(am);
				}
				Instant instantObj = new Instant(i, analogMeasurements);
				instants.add(instantObj);
				// if(i<=4 && i>=1){
				// Cluster clusterObj=new Cluster(i, analogMeasurements);
				// clusters.add(clusterObj);
				// }
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (connTraining != null) {
				try {
					connTraining.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
		RunKNNAlgorithm runKnnAlgorithmSubstationObj = new RunKNNAlgorithm();
		ArrayList<Instant> testInstants = runKnnAlgorithmSubstationObj
				.runKNNAlgorithm(connTest,instants, k);
		logger.debug(testInstants.size());
		logger.debug("*************************************Results of Test Data Instants****************************************");
		displayInstants(testInstants);
		logger.debug("*************************************Results of Test Data Instants****************************************");
	}

	public static ArrayList<Cluster> calculateKMeans(
			ArrayList<Instant> instants, ArrayList<Cluster> clusters) {
		ArrayList<Cluster> newClusters = new ArrayList<Cluster>();
		int iteration = 1;
		do {
			newClusters = new ArrayList<Cluster>();
			// logger.debug("Initial cluster at iteration "+iteration+":");
			for (int j = 0; j < clusters.size(); j++) {
				Cluster clusterObj = new Cluster(clusters.get(j).getLabel(),
						clusters.get(j).getAnalogMeasurements());
				newClusters.add(clusterObj);
			}
			// displayClusters(newClusters);
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
			// logger.debug("Cluster After iteration "+iteration+":");
			// displayClusters(clusters);
			iteration++;
		} while (!converge(clusters, newClusters));
		logger.debug("Clustering converges after "+iteration+" iterations");
		return clusters;
	}

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
				analogMeasurementObj = new AnalogMeasurement(0, angleSum
						/ count, voltageSum / count, clusterObj
						.getAnalogMeasurements().get(j).getSubstationRdfID());
			} else {
				 logger.info("Count becomes zero for cluster "+clusterObj.getLabel());
				analogMeasurementObj = clusterObj.getAnalogMeasurements()
						.get(j);
			}
			analogMeasurements.add(analogMeasurementObj);
		}
		return analogMeasurements;
	}

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

	static public void displayAnlogMeas(
			ArrayList<AnalogMeasurement> analogMeasurements) {
		for (int j = 0; j < analogMeasurements.size(); j++) {
			logger.debug(analogMeasurements.get(j).getInstant() + ":"
					+ analogMeasurements.get(j).getSubstationRdfID() + ":"
					+ analogMeasurements.get(j).getAngleValue() + ":"
					+ analogMeasurements.get(j).getVoltageValue() + ":"
					+ analogMeasurements.get(j).getCluster().getDesc());
		}
	}

	static public void displayInstants(ArrayList<Instant> instants) {
		for (int j = 0; j < instants.size(); j++) {
			logger.debug(instants.get(j).getInstant() + ":"
					+ instants.get(j).getAvgAngle() + ":"
					+ instants.get(j).getAvgVoltage() + ":"
					+ instants.get(j).getCluster().getDesc());
		}
	}

	static public ArrayList<Cluster> compareClusters(ArrayList<Cluster> clusters) {
		double a[] = { clusters.get(0).getVoltage(),
				clusters.get(1).getVoltage(), clusters.get(2).getVoltage(),
				clusters.get(3).getVoltage() };
		a = bubblesort(a);
		for (int i = 0; i < clusters.size(); i++) {
			if (clusters.get(i).getVoltage() == a[0])
				clusters.get(i).setDesc("Generator on service");
			else if (clusters.get(i).getVoltage() == a[1])
				clusters.get(i).setDesc("Line on service");
			else if (clusters.get(i).getVoltage() == a[2])
				clusters.get(i).setDesc("Day-time higher load");
			else if (clusters.get(i).getVoltage() == a[3])
				clusters.get(i).setDesc("Night time lower load");
		}
		return clusters;
	}

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

}
