JFLAGS = -g
CP = '-cp $(CLASSPATH):../'
SHELL := /bin/bash
INSTANCESFILE := instanceNames.txt
INSTANCES := $(shell cut -d' ' -f1 $(INSTANCESFILE))

results: $(foreach i, $(INSTANCES), $(subst Instances,Correctness,$i))


%.txt:
	# '-' at the beginning indicates that if there is an error code, make should not abort this rule
	# tests whether result is stable and adheres to quotas
	mkdir -p $(dir $*)
	echo $*.txt

	-java -cp $(CP) shared_resources/Tester $(subst Correctness,Instances,$*.txt) $(subst Correctness,ResultsApprox,$*.txt) ResultsApprox  >> $*.txt
	-java -cp $(CP) shared_resources/Tester $(subst Correctness,Instances,$*.txt) $(subst Correctness,ResultsOptimalMax,$*.txt) ResultsOptimalMax  >> $*.txt
	-java -cp $(CP) shared_resources/Tester $(subst Correctness,Instances,$*.txt) $(subst Correctness,ResultsOptimalMin,$*.txt) ResultsOptimalMin  >> $*.txt


