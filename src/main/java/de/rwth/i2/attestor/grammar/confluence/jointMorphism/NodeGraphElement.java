package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

public class NodeGraphElement extends GraphElement {
    public NodeGraphElement(int privateId) {
        super(privateId, null);
    }

    @Override
    public String toString() {
        return "node" + getPrivateId();
    }
}
