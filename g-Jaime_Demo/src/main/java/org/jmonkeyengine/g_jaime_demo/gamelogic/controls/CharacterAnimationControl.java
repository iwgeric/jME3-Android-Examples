package org.jmonkeyengine.g_jaime_demo.gamelogic.controls;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.SkeletonControl;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class CharacterAnimationControl extends AbstractControl {
    private static final Logger logger = Logger.getLogger(CharacterAnimationControl.class.getName());
    private BetterCharacterControl characterPhysicsControl = null;
    private AnimChannel animChannel = null;

    public void setSpatial(Spatial spatial) {
        if (spatial == null) {
            if (characterPhysicsControl != null) {
                // TODO: is any additional cleanup required here?

                characterPhysicsControl = null;
            }
            if (animChannel != null) {
                // TODO: is any additional cleanup required here?

                animChannel = null;
            }

            return;
        }

        List<BetterCharacterControl> charControls = findCharacterControls(spatial);
        if (charControls.size() == 1) {
            characterPhysicsControl = charControls.get(0);
        } else {
            throw new IllegalStateException(
                    "Invalid number of character controls.  " +
                            "Character Animation Control not allowed " +
                            "with " + charControls.size() + " BetterCharacterControls"
            );
        }

        List<AnimControl> animControls = findAnimationControls(spatial);
        if (animControls.size() == 1) {
            AnimControl animControl = animControls.get(0);
            if (animControl.getNumChannels() == 0) {
                animChannel = animControl.createChannel();
                animChannel.setAnim("Idle");
            } else if (animControl.getNumChannels() == 1) {
                animChannel = animControl.getChannel(0);
                animChannel.setAnim("Idle");
            } else {
                throw new IllegalStateException(
                        "Invalid number of animation channels.  " +
                                "Character Animation Control not allowed " +
                                "with " + animControl.getNumChannels() + " Animation Channels"
                );

            }
        } else {
            throw new IllegalStateException(
                    "Invalid number of animation controls.  " +
                            "Character Animation Control not allowed " +
                            "with " + animControls.size() + " Animation Controls"
            );
        }

        logger.log(Level.INFO, "Character Animation Control setup with charPhysicsControl: {0}, animChannel: {1}",
                new Object[]{characterPhysicsControl, animChannel});


        List<SkeletonControl> skeletonControls = findSkeletonControls(spatial);
        if (skeletonControls.size() == 1) {
            SkeletonControl skeletonControl = skeletonControls.get(0);
            skeletonControl.setHardwareSkinningPreferred(true);
        } else {
            throw new IllegalStateException(
                    "Invalid number of character skeleton controls.  " +
                            "Character Animation Control not allowed " +
                            "with " + skeletonControls.size() + " SkeletonCharacterControls"
            );
        }

        spatial.setLodLevel(1);

    }


    @Override
    protected void controlUpdate(float tpf) {

        if (characterPhysicsControl.getWalkDirection().length() > 5) {
            if (!"Run".equals(animChannel.getAnimationName())) {
                animChannel.setAnim("Run");
            }
        } else if (characterPhysicsControl.getWalkDirection().length() > 0) {
            if(!"Walk".equals(animChannel.getAnimationName())) {
                animChannel.setAnim("Walk");
            }
        } else {
            if(!"Idle".equals(animChannel.getAnimationName())) {
                animChannel.setAnim("Idle");
            }
        }

    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {

    }


    private List<BetterCharacterControl> findCharacterControls(Spatial spatial) {
        final List<BetterCharacterControl> charControls = new ArrayList<BetterCharacterControl>();
        spatial.breadthFirstTraversal(new SceneGraphVisitor() {
            @Override
            public void visit(Spatial spatial) {
                BetterCharacterControl charControl = spatial.getControl(BetterCharacterControl.class);
                if (charControl != null) {
                    charControls.add(charControl);
                }
            }
        });

        return charControls;

    }

    private List<AnimControl> findAnimationControls(Spatial spatial) {
        final List<AnimControl> animControls = new ArrayList<AnimControl>();
        spatial.breadthFirstTraversal(new SceneGraphVisitor() {
            @Override
            public void visit(Spatial spatial) {
                AnimControl animControl = spatial.getControl(AnimControl.class);
                if (animControl != null) {
                    animControls.add(animControl);
                }
            }
        });

        return animControls;

    }

    private List<SkeletonControl> findSkeletonControls(Spatial spatial) {
        final List<SkeletonControl> skeletonControls = new ArrayList<SkeletonControl>();
        spatial.breadthFirstTraversal(new SceneGraphVisitor() {
            @Override
            public void visit(Spatial spatial) {
                SkeletonControl skeletonControl = spatial.getControl(SkeletonControl.class);
                if (skeletonControl != null) {
                    skeletonControls.add(skeletonControl);
                }
            }
        });

        return skeletonControls;

    }


}
