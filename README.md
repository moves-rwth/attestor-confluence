# What is this tool?
This confluence checker is designed to check backwards-confluence of hypergraph replacement grammars with a data structure property. This tool is designed to help [Attestor][1] check grammars for confluence and complete them in case they are not confluent. This guarantees uniquenes of abstraction in the symbolic execution phase of Attestors verification process. A confluent grammar especially allows efficient language inclusion check, as well as entailment checking for a fragement of seperation logic.

# System Requirements

The following software has to be installed prior to the installation of Attestor:

- [Java JDK 1.8][2]
- [Apache Maven][3]

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

### `--root-path`, `-rp` *PATH*
Determines the provided path as a common prefix for all other paths provided in command line options. More precisely, affected options whose arguments are concatenated with prefix *PATH* are:

- `--grammar` ([link](#--grammar--g-grammar_file)
- `--inductive-predicate` ([link](#--inductive-predicate--sid-sid_file))
- `--export-grammar` ([link](#--export-grammar-path))
- `--export-latex` ([link](#--export-latex-path))


## Input Options

### `--name`, `-n` *TEXT* 
Specifies a name for the grammar as *TEXT*. Does not have any technical purpose, other than displaying the name on the report.

### `--grammar`, `-g` *GRAMMAR_FILE*
Loads a user-supplied graph grammar from the provided *GRAMMAR_FILE*.

Please confer [syntax for graph grammars](https://github.com/moves-rwth/attestor/wiki/Graph-Grammar-Syntax) for further details on writing custom graph grammars.

If `--root-path` ([link](#--root-path--rp-path)) is set then the common root path is added as a prefix to the grammar file.

### `--inductive-predicate`, `-sid` *SID_FILE*
Loads a user-supplied system of inductive predicate definitions (SID) written in a fragment of symbolic heap separation logic. The SID will internally be converted into a graph grammar. Please confer the [syntax for inductive predicate definitions](https://github.com/moves-rwth/attestor/wiki/SID-Syntax) for further details on writing custom predicate definitions.

If `--root-path` ([link](#--root-path--rp-path)) is set then the common root path is added as a prefix to the input file.

### `--predefined-grammar`, `-pg` *NAME*
Adds a predefined graph grammar with the provided name to the grammars. The predefined grammars are:
- SLList
- DLList
- BT

For more information, also confere the [specification of predefined graph grammars](https://github.com/moves-rwth/attestor/wiki/Predefined-Data-Structures).

## Completion Options

### `--completion-algorithm`, `-ca` *NAME*
Uses the completion algorithm specified by *NAME*. 

Available completion algorithms are:
- `completionAbstractionBlocking`
- `addRulesNewNonterminalHeuristic`
- `joinGeneratedNonterminals`
- `singleNonterminalRuleAddingHeuristic`
- `ruleRestriction`
- `onlyRuleAdding`
- `onlyRuleAddingNotLocalConcretizable`
- `combinedAlgorithm1`
- `combinedAlgorithm1NoLocalConcretizabilityCheck`
- `combinedAlgorithm2`
- `combinedAlgorithm2NoLocalConcretizabilityCheck`

### `--completion-heuristics`, `-ch` *LIST_OF_NAMES*
Adds the completion heuristics with names in the list *LIST_OF_NAMES* to the completion algorithm. The list is structured as the list of names seperated by commans. If no completion algorithm is specified, a default algorithm with no heuristics is used. 

Available completion heuristics are:
- `AddRuleHandleWithSubgraph`
- `AddRulesNewNonterminal`
- `CompletionAbstractionBlocking`
- `CompletionRuleRestriction`
- `JoinGeneratedNonterminals`
- `SingleNonTerminalRuleAdding`

## Output Options

### `--export-grammar` *PATH*
Exports the graph grammar after completion. The exported grammar is written to the directory *PATH*.

If `--root-path` ([link](#--root-path--rp-path)) is set then the common root path is added as a prefix to the input file.

### `--export-latex` *PATH*
Exports the latex output as a tikz visualization for critical pairs before completion and the not strongly-joinable critical pairs after completion, as well as the grammar before completion and after completion in the directory *PATH*.

If `--root-path` ([link](#--root-path--rp-path)) is set then the common root path is added as a prefix to the input file.

## Logging Options

### `--quit`, `-q`
Suppresses all output generated by the logger.

### `--verbose`, `-v`
Generate additional logging output that does not require knowledge about implementation details.

### `--debug`
Generate additional logging output that may require deep knowledge about implementation details.


[1]: https://github.com/moves-rwth/attestor
[2]: http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
[3]: http://maven.apache.org/
[4]: https://github.com/Sable/soot
