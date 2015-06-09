package com.caeps.run;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.caeps.loadDatabase.Cluster;
import com.caeps.loadDatabase.ConnectToDB;
import com.caeps.loadDatabase.Instant;

public class RunKNNAlgorithm {

	private static Logger logger = Logger.getLogger(RunMeansAlgorithm.class);
	ArrayList<Instant> instants = new ArrayList<Instant>();

	ArrayList<Instant> runKNNAlgorithm(ArrayList<Instant> trainingInstants) {

		ConnectToDB connectDBObj = new ConnectToDB();
		Connection conn = connectDBObj.establishConnection(
				"jdbc:mysql://localhost:3306/powersystemassignment2testdata",
				"root", "root");
		try {

			for (int i = 1; i <= 40; i++) {
				String query = "Select * from analog_meas where name like '%VOLT%' and time="
						+ i;
				PreparedStatement stat = conn.prepareStatement(query);
				ResultSet rsInstant;
				rsInstant = stat.executeQuery();
				double voltSumInstant = 0;
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
				double angleSumInstant = 0;
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
		}
		instants = calculateStateForInstants(instants, trainingInstants, 7);
		return instants;
	}

	ArrayList<Instant> calculateStateForInstants(ArrayList<Instant> instants,
			ArrayList<Instant> trainingInstants, int k) {
		for (int i = 0; i < instants.size(); i++) {
			ArrayList<Instant> kNearestInstants = new ArrayList<Instant>();
			ArrayList<Instant> trainingInstantsClipped = new ArrayList<Instant>(
					trainingInstants);
			int[] stateCount = new int[4];
			double distMin = Double.POSITIVE_INFINITY;
			Instant nearestInstant = null;
			logger.debug("Test Instant: " + (i + 1));
			for (int m = 1; m <= k; m++) {
				for (int j = 0; j < trainingInstantsClipped.size(); j++) {
					double dist = Math.pow(
							Math.pow(instants.get(i).getAvgVoltage()
									- trainingInstantsClipped.get(j)
											.getAvgVoltage(), 2)
									+ Math.pow(instants.get(i).getAvgAngle()
											- trainingInstantsClipped.get(j)
													.getAvgAngle(), 2), 0.5);
					if (dist < distMin) {
						nearestInstant = trainingInstantsClipped.get(j);
						distMin = dist;
					}
				}
				distMin = Double.POSITIVE_INFINITY;
				kNearestInstants.add(nearestInstant);
				logger.debug("Least Dist sample " + m + ": "
						+ nearestInstant.getInstant());
				trainingInstantsClipped.remove(nearestInstant);
				// logger.debug(trainingInstantsClipped.size());
				// logger.debug(trainingInstants.size());
				int labelNearestInstant = nearestInstant.getCluster()
						.getLabel();
				stateCount[labelNearestInstant - 1]++;
			}
			Instant a = instants.get(i);
			a.setCluster(state(stateCount, trainingInstants));
			instants.set(i, a);
			logger.debug(stateCount[0] + " " + stateCount[1] + " "
					+ stateCount[2] + " " + stateCount[3]);
		}
		logger.debug(instants.size());
		return instants;
	}

	public Cluster state(int a[], ArrayList<Instant> trainingInstants) {
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
