from __future__ import print_function
from sklearn.ensemble import RandomForestClassifier
import sys
import util

# Train data: 2017-09-03 - 2017-11-11

# def train_mlp(X, Y):
#     mlp = MLPClassifier(hidden_layer_sizes=(10,), activation="identity", solver='sgd', max_iter=(len(X) * 10))
#     clf = mlp.fit(X, Y)
#     print("Training results:", get_scores(clf, X, Y))
#     return clf


def train_rand_forest(X, Y):
    rfc = RandomForestClassifier(n_estimators=30)
    clf = rfc.fit(X, Y)
    print("Training results:", get_scores(clf, X, Y))
    return clf


#def train_ridge(X, Y):
#    ridge = RidgeClassifierCV([.01, .1, 1, 10, 100], True)
#    clf = ridge.fit(X, Y)
#    print("Training results:", get_scores(clf, X, Y))
#    return clf

def classifier_per_user(trainer, X=None, Y=None, user_data=None):
    classifiers = {}
    if user_data == None:
        user_data = util.split_data_by_user(X, Y)
    for user, data in user_data.items():  # user_data.iteritems()
        print("Training user: ", user, file=sys.stderr)
        X_user = data[0]
        Y_user = data[1]
        X_user = util.format_data(X_user)
        clf = trainer(X_user, Y_user)
        classifiers[user] = clf
    #if X == None or Y == None:
    #    X, Y = util.combine_user_data(user_data)
    #X = util.format_data(X)
    #clf = trainer(X, Y)
    #classifiers['all'] = clf
    return classifiers


def pred_conf_accuracy(classifiers, X=None, Y_true=None, user_data=None):
    if user_data == None:
        user_data = util.split_data_by_user(X, Y_true)
    correct_probs = []
    incorrect_probs = []
    for user, data in user_data.items():  # user_data.iteritems()
        if data[0] != []:
            X_user = util.format_data(data[0])
            #print(len(X_user))        
            if user in classifiers:
                clf_user = classifiers[user]
                pred_user = clf_user.predict(X_user)
                pred_probs = clf_user.predict_proba(X_user)
                #clf_all = classifiers['all']
                #if np.count_nonzero(pred_user) < 3:
                #    pred_all = clf_all.predict(X_user)
                #    Y_pred = np.logical_or(pred_user, pred_all)
                #else:
                Y_pred = pred_user
            else:
                clf = classifiers['all']
                Y_pred = clf.predict(X_user)
                pred_probs = clf.predict_proba(X_user)
            #if len(X_user) > 1:
            #    print(user)
            #    return pred_probs
            #print(len(Y_pred), len(data[1]), len(pred_probs))
            indices = range(len(X_user))
            #print(len(Y_pred), len(data[1]), len(indices))
            for yp, yt, i in zip(Y_pred, data[1], indices):
                #print(len(yp), len(yt), len(pred_probs))
                for pred, actual, prob in zip(yp, yt, pred_probs):
                    #print(pred, actual)
                    if pred == 1.0 and actual == 1.0:
                        #print(prob[i])
                        correct_probs.append(prob[i][-1])
                    elif pred != 0.0 or actual != 0.0:
                        #print(prob[i])
                        if pred == 1.0:
                            incorrect_probs.append(prob[i][-1])
                        else:
                            incorrect_probs.append(prob[i][0])
    return correct_probs, incorrect_probs

                    

def score_user_classifiers(classifiers, X=None, Y_true=None, user_data=None):
    if user_data == None:
        user_data = util.split_data_by_user(X, Y_true)
    true_positives = []
    selected_routes = []
    actual_routes = []
    users = []
    for user, data in user_data.items():  # user_data.iteritems()
        if data[0] != []:
            X_user = util.format_data(data[0])
            if user in classifiers:
                clf_user = classifiers[user]
                pred_user = clf_user.predict(X_user)
                #clf_all = classifiers['all']
                #if np.count_nonzero(pred_user) < 3:
                #    pred_all = clf_all.predict(X_user)
                #    Y_pred = np.logical_or(pred_user, pred_all)
                #else:
                Y_pred = pred_user
            else:
                clf = classifiers['all']
                Y_pred = clf.predict(X_user)
            true, selected, actual = util.compare_labels(Y_pred, data[1])
            users.append(user)
            true_positives.append(true)
            selected_routes.append(selected)
            actual_routes.append(actual)
    precision = sum(true_positives) / sum(selected_routes)
    recall = sum(true_positives) / sum(actual_routes)
    f1 = 2 * (precision * recall) / (precision + recall)
    print("Selected routes: {}. Precision: {}, Recall: {}, F1: {}".format(sum(selected_routes), precision, recall, f1))
    return users, true_positives, selected_routes, actual_routes


def get_scores(clf, X, Y_true):
    Y_pred = clf.predict(X)
    #print(Y_pred[0])
    true_positives, selected_routes, actual_routes = util.compare_labels(Y_pred, Y_true)
    precision = true_positives / selected_routes
    recall = true_positives / actual_routes
    f1 = 2 * (precision * recall) / (precision + recall)
    return selected_routes, precision, recall, f1


#Convenience function
def train_and_validate(start_date, end_date, inout=False):
    if inout:
        X, Y = util.get_data_inout(start_date, end_date)
    else:
        X, Y, rd = util.get_data(['PAAC', 'MTA'], start_date, end_date)
    user_data = util.split_data_by_user(X, Y)
    ud_train, ud_valid = util.split_user_data(user_data, .1)
    trainer = train_rand_forest
    clfs = classifier_per_user(trainer, user_data=ud_train)
    score_user_classifiers(clfs, user_data=ud_valid)
    return clfs, ud_valid




# def main():
#     train_and_validate('2017-09-01', '2019-08-31')
#     # train_and_validate('2018-07-01', '2019-08-31')
#
#
# main()