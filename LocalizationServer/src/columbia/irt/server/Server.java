package columbia.irt.server;

import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;


public class Server implements Runnable
{
	protected int          serverPort 	 = 9000;
	protected ServerSocket serverSocket  = null;
	protected boolean      isStopped     = false;
	protected Thread       runningThread = null;
	protected static int   primaryKey 	 = 1;
	public static HashMap<String, String> map = new HashMap<String, String>();
	public static boolean useWifiName = false;
	
	public Server(int port)
	{
		serverPort = port;
	}
	
	public void run()
	{
		synchronized(this)
		{
			this.runningThread = Thread.currentThread();
		}
		openServerSocket();
		while(! isStopped())
		{
			Socket clientSocket = null;
			try
			{
				clientSocket = this.serverSocket.accept();;
			}
			catch (IOException e)
			{
				if(isStopped())
				{
					System.out.println("Server Stopped.") ;
					return;
				}
				throw new RuntimeException("Error accepting client connection", e);
			}
			new Thread(new dataCollection(clientSocket)).start();
		}
		System.out.println("Server Stopped.");
	}
	
	private synchronized boolean isStopped()
	{
		return this.isStopped;
	}

	public synchronized void stop()
	{
		this.isStopped = true;
		try
		{
			this.serverSocket.close();
		}
		catch (IOException e)
		{
			throw new RuntimeException("Error closing server", e);
		}
	}

	private void openServerSocket()
	{
		try
		{
			this.serverSocket = new ServerSocket(this.serverPort);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Cannot open port " + this.serverPort, e);
		}
	}
	
	public static void main(String [] args) 
			throws ClassNotFoundException, IOException, InterruptedException, SQLException
	{
		Scanner inputReader = new Scanner(System.in);
		int port = 9254;
		Properties login = null;
		// Run the initial set up
		try
		{
			String cwd = System.getProperty("user.dir");
			System.out.println("Working Directory = " + System.getProperty("user.dir"));
			// Finally, load user/password credentials
			login = new Properties();
			try (FileReader in = new FileReader(cwd + "\\database.properties")) 
			{
			    login.load(in);
			}
			SqlConfiguration.username = login.getProperty("user");
			SqlConfiguration.password = login.getProperty("password");
			SqlConfiguration.DB = login.getProperty("schema");
			SqlConfiguration.TRAININGDATA = login.getProperty("android");
			
			// Custom Port if needed?
			if (args.length == 1)
			{
				port = Integer.parseInt(args[0]);
				if(port <= 1024)
				{
					System.err.println("Invalid Port! " + port + " use value over 1024!");
					System.exit(1);
				}
				else if(port > 65535)
				{
					System.err.println("Invalid Port! " + port + " use value below 65535!");
					System.exit(1);					
				}
			}
			
		}
		catch (NumberFormatException nfe)
		{
			System.out.println("Please enter a valid custom port number");
			System.exit(1);
		}
		catch (IOException e) 
		{
			System.out.println("Missing Database.Properties file in right location");
			System.exit(1);
		}
		
		// Create the Schema and Tables
		dataCollection.init();
		
		// Run the server
		Server Localizationserver = new Server(port);
		new Thread(Localizationserver).start();
		
		while(true)
		{
			try
			{
				System.out.print("sensory-server>");
				String input = inputReader.nextLine();
				input = input.trim();
				String [] commands = input.split(" ");
				
				// Clear CLI
				if (commands[0].equalsIgnoreCase("clr") || commands[0].equalsIgnoreCase("clear"))
				{	
					System.out.println("\033[H\033[2J");
					System.out.flush();
				}				
				// Get AP Manufacturer Based on MAC Address
				// It will also print a Frequency map on it
				else if(commands[0].equalsIgnoreCase("AP"))
				{
					getAccessPoint AP = new getAccessPoint();
					(new Thread(AP)).start();
				}
				// Print the entire sensory table
				else if(commands[0].equalsIgnoreCase("sensory"))
				{
					// Print Android Table and iPhone table
					dataCollection.printTable(dataCollection.DB, dataCollection.TRAININGDATA, false);
					dataCollection.printTable(dataCollection.DB, login.getProperty("table"), false);
				}
				// Print the Wi-Fi Table
				// It will have same structure as created in SST REU 2017
				else if(commands[0].equalsIgnoreCase("wifi"))
				{
					if(commands.length <= 2)
					{
						continue;
					}
					int limit = Integer.parseInt(commands[1]);
					String building = commands[2]; //get all other args as part of building 
					for(int i = 3; i < commands.length; i++)
					{
						building += " ";
						building += commands[i];
					}
					// System.out.println("Building: " + building + " int: " + limit);
					List<String> mac = dataCollection.getMACAddressRows(building, limit);
					String table = building.replace(" ", "_");

					if(dataCollection.createTables(table, mac))
					{
						if(dataCollection.updateTable(table, mac))
						{
							System.out.println("Successfully created Wifi Lookup Table...printing it!");
							dataCollection.printTable(dataCollection.DB, table, true);
						}
						else
						{
							System.out.println("Failed to update Look up Table");
						}
					}
					else
					{
						System.out.println("Failed to Create Look up table!");
					}
				}
				else if(commands[0].equalsIgnoreCase("frequency"))
				{
					if(commands.length <= 1)
					{
						continue;
					}
					String building = commands[1]; //get all other args as part of building 
					for(int i = 2; i < commands.length; i++)
					{
						building += " ";
						building += commands[i];
					}
					dataCollection.getMACAddressFrequencyMap(building);
				}
				// Exit the shell
				else if (commands[0].equalsIgnoreCase("exit"))
				{
					break;
				}
			}
			catch(NumberFormatException e)
			{
				continue;
			}
		}
		inputReader.close();
		System.out.println("Stopping Server...");
		Localizationserver.stop();
	}
}