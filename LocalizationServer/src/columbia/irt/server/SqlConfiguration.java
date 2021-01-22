package columbia.irt.server;

public class SqlConfiguration 
{
	// SQL Driver settings
	protected final static String URL = "jdbc:mysql://localhost:3306/?useSSL=false";
	protected final static String myDriver = "org.gjt.mm.mysql.Driver";
	
	// SQL Login
	protected static String username = "";
	protected static String password = "";
	
	// SQL Tables/Database names
	protected static String DB = "columbia"; // sensory schema
	protected static String TRAININGDATA = ""; // Android Table
	protected final static String APTRAIN = "Wifi"; //Android WiFi Table
}
