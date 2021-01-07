import pandas as pd
from sqlalchemy import create_engine
import configparser
from flask import Flask, request
app = Flask(__name__)


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
