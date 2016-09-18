package com.picarious.sa;

public interface NeighborGenerator {
    State newStateFrom(State currentState);

    State initialState();
}
