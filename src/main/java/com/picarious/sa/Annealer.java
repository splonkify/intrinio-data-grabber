package com.picarious.sa;

import org.springframework.stereotype.Service;

@Service
public class Annealer {
    private static final int KMAX = 1000;

    public State search(NeighborGenerator neighborGenerator) {
        State currentState = neighborGenerator.initialState();
        for (int step = 0; step < KMAX; step++) {
            double temperature = calculateTemperature(step, KMAX);
            State candidateState = neighborGenerator.newStateFrom(currentState);
            if (acceptable(currentState, candidateState, temperature)) {
                currentState = candidateState;
            }
        }
        return currentState;
    }

    private boolean acceptable(State currentState, State candidateState, double temperature) {
        double rand = Math.random();
        double probability = Math.exp((currentState.energy() - candidateState.energy()) / temperature);
        if (probability >= rand) {
            return true;
        }
        return false;
    }

    private double calculateTemperature(int step, int kmax) {
        return kmax - step;
    }
}
