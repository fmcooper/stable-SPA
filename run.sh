

javac -d . shared_resources/*.java
javac -d . approx/*.java
javac -d . ip/*.java


find ./Evaluations -name 'instance*.txt' | grep -v "Results\|Correctness\|instanceNames" > experiments/instanceNames.txt


sed -i '' 's_\./_\.\./_g' experiments/instanceNames.txt
# sed -i 's_\./_\.\./Evaluations/_g' experiments/instanceNames.txt


(cd experiments && make -f evaluationsApprox.mk)
(cd experiments && make -f evaluationsOptimalMax.mk)
(cd experiments && make -f evaluationsOptimalMin.mk)

(cd experiments && make -f correctness.mk)
