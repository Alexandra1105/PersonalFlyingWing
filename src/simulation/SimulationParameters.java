package simulation;

import org.jfree.data.time.Millisecond;

import java.util.HashMap;
import java.util.Vector;
import javax.vecmath.*;

import static java.lang.System.currentTimeMillis;

/**
 * Created by david on 23/04/16.
 */
public class SimulationParameters {

    private Vector3f previousPosition;
    private Vector3f previousVelocity;
    private Vector3f previousAcceleration;

    private final float Cl = 1.1282f;
    private final float Cd = 0.1096f;
    private final float p = 0.02f;
    private final float S = 8.0f;
    private final float m = 350.0f; // mass kg.
    private final float T = 2600f; // Newtons.

    private final float delta_t = 1f; // 1 sec.


    SimulationParameters() {

        this.previousPosition = new Vector3f();
        this.previousVelocity = new Vector3f();
        this.previousAcceleration = new Vector3f();
    }

    private Vector3f getCurrentAcceleration() {

        Vector3f acceleration = new Vector3f();

        Vector3f tempVector = new Vector3f(this.calculateL() / m,
                (this.T - calculateD()) / m,
                0);

        tempVector.sub(new Vector3f(0f, 0f, -0.37f));

        acceleration.add(tempVector);






        return acceleration;
    }

    private Vector3f getCurrentVelocity() {

        Vector3f velocity = new Vector3f();
        Vector3f tempVector;

        tempVector = previousAcceleration;
        tempVector.scale(delta_t);

        velocity.add(previousVelocity);
        velocity.add(tempVector);

        return velocity;
    }

    private float getCurrentTime() {

        float time = 0f;
        //TODO
        ;

        return time;
    }

    private float calculateL() {



        return 0f;
    }

    private float calculateD() {



        return 0f;
    }

    public HashMap<String, Float> getCurrentSimulationParameters(Vector3f currrentGliderPosition) {

        HashMap<String, Float> currentSimParams = new HashMap<>();

        Vector3f currentAcceleration = this.getCurrentAcceleration();
        Vector3f currentVelocity = this.getCurrentVelocity();
        float currentTime = this.getCurrentTime();






        // Testing code
        currentAcceleration = new Vector3f(2f, 2f, 2f);

        // Testing code
        currentVelocity = new Vector3f(5f, 5f, 5f);







        currentSimParams.put("acceleration", currentAcceleration.length());
        currentSimParams.put("velocity", currentVelocity.length());
        currentSimParams.put("time", currentTime);



        // Update internal params for next function call.
        this.previousAcceleration = currentAcceleration;
        this.previousVelocity = currentVelocity;

        return currentSimParams;

    }

}
