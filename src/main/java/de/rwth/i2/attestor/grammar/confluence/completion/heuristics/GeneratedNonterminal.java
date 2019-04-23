package de.rwth.i2.attestor.grammar.confluence.completion.heuristics;

import de.rwth.i2.attestor.graph.Nonterminal;

import java.util.Arrays;

/**
 * A nonterminal that was generated to achieve confluence
 */
public class GeneratedNonterminal implements Nonterminal {
    private final int rank;
    private final int id;
    private final boolean[] isReductionTentacle;


    GeneratedNonterminal(int rank, boolean[] isReductionTentacle, int id) {
        this.rank = rank;
        this.id = id;
        this.isReductionTentacle = Arrays.copyOf(isReductionTentacle, rank);
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof GeneratedNonterminal) {
            GeneratedNonterminal otherGeneratedNonterminal = (GeneratedNonterminal) o;
            return otherGeneratedNonterminal.id == id;
        } else {
            return false;
        }
    }

    @Override
    public int getRank() {
        return rank;
    }

    @Override
    public boolean isReductionTentacle(int tentacle) {
        return isReductionTentacle[tentacle];
    }

    @Override
    public void setReductionTentacle(int tentacle) {
        isReductionTentacle[tentacle] = true;
    }

    @Override
    public void unsetReductionTentacle(int tentacle) {
        isReductionTentacle[tentacle] = false;
    }

    @Override
    public String getLabel() {
        return "X" + id;
    }


}
