package com.picarious.sa;

import com.picarious.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class Annealer {
    private static final int KMAX = 1000;
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public State search(NeighborGenerator neighborGenerator) {
        double temperature = 1.0;
        double alpha = 0.99;
        double temp_min = 0.00001;
        State currentState = neighborGenerator.initialState();
        State best = currentState;
        for (int step = 0; step < KMAX; step++) {
            log.info("Temp: " + temperature);
            State candidateState = neighborGenerator.newStateFrom(currentState);
            if (acceptable(currentState, candidateState, temperature)) {
                currentState = candidateState;
                if (currentState.energy() < best.energy()) {
                    best = currentState;
                }
                log.info("Accepted: " + candidateState.toString());
            } else {
                log.info("Rejected: " + candidateState.toString());
            }
            temperature = temperature * alpha;
            if (temperature <= temp_min) {
                break;
            }
        }
        return best;
    }

    private boolean acceptable(State currentState, State candidateState, double temperature) {
        double rand = Math.random();
        int currentEnergy = currentState.energy();
        int candidateEnergy = candidateState.energy();
        if (candidateEnergy < currentEnergy) {
            return true;
        }
        double ratio = (currentEnergy - candidateEnergy) / (temperature * 100);
        double probability = Math.exp(ratio);
        if (probability >= rand) {
            return true;
        }
        return false;
    }

    private double calculateTemperature(int step, int kmax) {
        return ((double) kmax - (double) step) / (double) kmax;
    }
}
