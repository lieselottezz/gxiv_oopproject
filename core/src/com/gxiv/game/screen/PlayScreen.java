package com.gxiv.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gxiv.game.Gxiv;
import com.gxiv.game.hud.Hud;
import com.gxiv.game.hud.Pause;
import com.gxiv.game.sprites.enemies.RomanArmy;
import com.gxiv.game.sprites.items.Item;
import com.gxiv.game.sprites.items.ItemDef;
import com.gxiv.game.sprites.items.Mushroom;
import com.gxiv.game.sprites.Player;
import com.gxiv.game.sprites.tileobjects.CeilTurret;
import com.gxiv.game.sprites.tileobjects.GroundTurret;
import com.gxiv.game.tools.B2WorldCreator;
import com.gxiv.game.tools.WorldContactListener;
import com.gxiv.game.util.AssetsManager;
import com.gxiv.game.util.Constants;
import com.gxiv.game.util.MusicManager;

import java.util.concurrent.LinkedBlockingDeque;

public class PlayScreen implements Screen {

    /*PlayScreen Setup*/
    private Hud hud;
    private Gxiv game;
    private World world;
    private Player player;
    private TiledMap map;
    private Array<Item> items;
    private Viewport gamePort;
    private TextureAtlas atlas;
    private MusicManager music;
    private Box2DDebugRenderer b2dr;
    private OrthographicCamera gamecam;
    private OrthogonalTiledMapRenderer renderer;
    private LinkedBlockingDeque<ItemDef> itemsToSpawn;
    private B2WorldCreator creator;

    /*Pause State Logic*/
    private boolean isPaused = false;
    private boolean pauseHelper = false;

    public PlayScreen(Gxiv game){

        this.game = game;
        hud = new Hud(game.batch);
        gamecam = new OrthographicCamera();
        MusicManager music = new MusicManager();
        atlas = new TextureAtlas("GXIV.pack");
        gamePort = new FitViewport(
                Constants.V_WIDTH / Constants.PPM,
                Constants.V_HEIGHT / Constants.PPM, gamecam
        );

        TmxMapLoader mapLoader = new TmxMapLoader();
        map = mapLoader.load(AssetsManager.getNameMap());
        renderer = new OrthogonalTiledMapRenderer(map, 1/ Constants.PPM);

        gamecam.position.set(gamePort.getWorldWidth() / 2 , gamePort.getWorldHeight() / 2, 0);

        world = new World(new Vector2(0,-10), true);
        b2dr = new Box2DDebugRenderer();
        creator = new B2WorldCreator(this);

        player = new Player(this);

        world.setContactListener(new WorldContactListener());
        Constants.worldTimer = 300;
        music.stopMusic();
        music.setMusic(Constants.STAGE_1_BGM);
        music.playMusic();


        items = new Array<Item>();
        itemsToSpawn = new LinkedBlockingDeque<ItemDef>();

    }

    public void spawnItem(ItemDef idef){
        itemsToSpawn.add(idef);
    }

    public Viewport getGamePort(){
        return gamePort;
    }

    private void handleSpawningItems(){
        if(!itemsToSpawn.isEmpty()){
            ItemDef idef = itemsToSpawn.poll();
            if(idef.type == Mushroom.class){
                items.add(new Mushroom(this, idef.position.x, idef.position.y));
            }
        }
    }

    public TextureAtlas getAtlas(){
        return atlas;
    }

    @Override
    public void show() {

    }

    private void handleInput(float dt){
        if(!player.isDead()) {

            /*Left UP Key -> Jump*/
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP) && player.getState() != Player.State.JUMPING && player.getState() != Player.State.FALLING && !isPaused) {
                player.b2body.applyLinearImpulse(new Vector2(0, 4f), player.b2body.getWorldCenter(), true);
            }

            /*Right Key -> Move Right*/
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && player.b2body.getLinearVelocity().x <= 2 && !isPaused)
                player.b2body.applyLinearImpulse(new Vector2(0.1f, 0), player.b2body.getWorldCenter(), true);

            /*Left Key -> Move Left*/
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && player.b2body.getLinearVelocity().x >= -2 && !isPaused)
                player.b2body.applyLinearImpulse(new Vector2(-0.1f, 0), player.b2body.getWorldCenter(), true);

            /*Space Key -> Fire*/
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !isPaused && player.getFireTime() >= 1) {
                player.fire();
                Gdx.app.log("FireTime",""+player.getFireTime());
                player.setFireTime(0);
            }

            /*Escape -> Pause*/
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                // Use a helper so that a held-down button does not continuously switch between states with every tick
                if (pauseHelper) {
                    if (isPaused) {
                        Gdx.app.log("State", "Playing");
                        isPaused = false;
                    } else {
                        Gdx.app.log("State", "Pause");
                        isPaused = true;
                    }
                    pauseHelper = false;
                }
             else {
                pauseHelper = true;
            }
        }
        }

    }

    private void update(float dt){
        handleInput(dt);
        handleSpawningItems();
        world.step(1/60f, 6, 2);
        player.update(dt);


//        for(Item item : items)
//            item.update(dt);
        if (!isPaused) {
            for(RomanArmy enemy : creator.getArr()){
                enemy.update(dt);
                if(!enemy.getDestroy() && enemy.getX() < player.getX() + 224 / Constants.PPM)
                    enemy.b2body.setActive(true);
            }
//            for(Item item : items)
//                item.update(dt);
            hud.update(dt);

            if(player.currentState != Player.State.DEAD) {
                gamecam.position.x = player.b2body.getPosition().x;
            }
            gamecam.update();
            renderer.setView(gamecam);
        } else {
            new Pause(game.batch);
        }


    }

    @Override
    public void render(float delta) {
        if (!isPaused) {
           update(delta);
        }
        handleInput(delta);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        renderer.render();
//
        b2dr.render(world, gamecam.combined);

        game.batch.setProjectionMatrix(gamecam.combined);
        game.batch.begin();
        player.draw(game.batch);
        for(RomanArmy enemy : creator.getArr())
            if(!enemy.getDestroy())
                enemy.draw(game.batch);
        // Ground Turret Shoot System
        for(GroundTurret turret : creator.getGroundTurretArray()){
            if(!turret.getDestroy() && turret.getFireTime() >= 1 && (turret.body.getPosition().x < player.getX() + 224 / Constants.PPM && !(turret.body.getPosition().x < player.getX() - 224 / Constants.PPM) && !isPaused)) {
                turret.fire();
                turret.setFireTime(0);
            }
            else{
                turret.addFireTime(0.02f);
            }
            if(!isPaused){
                turret.update(delta);
            }
            turret.draw(game.batch);

        }
        // Ceil Turret Shoot System
        for(CeilTurret turret : creator.getCeilTurretArray()){
            if(!turret.getDestroy() && turret.getFireTime() >= 1 && (turret.body.getPosition().x < player.getX() + 224 / Constants.PPM && !(turret.body.getPosition().x < player.getX() - 224 / Constants.PPM)) && !isPaused) {
                turret.fire();
                turret.setFireTime(0);
            }
            else{
                turret.addFireTime(0.02f);
            }
            if(!isPaused){
                turret.update(delta);
            }
            turret.draw(game.batch);

        }
//        for(Item item: items){
//            item.draw(game.batch);
//        }
        game.batch.end();

        game.batch.setProjectionMatrix(hud.stage.getCamera().combined);
        hud.stage.draw();
        if (player.getNextMapTouch()){
            game.setScreen(new ScoreSum1(game));
            dispose();
        }
        if (gameOver()){
            game.setScreen(new GameOverScreen(game));
            dispose();
        }

    }

    private boolean gameOver(){
        return player.isDead() && player.getStateTimer() > 3;
    }

    @Override
    public void resize(int width, int height) {
        gamePort.update(width, height);
    }

    public TiledMap getMap(){
        return map;
    }

    public World getWorld() {
        return world;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
        world.dispose();
        b2dr.dispose();
        hud.dispose();
    }
}
