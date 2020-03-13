import sys
import numpy as np
import glob
from scipy.stats import sem
from sys import argv
import matplotlib.ticker as FormatStrFormatter
import os, os.path
import datetime

# Iterates over all correctness files and collates results.
# @author Frances

np.set_printoptions(suppress=True)
prePath = sys.argv[1]

################# variables
correctnessNames = os.listdir(prePath)
dirName = "stats/results"
outFileName = dirName + "/correctness.txt"

#####################################
# main
#####################################
def main():
    if not os.path.exists(dirName):
        os.makedirs(dirName)

    now = datetime.datetime.now()
    outFile = open(outFileName, 'w')
    outFile.write("Correctness runthrough conducted at: " 
        + now.strftime("%Y-%m-%d %H:%M") + "\n\n")
    outFile.close()

    failFnames = ""

    # for each experiment type
    for cname in correctnessNames:
        pathResults = prePath + cname + "/Correctness/"

        totalCorrectnessChecked = 0

        total_approx_timeout = 0
        total_stableMax_timeout = 0
        total_stableMin_timeout = 0

        totalPassed = 0
        totalFailed = 0


        # run over the results to get the optimal matching indices
        for name in os.listdir(pathResults):
            if os.path.isfile(pathResults + name):
                totalCorrectnessChecked+=1
                with open(pathResults + name) as f:
                    # pass if either timeout or pass correctness tests
                    inst_approx_pass_stable = False
                    inst_stableMax_pass_stable = False
                    inst_stableMin_pass_stable = False
                    inst_approx_pass_ul = False
                    inst_stableMax_pass_ul = False
                    inst_stableMin_pass_ul = False

                    content = f.readlines()
                    for s in content:


                        if "ResultsApprox_Correctness" in s and "timeout" in s:
                            inst_approx_pass_stable = True
                            inst_approx_pass_ul = True
                            total_approx_timeout += 1
                        if "ResultsApprox_Correctness_stable" in s and "true" in s:
                                inst_approx_pass_stable = True
                        if "ResultsApprox_Correctness_upperLower" in s and "true" in s:
                                inst_approx_pass_ul = True

                        if "ResultsOptimalMax_Correctness" in s and "timeout" in s:
                            inst_stableMax_pass_stable = True
                            inst_stableMax_pass_ul = True
                            total_stableMax_timeout += 1
                        if "ResultsOptimalMax_Correctness_stable" in s and "true" in s:
                                inst_stableMax_pass_stable = True
                        if "ResultsOptimalMax_Correctness_upperLower" in s and "true" in s:
                                inst_stableMax_pass_ul = True

                        if "ResultsOptimalMin_Correctness" in s and "timeout" in s:
                            inst_stableMin_pass_stable = True
                            inst_stableMin_pass_ul = True
                            total_stableMin_timeout += 1
                        if "ResultsOptimalMin_Correctness_stable" in s and "true" in s:
                                inst_stableMin_pass_stable = True
                        if "ResultsOptimalMin_Correctness_upperLower" in s and "true" in s:
                                inst_stableMin_pass_ul = True
                            


                    # an instance passed if all correctness tests either passed or the instance timed out
                    passed = False
                    if (inst_approx_pass_stable and inst_approx_pass_ul and \
                        inst_stableMax_pass_stable and inst_stableMax_pass_ul and \
                        inst_stableMin_pass_stable and inst_stableMin_pass_ul):
                        passed = True

                    # record how many instances passed and failed
                    if passed:
                        totalPassed = totalPassed + 1
                    else:
                        totalFailed = totalFailed + 1
                        failFnames += pathResults + name + "\n"



        # output to a summary file
        outFile = open(outFileName, 'a')
        outFile.write("# experiment: " + cname + "\t")
        outFile.write("totalChecked: " + str(totalCorrectnessChecked) + "\t")
        outFile.write("total_approx_timeout: " + str(total_approx_timeout) + "\t")
        outFile.write("total_stableMax_timeout: " + str(total_stableMax_timeout) + "\t")
        outFile.write("total_stableMin_timeout: " + str(total_stableMin_timeout) + "\t")
        outFile.write("totalPassed: " + str(totalPassed) + "\t")
        outFile.write("totalFailed: " + str(totalFailed) + "\t")
        outFile.write("\n")
        outFile.close()

    outFile = open(outFileName, 'a')
    outFile.write("\n\n# fails: \n" + failFnames)
    outFile.close()

    exit(0)



#####################################
# main def
#####################################
if __name__ == '__main__':
    main()

