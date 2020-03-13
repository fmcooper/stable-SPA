JFLAGS = -g
CP = '-cp $(CLASSPATH):../'
SHELL := /bin/bash
INSTANCESFILE := instanceNames.txt
INSTANCES := $(shell cut -d' ' -f1 $(INSTANCESFILE))
TIMEOUT := 1

results: $(foreach i, $(INSTANCES), $(subst Instances,ResultsOptimalMin,$i))

%.txt:
	mkdir -p $(dir $*)
	# '-' at the beginning indicates that if there is an error code, make should not abort this rule
	# the $$? must be used in the same shell as the previous command, hence it is added after a ';' at the end
	-gtimeout $(TIMEOUT) java -cp $(CP) ip/evaluate $(subst ResultsOptimalMin,Instances,$*.txt) -min > $*.txt;  if [ $$? -ne 0 ] ; then echo "timeout $(TIMEOUT)s" > $*.txt ; fi

