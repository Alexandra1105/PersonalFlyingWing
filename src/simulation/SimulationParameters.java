package simulation;

import java.util.HashMap;

import java.lang.Math;

import java.util.Random;

/**
 * Created by david on 23/04/16.
 */
public class SimulationParameters {

    private float previousPosition;
    private float previousVelocity;
    private float previousAcceleration;

    private final float COEF_LIFT = 1.1282f; // coefficient of lift
    private final float COEF_DRAG = 0.1096f; // coefficient of drag
    private final float DENSITY = 0.02f;
    private final float WING_AREA = 8.0f;
    private final float MASS = 350.0f; // mass kg.

    //private final float MAX_THRUST = 2600f; // Newtons.

    private final float DELTA_T = 1f; // 1 sec.


    SimulationParameters() {

        this.previousPosition = new Float(0);
        this.previousVelocity = new Float(0);
        this.previousAcceleration = new Float(0);
    }

    private Float getCurrentVelocity(Float currentGliderPosition) {

        return (currentGliderPosition - previousPosition) / DELTA_T;
    }

    private double getCurrentLift(Float currentGliderPosition) {

        double currentVelocity = getCurrentVelocity(currentGliderPosition);

        return 0.5f * COEF_LIFT * DENSITY * (Math.pow(currentVelocity, 2)) * WING_AREA;
    }

    private double getCurrentDrag(Float currentGliderPosition) {

        double currentVelocity = getCurrentVelocity(currentGliderPosition);

        return 0.5f * COEF_DRAG * DENSITY * (Math.pow(currentVelocity, 2)) * WING_AREA;
    }

    public HashMap<String, Float> getCurrentSimulationParameters(Float currentGliderPosition) {

        HashMap<String, Float> currentSimParams = new HashMap<>();

        // For purposes of simulation, generate random acc, vel and thrust values.
        Random random = new Random();

        float currentAcceleration = random.nextFloat() * 5.0f;
        float currentVelocity = random.nextFloat() * 200;
        float currentThrust = random.nextFloat() * 3000;

        // Place current params in data structure for consumption by graphs.
        currentSimParams.put("acceleration", currentAcceleration);
        currentSimParams.put("velocity", currentVelocity);
        currentSimParams.put("thrust", currentThrust);

        // Update internal params for next function call.
        this.previousAcceleration = currentAcceleration;
        this.previousVelocity = currentVelocity;

        return currentSimParams;

    }

}