package de.rwth.i2.attestor.grammar.confluence;

import de.rwth.i2.attestor.grammar.Grammar;
import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;
import de.rwth.i2.attestor.grammar.confluence.completion.ExampleCompletionAlgorithms;
import de.rwth.i2.attestor.io.tikzOutput.TikzExport;
import de.rwth.i2.attestor.main.Attestor;
import de.rwth.i2.attestor.main.scene.DefaultScene;
import de.rwth.i2.attestor.main.scene.SceneObject;
import de.rwth.i2.attestor.seplog.InductivePredicatesParser;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;

public class TestGrammars {

    public static void main(String[] args) {
        getReportsForGrammar("SimpleDLL");
        getReportsForGrammar("LinkedTree1");
        getReportsForGrammar("InTree");
        getReportsForGrammar("InTreeLinkedLeaves");
        getReportsForGrammar("LinkedTree2");
    }

    /**
     * Exports .tex files for grammar and critical pairs before and after completion.
     * Also outputs number of critical pairs before and after to stdout.
     */
    public static void getReportsForGrammar(String grammarName) {
        System.out.println(grammarName);
        try {
            new File("reports/" + grammarName ).mkdirs();
            NamedGrammar grammar = getSeparationLogicNamedGrammar(grammarName);
            String fileName = "reports/" + grammarName + "/{0}_" + grammarName + ".tex";
            TikzExport export;

            // Export initial grammar
            export = new TikzExport(MessageFormat.format(fileName, "initial_grammar"), true);
            export.exportGrammar(grammar, true);
            export.finishExport();

            // Get initial critical pairs
            CriticalPairFinder finder = new CriticalPairFinder(grammar);
            Collection<CriticalPair> initialCriticalPairs = finder.getCriticalPairsMaxJoinability(Joinability.WEAKLY_JOINABLE);
            System.out.println("Number initial critical pairs: " + initialCriticalPairs.size());

            export = new TikzExport(MessageFormat.format(fileName, "initial_critical_pairs"), true);
            export.exportCriticalPairs(initialCriticalPairs);
            export.finishExport();

            // Run completion
            CompletionState completionResult = ExampleCompletionAlgorithms.algorithm1(grammar);

            // Get resulting critical pairs
            Collection<CriticalPair> resultingCriticalPairs = completionResult.getCriticalPairs();
            System.out.println("Number final critical pairs: " + resultingCriticalPairs.size());
            export = new TikzExport(MessageFormat.format(fileName, "resulting_critical_pairs"), true);
            export.exportCriticalPairs(resultingCriticalPairs);
            export.finishExport();

            // Get resulting grammar
            NamedGrammar resultingGrammar = completionResult.getGrammar();
            export = new TikzExport(MessageFormat.format(fileName, "resulting_grammar"), true);
            export.exportGrammar(resultingGrammar, true);
            export.finishExport();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Grammar getSeparationLogicGrammar(String grammarName) throws IOException {
        DefaultScene scene = new DefaultScene();
        SceneObject sceneObject = new SceneObject(scene) {};
        InductivePredicatesParser parser = new InductivePredicatesParser(sceneObject);
        return parser.parseFromUrl(
                Attestor.class.getClassLoader().getResource("confluenceTestGrammars/" + grammarName + ".sid")
        );
    }

    public static NamedGrammar getSeparationLogicNamedGrammar(String grammarName) throws IOException {
        return new NamedGrammar(getSeparationLogicGrammar(grammarName), grammarName);
    }

}
