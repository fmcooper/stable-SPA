# stable-SPA Readme
3/2-approximation algorithm to the maximum stable matching for instances of SPA-ST
******************************
******************************

1) what is this software?
2) software and data
3) compiling
4) running a single instance
5) running multiple instances


******************************

# 1) what is this software?

This software runs a 3/2-approximation algorithm to the maximum stable 
matching for instances of the Student-Project Allocation problem with
lecturer preferences over Students including Ties (SPA-ST). An IP program
is also provided which can find both the maximum stable matching and 
minimum stable matching for instances of SPA-ST.


******************************

# 2) software and data

You must have Java installed on your computer to run all programs and 
Gurobi installed to run the IP program.

For Gurobi installation follow the guide at: 
http://www.gurobi.com/documentation/6.5/ making sure you install a licence
(academic licences are avaliable free) and set the $PATH, $CLASSPATH and
$LD_LIBRARY_PATH variables as instructed.

Download / git clone the stable-SPA software from Github:
https://github.com/fmcooper/stable-SPA

Data to use with this program can be found at:
http://dx.doi.org/10.5525/gla.researchdata.583 and is placed in the 
'Evaluations' directory.

The software is located at: https://doi.org/10.5281/zenodo.1183222


******************************

# 3) compiling

cd into the stable-SPA directory and then run the following commands:

```bash
$ javac -d . shared_resources/*.java
$ javac -d . approx/*.java
$ javac -d . ip/*.java
```

If you get Gurobi errors at this point then you will need to check you
have followed all installation instructions above. Also ensure that your
path variables have been correctly exported by running "$ echo $PATH" etc. 


******************************

# 4) running a single instance

To use the approximation algorithm run the following command from the 
stable-SPA directory:

```bash
$ java approx/Main Evaluations/SIZE4ex/Instances/instance_0.txt
```

The result will be output to terminal.

To use the IP program run the following command from the stable-SPA 
directory:

```bash
$ java ip/evaluate Evaluations/SIZE4ex/Instances/instance_0.txt -max
```

Again results will be output to terminal. Exchange '-max' for '-min'
to find a minimum stable matching.


******************************

# 5) running multiple instances

Multiple instances can be run using the make files in the experiments
directory. Important note: If the make files are not executing the java
program it may be because you have already created the results files! 
Delete the results files if you want to re-run the experiments.

There are 4 make files:
- evaluationsApprox.mk
- evaluationsOptimalMax.mk
- evaluationsOptimalMin.mk
- correctness.mk

Each make file looks at the 'instanceNames.txt' file to see which 
instances to run over. This file is easy to create (instructions below),
and had been done for you for the example data.

To run the approximation algorithm over all instances in 'instances.txt'
cd to the experiments directory and use the following command:

```bash
$ make -f evaluationsApprox.mk
```

The -j option can be used to run several instances at once, where the
number of instances you choose will depend on the number of processors
you have. Note that the ip is set to run using two threads. For example:

```bash
$ make -j 28 -f evaluationsApprox.mk
```

or
```bash
$ make -j 14 -f evaluationsOptimalMax.mk
```

Once evaluationsApprox.mk, evaluationsOptimalMax.mk and 
evaluationsOptimalMin.mk have been run, we can use the correctness.mk
file to check the stability of each output.

```bash
$ make -f correctness.mk
```

All results files have now been created and can be browsed in the 
'Evaluations' directory.


******************************

creating instanceNames.txt

Use the following commands in the Evaluations directory, then move 
'instanceNames.txt' to the experiments directory

```bash
$ find . -name 'instance*.txt' | grep -v "Results\|Correctness\|instanceNames" > instanceNames.txt
```

Then do this for mac:
```bash
$ sed -i '' 's_\./_\.\./Evaluations/_g' instanceNames.txt
```

or this for linux:
```bash
$ sed -i 's_\./_\.\./Evaluations/_g' instanceNames.txt
```
