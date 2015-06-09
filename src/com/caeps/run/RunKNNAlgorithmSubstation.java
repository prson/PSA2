package com.caeps.run;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.caeps.loadDatabase.AnalogMeasurement;
import com.caeps.loadDatabase.Cluster;
import com.caeps.loadDatabase.ConnectToDB;
import com.caeps.loadDatabase.Instant;

public class RunKNNAlgorithmSubstation {
	
	private static Logger logger = Logger.getLogger(RunMeansAlgorithm.class);
	ArrayList<Instant> instants = new ArrayList<Instant>();

	ArrayList<AnalogMeasurement> runKNNAlgorithmSubstation(ArrayList<AnalogMeasurement> trainingAnalogMeasurements) {

		ConnectToDB connectDBObj = new ConnectToDB();
		Connection conn = connectDBObj.establishConnection(
				"jdbc:mysql://localhost:3306/powersystemassignment2testdata",
				"root", "root");
		ArrayList<AnalogMeasurement> analogMeasurements = new ArrayList<AnalogMeasurement>();
		try {
			
			for (int i = 1; i <= 200; i++) {
				String query = "Select * from substations";
				PreparedStatement stat = conn.prepareStatement(query);
				ResultSet rs = stat.executeQuery();
				while (rs.next()) {
					String rdfId = rs.getString("rdfid");
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
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		analogMeasurements= calculateStateForAnalogMeasurements(analogMeasurements, trainingAnalogMeasurements, 7);
		return analogMeasurements;
	}
	
	ArrayList<AnalogMeasurement> calculateStateForAnalogMeasurements(ArrayList<AnalogMeasurement> analogMeasurements,
			ArrayList<AnalogMeasurement> trainingAnalogMeasurements, int k) {
		for (int i = 0; i < analogMeasurements.size(); i++) {
			ArrayList<AnalogMeasurement> kNearestAnalogMeasurements= new ArrayList<AnalogMeasurement>();
			ArrayList<AnalogMeasurement> trainingAnalogMeasurementsClipped = new ArrayList<AnalogMeasurement>(trainingAnalogMeasurements);
			int[] stateCount = new int[4];
			double distMin = Double.POSITIVE_INFINITY;
			AnalogMeasurement nearestAnalogMeasurement= null;
			logger.debug("Test Analog Measurement: " + (i + 1));
			for (int m = 1; m <= k; m++) {
				for (int j = 0; j < trainingAnalogMeasurementsClipped.size(); j++) {
					double dist = Math.pow(
							Math.pow(analogMeasurements.get(i).getVoltageValue()
									- trainingAnalogMeasurementsClipped.get(j)
											.getVoltageValue(), 2)
									+ Math.pow(analogMeasurements.get(i).getAngleValue()
											- trainingAnalogMeasurementsClipped.get(j)
													.getAngleValue(), 2), 0.5);
					if (dist < distMin) {
						nearestAnalogMeasurement = trainingAnalogMeasurementsClipped.get(j);
						distMin = dist;
					}
				}
				distMin = Double.POSITIVE_INFINITY;
				kNearestAnalogMeasurements.add(nearestAnalogMeasurement);
				logger.debug("Least Dist sample " + m + ": "
						+ nearestAnalogMeasurement.getInstant());
				trainingAnalogMeasurementsClipped.remove(nearestAnalogMeasurement);
				// logger.debug(trainingInstantsClipped.size());
				// logger.debug(trainingInstants.size());
				int labelNearestInstant = nearestAnalogMeasurement.getCluster().getLabel();
				stateCount[labelNearestInstant - 1]++;
			}
			AnalogMeasurement a = analogMeasurements.get(i);
			a.setCluster(state(stateCount, trainingAnalogMeasurements));
			analogMeasurements.set(i, a);
			logger.debug(stateCount[0] + " " + stateCount[1] + " "
					+ stateCount[2] + " " + stateCount[3]);
		}
		logger.debug(analogMeasurements.size());
		return analogMeasurements;
	}

	
	public Cluster state(int a[], ArrayList<AnalogMeasurement> trainingAnalogMeasurements) {
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
				analogMeasurementObj  = trainingAnalogMeasurements.get(i);
			}
		}
		return analogMeasurementObj.getCluster();
	}
	
/*	public static ArrayList<Substation> getSubstations(Connection conn) {
		ArrayList<Substation> substations = new ArrayList<Substation>();
		String query = "Select * from substations";
		try {
			PreparedStatement stat = conn.prepareStatement(query);
			ResultSet rs = stat.executeQuery();
			while (rs.next()) {
				ArrayList<AnalogMeasurement> analogMeasurementsforEachSubst = new ArrayList<AnalogMeasurement>();
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
					AnalogMeasurement am = new AnalogMeasurement(time,nameMeas, time, value);
					analogMeasurementsforEachSubst.add(am);
				}
				Substation substObj = new Substation(rdfId, name,analogMeasurementsforEachSubst, regionId);
				substations.add(substObj);
			}

		} catch (SQLException e) { // TODO Auto-generated catch block
			e.printStackTrace();
		}

		return substations;
	}
*/
}
