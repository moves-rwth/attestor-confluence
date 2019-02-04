package de.rwth.i2.attestor.grammar.confluence.JointMorphism;


/**
 * A joint morphism can be compatible, incompatible or can become compatible later.
 * INCOMPATIBLE: All JointMorphisms where the overlapping equivalences are are superset are not compatible
 * COMPATIBLE: The current JointMorphism is compatible
 * NOT_COMPATIBLE_YET: The current JointMorphism in incompatible, but there might be another JointMorphism with
 * *additional* equivalences that is compatible.
 */
public enum JointMorphismCompatibility {
    COMPATIBLE, INCOMPATIBLE, NOT_COMPATIBLE_YET;
}
