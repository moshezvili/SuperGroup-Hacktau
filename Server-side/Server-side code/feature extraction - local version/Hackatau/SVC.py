
from sklearn.model_selection import KFold, train_test_split
from sklearn.preprocessing import StandardScaler
from sklearn.svm import SVC
from sklearn.model_selection import GridSearchCV
from sklearn.neighbors import KNeighborsClassifier
from pickle import dump
from sklearn.metrics import classification_report
from sklearn.metrics import confusion_matrix
from sklearn.metrics import accuracy_score

def SVC(dataset):
    dataset = dataset.sample(frac=1).reset_index(drop=True)
    print(dataset['Result'].value_counts())
    X = dataset.drop('Result', axis=1)
    Y = dataset.Result
    validation_size = 0.10
    seed = 7
    X_train, X_validation, Y_train, Y_validation = train_test_split(X, Y, test_size=validation_size, random_state=seed)
    num_folds = 3
    seed = 7
    scoring = 'accuracy'
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
    scaler = StandardScaler().fit(X_train)
    rescaledX = scaler.transform(X_train)
    #  model = SVC(C=2.0,kernel='rbf')
    # # model = KNeighborsClassifier(n_neighbors=5)
    # model.fit(rescaledX, Y_train)
    # # save the model to disk
    # filename = 'finalized_model.sav'
    # dump(model, open(filename, 'wb'))
    # # estimate accuracy on validation dataset
    # rescaledValidationX = scaler.transform(X_validation)
    # predictions = model.predict(rescaledValidationX)
    # print(accuracy_score(Y_validation, predictions))
    # print(confusion_matrix(Y_validation, predictions))
    # print(classification_report(Y_validation, predictions))
