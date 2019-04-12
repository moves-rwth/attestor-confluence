package de.rwth.i2.attestor.grammar.typedness;

import de.rwth.i2.attestor.grammar.Grammar;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.SelectorLabel;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import gnu.trove.list.array.TIntArrayList;

import java.util.*;

/**
 * A class to express the type of one tentacle.
 */
public class TentacleType {
    private final GrammarTypedness grammarTypedness;
    private final Nonterminal nonterminal;
    private final int tentacle;
    private final List<TentacleType> dependencies;  // This type depends on those types which have not yet been fully calculated
    private final Set<SelectorLabel> immediateReachable;  // All types that can get generated through a single rule application
    private Set<SelectorLabel> allTypes;
    private boolean calculationInProgress;  // Used to resolve recursive dependency loops


    public TentacleType(GrammarTypedness grammarTypedness, Nonterminal nt, int tentacle) {
        this.calculationInProgress = false;
        this.grammarTypedness = grammarTypedness;
        this.nonterminal = nt;
        this.tentacle = tentacle;
        this.allTypes = null;  // Compute only when needed

        // Initialize immediateReachable and dependencies
        Grammar grammar = grammarTypedness.getGrammar();
        this.immediateReachable = new HashSet<>();
        this.dependencies = new ArrayList<>();
        for (HeapConfiguration rhs : grammar.getRightHandSidesFor(nt)) {
            int node = rhs.externalNodeAt(tentacle);
            immediateReachable.addAll(rhs.selectorLabelsOf(node));
            rhs.attachedNonterminalEdgesOf(node).forEach(ntEdge-> {
                Nonterminal ntDependency = rhs.labelOf(ntEdge);
                TIntArrayList attachedNodes = rhs.attachedNodesOf(ntEdge);
                int offset = 0;
                while (offset != -1) {
                    offset = attachedNodes.indexOf(offset, node);
                    if (offset != -1) {
                        // attachedNodes[offset] == node
                        dependencies.add(grammarTypedness.getTentacleType(nt, offset));
                        offset++;  // Search following indices
                    }
                }
                return true;
            });
        }

    }

    public Nonterminal getNonterminal() {
        return nonterminal;
    }

    public int getTentacle() {
        return tentacle;
    }

    public Set<SelectorLabel> getImmediateReachable() {
        return Collections.unmodifiableSet(immediateReachable);
    }

    public Set<SelectorLabel> getAllTypes() {
        if (allTypes == null) {
            allTypes = new HashSet<>();
            addDependencyTypes(allTypes);
        }
        return Collections.unmodifiableSet(allTypes);

    }

    private void addDependencyTypes(Set<SelectorLabel> selectorLabels) {
        if (!calculationInProgress) {  // Resolve cyclic dependencies
            calculationInProgress = true;

            selectorLabels.addAll(getImmediateReachable());
            for (TentacleType dependency : dependencies) {
                dependency.addDependencyTypes(selectorLabels);
            }

            calculationInProgress = false;
        }
    }

}
