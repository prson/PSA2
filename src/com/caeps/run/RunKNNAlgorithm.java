package com.caeps.run;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.caeps.loadDatabase.AnalogMeasurement;
import com.caeps.loadDatabase.Cluster;
import com.caeps.loadDatabase.Instant;

public class RunKNNAlgorithm {

	private static Logger logger = Logger.getLogger(RunKNNAlgorithm.class);
	ArrayList<Instant> testInstants = new ArrayList<Instant>();

	ArrayList<Instant> runKNNAlgorithm(Connection connTest, ArrayList<Instant> trainingInstants, int k) {

	ConnectToDB connectDBObj = new ConnectToDB();
	connTest = connectDBObj.establishConnection("jdbc:mysql://localhost:3306/powersystemassignment2testdata","root", "root");
		try {

			String query = "SELECT MAX(time) FROM analog_meas";
			PreparedStatement stat = connTest.prepareStatement(query);
			ResultSet rs = stat.executeQuery();
			rs.next();
			int numOfTestDataInstants=rs.getInt("MAX(time)");

			// Creating the instants from the database
			for (int i = 1; i <= numOfTestDataInstants; i++) {
				query = "Select * from substations";
				ArrayList<AnalogMeasurement> analogMeasurements = new ArrayList<AnalogMeasurement>();
				stat = connTest.prepareStatement(query);
				rs = stat.executeQuery();
				while (rs.next()) {
					String rdfId = rs.getString("rdfid");
					query = "Select * from analog_meas where sub_rdfid='"
							+ rdfId + "' and time=" + i
							+ " and name like '%VOLT%'";
					// logger.debug(query);
					stat = connTest.prepareStatement(query);
					ResultSet rs1 = stat.executeQuery();
					rs1.next();
					double voltageValue = rs1.getDouble("value");
					query = "Select * from analog_meas where sub_rdfid='"
							+ rdfId + "' and time=" + i
							+ " and name like '%ANG%'";
					// logger.debug(query);
					stat = connTest.prepareStatement(query);
					ResultSet rs2 = stat.executeQuery();
					rs2.next();
					double angleValue = rs2.getDouble("value");
					AnalogMeasurement am = new AnalogMeasurement(i, angleValue,
							voltageValue, rdfId);
					analogMeasurements.add(am);
				}
				Instant instantObj = new Instant(i, analogMeasurements);
				testInstants.add(instantObj);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (connTest != null) {
				try {
					connTest.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		testInstants = identifyClusterForInstants(testInstants,
				trainingInstants, k);
		return testInstants;
	}

	ArrayList<Instant> identifyClusterForInstants(
			ArrayList<Instant> testInstants,
			ArrayList<Instant> trainingInstants, int k) {
		for (int i = 0; i < testInstants.size(); i++) {
			ArrayList<Instant> kNearestInstants = new ArrayList<Instant>();
			ArrayList<Instant> trainingInstantsClipped = new ArrayList<Instant>(
					trainingInstants);
			int[] stateCount = new int[4];
			double distMin = Double.POSITIVE_INFINITY;
			Instant nearestInstant = null;
//			logger.debug("Test Instant: " + (i + 1));
			for (int m = 1; m <= k; m++) {
				for (int j = 0; j < trainingInstantsClipped.size(); j++) {
					double dist = 0;
					for (int l = 0; l < testInstants.get(i)
							.getAnalogMeasurements().size(); l++) {
						dist += Math.pow(testInstants.get(i)
								.getAnalogMeasurements().get(l).getAngleValue()
								- trainingInstantsClipped.get(j)
										.getAnalogMeasurements().get(l)
										.getAngleValue(), 2)
								+ Math.pow(testInstants.get(i)
										.getAnalogMeasurements().get(l)
										.getVoltageValue()
										- trainingInstantsClipped.get(j)
												.getAnalogMeasurements().get(l)
												.getVoltageValue(), 2);
					}
					dist = Math.pow(dist, 0.5);
					if (dist < distMin) {
						nearestInstant = trainingInstantsClipped.get(j);
						distMin = dist;
					}
				}
				distMin = Double.POSITIVE_INFINITY;
				kNearestInstants.add(nearestInstant);
//				logger.debug("Least Dist sample " + m + ": "
//						+ nearestInstant.getInstant());
				trainingInstantsClipped.remove(nearestInstant);
				// logger.debug(trainingInstantsClipped.size());
				// logger.debug(trainingInstants.size());
				int labelNearestInstant = nearestInstant.getCluster()
						.getLabel();
				stateCount[labelNearestInstant - 1]++;
			}
			Instant a = testInstants.get(i);
			a.setCluster(getCluster(stateCount, trainingInstants));
			testInstants.set(i, a);
//			logger.debug(stateCount[0] + " " + stateCount[1] + " "
//					+ stateCount[2] + " " + stateCount[3]);
		}
//		logger.debug(testInstants.size());
		return testInstants;
	}

	public Cluster getCluster(int a[], ArrayList<Instant> trainingInstants) {
		Instant instantObj = null;
		double max = Double.NEGATIVE_INFINITY;
		int maxIndex = -1;
		for (int k = 0; k < a.length; k++) {
			if (max < a[k]) {
				maxIndex = k;
				max = a[k];
			}
		}
		maxIndex++;
		for (int i = 0; i < trainingInstants.size(); i++) {
			if (trainingInstants.get(i).getCluster().getLabel() == maxIndex) {
				instantObj = trainingInstants.get(i);
			}
		}
		return instantObj.getCluster();
	}
}
