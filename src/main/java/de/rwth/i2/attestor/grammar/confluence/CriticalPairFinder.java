package de.rwth.i2.attestor.grammar.confluence;

import de.rwth.i2.attestor.grammar.*;
import de.rwth.i2.attestor.grammar.confluence.benchmark.OverlappingStatisticCollector;
import de.rwth.i2.attestor.grammar.confluence.benchmark.StartStopTimer;
import de.rwth.i2.attestor.grammar.confluence.jointMorphism.*;
import org.json.JSONObject;

import java.util.*;


/**
 *
 * Computes the critical pairs of a grammar on construction.
 * Provides methods to access information about the critical pairs.
 *
 * The implemented algorithm to find the critical pairs is based on the work in: "Efficient Detection of Conflicts in
 * Graph-based Model Transformation" by Leen Lambers, Hartmut Ehrig & Fernando Orejas
 * TODO: How to correctly cite in javadoc
 *
 *
 * TODO: Add option to early abort if critical pairs already Strong joinable or weak joinable
 *
 * # Benchmarks:
 *
 * Runtime: Complete
 * For each grammar rule combination: Runtime Edge overlapping / Node overlapping / Complete
 * For each grammar rule combination: Pruning levels
 *
 * @author Johannes Schulte
 */
public class CriticalPairFinder {

    private Joinability joinabilityResult;
    final private Collection<CriticalPair> criticalPairs;
    final private NamedGrammar underlyingGrammar;
    private StartStopTimer timer; // Measure the time of the whole critical pair detection
    private OverlappingStatisticCollector edgeOverlappingStatistic = new OverlappingStatisticCollector();
    private OverlappingStatisticCollector nodeOverlappingStatistic = new OverlappingStatisticCollector();
    private OverlappingStatisticCollector validOverlappingStatistic = new OverlappingStatisticCollector();

    public CriticalPairFinder(NamedGrammar grammar) {
        this.underlyingGrammar = grammar;
        this.criticalPairs = new ArrayList<>();
        this.joinabilityResult = Joinability.STRONGLY_JOINABLE;

        computeAllCriticalPairs();
    }

    private void computeAllCriticalPairs() {
        timer = new StartStopTimer();
        timer.startTimer();

        // Add critical pairs for all getCombinations of rules

        // 1. Convert to a list to reference each rule by one index
        List<GrammarRule> individualGrammarRules = new ArrayList<>(underlyingGrammar.getActiveRules());

        // 2. Iterate over all pairs of individual grammar rules and add the critical pairs for each pair

        for (int i = 0; i < individualGrammarRules.size(); i++) {
            for (int j = i; j < individualGrammarRules.size(); j++) {
                GrammarRule r1 = individualGrammarRules.get(i);
                GrammarRule r2 = individualGrammarRules.get(j);
                addCriticalPairsForCollapsedRule(r1, r2);
            }
        }

        timer.stopTimer();
    }



    /**
     * This method computes all possible jointly surjective morphisms g1, g2 such that (g1: l1 -> s, g2: l2 -> s)
     * for the two right hand sides (l1, l2) of the rules r1, r2.
     * For each of these morphisms we check if it induces a critical pair.
     *
     * @param r1 The first rule
     * @param r2 The second rule
     */
    private void addCriticalPairsForCollapsedRule(GrammarRule r1,
                                                  GrammarRule r2) {
        CollapsedHeapConfiguration hc1 = r1.getCollapsedHeapConfiguration();
        CollapsedHeapConfiguration hc2 = r2.getCollapsedHeapConfiguration();
        HeapConfigurationContext context = new HeapConfigurationContext(hc1, hc2);

        // Start to measure time for the edge overlapping computation
        edgeOverlappingStatistic.startTimer();

        for (Overlapping eOverlapping : EdgeOverlapping.getEdgeOverlapping(context, edgeOverlappingStatistic)) {
            EdgeOverlapping edgeOverlapping = (EdgeOverlapping) eOverlapping;
            // Check if the current edgeOverlapping allows for compatible node overlappings
            if (edgeOverlapping.isEdgeOverlappingValid()) {

                // Pause edge overlapping time and start node overlapping time
                edgeOverlappingStatistic.stopTimer();
                nodeOverlappingStatistic.startTimer();
                for (Overlapping nOverlapping : NodeOverlapping.getNodeOverlapping(edgeOverlapping, nodeOverlappingStatistic)) {
                    // Found a compatible overlapping
                    NodeOverlapping nodeOverlapping = (NodeOverlapping) nOverlapping;

                    // Check that the rule applications are not independent (They should share at least one internal node)
                    if (!nodeOverlapping.isNodeOverlappingIndependent()) {
                        nodeOverlappingStatistic.stopTimer();
                        validOverlappingStatistic.startTimer();

                        CriticalPair newCriticalPair = new CriticalPair(nodeOverlapping, edgeOverlapping, underlyingGrammar, r1, r2);
                        if (!underlyingGrammar.blockHeapAbstraction(newCriticalPair.getJointHeapConfiguration().getHeapConfiguration())) {
                            // This is only a critical pair if the abstraction is not blocked by the grammar
                            criticalPairs.add(newCriticalPair);
                            joinabilityResult = joinabilityResult.getCollectiveJoinability(newCriticalPair.getJoinability());
                            validOverlappingStatistic.logPruning(nodeOverlapping.getLevel());
                        }
                        validOverlappingStatistic.stopTimer();
                        nodeOverlappingStatistic.startTimer();
                    }
                }

                // Continue edge overlapping time and stop node overlapping time
                nodeOverlappingStatistic.stopTimer();
                edgeOverlappingStatistic.startTimer();
            }
        }

        edgeOverlappingStatistic.stopTimer();
    }

    public Collection<CriticalPair> getCriticalPairs() {
        return Collections.unmodifiableCollection(criticalPairs);
    }

    /**
     * Returns the critical pairs that are joinable by at most the given joinability.
     * So if maxJoinability == WEAKLY_JOINABLE then only critical pairs that are not joinable or weakly joinable are returned.
     */
    public Collection<CriticalPair> getCriticalPairsMaxJoinability(Joinability maxJoinability) {
        Collection result = new ArrayList();
        for (CriticalPair criticalPair : criticalPairs) {
            if (criticalPair.getJoinability().getValue() <= maxJoinability.getValue()) {
                result.add(criticalPair);
            }
        }
        return result;
    }

    public Joinability getJoinabilityResult() {
        return joinabilityResult;
    }

    public JSONObject getJsonStatistic() {
        JSONObject result = new JSONObject();
        result.put("edgeOverlappingStatistic", edgeOverlappingStatistic.getJsonStatistic());
        result.put("nodeOverlappingStatistic", nodeOverlappingStatistic.getJsonStatistic());
        result.put("validOverlappingStatistic", validOverlappingStatistic.getJsonStatistic());
        return result;
    }
}
