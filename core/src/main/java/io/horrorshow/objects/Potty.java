package io.horrorshow.objects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import io.horrorshow.Hero;

import static io.horrorshow.Hero.PPM;

public class Potty extends Sprite {

    private final Vector2 buf_vector2 = new Vector2();
    private final TextureRegion[] pottyStand = new TextureRegion[4];
    private final Animation<TextureRegion>[] pottyRun = new Animation[4];
    public World world;
    public Body b2body;
    private int direction = 0;
    private int prevDirection = 0;
    private float stateTimer;

    public Potty(World world, TextureAtlas atlas) {
        var logRegion = atlas.findRegion("log");
        this.world = world;

        stateTimer = 0;

        definePotty();
        pottyStand[0] = new TextureRegion(logRegion, 0, 0, 32, 32);
        pottyStand[1] = new TextureRegion(logRegion, 0, 32, 32, 32);
        pottyStand[2] = new TextureRegion(logRegion, 0, 2 * 32, 32, 32);
        pottyStand[3] = new TextureRegion(logRegion, 0, 3 * 32, 32, 32);

        Array<TextureRegion> frames = new Array<>();
        for (int y = 0; y < 4; y++) {
            for (int i = 1; i <= 3; i++) {
                frames.add(new TextureRegion(logRegion, i * 32, y * 32, 32, 32));
            }
            pottyRun[y] = new Animation<>(0.1f, frames);
            frames.clear();
        }

        setBounds(0, 0, 32 / PPM, 32 / PPM);
        setRegion(pottyStand[0]);
    }

    private void definePotty() {
        BodyDef bDef = new BodyDef();
        bDef.position.set(0, 0);
        bDef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bDef);

        b2body.setLinearDamping(9f);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(0.7f);
        fdef.shape = shape;

        fdef.filter.categoryBits = Hero.POTTY_BIT;

        b2body.createFixture(fdef).setUserData(this);
    }

    public void update(float dt) {

        setPosition(b2body.getPosition().x - getWidth() / 2,
                b2body.getPosition().y - getHeight() / 2 + 6 / PPM);
        setRegion(getFrame(dt));

        if (prevDirection != direction) {
            stateTimer = 0;
            prevDirection = direction;
        }
        stateTimer += dt;
    }

    private TextureRegion getFrame(float dt) {
        var vel = b2body.getLinearVelocity();
        if (Math.abs(vel.x) > Math.abs(vel.y)) {
            if (vel.x > 0) direction = 2;
            else direction = 3;
        } else {
            if (vel.y > 0) direction = 1;
            else direction = 0;
        }
        if (Math.abs(vel.x) + Math.abs(vel.y) < 0.4) {
            return pottyStand[direction];
        } else {
            return pottyRun[direction].getKeyFrame(stateTimer, true);
        }
    }

    public void move(Vector2 dir) {
        var x = dir.x - b2body.getPosition().x;
        var y = dir.y - b2body.getPosition().y;
        var v = new Vector2(x, y);
        v.nor();
        b2body.applyLinearImpulse(
                v, b2body.getWorldCenter(), true);
    }
}