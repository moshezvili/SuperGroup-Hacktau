import numpy as np
def findMaxPeaks(v, m=10):
    '''
    :param: subAreaInstance is an instance of subArea
    :param: m number of points to either side that should be larger than the current point, in order to be considered a peak
    :return: local index of the max-point (relative to AOI)
    '''

    peaksIndsList01 = []
    v = np.array(v)
    for curInd in range(m, len(v)-m):
        if curInd > m:
            leftInd = curInd - m
        else:
            leftInd = 0

        if curInd + m < len(v):
            rightInd = curInd + m
        else:
            rightInd = len(v) - curInd

        if np.all(v[leftInd:curInd] <= v[curInd]) and np.all(v[curInd+1:rightInd] <= v[curInd]):
            peaksIndsList01.append(curInd)

    # TO-DO: what if there are consecutive equal values?
    # Need to filter-out consecutive peaks that are just equal consecutive values

    return peaksIndsList01

