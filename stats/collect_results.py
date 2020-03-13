import sys
import numpy as np
import math
from sys import argv
import os, os.path
import datetime

# collect results for each experiment together
# @author Frances

timeout_ms = 1800000

results_post_paths = ["ResultsApprox", "ResultsOptimalMax", "ResultsOptimalMin"]
post_path_instances = "Instances"

# statistical information for a single instance
class instance:
    def __init__(self):
        # general stats
        self.approx_size = -1
        self.approx_duration_total_ms = -1
        self.max_size = -1
        self.max_duration_total_ms = -1
        self.min_size = -1
        self.min_duration_total_ms = -1

        self.approx_timeout = False
        self.max_timeout = False
        self.min_timeout = False


# Iterates over all results files and collates results.
np.set_printoptions(suppress=True)
pre_path = sys.argv[1]

################# variables
exp_names = sorted(os.listdir(pre_path))
dirName = "stats/results"
av_stats = dirName + '/collected_results.txt'
d = {}
maxprefs = {}

#####################################
# main
#####################################
def main():
    if not os.path.exists(dirName):
        os.makedirs(dirName)

    # calculate results
    now = datetime.datetime.now()
    av_stats_file = open(av_stats, 'w')
    av_stats_file.write(
        "Results collected at: " + now.strftime("%Y-%m-%d %H:%M") + "\n\n")
    av_stats_file.close()

    # for each experiment type
    for ind, instance_type in enumerate(exp_names):
        print(instance_type)

        collectResults(
            instance_type, 
            pre_path, 
            post_path_instances, 
            results_post_paths)

    exit(0)


#####################################
# collectResults
#####################################
def collectResults(
    instance_type, 
    pre_path, 
    post_path_instances, 
    results_post_paths):

    infeasibleCounts = []

    # collect instance information
    pathInstance = pre_path + '/' + instance_type + '/' + post_path_instances
    numStudents, numProjects, numLecturers = collectInstanceData(pathInstance)

    # for each instance type collect results data
    path_stub = pre_path + '/' + instance_type + '/'
    total_instances, all_instances = collectResultsData(instance_type, path_stub, results_post_paths)


    outputToFile(instance_type, total_instances, all_instances, numStudents, numProjects, numLecturers);



#####################################
# collectInstanceData
#####################################
def collectInstanceData(pathInstance):
    numMen = ""

    # run over the instance statistics
    name = os.listdir(pathInstance)[0]
    with open(pathInstance + '/' + name) as f:
        content = f.readlines()
        info = content[0].split()
        numStudents = info[0];
        numProjects = info[1];
        numLecturers = info[2];

    return numStudents, numProjects, numLecturers



#####################################
# collectRawData
#####################################
def collectResultsData(instance_type, path_stub, results_post_paths):
    all_instances = []
    total_timeout = 0
    total_instances = 0


    instance_names = os.listdir(path_stub + results_post_paths[0])

    for instName in instance_names:
        timeout = False
        error = False
        inst = instance()
        total_instances += 1


        for exp in results_post_paths:
        
            instFileName = path_stub + exp + '/' + instName

            with open(instFileName) as f:
                content = f.readlines()
                for s in content:
                    # general info
                    if "error" in s:
                        error = True

                    if exp == 'ResultsApprox':
                        if "timeout" in s:
                            inst.approx_timeout = True
                        if "Matching_size" in s:   
                            inst.approx_size = int(s.split()[1].split("/")[0])
                        if "Duration_Total_milliseconds" in s:   
                            inst.approx_duration_total_ms = int(s.split()[1])

                    elif exp == 'ResultsOptimalMax':
                        if "timeout" in s:
                            inst.max_timeout = True
                        if "Matching_size" in s:   
                            inst.max_size = int(s.split()[1].split("/")[0])
                        if "Duration_Total_milliseconds" in s:   
                            inst.max_duration_total_ms = int(s.split()[1])

                    elif exp == 'ResultsOptimalMin':
                        if "timeout" in s:
                            inst.min_timeout = True
                        if "Matching_size" in s:   
                            inst.min_size = int(s.split()[1].split("/")[0])
                        if "Duration_Total_milliseconds" in s:   
                            inst.min_duration_total_ms = int(s.split()[1])


        # save the results
        if not error:
                all_instances.append(inst) 
         

    return total_instances, all_instances


#####################################
# outputToFile
#####################################
def outputToFile(instance_type, total_instances, all_instances, numStudents, numProjects, numLecturers):

    # calculate the averages and output
    avstatsFile = open(av_stats, "a")
    avstatsFile.write('\n# stats for all instance types of ' + instance_type + '\n')
    avstatsFile.write('{}_numStudents {:10}\n'.format(instance_type, numStudents))
    avstatsFile.write('{}_numProjects {:10}\n'.format(instance_type, numProjects))
    avstatsFile.write('{}_numLecturers {:10}\n'.format(instance_type, numLecturers))
    
    # total num instances and timeout
    avstatsFile.write('{}_numInstances {:10}\n'.format(instance_type, str(total_instances)))
    approx_num_timeout = 0
    max_num_timeout = 0
    min_num_timeout = 0
    for inst in all_instances:
        if inst.approx_timeout:
            approx_num_timeout += 1
        if inst.max_timeout:
            max_num_timeout += 1
        if inst.min_timeout:
            min_num_timeout += 1
    avstatsFile.write('{}_approx_NumTimeout {:10}\n'.format(instance_type, str(approx_num_timeout)))
    avstatsFile.write('{}_max_NumTimeout {:10}\n'.format(instance_type, str(max_num_timeout)))
    avstatsFile.write('{}_min_NumTimeout {:10}\n'.format(instance_type, str(min_num_timeout)))

    # durations
    approx_durations_tot = np.array([inst.approx_duration_total_ms for inst in all_instances])
    orig_size = len(approx_durations_tot)
    approx_durations_tot = getNonNegArray(approx_durations_tot)
    new_size = len(approx_durations_tot)
    approx_durations_tot = np.pad(approx_durations_tot, (0, orig_size - new_size), 'constant', constant_values=(timeout_ms))
    writeStatsToFile(avstatsFile, instance_type, "approx_duration_total_ms", approx_durations_tot)

    max_durations_tot = np.array([inst.max_duration_total_ms for inst in all_instances])
    orig_size = len(max_durations_tot)
    max_durations_tot = getNonNegArray(max_durations_tot)
    new_size = len(max_durations_tot)
    max_durations_tot = np.pad(max_durations_tot, (0, orig_size - new_size), 'constant', constant_values=(timeout_ms))
    writeStatsToFile(avstatsFile, instance_type, "max_duration_total_ms", max_durations_tot)

    min_durations_tot = np.array([inst.min_duration_total_ms for inst in all_instances])
    orig_size = len(min_durations_tot)
    min_durations_tot = getNonNegArray(min_durations_tot)
    new_size = len(min_durations_tot)
    min_durations_tot = np.pad(min_durations_tot, (0, orig_size - new_size), 'constant', constant_values=(timeout_ms))
    writeStatsToFile(avstatsFile, instance_type, "min_duration_total_ms", min_durations_tot)

    # measures
    approx_sizes = getNonNegArray(np.array([inst.approx_size for inst in all_instances]))
    max_sizes = getNonNegArray(np.array([inst.max_size for inst in all_instances]))
    min_sizes = getNonNegArray(np.array([inst.min_size for inst in all_instances]))

    writeStatsToFile(avstatsFile, instance_type, "approx_size", approx_sizes)
    writeStatsToFile(avstatsFile, instance_type, "max_size", max_sizes)
    writeStatsToFile(avstatsFile, instance_type, "min_size", min_sizes)

    # average sizes with more decimal places
    avstatsFile.write('{}_Av_approx_size_moredp {:0.8f}\n'.format(instance_type, getAverage(approx_sizes)))
    avstatsFile.write('{}_Av_max_size_moredp {:0.8f}\n'.format(instance_type, getAverage(max_sizes)))
    avstatsFile.write('{}_Av_min_size_moredp {:0.8f}\n'.format(instance_type, getAverage(min_sizes)))

    countOpt = 0
    countOptWithin2percent = 0
    minRatio = 1.0

    for inst in all_instances:
        # compare the size approx with size max
        # 1. count the number of times approx is optimal
        # 2. find the minimum ratio

        if not inst.approx_timeout and not inst.max_timeout:
            if inst.approx_size == inst.max_size:
                countOpt += 1
            if inst.approx_size >= float(inst.max_size) * 0.98:
                countOptWithin2percent += 1
            if inst.approx_size > 0.0:
                ratio = float(inst.approx_size) / float(inst.max_size)
                if ratio < minRatio:
                    minRatio = ratio
     
    num_max_completed = total_instances - max_num_timeout 
    if not num_max_completed == 0:
        avstatsFile.write('{}_approx_opt {:0.1f}\n'.format(instance_type, float(countOpt)/float(num_max_completed) * 100))
        avstatsFile.write('{}_approx_2pc {:0.1f}\n'.format(instance_type, float(countOptWithin2percent)/float(num_max_completed) * 100))
    else:
        avstatsFile.write('{}_approx_opt {:0.1f}\n'.format(instance_type, -1))
        avstatsFile.write('{}_approx_2pc {:0.1f}\n'.format(instance_type, -1))

    avstatsFile.write('{}_approx_min_ratio {:0.4f}\n'.format(instance_type, minRatio))

    avstatsFile.close()


def writeStatsToFile(avstatsFile, instance_type, stats_name, stats):
    avstatsFile.write('{}_Av_{} {:0.1f}\n'.format(instance_type, stats_name, getAverage(stats)))
    avstatsFile.write('{}_median_{} {:0.1f}\n'.format(instance_type, stats_name, getMedian(stats)))
    avstatsFile.write('{}_5Per_{} {:0.1f}\n'.format(instance_type, stats_name, getPercentile(stats, 5.0)))
    avstatsFile.write('{}_95Per_{} {:0.1f}\n'.format(instance_type, stats_name, getPercentile(stats, 95.0)))




# returns an array with negative elements removed
def getNonNegArray(array):
    new_array = []
    for elem in array:
        if elem >= 0:
            new_array.append(elem)
    return new_array


# gets the average of an array or returns -1 if array is 0 in length
def getAverage(array):
    if len(array) == 0:
        return -1
    else:
        return np.mean(array, dtype=np.float64)


# gets the average of an array discounting negative elements
def getAverageNonNeg(array):
    return getAverage(getNonNegArray(array))


# gets the minimum of an array or returns -1 if array is 0 in length
def getMin(array):
    if len(array) == 0:
        return -1
    else:
        return np.min(array)


# gets the maximum of an array or returns -1 if array is 0 in length
def getMax(array):
    if len(array) == 0:
        return -1
    else:
        return np.max(array)


# gets the average profile of an array or returns -1 if array is 0 in length
def getAverageProfile(array2D):
    avP = [];
    # average profile
    if len(array2D) == 0:
        avP.append(-1.0)
    else:
        profile = np.array(array2D)
        # print profile
        # print profile.shape
        avprofile = profile.mean(axis=0)
        avprofile = np.around(avprofile, decimals=1)
        for x in avprofile:
            avP.append(x)
    return avP


# gets the median of an array or returns -1 if array is 0 in length
def getMedian(array):
    if len(array) == 0:
        return -1
    else:
        return np.median(array)


# gets the median of an array discounting negative elements
def getMedianNonNeg(array):
    return getMedian(getNonNegArray(array))


# gets a given percentile of an array or returns -1 if array is 0 in length
def getPercentile(array, percentile):
    if len(array) == 0:
        return -1
    else:
        return np.percentile(array, percentile)


# gets a given percentile of an array discounting negative elements
def getPercentileNonNeg(array, percentile):
    return getPercentile(getNonNegArray(array), percentile)


# gets the minimum, maximum and average profile degree of an array or returns -1
# if array is 0 in length
def getMinMaxAvDegree(array2D):
    minDegree = -1
    maxDegree = -1
    totalDegree = 0.0
    avDegree = -1
    # average profile
    if len(array2D) == 0:
        return minDegree, maxDegree, avDegree
    else:
        for profile in array2D:
            degree = getDegree(profile)
            totalDegree += degree
            if minDegree == -1 or degree < minDegree:
                minDegree = degree
            if maxDegree == -1 or degree > maxDegree:
                maxDegree = degree
    avDegree = float(totalDegree) / float(len(array2D))

    return minDegree, maxDegree, avDegree


# gets the minimum, maximum and average choice of an array or returns -1 if
# array is 0 in length
def getMinMaxAvChoices(array2D, firstOrLast):
    minChoice = -1
    maxChoice = -1
    totalChoice = 0.0
    avChoice = -1
    if len(array2D) == 0:
        return minChoice, maxChoice, avChoice
    else:
        for profile in array2D:
            numChoice = -1;
            if firstOrLast == "first":
                numChoice = profile[0]
            elif firstOrLast == "last":
                numChoice = profile[len(profile) - 1]
            totalChoice += numChoice
            if minChoice == -1 or numChoice < minChoice:
                minChoice = numChoice
            if maxChoice == -1 or numChoice > maxChoice:
                maxChoice = numChoice
    avChoice = float(totalChoice) / float(len(array2D))

    return minChoice, maxChoice, avChoice


# gets the average profile of an array or returns -1 if array is 0 in length
def getAverageProfileString(array):
    avPString = "";
    # average profile
    avPString = "$<$ "
    for x in array:
        avPString += '${:0.1f}$ '.format(x)
    avPString += "$>$"
    return avPString


# gets the degree of a profile array or returns -1 if array is 0 in length
def getDegree(profile):
    # average profile
    if len(profile) == 0:
        return -1
    else:
        count = 0
        for i in reversed(profile):
            if i == 0:
                count = count + 1;
            if not i == 0:
                return len(profile) - count
        return len(profile) - count


# returns the average number of people in a final section of the profile
def getMinMaxAvLast(array2D, fractionOfProfile):
    if len(array2D) == 0:
        return -1, -1, -1
    else:
        profile2D = np.array(array2D)
        sums = []
        indexFrom = int(len(profile2D[0]) * (1 - fractionOfProfile))
        for i in range(len(profile2D)):
            sums.append(sum(profile2D[i][indexFrom:]))
        return np.array(sums).min(), np.array(sums).max(), np.array(sums).mean()


# returns the cieling of the division of two numbers
def ceildiv(a, b):
    return -(-a // b)


# gets the average of an array or returns -1 if array is 0 in length
def getAverage(array):
    if len(array) == 0:
        return -1
    else:
        return np.mean(array, dtype=np.float64)


#####################################
# main def
#####################################
if __name__ == '__main__':
    main()

