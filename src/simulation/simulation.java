package simulation;

import graphs.*;

import java.util.HashMap;
import java.util.Random;
import java.util.Vector;
import java.util.Observable;

import java.io.FileNotFoundException;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.*;

import javax.vecmath.Color3f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.loaders.objectfile.ObjectFile;
import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;

/**
 * Created by david on 23/04/16.
 */
public class simulation extends Observable implements Runnable {

    private final Vector3f MIN_SIM_BOUNDS = new Vector3f(-0.15f, -0.15f, 0.1f);
    private final Vector3f MAX_SIM_BOUNDS = new Vector3f(0.15f, 0.15f, 0.2f);

    private BranchGroup scene;
    private Shape3D glider; // Structure for storing glider obj file.
    public int numGliderPosUpdates; // Param used to store num times glider position updated.
    private int simFidelity; // Number of discrete simulation steps
    private Vector<Vector3f> gliderPositionPoints; // Array of glider positions that drive the simulation
    private Vector3f startPoint;
    private Vector3f endPoint;
    private SimulationParameters simulationParameters; // Reference to simulation parameters class.

    public simulation(int simFidelity) {

        this.simFidelity = simFidelity; // num steps between glider start and end position.

        // Load glider object
        this.glider = objLoader("AssyFull.obj");

        // Get simulation start and end points.
        startPoint = getRandPointUsingSimBounds();
        endPoint = getRandPointUsingSimBounds();

        // Add graph observers for data updates.
        createGraphObservers();

        // Create instance for storing and computing simulation parameters.
        this.simulationParameters = new SimulationParameters();

        // Create scene and add relevant capabilities
        scene = createSceneGraph();
        scene.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
        scene.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);

        numGliderPosUpdates = 0;

        gliderPositionPoints = getSimGliderPositionPoints();

        // Create canvas to associate with universe
        //GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        //Canvas3D canvas = new Canvas3D(config);

        //SimpleUniverse u = new SimpleUniverse(canvas);
        SimpleUniverse u = new SimpleUniverse();

        OrbitBehavior orbit = new OrbitBehavior(u.getCanvas(), OrbitBehavior.REVERSE_ROTATE);
        orbit.setSchedulingBounds(new BoundingSphere());
        u.getViewingPlatform().setViewPlatformBehavior(orbit);

        u.addBranchGraph(scene);

    }

    void createGraphObservers() {

        this.addObserver(new VelocityGraph());
        this.addObserver(new AccelerationGraph());
    }

    Vector<Vector3f> getSimGliderPositionPoints() {

        Vector<Vector3f> gliderPositionPoints = new Vector<Vector3f>();

        float deltaX = (startPoint.getX() - endPoint.getX()) / this.simFidelity;
        float deltaY = (startPoint.getY() - endPoint.getY()) / this.simFidelity;
        float deltaZ = (startPoint.getZ() - endPoint.getZ()) / this.simFidelity;

        // Populate glider positions for this simulation.
        for (int i = 0; i < simFidelity; i++) {

            float currentX = startPoint.getX() + (deltaX * (i + 1));
            float currentY = startPoint.getY() + (deltaY * (i + 1));
            float currentZ = startPoint.getZ() + (deltaZ * (i + 1));
            Vector3f point = new Vector3f(currentX, currentY, currentZ);

            gliderPositionPoints.add(point);
        }

        return gliderPositionPoints;

    }

    private Vector3f getRandPointUsingSimBounds() {

        Random random = new Random();
        float randX = MIN_SIM_BOUNDS.getX() + random.nextFloat() * (MAX_SIM_BOUNDS.getX() - MIN_SIM_BOUNDS.getX());
        float randY = MIN_SIM_BOUNDS.getY() + random.nextFloat() * (MAX_SIM_BOUNDS.getY() - MIN_SIM_BOUNDS.getY());
        float randZ = MIN_SIM_BOUNDS.getZ() + random.nextFloat() * (MAX_SIM_BOUNDS.getZ() - MIN_SIM_BOUNDS.getZ());

        Vector3f randVector = new Vector3f(randX, randY, randZ);

        return randVector;
    }

    private BranchGroup createSceneGraph() {

        // Create the root of the branch graph
        BranchGroup objRoot = new BranchGroup();

        objRoot.addChild(this.createLandscape());

        return objRoot;
    }

    private TransformGroup createLandscape() {

        Appearance appearance = new Appearance();
        appearance.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
        appearance.setColoringAttributes(new ColoringAttributes(
                new Color3f(24.0f, 23.6f, 0.17f), ColoringAttributes.NICEST));

        Shape3D environment = objLoader("mars_environ.obj");
        environment.setAppearance(appearance);

        TransformGroup objTrans = new TransformGroup();
        objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE); // Specifies that the node allows writing its object's transform information

        Transform3D pos1 = new Transform3D();
        pos1.setTranslation(new Vector3f(0.0f,0.0f,0.0f));

        objTrans.setTransform(pos1);

        objTrans.addChild(environment);

        return objTrans;

    }

    private Shape3D objLoader(String filename) {

        boolean noTriangulate = false;
        boolean noStripify = false;
        double creaseAngle = 60.0;

        int flags = ObjectFile.RESIZE;
        if (!noTriangulate)
            flags |= ObjectFile.TRIANGULATE;
        if (!noStripify)
            flags |= ObjectFile.STRIPIFY;
        ObjectFile f = new ObjectFile(flags,
                (float) (creaseAngle * Math.PI / 180.0));
        Scene s = null;
        try {
            s = f.load(filename);
        } catch (FileNotFoundException e) {
            System.err.println(e);
            System.exit(1);
        } catch (ParsingErrorException e) {
            System.err.println(e);
            System.exit(1);
        } catch (IncorrectFormatException e) {
            System.err.println(e);
            System.exit(1);
        }

        Shape3D tempChild = (Shape3D)s.getSceneGroup().getChild(0);
        s.getSceneGroup().removeAllChildren();

        return tempChild;
    }

    public void updateGliderPosition(Vector3f position) {

        TransformGroup objTrans = new TransformGroup();
        objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE); // Specifies that the node allows writing its object's transform information

        // Create a simple shape leaf node, add it to the scene graph.
        Appearance appearance = new Appearance();
        appearance.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
        appearance.setColoringAttributes(new ColoringAttributes(
                new Color3f(23.5f, 23.4f, 20.6f), ColoringAttributes.NICEST));

        // Update glider position.
        Transform3D pos1 = new Transform3D();
        pos1.setTranslation(position);

        pos1.setRotation(new Matrix3f(1, 0, 0,
                                    0, 0, -1,
                                    0, 1, 0));

        // Rescale the glider
        pos1.setScale(0.05);

        objTrans.setTransform(pos1);

        // Colour glider.
        glider.setAppearance(appearance);

        objTrans.addChild(this.glider.cloneTree());

        // Create BranchGroup to house new glider object.
        BranchGroup childBranchGroup = new BranchGroup();
        childBranchGroup.setCapability(BranchGroup.ALLOW_DETACH);
        childBranchGroup.addChild(objTrans);
        scene.addChild(childBranchGroup);

        numGliderPosUpdates++;

    }

    @Override
    public void run() {

        for(int i = 0; i < this.gliderPositionPoints.size(); i++) {

            Vector3f currentGliderPosition = this.gliderPositionPoints.get(i);

            HashMap<String, Float> currentSimParams = this.simulationParameters.
                    getCurrentSimulationParameters(currentGliderPosition);

            // Update graphs.
            setChanged();
            notifyObservers(currentSimParams);

            this.updateGliderPosition(currentGliderPosition);

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                ;
            }

            // Remove previous glider.
            scene.removeChild(1);
        }

    }

    public static void main( String[] args ) {

        simulation currentSimulation = new simulation(30);
        currentSimulation.run();

    }
}
