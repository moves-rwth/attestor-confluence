package de.rwth.i2.attestor.grammar.confluence;

import de.rwth.i2.attestor.grammar.Grammar;
import de.rwth.i2.attestor.grammar.GrammarBuilder;
import de.rwth.i2.attestor.graph.BasicNonterminal;
import de.rwth.i2.attestor.graph.BasicSelectorLabel;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.SelectorLabel;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.heap.HeapConfigurationBuilder;
import de.rwth.i2.attestor.graph.heap.internal.InternalHeapConfiguration;
import de.rwth.i2.attestor.types.GeneralType;
import de.rwth.i2.attestor.types.Type;
import gnu.trove.list.array.TIntArrayList;

public class TestGrammars {

    public static Grammar getSimpleDLList() {
        BasicNonterminal.Factory factory = new BasicNonterminal.Factory();
        Nonterminal list = factory.create("L", 2, new boolean[]{false, false});
        BasicSelectorLabel.Factory factory1 = new BasicSelectorLabel.Factory();
        SelectorLabel nextPointer = factory1.get("n");
        SelectorLabel previousPointer = factory1.get("p");
        GeneralType.Factory factory2 = new GeneralType.Factory();
        Type listElement = factory2.get("ListElement");
        TIntArrayList nodesHc1 = new TIntArrayList(2);
        HeapConfiguration hc1 = new InternalHeapConfiguration().builder()
                .addNodes(listElement, 2, nodesHc1)
                .setExternal(nodesHc1.get(0)).setExternal(nodesHc1.get(1))
                .addSelector(nodesHc1.get(0), nextPointer, nodesHc1.get(1))
                .addSelector(nodesHc1.get(1), previousPointer, nodesHc1.get(0))
                .build();

        TIntArrayList nodesHc2 = new TIntArrayList(3);
        HeapConfigurationBuilder hc2Builder = new InternalHeapConfiguration().builder()
                .addNodes(listElement, 3, nodesHc2)
                .setExternal(nodesHc2.get(0)).setExternal(nodesHc2.get(2))
                .addSelector(nodesHc2.get(0), nextPointer, nodesHc2.get(1))
                .addSelector(nodesHc2.get(1), previousPointer, nodesHc2.get(0));

        int nonTerminalEdge = hc2Builder.addNonterminalEdgeAndReturnId(list, TIntArrayList.wrap(new int[] {nodesHc2.get(1), nodesHc2.get(2)}));
        HeapConfiguration hc2 = hc2Builder.build();

        return new GrammarBuilder()
                .addRule(list, hc1)
                .addRule(list, hc2)
                .updateCollapsedRules()
                .build();
    }

    public static Grammar getLinkedTreeGrammar() {
        // Create Factories
        BasicNonterminal.Factory nonterminalFactory = new BasicNonterminal.Factory();
        BasicSelectorLabel.Factory selectorFactory = new BasicSelectorLabel.Factory();
        GeneralType.Factory typeFactory = new GeneralType.Factory();

        // Create required nonterminal, selectors and types
        Nonterminal nt = nonterminalFactory.create("LT", 3, new boolean[]{false, false, true});
        Type element = typeFactory.get("E");
        SelectorLabel left = selectorFactory.get("l");
        SelectorLabel right = selectorFactory.get("r");
        SelectorLabel next = selectorFactory.get("n");

        // Create required RHS
        TIntArrayList nodes1 = new TIntArrayList();
        HeapConfiguration hc1 = new InternalHeapConfiguration().builder()
                .addNodes(element, 7, nodes1)
                .setExternal(nodes1.get(0)).setExternal(nodes1.get(1)).setExternal(nodes1.get(2))
                .addSelector(nodes1.get(0), left, nodes1.get(3))
                .addSelector(nodes1.get(0), right, nodes1.get(4))
                .addSelector(nodes1.get(5), next, nodes1.get(6))
                .addNonterminalEdge(nt, TIntArrayList.wrap(new int[]{nodes1.get(3), nodes1.get(1), nodes1.get(5)}))
                .addNonterminalEdge(nt, TIntArrayList.wrap(new int[]{nodes1.get(4), nodes1.get(6), nodes1.get(2)}))
                .build();

        TIntArrayList nodes2 = new TIntArrayList();
        HeapConfiguration hc2 = new InternalHeapConfiguration().builder()
                .addNodes(element, 3, nodes2)
                .setExternal(nodes2.get(0)).setExternal(nodes2.get(1)).setExternal(nodes2.get(2))
                .addSelector(nodes1.get(0), left, nodes1.get(1))
                .addSelector(nodes1.get(0), right, nodes1.get(2))
                .addSelector(nodes1.get(1), next, nodes1.get(2))
                .build();

        return new GrammarBuilder()
                .addRule(nt, hc1)
                .addRule(nt, hc2)
                .updateCollapsedRules()
                .build();
    }

    public static Grammar getCyclicListGrammar() {
        // TODO
        return null;
    }

}
