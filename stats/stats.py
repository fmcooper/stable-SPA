import matplotlib
matplotlib.use('Agg')
import sys
import numpy as np
import matplotlib.pylab as plt
import glob
from scipy.stats import sem
from sys import argv
import matplotlib.ticker as FormatStrFormatter
import os
from scipy.optimize import curve_fit

np.set_printoptions(suppress=True)
prePath = sys.argv[1]

################# variables
SIZE = ["SIZE1", "SIZE2", "SIZE3", "SIZE4", "SIZE5", "SIZE6", "SIZE7", "SIZE8", "SIZE9", "SIZE10"]
PREF = ["PREF1", "PREF2", "PREF3", "PREF4", "PREF5", "PREF6", "PREF7", "PREF8", "PREF9", "PREF10"]
TIES = ["TIES1", "TIES2", "TIES3", "TIES4", "TIES5", "TIES6", "TIES7", "TIES8", "TIES9", "TIES10", "TIES11"]
SCAL = ["SCAL1", "SCAL2", "SCAL3", "SCAL4", "SCAL5"]
SCALP = ["SCALP1", "SCALP2", "SCALP3", "SCALP4", "SCALP5", "SCALP6"]
experiments = [SIZE, PREF, TIES, SCAL, SCALP]
experimentsNames = ["SIZE", "PREF", "TIES", "SCAL", "SCALP"]
listTypesPerExp = [10, 10, 11, 5, 6]

# axis values for plots
experiments_plots = [SIZE, PREF, TIES]
SIZE_vals = [100, 200, 300, 400, 500, 600, 700, 800, 900, 1000]
PREF_vals = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
TIES_vals = [0.0, 0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5]
experiment_vals=[SIZE_vals, PREF_vals, TIES_vals]

# Increasing probability of ties experimental results.
# Scalability experimental results.

# SIZE = ["SIZE1",]
#experiments = [TINY, ]
#experimentsNames = ["TINY", ]
#listTypesPerExp = [3, ]

listresType = ['Approx', 'OptimalMax', 'OptimalMin']

dirName = "stats/results"
av_stats = "stats/results/collected_results.txt"

d = {}
maxprefs = {}

#####################################
# main
#####################################
def main():
    if not os.path.exists(dirName):
        os.makedirs(dirName)

    # get averages
    getAverages()

    # create plots and tables
    createPlots()
    createPaperTables()

    exit(0)


#####################################
# collect the raw data from each instance file
#####################################
def getAverages():
    global d
    with open(av_stats) as inF:
        content = inF.readlines()
        for s in content:
            if len(s) < 2 or s[0]=="#":
                continue
            else:
                ssplit = s.split()
                d[ssplit[0]] = ssplit[1:]


#####################################
# matplotlib plots
#####################################
def createPlots():
    # collect the data
    for i, exp_type in enumerate(experiments_plots):

        approx_median, approx_5, approx_95 = [], [], []
        max_median, size_max_5, max_95 = [], [], []
        min_median, size_min_5, min_95 = [], [], []

        # collect data
        for exp in exp_type:
            approx_median.append(float(d['{}_median_approx_size'.format(exp)][0]))
            approx_5.append(float(d['{}_5Per_approx_size'.format(exp)][0]))
            approx_95.append(float(d['{}_95Per_approx_size'.format(exp)][0]))
            max_median.append(float(d['{}_median_max_size'.format(exp)][0]))
            size_max_5.append(float(d['{}_5Per_max_size'.format(exp)][0]))
            max_95.append(float(d['{}_95Per_max_size'.format(exp)][0]))
            min_median.append(float(d['{}_median_min_size'.format(exp)][0]))
            size_min_5.append(float(d['{}_5Per_min_size'.format(exp)][0]))
            min_95.append(float(d['{}_95Per_min_size'.format(exp)][0]))
        
        # create plots
        if experimentsNames[i] == "PREF":
            curve_log = True
            xlabel = "$l_{min} = l_{max}$"
        if experimentsNames[i] == "SIZE":
            curve_log = False
            xlabel = "$n$"
        if experimentsNames[i] == "TIES":
            curve_log = False
            xlabel = "$t_s = t_l$"
        
        createPlot(curve_log, experimentsNames[i], experiment_vals[i], xlabel, "Size of stable matching.", \
            approx_median, approx_5, approx_95, "Approx", \
            max_median, size_max_5, max_95, "Min", \
            min_median, size_min_5, min_95, "Max")

        


# 1D curve function
def func1D(x, a, b):
    return a + b*x


# 2D curve function
def func2D(x, a, b, c):
    y = a + b*x + c*x*x
    return y


# creates the plot 
def createPlot(curve_log, plot_label, xlist, xlabel, ylabel, \
            approx_median, approx_5, approx_95, approx_label, \
            max_median, size_max_5, max_95, min_label, \
            min_median, size_min_5, min_95, max_label):
    

    # starting the plotting
    matplotlib.rcParams.update({'font.size': 14})
    plt.figure()
    plt.figure(facecolor='w', edgecolor='k', figsize=(7, 5))
    plt.xlabel(xlabel)
    plt.ylabel(ylabel)

    # curves
    if curve_log:
        approxCV,_ = curve_fit(func2D, np.log(xlist), approx_median)
        maxCV,_ = curve_fit(func2D, np.log(xlist), max_median)
        minCV,_ = curve_fit(func2D, np.log(xlist), min_median)
        approx_5_CV,_ = curve_fit(func2D, np.log(xlist), approx_5)
        max_5_CV,_ = curve_fit(func2D, np.log(xlist), size_max_5)
        min_5_CV,_ = curve_fit(func2D, np.log(xlist), size_min_5)
        approx_95_CV,_ = curve_fit(func2D, np.log(xlist), approx_95)
        max_95_CV,_ = curve_fit(func2D, np.log(xlist), max_95)
        min_95_CV,_ = curve_fit(func2D, np.log(xlist), min_95)
    else:
        approxCV,_ = curve_fit(func2D, xlist, approx_median)
        maxCV,_ = curve_fit(func2D, xlist, max_median)
        minCV,_ = curve_fit(func2D, xlist, min_median)
        approx_5_CV,_ = curve_fit(func2D, xlist, approx_5)
        max_5_CV,_ = curve_fit(func2D, xlist, size_max_5)
        min_5_CV,_ = curve_fit(func2D, xlist, size_min_5)
        approx_95_CV,_ = curve_fit(func2D, xlist, approx_95)
        max_95_CV,_ = curve_fit(func2D, xlist, max_95)
        min_95_CV,_ = curve_fit(func2D, xlist, min_95)

    xlist = np.array(xlist)


    # plot the points
    plt.plot(xlist, max_median, 'o', markersize=4, label=max_label, color='orangered')
    plt.plot(xlist, approx_median, '*', markersize=4, label=approx_label, color='skyblue')
    plt.plot(xlist, min_median, 's', markersize=4, label=min_label, color='seagreen')

    xlist = np.linspace(xlist[0], xlist[-1], 200)
    
    if curve_log:
        # plot the best fit curves
        plt.plot(xlist, func2D(np.log(xlist), maxCV[0], maxCV[1], maxCV[2]), '-', color='orangered')
        plt.plot(xlist, func2D(np.log(xlist), approxCV[0], approxCV[1], approxCV[2]), '-', color='skyblue')
        plt.plot(xlist, func2D(np.log(xlist), minCV[0], minCV[1], minCV[2]), '-', color='seagreen')

        # plot the confidence intervals
        plt.fill_between(xlist, func2D(np.log(xlist), max_5_CV[0], max_5_CV[1], max_5_CV[2]), \
                                func2D(np.log(xlist), max_95_CV[0], max_95_CV[1], max_95_CV[2]), color='orangered', alpha=.5)
        plt.fill_between(xlist, func2D(np.log(xlist), approx_5_CV[0], approx_5_CV[1], approx_5_CV[2]), \
                                func2D(np.log(xlist), approx_95_CV[0], approx_95_CV[1], approx_95_CV[2]), color='skyblue', alpha=.5)
        plt.fill_between(xlist, func2D(np.log(xlist), min_5_CV[0], min_5_CV[1], min_5_CV[2]), \
                                func2D(np.log(xlist), min_95_CV[0], min_95_CV[1], min_95_CV[2]), color='seagreen', alpha=.5)
    else:
        # plot the best fit curves
        plt.plot(xlist, func2D(xlist, maxCV[0], maxCV[1], maxCV[2]), '-', color='orangered')
        plt.plot(xlist, func2D(xlist, approxCV[0], approxCV[1], approxCV[2]), '-', color='skyblue')
        plt.plot(xlist, func2D(xlist, minCV[0], minCV[1], minCV[2]), '-', color='seagreen')

        # plot the confidence intervals
        plt.fill_between(xlist, func2D(xlist, max_5_CV[0], max_5_CV[1], max_5_CV[2]), \
                                func2D(xlist, max_95_CV[0], max_95_CV[1], max_95_CV[2]), color='orangered', alpha=.5)
        plt.fill_between(xlist, func2D(xlist, approx_5_CV[0], approx_5_CV[1], approx_5_CV[2]), \
                                func2D(xlist, approx_95_CV[0], approx_95_CV[1], approx_95_CV[2]), color='skyblue', alpha=.5)
        plt.fill_between(xlist, func2D(xlist, min_5_CV[0], min_5_CV[1], min_5_CV[2]), \
                                func2D(xlist, min_95_CV[0], min_95_CV[1], min_95_CV[2]), color='seagreen', alpha=.5)

    # plt.plot([100, 1000], [100, 1000000], "--", label="Gradient 4n")


    # plt.xlim(xmin=0, xmax=1000)
    # plt.yscale('log')
    plt.grid()
    plt.legend()
    # plt.legend(loc='upper left')
    plt.tight_layout()
    filename = dirName + "/" + plot_label + "_plot.pdf"
    plt.savefig(filename)
    plt.close()
    plt.rcParams.update(plt.rcParamsDefault)


#####################################
# create tables
#####################################
def createPaperTables():
    for index, exp in enumerate(experimentsNames):
        latexpaper = dirName + "/" + 'latex_paper_tables' + exp + '.txt'
        latexPaperFile = open(latexpaper, 'w')
        latexPaperFile.write('')
        latexPaperFile.close
        latexPaperFile.write('\\begin{table}[tbp] \centerline{')
        latexPaperFile.write('\\begin{tabular}{ p{1.3cm} R{1.3cm} R{1.3cm} R{1.3cm} R{2.2cm} R{1.3cm} R{1.3cm} R{1.3cm} R{1.6cm} R{2.2cm} R{1.3cm} R{1.3cm} }') 
        latexPaperFile.write('\hline\hline ')
        latexPaperFile.write('& \multirow{2}{1.5cm}{minimum A/Max} & \multirow{2}{1.5cm}{\% A=Max} & \multirow{2}{1.5cm}{\% A$\geq 0.98 $Max} & \multicolumn{5}{c}{average size} & \multicolumn{3}{c}{average total time (ms)}\\\\ \n')
        latexPaperFile.write('Case &  &  &  & A & Min & Max & A/Max & Min/Max & A & Min & Max \\\\ \n')
        latexPaperFile.write('\hline ')

        for i in range(1, listTypesPerExp[index] + 1):
            d[exp+str(i)+'_max_NumTimeout']

            if int(d[exp+str(i)+'_max_NumTimeout'][0]) == int(d[exp+str(i)+'_numInstances'][0]):
                sizeAppCompOpt = -1.0
                sizeMinCompOpt = -1.0
            else:
                sizeAppCompOpt = '{:0.3f}'.format(float(d[exp+str(i)+'_Av_approx_size_moredp'][0]) / float(d[exp+str(i)+'_Av_max_size_moredp'][0]))
                sizeMinCompOpt = '{:0.3f}'.format(float(d[exp+str(i)+'_Av_min_size_moredp'][0]) / float(d[exp+str(i)+'_Av_max_size_moredp'][0]))

            latexPaperFile.write('{} & ${}$ & ${}$ & ${}$ & ${}$ & ${}$ & ${}$ & ${}$ & ${}$ & ${}$ & ${}$ & ${}$ \\\\ \n '.format(\
                exp + str(i), d[exp+str(i)+'_approx_min_ratio'][0], d[exp+str(i)+'_approx_opt'][0], \
                d[exp+str(i)+'_approx_2pc'][0], d[exp+str(i)+'_Av_approx_size'][0], \
                d[exp+str(i)+'_Av_min_size'][0], d[exp+str(i)+'_Av_max_size'][0], \
                sizeAppCompOpt, sizeMinCompOpt, \
                d[exp+str(i)+'_Av_approx_duration_total_ms'][0], \
                d[exp+str(i)+'_Av_min_duration_total_ms'][0], d[exp+str(i)+'_Av_max_duration_total_ms'][0]))

        # finishing the latex results file
        latexPaperFile.write('\hline\hline \end{tabular}} \caption{Paper results.} \label{} \end{table} ')
        latexPaperFile.close 


    
        latexpaper = dirName + "/" + 'latex_paper_tables' + exp + '_timings.txt'
        latexPaperFile = open(latexpaper, 'w')
        latexPaperFile.write('')
        latexPaperFile.close
        latexPaperFile.write('\\begin{table}[tbp] \centerline{')
        latexPaperFile.write('\\begin{tabular}{ p{1.5cm} R{1.5cm} R{1.5cm} R{1.5cm} R{2.5cm} R{2cm} R{2cm} }') 
        latexPaperFile.write('\hline\hline ')
        latexPaperFile.write('& \multicolumn{3}{c}{instances feasible} & \multicolumn{3}{c}{average time (ms)}\\\\ \n')
        latexPaperFile.write('Case & A & Min & Max & A & Min & Max \\\\ \n')
        latexPaperFile.write('\hline ')

        for i in range(1, listTypesPerExp[index] + 1):
            app_num_feas = int(d[exp+str(i)+'_numInstances'][0]) - int(d[exp+str(i)+'_approx_NumTimeout'][0])
            max_num_feas = int(d[exp+str(i)+'_numInstances'][0]) - int(d[exp+str(i)+'_max_NumTimeout'][0])
            min_num_feas = int(d[exp+str(i)+'_numInstances'][0]) - int(d[exp+str(i)+'_min_NumTimeout'][0])
            latexPaperFile.write('{} & ${}$ & ${}$ & ${}$ & ${}$ & ${}$ & ${}$ \\\\ \n '.format(\
                exp + str(i), app_num_feas, min_num_feas, max_num_feas, \
                d[exp+str(i)+'_Av_approx_duration_total_ms'][0], \
                d[exp+str(i)+'_Av_min_duration_total_ms'][0], \
                d[exp+str(i)+'_Av_max_duration_total_ms'][0]))

        # finishing the latex results file
        latexPaperFile.write('\hline\hline \end{tabular}} \caption{Paper results - timings.} \label{} \end{table} ')
        latexPaperFile.close  
    
 










# a standard graph plot
def standardPlot(title, xaxlabel, yaxlabel, x, y, exptype, saveName):
    plt.figure()
    plt.figure(facecolor='w', edgecolor='k', figsize=(8, 5))
    plt.title(title)
    plt.xlabel(xaxlabel)
    plt.ylabel(yaxlabel)
    plt.ylim(0,1005)
    plt.xticks(x, exptype)
    plt.plot(x, y, 'o')
    plt.grid(True)
    plt.savefig(saveName)



# gets the average of an array or returns -1 if array is 0 in length
def getAverage(array):
    if len(array) == 0:
        return -1
    else:
        return np.mean(array, dtype=np.float64)



# returns the cieling of the division of two numbers
def ceildiv(a, b):
    return -(-a // b)



#####################################
# main def
#####################################
if __name__ == '__main__':
    main()

