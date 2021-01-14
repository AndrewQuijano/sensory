package columbia.irt.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import android.net.wifi.ScanResult;
import columbia.irt.network.structs.FloorData;


public class dataCollection implements Runnable
{
	// SQL Login
	protected static String username = "";
	protected static String password = "";
	protected final static String DB = "IRT";

	// I/O
	private ObjectInputStream fromClient = null;
	private ObjectOutputStream toClient = null;

	// SQL
	protected final static String myDriver = "org.gjt.mm.mysql.Driver";
	protected final static String URL = "jdbc:mysql://localhost:3306/?useSSL=false";

	protected final static String TRAININGDATA = "dataset";
	protected final static String APTRAIN = "Wifi";

	protected final static String WifiLUT = "WifiLUT";

	protected Socket incomingClient = null;

	public dataCollection(Socket clntSock)
	{
		this.incomingClient = clntSock;
	}

	public void run()
	{
		try 
		{
			fromClient = new ObjectInputStream(incomingClient.getInputStream());
			toClient = new ObjectOutputStream(incomingClient.getOutputStream());
			// Read Object
			Object x = fromClient.readObject();

			if (x instanceof FloorData)
			{
				boolean result = submitTrainingData((FloorData) x);
				toClient.writeBoolean(result);
			}

			//Close I/O streams and Socket
			this.closeClientConnection();
		}
		catch(StreamCorruptedException ef)
		{
			ef.getMessage();
		}
		catch(EOFException ef)
		{
			ef.printStackTrace();
		}
		catch(IOException ie)
		{
			ie.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * This method will read the sendData struct, which contains all the features
	 * collected in the original incarnation of "Sensory" and store it in a MySQL database
	 * @param input
	 * @return - true: insertion was successful. False otherwise
	 */
	public static boolean submitTrainingData(FloorData input)
	{
		try
		{
			Class.forName(myDriver);
			PreparedStatement insert = null;
			Connection conn = DriverManager.getConnection(URL, username, password);
			
			String SQL = "insert into " + DB + "." + TRAININGDATA + " "
					+ "values "
					+ "(?, "				// Primary Key
					+ "?, ?, ?, ?, ?, "   	// 5 Classes
					+ "?, ?, ?, ?, ?, ? "	// 6 GPS
					+ "?, ?, "				// 2 barometric
					+ "?, ?, ?, ?, ?, "		// 5 env features
					+ "?, ?, ?, ?, "		// 4 Magnetic field
					+ ");";

			// Fill up Regular Dataset based on sensory
			insert = conn.prepareStatement(SQL);

			// (5) Fill up Indoors/Created At/Device ID/Floor/RSSI
			insert.setInt(1, input.getIndoors());
			insert.setString(2, input.created_at());
			insert.setString(3, input.created_at());
			insert.setString(4, input.floor());
			insert.setInt(5, input.rssi_strength());
			
			// (6) Fill up GPS
			insert.setDouble(6, input.gps_longitude());
			insert.setDouble(7, input.gps_latitude());
			insert.setDouble(8, input.gps_vertical_accuracy());
			insert.setDouble(9, input.gps_horizontal_accuracy());
			insert.setDouble(10, input.gps_course());
			insert.setDouble(11, input.gps_speed());
			
			// (2) Barometric
			insert.setDouble(12, input.barometric_pressure());
			insert.setDouble(13, input.barometric_relative_altitude());
			
			// (5) Environment
			insert.setString(14, input.environment_context());
			insert.setString(15, input.environment_context());
			insert.setString(16, input.environment_mean_bldg_floors());
			insert.setString(17, input.city_name());
			insert.setString(18, input.country_name());

			// (4) Fill up Magnetic Field
			insert.setDouble(19, input.magnet_x_mt());
			insert.setDouble(20, input.magnet_y_mt());
			insert.setDouble(21, input.magnet_z_mt());
			insert.setDouble(22, input.magnet_total());	

			//Execute and Close SQL Command
			insert.execute();
			
			//DO NOT FORGET TO COMMIT!!
			conn.prepareCall("commit;").execute();
			return true;
		}
		catch(SQLException se)
		{
			se.printStackTrace();
			return false;
		}
		catch(ClassNotFoundException cnf)
		{
			System.err.println("Class Not Found Exception Caught");
			return false;
		}
	}

	public static boolean submitWifiData(String Room, String floor, String Building, List<ScanResult> result)
	{
		try
		{
			Class.forName(myDriver);
			PreparedStatement insert = null;
			Connection conn = DriverManager.getConnection(URL, username, password);
			
			// Fill up Wifi Training Table
			for (ScanResult res: result)
			{
				insert = conn.prepareStatement(""
						+ "insert into " + DB + "." + APTRAIN + " "
						+ "values(?, ?, ?, ?, "
						+ "?, ?, ?, ?, ?, "
						+ "?, ?, ?, ?, ?, ?);");
				insert.setString(2, Room);
				insert.setString(3, res.BSSID);
				insert.setString(4, res.SSID);
				insert.setString(5, res.capabilities);
				insert.setInt(9, res.frequency);
				insert.setInt	(10, res.level);
				//Execute and Close SQL Command
				insert.execute();
				insert.close();
			}
			return true;
		}
		catch(SQLException se)
		{
			se.printStackTrace();
			return false;
		}
		catch(ClassNotFoundException cnf)
		{
			return false;
		}
	}
	
	/**
	 * 1- Create the schema 
	 * 2- Create the Sensory Table for Android
	 * 3- Create the Sensory Table for iOS
	 * 4- Create the Wi-Fi Table for Android sensory
	 * @return 
	 * true: successfully initialized
	 * false: failed, might alraedy by there though
	 */
	public static boolean init()
	{
		try
		{
			Class.forName(myDriver);
			System.out.println("Connecting to a local database...");
			Connection conn = DriverManager.getConnection(URL, username, password);
			Statement stmt = conn.createStatement();
			
			// Build Database...
			stmt.execute("CREATE DATABASE " + DB);
			
			//===================BUILD TRAINING TABLE=================================
			/*
				CREATE TABLE fiu.dataset
				(  
				ScanID Integer not null,

				Longitude Double not null, 
				Latitude Double not null, 
				Altitude Double not null, 
				Baro_Altitude not null,

				Horizontal Double not null, 
				Vertical Double not null, 

				luminosity Double not null, 
				Humidity Double not null, 
				Pressure Double not null, 
				Temperature Double not null,
				Audio Double not null,

				MagnetX Double not null, 
				MagnetY Double not null, 
				MagnetZ Double not null, 
				TotalMagnet Double not null,
				currentTime DATETIME not null,

				Floor Text not null,
				Room Text,
				Building Text,
				Position Integer not null,
				PhoneNumber Text,
				OS Text not null,
				Model Text not null,
				Device Text not null,
				Product Text not null
				);
			 */

			String sqlTrain = "CREATE TABLE " + DB + "." + TRAININGDATA + " " +
					"(" + 
					" ScanID Integer PRIMARY KEY, "
					// Location
					+ "Longitude DECIMAL(10,3) not null, "
					+ "Latitude DECIMAL(10,3) not null, "
					+ "Altitude DECIMAL(10,3) not null, "
					+ "Barometric_Altitude DECIMAL(10,3) not null, "
					+ "sea_level DECIMAL(10,3) not null, "
					
					// Accuracy
					+ "Horizontal DECIMAL(10,2) not null, "
					+ "Vertical DECIMAL(10,2) not null, "
					
					// Environment
					+ "Luminosity DECIMAL(10,3) not null, "
					+ "Humidity DECIMAL(10,2) not null, "
					+ "Pressure DECIMAL(10,4) not null, "
					+ "Temperature DECIMAL(10,2) not null, "
					+ "Audio DECIMAL(10, 2) not null, "
					+ "Weather Text not null, "
					
					// Magnetic Field
					+ "MagnetX DECIMAL(10,3) not null, "
					+ "MagnetY DECIMAL(10,3) not null, "
					+ "MagnetZ DECIMAL(10,3) not null, "
					+ "TotalMagnet DECIMAL(10,5) not null, "
					
					// Room/Phone/Time
					+ "currentTime DATETIME not null, "
					+ "Floor Text, "
					+ "Room Text, "
					+ "Building Text, "
					+ "Position Integer not null, "
					+ "PhoneNumber Text, "
					
					// Phone Data
					+ "OS Text not null,"
					+ "Model Text not null,"
					+ "Device Text not null,"
					+ "Product Text not null"
					+ ");";
			//System.out.println(sqlTrain);
			stmt.executeUpdate(sqlTrain);
			stmt.close();

			stmt = conn.createStatement();

			// COMPLETE SCAN RESULT TABLE
			/*
			CREATE TABLE columbia.wifi
			(  
			ScanID Integer, 
			Room Text, 
			MACAddress Text,
			SSID Text,
			capability Text,
			centerFreq0 Integer,
			centerFreq1 Integer,
			channelWidth Text,
			frequency Integer,
			RSS Integer,
			operaterFriendlyName Text,
			timestamp Integer,
			venueName Text,
			80211mc Integer,
			passPoint Integer
			);
			 */
			stmt = conn.createStatement();
			stmt.executeUpdate("CREATE TABLE " + DB + "." + APTRAIN
					+ "( " 
					+ "ScanID Integer, " 
					+ "Room Text, "
					+ "MACAddress Text, "
					+ "SSID Text, "
					+ "capability Text, "
					+ "centerFreq0 Integer, "
					+ "centerFreq1 Integer, "
					+ "channelWidth Text, "
					+ "frequency Integer, "
					+ "RSS Integer, "
					+ "operaterFriendlyName Text, "
					+ "timestamp Integer, "
					+ "venueName Text, "
					+ "80211mc Integer, "
					+ "passPoint Integer, "
					+ "CONSTRAINT AP_ScanID FOREIGN KEY (ScanID) "
					+ "REFERENCES " + DB + "." + TRAININGDATA + "(ScanID) "
					+ ");");
			stmt.close();
			return true;
		}

		catch(SQLException sql)
		{
			sql.printStackTrace();
			return false;
		}
		catch (ClassNotFoundException e) 
		{
			return false;
		}
	}
	
	/**
	 * Given a Database and Table name, print a CSV to be used for analysis
	 * @param database - (String) Name of Database/Scheme 
	 * @param table - (String) Name of table you want to print to CSV
	 */
	public static void printTable(String database, String table)
	{
		List<String> columns = new ArrayList<String>();
		String header = "";
		PrintWriter writeCSV = null;
		try
		{
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(URL, username, password);
			PreparedStatement st = conn.prepareStatement("SHOW COLUMNS FROM ?.?;");
			st.setString(1, database);
			st.setString(2, table);
			
			ResultSet rs;
			
			// Get the Column Names
			rs = st.executeQuery();
			while (rs.next())
			{
				columns.add(rs.getString("Field"));
			}
			header = columns.stream().collect(Collectors.joining(","));
			
			// Use Select * to get all the rows
			st = conn.prepareStatement("SELECT * FROM ?.?;");
			st.setString(1, database);
			st.setString(2, table);
			rs = st.executeQuery();
			ResultSetMetaData resultSetMetaData = rs.getMetaData();
			
			// Print the values to a CSV file
			writeCSV = new PrintWriter(
					new BufferedWriter(
							new OutputStreamWriter(
									new FileOutputStream(database + '-' + table + ".csv"))));
			
			writeCSV.println(header);
			
			while(rs.next())
			{
				// For each row
				for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) 
				{
					int type = resultSetMetaData.getColumnType(i);
					if (type == Types.VARCHAR || type == Types.CHAR) 
					{
						writeCSV.println(rs.getString(i));
					}
					else if(type == Types.FLOAT)
					{
						writeCSV.println(rs.getFloat(i));
					}
					else if(type == Types.INTEGER)
					{
						writeCSV.println(rs.getInt(i));	
					}
					// Can fill for more types, but these are all I need really...
				}
			}
			writeCSV.close();
		}
		catch(ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
	}


	/**
	 * Build a new MySQL table to train a Wi-Fi classifier
	 * The Columns will be comprised of the most frequently seen MAC Addresses
	 * @throws IOException
	 */
	public static List<String> getMACAddressRows(double precent)
	{
		return null;
	}
	
	/**
	 * 
	 * @param frequency - Up to X AP frequencies
	 * @return 
	 */
	public static List<String> getMACAddressRows(String building, int frequency)
	{
		List<String> common_aps = new ArrayList<String>();
		try
		{
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(URL, username, password);
			
			/*
			 * SELECT MACADDRESS, COUNT(MACADDRESS) AS count
			 * FROM <Schema>.<Wifi-Table>
			 * WHERE Building = <Building for Classifier?>
			 * GROUP BY MACADDRESS
			 * ORDER BY count DESC
			 * LIMIT 20;
			 */
			PreparedStatement state = conn.prepareStatement(
					"SELECT MACADDRESS, Count(MACADDRESS) as count "
					+ "from " + DB + "." + TRAININGDATA + " "
					+ "Where Map= ?"
					+ "group by MACADDRESS "
					+ "ORDER BY count DESC LIMIT " + frequency + ";");
			state.setString(1, building);
			ResultSet rs = state.executeQuery();
			while (rs.next())
			{
				common_aps.add(rs.getString("MACADDRESS"));
			}
		}
		catch(ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return common_aps;
	}


	/**
	 * Creates a Table with both the Classes and all other columns to be MAC Addresses
	 * of the AP detected in a building.
	 * Source: SSTREU2017 createTables
	 * @throws IOException
	 */
	public static boolean createTables(String building, List<String> MAC_Address)
	{
		try
		{
			Class.forName(myDriver);
			System.out.println("Connecting to a local database...");
			Connection conn = DriverManager.getConnection(URL, username, password);
			Statement stmt = conn.createStatement();
			
			// BUILD ONE TABLE FOR THE BUILDING
			// Cols: Room, Floor, <MAC ADDRESSES>
			String sql =
					"CREATE TABLE " + DB + ".Wifi-" + building +
					"("
					+ " Room Text not NULL, "
					+ " Floor Text not NULL, ";
			String add = "";
			for (int i = 0; i < MAC_Address.size(); i++)
			{
				add += makeColumnName(MAC_Address.get(i)) + " INTEGER not NULL,";
			}
				
			sql += add;
			sql +=");"; 
			stmt.executeUpdate(sql);	
			return true;
		}
		catch(SQLException | ClassNotFoundException se)
		{
			se.printStackTrace();
			return false;
		}
	}
	
	// All Column names in MySQL can't start with a number
	// So if the MAC Address starts with 1 - 9, swap it!
	protected static String makeColumnName(String column)
	{
		String answer = "";
		if(column == null || column.length() == 0)
		{
			return answer;
		}
		char first = column.charAt(0);
		// Map the following -> (Jump 65)
		// 0 (48) --> q (113)
		// 1 (49) --> r (114)
		// 2 (50) --> s (115)
		// 3 (51) --> t (116)
		// 4 (52) --> u (117)
		// 5 (53) --> v (118)
		// 6 (54) --> w (119)
		// 7 (55) --> x (120)
		// 8 (56) --> y (121)
		// 9 (57) --> z (122)

		if(Character.isDigit(first))
		{
			char alphabet = (char) (((int) first) + 65);
			answer = alphabet + column.substring(1);
			answer = answer.replace(':', '_');
			return answer;
		}
		else
		{
			answer = column.replace(':', '_');
			return answer;
		}
	}

	// All Column names in MySQL can't start with a number
	// So if the MAC Address starts with 1 - 9, swap it!
	protected static String getColumnName(String column)
	{
		String answer = "";
		if(column == null || column.length() == 0)
		{
			return answer;
		}
		char first = column.charAt(0);
		// NOTE WE DO REVERSE THIS TIME
		// Map the following -> (Jump 65)
		// 0 (48) --> q (113)
		// 1 (49) --> r (114)
		// 2 (50) --> s (115)
		// 3 (51) --> t (116)
		// 4 (52) --> u (117)
		// 5 (53) --> v (118)
		// 6 (54) --> w (119)
		// 7 (55) --> x (120)
		// 8 (56) --> y (121)
		// 9 (57) --> z (122)
		// If the first character in set [q, z] then -65 to get correct MAC back
		if("qrstuvwxyz".indexOf(first) != -1)
		{
			char alphabet = (char) (((int) first) - 65);
			answer = alphabet + column.substring(1);
			answer = answer.replace('_', ':');
			return answer;
		}
		else
		{
			answer = column.replace('_', ':');
			return answer;
		}
	}

	public static boolean updateTable()
	{
		return true;
	}
	
	private void closeClientConnection() throws IOException
	{
		fromClient.close();
		toClient.close();
		if (incomingClient != null && incomingClient.isConnected())
		{
			incomingClient.close();	
		}
	}
}