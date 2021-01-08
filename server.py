import pandas as pd
from sqlalchemy import create_engine
import configparser
from flask import Flask, request
import pymysql

app = Flask(__name__)

# Read config
config = configparser.ConfigParser()
config.read('database.properties')
usr = config['Database']['user']
passwd = config['Database']['password']
tbl = config['Database']['table']
scheme = config['Database']['schema']


# Set up the database for sensory
# 1- Create the schema specified
# 2- Create the table to store sensory output
# Reference: https://pythontic.com/database/mysql/create%20database
def setup():
    global usr
    global passwd
    global scheme
    global tbl

    cursor = pymysql.cursors.DictCursor
    connection = pymysql.connect(host="127.0.0.1", user=usr, password=passwd, charset="utf8mb4", cursorclass=cursor)

    try:
        # Create a cursor object
        cursor_instance = connection.cursor()

        # SQL Statement to create a schema
        schema = "CREATE SCHEMA IF NOT EXISTS '" + scheme + "'"
        cursor_instance.execute(schema)

        # SQL statement to create the table

        table = "CREATE TABLE IF NOT EXISTS " + scheme + '.' + tbl + \
                "( `ID` int NOT NULL AUTO_INCREMENT," \
                "`indoors` int DEFAULT NULL," \
                "`created_at` varchar(100) NOT NULL," \
                "`device_id` varchar(100) NOT NULL," \
                "`floor` int DEFAULT NULL, " \
                "`rssi_strength` int DEFAULT NULL," \
                "`gps_alt` float DEFAULT NULL," \
                "`gps_longitude` float DEFAULT NULL," \
                "`gps_latitude' float DEFAULT NULL," \
                "`gps_vertical_accuracy` int DEFAULT NULL," \
                "`gps_horizontal_accuracy` int DEFAULT NULL," \
                "`gps_course` float DEFAULT NULL," \
                "`gps_speed` float DEFAULT NULL," \
                "`baro_relative_altitude` float DEFAULT NULL," \
                "`baro_pressure` float DEFAULT NULL," \
                "`env_context` varchar(100) DEFAULT NULL," \
                "`env_mean_bldg_floors` varchar(100) DEFAULT NULL," \
                "`env_activity` varchar(100) DEFAULT NULL," \
                "`city_name` varchar(100) DEFAULT NULL," \
                "`country_name` varchar(100) DEFAULT NULL," \
                "`magnet_x_mt` float DEFAULT NULL," \
                "`magnet_y_mt` float DEFAULT NULL," \
                "`magnet_z_mt` float DEFAULT NULL," \
                "`magnet_total` float DEFAULT NULL," \
                "PRIMARY KEY(`ID`)" \
                ") ENGINE = InnoDB AUTO_INCREMENT = 1081 DEFAULT CHARSET = utf8mb4"
        cursor_instance.execute(table)

    except Exception as e:
        print("Exception occurred:{}".format(e))

    finally:
        connection.close()


def get_properties():
    config = configparser.ConfigParser()
    config.read('database.properties')
    usr = config['Database']['user']
    passwd = config['Database']['password']
    tbl = config['Database']['table']
    scheme = config['Database']['schema']
    engine = create_engine("mysql+pymysql://{u}:{p}@{h}/{s}".format(u=usr, p=passwd, h="127.0.0.1:3306", s=scheme),
                           pool_pre_ping=True)
    return tbl, engine


@app.route("/predict", methods=['GET', 'POST'])
def predict():
    content = request.json
    df = pd.DataFrame.from_dict(content["data"])
    print(df)
    table, engine = get_properties()
    df.to_sql(table, engine, if_exists='append', index=False)
    print(pd.read_sql_query("select * from {t}".format(t=table), con=engine))
    return content


if __name__ == "__main__":
    app.run(host="192.168.1.208", port=3000, debug=True)
