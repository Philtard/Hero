package io.horrorshow.renderer;

import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.utils.Array;
import io.horrorshow.objects.Guy;
import io.horrorshow.state.Direction;

import java.util.HashMap;
import java.util.Map;

import static io.horrorshow.Hero.PPM;
import static io.horrorshow.state.Direction.*;

public class PlayerRenderer implements Renderer {

    public static final Direction[] TEX_Y_DIR_ORDER =
            {DOWN, RIGHT, UP, LEFT};

    private final Map<Direction, TextureRegion> standTexture = new HashMap<>();
    private final Map<Direction, Animation<TextureRegion>> walkAnimations = new HashMap<>();
    private final Map<Direction, Animation<TextureRegion>> swordAnimations = new HashMap<>();
    private final Map<Direction, Animation<TextureRegion>> lift = new HashMap<>();
    private final Map<Direction, TextureRegion> carryStand = new HashMap<>();
    private final Map<Direction, Animation<TextureRegion>> carryWalk = new HashMap<>();
    private final Guy player;
    private final Sprite sprite = new Sprite();

    public PlayerRenderer(Guy player, TextureAtlas atlas) {
        this.player = player;
        var region = atlas.findRegion("character");

        for (int y = 0; y < TEX_Y_DIR_ORDER.length; y++) {
            standTexture.put(TEX_Y_DIR_ORDER[y], new TextureRegion(region, 0, y * 32, 32, 32));
            carryStand.put(TEX_Y_DIR_ORDER[y], new TextureRegion(region, 4 * 32, 4 * 32 + y * 32, 32, 32));
        }
//        standTexture.put(DOWN, new TextureRegion(region, 0, 0, 32, 32));
//        standTexture.put(RIGHT, new TextureRegion(region, 0, 32, 32, 32));
//        standTexture.put(UP, new TextureRegion(region, 0, 2 * 32, 32, 32));
//        standTexture.put(LEFT, new TextureRegion(region, 0, 3 * 32, 32, 32));

        Array<TextureRegion> frames = new Array<>();
        for (int y = 0; y < 4; y++) {
            // walk
            for (int x = 1; x < 4; x++) {
                frames.add(new TextureRegion(region, x * 32, y * 32, 32, 32));
            }
            walkAnimations.put(TEX_Y_DIR_ORDER[y], new Animation<>(0.1f, frames));
            frames.clear();
            // sword hit
            for (int x = 0; x < 4; x++) {
                frames.add(new TextureRegion(region, x * 32, 4 * 32 + y * 32, 32, 32));
            }
            swordAnimations.put(TEX_Y_DIR_ORDER[y], new Animation<>(0.1f, frames));
            frames.clear();
            // lift
            for (int x = 0; x < 3; x++) {
                frames.add(new TextureRegion(region, 4 * 32 + x * 32, y * 32, 32, 32));
            }
            lift.put(TEX_Y_DIR_ORDER[y], new Animation<>(0.3f, frames));
            frames.clear();
            // carry walking
            for (int x = 1; x < 4; x++) {
                frames.add(new TextureRegion(region, 4 * 32 + x * 32, 4 * 32 + y * 32, 32, 32));
            }
            carryWalk.put(TEX_Y_DIR_ORDER[y], new Animation<>(0.1f, frames));
            frames.clear();
        }

        sprite.setBounds(0, 0, 32 / PPM, 32 / PPM);
        sprite.setRegion(standTexture.get(DOWN));
    }

    @Override
    public void render(SpriteBatch batch) {
        var pos = player.b2body.getPosition();
        sprite.setPosition(pos.x - sprite.getWidth() / 2,
                pos.y - sprite.getHeight() / 2 + 6 / PPM);
        sprite.setRegion(getFrame());
        sprite.draw(batch);
    }

    private TextureRegion getFrame() {
        var state = player.state;
        switch (state.getState()) {
            case WALK:
                return walkAnimations.get(player.orientation).getKeyFrame(state.stateTimer(), true);
            case SWORD:
                return swordAnimations.get(player.orientation).getKeyFrame(state.stateTimer(), false);
            case LIFT:
                return lift.get(player.orientation).getKeyFrame(state.stateTimer(), false);
            case CARRY:
                if (state.isMoving()) {
                    return carryWalk.get(player.orientation).getKeyFrame(state.stateTimer(), true);
                } else {
                    return carryStand.get(player.orientation);
                }
            case STAND:
            default:
                return standTexture.get(player.orientation);
        }
    }
}
