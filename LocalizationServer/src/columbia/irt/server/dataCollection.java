package columbia.irt.server;

import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

import columbia.irt.struct.FloorData;
import columbia.irt.struct.WifiData;


public class dataCollection implements Runnable
{
	// SQL Login and parameters
	protected static String username = "";
	protected static String password = "";
	protected static String DB = "";
	protected static String TRAININGDATA = "";

	// I/O
	private ObjectInputStream fromClient = null;
	private ObjectOutputStream toClient = null;

	// SQL
	protected final static String myDriver = "org.gjt.mm.mysql.Driver";
	protected final static String URL = "jdbc:mysql://localhost:3306/?useSSL=false";

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
			boolean result;
			FloorData f = null;
			
			fromClient = new ObjectInputStream(incomingClient.getInputStream());
			toClient = new ObjectOutputStream(incomingClient.getOutputStream());
			// Read Object
			Object x = fromClient.readObject();

			if (x instanceof FloorData)
			{
				f = (FloorData) x;
				result = submitTrainingData(f);
				System.out.println(f.toString());
					
				WifiData wifi = f.wifi();
				int scan = getScanID();

				result = submitWifiData(scan, f.room(), f.floor(), f.building(), wifi);
				toClient.writeBoolean(result);
				toClient.flush();
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
					+ "(default, "					// Primary Key, already done for you!
					+ "?, ?, ?, ?, ?, ?, ?, ?, ?, "	// 9 Classes
					+ "?, ?, ?, ?, ?, ?, ?, "		// 7 GPS
					+ "?, ?, ?, "					// 3 barometric
					+ "?, ?, ?, ?, ?, "				// 5 env features
					+ "?, ?, ?, ?"					// 4 Magnetic field
					+ ");";

			// Fill up Regular Data set based on sensory
			insert = conn.prepareStatement(SQL);

			// (5) Fill up Indoors/Created At/Device ID/Floor/RSSI
			insert.setInt(1, input.getIndoors());
			insert.setString(2, input.created_at());
			insert.setString(3, input.device_id());
			insert.setString(4, input.room());
			insert.setInt(5, Integer.parseInt(input.floor()));
			insert.setString(6, input.building());
			insert.setString(7, input.connected_ap());
			insert.setInt(8, input.rssi_strength());
			insert.setInt(9, input.is_center());
			
			// (6) Fill up GPS
			insert.setDouble(10, input.gps_alt());
			insert.setDouble(11, input.gps_longitude());
			insert.setDouble(12, input.gps_latitude());
			insert.setDouble(13, input.gps_vertical_accuracy());
			insert.setDouble(14, input.gps_horizontal_accuracy());
			insert.setDouble(15, input.gps_course());
			insert.setDouble(16, input.gps_speed());
			
			// (3) Barometric
			insert.setDouble(17, input.sea_level());
			insert.setDouble(18, input.barometric_pressure());
			insert.setDouble(19, input.barometric_relative_altitude());
			
			// (5) Environment
			insert.setString(20, input.environment_context());
			insert.setString(21, input.environment_context());
			insert.setString(22, input.environment_mean_bldg_floors());
			insert.setString(23, input.city_name());
			insert.setString(24, input.country_name());
			
			// (4) Fill up Magnetic Field
			insert.setDouble(25, input.magnet_x_mt());
			insert.setDouble(26, input.magnet_y_mt());
			insert.setDouble(27, input.magnet_z_mt());
			insert.setDouble(28, input.magnet_total());	
			
			//System.out.println(SQL);
			
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

	public static int getScanID()
	{
		int id = -1;
		try
		{
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(URL, username, password);
			String query = "SELECT ID "
					+ " FROM " + DB + '.' + TRAININGDATA
					+ " ORDER BY ID DESC "
					+ " LIMIT 1;";
			PreparedStatement st = conn.prepareStatement(query);
			st.execute();
			ResultSet rs = st.executeQuery();
			
			while(rs.next())
			{
				id = rs.getInt(1);
			}
			return id;
		}
		
		catch(SQLException se)
		{
			se.printStackTrace();
			return -1;
		}
		catch(ClassNotFoundException cnf)
		{
			System.err.println("Class Not Found Exception Caught");
			return -1;
		}
	}
	
	public static boolean submitWifiData(int scanID, String Room, String floor, String Building, WifiData result)
	{
		PreparedStatement insert = null;
		try
		{
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(URL, username, password);

			
			// Fill up Wi-Fi Training Table
			for (int i = 0; i < result.WifiAPs.length; i++)
			{
				insert = conn.prepareStatement(""
						+ "insert into " + DB + "." + APTRAIN + " values("
						+ "?, ?, ?, ?, " 	// (4) Scan ID and labels
						+ "?, ?, ?, ?, ?, "	// (5) Basic Scan Result Labels
						+ "?, ?, ?, ?, "	// (4) First 4 advanced features
						+ "?, ?, ?, ? "		// (4) Last 4 advanced features
						+ ");");
				
				// Scan id and labels
				insert.setInt(1, scanID);
				insert.setString(2, Room);
				insert.setString(3, floor);
				insert.setString(4, Building);
				
				// Basic 5
				insert.setString(5, result.WifiAPs[i]);
				insert.setString(6, result.SSID[i]);
				insert.setString(7, result.capabilities[i]);
				insert.setInt(8, result.frequency[i]);
				insert.setInt(9, result.WifiRSS[i]);
				
				// 8 Advanced features
				insert.setInt(10, result.centerFreq0[i]);
				insert.setInt(11, result.centerFreq1[i]);
				insert.setString(12, result.channelWidth[i]);
				insert.setString(13, result.operatorFriendlyName[i]);
			
				insert.setLong(14, result.timestamp[i]);
				insert.setString(15, result.vanueName[i]);
				insert.setInt(16, result.is80211mc[i]);
				insert.setInt(17, result.isPassPoint[i]);

				//Execute and Close SQL Command
				insert.execute();
				insert.close();
			}
			
			conn.prepareCall("commit;").execute();
			return true;
		}
		catch(SQLException se)
		{
			System.out.println("Evil: " + insert.toString());
			se.printStackTrace();
			return false;
		}
		catch(ClassNotFoundException cnf)
		{
			cnf.printStackTrace();
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
	 * 
	 * References:
	 * https://stackoverflow.com/questions/39463134/how-to-store-emoji-character-in-mysql-database
	 * https://stackoverflow.com/questions/38813689/cant-initialize-character-set-utf8mb4-with-windows-mysql-python
	 * 
	 * THERE ARE APPARENTLY UTF-8 EMOJIS IN SSIDs! This configuration was needed JUST IN CASE!
	 */
	public static boolean init()
	{
		Statement stmt = null;
		try
		{
			Class.forName(myDriver);
			System.out.println("Connecting to a local database...");
			Connection conn = DriverManager.getConnection(URL, username, password);
			stmt = conn.createStatement();
			
			// Build Database...
			// https://stackoverflow.com/questions/39463134/how-to-store-emoji-character-in-mysql-database
			stmt.execute("CREATE DATABASE IF NOT EXISTS " + DB + " DEFAULT CHARSET = utf8mb4 DEFAULT COLLATE = utf8mb4_unicode_ci");

			String sqlTrain = "CREATE TABLE IF NOT EXISTS " + DB + "." + TRAININGDATA + "(" + 
					"  `ID` int NOT NULL AUTO_INCREMENT, " + 
					"  `indoors` int DEFAULT NULL, " + 
					"  `created_at` varchar(100) NOT NULL, " + 
					"  `device_id` varchar(100) NOT NULL, " + 
					"  `room` varchar(100) NOT NULL, " + //new
					"  `floor` int DEFAULT NULL, " + 
					"  `building` varchar(100) NOT NULL, " + //new
					"  `connected_ap` varchar(100) NOT NULL, "  + // new
					"  `rssi_strength` int DEFAULT NULL, " + 
					"  `is_center` varchar(100) NOT NULL, "  + // new
					"  `gps_alt` float DEFAULT NULL, " + 
					"  `gps_longitude` float DEFAULT NULL, " + 
					"  `gps_latitude` float DEFAULT NULL, " + 
					"  `gps_vertical_accuracy` int DEFAULT NULL, " + 
					"  `gps_horizontal_accuracy` int DEFAULT NULL, " + 
					"  `gps_course` float DEFAULT NULL, " + 
					"  `gps_speed` float DEFAULT NULL, " + 
					"  `baro_sea_level` float DEFAULT NULL, " + // new
					"  `baro_relative_altitude` float DEFAULT NULL, " + 
					"  `baro_pressure` float DEFAULT NULL, " + 
					"  `env_context` varchar(100) DEFAULT NULL, " + 
					"  `env_mean_bldg_floors` varchar(100) DEFAULT NULL, " + 
					"  `env_activity` varchar(100) DEFAULT NULL, " + 
					"  `city_name` varchar(100) DEFAULT NULL, " + 
					"  `country_name` varchar(100) DEFAULT NULL, " + 
					"  `magnet_x_mt` float DEFAULT NULL, " + 
					"  `magnet_y_mt` float DEFAULT NULL, " + 
					"  `magnet_z_mt` float DEFAULT NULL, " + 
					"  `magnet_total` float DEFAULT NULL, " + 
					"  PRIMARY KEY (`ID`)" + 
					") AUTO_INCREMENT=1081 ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE = utf8mb4_unicode_ci";
			//System.out.println(sqlTrain);
			stmt.executeUpdate(sqlTrain);
			stmt.close();

			stmt = conn.createStatement();
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS " + DB + "." + APTRAIN
					+ "( " 
					+ "ID Integer NOT NULL, "
					+ "Room varchar(100) DEFAULT NULL,  "
					+ "Floor varchar(100) DEFAULT NULL,  "
					+ "Building varchar(100) DEFAULT NULL,  "
					+ "MACAddress varchar(100), "
					// OK There are UTF-8 emojis on some SSIDs....
					+ "SSID varchar(100), "
					+ "capability  varchar(100), "
					+ "frequency Integer, "
					+ "RSS Integer, "
			  		+ "centerFreq0 Integer, "
					+ "centerFreq1 Integer, "
					+ "channelWidth varchar(100), "
					+ "operaterFriendlyName varchar(100) , "
					+ "timestamp Integer, "
					+ "venueName varchar(100) , "
					+ "80211mc Integer, "
					+ "passPoint Integer, "
					+ "FOREIGN KEY (ID) REFERENCES " + DB + '.' + TRAININGDATA + "(ID) "
					+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci");
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
			PreparedStatement st = conn.prepareStatement("SHOW COLUMNS FROM " + database + '.' + table);
			ResultSet rs;
			
			//System.out.println(st.toString());
			// Get the Column Names
			rs = st.executeQuery();
			while (rs.next())
			{
				columns.add(rs.getString("Field"));
			}
			header = columns.stream().collect(Collectors.joining(","));
			
			// Use Select * to get all the rows
			st = conn.prepareStatement("SELECT * FROM " + database + '.' + table);
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
				// For more types: 
				// https://docs.oracle.com/javase/8/docs/api/constant-values.html
				for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) 
				{
					int type = resultSetMetaData.getColumnType(i);
					if (type == Types.VARCHAR || type == Types.CHAR) 
					{
						writeCSV.print(rs.getString(i));
					}
					else if(type == Types.FLOAT)
					{
						writeCSV.print(rs.getFloat(i));
					}
					else if(type == Types.INTEGER || type == Types.REAL)
					{
						writeCSV.print(rs.getInt(i));	
					}
					writeCSV.print(',');
				}
				writeCSV.println();
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