package columbia.irt.server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

public class getAccessPoint implements Runnable 
{
	public String [] Makers;
	
	// SQL
	protected final static String myDriver = "org.gjt.mm.mysql.Driver";
	protected final static String DB = "columbia";
	protected final static String URL = "jdbc:mysql://localhost:3306/?useSSL=false";
	protected final static String APTRAIN = "Wifi";
	protected static String username = "";
	protected static String password = "";
	
	public void run() 
	{
		// Should be done before Lookup Tables are made...
		// Get all APs from MySQL database
		
		ArrayList<String> APs = new ArrayList<String>();
		try 
		{
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(URL, username, password);
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT MACAddress from " + DB + "." + APTRAIN + ";");
			
			while (rs.next())
			{
				// Note to self: DO NOT USE FOR LOOPS!! 
				APs.add(rs.getString("MACADDRESS"));
			}
			
			//---------------DO GET REQUEST--------------------
			Makers = new String[APs.size()];
			HashMap<String, Integer> Maker_freq = new HashMap<String, Integer>();
			int last = 0;
		
			// Now get the Manufacturer of all the AP's
			for (int i = 0; i < APs.size(); i++)
			{
				URL url = new URL("http://api.macvendors.com/" + APs.get(i));
				HttpURLConnection connect = (HttpURLConnection) url.openConnection();
				connect.setRequestMethod("GET");
				connect.addRequestProperty("User-Agent", "Mozilla/5.0");
				try
				{
					String redirect = connect.getHeaderField("Location");
					if (redirect != null)
					{
					    connect = (HttpURLConnection) new URL(redirect).openConnection();
					}
					
					int responseCode = connect.getResponseCode();
					// Too many requests, SLOW DOWN AND TRY AGAIN!
					if(responseCode == 429)
					{
						System.err.println("TOO MANY REQUESTS SENT!");
						Thread.sleep(2400);
						--i;
						// If I backtrack too much, just bail!
						if(last > i)
						{
							break;
						}
						continue;
					}
					System.out.println("\nSending 'GET' request to URL : " + url);
					System.out.println("Response Code : " + responseCode);
					
					BufferedReader rd = new BufferedReader(new InputStreamReader(connect.getInputStream()));
					String line;
					StringBuffer response = new StringBuffer();
					
					while ((line = rd.readLine()) != null) 
					{
						response.append(line);
					}
					rd.close();
					Makers[i] = response.toString();
					
					// Check frequency...
					if(Maker_freq.get(Makers[i])==null)
					{
						Maker_freq.put(Makers[i], 1);
					}
					else
					{
						Maker_freq.put(Makers[i], Maker_freq.get(Makers[i]).intValue() + 1);
					}
					last = i;
				}
				catch(FileNotFoundException f)
				{
					Makers[i] = "NOT FOUND";
				}
				catch(ConnectException f)
				{
					Makers[i] = "NOT FOUND";
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
				System.out.println("Manufacturer: " + i + " Company: " + Makers[i]);
				Thread.sleep(1200);//No API 1 request a second, add .2 as as slack
			}
			//sdataCollection.printHashMap(Maker_freq);
		}
		catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
		catch (MalformedURLException e1)
		{
			e1.printStackTrace();
		} 
		catch (ProtocolException e1) 
		{
			e1.printStackTrace();
		}
		catch (IOException e1) 
		{
			e1.printStackTrace();
		}
	}
}
