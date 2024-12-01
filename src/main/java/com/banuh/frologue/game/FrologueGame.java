package com.banuh.frologue.game;

import com.banuh.frologue.core.Game;
import com.banuh.frologue.game.scenes.PlayScene;
import javafx.scene.canvas.Canvas;

public class FrologueGame extends Game {
    public FrologueGame(Canvas canvas, int width, int height) {
        super(canvas, width, height);
    }

    public int levelCount = 4;

    @Override
    public void preload() {
        String[] types = {"normal", "ox", "space", "umbrella", "witch", "ninja"};
        for (String type: types) {
            addSprite("frog-"+type+"-death", "img/frog-"+type+"/death.png", 1, 4, 8);
            addSprite("frog-"+type+"-fall", "img/frog-"+type+"/fall.png");
            addSprite("frog-"+type+"-hurt", "img/frog-"+type+"/hurt.png", 1, 4, 8);
            addSprite("frog-"+type+"-idle", "img/frog-"+type+"/idle.png", 2, 2, 8);
            addSprite("frog-"+type+"-jump", "img/frog-"+type+"/jump.png");
            addSprite("frog-"+type+"-land", "img/frog-"+type+"/land.png", 1, 2, 8);
            addSprite("frog-"+type+"-move", "img/frog-"+type+"/land.png", 1, 2, 8);
            addSprite("frog-"+type+"-turn", "img/frog-"+type+"/turn.png");
            addSprite("frog-"+type+"-charging", "img/frog-"+type+"/charge.png", 1, 2, 4);
            addSprite("frog-"+type+"-charged", "img/frog-"+type+"/charge.png", 1, 2, 8);
        }

        addSprite("frog-man-charged", "img/frog-man/death.png", 1, 10, 8);
        addSprite("frog-man-fall", "img/frog-man/fall.png", 1, 3, 8);
        addSprite("frog-man-hurt", "img/frog-man/hurt.png");
        addSprite("frog-man-idle", "img/frog-man/idle.png", 1, 10, 8);
        addSprite("frog-man-jump", "img/frog-man/jump.png", 1, 3, 8);
        addSprite("frog-man-land", "img/frog-man/land.png");
        addSprite("frog-man-move", "img/frog-man/move.png", 1, 6, 8);
        addSprite("frog-man-turn", "img/frog-man/turn.png", 1, 3, 4);
        addSprite("frog-man-charging", "img/frog-man/charge.png", 1, 2, 4);
        addSprite("frog-man-charged", "img/frog-man/charge.png", 1, 2, 8);

        addSprite("item-energy_drink", "img/items/energy-drink.png");

        addTileMap("first_map", "map/bottom");
        addTileMap("test", "tilemap/sample_map");

        addSound("jump", "sound/effect/Jump.wav");
        addSound("reflect", "sound/effect/reflect.mp3");

        for (int i = 1; i <= levelCount; i++) {
            addTileMap("level-" + i, "map/map" + i);
        }
    }


    @Override
    public void update() {

    }

    @Override
    public void render() {

    }

    @Override
    public void start() {
        this.addScene(new PlayScene(this, "test"));
        this.setCurrentScene("test");
        this.camera.scale = 3;
    }
}
