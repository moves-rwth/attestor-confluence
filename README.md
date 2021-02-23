# What is this tool?
This confluence checker is designed to check backwards-confluence of hypergraph replacement grammars with a data structure property. This tool is designed to help [Attestor][1] check grammars for confluence and complete them in case they are not confluent. This guarantees uniquenes of abstraction in the symbolic execution phase of Attestors verification process. A confluent grammar especially allows efficient language inclusion check, as well as entailment checking for a fragement of seperation logic.

# System Requirements

The following software has to be installed prior to the installation of Attestor:

- [Java JDK 1.8][2]
- [Apache Maven][3]
- Since Attestor uses [soot][4], please make sure that rt.jar is in your `CLASSPATH` and that `JAVA_HOME` is set correctly.


# Installation
To install the latest version of the Attestor confluence checker, please proceed as follows: 

    $ git clone https://github.com/moves-rwth/attestor-confluence.git
    $ mvn install

Please note that the installation requires an internet connection as maven will install additional dependencies.

## Getting Started

After installation, an executable jar file is created in the directory `target` within the cloned repository. The name of executable jar is of the form 

     attestor-confluence-<VERSION>-jar-with-dependencies.jar 

where `<VERSION>` is the previously cloned version of the Attestor repository.
To execute Attestor, it suffices to run

     $ java -jar attestor-confluence-<VERSION>-jar-with-dependencies.jar 

from within the `target` directory. 
This should display a help page explaining all available [command line options](#command-line-options).
Since the above jar file contains all dependencies, it is safe to rename it and move the file to a more convenient directory.

# Tour

# Command Line Options


[1]: https://github.com/moves-rwth/attestor
[2]: http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
[3]: http://maven.apache.org/
[4]: https://github.com/Sable/soot
