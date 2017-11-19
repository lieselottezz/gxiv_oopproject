package com.gxiv.game.sprites.tileobjects;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.utils.Array;
import com.gxiv.game.hud.Hud;
import com.gxiv.game.screen.PlayScreen;
import com.gxiv.game.sprites.bullet.CTurretBullet;
import com.gxiv.game.sprites.bullet.GTurretBullet;
import com.gxiv.game.sprites.bullet.Revolver;
import com.gxiv.game.util.AssetsManager;
import com.gxiv.game.util.Constants;

public class CeilTurret extends InteractiveTileObject{
    float turret = 0;
    private Array<CTurretBullet> bullets;
    private float fireTime;
    private boolean setToDestroy;
    public CeilTurret(PlayScreen screen, RectangleMapObject objects){
        super(screen, objects);
        fixture.setUserData(this);
        bullets = new Array<CTurretBullet>();
        setToDestroy = false;
        fireTime = 0;
        setCategoryFilter(Constants.GROUND_TURRET_BIT);
    }

    @Override
    public void hitOnBullet(Revolver bullet) {
        turret += 1;
        if(turret == 3){
            setCategoryFilter(Constants.DESTROY_BIT);
            getCell1().setTile(null);
            getCell2().setTile(null);
            getCell3().setTile(null);
            getCell4().setTile(null);
            Hud.addScore(100);
            Constants.cT += 1;
            Revolver.delay = 0;
            setToDestroy = true;
            AssetsManager.manager.get("audio/sounds/breakblock.wav", Sound.class).play();
        }
    }
    public void update(float dt){
        fireTime += 0.0001;
        for(CTurretBullet bullet: bullets) {
            bullet.update(dt);
            if(bullet.isDestroyed())
                bullets.removeValue(bullet, true);
        }
    }

    public void fire(){
        bullets.add(new CTurretBullet(screen, body.getPosition().x, body.getPosition().y));
        AssetsManager.manager.get("audio/sounds/laser.wav", Sound.class).play();
    }

    public void draw(Batch batch){
        for(CTurretBullet bullet : bullets){
            bullet.draw(batch);
        }
    }

    public boolean getDestroy(){
        return setToDestroy;
    }

    public float getFireTime() {
        return fireTime;
    }

    public void setFireTime(float fireTime) {
        this.fireTime = fireTime;
    }

    public void addFireTime(float fireTime) {
        this.fireTime += fireTime;
    }
}
