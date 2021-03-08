package de.rwth.i2.attestor.grammar.confluence.main;

import de.rwth.i2.attestor.main.AbstractPhase;
import de.rwth.i2.attestor.main.scene.DefaultScene;
import de.rwth.i2.attestor.phases.communication.InputSettings;
import de.rwth.i2.attestor.phases.communication.OutputSettings;
import de.rwth.i2.attestor.phases.transformers.InputSettingsTransformer;
import de.rwth.i2.attestor.phases.transformers.OutputSettingsTransformer;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import java.util.Arrays;
import java.util.Iterator;

public class ConfluenceCommandLinePhase extends AbstractPhase implements InputSettingsTransformer, OutputSettingsTransformer {

    private final String[] originalCommandLineArguments;
    private final InputSettings inputSettings = new InputSettings();
    private final OutputSettings outputSettings = new OutputSettings();

    private final Options commandLineOptions = new Options();

    public ConfluenceCommandLinePhase(DefaultScene scene, String[] args) {
        super(scene);
        setupOptions();
        this.originalCommandLineArguments = args;
    }

    @Override
    public InputSettings getInputSettings() {
        return inputSettings;
    }

    @Override
    public OutputSettings getOutputSettings() { return outputSettings; }

    @Override
    public String getName() {
        return "Confluence Command Line";
    }

    @Override
    public void executePhase() throws IllegalArgumentException {
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine commandLine = parser.parse(commandLineOptions, originalCommandLineArguments);

            for(Iterator<Option> optionIterator = commandLine.iterator(); optionIterator.hasNext();){
                Option option = optionIterator.next();
                processOption(option);
            }
        } catch (ParseException e) {
            printHelp();
            throw new IllegalArgumentException(e.getMessage());
        }
    }



    @Override
    public void logSummary() {
        if(inputSettings.hasGrammarName()) {
            logSum("Analyzed grammar: " + inputSettings.getGrammarName());
        }
    }

    @Override
    public boolean isVerificationPhase() {
        return false;
    }

    private void processOption(Option option) {
        String optionName = option.getLongOpt();

        switch(optionName){
            case "root-path":
                String rootPath = option.getValue();
                logger.info("root path: "+rootPath);
                inputSettings.setRootPath(rootPath);
                outputSettings.setRootPath(rootPath);
                break;
            case "grammar":
                String grammar = option.getValue();
                logger.info("grammar: " + grammar);
                inputSettings.addUserDefinedGrammarFile(grammar);
                break;
            case "inductive-predicates":
                String sid = option.getValue();
                logger.info("system of inductive predicates: " + sid);
                inputSettings.addUserDefinedInductivePredicatesFile(sid);
                break;
            case "predefined-grammar":
                String grammarName = option.getValue();
                if(grammarName == null) {
                    throw new IllegalArgumentException("Unspecified grammar name");
                }
                logger.info("predefined grammar: " + grammarName);
                inputSettings.addPredefinedGrammarName(grammarName);
                break;
            case "name":
                String name = option.getValue();
                logger.info("Name: "+name);
                inputSettings.setGrammarName(name);
                break;
            case "completion-heuristics":
                String[] heuristics = option.getValues();
                logger.info(" Completion Heuristic list: "+ Arrays.toString(heuristics));
                for(String heuristic: heuristics){
                    inputSettings.addCompletionHeuristic(heuristic);
                }
                break;
            case "completion-algorithm":
                String algorithm = option.getValue();
                logger.info("Completion Algorithm: "+ algorithm);
                inputSettings.setCompletionAlgorithm(algorithm);
                break;
            case "export-grammar":
                String file = option.getValue();
                logger.info("Export grammar: "+file);
                outputSettings.setExportGrammarPath(file);
                break;
            case "export-latex":
                String directory = option.getValue();
                logger.info("Export latex directory: "+directory);
                outputSettings.setExportLatexPath(directory);
                break;
            case "quit":
                Configurator.setRootLevel(Level.OFF);
                break;
            case "verbose":
                Configurator.setRootLevel(Level.INFO);
                break;
            case "debug":
                Configurator.setRootLevel(Level.DEBUG);
                break;
            default:
                throw new IllegalArgumentException("Unknown command line option: " + optionName);
        }

    }

    public void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "java -jar AttestorConfluence", this.commandLineOptions);
    }


    private void setupOptions() {
        commandLineOptions.addOption(
                Option.builder("rp")
                        .longOpt("root-path")
                        .hasArg()
                        .argName("path")
                        .desc("Determines the provided path as a common prefix for all other paths provided " +
                                "in command line options. More precisely, affected options whose arguments are " +
                                "concatenated with prefix <path> are: \n" +
                                "* --grammar\n" +
                                "* --inductive-predicates\n" +
                                "* --export-grammar\n" +
                                "* --export-latex\n" +
                                "If option --root-path is not explicitly, the root path is set to the empty string.")
                        .build()
        );

        commandLineOptions.addOption(
                Option.builder("n")
                        .longOpt("name")
                        .hasArg()
                        .argName("text")
                        .desc("Optionally provides a descriptive " +
                                "name for the (collection of) specified grammar.")
                        .build()
        );

        commandLineOptions.addOption(
                Option.builder("g")
                    .longOpt("grammar")
                    .hasArg()
                    .argName("file")
                    .desc("Loads a user-supplied graph grammar from the provided <file>." +
                          "Please confer the syntax for graph grammars for further details on " +
                          "writing custom graph grammars. ")
                    .build()
        );

        commandLineOptions.addOption(
                Option.builder("sid")
                        .longOpt("inductive-predicates")
                        .hasArg()
                        .argName("file")
                        .desc("Loads a user-supplied System of Inductive predicate Definitions (SID)" +
                              "written in a fragment of symbolic heap separation logic. " +
                              "Please confer the syntax for inductive predicate definitions for further" +
                              "details on writing custom predicate definitions.")
                        .build()
        );

        commandLineOptions.addOption(
                Option.builder("pg")
                        .longOpt("predefined-grammar")
                        .hasArg()
                        .argName("name")
                        .desc("Adds a predefined graph grammar with the provided name to the grammars " +
                                "used in the analysis. " +
                                "Please confer the list of predefined data structures for further details " +
                                "on available predefined graph grammars.")
                        .build()
        );

        commandLineOptions.addOption(
                Option.builder("ch")
                        .longOpt("completion-heuristics")
                        .hasArgs()
                        .valueSeparator(',')
                        .argName("list")
                        .desc("Adds a list of heuristics to complete a non-confluence grammar into a confluence " +
                                "grammar by adding and removing grammar rules that. " +
                                "Use ',' to separate heuristics that are applied sequential on the grammar. "+
                                "Please confer the list of implemented completion heuristics for further details "+
                                "on available heuristics for completion.")
                        .build()
        );

        commandLineOptions.addOption(
                Option.builder("ca")
                        .longOpt("completion-algorithm")
                        .hasArg()
                        .argName("name")
                        .desc("Uses a predefined completion heuristic algorithm to complete a non-confluence grammar " +
                                "into a confluence grammar by adding and removing grammar rules. " +
                                "Please confer the list of implemented completion algorithms for further deatails " +
                                "on available algorithms for completion.")
                        .build()
        );

        commandLineOptions.addOption(
                Option.builder()
                        .longOpt("export-grammar")
                        .hasArg()
                        .argName("path")
                        .desc("Exports the graph grammars used within the analysis as a json file. " +
                                "The exported grammar is written to a directory ROOT_PATH/<path>, where ROOT_PATH is" +
                                " the path determined by --root-path.")
                        .build()
        );

        commandLineOptions.addOption(
                Option.builder()
                        .longOpt("export-latex")
                        .hasArg()
                        .argName("directory")
                        .desc("Defined a directory where debug output is written to. The debug output tex files. "+
                                "The tex files heavily uses tikz pictures to display various steps in the computation, "+
                                "namely the initial grammar, the critical pairs and their joinability, the "+
                                "not strongly joinable critical pairs, the grammar after completion and "+
                                "the remaining not strongly joinable critical pairs after completion.")
                        .build()
        );

        OptionGroup debugOptions = new OptionGroup();

        debugOptions.addOption(
                Option.builder("q")
                        .longOpt("quiet")
                        .desc("Suppresses most output passed to the logger.")
                        .build()
        );

        debugOptions.addOption(
                Option.builder("v")
                        .longOpt("verbose")
                        .desc("Logs additional information about the execution of phases.")
                        .build()
        );

        debugOptions.addOption(
                Option.builder()
                        .longOpt("debug")
                        .desc("Logs additional debug data about the execution of phases.")
                        .build()
        );

        commandLineOptions.addOptionGroup(debugOptions);
    }
}
