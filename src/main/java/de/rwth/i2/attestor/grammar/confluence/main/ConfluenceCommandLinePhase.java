package de.rwth.i2.attestor.grammar.confluence.main;

import de.rwth.i2.attestor.main.AbstractPhase;
import de.rwth.i2.attestor.main.scene.DefaultScene;
import de.rwth.i2.attestor.phases.commandLineInterface.CommandLineReader;
import de.rwth.i2.attestor.phases.communication.InputSettings;
import de.rwth.i2.attestor.phases.transformers.InputSettingsTransformer;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.util.Iterator;

public class ConfluenceCommandLinePhase extends AbstractPhase implements InputSettingsTransformer {

    private final String[] originalCommandLineArguments;
    private final InputSettings inputSettings = new InputSettings();

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
    public String getName() {
        return "Confluence Command Line";
    }

    @Override
    public void executePhase() throws IllegalArgumentException {
        try {
            inputSettings.setDescription("Grammar");
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
        logSum("Analyzed grammar: "+inputSettings.getDescription());
    }

    @Override
    public boolean isVerificationPhase() {
        return false;
    }

    private void processOption(Option option) {
        String optionName = option.getLongOpt();

        switch(optionName){
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
                inputSettings.setDescription(name);
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
                                "The fixed node type and selector names can be renamed using --rename " +
                                "Please confer the list of predefined data structures for further details " +
                                "on available predefined graph grammars.")
                        .build()
        );
    }
}
