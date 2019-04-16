package de.rwth.i2.attestor.grammar.typedness;

import de.rwth.i2.attestor.grammar.Grammar;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.SelectorLabel;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.util.Pair;

import java.util.*;

/**
 * A class to express the type of one tentacle.
 */
public class TentacleType {
    private final GrammarTypedness grammarTypedness;
    private final Nonterminal nonterminal;
    private final int tentacle;
    private Set<TentacleType> dependencies;
    private Set<SelectorLabel> immediateReachable;  // All types that can get generated through a single rule application
    private Set<SelectorLabel> allTypes;
    private boolean calculationInProgress;  // Used to resolve recursive dependency loops


    public TentacleType(GrammarTypedness grammarTypedness, Nonterminal nt, int tentacle) {
        this.calculationInProgress = false;
        this.nonterminal = nt;
        this.tentacle = tentacle;
        this.allTypes = null;  // Compute only when needed
        this.grammarTypedness = grammarTypedness;
        this.dependencies = null;
        this.immediateReachable = null;
    }

    private void initializeDependencies() {
        if (dependencies == null) {
            // Initialize immediateReachable and dependencies
            Grammar grammar = grammarTypedness.getGrammar();
            immediateReachable = new HashSet<>();
            dependencies = new HashSet<>();
            for (HeapConfiguration rhs : grammar.getRightHandSidesFor(nonterminal)) {
                int node = rhs.externalNodeAt(tentacle);
                immediateReachable.addAll(rhs.selectorLabelsOf(node));
                for (Pair<Nonterminal, Integer> ntTentacle : GrammarTypedness.getConnectedTentacles(rhs, node)) {
                    dependencies.add(grammarTypedness.getTentacleType(ntTentacle.first(), ntTentacle.second()));
                }
            }
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

            initializeDependencies();
            selectorLabels.addAll(getImmediateReachable());
            for (TentacleType dependency : dependencies) {
                dependency.addDependencyTypes(selectorLabels);
            }

            calculationInProgress = false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(nonterminal.hashCode(), tentacle);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TentacleType) {
            TentacleType otherTentacleType = (TentacleType) o;
            return nonterminal.equals(otherTentacleType.nonterminal) && tentacle == otherTentacleType.tentacle;
        } else {
            return false;
        }
    }
}
