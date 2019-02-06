package de.rwth.i2.attestor.graph;

import de.rwth.i2.attestor.graph.digraph.NodeLabel;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class SelectorAsNonterminal implements Nonterminal {
    public static final String prefix = "@";
    private final String label;

    public SelectorAsNonterminal(SelectorLabel sel) {
        label = prefix + sel.getLabel();
    }

    @Override
    public int getRank() {
        return 2;
    }

    @Override
    public boolean isReductionTentacle(int tentacle) {
        throw new NotImplementedException();
    }

    @Override
    public void setReductionTentacle(int tentacle) {
        throw new NotImplementedException();
    }

    @Override
    public void unsetReductionTentacle(int tentacle) {
        throw new NotImplementedException();
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public boolean matches(NodeLabel other) {
        if (other instanceof SelectorAsNonterminal) {
            return label.equals(((SelectorAsNonterminal) other).getLabel());
        } else {
            return false;
        }
    }
}
