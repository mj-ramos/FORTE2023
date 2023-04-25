# FORTE2023

## Java Pathfinder
Downloaded from https://github.com/javapathfinder/jpf-core/tree/45a4450cd0bd1193df5419f7c9d9b89807d00db6
Used with jdk-8
  
To execute JPF with one of the samples change to the directory of that sample (for example `cd bank/samples/bank1`), execute the compile commands provided in the test.txt of that directory and run the following command:  
`java -jar <jpf-core-dir>/build/RunJPF.jar +classpath=./bin/<application-main-class> +listener=gov.nasa.jpf.listener.PreciseRaceDetector <test-class>`
  
 For the bank samples, `<test-class>` is `BankTest` and for the bounded buffer samples is `Test`.
