package columbia.irt.struct;

import java.io.Serializable;

import columbia.irt.sensors.BarometricAltimeter;
import columbia.irt.sensors.GPSAltimeter;
import columbia.irt.sensors.MagneticFieldSensor;

/**
 * This class contains all the data from the original William Falcon Dataset
 */
public class floorData implements Serializable
{
    // 1 - indoors 0 - outdoors
    private int indoors;
    // Time of scan
    private String created_at;
    // Time scan started
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
    public floorData(GPSAltimeter gps, BarometricAltimeter bps, MagneticFieldSensor mps)
    {
        indoors = 1;
        created_at = "";
        session_id = "";
        floor = "";
        rssi_strength = -1;
        gps_latitude = gps.latitude;
        gps_longitude = gps.longitude;
        gps_vertical_accuracy = gps.vAccuracy;
        gps_horizontal_accuracy = gps.hAccuracy;
        gps_course = gps.course;
        gps_speed = gps.speed;
        barometric_relative_altitude = bps.barometricAltitude;
        barometric_pressure = bps.pressure;
        environment_context = "";
        environment_mean_bldg_floors = "";
        environment_activity = "";
        city_name = gps.city_name;
        country_name = gps.country_name;
        magnet_x_mt = mps.magnetX;
        magnet_y_mt = mps.magnetY;
        magnet_z_mt = mps.magnetZ;
        magnet_total = magnet_x_mt * magnet_x_mt;
        magnet_total += magnet_y_mt * magnet_y_mt;
        magnet_total += magnet_z_mt * magnet_z_mt;
        magnet_total = Math.sqrt(magnet_total);
    }
}
