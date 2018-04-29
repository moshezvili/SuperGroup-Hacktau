from flask import Flask, request, Response, json, render_template
import pickle
import numpy as np
import pandas as pd
import io
import csv
import urllib2
import time
from PPG import *

app = Flask(__name__)

@app.route('/',methods=['POST','GET'])
def home():
        pkl_file = open('logmodel.pkl', 'rb')
        logmodel = pickle.load(pkl_file)
        new_vector=np.zeros((6, 54))
        prediction = logmodel.predict(new_vector)
        return render_template('home.html',prediction=prediction)

@app.route('/hello',methods=['GET'])
def api_hello():
    try:
        if 'name' in request.args:
            #download from s3
            url = 'https://s3-us-west-2.amazonaws.com/alcotest/data/data.csv'
            response = urllib2.urlopen(url)
            html = response.read()
            with open('temp.csv', 'wb') as f:
                f.write(html)
            f.close()
            df = pd.read_csv('temp.csv')
            vector = DataToFeatures(df)
            model = pickle.load(open('model.p','rb'))
            ans = model.predict(np.array(vector).reshape(1,3))
            ans = ans[0]
            
            if ans:
                print('true')
                return 'true'
            else:
                print('false')
                return 'false'
        else:
            return 'false'
    except Exception as x:
        print(x)
        return 'false'
@app.route('/csvport', methods=["POST"])
def transform_view():
    f = request.files['data_file']
    if not f:
        return "No file added"

    stream = io.StringIO(f.stream.read().decode("UTF8"), newline=None)
    csv_input = csv.reader(stream)
    print(csv_input)
    for row in csv_input:
        print(row)
    stream.seek(0)
    data = {
        'success'  : 'success'
    }
    js = json.dumps(data)
    resp = Response(js, status=200, mimetype='application/json')
    return resp

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=80)