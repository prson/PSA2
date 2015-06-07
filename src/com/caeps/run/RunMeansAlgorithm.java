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
import com.caeps.loadDatabase.Substation;

public class RunMeansAlgorithm {

	static ArrayList<AnalogMeasurements> analogMeasurements = new ArrayList<AnalogMeasurements>();
	private static Logger logger = Logger.getLogger(RunMeansAlgorithm.class);

	public static void main(String args[]) {
		ConnectToDB connectDBObj = new ConnectToDB();
		Connection conn = connectDBObj.establishConnection(
				"jdbc:mysql://localhost:3306/powersystemassignment2", "root",
				"root");

//		ArrayList<Substation> substations = new ArrayList<Substation>();
//
//		substations = getSubstations(conn, analogMeasurements);
//		// displaySubst(substations);

		ArrayList<Cluster> clusters = new ArrayList<Cluster>();
		Cluster a = new Cluster("1");
		a.setAngle(0);
		a.setVoltage(1);
		clusters.add(a);
		Cluster b = new Cluster("2");
		b.setAngle(5);
		b.setVoltage(1);
		clusters.add(b);
		Cluster c = new Cluster("3");
		c.setAngle(-5);
		c.setVoltage(1);
		clusters.add(c);
		Cluster d = new Cluster("4");
		d.setAngle(-10);
		d.setVoltage(1);
		clusters.add(d);

		ArrayList<AnalogMeasurement> analogMeasurements2 = new ArrayList<AnalogMeasurement>();
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
//					System.out.println(query);
					stat = conn.prepareStatement(query);
					ResultSet rs1 = stat.executeQuery();
					rs1.next();
					double voltageValue = rs1.getDouble("value");
					query = "Select * from analog_meas where sub_rdfid='"
							+ rdfId + "' and time=" + i
							+ " and name like '%ANG%'";
//					System.out.println(query);
					stat = conn.prepareStatement(query);
					ResultSet rs2 = stat.executeQuery();
					rs2.next();
					double angleValue = rs2.getDouble("value");
					AnalogMeasurement am = new AnalogMeasurement(i, angleValue,
							voltageValue, rdfId);
					analogMeasurements2.add(am);
				}
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		clusters = calculateKMeans(analogMeasurements2, clusters);
		displayAnlogMeas(analogMeasurements2);
		
		

		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

/*	public static ArrayList<Substation> getSubstations(Connection conn,
			ArrayList<AnalogMeasurements> analogMeasurements) {
		ArrayList<Substation> substations = new ArrayList<Substation>();
		String query = "Select * from substations";
		try {
			PreparedStatement stat = conn.prepareStatement(query);
			ResultSet rs = stat.executeQuery();
			while (rs.next()) {
				ArrayList<AnalogMeasurements> analogMeasurementsforEachSubst = new ArrayList<AnalogMeasurements>();
				String rdfId = rs.getString("rdfid");
				String name = rs.getString("name");
				String regionId = rs.getString("region_id");
				query = "Select * from analog_meas where sub_rdfid='" + rdfId
						+ "'";
				System.out.println(query);
				stat = conn.prepareStatement(query);
				ResultSet rs1 = stat.executeQuery();
				while (rs1.next()) {
					String rdfIdMeas = rs1.getString("rdfid");
					String nameMeas = rs1.getString("name");
					int time = rs1.getInt("time");
					double value = rs1.getDouble("value");
					AnalogMeasurements am = new AnalogMeasurements(rdfIdMeas,
							nameMeas, time, value);
					analogMeasurementsforEachSubst.add(am);
					analogMeasurements.add(am);
				}
				Substation substObj = new Substation(rdfId, name,
						analogMeasurementsforEachSubst, regionId);
				substations.add(substObj);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return substations;
	}*/

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
			ArrayList<AnalogMeasurement> analogMeasurements,
			ArrayList<Cluster> clusters) {
		boolean converge = false;
		displayClusters(clusters);
		while (!converge) {
			for (int i = 0; i < analogMeasurements.size(); i++) {
				AnalogMeasurement am = analogMeasurements.get(i);
				Cluster a = getCluster(am, clusters);
				am.setCluster(a);
				analogMeasurements.set(i, am);
			}

			for (int j = 0; j < clusters.size(); j++) {
				converge = true;
				Cluster a = clusters.get(j);
				double voltMeanOld = a.getVoltage();
				double angleMeanOld = a.getAngle();
				double voltMean = findVoltageMean(analogMeasurements, a);
				double angleMean = findAngleMean(analogMeasurements, a);
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

	public static Cluster getCluster(AnalogMeasurement am, ArrayList<Cluster> clusters) {
		Cluster a = null;
		double minDist = 100;
		for (int i = 0; i < clusters.size(); i++) {
			double dist = Math.sqrt(Math.pow(
					am.getAngleValue() - clusters.get(i).getAngle(), 2)
					+ (Math.pow(am.getVoltageValue()
							- clusters.get(i).getVoltage(), 2)));
			if (dist < minDist) {
				minDist=dist;
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
		logger.debug(sum+":"+count);
		if(count!=0)
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
		if(count!=0)
			return (sum / count);
		else
			return a.getAngle();
	}
	
	static public void displayClusters(ArrayList<Cluster> clusters){
		for (int j = 0; j < clusters.size(); j++) {
			logger.debug(clusters.get(j).getLabel()+":"+clusters.get(j).getVoltage()+":"+clusters.get(j).getAngle());
		}
	}
	
	static public void displayAnlogMeas(ArrayList<AnalogMeasurement> analogMeasurements){
		for (int j = 0; j < analogMeasurements.size(); j++) {
			logger.debug(analogMeasurements.get(j).getInstant()+":"+analogMeasurements.get(j).getSubstationRdfID()+":"+analogMeasurements.get(j).getAngleValue()+":"+analogMeasurements.get(j).getVoltageValue()+":"+analogMeasurements.get(j).getCluster().getLabel());
		}
	}
}
