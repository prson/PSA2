package com.caeps.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.DefaultCaret;

import java.sql.*;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.log4j.Logger;

import com.caeps.loadDatabase.Cluster;
import com.caeps.loadDatabase.Instant;
import com.caeps.run.ConnectToDB;
import com.caeps.run.RunKNNAlgorithm;
import com.caeps.run.RunMeansAlgorithm;

public class PSAnalysisPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(PSAnalysisPanel.class);

	JTextField learningSetConnectionUrlField;
	JTextField learningSetConnectionUsernameField;
	JPasswordField learningSetConnectionPasswordField;
	JTextField learningSetFilenameUrlField;
	public static Connection learningSetConn = null;
	boolean learningSetConnectionEstablished = false;
	Reader learningSetDocReader = null;

	JTextField testSetConnectionUrlField;
	JTextField testSetConnectionUsernameField;
	JPasswordField testSetConnectionPasswordField;
	JTextField testSetFilenameUrlField;	
	public static Connection testSetConn = null;
	boolean testSetConnectionEstablished = false;
	Reader testSetDocReader = null;

	Statement stmt = null;

	JTextArea resultsArea;
	public static JTextArea consoleArea;
	
	ArrayList<Cluster> clusters;
	ArrayList<Instant> learningInstants;
	ArrayList<Instant> testInstants;
	
	private int k=7;
	

	public PSAnalysisPanel() {
		
		JLabel learningSetConnectionUrlLabel = new JLabel("Learning set connection URL: ");

		learningSetConnectionUrlField = new JTextField();
		learningSetConnectionUrlField.setText("jdbc:mysql://localhost:3306/");
		learningSetConnectionUrlField.setColumns(15);

		JLabel learningSetConnectionUsernameLabel = new JLabel("Username: ");

		learningSetConnectionUsernameField = new JTextField();
		learningSetConnectionUsernameField.setText("root");
		learningSetConnectionUsernameField.setColumns(5);

		JLabel learningSetConnectionPasswordLabel = new JLabel("Password: ");

		learningSetConnectionPasswordField = new JPasswordField();
		learningSetConnectionPasswordField.setText("root");
		learningSetConnectionPasswordField.setColumns(5);

		JButton establishLearningSetConnectionButton = new JButton("Establish Connection");
		EstablishLearningSetConnectionMouseListener establishLearningSetConnectionMouseListener = new EstablishLearningSetConnectionMouseListener();
		establishLearningSetConnectionButton.addMouseListener(establishLearningSetConnectionMouseListener);

		JPanel establishLearningSetConnectionPanel = new JPanel();
		establishLearningSetConnectionPanel
				.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory
								.createTitledBorder("Enter the learning set database connection parameters"),
						BorderFactory.createEmptyBorder(15, 15, 15, 15)));
		establishLearningSetConnectionPanel.add(learningSetConnectionUrlLabel);
		establishLearningSetConnectionPanel.add(learningSetConnectionUrlField);
		establishLearningSetConnectionPanel.add(learningSetConnectionUsernameLabel);
		establishLearningSetConnectionPanel.add(learningSetConnectionUsernameField);
		establishLearningSetConnectionPanel.add(learningSetConnectionPasswordLabel);
		establishLearningSetConnectionPanel.add(learningSetConnectionPasswordField);
		establishLearningSetConnectionPanel.add(establishLearningSetConnectionButton);

		JLabel learningSetFilenameUrlLabel = new JLabel("Learning set SQL file Location: ");

		learningSetFilenameUrlField = new JTextField();
		learningSetFilenameUrlField.setText("assignment2correct_data.sql");
		learningSetFilenameUrlField.setColumns(30);

		JButton loadLearningSetFileButton = new JButton("Load File");
		LoadLearningSetFileMouseListener loadLearningSetFileMouseListener = new LoadLearningSetFileMouseListener();
		loadLearningSetFileButton.addMouseListener(loadLearningSetFileMouseListener);

		JPanel loadLearningSetFilePanel = new JPanel();
		loadLearningSetFilePanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Learning Set SQL file details"),
				BorderFactory.createEmptyBorder(15, 15, 15, 15)));
		loadLearningSetFilePanel.add(learningSetFilenameUrlLabel);
		loadLearningSetFilePanel.add(learningSetFilenameUrlField);
		loadLearningSetFilePanel.add(loadLearningSetFileButton);


		JLabel testSetConnectionUrlLabel = new JLabel("Test set connection URL: ");

		testSetConnectionUrlField = new JTextField();
		testSetConnectionUrlField.setText("jdbc:mysql://localhost:3306/");
		testSetConnectionUrlField.setColumns(15);

		JLabel testSetConnectionUsernameLabel = new JLabel("Username: ");

		testSetConnectionUsernameField = new JTextField();
		testSetConnectionUsernameField.setText("root");
		testSetConnectionUsernameField.setColumns(5);

		JLabel testSetConnectionPasswordLabel = new JLabel("Password: ");

		testSetConnectionPasswordField = new JPasswordField();
		testSetConnectionPasswordField.setText("root");
		testSetConnectionPasswordField.setColumns(5);

		//Establish Test Connection Button
		JButton establishTestSetConnectionButton = new JButton("Establish Test Connection");
		EstablishTestSetConnectionMouseListener establishTestSetConnectionMouseListener = new EstablishTestSetConnectionMouseListener();
		establishTestSetConnectionButton.addMouseListener(establishTestSetConnectionMouseListener);

		JPanel establishTestSetConnectionPanel = new JPanel();
		establishTestSetConnectionPanel
				.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory
								.createTitledBorder("Enter the test set database connection parameters"),
						BorderFactory.createEmptyBorder(15, 15, 15, 15)));
		establishTestSetConnectionPanel.add(testSetConnectionUrlLabel);
		establishTestSetConnectionPanel.add(testSetConnectionUrlField);
		establishTestSetConnectionPanel.add(testSetConnectionUsernameLabel);
		establishTestSetConnectionPanel.add(testSetConnectionUsernameField);
		establishTestSetConnectionPanel.add(testSetConnectionPasswordLabel);
		establishTestSetConnectionPanel.add(testSetConnectionPasswordField);
		establishTestSetConnectionPanel.add(establishTestSetConnectionButton);

		JLabel testSetFilenameUrlLabel = new JLabel("Test set file Location: ");

		testSetFilenameUrlField = new JTextField();
		testSetFilenameUrlField.setText("assignment2_testset.sql");
		testSetFilenameUrlField.setColumns(30);

		JButton loadTestSetFileButton = new JButton("Load File");
		LoadTestSetFileMouseListener loadTestSetFileMouseListener = new LoadTestSetFileMouseListener();
		loadTestSetFileButton.addMouseListener(loadTestSetFileMouseListener);

		JPanel loadTestSetFilePanel = new JPanel();
		loadLearningSetFilePanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Enter the SQL file details"),
				BorderFactory.createEmptyBorder(15, 15, 15, 15)));
		loadTestSetFilePanel.add(testSetFilenameUrlLabel);
		loadTestSetFilePanel.add(testSetFilenameUrlField);
		loadTestSetFilePanel.add(loadTestSetFileButton);		
		
		// The execute clustering button
		JButton clusterLearningSet = new JButton("Cluster Learning Set");
		ClusterLearningSetMouseListener clusterLearningSetMouseListener = new ClusterLearningSetMouseListener();
		clusterLearningSet.addMouseListener(clusterLearningSetMouseListener);

		JButton classifyTestSet = new JButton("Classify Test Set");
		ClassifyTestSetMouseListener classifyTestSetMouseListener = new ClassifyTestSetMouseListener();
		classifyTestSet.addMouseListener(classifyTestSetMouseListener);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonPanel.setPreferredSize(new Dimension(200, 200));
		buttonPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Perform Ops"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		buttonPanel.add(clusterLearningSet);
		buttonPanel.add(classifyTestSet);

		
		resultsArea = new JTextArea();
		resultsArea.setPreferredSize(new Dimension(500, 200));
		resultsArea.setLineWrap(true);
		resultsArea.setEditable(false);

		JPanel resultsPanel = new JPanel();
		resultsPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Results"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		resultsPanel.add(resultsArea);

		JPanel performOpsPanel = new JPanel();
		performOpsPanel.add(buttonPanel);
		performOpsPanel.add(resultsPanel);

		
		consoleArea = new JTextArea();
		consoleArea.setForeground(Color.WHITE);
		consoleArea.setBackground(Color.BLACK);
		consoleArea.setEditable(false);

		DefaultCaret caret = (DefaultCaret) consoleArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		JScrollPane scroll = new JScrollPane(consoleArea);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll.setPreferredSize(new Dimension(800, 100));

		JPanel consolePanel = new JPanel();
		consolePanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Console"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		consolePanel.add(scroll);
		consoleArea.append("Welcome!");

		JButton exitButton = new JButton("Exit");
		ExitMouseListener exitMouseListener = new ExitMouseListener();
		exitButton.addMouseListener(exitMouseListener);

		exitButton.setPreferredSize(new Dimension(120, 30));

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(establishLearningSetConnectionPanel);
		add(loadLearningSetFilePanel);
		add(establishTestSetConnectionPanel);
		add(loadTestSetFilePanel);
		add(performOpsPanel);
		add(consolePanel);
		add(exitButton);

	}

	class EstablishLearningSetConnectionMouseListener implements MouseListener {

		public void mouseClicked(MouseEvent e) {
			if (learningSetConn != null) {
				try {
					learningSetConn.close();
				} catch (SQLException e1) {
					logger.error("Error in closing previous connection.", e1);
					consoleArea
							.append("\nError in closing previous connection. Check logs for details.");
				}
			}
			learningSetConn = null;
			logger.debug("Inside establish learning set connection listener");
			learningSetConn = (new ConnectToDB()).establishConnection(
					learningSetConnectionUrlField.getText(),
					learningSetConnectionUsernameField.getText(),
					String.valueOf(learningSetConnectionPasswordField.getPassword()));
			if (learningSetConn != null) {
				consoleArea.append("\nCongrats! Learning set connection established.");
			} else {
				consoleArea
						.append("\nSorry! Connection not established, check the logs and try again.");
			}
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
		}
	}

	class LoadLearningSetFileMouseListener implements MouseListener {

		public void mouseClicked(MouseEvent e) {
			logger.debug("Inside load learning set SQL file listener");

			try {
				learningSetDocReader = new BufferedReader(
				        new FileReader(learningSetFilenameUrlField.getText()));
			} catch (FileNotFoundException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

			if (learningSetDocReader != null) {
				if (learningSetConn != null) {
					consoleArea
							.append("\nCongrats! Learning set SQL file exists. Now executing and loading the database.....");
					try {
						ScriptRunner sr = new ScriptRunner(learningSetConn);
						String query = "Create database if not exists PS2Learning";
						PreparedStatement stat = learningSetConn.prepareStatement(query);
						stat.executeUpdate();
						query = "use PS2Learning";
						stat.executeUpdate();
			 			sr.runScript(learningSetDocReader);
			 
					} catch (Exception e1) {
						consoleArea
						.append("\nSorry! Failed to execute the learning set SQL script. Check the logs for details");
						logger.error("Failed to execute the learning set SQL script.", e1);
					}
				} else {
					consoleArea
							.append("\nOoopsss! Learning set SQL file exists. But connection is not established. Please establish a connection and try again. Visit the logs for details");
					logger.error("Ooopsss! Learning set SQL file exists. But connection is not established. Please establish a connection and try again. Visit the logs for details");
					learningSetDocReader = null;
				}
			} else {
				consoleArea
						.append("\nSorry! Learning set SQL file could not be read, check the logs and try again.");
				logger.error("Sorry! Learning set SQL file could not be read, check the logs and try again.");
			}
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
		}
	}

	class ClusterLearningSetMouseListener implements MouseListener {

		public void mouseClicked(MouseEvent e) {
			logger.debug("Inside cluster learning set listener");
			if (learningSetConn == null) {
				consoleArea
						.append("\nThe connection is not established. Please establish connection and load the SQL file before clustering learning set!");
				logger.error("The connection is not established. Please establish connection and load the SQL file before clustering learning set!");
			} else if (learningSetDocReader == null) {
				consoleArea
						.append("\nThe learning set SQL file has not been read yet. Please load the SQL file before clustering learning set!");
				logger.error("The learning set SQL file has not been read yet. Please load the SQL file before clustering learning set!");
			} else {
				consoleArea.append("\nClustering learning set...");

				RunMeansAlgorithm runMeanObj=new RunMeansAlgorithm();
				runMeanObj.formClusters(learningSetConn);
				clusters=runMeanObj.getClusters();
				learningInstants=runMeanObj.getInstants();
				String result="Clusters:\nCluster Type                               ";
				for(int j=0;j<clusters.get(0).getAnalogMeasurements().size();j++){
					result+="Sub "+(j+1);
				}
				result=result+"\n";
				for(int i=0;i<clusters.size();i++){
					result+=clusters.get(i).getDesc();
					for(int j=0;j<clusters.get(i).getAnalogMeasurements().size();j++){
						result+=clusters.get(i).getAnalogMeasurements().get(j).getVoltageValue();
					}
					result+="                         ";
					for(int j=0;j<clusters.get(i).getAnalogMeasurements().size();j++){
						result+=clusters.get(i).getAnalogMeasurements().get(j).getAngleValue();
					}
				}
				
				consoleArea.append("\nLearning set clustered!");
				resultsArea.setText(result);
			}
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
		}
	}

	class EstablishTestSetConnectionMouseListener implements MouseListener {

		public void mouseClicked(MouseEvent e) {
			if (testSetConn != null) {
				try {
					testSetConn.close();
				} catch (SQLException e1) {
					logger.error("Error in closing previous connection.", e1);
					consoleArea
							.append("\nError in closing previous connection. Check logs for details.");
				}
			}
			testSetConn = null;
			logger.debug("Inside establish test set connection listener");
			testSetConn = (new ConnectToDB()).establishConnection(
					testSetConnectionUrlField.getText(),
					testSetConnectionUsernameField.getText(),
					String.valueOf(testSetConnectionPasswordField.getPassword()));
			if (testSetConn != null) {
				consoleArea.append("\nCongrats! Test set connection established.");
			} else {
				consoleArea
						.append("\nSorry! Connection not established, check the logs and try again.");
			}
		}
		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
		}
	}

	class LoadTestSetFileMouseListener implements MouseListener {

		public void mouseClicked(MouseEvent e) {
			logger.debug("Inside load file listener");
			try {
				testSetDocReader = new BufferedReader(
				        new FileReader(testSetFilenameUrlField.getText()));
			} catch (FileNotFoundException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

			if (testSetDocReader != null) {
				if (testSetConn != null) {
					consoleArea
							.append("\nCongrats! Test set SQL file exists. Now executing and loading the database.....");
					try {
						ScriptRunner sr = new ScriptRunner(testSetConn);
						String query = "Create database if not exists PS2Test";
						PreparedStatement stat = testSetConn.prepareStatement(query);
						stat.executeUpdate();
						query = "use PS2Test";
						stat.executeUpdate();
			 			sr.runScript(testSetDocReader);
					} catch (Exception e1) {
						consoleArea
						.append("\nSorry! Failed to execute the test set SQL script. Check the logs for details");
						logger.error("Failed to execute the test set SQL script.", e1);
					}
				} else {
					consoleArea
							.append("\nOoopsss! Test set SQL file exists. But connection is not established. Please establish a connection and try again. Visit the logs for details");
					logger.error("Ooopsss! Test set SQL file exists. But connection is not established. Please establish a connection and try again. Visit the logs for details");
					testSetDocReader = null;
				}
			} else {
				consoleArea
						.append("\nSorry! Test set SQL file could not be read, check the logs and try again.");
				logger.error("Sorry! Test set SQL file could not be read, check the logs and try again.");
			}
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
		}
	}

	class ClassifyTestSetMouseListener implements MouseListener {

		public void mouseClicked(MouseEvent e) {
			logger.debug("Inside classify test set mouse listener");
			if (testSetConn == null) {
				consoleArea
						.append("\nThe connection is not established. Please establish connection and load the file before classifying test set!");
				logger.error("The connection is not established. Please establish connection and load the file before classifying test set!");
			} else if (testSetDocReader == null) {
				consoleArea
						.append("\nThe test set SQL file has not been read yet. Please load the file before classifying test set!");
				logger.error("The test set SQL file has not been read yet. Please load the file before classifying test set!");
			} else {
				consoleArea.append("\nClassifying test set...");
				
				RunKNNAlgorithm runKNNObj=new RunKNNAlgorithm();
				runKNNObj.runKNNAlgorithm(learningSetConn, learningInstants, k);
				testInstants=runKNNObj.getTestInstants();
				runKNNObj.displayInstants(testInstants);
				String result="Instant after classficiation:\n";
				for (int j = 0; j < testInstants.size(); j++) {
					result+=testInstants.get(j).getInstant() + ":"
							+ testInstants.get(j).getAvgAngle() + ":"
							+ testInstants.get(j).getAvgVoltage() + ":"
							+ testInstants.get(j).getCluster().getDesc()+"\n";
				}
				
				consoleArea.append("\nTest set classified!");
				resultsArea.setText(result);
			}
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
		}
	}

	class ExitMouseListener implements MouseListener {

		public void mouseClicked(MouseEvent e) {
			logger.debug("Closing connections and exiting!");
			if (learningSetConn != null) {
				try {
					logger.debug("Closing the learning set connection and exiting!");
					learningSetConn.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
			if (testSetConn != null) {
				try {
					logger.debug("Closing the test set connection and exiting!");
					testSetConn.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
			System.exit(0);
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
		}
	}
}
