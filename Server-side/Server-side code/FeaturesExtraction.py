import numpy as np
def FeaturesExtraction(peakX, peakY,valleyX, valleyY,signal,time):
    j = 0
    while valleyX[j] < peakX[0]:
        j+=1
    valleyX = valleyX [j :]
    if ( len(valleyX) > len(peakX)):
        valleyX = valleyX[: len(peakX)]
    else:
        peakX = peakX[: len(valleyX)]
    PeaksVector = np.array(list(peakX))
    valleyVector = np.array(list(valleyX))
    TimeDifferent =  valleyVector - PeaksVector
    median = np.round(np.median (TimeDifferent))
    medianPlus2std = np.round(median + 2*np.std(TimeDifferent))

    p=len(peakX)
    teta = []
    Xgag = []
    Ygag = []
    k=0
    for i in range(p):
        if(median<(valleyX[i]-peakX[i]) < medianPlus2std):
            X = time[peakX[i]:(peakX[i]+ int(median))]
            Y = signal [peakX[i]:(peakX[i] + int(median))]
            #p1 = (peakX[i] + valleyX[i])/2;
            #p2 = (peakY[i] + valleyY[i])/2;
            #b1 = ((peakX[i] - p1)*(peakY[i] - p2)+(valleyX[i]-p1)*(valleyY[i]-p2))/((peakX[i]-p1)^2 +(valleyX[i]-p1)^2)
            # b0 = p2-b1*p1
            teta.append(np.arctan((peakY[i] - valleyY[i])/(peakX[i] - valleyX[i])))
            newteta = np.arctan((peakY[i] - valleyY[i])/(peakX[i] - valleyX[i]))
            Xgag.append(X*np.cos(newteta) + Y*np.sin(newteta))
            Ygag.append(-X*np.cos(newteta)+Y*np.cos(newteta))

    TetaFeature = np.average(teta)
    Omega = np.average(Ygag , axis = 0)
    Nu = min(Omega)
    diff1 = np.gradient(Omega)
    diff2 = np.gradient(Omega,2)
    a=abs(diff2)
    k = (abs(diff2))/np.power((1 + np.power(diff1,2)),3/2)
    Kfeature = np.sum(k)
    return [TetaFeature,Nu,Kfeature]



