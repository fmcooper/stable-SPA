# stable-SPA Readme
3/2-approximation algorithm to the maximum stable matching for instances of SPA-ST
******************************

1) what is this software?
2) software and data
3) running a single instance
4) running all experiments
5) interpreting results files
6) running a trace

******************************

# 1) what is this software?

This software runs a 3/2-approximation algorithm to the maximum stable 
matching for instances of the Student-Project Allocation problem with
lecturer preferences over Students including Ties (SPA-ST). An IP program
is also provided which can find both the maximum stable matching and 
minimum stable matching for instances of SPA-ST.


******************************

# 2) software and data

You must have Java and Python installed on your computer to run all programs and 
Gurobi installed to run the IP program.

For Gurobi installation follow the guide at: 
http://www.gurobi.com/documentation/6.5/ making sure you install a licence
(academic licences are avaliable free) and set the $PATH, $CLASSPATH and
$LD_LIBRARY_PATH variables as instructed.

Download / git clone the stable-SPA software from Github:
https://github.com/fmcooper/stable-SPA


Data and software version information can be found in the following paper: 
A 3/2-approximation algorithm for the Student-Project Allocation problem
Authors: Frances Cooper and David Manlove
Submitted to SEA conference 2018

The data is located at: https://doi.org/10.5281/zenodo.1186823
The software is located at: https://doi.org/10.5281/zenodo.1183221


******************************

# 3) running a single instance

To use the approximation algorithm run the following command from the 
stable-SPA directory:

```bash
javac -d . shared_resources/*.java
javac -d . approx/*.java
javac -d . ip/*.java

$ java approx/Main Evaluations/SIZE4ex/Instances/instance_0.txt
```

The result will be output to terminal.

To use the IP program, compile the code as above and then run the following command from the stable-SPA 
directory:

```bash
$ java ip/evaluate Evaluations/SIZE4ex/Instances/instance_0.txt -max
```

Again results will be output to terminal. Exchange '-max' for '-min'
to find a minimum stable matching.

******************************

# 4) running all experiments

If you are on a mac computer, cd into the stable-SPA directory and then run the following command:

```bash
source run.sh
```

This will compile the software and run all experiments.

If you get Gurobi errors at this point then you will need to check you
have followed all installation instructions above. Also ensure that your
path variables have been correctly exported by running "$ echo $PATH" etc. 

Notes
* If you are on linux, ypu will need to change the timeout command in each of the ``experiments/*.mk`` files from "gtimeout" to "timeout".
* If you wish to run algorithms / statistics colectors separately, please see the ``run.sh`` script for an example of how they are called.
* If you change the instance types that are run, the ``stats.py`` script will need to be changed to reflect this.
* Remember to delete existing results files if you want to recreate them.



******************************

# 5) interpreting results files

For each instance (e.g. ``Evaluation/SIZE1/Instances/instance_0.txt``), there will be four results files created in the "grandparent's" directory .

* ``Evaluation/SIZE1/ResultsApprox/instance_0.txt``: This is the results file for the approximation algorithm.
* ``Evaluation/SIZE1/ResultsOptimalMax/instance_0.txt``: This is the results file for the maximum stable matching found by the IP.
* ``Evaluation/SIZE1/ResultsOptimalMin/instance_0.txt``: This is the results file for the minimim stable matching found by the IP.
* ``Evaluation/SIZE1/Correctness/instance_0.txt``: This is the correctness testing summary for this instance.



******************************

# 5) running a trace

It is possible to print a trace of the workings of the approximation algorithm for a particular instance. 

First, set the ``trace`` boolean in the ``approx/Approx.java`` file to true.

Next, compile the code from the root directory:

```bash
javac -d . shared_resources/*.java
javac -d . approx/*.java
```

Finally, from the root directory, run a single instance. For example:

```bash
java approx/Main examples/example_1/example_1.txt
``` 

