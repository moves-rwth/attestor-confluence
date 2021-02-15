package de.rwth.i2.attestor.grammar.confluence.completion;

import de.rwth.i2.attestor.grammar.Grammar;
import de.rwth.i2.attestor.grammar.ConfluenceWrapperGrammar;
import de.rwth.i2.attestor.grammar.confluence.completion.heuristics.*;
import de.rwth.i2.attestor.grammar.confluence.completion.loss.NumberCriticalPairLoss;
import de.rwth.i2.attestor.grammar.confluence.completion.strategies.GreedyCompletion;
import de.rwth.i2.attestor.grammar.confluence.completion.validity.CheckDataStructureGrammar;
import de.rwth.i2.attestor.grammar.confluence.completion.validity.GrammarValidity;
import de.rwth.i2.attestor.grammar.confluence.completion.validity.LocalConcretizability;
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
import java.util.List;

public class CompletionPhase extends AbstractPhase implements GrammarTransformer {

    private Grammar grammar = null;
    private boolean isConfluent = false;
    private boolean isActive = false;
    private OutputSettings outputSettings;

    public CompletionPhase(Scene scene) {
        super(scene);
    }

    @Override
    public String getName() { return "Confluence Completion"; }

    @Override
    public void executePhase() throws IOException {
        grammar = getPhase(GrammarTransformer.class).getGrammar();
        outputSettings = getPhase(OutputSettingsTransformer.class).getOutputSettings();
        if(grammar == null){
            throw new IllegalArgumentException("No grammar was provided.");
        }
        this.grammar = grammar;
        InputSettings inputSettings = getPhase(InputSettingsTransformer.class).getInputSettings();
        if(!inputSettings.getCompletionHeuristics().isEmpty() || !inputSettings.getCompletionAlgorithm().isEmpty()) {
            isActive = true;
            CompletionAlgorithm completionAlgorithm = buildCompletionAlgorithm(inputSettings.getCompletionAlgorithm());
            addHeuristicsCompletionAlgorithm(completionAlgorithm, inputSettings.getCompletionHeuristics());
            ConfluenceWrapperGrammar confluenceWrapperGrammar = new ConfluenceWrapperGrammar(this.grammar, "Inputed Grammar");
            CompletionState result = completionAlgorithm.runCompletionAlgorithm(confluenceWrapperGrammar);
            grammar = result.getGrammar().getConcretizationGrammar();
            if(result.getCriticalPairs().isEmpty()){
                isConfluent = true;
            }
            try {
                exportLatex(result);
            } catch (IOException e) {
                logger.error("Could not export latex.",e);
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

    private void exportLatex(CompletionState result) throws IOException {
        OutputSettings outputSettings = getPhase(OutputSettingsTransformer.class).getOutputSettings();

        if(outputSettings.getExportLatexPath() == null){
            return;
        }

        logger.info("Start exporting completion latex files...");

        FileUtils.createDirectories(outputSettings.getExportLatexPath());

        TikzExport exportCriticalPairs = new TikzExport(outputSettings.getExportLatexPath() + File.separator + "criticalPairsAfterCompletion.tex", true);
        exportCriticalPairs.exportCriticalPairs(result.getCriticalPairs());
        exportCriticalPairs.finishExport();

        TikzExport exportGrammar = new TikzExport(outputSettings.getExportLatexPath() + File.separator + "grammarAfterCompletion.tex", true);
        exportGrammar.exportGrammar(result.getGrammar(), false);
        exportGrammar.finishExport();

        logger.info("Completion latex files exported!");
    }

    @Override
    public boolean isVerificationPhase() {
        return false;
    }

    @Override
    public Grammar getGrammar() {
        return this.grammar;
    }
}
