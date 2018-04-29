import scipy as sc
#from tkinter.filedialog import askopenfilename, askdirectory
import pandas as pd
import numpy as np
from scipy import signal
from findpeaks import *
from FeaturesExtraction import *
from os import listdir
from os.path import isfile, join
#from SVC import SVC


def DataToFeatures (df):
    time = df.iloc[:,0]
    ir = df.iloc[:,1]
    time=time.as_matrix()
    ir=ir.as_matrix()
    fs=70
    nyq = fs/2
    ts=1/fs
    Nir=len(ir)
    # print(Nir)
    bpf = signal.firwin(200,[0.7/nyq ,1.9/nyq],pass_zero=False)
    ir_f=signal.filtfilt(bpf,1,ir)
    ir_f = ir_f[100 : ]
    time = time[100 : ]
    peakX = findMaxPeaks(ir_f)
    peakY = ir_f[peakX]
    valleyX = findMaxPeaks(-1*ir_f)
    ValleyY = ir_f[valleyX]
    return FeaturesExtraction(peakX,peakY,valleyX, ValleyY, ir_f,time)
