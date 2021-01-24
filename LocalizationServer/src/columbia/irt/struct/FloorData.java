package columbia.irt.struct;

import java.io.Serializable;

public class FloorData implements Serializable
{
	private static final long serialVersionUID = 5376412116682569286L;

	// 1 - indoors 0 - outdoors
	private final int indoors;
	// Time of scan
	private final String created_at;
	private final String session_id;
	// Time scan started, but oops got device ID though
	private final String device_id;
	private final String room;
	private final String floor;
	private final String building;
	private final String connected_ap;
	private final int is_center;

	private final int rssi_strength;
	private final double gps_alt;
	private final double gps_latitude;
	private final double gps_longitude;
	private final double gps_vertical_accuracy;
	private final double gps_horizontal_accuracy;
	private final double gps_course;
	private final double gps_speed;
	private final double sea_level;
	private final double barometric_relative_altitude;
	private final double barometric_pressure;
	private final String environment_context;
	private final String environment_mean_bldg_floors;
	private final String environment_activity;
	private final String city_name;
	private final String country_name;
	private final double magnet_x_mt;
	private final double magnet_y_mt;
	private final double magnet_z_mt;
	private double magnet_total;

	private final WifiData result;

	// This is only for Sensory functionality
	// Fill data from sensors
	public FloorData(
			int indoors, String created_at, String session_id, String device_id,
			String room, String floor, String building, String connected_ap,
			int rssi_strength, int is_center, double gps_alt, double gps_latitude,
			double gps_longitude, double gps_vertical_accuracy, double gps_horizontal_accuracy,
			double gps_course, double gps_speed,
			double sea_level, double barometric_pressure, double barometric_relative_altitude,
			String environment_context, String environment_mean_bldg_floors,
			String environment_activity, String city_name, String country_name,
			double magnet_x_mt, double magnet_y_mt, double magnet_z_mt, WifiData result
	)
	{
		// Table has ID as auto-incremented private key
		this.indoors = indoors;
		this.created_at = created_at;
		this.session_id = session_id;
		this.device_id = device_id;
		this.room = room;
		this.floor = floor;
		this.building = building;
		this.connected_ap = connected_ap;
		this.rssi_strength = rssi_strength;
		this.is_center = is_center;

		this.gps_alt = gps_alt;
		this.gps_latitude = gps_latitude;
		this.gps_longitude = gps_longitude;
		this.gps_vertical_accuracy = gps_vertical_accuracy;
		this.gps_horizontal_accuracy = gps_horizontal_accuracy;
		this.gps_course = gps_course;
		this.gps_speed = gps_speed;
		this.sea_level = sea_level;
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
		this.result = result;
	}

	public int getIndoors()
	{
		return this.indoors;
	}

	public String created_at()
	{
		return this.created_at;
	}
	
	public String session_id()
	{
		return this.session_id;
	}
	
	public String device_id()
	{
		return this.device_id;
	}

	public String room()
	{
		return this.room;
	}

	public String floor()
	{
		return this.floor;
	}

	public String building()
	{
		return this.building;
	}

	public String connected_ap()
	{
		return this.connected_ap;
	}

	public int rssi_strength()
	{
		return this.rssi_strength;
	}

	public int is_center()
	{
		return this.is_center;
	}

	public double gps_alt()
	{
		return this.gps_alt;
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

	public double sea_level()
	{
		return this.sea_level;
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

	public WifiData wifi()
	{
		return this.result;
	}

	public String toString()
	{
		return String.valueOf(this.getIndoors()) +
				',' +
				this.created_at() +
				',' +
				this.session_id() +
				',' +
				this.device_id() +
				',' +
				this.room() +
				',' +
				this.floor() +
				',' +
				this.building() +
				',' +
				this.connected_ap() +
				',' +
				this.rssi_strength() +
				',' +
				this.is_center() +
				',' +
				this.gps_latitude() +
				',' +
				this.gps_alt() +
				',' +
				this.gps_longitude() +
				',' +
				this.gps_vertical_accuracy() +
				',' +
				this.gps_horizontal_accuracy() +
				',' +
				this.gps_course() +
				',' +
				this.gps_speed() +
				',' +
				this.sea_level() +
				',' +
				this.barometric_relative_altitude() +
				',' +
				this.barometric_pressure() +
				',' +
				this.environment_context() +
				',' +
				this.environment_mean_bldg_floors() +
				',' +
				this.environment_activity() +
				',' +
				this.city_name() +
				',' +
				this.country_name() +
				',' +
				this.magnet_x_mt() +
				',' +
				this.magnet_y_mt() +
				',' +
				this.magnet_z_mt() +
				',' +
				this.magnet_total() +
				',' +
				wifi().toString();
	}
}