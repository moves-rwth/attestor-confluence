package de.rwth.i2.attestor.io.tikzOutput;

import de.rwth.i2.attestor.grammar.Grammar;
import de.rwth.i2.attestor.grammar.GrammarBuilder;
import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.CriticalPair;
import de.rwth.i2.attestor.grammar.confluence.CriticalPairFinder;
import de.rwth.i2.attestor.grammar.confluence.Joinability;
import de.rwth.i2.attestor.grammar.confluence.TestGrammars;
import de.rwth.i2.attestor.grammar.confluence.benchmark.BenchmarkRunner;
import de.rwth.i2.attestor.grammar.confluence.main.ConfluenceTool;
import de.rwth.i2.attestor.graph.BasicNonterminal;
import de.rwth.i2.attestor.graph.BasicSelectorLabel;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.SelectorLabel;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.heap.HeapConfigurationBuilder;
import de.rwth.i2.attestor.graph.heap.internal.InternalHeapConfiguration;
import de.rwth.i2.attestor.main.scene.SceneObject;
import de.rwth.i2.attestor.types.GeneralType;
import de.rwth.i2.attestor.types.Type;
import gnu.trove.list.array.TIntArrayList;

import javax.naming.Name;
import java.io.IOException;
import java.util.Collection;


// TODO: Remove this test class
public class Temp {

    public static void main(String args[]) {
        //exportDefaultGrammar("BT_conf");
        //exportDefaultGrammar("BT");
        exportDefaultGrammar("DLList");
        //exportDefaultGrammar("SLList");
        //exportDefaultGrammar("DLList_simple_one_way");
        //exportDefaultGrammar("DLList_simple_two_way");
        try {
            NamedGrammar grammar = BenchmarkRunner.getSeparationLogicNamedGrammar("SimpleDLL");

            TikzExport exporter = new TikzExport("reports/simple-dll-list-grammar-report.tex", true);
            exporter.exportGrammar(grammar, true);
            exporter.finishExport();

            exporter = new TikzExport("reports/simple-dll-list-critical-pair-report.tex", true);
            CriticalPairFinder criticalPairFinder = new CriticalPairFinder(grammar);
            Collection<CriticalPair> criticalPairs = criticalPairFinder.getCriticalPairsMaxJoinability(Joinability.WEAKLY_JOINABLE);
            exporter.exportCriticalPairs(criticalPairs);
            exporter.finishExport();
        } catch (Exception e) {
            System.err.println("Error occurred");
        }

        try {
            NamedGrammar grammar = new NamedGrammar(getTernaryGrammar(), "Ternary");

            TikzExport exporter = new TikzExport("reports/ternary-grammar-report.tex", true);
            exporter.exportGrammar(grammar, true);
            exporter.finishExport();

            exporter = new TikzExport("reports/ternary-critical-pair-report.tex", true);
            CriticalPairFinder criticalPairFinder = new CriticalPairFinder(grammar);
            Collection<CriticalPair> criticalPairs = criticalPairFinder.getCriticalPairsMaxJoinability(Joinability.WEAKLY_JOINABLE);
            exporter.exportCriticalPairs(criticalPairs);
            exporter.finishExport();
        } catch (Exception e) {
            System.err.println("Error occurred");
        }
    }

    public static void exportDefaultGrammar(String defaultGrammarName) {
        NamedGrammar grammar = ConfluenceTool.parseGrammar(defaultGrammarName);
        try {
            TikzExport exporter = new TikzExport("reports/" + defaultGrammarName + "-grammar-report.tex", true);
            exporter.exportGrammar(grammar, true);
            exporter.finishExport();
        } catch (IOException e) {
            System.err.println("IO Exception occurred");
        }
        CriticalPairFinder criticalPairFinder = new CriticalPairFinder(grammar);
        Collection<CriticalPair> criticalPairs = criticalPairFinder.getCriticalPairsMaxJoinability(Joinability.WEAKLY_JOINABLE);
        try {
            TikzExport exporter = new TikzExport("reports/" + defaultGrammarName + "-critical-pair-report.tex", true);
            exporter.exportCriticalPairs(criticalPairs);
            exporter.finishExport();
        } catch (IOException e) {
            System.err.println("IO Exception occurred");
        }
    }

    private static Grammar getTernaryGrammar() {
        BasicNonterminal.Factory factory = new BasicNonterminal.Factory();
        Nonterminal list = factory.create("N", 3, new boolean[]{false, false, false});
        BasicSelectorLabel.Factory factory1 = new BasicSelectorLabel.Factory();
        SelectorLabel pointer = factory1.get("a");
        SelectorLabel pointer2 = factory1.get("b");
        GeneralType.Factory factory2 = new GeneralType.Factory();
        Type listElement = factory2.get("Element");
        TIntArrayList nodesHc1 = new TIntArrayList(3);
        HeapConfiguration hc1 = new InternalHeapConfiguration().builder()
                .addNodes(listElement, 3, nodesHc1)
                .setExternal(nodesHc1.get(0)).setExternal(nodesHc1.get(1)).setExternal(nodesHc1.get(2))
                .addSelector(nodesHc1.get(0), pointer, nodesHc1.get(1))
                .addSelector(nodesHc1.get(1), pointer, nodesHc1.get(2))
                .addSelector(nodesHc1.get(2), pointer2, nodesHc1.get(0))
                .build();

        TIntArrayList nodesHc2 = new TIntArrayList(6);
        HeapConfigurationBuilder hc2Builder = new InternalHeapConfiguration().builder()
                .addNodes(listElement, 6, nodesHc2)
                .setExternal(nodesHc2.get(3)).setExternal(nodesHc2.get(4)).setExternal(nodesHc2.get(5))
                .addSelector(nodesHc2.get(0), pointer, nodesHc2.get(3))
                .addSelector(nodesHc2.get(1), pointer, nodesHc2.get(4))
                .addSelector(nodesHc2.get(2), pointer, nodesHc2.get(5));

        int nonTerminalEdge = hc2Builder.addNonterminalEdgeAndReturnId(list, TIntArrayList.wrap(new int[] {nodesHc2.get(0), nodesHc2.get(1), nodesHc2.get(2)}));
        HeapConfiguration hc2 = hc2Builder.build();

        return new GrammarBuilder()
                .addRule(list, hc1)
                .addRule(list, hc2)
                .updateCollapsedRules()
                .build();
    }
}
