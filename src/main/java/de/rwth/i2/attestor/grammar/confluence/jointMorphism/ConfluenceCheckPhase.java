package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.grammar.Grammar;
import de.rwth.i2.attestor.grammar.ConfluenceWrapperGrammar;
import de.rwth.i2.attestor.grammar.confluence.CriticalPair;
import de.rwth.i2.attestor.grammar.confluence.CriticalPairFinder;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;
import de.rwth.i2.attestor.io.FileUtils;
import de.rwth.i2.attestor.io.tikzOutput.TikzExport;
import de.rwth.i2.attestor.main.AbstractPhase;
import de.rwth.i2.attestor.main.scene.Scene;
import de.rwth.i2.attestor.phases.communication.InputSettings;
import de.rwth.i2.attestor.phases.communication.OutputSettings;
import de.rwth.i2.attestor.phases.transformers.GrammarTransformer;
import de.rwth.i2.attestor.phases.transformers.InputSettingsTransformer;
import de.rwth.i2.attestor.phases.transformers.OutputSettingsTransformer;

import java.io.File;
import java.io.IOException;

public class ConfluenceCheckPhase extends AbstractPhase {

    private int numberWeaklyJoinable = 0;
    private int numberStronglyJoinable = 0;
    private int numberNotJoinable = 0;

    public ConfluenceCheckPhase(Scene scene) {
        super(scene);
    }

    @Override
    public String getName() {
        return "Confluence Check";
    }

    public boolean isConfluent() {
        return numberNotJoinable == 0 && numberWeaklyJoinable == 0;
    }

    @Override
    public void executePhase() throws IllegalArgumentException {
        Grammar grammar = getPhase(GrammarTransformer.class).getGrammar();
        if(grammar == null){
            throw new IllegalArgumentException("No grammar was provided.");
        }
        InputSettings inputSettings = getPhase(InputSettingsTransformer.class).getInputSettings();
        ConfluenceWrapperGrammar confluenceWrapperGrammar = new ConfluenceWrapperGrammar(grammar, inputSettings.getGrammarName());
        CriticalPairFinder finder = checkGrammar(confluenceWrapperGrammar);
        try {
            exportLatex(finder, confluenceWrapperGrammar);
        } catch (IOException e) {
            logger.error("Could not output latex code.",e);
        }
    }

    @Override
    public void logSummary() {
        if(isConfluent()) {
            logHighlight("The grammar is backwards confluent.");
        }else{
            logHighlight("The grammar is NOT backwards confluent.");
        }
        logSum("+-------------------------+------------------+");
        logSum(String.format("| Strongly Joinable Pair  | %16d |", numberStronglyJoinable));
        logSum(String.format("| Weakly Joinable Pair    | %16d |",numberWeaklyJoinable));
        logSum(String.format("| Not Joinable Pair       | %16d |",numberNotJoinable));
        logSum("+-------------------------+------------------+");
    }

    @Override
    public boolean isVerificationPhase() {
        return true;
    }

    private CriticalPairFinder checkGrammar(ConfluenceWrapperGrammar confluenceWrapperGrammar){

        CriticalPairFinder criticalPairFinder = new CriticalPairFinder(confluenceWrapperGrammar);
        for(CriticalPair criticalPair : criticalPairFinder.getCriticalPairs()) {
            switch (criticalPair.getJoinability()) {
                case WEAKLY_JOINABLE:
                    numberWeaklyJoinable++;
                    break;
                case STRONGLY_JOINABLE:
                    numberStronglyJoinable++;
                    break;
                case NOT_JOINABLE:
                    numberNotJoinable++;
                    break;
            }
        }
        return criticalPairFinder;
    }

    private void exportLatex(CriticalPairFinder result, ConfluenceWrapperGrammar grammar) throws IOException {
        OutputSettings outputSettings = getPhase(OutputSettingsTransformer.class).getOutputSettings();

        if(outputSettings.getExportLatexPath() == null){
            return;
        }


        logger.info("Start exporting confluence check latex files...");

        FileUtils.createDirectories(outputSettings.getExportLatexPath());

        TikzExport exportCriticalPairs = new TikzExport(outputSettings.getExportLatexPath() + File.separator + "criticalPairs.tex", true);
        exportCriticalPairs.exportCriticalPairs(result.getCriticalPairs());
        exportCriticalPairs.finishExport();

        TikzExport exportGrammar = new TikzExport(outputSettings.getExportLatexPath() + File.separator + "grammar.tex", true);
        exportGrammar.exportGrammar(grammar, false);
        exportGrammar.finishExport();

        logger.info("Confluence check latex files exported!");
    }
}
