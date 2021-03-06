package utils.dbconnector;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class ConnectDB {
	private Connection conn;
	private Session session;

	public ConnectDB() {
		conn = null;
		session = null;
	}

	public Connection startConnection() {
		// JDBC driver name and database URL
		final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
		// STEP 1:Database credentials
		String db_url, port, db_name, USER, PASS, DB_URL, ssh_user, ssh_pass;
		try {

			File file = new File(
					"src/main/java/utils/dbconnector/credentials.xml");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			NodeList nodeList = doc.getElementsByTagName("dbconnection");
			Node nNode = nodeList.item(0);
			Element dElement = (Element) nNode;
			db_url = dElement.getElementsByTagName("url").item(0)
					.getTextContent();
			port = dElement.getElementsByTagName("port").item(0)
					.getTextContent();
			db_name = dElement.getElementsByTagName("db").item(0)
					.getTextContent();
			USER = dElement.getElementsByTagName("username").item(0)
					.getTextContent();
			PASS = dElement.getElementsByTagName("password").item(0)
					.getTextContent();
			ssh_user = dElement.getElementsByTagName("sshuser").item(0)
					.getTextContent();
			ssh_pass = dElement.getElementsByTagName("sshpass").item(0)
					.getTextContent();
			String rhost = dElement.getElementsByTagName("rhost").item(0)
					.getTextContent();
			;
			int lport = 5657;
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");

			if (session == null) {
				JSch jsch = new JSch();
				session = jsch.getSession(ssh_user, rhost, 22);
				session.setPassword(ssh_pass);
				session.setConfig(config);
				session.connect();
				session.setPortForwardingL(lport, db_url,
						Integer.parseInt(port));
			}

			DB_URL = "jdbc:mysql://" + db_url + ":" + lport + "/" + db_name;
			// STEP 2: Register JDBC driver
			Class.forName(JDBC_DRIVER);
			// STEP 3: Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			System.out.println("Connection Established\n\n");

		} catch (Exception se) {
			// Handle errors for JDBC
			se.printStackTrace();

		}
		return conn;
	}

	public void closeConnection() {
		try {
			if (session != null) {
				session.disconnect();
			}
			if (conn != null)
				conn.close();

		} catch (SQLException se) {
			se.printStackTrace();
		}
		System.out.println("Connection Closed!");
	}
}
