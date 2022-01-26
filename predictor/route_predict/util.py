import os
import psycopg2
import requests
import csv
from io import StringIO
import numpy as np
from datetime import timedelta, datetime
import boto3
import botocore

routes_url = '<YOUR_URL>/oba/api/where/routes-for-location.json?key=TEST&radius=500&maxCount=1000'
global_db_params = {'host': os.environ['RDS_HOSTNAME'], 'db': os.environ['RDS_DB_NAME'], 'user': os.environ['RDS_USERNAME'], 'password': os.environ['RDS_PASSWORD']}


def nearby_routes(lat, lon):
    r = requests.get(routes_url + "&lat=" + str(lat) + "&lon=" + str(lon))
    res = r.json()
    data = res['data']['list']
    routes = []

    for entry in data:
        routes.append(entry['shortName'])
    return routes
    return []


def execute_query(query, db_params=None):
    global global_db_params
    if not db_params:
        db_params = global_db_params
    #print "Database params", db_params
    connection_string = "host=" + db_params['host'] + " dbname=" + db_params['db'] + " user=" + db_params['user'] + " password=" + db_params['password']
    try:
        connection = psycopg2.connect(connection_string)
        connection.autocommit = True
        cur = connection.cursor()
    except Exception as e:
        print(e)
    try:
        cur.execute(query)
    except Exception as e:
        print(e)
    try:
        header_map = {desc[0]: i for i, desc in enumerate(cur.description)}
        return cur.fetchall(), header_map
    except Exception as e:
        print(e)


def num_logs(device_id, start_time, end_time):
    query = "SELECT COUNT(*) FROM user_data.route WHERE event = 'write' AND device_id = '" + device_id + "' AND stamp >= '" + start_time + "' AND stamp < '" + end_time + "'"
    rows = execute_query(query)
    return int(rows[0][0])


#The order of agencies and route_files must match
def get_all_routes(agencies, route_files=None):
    all_routes = []
    if not route_files:
        for agency in agencies:
            bucket, path = getGTFSPath(agency)
            routes_string = get_route_file_s3(bucket, path)
            fd = StringIO(routes_string.decode('utf-8'))
            reader = csv.reader(fd)
            all_routes.extend(read_routes(reader, agency))
    else:
        for agency, path in zip(agencies, route_files):
            all_routes.extend(get_routes(agency, path))
    all_routes.sort()
    return all_routes


def get_routes(agency_id, path):
    with open(path) as f:
        reader = csv.reader(f)
        return read_routes(reader, agency_id)


def read_routes(reader, agency_id):
    routes = []
    header = next(reader) #reader.next()
    header = [col.encode('ascii', 'ignore') for col in header]  # [col.decode('utf8').encode('ascii', 'ignore') for col in header]
    route_name_index = header.index(b'route_short_name')  # header.index('route_short_name')
    for row in reader:
        route_name = row[route_name_index]
        route_name = agency_id + '_' + route_name
        if route_name not in routes:
            routes.append(route_name)
    return routes


def getGTFSPath(agency_id):
    dynamodb = boto3.client('dynamodb', region_name='us-east-1')
    print(boto3.resource('dynamodb', region_name='us-east-1').get_available_subresources())
    print(dynamodb.list_tables())
    current_date = datetime.today().strftime('%Y%m%d')
    response = dynamodb.query(TableName='GtfsVersions',
                              KeyConditionExpression='agency_id = :v_agency_id and end_date >= :v_end_date',
                              ExpressionAttributeValues={':v_agency_id': {'S': agency_id.upper()},
                                                         ':v_end_date': {'S': current_date}})
    items = response['Items']
    for item in items:
        if item['start_date']['S'] <= current_date:
            return str(item['s3_bucket']['S']), str(item['s3_folder']['S'])


def get_route_file_s3(bucket_name, path):
    s3 = boto3.resource('s3')
    try:
        route_object = s3.Object(bucket_name, path + 'routes.txt')
    except botocore.exceptions.ClientError as e:
        print(e)
    response = route_object.get(ResponseContentType='json')
    return response['Body'].read()


def indices_to_array(i, size):
    a = np.zeros(size).astype(int)
    a[i] = 1
    return a


def routes_to_array(routes, route_dict):
    route_indices = []
    for route in routes:
        if route in route_dict:
            route_indices.append(route_dict[route])
        else:
            print(route, "not found in route_dict")
    # In case no routes in sample exist in the current GTFS
    if route_indices:
        route_array = indices_to_array(route_indices, len(route_dict)).tolist()
        return route_array
    else:
        return []


def add_agency(routes, agency):
    return [agency + '_' + route for route in routes]


def user_route_freqs(device_id, start_time, end_time):
    query = "SELECT agency_id, route_short_name, COUNT(*) AS count FROM user_data.route WHERE event = 'write' AND device_id = '" + device_id + "' AND stamp >= '" + start_time + "' AND stamp < '" + end_time + "' GROUP BY route_short_name, agency_id ORDER BY route_short_name DESC;"
    filters = execute_query(query)
    route_freqs = {f[0] + '_' + f[1]: int(f[2]) for f in filters}
    routes = route_freqs.keys()
    return route_freqs, routes


def construct_sample(logs, i, rt_fields):
    sample_id = i
    device_id = logs[i][rt_fields['device_id']]
    routes = [logs[i][rt_fields['route_short_name']]]
    lat = logs[i][rt_fields['user_lat']]
    lon = logs[i][rt_fields['user_lon']]
    stamp = logs[i][rt_fields['stamp']]
    agency = logs[i][rt_fields['agency_id']]
    #print logs[i][rt_fields['device_id']], [logs[i][rt_fields['route_name']]], logs[i][rt_fields['lat']], logs[i][rt_fields['lon']], logs[i][rt_fields['stamp']]
    td = timedelta(seconds=30)
    prev_stamp = stamp
    while (i < (len(logs) - 1) and (logs[i+1][rt_fields['stamp']] - prev_stamp) < td and device_id == logs[i+1][rt_fields['device_id']]):
        i += 1
        prev_stamp = logs[i][rt_fields['stamp']]
        routes.append(logs[i][rt_fields['route_short_name']])
        #print logs[i][rt_fields['device_id']], [logs[i][rt_fields['route_name']]], logs[i][rt_fields['lat']], logs[i][rt_fields['lon']], logs[i][rt_fields['stamp']]
    sample = {'routes': set(routes), 'device_id': device_id, 'lat': lat, 'lon': lon, 'stamp': stamp, 'agency': agency, 'id': sample_id}
    #print "---- finished sample ----"
    return sample, i


def get_samples(start_time, end_time, filter_cond=False):
    if filter_cond:
        cond_filter_string =  "AND device_id IN (SELECT device_id FROM exp.user_condition WHERE experiment_id = 1 AND (condition_num = 1 OR condition_num = 2)) "
        query = "SELECT * FROM user_data.route WHERE event='write' AND stamp >= '" + start_time + "' AND stamp < '" + end_time + "' " + cond_filter_string + "ORDER BY device_id, stamp;"
    else:
        query = "SELECT * FROM user_data.route WHERE event='write' AND stamp >= '" + start_time + "' AND stamp < '" + end_time + "' ORDER BY device_id, stamp;"
    rows, rt_fields = execute_query(query)
    samples = []
    i = 0
    while i < len(rows):
        sample, i = construct_sample(rows, i, rt_fields)
        samples.append(sample)
        i += 1
    return samples


def get_data(agencies, start_time, end_time, filter_cond=False, route_files=None, routes=None, with_avail=None, with_freq=None):
    samples = get_samples(start_time, end_time, filter_cond=filter_cond)
    if not routes:
        if not route_files:
            routes = get_all_routes(agencies)
        else:
            routes = get_all_routes(agencies, route_files=route_files)

    route_dict = {rt: idx for idx, rt in enumerate(routes)}
    X = []
    Y = []
    for sample in samples:
            row = [sample['id'], sample['device_id'], sample['lat'], sample['lon'], sample['stamp']]
            if with_avail:
                avail_routes = nearby_routes(sample['lat'], sample['lon'])
                avail_routes = add_agency(avail_routes, sample['agency'])
                avail_routes_values = routes_to_array(avail_routes, route_dict).tolist()
                row.extend(avail_routes_values)
            if with_freq:
                route_freqs, routes = user_route_freqs(sample['device_id'], start_time, end_time)
                route_freq_values = routes_to_array(routes, route_dict).tolist()
                for route in route_freqs.keys():
                    if route in route_dict:
                        index = route_dict[route]
                        route_freq_values[index] *= route_freqs[route]
                    else:
                        print(route, "not found in route_dict for user: ", sample['device_id'])
                row.extend(route_freq_values)
            sample_routes = add_agency(sample['routes'], sample['agency'])
            route_values = routes_to_array(sample_routes, route_dict)
            if route_values:
                X.append(row)
                Y.append(route_values)
    route_dict = dict({rt: idx for idx, rt in enumerate(routes)}) # , **{idx: rt for idx, rt in enumerate(routes)}
    return X, Y, route_dict


def get_data_inout(start_time, end_time, filter_cond=False):
    if filter_cond:
        cond_filter_string =  "AND device_id IN (SELECT device_id FROM exp.user_condition WHERE experiment_id = 1 AND (condition_num = 1 OR condition_num = 2)) "
        query = "SELECT * FROM log.button WHERE event = 'write' AND (button_type = 'in_filter' OR button_type = 'out_filter') AND stamp >= '" + start_time + "' AND stamp < '" + end_time + "' " + cond_filter_string + "ORDER BY device_id, stamp;"
    else:
        query = "SELECT * FROM log.button WHERE event = 'write' AND (button_type = 'in_filter' OR button_type = 'out_filter') AND stamp >= '" + start_time + "' AND stamp < '" + end_time + "' ORDER BY device_id, stamp;"
    rows, io_fields = execute_query(query)
    X = []
    Y = []
    for i, row in enumerate(rows):
        device_id = row[io_fields['device_id']]
        lat = row[io_fields['user_lat']]
        lon = row[io_fields['user_lon']]
        stamp = row[io_fields['stamp']]
        if row[io_fields['button_type']] == 'in_filter':
            Y.append([1, 0])
        else:
            Y.append([0, 1])
        X.append([i, device_id, lat, lon, stamp])
    return X, Y


# Assumes data is ordered
def split_data(X, Y, valid_percent):
    valid_size = int(np.floor(len(X) * valid_percent))
    #Split this way so single entries are in train not valid
    Xv = X[len(X) - valid_size:]
    Yv = Y[len(X) - valid_size:]
    Xt = X[:len(X) - valid_size]
    Yt = Y[:len(X) - valid_size]
    return Xt, Yt, Xv, Yv


#Seperate data into different X and Y for each user
def split_data_by_user(X, Y):
    user_data = {}
    for sample, labels in zip(X, Y):
        if sample[1] not in user_data:
            if sample[1] == 'null':
                continue
            user_data[sample[1]] = [[], []]
        user_data[sample[1]][0].append(sample)
        user_data[sample[1]][1].append(labels)
    return user_data


#Split data for each user into train and valid
def split_user_data(user_data, valid_percent):
    user_data_train = {}
    user_data_valid = {}
    for user, data in user_data.items():  # user_data.iteritems()
        Xt, Yt, Xv, Yv = split_data(data[0], data[1], valid_percent)
        user_data_train[user] = [Xt, Yt]
        user_data_valid[user] = [Xv, Yv]
    return user_data_train, user_data_valid


def combine_user_data(user_data):
    X = []
    Y = []
    for user, data in user_data.iteritems():
        X.extend(data[0])
        Y.extend(data[1])
    return X, Y


def write_data(route_files, start_time, end_time, out_file, with_avail=None, with_freq=None):
    routes = get_all_routes(route_files)
    with open(out_file, 'wb') as res_file:
        writer = csv.writer(res_file)
        header = ['sample_id', 'device_id','lat','lon','stamp']
        if with_avail:
            route_avail_header = [route + '_avail' for route in routes]
            header.extend(route_avail_header)
        if with_freq:
            route_freq_header = [route + '_freq' for route in routes]
            header.extend(route_freq_header)
        header.extend(routes)
        #print header
        writer.writerow(header)
        X, Y = get_data(route_files, start_time, end_time, routes=routes, with_avail=with_avail, with_freq=with_freq)
        for x, y in zip(X, Y):
            row = x + y
            writer.writerow(row)


def read_csv_data(path, target_start_col):
    with open(path, 'r') as in_file:
        data_reader = csv.reader(in_file)
        header = data_reader.next()
        samples = []
        for row in data_reader:
            x = row[:target_start_col - 1]
            target = row[target_start_col - 1:]
            samples.append([x, target])
        return samples


def filter_users(X, Y, users):
    new_X = []
    new_Y = []
    for x, y in zip(X, Y):
        if x[1] in users:
            new_X.append(x)
            new_Y.append(y)
    return new_X, new_Y


def time_in_day(time):
    if type(time) == str:
        time = datetime.strptime(time, '%Y-%m-%d %H:%M:%S.%f')
    return time.hour * 60 * 60 + time.minute * 60 + time.second


def is_weekday(time):
    if type(time) == str:
        time = datetime.strptime(time, '%Y-%m-%d %H:%M:%S.%f')
    dow = time.weekday()
    if dow > 4:
        return 0
    else:
        return 1


def weekday_vector(time):
    if type(time) == str:
        time = datetime.strptime(time, '%Y-%m-%d %H:%M:%S.%f')
    return list(indices_to_array(time.weekday(), 7))


def format_data(X):
    X = [x[2:] for x in X]
    for x in X:
        #dow = is_weekday(x[-1])
        #dow = weekday_vector(x[-1])
        x[-1] = time_in_day(x[-1])
        #x.append(dow)
        #x.extend(dow)
    return X


def compare_labels(Y_pred, Y_true):
    true_positives = 0.0
    selected_routes = 0.0
    actual_routes = 0.0
    for yp, yt in zip(Y_pred, Y_true):
        #print len(yp), len(yt)
        for pred, actual in zip(yp, yt):
            true_positives += pred * actual
            selected_routes += pred
            actual_routes += actual
    return true_positives, selected_routes, actual_routes


def remove_days(num_days, user_data):
    new_user_data = {}
    for user, data in user_data.iteritems():
        data = zip(data[0], data[1])
        def getKey(entry):
            return entry[0][-1]
        data = sorted(data, key=getKey)
        start_time = data[0][0][-1]
        td = timedelta(days=num_days)
        i = 0
        while i < len(data) and data[i][0][-1] - start_time < td:
            i += 1
        # undo zip with reduced data
        new_data = zip(*data[i:])
        if new_data != []:
            new_user_data[user] = [list(new_data[0]), list(new_data[1])]
    return new_user_data
