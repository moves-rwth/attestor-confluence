package de.rwth.i2.attestor.grammar.confluence.completion;

import de.rwth.i2.attestor.grammar.Grammar;
import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.TikzReporter;
import de.rwth.i2.attestor.grammar.confluence.completion.heuristics.*;
import de.rwth.i2.attestor.grammar.confluence.completion.loss.NumberCriticalPairLoss;
import de.rwth.i2.attestor.grammar.confluence.completion.strategies.GreedyCompletion;
import de.rwth.i2.attestor.grammar.confluence.completion.validity.CheckDataStructureGrammar;
import de.rwth.i2.attestor.grammar.confluence.completion.validity.GrammarValidity;
import de.rwth.i2.attestor.grammar.confluence.completion.validity.LocalConcretizability;
import de.rwth.i2.attestor.main.AbstractPhase;
import de.rwth.i2.attestor.main.scene.Scene;
import de.rwth.i2.attestor.phases.communication.InputSettings;
import de.rwth.i2.attestor.phases.transformers.GrammarTransformer;
import de.rwth.i2.attestor.phases.transformers.InputSettingsTransformer;
import de.rwth.i2.attestor.util.Pair;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class CompletionPhase extends AbstractPhase implements GrammarTransformer, TikzReporter {

    private Grammar grammar = null;
    private boolean isConfluent = false;
    private boolean isActive = false;

    public CompletionPhase(Scene scene) {
        super(scene);
    }

    @Override
    public String getName() { return "Confluence Completion"; }

    @Override
    public void executePhase() throws IOException {
        Grammar grammar = getPhase(GrammarTransformer.class).getGrammar();
        if(grammar == null){
            throw new IllegalArgumentException("No grammar was provided.");
        }
        this.grammar = grammar;
        InputSettings inputSettings = getPhase(InputSettingsTransformer.class).getInputSettings();
        if(!inputSettings.getCompletionHeuristics().isEmpty()) {
            isActive = true;
            CompletionAlgorithm completionAlgorithm = buildCompletionAlgorithm(inputSettings.getCompletionAlgorithm());
            addHeuristicsCompletionAlgorithm(completionAlgorithm, inputSettings.getCompletionHeuristics());
            NamedGrammar namedGrammar = new NamedGrammar(this.grammar, "Inputed Grammar");
            CompletionState result = completionAlgorithm.runCompletionAlgorithm(namedGrammar);
            if(result.getCriticalPairs().isEmpty()){
                isConfluent = true;
            }
        }

    }

    private CompletionAlgorithm buildCompletionAlgorithm(String algorithm) throws IOException {
        switch(algorithm){
            case "completionAbstractionBlocking":
                return ExampleCompletionAlgorithms.completionAbstractionBlocking();
            case "addRulesNewNonterminalHeuristic":
                return ExampleCompletionAlgorithms.addRulesNewNonterminalHeuristic();
            case "joinGeneratedNonterminals":
                return ExampleCompletionAlgorithms.joinGeneratedNonterminals();
            case "singleNonterminalRuleAddingHeuristic":
                return ExampleCompletionAlgorithms.singleNonterminalRuleAddingHeuristic();
            case "ruleRestriction":
                return ExampleCompletionAlgorithms.ruleRestriction();
            case "onlyRuleAdding":
                return ExampleCompletionAlgorithms.onlyRuleAdding();
            case "onlyRuleAddingNotLocalConcretizable":
                return ExampleCompletionAlgorithms.onlyRuleAddingNotLocalConcretizable();
            case "combinedAlgorithm1":
                return ExampleCompletionAlgorithms.combinedAlgorithm1();
            case "combinedAlgorithm1NoLocalConcretizabilityCheck":
                return ExampleCompletionAlgorithms.combinedAlgorithm1NoLocalConcretizabilityCheck();
            case "combinedAlgorithm2":
                return ExampleCompletionAlgorithms.combinedAlgorithm2();
            case "combinedAlgorithm2NoLocalConcretizabilityCheck":
                return ExampleCompletionAlgorithms.combinedAlgorithm2NoLocalConcretizabilityCheck();
            case "":
                return new CompletionAlgorithm("user defined algorithm")
                        .setCompletionStrategy(new GreedyCompletion(0))
                        .setCompletionStateLoss(new NumberCriticalPairLoss())
                        .addGrammarValidityCheck(new CheckDataStructureGrammar());
            default:
                throw new IOException("Algorithm "+algorithm+" unknown!");
        }
    }

    private CompletionAlgorithm addHeuristicsCompletionAlgorithm(CompletionAlgorithm algorithm, List<String> heuristics) throws IOException {
        for(String heuristic: heuristics){
            switch(heuristic){
                case "AddRuleHandleWithSubgraph":
                    algorithm.addHeuristic(new AddRuleHandleWithSubgraphHeuristic());
                    break;
                case "AddRulesNewNonterminal":
                    algorithm.addHeuristic(new AddRulesNewNonterminalHeuristic());
                    break;
                case "CompletionAbstractionBlocking":
                    algorithm.addHeuristic(new CompletionAbstractionBlockingHeuristic());
                    break;
                case "CompletionRuleRestriction":
                    algorithm.addHeuristic(new CompletionRuleRestrictionHeuristic(false, true));
                    break;
                case "JoinGeneratedNonterminals":
                    algorithm.addHeuristic(new JoinGeneratedNonterminalsHeuristic());
                    break;
                case "SingleNonTerminalRuleAdding":
                    algorithm.addHeuristic(new SingleNonterminalRuleAddingHeuristic());
                    break;
                default:
                    throw new IOException("Heuristic "+heuristic+" unknown!");
            }
        }

        if( ( heuristics.contains("AddRulesNewNonterminal")
           || heuristics.contains("JoinGeneratedNonterminals")
           || heuristics.contains("SingleNonterminalRuleAdding") )
        && !containsLocalConcretizability(algorithm) ){
            algorithm.addGrammarValidityCheck(new LocalConcretizability());
        }
        return algorithm;
    }

    private boolean containsLocalConcretizability(CompletionAlgorithm algorithm) {
        for(GrammarValidity validity: algorithm.getValidityChecks()){
            if(validity instanceof  LocalConcretizability){
                return true;
            }
        }
        return false;
    }

    @Override
    public void logSummary() {
        if(isActive){
            if(isConfluent) {
                logHighlight("The grammar after completion is backwards confluent.");
            }else{
                logHighlight("The grammar after completion is NOT backwards confluent.");
            }
        }
    }

    @Override
    public boolean isVerificationPhase() {
        return false;
    }

    @Override
    public Grammar getGrammar() {
        return null; //TODO
    }

    @Override
    public Collection<Pair<String, TikzReporter>> getTikzExporter() {
        return null; //TODO
    }
}
