package com.caeps.run;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;

/**
 * The Class ConnectToDB.
 */
public class ConnectToDB {
	
	/** The logger. */
	Logger logger = Logger.getLogger(ConnectToDB.class);

	/**
	 * Instantiates a new connect to db.
	 */
	public ConnectToDB() {
	}

	// establishing a connection to the database
	/**
	 * Establish connection.
	 *
	 * @param url the url
	 * @param username the username
	 * @param password the password
	 * @return the connection
	 */
	public Connection establishConnection(String url, String username, String password) {

		try {
			logger.debug("Loading driver...");
			Class.forName("com.mysql.jdbc.Driver");
			logger.debug("Driver loaded!");
		} catch (ClassNotFoundException e) {
			logger.error("Cannot find the driver in the classpath!", e);
		}

		Connection connection = null;
		try {
			logger.debug("Connecting database...");
			connection = (Connection) DriverManager.getConnection(url,
					username, password);
			logger.debug("Database connected!");
			return connection;
		} catch (SQLException e) {
			logger.error("Cannot connect the database!", e);
			connection=null;
			return connection;
		} finally {
		}
	}
}
