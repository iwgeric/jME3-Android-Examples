package org.jmonkeyengine.g_jaime_demo.gamelogic.assetloader;

import com.jme3.app.Application;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.scene.Spatial;
import com.jme3.scene.UserData;

import org.jmonkeyengine.g_jaime_demo.gamelogic.controls.CustomChaseCamera;
import org.jmonkeyengine.g_jaime_demo.gamelogic.controls.CharacterAnimationControl;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of LoadTask to load the scene and Jaime character.  Creates the physics controls
 * on the separate thread, but adds the controls to the physicsSpace during onLoadComplete from the
 * main render thread.  The OpenGL data is also pre-loaded from the main render thread to avoid
 * hesitations when the objects are eventually added to the scene graph.
 *
 */
public class SceneDataLoadTask implements LoadTask {
    private static final Logger logger = Logger.getLogger(SceneDataLoadTask.class.getName());

    private final String worldFileName = "Scenes/World1.j3o";
    private final String sceneNodeName = "Scene";
    private final String navMeshName = "NavMesh";
    private final String groundName = "Ground";
    private final String otherObjectsNodeName = "SceneObjects";
    private final String mainCharacterName = "Jaime";

    private SceneLoadData loadData = null;

    public SceneLoadData getLoadData() {
        return loadData;
    }

    @Override
    public Object load(Application app) {
        if (loadData != null) {
            throw new IllegalStateException("Scene is already loaded!");
        }

        SceneLoadData loadData = new SceneLoadData();

        BulletAppState bulletAppState = app.getStateManager().getState(BulletAppState.class);
        PhysicsSpace physicsSpace = (bulletAppState != null)? bulletAppState.getPhysicsSpace(): null;
        if (physicsSpace == null) {
            throw new IllegalArgumentException("PhysicsSpace could not be found!");
        }

        // Load main j3o that includes the entire world.
        loadData.world = (Node)app.getAssetManager().loadModel(worldFileName);
        if (loadData.world == null) {
            throw new IllegalArgumentException("worldFileName[" + worldFileName + "] did not load.");
        }

        // Get the static scene objects
        loadData.scene = (Node)loadData.world.getChild(sceneNodeName);
        if (loadData.scene == null) {
            throw new IllegalArgumentException("scene " + sceneNodeName + " was not found.");
        }
        // create mesh collision shape around static scene
        // NavMesh Geometry has JmePhysicsIgnore UserData so it will not
        //   be included in the collision shape
        createIndivMeshRigidBodies(loadData.scene, 0f, true);

        // Get the NavMesh from the scene node
        loadData.navMesh = (Geometry)loadData.scene.getChild(navMeshName);
        if (loadData.navMesh == null) {
            throw new IllegalArgumentException("navMesh " + navMeshName + " was not found.");
        }
        // Get the Ground from the scene node
        loadData.ground = loadData.scene.getChild(groundName);
        if (loadData.ground == null) {
            throw new IllegalArgumentException("ground " + groundName + " was not found.");
        }

        // Get the node that contains dynamic objects placed around the scene
        loadData.otherObjects = (Node)loadData.world.getChild(otherObjectsNodeName);
        if (loadData.otherObjects == null) {
            logger.log(Level.SEVERE, "otherObjects {0} was not found", otherObjectsNodeName);
            loadData.otherObjects = new Node("EmptyOtherObjects");
        }
        // For each child in the otherObjects node, create a dynamic physics RigidBodyControl
        //   with a mass of 5kg.
        createIndivMeshRigidBodies(loadData.otherObjects, 5f, true);

        // Get the main character node from the world node and create a CharacterHandler
        loadData.mainCharacter = (Node)loadData.world.getChild(mainCharacterName);
        if (loadData.mainCharacter == null) {
            throw new IllegalArgumentException("mainCharacter " + mainCharacterName + " was not found.");
        }
        configureCharacterControls(app, loadData.mainCharacter);


        return loadData;
    }

    /**
     * Called from the asset loaded after the main load is complete.  This method is called
     * on the main render thread after the assets are loaded on a separate thread.
     * @param app  Application object
     * @param object Loaded object created during the load method
     */
    @Override
    public void onDoneLoading(Application app, Object object) {
        SceneLoadData loadData = (SceneLoadData)object;

        BulletAppState bulletAppState = app.getStateManager().getState(BulletAppState.class);
        PhysicsSpace physicsSpace = (bulletAppState != null)? bulletAppState.getPhysicsSpace(): null;
        if (physicsSpace == null) {
            throw new IllegalArgumentException("PhysicsSpace could not be found!");
        }

//        if (loadData.scene != null) {
//            // Add all the rigid bodies to the physics space
//            logger.log(Level.INFO, "Adding all rigid bodies in {0} to PhysicsSpace", loadData.scene.getName());
//            physicsSpace.addAll(loadData.scene);
//        }
//
//        if (loadData.otherObjects != null) {
//            // Add all the rigid bodies to the physics space
//            logger.log(Level.INFO, "Adding all rigid bodies in {0} to PhysicsSpace", loadData.otherObjects.getName());
//            physicsSpace.addAll(loadData.otherObjects);
//        }

        if (loadData.world != null) {
            // Add all the rigid bodies to the physics space
            logger.log(Level.INFO, "Adding all rigid bodies in {0} to PhysicsSpace", loadData.world.getName());
            physicsSpace.addAll(loadData.world);
        }

        if (loadData.mainCharacter != null) {
            CustomChaseCamera chaseCamera = loadData.getMainCharacter().getControl(CustomChaseCamera.class);
            chaseCamera.setPhysicsSpace(physicsSpace);
        }

        // preloadScene is done here to send the Android Bitmap textures
        //   to OpenGL and then recycle the Android Bitmap images
        // This helps remove game hesitations when bringing a texture into
        //   view for the first time since the image is already loaded to OpenGL
        app.getRenderManager().preloadScene(loadData.getWorld());

        this.loadData = loadData;
    }

    @Override
    public boolean isLoaded() {
        return (loadData != null);
    }

    private void createIndivMeshRigidBodies(final Spatial spatial, final float mass,
            final boolean enableLogging) {

        SceneGraphVisitorAdapter v = new SceneGraphVisitorAdapter() {
            @Override
            public void visit(Node node) {
                // Skip creating rigid body of Nodes, only do the Geometries
                // Allows bullet to take advantage of broadphase detection
            }

            @Override
            public void visit(Geometry geometry) {
                Boolean bool = geometry.getUserData(UserData.JME_PHYSICSIGNORE);
                if (bool != null && bool.booleanValue()) {
                    logger.log(Level.INFO, "rigid body skipped for {0}", geometry.getName());
                    return;
                }

                CollisionShape colShape;
                if (mass > 0) {
                    colShape = CollisionShapeFactory.createDynamicMeshShape(geometry);
                } else {
                    colShape = CollisionShapeFactory.createMeshShape(geometry);
                }

                RigidBodyControl rigidBodyControl = new RigidBodyControl(colShape, mass);
                geometry.addControl(rigidBodyControl);
                logger.log(Level.INFO, "Created Physics Control: {0}", rigidBodyControl);
            }

        };

//        spatial.breadthFirstTraversal(v);
        spatial.depthFirstTraversal(v);
    }

    private void configureCharacterControls(Application app, Spatial spatial) {
        if (spatial.getWorldBound() instanceof BoundingBox) {
            BoundingBox bb = (BoundingBox)spatial.getWorldBound();
//            lookAtOffset.set(Vector3f.UNIT_Y.mult(bb.getYExtent()*2f));
            float radius = Math.min(bb.getXExtent(), bb.getZExtent());
            float height = bb.getYExtent();
            height = Math.max(height, radius*2.5f);
            float mass = 50f;
            BetterCharacterControl charPhysicsControl = new BetterCharacterControl(radius, height, mass);
            charPhysicsControl.setViewDirection(spatial.getWorldRotation().mult(Vector3f.UNIT_Z));
            charPhysicsControl.setJumpForce(Vector3f.UNIT_Y.mult(mass * 2f));
            spatial.addControl(charPhysicsControl);
            logger.log(Level.INFO, "Added Character Control with Height: {0}, radius: {1}, mass: {2}",
                    new Object[]{height, radius, mass});

            spatial.addControl(new CharacterAnimationControl());
            spatial.setShadowMode(RenderQueue.ShadowMode.Cast);

            CustomChaseCamera chaseCamera = new CustomChaseCamera(app.getCamera(), spatial);
            chaseCamera.setLookAtOffset(new Vector3f(0f, height, 0f));
            chaseCamera.setDefaultDistance(height * 4f);
//            chaseCamera.setDefaultDistance(50f);
            chaseCamera.setMaxDistance(50f);
            chaseCamera.setMinDistance(2f);
            chaseCamera.setDefaultVerticalRotation(FastMath.QUARTER_PI / 3f);
            chaseCamera.setMaxVerticalRotation(FastMath.HALF_PI - 0.1f);
            chaseCamera.setMinVerticalRotation(0f);
            chaseCamera.setDefaultHorizontalRotation(FastMath.HALF_PI);
            chaseCamera.setSmoothMotion(false);
            chaseCamera.setRotationSensitivity(3f);
//            chaseCamera.setRotationSensitivity(rotateSpeed);
//            chaseCamera.setZoomSensitivity(zoomSpeed);
//            customChaseCam.setChasingSensitivity(0.7f);
//            customChaseCam.setTrailingEnabled(false);

        } else {
            logger.log(Level.INFO, "WorldBound is not a BoundingBox, Character Control not created.");
        }
    }



    public class SceneLoadData {
        private Node world;
        private Node scene;
        private Geometry navMesh;
        private Spatial ground;
        private Node otherObjects;
        private Node mainCharacter;

        public Node getWorld() { return world; }
        public Node getScene() { return scene; }
        public Geometry getNavMesh() { return navMesh; }
        public Spatial getGround() { return ground; }
        public Node getOtherObjects() { return otherObjects; }
        public Node getMainCharacter() { return mainCharacter; }
    }
}
