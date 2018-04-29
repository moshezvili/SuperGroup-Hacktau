
# Load libraries
import numpy as np
import matplotlib.pyplot as plt
from pickle import dump
from pickle import load
from matplotlib import pyplot
from pandas import read_csv
from pandas import set_option
# from pandas.tools.plotting import scatter_matrix
from  pandas.plotting import scatter_matrix
from sklearn.preprocessing import StandardScaler
# from sklearn.model_selection import train_test_split
from sklearn.model_selection import KFold, train_test_split
from sklearn.model_selection import cross_val_score
from sklearn.model_selection import GridSearchCV
from sklearn.metrics import classification_report
from sklearn.metrics import confusion_matrix
from sklearn.metrics import accuracy_score
from sklearn.pipeline import Pipeline
from sklearn.linear_model import LogisticRegression
from sklearn.tree import DecisionTreeClassifier
from sklearn.neighbors import KNeighborsClassifier
from sklearn.discriminant_analysis import LinearDiscriminantAnalysis
from sklearn.naive_bayes import GaussianNB
from sklearn.svm import SVC
from sklearn.ensemble import AdaBoostClassifier
from sklearn.ensemble import GradientBoostingClassifier
from sklearn.ensemble import RandomForestClassifier
from sklearn.ensemble import ExtraTreesClassifier
from sklearn.metrics import make_scorer
import  time

time_beg = time.time()
def criteria_precision(y_true, y_pred):
    res = confusion_matrix(y_true, y_pred)[1, 1]/\
          (confusion_matrix(y_true, y_pred)[1, 1]
           + confusion_matrix(y_true, y_pred)[0, 1])
    return res
# def criteria_tp(y_true, y_pred):
#     return confusion_matrix(y_true, y_pred)[1, 1]

# scoring = {'tp' : make_scorer(criteria_precision),
#            'tn' : make_scorer(criteria_tp)}

scoring = make_scorer(criteria_precision)


# Load positive dataset
#file_name = r'C:\dataBase\ml\300pos_features_transposed_with_header.csv'
# file_name = '300posWithUID_features.csv'
#df_pos = read_csv(file_name)
# print(df_pos.head(7))
# sorted_pos = df_pos.groupby('uid')['uid'].count().sort_values()


# # Summarize Data

# # Descriptive statistics
# # shape
# print(df_pos.shape)
# # types
# set_option('display.max_rows', 500)
# print(df_pos.dtypes)
# # head
# set_option('display.width', 100)
# print(df_pos.head(20))
# # descriptions, change precision to 3 places
# set_option('precision', 3)
# print(df_pos.describe())
# # class distribution
# # print(dataset.groupby(60).size())    !!!!!!!!!!
#
#
# # Data visualizations
# # histograms
# df_pos.hist()
# pyplot.show()
# # density
# df_pos.plot(kind='density', subplots=True, layout=(8,8), sharex=False, legend=False)
# pyplot.show()
# # box and whisker plots
# df_pos.boxplot()
# pyplot.show()
#
# # scatter plot matrix
# scatter_matrix(df_pos)
# pyplot.show()
#
# # correlation matrix
# fig = pyplot.figure()
# ax = fig.add_subplot(111)
# cax = ax.matshow(df_pos.corr(), vmin=-1, vmax=1, interpolation='none')
# fig.colorbar(cax)
# pyplot.show()



# Load negative dataset
#file_name = r'C:\dataBase\ml\300neg_4D_features_transposed.csv'
# df_neg = read_csv(file_name, header=None)
#df_neg = read_csv(file_name)

# # Summarize Data
#
# # Descriptive statistics
# # shape
# print(df_neg.shape)
# # types
# set_option('display.max_rows', 500)
# print(df_neg.dtypes)
# # head
# set_option('display.width', 100)
# print(df_neg.head(20))
# # descriptions, change precision to 3 places
# set_option('precision', 3)
# print(df_neg.describe())
# # class distribution
# # print(dataset.groupby(60).size())    !!!!!!!!!!
#
#
# # Data visualizations
# # histograms
# df_neg.hist()
# pyplot.show()
# # density
# df_neg.plot(kind='density', subplots=True, layout=(8,8), sharex=False, legend=False)
# pyplot.show()
# # box and whisker plots
# df_neg.boxplot()
# pyplot.show()
#
# # scatter plot matrix
# scatter_matrix(df_neg)
# pyplot.show()
#
# # correlation matrix
# fig = pyplot.figure()
# ax = fig.add_subplot(111)
# cax = ax.matshow(df_neg.corr(), vmin=-1, vmax=1, interpolation='none')
# fig.colorbar(cax)
# pyplot.show()

#df_pos['Result'] = 1
#df_neg['Result'] = 0

#dataset = df_neg.append(df_pos)
dataset = dataset.sample(frac=1).reset_index(drop=True)

# print(dataset.head(5))
print(dataset['Result'].value_counts())
#Total data

# # Summarize Data
#
# # Descriptive statistics
# # shape
# print(dataset.shape)
# # types
# set_option('display.max_rows', 500)
# print(dataset.dtypes)
# # head
# set_option('display.width', 100)
# print(dataset.head(20))
# # descriptions, change precision to 3 places
# set_option('precision', 3)
# print(dataset.describe())



# # Data visualizations
# # histograms
# dataset.hist()
# pyplot.show()
# # density
# dataset.plot(kind='density', subplots=True, layout=(8,8), sharex=False, legend=False)
# pyplot.show()
# # box and whisker plots
# bp = dataset.boxplot()
# plt.xticks(rotation=45)
#
# pyplot.show()
#
# # correlation matrix
# fig = pyplot.figure()
# ax = fig.add_subplot(111)
# cax = ax.matshow(df_pos.corr(), vmin=-1, vmax=1, interpolation='none')
# fig.colorbar(cax)
# pyplot.show()
#
#
# # scattre matrix
# sm = scatter_matrix(dataset, c=np.where(dataset.Result == 1, 'blue', 'pink'))
# #Change label rotation
# [s.xaxis.label.set_rotation(90) for s in sm.reshape(-1)]
# [s.yaxis.label.set_rotation(0) for s in sm.reshape(-1)]
# #May need to offset label when rotating to prevent overlap of figure
# [s.get_yaxis().set_label_coords(-3.0,0.5) for s in sm.reshape(-1)]
# pyplot.show()
#
# print('Hi')
# Prepare Data

# Split-out validation dataset
# array = dataset.values
X = dataset.drop('Result', axis = 1)
Y = dataset.Result
# X = array[:,0:60].astype(float)
# Y = array[:,60]

validation_size = 0.20
seed = 7
X_train, X_validation, Y_train, Y_validation = train_test_split(X, Y, test_size=validation_size, random_state=seed)


# Evaluate Algorithms

# Test options and evaluation metric
num_folds = 10
seed = 7
scoring = 'accuracy'

# # Spot Check Algorithms
# models = []
# models.append(('LR', LogisticRegression()))
# models.append(('LDA', LinearDiscriminantAnalysis()))
# models.append(('KNN', KNeighborsClassifier()))
# models.append(('CART', DecisionTreeClassifier()))
# models.append(('NB', GaussianNB()))
# models.append(('SVM', SVC()))
# results = []
# names = []
# for name, model in models:
# 	kfold = KFold(n_splits=num_folds, random_state=seed)
# 	cv_results = cross_val_score(model, X_train, Y_train, cv=kfold, scoring=scoring)
# 	results.append(cv_results)
# 	names.append(name)
# 	msg = "%s: %f (%f)" % (name, cv_results.mean(), cv_results.std())
# 	print(msg)
#
# # Compare Algorithms
# fig = pyplot.figure()
# fig.suptitle('Algorithm Comparison')
# ax = fig.add_subplot(111)
# pyplot.boxplot(results)
# ax.set_xticklabels(names)
# pyplot.show()
#
#
# # Standardize the dataset
# pipelines = []
# pipelines.append(('ScaledLR', Pipeline([('Scaler', StandardScaler()),('LR', LogisticRegression())])))
# pipelines.append(('ScaledLDA', Pipeline([('Scaler', StandardScaler()),('LDA', LinearDiscriminantAnalysis())])))
# pipelines.append(('ScaledKNN', Pipeline([('Scaler', StandardScaler()),('KNN', KNeighborsClassifier())])))
# pipelines.append(('ScaledCART', Pipeline([('Scaler', StandardScaler()),('CART', DecisionTreeClassifier())])))
# pipelines.append(('ScaledNB', Pipeline([('Scaler', StandardScaler()),('NB', GaussianNB())])))
# pipelines.append(('ScaledSVM', Pipeline([('Scaler', StandardScaler()),('SVM', SVC())])))
# results = []
# names = []
# for name, model in pipelines:
# 	kfold = KFold(n_splits=num_folds, random_state=seed)
# 	cv_results = cross_val_score(model, X_train, Y_train, cv=kfold, scoring=scoring)
# 	results.append(cv_results)
# 	names.append(name)
# 	msg = "%s: %f (%f)" % (name, cv_results.mean(), cv_results.std())
# 	print(msg)
#
# # Compare Algorithms
# fig = pyplot.figure()
# fig.suptitle('Scaled Algorithm Comparison')
# ax = fig.add_subplot(111)
# pyplot.boxplot(results)
# ax.set_xticklabels(names)
# pyplot.show()
#
#
# # Tune scaled KNN
# scaler = StandardScaler().fit(X_train)
# rescaledX = scaler.transform(X_train)
# neighbors = [1,3,5,7,9,11,13,15,17,19,21]
# param_grid = dict(n_neighbors=neighbors)
# model = KNeighborsClassifier()
# kfold = KFold(n_splits=num_folds, random_state=seed)
# grid = GridSearchCV(estimator=model, param_grid=param_grid, scoring=scoring, cv=kfold)
# grid_result = grid.fit(rescaledX, Y_train)
# print("Best: %f using %s" % (grid_result.best_score_, grid_result.best_params_))
# means = grid_result.cv_results_['mean_test_score']
# stds = grid_result.cv_results_['std_test_score']
# params = grid_result.cv_results_['params']
# for mean, stdev, param in zip(means, stds, params):
#     print("%f (%f) with: %r" % (mean, stdev, param))
#
#
# Tune scaled SVM
scaler = StandardScaler().fit(X_train)
rescaledX = scaler.transform(X_train)
c_values = [0.1, 0.3, 0.5, 0.7, 0.9, 1.0, 1.3, 1.5, 1.7, 2.0]
kernel_values = ['linear', 'poly', 'rbf', 'sigmoid']
param_grid = dict(C=c_values, kernel=kernel_values)
model = SVC()
kfold = KFold(n_splits=num_folds, random_state=seed)
grid = GridSearchCV(estimator=model, param_grid=param_grid, scoring=scoring, cv=kfold)
grid_result = grid.fit(rescaledX, Y_train)
print("Best: %f using %s" % (grid_result.best_score_, grid_result.best_params_))
means = grid_result.cv_results_['mean_test_score']
stds = grid_result.cv_results_['std_test_score']
params = grid_result.cv_results_['params']
for mean, stdev, param in zip(means, stds, params):
    print("%f (%f) with: %r" % (mean, stdev, param))
#
#
#
#
# # ensembles
# ensembles = []
# ensembles.append(('AB', AdaBoostClassifier()))
# ensembles.append(('GBM', GradientBoostingClassifier()))
# ensembles.append(('RF', RandomForestClassifier()))
# ensembles.append(('ET', ExtraTreesClassifier()))
# results = []
# names = []
# for name, model in ensembles:
# 	kfold = KFold(n_splits=num_folds, random_state=seed)
# 	cv_results = cross_val_score(model, X_train, Y_train, cv=kfold, scoring=scoring)
# 	results.append(cv_results)
# 	names.append(name)
# 	msg = "%s: %f (%f)" % (name, cv_results.mean(), cv_results.std())
# 	print(msg)
#
# # Compare Algorithms
# fig = pyplot.figure()
# fig.suptitle('Ensemble Algorithm Comparison')
# ax = fig.add_subplot(111)
# pyplot.boxplot(results)
# ax.set_xticklabels(names)
# pyplot.show()
#
#
#
# # Finalize Model
#
# # prepare the model
# scaler = StandardScaler().fit(X_train)
# rescaledX = scaler.transform(X_train)
# model = SVC(C=2.0,kernel='rbf')
# model.fit(rescaledX, Y_train)
# # estimate accuracy on validation dataset
# rescaledValidationX = scaler.transform(X_validation)
# predictions = model.predict(rescaledValidationX)
# print(accuracy_score(Y_validation, predictions))
# print(confusion_matrix(Y_validation, predictions))
# print(classification_report(Y_validation, predictions))


# # prepare the model
scaler = StandardScaler().fit(X_train)
rescaledX = scaler.transform(X_train)
# model = SVC(C=2.0,kernel='rbf')
model = KNeighborsClassifier(n_neighbors=5)
model.fit(rescaledX, Y_train)
# save the model to disk
filename = 'finalized_model.sav'
dump(model, open(filename, 'wb'))
# estimate accuracy on validation dataset
rescaledValidationX = scaler.transform(X_validation)
predictions = model.predict(rescaledValidationX)
print(accuracy_score(Y_validation, predictions))
print(confusion_matrix(Y_validation, predictions))
print(classification_report(Y_validation, predictions))

# # load the model from disk
# loaded_model = load(open(filename, 'rb'))
# result = loaded_model.score(rescaledValidationX, Y_validation)
#
# print(accuracy_score(Y_validation, predictions))
# print(confusion_matrix(Y_validation, predictions))
# print(classification_report(Y_validation, predictions))


#Tesing
# file_name = r'C:\dataBase\ml\300pos_validation.csv'
# # file_name = '300posWithUID_features.csv'
# df_test = read_csv(file_name)
# df_test['Result'] = 1
# X_test = df_test.drop('Result', axis = 1)
# Y_test = df_test.Result
#
# rescaled_testX = scaler.transform(X_test)
# test_predictions = model.predict(rescaled_testX)
# print(accuracy_score(Y_test, test_predictions))
# print(confusion_matrix(Y_test, test_predictions))
# print(classification_report(Y_test, test_predictions))

time_end = time.time()
print(time_end - time_beg)
