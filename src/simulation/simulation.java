package simulation;

import com.sun.j3d.utils.universe.SimpleUniverse;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.*;
import javax.vecmath.*;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import graphs.*;

import java.awt.GraphicsConfiguration;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;
import java.util.Observable;
import com.sun.j3d.loaders.objectfile.ObjectFile;


import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.media.j3d.Alpha;
import javax.media.j3d.AmbientLight;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.RotationInterpolator;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;

import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.objectfile.ObjectFile;
import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.universe.PlatformGeometry;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;

/**
 * Created by david on 23/04/16.
 */
public class simulation extends Observable implements Runnable {

    private Scene glider;

    private BranchGroup scene;
    public int numGliderPosUpdates; // Param used to store num times glider position updated.
    private final Vector3f minSimBounds = new Vector3f(-0.15f, -0.15f, 0.1f);
    private final Vector3f maxSimBounds = new Vector3f(0.15f, 0.15f, 0.2f);
    private int simFidelity;
    private Vector<Vector3f> gliderPositionPoints;
    private Vector3f startPoint;
    private Vector3f endPoint;
    private SimulationParameters simulationParameters;

    private boolean noTriangulate = false;
    private boolean noStripify = false;
    private double creaseAngle = 60.0;

    public simulation(int simFidelity) {


        this.simFidelity = simFidelity; // num steps between glider start and end position.

        this.glider = gliderLoader();

        // Get simulation start and end points.
        startPoint = getRandPointUsingSimBounds();
        endPoint = getRandPointUsingSimBounds();

        // Add graph observers for data updates.
        createGraphObservers();

        // Create instance for storing and computing simulation parameters.
        this.simulationParameters = new SimulationParameters();

        scene = createSceneGraph();
        numGliderPosUpdates = 0;
        scene.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);

        gliderPositionPoints = getSimGliderPositionPoints();

        // Create canvas to associate with universe
        //GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        //Canvas3D canvas = new Canvas3D(config);

        //SimpleUniverse u = new SimpleUniverse(canvas);
        SimpleUniverse u = new SimpleUniverse();

        OrbitBehavior orbit = new OrbitBehavior(u.getCanvas(), OrbitBehavior.REVERSE_ROTATE);
        orbit.setSchedulingBounds(new BoundingSphere());
        u.getViewingPlatform().setViewPlatformBehavior(orbit);
        //u.getViewingPlatform().setNominalViewingTransform();

        u.addBranchGraph(scene);

    }

    void createGraphObservers() {

        this.addObserver(new TimeGraph());
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
        float randX = minSimBounds.getX() + random.nextFloat() * (maxSimBounds.getX() - minSimBounds.getX());
        float randY = minSimBounds.getY() + random.nextFloat() * (maxSimBounds.getY() - minSimBounds.getY());
        float randZ = minSimBounds.getZ() + random.nextFloat() * (maxSimBounds.getZ() - minSimBounds.getZ());

        Vector3f randVector = new Vector3f(randX, randY, randZ);

        return randVector;
    }

    private BranchGroup createSceneGraph() {

        // Create the root of the branch graph
        BranchGroup objRoot = new BranchGroup();

        objRoot.addChild(this.createLandscape());
        objRoot.addChild(this.createGlider());

        // Bounding Sphere for light creation
        /*BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);

        Color3f light1Color = new Color3f(1.0f, 0.0f, 0.2f);
        Vector3f light1Direction = new Vector3f(4.0f, -7.0f, -12.0f);
        DirectionalLight light1 = new DirectionalLight(light1Color, light1Direction);
        light1.setInfluencingBounds(bounds);

        objRoot.addChild(light1);

        // Set up the ambient light
        Color3f ambientColor = new Color3f(1.0f, 1.0f, 1.0f);
        AmbientLight ambientLightNode = new AmbientLight(ambientColor);
        ambientLightNode.setInfluencingBounds(bounds);

        objRoot.addChild(ambientLightNode);*/

        return objRoot;
    }

    private TransformGroup createLandscape() {

        // Create a simple shape leaf node, add it to the scene graph.
        Appearance appearance = new Appearance();
        appearance.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
        appearance.setColoringAttributes(new ColoringAttributes(
                new Color3f(24.0f, 23.6f, 0.17f), ColoringAttributes.NICEST));

        Box box = new Box(0.3000f, 0.3000f, 0.0480f, appearance);

        TransformGroup objTrans = new TransformGroup();
        objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE); // Specifies that the node allows writing its object's transform information

        Transform3D pos1 = new Transform3D();
        pos1.setTranslation(new Vector3f(0.0f,0.0f,0.0f));

        // Set rotation.
        //Matrix3f rotMatrix = new Matrix3f(0f, 0f, 1f, 0f, 1f, 0f, -1f, 0f, 0f);
        //pos1.setRotation(rotMatrix);

        objTrans.setTransform(pos1);
        objTrans.addChild(box);

        return objTrans;

    }

    private TransformGroup createGlider() {

        // Create a simple shape leaf node, add it to the scene graph.
        Appearance appearance = new Appearance();
        appearance.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
        appearance.setColoringAttributes(new ColoringAttributes(
                new Color3f(23.5f, 23.4f, 20.6f), ColoringAttributes.NICEST));

        Box box = new Box(0.003000f, 0.003000f, 0.000480f, appearance);















        TransformGroup objTrans = new TransformGroup();
        objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE); // Specifies that the node allows writing its object's transform information

        Transform3D pos1 = new Transform3D();
        // Set starting position for glider
        pos1.setTranslation(startPoint);

        // Set rotation.
        //Matrix3f rotMatrix = new Matrix3f(0f, 0f, 1f, 0f, 1f, 0f, -1f, 0f, 0f);
        //pos1.setRotation(rotMatrix);

        objTrans.setTransform(pos1);
        //objTrans.addChild(box);

        BranchGroup gliderBranchGroup = glider.getSceneGroup();

        Node tempChild = glider.getSceneGroup().getChild(0);
        glider.getSceneGroup().removeAllChildren();


        objTrans.addChild(tempChild);


        return objTrans;

    }

    private Scene gliderLoader() {

        int flags = ObjectFile.RESIZE;
        if (!noTriangulate)
            flags |= ObjectFile.TRIANGULATE;
        if (!noStripify)
            flags |= ObjectFile.STRIPIFY;
        ObjectFile f = new ObjectFile(flags,
                (float) (creaseAngle * Math.PI / 180.0));
        Scene s = null;
        try {
            s = f.load("AssyFull.obj");
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
        return s;
    }

    public void updateGliderPosition(Vector3f position) {

        TransformGroup objTrans = new TransformGroup();
        objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE); // Specifies that the node allows writing its object's transform information

        // Create a simple shape leaf node, add it to the scene graph.
        Appearance appearance = new Appearance();
        appearance.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
        appearance.setColoringAttributes(new ColoringAttributes(
                new Color3f(23.5f, 23.4f, 20.6f), ColoringAttributes.NICEST));

        Box box = new Box(0.003000f, 0.003000f, 0.000480f, appearance);

        Transform3D pos1 = new Transform3D();
        pos1.setTranslation(position);

        // Set rotation.
        //Matrix3f rotMatrix = new Matrix3f(0f, 0f, 1f, 0f, 1f, 0f, -1f, 0f, 0f);
        //pos1.setRotation(rotMatrix);

        objTrans.setTransform(pos1);
        objTrans.addChild(box);

        // Create BranchGroup to house new glider object.
        BranchGroup childBranchGroup = new BranchGroup();
        childBranchGroup.addChild(objTrans);
        scene.addChild(childBranchGroup);

        numGliderPosUpdates++;

        System.out.println(scene.numChildren());

    }

    @Override
    public void run() {

        for(int i = 0; i < this.gliderPositionPoints.size(); i++) {

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                ;
            }

            Vector3f currentGliderPosition = this.gliderPositionPoints.get(i);

            HashMap<String, Float> currentSimParams = this.simulationParameters.
                    getCurrentSimulationParameters(currentGliderPosition);

            // Update graphs.
            setChanged();
            notifyObservers(currentSimParams);

            this.updateGliderPosition(currentGliderPosition);
        }

    }

    public static void main( String[] args ) {

        simulation currentSimulation = new simulation(30);
        currentSimulation.run();

        //AccelerationGraph test = new AccelerationGraph();

    }
}
