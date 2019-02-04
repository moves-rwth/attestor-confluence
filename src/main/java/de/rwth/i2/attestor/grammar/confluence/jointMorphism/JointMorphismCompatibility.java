package de.rwth.i2.attestor.grammar.confluence.jointMorphism;


/**
 * A joint morphism can be compatible, incompatible or can become compatible later.
 * INCOMPATIBLE: All JointMorphisms where the overlapping equivalences are are superset are not compatible
 * COMPATIBLE: The current jointMorphism is compatible
 * NOT_COMPATIBLE_YET: The current jointMorphism in incompatible, but there might be another jointMorphism with
 * *additional* equivalences that is compatible.
 */
public enum JointMorphismCompatibility {
    COMPATIBLE, INCOMPATIBLE, NOT_COMPATIBLE_YET;
}
