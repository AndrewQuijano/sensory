package columbia.irt.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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
import java.util.List;

public class getAccessPoint extends SqlConfiguration implements Runnable 
{
	public void run() 
	{
		// Should be done before Lookup Tables are made...
		// Get all APs from MySQL database
		
		List<String> APs = new ArrayList<String>();
		try 
		{
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(URL, username, password);
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT distinct MACAddress from " + DB + "." + APTRAIN + ";");
			
			while (rs.next())
			{
				APs.add(rs.getString("MACADDRESS"));
			}
			
			//---------------DO GET REQUEST--------------------
			HashMap<String, Integer> Maker_freq = new HashMap<String, Integer>();
			int last = 0;
			String manufacturer = null;
			
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
						Thread.sleep(2400);
						--i;
						// If I backtrack too much, just bail!
						if(last > i)
						{
							break;
						}
						continue;
					}
					else if(responseCode == 200)
					{
						BufferedReader rd = new BufferedReader(new InputStreamReader(connect.getInputStream()));
						String line;
						StringBuffer response = new StringBuffer();
						
						while ((line = rd.readLine()) != null) 
						{
							response.append(line);
						}
						rd.close();
						manufacturer = response.toString();
					}
					else
					{
						System.out.println("ERROR, NOT 200 or 429");
						System.out.println("Sending 'GET' request to URL : " + url);
						System.out.println("Response Code : " + responseCode);
						continue;
					}
					
					// Check frequency...
					if(Maker_freq.get(manufacturer ) == null)
					{
						Maker_freq.put(manufacturer , 1);
					}
					else
					{
						Maker_freq.put(manufacturer , Maker_freq.get(manufacturer).intValue() + 1);
					}
					last = i;
				}
				catch(IOException f) 
				{
					f.printStackTrace();
				}
				System.out.println("AP: " + APs.get(i) + " Company: " + manufacturer);
				Thread.sleep(1200); //No API 1 request a second, add .2 as as slack
			}
			
			// Print to CSV
			PrintWriter writeCSV = new PrintWriter(
					new BufferedWriter(
							new OutputStreamWriter(
									new FileOutputStream("manufacturer-frequency.csv"))));
			writeCSV.println("Manufacturer,Frequency");
			
			for (String key: Maker_freq.keySet())
			{
	            Integer value = Maker_freq.get(key);  
	            writeCSV.println(key + "," + value);  
			}
			writeCSV.close();
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
