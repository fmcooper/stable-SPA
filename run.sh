#!/bin/bash

EVAL_PATH="../../../../../Downloads/stable-SPA_data"

# compile code
javac -d . shared_resources/*.java
javac -d . approx/*.java
javac -d . ip/*.java

# create the list of instances
find $EVAL_PATH -name 'instance*.txt' | grep -v "Results\|Correctness\|instanceNames" > experiments/instanceNames.txt
sed -i '' 's_^_\.\.\/_g' experiments/instanceNames.txt			# may need to adapt for linux   

# run the experiments and correctness testing
(cd experiments && make -f evaluationsApprox.mk)
(cd experiments && make -f evaluationsOptimalMax.mk)
(cd experiments && make -f evaluationsOptimalMin.mk)
(cd experiments && make -f correctness.mk)

# results
python stats/correctness.py $EVAL_PATH
python stats/collect_results.py $EVAL_PATH
python stats/stats.py $EVAL_PATH
