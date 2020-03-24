JFLAGS = -g
CP = '-cp $(CLASSPATH):../'
SHELL := /bin/bash
INSTANCESFILE := instanceNames.txt
INSTANCES := $(shell cut -d' ' -f1 $(INSTANCESFILE))
TIMEOUT := 1800


results: $(foreach i, $(INSTANCES), $(subst Instances,ResultsApprox,$i))

%.txt:
	mkdir -p $(dir $*)
	-gtimeout $(TIMEOUT) java -cp $(CP) approx/Main $(subst ResultsApprox,Instances,$*.txt) > $*.txt;  if [ $$? -ne 0 ] ; then echo "timeout $(TIMEOUT)s" > $*.txt ; fi



