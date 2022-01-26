
from __future__ import print_function
from flask import Flask, request, jsonify
from datetime import datetime
from wtforms import Form, DecimalField, StringField, DateTimeField, validators
import threading
import sys
import sci_learn_algs as sla
import util


modelThread = threading.Thread()
route_dict = {}
route_models = {}
user_datasizes_route = {}
inout_models = {}
user_datasizes_inout = {}


class PredictionArgs(Form):
    user_lat = DecimalField('user_lat', [validators.DataRequired()])
    user_lon = DecimalField('user_lon', [validators.DataRequired()])
    device_id = StringField('device_id', [validators.DataRequired()])
    stamp = DateTimeField('stamp', [validators.DataRequired()], default=datetime.now, format='%Y-%m-%d %H:%M:%S.%f')


def get_inputs(request):
    if request.method == 'POST':
        args = PredictionArgs(request.form)
    else:
        args = PredictionArgs(request.args)
    if args.validate():
        user_lat = float(args.user_lat.data)
        user_lon = float(args.user_lon.data)
        device_id = str(args.device_id.data)
        stamp = args.stamp.data
        time = util.time_in_day(stamp)
        x = [user_lat, user_lon, time]
        return device_id, x
    else:
        return None, None


# Elastic Beanstalk initaliztion
def create_app():
    application = Flask(__name__)
    application.debug=True

    def start_training_schedule():
        modelThread = threading.Timer(1, scheduled_training, ())
        modelThread.start()        

    def scheduled_training():        
        print("Initiating training", file=sys.stderr)
        global modelThread
        now = datetime.now()
        train_models(now.strftime('%Y-%m-%d'))
        time_til_retrain = ((24 - now.hour + 1) * 60 * 60) + ((60 - now.minute - 1) * 60) + (60 - now.second)
        modelThread = threading.Timer(time_til_retrain, scheduled_training, ())
        modelThread.start()        


    def train_models(current_date):        
        global route_dict
        global route_models
        global user_datasizes_route
        global inout_models
        global user_datasizes_inout        

        X_route, Y_route, route_dict = util.get_data(['PAAC', 'MTA'], '2017-09-01', current_date, filter_cond=True)
        user_data_route = util.split_data_by_user(X_route, Y_route)        
        user_datasizes_route = {user: len(user_data_route[user][0]) for user in user_data_route.keys()}
        #print(user_datasizes_route)        
        trainer = sla.train_rand_forest
        route_models = sla.classifier_per_user(trainer, user_data=user_data_route)
        X_inout, Y_inout = util.get_data_inout('2017-09-01', current_date, filter_cond=True)
        user_data_inout = util.split_data_by_user(X_inout, Y_inout)
        user_datasizes_inout = {user: len(user_data_inout[user][0]) for user in user_data_inout.keys()}
        inout_models = sla.classifier_per_user(trainer, user_data=user_data_inout)

    start_training_schedule()
    return application


application = create_app()


@application.route('/', methods=['GET', 'POST'])
@application.route('/routes', methods=['GET', 'POST'])
def index():
    global route_models
    global user_datasizes_route
    global route_dict

    device_id, x = get_inputs(request)
    if device_id != None and x != None:
        # 80 is number of samples, not number of logs
        if device_id in route_models and user_datasizes_route[device_id] >= 80:
            clf = route_models[device_id]
            predictions = clf.predict([x]).tolist()[0]
            #print(predictions, file=sys.stderr)        
            routes = [route_dict[idx] for idx, value in enumerate(predictions) if value == 1.0]
            return jsonify({'data': routes})
        else:
            return jsonify({'data': []})
    else:
        return "Error getting inputs"


@application.route('/inout', methods=['GET', 'POST'])
def inout_prediction():
    global inout_models
    global user_datasizes_inout
    device_id, x = get_inputs(request)
    #print(device_id)
    if device_id != None and x != None:
        # 80 is number of samples, not number of logs
        if device_id in inout_models and user_datasizes_inout[device_id] >= 80:
            clf = inout_models[device_id]
            prediction = clf.predict([x]).tolist()[0]
            if prediction[0] and prediction[1]:
                return jsonify({'data': ""})
            elif prediction[0]:
                return jsonify({'data': "in_filter"})
            elif prediction[1]:
                return jsonify({'data': "out_filter"})
            else:
                return jsonify({'data': ""})
        else:
            return jsonify({'data': ""})
    else:
        return "Error getting inputs"


# Convenience function for debugging
@application.route('/datasizes', methods=['GET', 'POST'])
def user_datasizes():
    return jsonify({'routes': user_datasizes_route, 'inout': user_datasizes_inout})


# run the app.
if __name__ == "__main__":
    # Setting debug to True enables debug output. This line should be
    # removed before deploying a production app.
    application.debug = True
    application.run()

