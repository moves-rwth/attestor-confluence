package de.rwth.i2.attestor.grammar.confluence;

public enum Joinability {
    STRONGLY_JOINABLE, WEAKLY_JOINABLE, NOT_JOINABLE;

    public int getValue() {
        switch (this) {
            case NOT_JOINABLE:
                return 0;
            case WEAKLY_JOINABLE:
                return 1;
            case STRONGLY_JOINABLE:
                return 2;
        }
        throw new RuntimeException("Unexpected Joinability");
    }

    private static Joinability getJoinability(int value) {
        switch (value) {
            case 0:
                return NOT_JOINABLE;
            case 1:
                return WEAKLY_JOINABLE;
            case 2:
                return STRONGLY_JOINABLE;
            default:
                throw new IllegalArgumentException("Unexpected value");
        }
    }

    public Joinability getCollectiveJoinability(Joinability otherJoinability) {
        return getJoinability(Math.min(this.getValue(), otherJoinability.getValue()));
    }
}