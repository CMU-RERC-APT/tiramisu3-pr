import sys
import numpy as np
import torch
import torch.nn as pynn
import torch.optim as optim
from torch.autograd import Variable

import nns
import util

def train(X, Y):

    X = np.array(X)
    Y = np.array(Y)

    net = nns.BasicNN(len(X[0]), len(Y[0]), len(Y[0]))

    optimizer = optim.Adam(net.parameters())

    #criterion = pynn.MultiLabelMarginLoss()
    #criterion = pynn.MultiLabelSoftMarginLoss()
    criterion = pynn.BCEWithLogitsLoss()

    try:
        epochs = 20
        for e in range(epochs):
            cum_loss = torch.zeros(1)
            for x, y in zip(X, Y):
                ipt = format_input(x)
                #y = np.nonzero(y)[0]
                labels = Variable(torch.from_numpy(y).float()).view(1, -1)
                #def closure():
                optimizer.zero_grad()
                output = net(ipt)
                loss = criterion(output, labels)
                cum_loss = torch.add(cum_loss, loss.data)
                loss.backward()
                    #return loss
                optimizer.step()
                #optimizer.step(closure)
            print torch.div(cum_loss, len(X))
    except:
        print "Error:", sys.exc_info()
    finally:
        return net

def score_model(net, X, Y_true):

    X = np.array(X)
    Y_true = np.array(Y_true)
    
    true_positives = 0.0
    selected_routes = 0.0
    actual_routes = 0.0
    sig = pynn.Sigmoid()

    for x, y_true in zip(X, Y_true):
        ipt = format_input(x)
        out = net(ipt)
        out = sig(out)
        y_pred = np.around(out.data.numpy())[0]
        #print y_pred, y_true
        for pred, actual in zip(y_pred, y_true):
            true_positives += pred * actual
            selected_routes += pred
            actual_routes += actual

    precision = true_positives / selected_routes
    recall = true_positives / actual_routes
    f1 = 2 * (precision * recall) / (precision + recall)

    return precision, recall, f1
        
    
def format_input(itp):

    return Variable(torch.from_numpy(itp).float()).unsqueeze(0)
