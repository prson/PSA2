package com.caeps.run;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.caeps.loadDatabase.AnalogMeasurement;
import com.caeps.loadDatabase.AnalogMeasurements;
import com.caeps.loadDatabase.Cluster;
import com.caeps.loadDatabase.ConnectToDB;
import com.caeps.loadDatabase.Instant;
import com.caeps.loadDatabase.Substation;

public class RunMeansAlgorithm {

	private static Logger logger = Logger.getLogger(RunMeansAlgorithm.class);
	static ArrayList<Cluster> clusters = new ArrayList<Cluster>();

	public static void main(String args[]) {
		ConnectToDB connectDBObj = new ConnectToDB();
		Connection conn = connectDBObj.establishConnection(
				"jdbc:mysql://localhost:3306/powersystemassignment2", "root",
				"root");

		Cluster a = new Cluster(1);
		a.setAngle(0);
		a.setVoltage(1);
		clusters.add(a);
		Cluster b = new Cluster(2);
		b.setAngle(5);
		b.setVoltage(1);
		clusters.add(b);
		Cluster c = new Cluster(3);
		c.setAngle(-5);
		c.setVoltage(1);
		clusters.add(c);
		Cluster d = new Cluster(4);
		d.setAngle(-10);
		d.setVoltage(1);
		clusters.add(d);

		ArrayList<AnalogMeasurement> analogMeasurements = new ArrayList<AnalogMeasurement>();
		ArrayList<Instant> instants = null;
		String query = "Select * from substations";
		try {
			PreparedStatement stat = conn.prepareStatement(query);
			ResultSet rs = stat.executeQuery();
			while (rs.next()) {

				String rdfId = rs.getString("rdfid");
				for (int i = 1; i <= 200; i++) {
					query = "Select * from analog_meas where sub_rdfid='"
							+ rdfId + "' and time=" + i
							+ " and name like '%VOLT%'";
					// System.out.println(query);
					stat = conn.prepareStatement(query);
					ResultSet rs1 = stat.executeQuery();
					rs1.next();
					double voltageValue = rs1.getDouble("value");
					query = "Select * from analog_meas where sub_rdfid='"
							+ rdfId + "' and time=" + i
							+ " and name like '%ANG%'";
					// System.out.println(query);
					stat = conn.prepareStatement(query);
					ResultSet rs2 = stat.executeQuery();
					rs2.next();
					double angleValue = rs2.getDouble("value");
					AnalogMeasurement am = new AnalogMeasurement(i, angleValue,
							voltageValue, rdfId);
					analogMeasurements.add(am);
				}
			}

			instants = new ArrayList<Instant>();
			double voltSumInstant = 0;
			double angleSumInstant = 0;
			instants = new ArrayList<Instant>();
			for (int i = 1; i <= 200; i++) {
				query = "Select * from analog_meas where name like '%VOLT%' and time="
						+ i;
				stat = conn.prepareStatement(query);
				ResultSet rsInstant = stat.executeQuery();
				voltSumInstant = 0;
				int count1 = 0;
				int count2 = 0;
				while (rsInstant.next()) {
					voltSumInstant = voltSumInstant
							+ rsInstant.getDouble("value");
					count1++;
				}
				query = "Select * from analog_meas where name like '%ANG%' and time="
						+ i;
				stat = conn.prepareStatement(query);
				rsInstant = stat.executeQuery();
				angleSumInstant = 0;
				while (rsInstant.next()) {
					angleSumInstant = angleSumInstant
							+ rsInstant.getDouble("value");
					count2++;
				}
				double avgVoltInstant = voltSumInstant / count1;
				double avgAngleInstant = angleSumInstant / count2;
				Instant instantObj = new Instant(i, avgVoltInstant,
						avgAngleInstant);
				instants.add(instantObj);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		
		clusters = calculateKMeans(analogMeasurements, clusters);
		clusters = compareClusters(clusters);
		logger.debug("************************************Starting with Method 1*******************************");
		logger.debug("************************************Resulting clusters based on Method 1*****************************************************");
		displayClusters(clusters);
		logger.debug("************************************Resulting clusters mabsed on Method 1*****************************************************");
		logger.debug("************************************Resulting analog measurements and relation based on Method 1*****************************************************");
		displayAnlogMeas(analogMeasurements);
		logger.debug("************************************Resulting analog measurements and relation based on Method 1*****************************************************");
		RunKNNAlgorithmSubstation runKnnAlgorithmSubstationObj=new RunKNNAlgorithmSubstation();
		ArrayList<AnalogMeasurement> testAnalogMeasurements=runKnnAlgorithmSubstationObj.runKNNAlgorithmSubstation(analogMeasurements);
		ArrayList<Instant> testInstants=calculateTestInstantSubstation(testAnalogMeasurements, analogMeasurements);
		logger.debug(testInstants.size());
		logger.debug("*************************************Results of Test Data Instants (Method 1)****************************************");
		displayInstants(testInstants);
		logger.debug("*************************************Results of Test Data Instants (Method 1)****************************************");
		
		logger.debug("************************************Starting with Method 2**************************************");
		clusters = calculateKMeansInstant(instants, clusters);
		logger.debug("*************************************Resulting clusters from Training Data (Method 2)*****************************************************");
		displayClusters(clusters);
		clusters = compareClusters(clusters);
		logger.debug("*************************************Resulting clusters from Training Data (Method 2)*****************************************************");
		logger.debug("*************************************Resulting Instants and relation from Training Data (Method 2)*********************************************");
		displayInstants(instants);
		logger.debug("*************************************Resulting Instants and relation from Training Data (Method 2)*********************************************");

		RunKNNAlgorithm runKNNAlgorithmObj = new RunKNNAlgorithm();
		testInstants = runKNNAlgorithmObj.runKNNAlgorithm(instants);
		logger.debug(testInstants.size());
		logger.debug("*************************************Results of Test Data Instants (Method 2) ****************************************");
		displayInstants(testInstants);
		logger.debug("*************************************Results of Test Data Instants (Method 2) ****************************************");

	}

	/*
	 * public static ArrayList<Substation> getSubstations(Connection conn,
	 * ArrayList<AnalogMeasurements> analogMeasurements) { ArrayList<Substation>
	 * substations = new ArrayList<Substation>(); String query =
	 * "Select * from substations"; try { PreparedStatement stat =
	 * conn.prepareStatement(query); ResultSet rs = stat.executeQuery(); while
	 * (rs.next()) { ArrayList<AnalogMeasurements>
	 * analogMeasurementsforEachSubst = new ArrayList<AnalogMeasurements>();
	 * String rdfId = rs.getString("rdfid"); String name = rs.getString("name");
	 * String regionId = rs.getString("region_id"); query =
	 * "Select * from analog_meas where sub_rdfid='" + rdfId + "'";
	 * System.out.println(query); stat = conn.prepareStatement(query); ResultSet
	 * rs1 = stat.executeQuery(); while (rs1.next()) { String rdfIdMeas =
	 * rs1.getString("rdfid"); String nameMeas = rs1.getString("name"); int time
	 * = rs1.getInt("time"); double value = rs1.getDouble("value");
	 * AnalogMeasurements am = new AnalogMeasurements(rdfIdMeas, nameMeas, time,
	 * value); analogMeasurementsforEachSubst.add(am);
	 * analogMeasurements.add(am); } Substation substObj = new Substation(rdfId,
	 * name, analogMeasurementsforEachSubst, regionId);
	 * substations.add(substObj); }
	 * 
	 * } catch (SQLException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); }
	 * 
	 * return substations; }
	 */

	public static ArrayList<Instant> calculateTestInstantSubstation(ArrayList<AnalogMeasurement> testAnalogMeasurements, ArrayList<AnalogMeasurement> trainingAnalogMeasurements) {
		ArrayList<Instant> testInstants=new ArrayList<Instant>();
		double voltSum = 0;
		double angleSum=0;
		int count=0;
		int anlogMeasurementClusterLabel=-1;
		int[] stateCount = new int[4];
		for (int i = 1; i <= 40; i++) {
			for(int j=0;j<testAnalogMeasurements.size();j++){
				if(testAnalogMeasurements.get(j).getInstant()==i){
					voltSum=voltSum+testAnalogMeasurements.get(j).getVoltageValue();
					angleSum=angleSum+testAnalogMeasurements.get(j).getAngleValue();
					count++;
					anlogMeasurementClusterLabel=testAnalogMeasurements.get(j).getCluster().getLabel();
					stateCount[anlogMeasurementClusterLabel- 1]++;
					}
				}
			
			Instant testInstantObj=new Instant(i, voltSum/count, angleSum/count);
			testInstantObj.setCluster(state(stateCount, trainingAnalogMeasurements));
			logger.debug(stateCount[0] + " " + stateCount[1] + " "
					+ stateCount[2] + " " + stateCount[3]);
			testInstants.add(testInstantObj);
			voltSum=0;
			angleSum=0;anlogMeasurementClusterLabel=-1;
			}
		return testInstants;
	}
	
	public static Cluster state(int a[], ArrayList<AnalogMeasurement> trainingAnalogMeasurements) {
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
	
	public static void displaySubst(ArrayList<Substation> substations) {
		for (int i = 0; i < substations.size(); i++) {
			Substation subsObj = substations.get(i);
			System.out.println(subsObj.getName());
			System.out.println(subsObj.getRdfId());
			System.out.println(subsObj.getRegion());
			ArrayList<AnalogMeasurements> analogMeasurementsforEachSubst = subsObj
					.getAnalogMeasurements();
			for (int j = 0; j < analogMeasurementsforEachSubst.size(); j++) {
				AnalogMeasurements analogMeas = analogMeasurementsforEachSubst
						.get(j);
				System.out.print(analogMeas.getInstant() + ":");
				System.out.print(analogMeas.getName() + ":");
				System.out.print(analogMeas.getRdfId() + ":");
				System.out.print(analogMeas.getValue() + ":");
				System.out.println();
			}
		}
	}

	public static ArrayList<Cluster> calculateKMeans(
			ArrayList<AnalogMeasurement> analogMeasurements1,
			ArrayList<Cluster> clusters) {
		boolean converge = false;
		displayClusters(clusters);
		while (!converge) {
			for (int i = 0; i < analogMeasurements1.size(); i++) {
				AnalogMeasurement am = analogMeasurements1.get(i);
				Cluster a = getCluster(am, clusters);
				am.setCluster(a);
				analogMeasurements1.set(i, am);
			}

			for (int j = 0; j < clusters.size(); j++) {
				converge = true;
				Cluster a = clusters.get(j);
				double voltMeanOld = a.getVoltage();
				double angleMeanOld = a.getAngle();
				double voltMean = findVoltageMean(analogMeasurements1, a);
				double angleMean = findAngleMean(analogMeasurements1, a);
				a.setAngle(angleMean);
				a.setVoltage(voltMean);
				clusters.set(j, a);
				if (angleMean != angleMeanOld || voltMean != voltMeanOld)
					converge = false;
			}
			displayClusters(clusters);
		}
		return clusters;
	}

	public static ArrayList<Cluster> calculateKMeansInstant(
			ArrayList<Instant> instants, ArrayList<Cluster> clusters) {
		boolean converge = false;
		displayClusters(clusters);
		while (!converge) {
			for (int i = 0; i < instants.size(); i++) {
				Instant instant = instants.get(i);
				Cluster a = getClusterInstant(instant, clusters);
				instant.setCluster(a);
				instants.set(i, instant);
			}
			// displayClusters(clusters);
			// displayInstants(instants);
			for (int j = 0; j < clusters.size(); j++) {
				converge = true;
				Cluster a = clusters.get(j);
				double voltMeanOld = a.getVoltage();
				double angleMeanOld = a.getAngle();
				double voltMean = findVoltageMeanInstant(instants, a);
				double angleMean = findAngleMeanInstant(instants, a);
				a.setAngle(angleMean);
				a.setVoltage(voltMean);
				clusters.set(j, a);
				if (angleMean != angleMeanOld || voltMean != voltMeanOld)
					converge = false;
			}
			displayClusters(clusters);
		}
		return clusters;
	}

	public static Cluster getCluster(AnalogMeasurement am,
			ArrayList<Cluster> clusters) {
		Cluster a = null;
		double minDist = 100;
		for (int i = 0; i < clusters.size(); i++) {
			double dist = Math.sqrt(Math.pow(
					am.getAngleValue() - clusters.get(i).getAngle(), 2)
					+ (Math.pow(am.getVoltageValue()
							- clusters.get(i).getVoltage(), 2)));
			if (dist < minDist) {
				minDist = dist;
				a = clusters.get(i);
			}
		}
		return a;
	}

	public static Cluster getClusterInstant(Instant am,
			ArrayList<Cluster> clusters) {
		Cluster a = null;
		double minDist = 100;
		for (int i = 0; i < clusters.size(); i++) {
			double dist = Math.sqrt(Math.pow(am.getAvgAngle()
					- clusters.get(i).getAngle(), 2)
					+ (Math.pow(am.getAvgVoltage()
							- clusters.get(i).getVoltage(), 2)));
			if (dist < minDist) {
				minDist = dist;
				a = clusters.get(i);
			}
		}
		return a;
	}

	public static double findVoltageMean(
			ArrayList<AnalogMeasurement> analogMeasurements, Cluster a) {
		double sum = 0;
		int count = 0;
		for (int i = 0; i < analogMeasurements.size(); i++) {
			AnalogMeasurement am = analogMeasurements.get(i);
			if (am.getCluster().getLabel() == a.getLabel()) {
				sum = sum + am.getVoltageValue();
				count = count + 1;
			}
		}
		logger.debug(sum + ":" + count);
		if (count != 0)
			return (sum / count);
		else
			return a.getVoltage();
	}

	public static double findVoltageMeanInstant(ArrayList<Instant> instants,
			Cluster a) {
		double sum = 0;
		int count = 0;
		for (int i = 0; i < instants.size(); i++) {
			Instant am = instants.get(i);
			if (am.getCluster().getLabel() == a.getLabel()) {
				sum = sum + am.getAvgVoltage();
				count = count + 1;
			}
		}
		logger.debug(sum + ":" + count);
		if (count != 0)
			return (sum / count);
		else
			return a.getVoltage();
	}

	public static double findAngleMean(
			ArrayList<AnalogMeasurement> analogMeasurements, Cluster a) {
		double sum = 0;
		int count = 0;
		for (int i = 0; i < analogMeasurements.size(); i++) {
			AnalogMeasurement am = analogMeasurements.get(i);
			if (am.getCluster().getLabel() == a.getLabel()) {
				sum = sum + am.getAngleValue();
				count = count + 1;
			}
		}
		if (count != 0)
			return (sum / count);
		else
			return a.getAngle();
	}

	public static double findAngleMeanInstant(ArrayList<Instant> instants,
			Cluster a) {
		double sum = 0;
		int count = 0;
		for (int i = 0; i < instants.size(); i++) {
			Instant am = instants.get(i);
			if (am.getCluster().getLabel() == a.getLabel()) {
				sum = sum + am.getAvgAngle();
				count = count + 1;
			}
		}
		if (count != 0)
			return (sum / count);
		else
			return a.getAngle();
	}

	static public void displayClusters(ArrayList<Cluster> clusters) {
		for (int j = 0; j < clusters.size(); j++) {
			logger.debug(clusters.get(j).getLabel() + ":"
					+ clusters.get(j).getVoltage() + ":"
					+ clusters.get(j).getAngle());
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
