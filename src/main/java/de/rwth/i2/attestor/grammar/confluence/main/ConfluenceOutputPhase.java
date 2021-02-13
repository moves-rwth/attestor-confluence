package de.rwth.i2.attestor.grammar.confluence.main;

import de.rwth.i2.attestor.grammar.GrammarExporter;
import de.rwth.i2.attestor.io.jsonExport.cytoscapeFormat.JsonGrammarExporter;
import de.rwth.i2.attestor.main.AbstractPhase;
import de.rwth.i2.attestor.main.PhaseRegistry;
import de.rwth.i2.attestor.main.scene.Scene;
import de.rwth.i2.attestor.phases.communication.OutputSettings;
import de.rwth.i2.attestor.phases.transformers.GrammarTransformer;
import de.rwth.i2.attestor.phases.transformers.OutputSettingsTransformer;
import de.rwth.i2.attestor.util.ZipUtils;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ConfluenceOutputPhase extends AbstractPhase {

    private OutputSettings outputSettings;
    private List<String> summaryMessages;

    public ConfluenceOutputPhase(Scene scene) {
        super(scene);
    }

    @Override
    public String getName() {
        return "Confluence Report";
    }

    @Override
    public void executePhase() {

        outputSettings = getPhase(OutputSettingsTransformer.class).getOutputSettings();

        try {
            exportGrammar();
            exportLatex();
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    @Override
    public void logSummary() {
        //TODO
    }

    @Override
    public boolean isVerificationPhase() {
        return false;
    }

    private void exportGrammar() throws IOException {

        String location = outputSettings.getExportGrammarPath();

        if(location == null) {
            return;
        }

        logger.info("Exporting grammar...");

        // Copy necessary libraries
        InputStream zis = getClass().getClassLoader().getResourceAsStream("grammarViewer.zip");

        File targetDirectory = new File(location + File.separator);
        ZipUtils.unzip(zis, targetDirectory);

        // Generate JSON files
        GrammarExporter exporter = new JsonGrammarExporter();
        exporter.export(location + File.separator + "grammarData",
                getPhase(GrammarTransformer.class).getGrammar());

        String summary = "Grammar exported to " + location;
        logger.info(summary);
        summaryMessages.add(summary);
    }

    public void exportLatex() {

    }
}
