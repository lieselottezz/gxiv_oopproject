package com.gxiv.game.sprites.bullet;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.gxiv.game.screen.PlayScreen;
import com.gxiv.game.util.Constants;

 public class FireBall extends Sprite{
        private PlayScreen screen;
        private World world;
        TextureRegion fireAnimation;
        private float stateTime;
        private boolean destroyed;
        private boolean setToDestroy;
        private boolean fireRight;

        Body b2body;

        public FireBall(PlayScreen screen, float x, float y, boolean fireRight){
            this.fireRight = fireRight;
            this.screen = screen;
            this.world = screen.getWorld();
            fireAnimation = new TextureRegion(screen.getAtlas().findRegion("GXIV"), 3, 3, 8, 8);
            setRegion(fireAnimation);
            setBounds(x, y, 6 / Constants.PPM, 6 / Constants.PPM);
            defineFireBall();
        }

        public void defineFireBall(){
            BodyDef bdef = new BodyDef();
            bdef.position.set(fireRight ? getX() + 4 /Constants.PPM : getX() - 4 /Constants.PPM, getY());
            bdef.type = BodyDef.BodyType.DynamicBody;
            if(!world.isLocked())
                b2body = world.createBody(bdef);

            FixtureDef fdef = new FixtureDef();
            CircleShape shape = new CircleShape();
            shape.setRadius(3 / Constants.PPM);
            fdef.filter.categoryBits = Constants.FIREBALL_BIT;
            fdef.filter.maskBits = Constants.GROUND_BIT |
                    Constants.COIN_BIT |
                    Constants.BRICK_BIT |
                    Constants.ENEMY_BIT |
                    Constants.OBJECT_BIT |
                    Constants.ENEMY_HEAD_BIT;
            fdef.shape = shape;
            fdef.restitution = 1;
            fdef.friction = 0;
            b2body.createFixture(fdef).setUserData(this);
            b2body.setLinearVelocity(new Vector2(fireRight ? 7 : -7,1f));

        }

        public void update(float dt){
            stateTime += dt;
            setRegion(fireAnimation);
            setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() / 2);
            if((stateTime > 0.9f || setToDestroy) && !destroyed) {
                world.destroyBody(b2body);
                destroyed = true;
            }
            if(b2body.getLinearVelocity().y > 2f)
                b2body.setLinearVelocity(b2body.getLinearVelocity().x, 2f);
            if((fireRight && b2body.getLinearVelocity().x < 0) || (!fireRight && b2body.getLinearVelocity().x > 0))
                setToDestroy();
        }

        public void setToDestroy(){
            setToDestroy = true;
        }

        public boolean isDestroyed(){
            return destroyed;
        }
 }
