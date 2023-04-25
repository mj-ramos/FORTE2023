# FORTE2023

## Java Pathfinder

To execute JPF with one of the samples change to the directory of that sample (for example `cd bank/samples/bank1`), execute the compile commands provided in the test.txt of that directory and run the following command:  
`java -jar <jpf-core-dir>/build/RunJPF.jar +classpath=./bin/<application-main-class> +listener=gov.nasa.jpf.listener.PreciseRaceDetector BankTest` 
