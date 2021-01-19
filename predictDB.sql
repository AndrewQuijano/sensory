/*may need to specify database*/
CREATE SCHEMA IF NOT EXISTS `sensory_schema`;

CREATE TABLE IF NOT EXISTS sensory_schema.`predictDB` (
  `ID` int NOT NULL AUTO_INCREMENT,
   `indoors` int DEFAULT NULL,
  `created_at` varchar(100) NOT NULL,
  `device_id` varchar(100) NOT NULL,
  `floor` int DEFAULT NULL,
   `rssi_strength` int DEFAULT NULL,
  `gps_alt` float DEFAULT NULL,
  `gps_longitude` float DEFAULT NULL,
  `gps_latitude` float DEFAULT NULL,
  `gps_vertical_accuracy` int DEFAULT NULL,
  `gps_horizontal_accuracy` int DEFAULT NULL,
  `gps_course` float DEFAULT NULL,
  `gps_speed` float DEFAULT NULL,
  `baro_relative_altitude` float DEFAULT NULL,
  `baro_pressure` float DEFAULT NULL,
    `env_context` varchar(100) DEFAULT NULL,
   `env_mean_bldg_floors` varchar(100) DEFAULT NULL,
   `env_activity` varchar(100) DEFAULT NULL,
   `city_name` varchar(100) DEFAULT NULL,
  `country_name` varchar(100) DEFAULT NULL,
  `magnet_x_mt` float DEFAULT NULL,
  `magnet_y_mt` float DEFAULT NULL,
  `magnet_z_mt` float DEFAULT NULL,
  `magnet_total` float DEFAULT NULL, 
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=1081 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci