package de.rwth.i2.attestor.grammar.confluence.main;

import de.rwth.i2.attestor.main.AbstractPhase;
import de.rwth.i2.attestor.main.scene.DefaultScene;
import de.rwth.i2.attestor.phases.commandLineInterface.CommandLineReader;
import de.rwth.i2.attestor.phases.communication.InputSettings;
import de.rwth.i2.attestor.phases.transformers.InputSettingsTransformer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import java.io.IOException;
import java.util.Iterator;

public class ConfluenceCommandLinePhase extends AbstractPhase implements InputSettingsTransformer {

    private final String[] originalCommandLineArguments;
    private final InputSettings inputSettings = new InputSettings();

    public ConfluenceCommandLinePhase(DefaultScene scene, String[] args) {
        super(scene);
        this.originalCommandLineArguments = args;
    }

    @Override
    public InputSettings getInputSettings() {
        return inputSettings;
    }

    @Override
    public String getName() {
        return "Confluence Command Line Interface";
    }

    @Override
    public void executePhase() throws IOException {
        // TODO: Support more options
        inputSettings.addPredefinedGrammarName(originalCommandLineArguments[0]);
    }

    @Override
    public void logSummary() {
        logSum("Analyzed method: " + inputSettings.getClassName() + "." + inputSettings.getMethodName());
        String description = inputSettings.getDescription();
        if(description != null) {
            logSum("Scenario: " + description);
        }
    }

    @Override
    public boolean isVerificationPhase() {
        return false;
    }
}
