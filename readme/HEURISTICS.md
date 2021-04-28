# Completion Heuristics
Attestor's confluence checker implements multiple completion methods to generate a confluence data structure grammar, while not forcing to be language-equivalent. We will here now go into more detail how these heuristics work.

## Add Rules New Nonterminal
To fix violating critical pairs, we can try to introduce a new nonterminal and two rules that allow deriving the new nonterminal to each graph of the critical pair. The problem of this heuristic arises when selecting external nodes for the new rules. This selection can be arbtirary in general. For this heuristic we try to use one arbitrarily chosen external node. 

### Join Generated Nonterminals
Assuming we have generated two nonterminals in the previous heuristic, these two nonterminals may again lead to non-confluent behavior. This heuristic tries to fix this problem by renaming one of the nonterminals into the other. This is only possible if the rank of the symbols matches, which, if generated as above, will indeed always match.

### Single Nonterminal Rule Adding
A special case of violating critical pairs is when one of the graphs only consist of one nonterminal edge and no terminal edges. In this case, we do not need to generate a new nonterminal as for [Add Rules New Nonterminal](#add-rules-new-nonterminal), but can instead only add one rule that allows a deriviation of the one nonterminal edge to the other graph of the pair.

## Completion Rule Restriction
This heuristic is tailord to Attestor. Attestor generates collapsed rules, i.e. rules thare in which node in the original rule are merged together. These collapsed rules, however, may never be used while stil allowing non-confluence behavior. To fix this problem, this heuristic tries to remove collapsed rules that lead to non-confluent behavior.

## Completion Abstraction Blocking
A rather practical solution to fix non-conluent behavior, would be to just add additional constraints when derivations are allowed. By forbidding derivations that behavior non-confluent, we can enforce only confluent behavior in further derivation steps. As such, this heuristic does really make the grammar confluent, but instead can be used for further application in Attestor.

# Completion Algorithms

All allgorithmus (currently) operate greedily on critical pairs and try to resolve these critical pairs to yield confluence data structure grammars. Multiple algorithms only apply one heuristics exhaustively, namely:

- `addRulesNewNonterminalHeuristic`
- `joinGeneratedNonterminals`
- `singleNonterminalRuleAddingHeuristic`
- `ruleRestriction`
- `completionAbstractionBlocking`

For combined Algorithms, we have two versions. One in which we enforce a usefull property for verification, local concretizability and one in which we do not enforce these.

## Only Rule Adding
In this algorithm, we include all heuristics that only add rules, namely

- `addRulesNewNonterminalHeuristic`
- `joinGeneratedNonterminals`
- `singleNonterminalRuleAddingHeuristic`

## Combined Algorithm 1/2
The combined Algorithms use all of the heuristics in different orders. Namely we either perform rule restrictions first or we first try adding symbols and rules. Indeed, the order does matter in both, effectivity and performance.

# Reference
Johannes Schulte. Automated Detection and Completion of Confluence for Graph Grammars. Master Thesis at RWTH Aachen University, 2019.
