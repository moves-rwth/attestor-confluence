package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.grammar.Grammar;
import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.CriticalPair;
import de.rwth.i2.attestor.grammar.confluence.CriticalPairFinder;
import de.rwth.i2.attestor.grammar.confluence.Joinability;
import de.rwth.i2.attestor.grammar.confluence.TikzReporter;
import de.rwth.i2.attestor.grammar.confluence.main.ConfluenceCommandLinePhase;
import de.rwth.i2.attestor.io.tikzOutput.TikzExport;
import de.rwth.i2.attestor.main.AbstractPhase;
import de.rwth.i2.attestor.main.scene.Scene;
import de.rwth.i2.attestor.phases.communication.InputSettings;
import de.rwth.i2.attestor.phases.transformers.GrammarTransformer;
import de.rwth.i2.attestor.phases.transformers.InputSettingsTransformer;
import de.rwth.i2.attestor.util.Pair;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class ConfluenceCheckPhase extends AbstractPhase implements TikzReporter {

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
        NamedGrammar namedGrammar = new NamedGrammar(grammar, inputSettings.getDescription());
        checkGrammar(namedGrammar);
    }

    @Override
    public void logSummary() {
        if(isConfluent()) {
            logHighlight("The grammar is backwards confluent.");
        }else{
            logHighlight("The grammar is NOT backwards confluent.");
        }
        logSum("+-------------------------+------------------+");
        logSum(String.format("| Strongly Joinable Nodes | %16d |", numberStronglyJoinable));
        logSum(String.format("| Weakly Joinable Nodes   | %16d |",numberWeaklyJoinable));
        logSum(String.format("| Not Joinable Nodes      | %16d |",numberNotJoinable));
        logSum("+-------------------------+------------------+");
    }

    @Override
    public boolean isVerificationPhase() {
        return true;
    }

    private void checkGrammar(NamedGrammar namedGrammar){

        CriticalPairFinder criticalPairFinder = new CriticalPairFinder(namedGrammar);
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
    }

    @Override
    public Collection<Pair<String, TikzReporter>> getTikzExporter() {
        return null; //TODO
    }
}
