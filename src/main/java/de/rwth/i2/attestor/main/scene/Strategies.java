package de.rwth.i2.attestor.main.scene;

import de.rwth.i2.attestor.grammar.canonicalization.CanonicalizationStrategy;
import de.rwth.i2.attestor.grammar.concretization.FullConcretizationStrategy;
import de.rwth.i2.attestor.grammar.concretization.SingleStepConcretizationStrategy;
import de.rwth.i2.attestor.grammar.languageInclusion.LanguageInclusionStrategy;
import de.rwth.i2.attestor.grammar.materialization.MaterializationStrategy;
import de.rwth.i2.attestor.stateSpaceGeneration.AbortStrategy;
import de.rwth.i2.attestor.stateSpaceGeneration.ExplorationStrategy;
import de.rwth.i2.attestor.stateSpaceGeneration.StateLabelingStrategy;
import de.rwth.i2.attestor.stateSpaceGeneration.StateRefinementStrategy;

public class Strategies {

    private SingleStepConcretizationStrategy singleStepConcretizationStrategy;
    private FullConcretizationStrategy fullConcretizationStrategy;
    private MaterializationStrategy materializationStrategy;
    private CanonicalizationStrategy aggressiveCanonicalizationStrategy;
    private CanonicalizationStrategy lenientCanonicalizationStrategy;
    private ExplorationStrategy explorationStrategy;
    private AbortStrategy abortStrategy;
    private StateLabelingStrategy stateLabelingStrategy;
    private StateRefinementStrategy stateRefinementStrategy;
    private LanguageInclusionStrategy languageInclusionStrategy;

    public SingleStepConcretizationStrategy getSingleStepConcretizationStrategy() {

        return singleStepConcretizationStrategy;
    }

    public void setSingleStepConcretizationStrategy(SingleStepConcretizationStrategy singleStepConcretizationStrategy) {

        this.singleStepConcretizationStrategy = singleStepConcretizationStrategy;
    }

    public FullConcretizationStrategy getFullConcretizationStrategy() {

        return fullConcretizationStrategy;
    }

    public void setFullConcretizationStrategy(FullConcretizationStrategy fullConcretizationStrategy) {

        this.fullConcretizationStrategy = fullConcretizationStrategy;
    }

    public MaterializationStrategy getMaterializationStrategy() {

        return materializationStrategy;
    }

    public void setMaterializationStrategy(MaterializationStrategy materializationStrategy) {

        this.materializationStrategy = materializationStrategy;
    }

    public CanonicalizationStrategy getAggressiveCanonicalizationStrategy() {

        return aggressiveCanonicalizationStrategy;
    }

    public void setAggressiveCanonicalizationStrategy(CanonicalizationStrategy aggressiveCanonicalizationStrategy) {

        this.aggressiveCanonicalizationStrategy = aggressiveCanonicalizationStrategy;
    }

    public CanonicalizationStrategy getLenientCanonicalizationStrategy() {

        return lenientCanonicalizationStrategy;
    }

    public void setLenientCanonicalizationStrategy(CanonicalizationStrategy lenientCanonicalizationStrategy) {

        this.lenientCanonicalizationStrategy = lenientCanonicalizationStrategy;
    }

    public ExplorationStrategy getExplorationStrategy() {

        return explorationStrategy;
    }

    public void setExplorationStrategy(ExplorationStrategy explorationStrategy) {

        this.explorationStrategy = explorationStrategy;
    }

    public AbortStrategy getAbortStrategy() {

        return abortStrategy;
    }

    public void setAbortStrategy(AbortStrategy abortStrategy) {

        this.abortStrategy = abortStrategy;
    }

    public StateLabelingStrategy getStateLabelingStrategy() {

        return stateLabelingStrategy;
    }

    public void setStateLabelingStrategy(StateLabelingStrategy stateLabelingStrategy) {

        this.stateLabelingStrategy = stateLabelingStrategy;
    }

    public StateRefinementStrategy getStateRefinementStrategy() {

        return stateRefinementStrategy;
    }

    public void setStateRefinementStrategy(StateRefinementStrategy stateRefinementStrategy) {

        this.stateRefinementStrategy = stateRefinementStrategy;
    }

    public LanguageInclusionStrategy getLanguageInclusionStrategy() {

        return languageInclusionStrategy;
    }

    public void setLanguageInclusionStrategy(LanguageInclusionStrategy languageInclusionStrategy) {

        this.languageInclusionStrategy = languageInclusionStrategy;
    }
}