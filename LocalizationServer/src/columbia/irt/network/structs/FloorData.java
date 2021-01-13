package columbia.irt.network.structs;

public class FloorData
{
	// 1 - indoors 0 - outdoors
	private int indoors;
	// Time of scan
	private String created_at;
	// Time scan started, but oops got device ID though
	private String session_id;
	private String floor;
	private int rssi_strength;
	private double gps_latitude;
	private double gps_longitude;
	private double gps_vertical_accuracy;
	private double gps_horizontal_accuracy;
	private double gps_course;
	private double gps_speed;
	private double barometric_relative_altitude;
	private double barometric_pressure;
	private String environment_context;
	private String environment_mean_bldg_floors;
	private String environment_activity;
	private String city_name;
	private String country_name;
	private double magnet_x_mt;
	private double magnet_y_mt;
	private double magnet_z_mt;
	private double magnet_total;

	// Fill data from sensors
	public FloorData(
			int indoors, String created_at, String session_id,
			String floor, int rssi_strength, double gps_latitude, 
			double gps_longitude, double gps_vertical_accuracy, double gps_horizontal_accuracy,
			double gps_course, double gps_speed, double barometric_relative_altitude,
			double barometric_pressure, String environment_context, String environment_mean_bldg_floors,
			String environment_activity, String city_name, String country_name,
			double magnet_x_mt, double magnet_y_mt, double magnet_z_mt
			)
	{
		// Table has ID as auto-incremented private key
		this.indoors = indoors;
		this.created_at = created_at; 
		this.session_id = session_id; //Oops, got device ID
		this.floor = floor;
		this.rssi_strength = rssi_strength;
		this.gps_latitude = gps_latitude;
		this.gps_longitude = gps_longitude;
		this.gps_vertical_accuracy = gps_vertical_accuracy;
		this.gps_horizontal_accuracy = gps_horizontal_accuracy;
		this.gps_course = gps_course;
		this.gps_speed = gps_speed;
		this.barometric_relative_altitude = barometric_relative_altitude;
		this.barometric_pressure = barometric_pressure;
		this.environment_context = environment_context;
		this.environment_mean_bldg_floors = environment_mean_bldg_floors;
		this.environment_activity = environment_activity;
		this.city_name = city_name;
		this.country_name = country_name;
		this.magnet_x_mt = magnet_x_mt;
		this.magnet_y_mt = magnet_y_mt;
		this.magnet_z_mt = magnet_z_mt;
		this.magnet_total = magnet_x_mt * magnet_x_mt;
		this.magnet_total += magnet_y_mt * magnet_y_mt;
		this.magnet_total += magnet_z_mt * magnet_z_mt;
		this.magnet_total = Math.sqrt(magnet_total);
	}
	
	public int getIndoors()
	{
		return this.indoors;
	}
	
	public String created_at()
	{
		return this.created_at;
	}
	
	// Time scan started
	public String session_id()
	{
		return this.session_id;
	}
	
	public String floor()
	{
		return this.floor;
	}
	
	public int rssi_strength()
	{
		return this.rssi_strength;
	}
	
	public double gps_latitude()
	{
		return this.gps_latitude;
	}
	
	public double gps_longitude()
	{
		return this.gps_longitude;
	}
	
	public double gps_vertical_accuracy()
	{
		return this.gps_vertical_accuracy;
	}
	
	public double gps_horizontal_accuracy()
	{
		return this.gps_horizontal_accuracy;
	}
	
	public double gps_course()
	{
		return this.gps_course;
	}
	
	public double gps_speed()
	{
		return this.gps_speed;
	}
	
	public double barometric_relative_altitude()
	{
		return this.barometric_relative_altitude;
	}
	
	public double barometric_pressure()
	{
		return this.barometric_pressure;
	}
	
	public String environment_context()
	{
		return this.environment_context;
	}
	
	public String environment_mean_bldg_floors()
	{
		return this.environment_mean_bldg_floors;
	}
	
	public String environment_activity()
	{
		return this.environment_activity;
	}
	
	public String city_name()
	{
		return this.city_name;
	}
	
	public String country_name()
	{
		return this.country_name;
	}
	
	public double magnet_x_mt()
	{
		return this.magnet_x_mt;
	}
	
	public double magnet_y_mt()
	{
		return this.magnet_y_mt;
	}
	
	public double magnet_z_mt()
	{
		return this.magnet_z_mt;
	}
	
	public double magnet_total()
	{
		return this.magnet_total;
	}
}