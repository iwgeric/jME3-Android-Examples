package org.jmonkeyengine.g_jaime_demo.gamelogic.Input;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.input.InputManager;
import com.jme3.input.TouchInput;
import com.jme3.input.controls.TouchListener;
import com.jme3.input.controls.TouchTrigger;
import com.jme3.input.event.TouchEvent;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;

import org.jmonkeyengine.g_jaime_demo.gamelogic.Main;
import org.jmonkeyengine.g_jaime_demo.gamelogic.UserSettings;
import org.jmonkeyengine.g_jaime_demo.gamelogic.controls.CustomChaseCamera;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class to deal with receiving touch input events from InputManager.
 * Includes a dpad image displayed on the screen and creates motion commands based on where
 * on the image the touch event happens.  If the touch event is not over the dpad, the
 * touch is treated as a request to rotate the camera.
 *
 */
public class TouchInputHandler extends BaseAppState implements TouchListener {
    private static final Logger logger = Logger.getLogger(TouchInputHandler.class.getName());

    private UserSettings userSettings = null;
    private InputManager inputManager;
    private Node guiNode = null;
    private SelectablePicture dpad = null;

    private CustomChaseCamera camera = null;
    private Quaternion horizCameraRotation = new Quaternion();
    private BetterCharacterControl phyControl = null;
    private Vector3f walkDirection = new Vector3f();
    private Vector3f lookDirection = new Vector3f();
    private Vector3f curWalkDirection = new Vector3f();
    private Vector3f curLookDirection = new Vector3f();
    private float maxRotation = 1f;
    private float[] camAngles = new float[3];

    /**
     * Main constructor that passes in the main spatial that contains the character control.  Touch
     * events from the dpad will send motion commands to the character control found in this spatial.
     * @param character Spatial that contains the physics character control.
     */
    public TouchInputHandler(Spatial character) {
        camera = findChaseCamera(character);
        phyControl = findCharacterControl(character);
    }

    /**
     * Method to change the spatial that contains the physics character control.
     * @param character Spatial that contains the physics character control.
     */
    public void setCharacter(Spatial character) {
        camera = findChaseCamera(character);
        phyControl = findCharacterControl(character);
    }

    /**
     * When initialize is called by the engine, store references, create the inputManager touch
     * trigger to receive touch events, and initialize the dpad.
     * @param app
     */
    @Override
    protected void initialize(Application app) {
        userSettings = UserSettings.getInstance();
        this.inputManager = app.getInputManager();
        this.guiNode = ((Main)app).getGuiNode();

        app.getInputManager().addMapping("TouchInputHandler", new TouchTrigger(TouchInput.ALL));

        int screenWidth = app.getContext().getSettings().getWidth();
        int screenHeight = app.getContext().getSettings().getHeight();
        initDpad(app.getAssetManager(), screenWidth, screenHeight);

    }

    /**
     * When cleanup is called by the engine, remove the dpad from the scene and remove the inputManager
     * mapping.
     * @param app
     */
    @Override
    protected void cleanup(Application app) {
        if (dpad != null) {
            dpad.removeFromParent();
            dpad = null;
        }
        app.getInputManager().deleteMapping("TouchInputHandler");
        userSettings = null;
    }

    /**
     * When onEnable is called by the engine, display the dpad and add the listener to inputManager
     * to receive the touch events.
     */
    @Override
    protected void onEnable() {
        if (dpad != null) {
            guiNode.attachChild(dpad);
        }
        inputManager.addListener(this, "TouchInputHandler");
    }

    /**
     * When onDisable is called by the engine, remove the dpad display and remove the touch listener
     * from inputManager.
     */
    @Override
    protected void onDisable() {
        if (dpad != null) {
            dpad.removeFromParent();
        }
        inputManager.removeListener(this);
    }

    /**
     * During update, set the walkDirection and viewDirection based on the dpad settings calculated
     * during the last touch event.
     * @param tpf
     */
    @Override
    public void update(float tpf) {
        super.update(tpf);
        if (dpad != null && dpad.getActivePointer() != null) {
            curWalkDirection.set(walkDirection);
            curWalkDirection.multLocal(userSettings.getCharacterMaxSpeed());

            // create a quat level with the ground
            camera.getCameraRotation().toAngles(camAngles);
            camAngles[0] = 0f;
            horizCameraRotation.fromAngles(camAngles).normalizeLocal();

            // rotate walk direction based on camera rotation level with the ground
            horizCameraRotation.multLocal(curWalkDirection);

            // set physics control to rotated walk direction
            phyControl.setWalkDirection(curWalkDirection);
            phyControl.setViewDirection(curWalkDirection);

        } else {
            // dpad is not active so set the physics to zero motion
            // (leave view direction as is)
            phyControl.setWalkDirection(Vector3f.ZERO);
        }
    }

    /**
     * onTouch is called by the engine during touch events.  Here is where we calculate the character
     * motion and facing direction if the touch event is related to the dpad.  If not, then the
     * touch event is treated as a request to rotate the camera view.
     * @param name
     * @param event
     * @param tpf
     */
    @Override
    public void onTouch(String name, TouchEvent event, float tpf) {
//        logger.log(Level.INFO, "onTouch type:{0}, pointer: {1}, X: {2}, Y: {3}, phyControl: {4}, camera: {5}",
//                new Object[]{event.getType(), event.getPointerId(), event.getX(), event.getType(), phyControl, camera});

        if (phyControl == null || camera == null) {
            return;
        }

        switch (event.getType()) {
            case DOWN:
                logger.log(Level.INFO, "onTouch Down Event, event.pointer: {0}, dpad.pointer: {1}",
                        new Object[]{event.getPointerId(), dpad.getActivePointer()});
                if (dpad.getActivePointer() != null) {
                    // dpad is already being controlled by some other pointer (ie. different finger)
                    return;
                } else {
                    // Record the pointer used on this down event for future comparison.
                    // Important because of multi-touch support.
                    // Check the position to see if the down event is inside the dpad.  If it is,
                    // use the position to determine the speed and direction of motion.
                    if (dpad.checkSelect(event.getX(), event.getY())) {
                        dpad.setActivePointer(event.getPointerId());

                        Vector2f strength = dpad.getLocationRatioFromCenter(event.getX(), event.getY());
                        walkDirection.set(-strength.x, 0f, strength.y);
                        lookDirection.set(walkDirection);
                    } else {
                        logger.log(Level.INFO, "Touch not in dpad");
                    }
                }
                break;
            case UP:
                logger.log(Level.INFO, "onTouch Up Event, event.pointer: {0}, dpad.pointer: {1}",
                        new Object[]{event.getPointerId(), dpad.getActivePointer()});
                // If the pointerID matches the one recorded in the Down event, reset
                // the walkDirection and stop the character motion.
                // Don't set the lookDirection so they character stays looking in the same direction
                if (dpad.getActivePointer() != null && dpad.getActivePointer() == event.getPointerId()) {
                    dpad.setActivePointer(null);
                    walkDirection.set(0f, 0f, 0f);
                }
                break;
            case SCROLL:
//                logger.log(Level.INFO, "onTouch SCROLL for pointerID:{0}", event.getPointerId());
                // If the current pointer is the same as the one recorded previously, then
                // use the touch location to determine the walk direction and speed.
                // Use SCROLL events to detect finger motion while pressing down.
                if (dpad.getActivePointer() != null && dpad.getActivePointer() == event.getPointerId()) {
                    Vector2f strength = dpad.getLocationRatioFromCenter(event.getX(), event.getY());
                    walkDirection.set(-strength.x, 0f, strength.y);
                    lookDirection.set(walkDirection);
                }
                break;
            default:

        }

    }

    /**
     * Method to initialize the dpad image.  The SelectablePicture class is used to allow for some
     * convenience methods for determining motion speed and direction based on the touch location
     * relative to the center of the dpad.
     * @param assetManager
     * @param screenWidth
     * @param screenHeight
     */
    private void initDpad(AssetManager assetManager, int screenWidth, int screenHeight) {
        // Size the dpad image to be 50% of the smallest dimension
        // (width or height based on screen orientation)
        float picWidth;
        float picHeight;
        if (screenWidth >= screenHeight) {
            picHeight = picWidth = screenHeight * 0.5f;
        } else {
            picHeight = picWidth = screenWidth * 0.5f;
        }
        int zDepth = 5; // positive z value used to make sure the image is visible over other items

        dpad = new SelectablePicture("Dpad", false);
        dpad.setImage(assetManager, "Interface/Dpad.png", true);
        dpad.setWidth(picWidth);
        dpad.setHeight(picHeight);
        dpad.setLocalTranslation(0f, 0f, zDepth); // set location of bottom-left corner of pic to bottom-left of screen
    }

    /**
     * Method to search the spatial and all its children (if any exist) for a CustomChaseCamera control.
     * @param spatial Spatial to search (and its children if a Node)
     * @return CustomChaseCamera control found
     */
    private CustomChaseCamera findChaseCamera(Spatial spatial) {
        if (spatial == null) {
            return null;
        }

        final List<CustomChaseCamera> chaseCameras = new ArrayList<CustomChaseCamera>();
        spatial.depthFirstTraversal(new SceneGraphVisitor() {
            @Override
            public void visit(Spatial spatial) {
                CustomChaseCamera tmpCamera = spatial.getControl(CustomChaseCamera.class);
                if (tmpCamera != null) {
                    chaseCameras.add(tmpCamera);
                }
            }
        });
        if (chaseCameras.isEmpty() || chaseCameras.size() > 1) {
            throw new IllegalArgumentException(
                    "Invalid number of CustomChaseCameras detected [" + chaseCameras.size() + "]");
        }
        return chaseCameras.get(0);
    }

    /**
     * Method to search a spatial (and all its children if a Node) for a physics BetterCharacterControl.
     * Used to set motion speed, direction, and facing direction of the character.
     * @param spatial Spatial to search (and its children if a Node)
     * @return BetterCharacterControl found.
     */
    private BetterCharacterControl findCharacterControl(Spatial spatial) {
        if (spatial == null) {
            return null;
        }

        final List<BetterCharacterControl> charControls = new ArrayList<BetterCharacterControl>();
        spatial.depthFirstTraversal(new SceneGraphVisitor() {
            @Override
            public void visit(Spatial spatial) {
                BetterCharacterControl tmpCharControl = spatial.getControl(BetterCharacterControl.class);
                if (tmpCharControl != null) {
                    charControls.add(tmpCharControl);
                }
            }
        });
        if (charControls.isEmpty() || charControls.size() > 1) {
            throw new IllegalArgumentException(
                    "Invalid number of BetterCharacterControls detected [" + charControls.size() + "]");
        }
        return charControls.get(0);
    }

}
