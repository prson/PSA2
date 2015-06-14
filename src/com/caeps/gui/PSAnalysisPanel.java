/* A panel to be embedded in an applet or a frame to display the functionalities of the application.
 * It takes in the connection details for the database server, the learning and test data
 * of the power system.
 * 
 * Author: Pratik Sonthalia and Radhakrishnan Natarajan
 * Date: 14 June' 15.
 * 
 */
package com.caeps.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
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

/**
 * The Class PSAnalysisPanel.
 * A class extending a java panel and implementing buttons, field and labels using the swing framework.
 */
public class PSAnalysisPanel extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The logger. */
	private static Logger logger = Logger.getLogger(PSAnalysisPanel.class);

	/** The learning set connection url field. */
	JTextField learningSetConnectionUrlField;
	
	/** The learning set connection username field. */
	JTextField learningSetConnectionUsernameField;
	
	/** The learning set connection password field. */
	JPasswordField learningSetConnectionPasswordField;
	
	/** The learning set filename url field. */
	JTextField learningSetFilenameUrlField;
	
	/** The learning set conn. */
	public static Connection learningSetConn = null;
	
	/** The learning set connection established. */
	boolean learningSetConnectionEstablished = false;
	
	/** The learning set doc reader. */
	Reader learningSetDocReader = null;

	/** The test set connection url field. */
	JTextField testSetConnectionUrlField;
	
	/** The test set connection username field. */
	JTextField testSetConnectionUsernameField;
	
	/** The test set connection password field. */
	JPasswordField testSetConnectionPasswordField;
	
	/** The test set filename url field. */
	JTextField testSetFilenameUrlField;	
	
	/** The test set conn. to the database*/
	public static Connection testSetConn = null;
	
	/** The test set connection established. */
	boolean testSetConnectionEstablished = false;
	
	/** The test set doc reader. */
	Reader testSetDocReader = null;

	/** The statement used to execute query */
	Statement stmt = null;

	/** The results area. */
	JTextArea resultsArea;
	
	/** The console area. */
	public static JTextArea consoleArea;
	
	/** The clusters. */
	ArrayList<Cluster> clusters;
	
	/** The learning instants. */
	ArrayList<Instant> learningInstants;
	
	/** The test instants. */
	ArrayList<Instant> testInstants;
	
	/** The k. */
	private int k=7;
	

	/**
	 * Instantiates a new PS analysis panel.
	 */
	public PSAnalysisPanel() {
		
		
		//Creating the java panel for inputting the learning parameter details (starts)
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
		loadLearningSetFilePanel.add(learningSetFilenameUrlLabel);
		loadLearningSetFilePanel.add(learningSetFilenameUrlField);
		loadLearningSetFilePanel.add(loadLearningSetFileButton);
		establishLearningSetConnectionPanel.add(loadLearningSetFilePanel);
		establishLearningSetConnectionPanel
		.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory
						.createTitledBorder("Enter the learning set database connection parameters"),
				BorderFactory.createEmptyBorder(5, 15, 35, 15)));

		//Creating the java panel for inputting the learning parameter details (ends)
		
		//Creating the java panel for inputting the test parameter details (starts)
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
		JButton establishTestSetConnectionButton = new JButton("Establish Connection");
		EstablishTestSetConnectionMouseListener establishTestSetConnectionMouseListener = new EstablishTestSetConnectionMouseListener();
		establishTestSetConnectionButton.addMouseListener(establishTestSetConnectionMouseListener);

		JPanel establishTestSetConnectionPanel = new JPanel();
		establishTestSetConnectionPanel
				.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory
								.createTitledBorder("Enter the test set database connection parameters"),
						BorderFactory.createEmptyBorder(5, 15, 35, 15)));
		establishTestSetConnectionPanel.add(testSetConnectionUrlLabel);
		establishTestSetConnectionPanel.add(testSetConnectionUrlField);
		establishTestSetConnectionPanel.add(testSetConnectionUsernameLabel);
		establishTestSetConnectionPanel.add(testSetConnectionUsernameField);
		establishTestSetConnectionPanel.add(testSetConnectionPasswordLabel);
		establishTestSetConnectionPanel.add(testSetConnectionPasswordField);
		establishTestSetConnectionPanel.add(establishTestSetConnectionButton);

		JLabel testSetFilenameUrlLabel = new JLabel("Test set SQL file Location: ");

		testSetFilenameUrlField = new JTextField();
		testSetFilenameUrlField.setText("assignment2_testset.sql");
		testSetFilenameUrlField.setColumns(30);

		JButton loadTestSetFileButton = new JButton("Load File");
		LoadTestSetFileMouseListener loadTestSetFileMouseListener = new LoadTestSetFileMouseListener();
		loadTestSetFileButton.addMouseListener(loadTestSetFileMouseListener);

		JPanel loadTestSetFilePanel = new JPanel();
		loadTestSetFilePanel.add(testSetFilenameUrlLabel);
		loadTestSetFilePanel.add(testSetFilenameUrlField);
		loadTestSetFilePanel.add(loadTestSetFileButton);
		establishTestSetConnectionPanel.add(loadTestSetFilePanel);
		
		//Creating the java panel for inputting the test parameter details (ends)
		
		//Creating the perform Ops panel containing the execute buttons
		
		// The execute clustering button
		JButton clusterLearningSet = new JButton("Cluster Learning Set");
		ClusterLearningSetMouseListener clusterLearningSetMouseListener = new ClusterLearningSetMouseListener();
		clusterLearningSet.addMouseListener(clusterLearningSetMouseListener);

		JButton classifyTestSet = new JButton("Classify Test Set");
		ClassifyTestSetMouseListener classifyTestSetMouseListener = new ClassifyTestSetMouseListener();
		classifyTestSet.addMouseListener(classifyTestSetMouseListener);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Perform Ops"),
				BorderFactory.createEmptyBorder(0, 5, 5, 5)));
		buttonPanel.add(clusterLearningSet);
		buttonPanel.add(classifyTestSet);


		//Creating the result area panel and text area, making it scrollable
		resultsArea = new JTextArea();
		resultsArea.setLineWrap(true);
		resultsArea.setEditable(false);
		
		DefaultCaret caretResultsArea = (DefaultCaret) resultsArea.getCaret();
		caretResultsArea.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		JScrollPane scrollResults = new JScrollPane(resultsArea);
		scrollResults.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollResults.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollResults.setPreferredSize(new Dimension(1000, 200));

		JPanel resultsPanel = new JPanel();
		resultsPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Results"),
				BorderFactory.createEmptyBorder(5, 5, 15, 5)));
		resultsPanel.add(scrollResults);

		//Creating the console area panel and text area, making it scrollable
		consoleArea = new JTextArea();
		consoleArea.setForeground(Color.WHITE);
		consoleArea.setBackground(Color.BLACK);
		consoleArea.setEditable(false);

		DefaultCaret caret = (DefaultCaret) consoleArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		JScrollPane scroll = new JScrollPane(consoleArea);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll.setPreferredSize(new Dimension(1000, 100));

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

		//Adding all the components to the main panel
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(establishLearningSetConnectionPanel);
//		add(loadLearningSetFilePanel);
		add(establishTestSetConnectionPanel);
//		add(loadTestSetFilePanel);
		add(buttonPanel);
		add(resultsPanel);
		add(consolePanel);
		add(exitButton);

	}

	/**
	 * The listener interface for receiving establishLearningSetConnectionMouse events.
	 * The class that is interested in processing a establishLearningSetConnectionMouse
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addEstablishLearningSetConnectionMouseListener<code> method. When
	 * the establishLearningSetConnectionMouse event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see EstablishLearningSetConnectionMouseEvent
	 */
	class EstablishLearningSetConnectionMouseListener implements MouseListener {

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
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

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
		 */
		public void mouseEntered(MouseEvent e) {
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
		 */
		public void mouseExited(MouseEvent e) {
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent e) {
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		public void mousePressed(MouseEvent e) {
		}
	}

	/**
	 * The listener interface for receiving loadLearningSetFileMouse events.
	 * The class that is interested in processing a loadLearningSetFileMouse
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addLoadLearningSetFileMouseListener<code> method. When
	 * the loadLearningSetFileMouse event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see LoadLearningSetFileMouseEvent
	 */
	class LoadLearningSetFileMouseListener implements MouseListener {

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseClicked(MouseEvent e) {
			logger.debug("Inside load learning set SQL file listener");

			try {
				learningSetDocReader = new BufferedReader(
				        new FileReader(learningSetFilenameUrlField.getText()));
			} catch (FileNotFoundException e2) {
				logger.debug("File not found exception while importing the learning data set sql file\n"+e);			
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
						stat = learningSetConn.prepareStatement(query);
						stat.executeUpdate();
			 			sr.runScript(learningSetDocReader);
			 			consoleArea.append("\nCongrats! Learning set SQL file loaded in the database");
			 
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

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
		 */
		public void mouseEntered(MouseEvent e) {
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
		 */
		public void mouseExited(MouseEvent e) {
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent e) {
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		public void mousePressed(MouseEvent e) {
		}
	}

	/**
	 * The listener interface for receiving clusterLearningSetMouse events.
	 * The class that is interested in processing a clusterLearningSetMouse
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addClusterLearningSetMouseListener<code> method. When
	 * the clusterLearningSetMouse event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see ClusterLearningSetMouseEvent
	 */
	class ClusterLearningSetMouseListener implements MouseListener {

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
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
				consoleArea.updateUI();

				RunMeansAlgorithm runMeanObj=new RunMeansAlgorithm();
				runMeanObj.formClusters(learningSetConn);
				clusters=runMeanObj.getClusters();
				learningInstants=runMeanObj.getInstants();
				String result="Clusters:\nCluster Type\t\t";
				for(int j=0;j<clusters.get(0).getAnalogMeasurements().size();j++){
					result+="Sub"+(j+1)+"\t";
				}
				result=result+"\n";
				for(int i=0;i<clusters.size();i++){
					result+="\n"+clusters.get(i).getDesc()+"\nVoltage\t\t";
					for(int j=0;j<clusters.get(i).getAnalogMeasurements().size();j++){
						result+=String.format("%2.4f",clusters.get(i).getAnalogMeasurements().get(j).getVoltageValue())+"\t";
					}
					result+="\nAngle\t\t";
					for(int j=0;j<clusters.get(i).getAnalogMeasurements().size();j++){
						result+=String.format("%2.4f",clusters.get(i).getAnalogMeasurements().get(j).getAngleValue())+"\t";
					}
				}
				
				consoleArea.append("\nLearning set clustered!");
				resultsArea.setText(result);
			}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
		 */
		public void mouseEntered(MouseEvent e) {
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
		 */
		public void mouseExited(MouseEvent e) {
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent e) {
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		public void mousePressed(MouseEvent e) {
		}
	}

	/**
	 * The listener interface for receiving establishTestSetConnectionMouse events.
	 * The class that is interested in processing a establishTestSetConnectionMouse
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addEstablishTestSetConnectionMouseListener<code> method. When
	 * the establishTestSetConnectionMouse event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see EstablishTestSetConnectionMouseEvent
	 */
	class EstablishTestSetConnectionMouseListener implements MouseListener {

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
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
		
		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
		 */
		public void mouseEntered(MouseEvent e) {
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
		 */
		public void mouseExited(MouseEvent e) {
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent e) {
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		public void mousePressed(MouseEvent e) {
		}
	}

	/**
	 * The listener interface for receiving loadTestSetFileMouse events.
	 * The class that is interested in processing a loadTestSetFileMouse
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addLoadTestSetFileMouseListener<code> method. When
	 * the loadTestSetFileMouse event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see LoadTestSetFileMouseEvent
	 */
	class LoadTestSetFileMouseListener implements MouseListener {

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseClicked(MouseEvent e) {
			logger.debug("Inside load file listener");
			try {
				testSetDocReader = new BufferedReader(
				        new FileReader(testSetFilenameUrlField.getText()));
			} catch (FileNotFoundException e2) {
				logger.debug("File not found excpetion while trying to load the test data set SQL File\n"+e);			}

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
						stat = testSetConn.prepareStatement(query);
						stat.executeUpdate();
			 			sr.runScript(testSetDocReader);
			 			consoleArea.append("\nCongrats! Test set SQL file loaded in the database");
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

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
		 */
		public void mouseEntered(MouseEvent e) {
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
		 */
		public void mouseExited(MouseEvent e) {
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent e) {
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		public void mousePressed(MouseEvent e) {
		}
	}

	/**
	 * The listener interface for receiving classifyTestSetMouse events.
	 * The class that is interested in processing a classifyTestSetMouse
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addClassifyTestSetMouseListener<code> method. When
	 * the classifyTestSetMouse event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see ClassifyTestSetMouseEvent
	 */
	class ClassifyTestSetMouseListener implements MouseListener {

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
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
				runKNNObj.runKNNAlgorithm(testSetConn, learningInstants, k);
				testInstants=runKNNObj.getTestInstants();
				runKNNObj.displayInstants(testInstants);
				String result="Test Classification Results:\n\nTime\tOperational State\n";
				for (int j = 0; j < testInstants.size(); j++) {
					result+=testInstants.get(j).getInstant() + "\t"
							+ testInstants.get(j).getCluster().getDesc()+"\n";
				}
				
				consoleArea.append("\nTest set classified!");
				resultsArea.setText(result);
				consoleArea.append("\nTest set clustering done!");
				
			}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
		 */
		public void mouseEntered(MouseEvent e) {
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
		 */
		public void mouseExited(MouseEvent e) {
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent e) {
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		public void mousePressed(MouseEvent e) {
		}
	}

	/**
	 * The listener interface for receiving exitMouse events.
	 * The class that is interested in processing a exitMouse
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addExitMouseListener<code> method. When
	 * the exitMouse event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see ExitMouseEvent
	 */
	class ExitMouseListener implements MouseListener {

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
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

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
		 */
		public void mouseEntered(MouseEvent e) {
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
		 */
		public void mouseExited(MouseEvent e) {
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent e) {
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		public void mousePressed(MouseEvent e) {
		}
	}
}