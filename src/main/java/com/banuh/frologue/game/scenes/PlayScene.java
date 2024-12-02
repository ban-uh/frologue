package com.banuh.frologue.game.scenes;

import com.banuh.frologue.core.Game;
import com.banuh.frologue.core.entity.Entity;
import com.banuh.frologue.core.scene.GameScene;
import com.banuh.frologue.core.tilemap.OverLap;
import com.banuh.frologue.core.tilemap.TileMap;
import com.banuh.frologue.core.utils.Vector2D;
import com.banuh.frologue.game.GlobalVariables;
import com.banuh.frologue.game.frog.*;
import com.banuh.frologue.game.item.EnergyDrink;
import com.banuh.frologue.game.item.GoldWorm;
import com.banuh.frologue.game.item.Item;
import com.banuh.frologue.server.RoomServer;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

import java.lang.reflect.Array;
import java.util.*;

public class PlayScene extends GameScene {
    public double GRAVITY = 9.8;
    public final int TILE_SIZE = 16;
    public Frog frog;

    public ArrayList<Item> droppedItems = new ArrayList<>();

    OverLap overLap;
    public HashMap<String, Frog> playerList = new HashMap<>();

    public PlayScene(Game game, String name) {
        super(game, name);
    }

    @Override
    public void update() {
        Iterator<Item> iterator = droppedItems.iterator();
        while (iterator.hasNext()) {
            Item item = iterator.next();
            if (frog.isCollision(item)) {
                item.use(frog);
                entityList.remove(item);
                iterator.remove();
            }
        }

//        System.out.println("JUMP: " + frog.getState("jump"));
//        System.out.println("MOVE: " + frog.getState("move"));
//        System.out.println("FALL: " + frog.getState("fall"));
//        System.out.println();

        frog.setVelocityX("wall", 0);
        OverLap overLap = isCollision(frog.getNextPos(), frog.getWidth(), frog.getHeight(), new ArrayList<Entity>(playerList.values()));
        game.camera.pos.setY(frog.pos.getY() - game.height/2f - 25);
//        game.camera.pos.set(0, 0);

        this.overLap = overLap;

//        if (overLap.is) {
//            System.out.println("TOP:    " + overLap.isTop);
//            System.out.println("RIGHT:  " + overLap.isRight);
//            System.out.println("BOTTOM: " + overLap.isBottom);
//            System.out.println("LEFT:   " + overLap.isLeft);
//            System.out.println();
//        }

        if (overLap.isRight || overLap.isLeft || frog.pos.getX() < 0 || frog.pos.getX() > game.width - frog.getWidth()) {
            if (frog.getState("jump") && !overLap.isBottom) { // 점프하다가 벽에 닿은 경우
                if (frog instanceof NinjaFrog) {
                    frog.getVelocity("move").addedY(-150);
                }

                game.playSound("reflect");
                frog.getVelocity("move_with_jump").multiplied(-1);
                frog.isFlip = !frog.isFlip;
            } else { // 그냥 벽에 닿은 경우
                if (overLap.isLeft) {
                    frog.pos.setX(overLap.leftTilePos.getX() + TILE_SIZE);
                } else if (overLap.isRight) {
                    frog.pos.setX(overLap.rightTilePos.getX() - frog.getWidth());
                }

                if (frog.pos.getX() >= game.width - frog.getWidth()) {
                    frog.pos.setX(game.width - frog.getWidth());
                } else if (frog.pos.getX() <= 0) {
                    frog.pos.setX(0);
                }
            }
        }

        if (overLap.isTop && !overLap.isBottom) {
            // 점프하다가 천장에 닿은 경우
            if (frog.getState("jump")) {
                frog.getVelocity("move").flipY();
                frog.getVelocity("move").multiplied(0.25);
                game.playSound("reflect");
            }
        }

        // 바닥에 닿아있는 경우
        if (overLap.isBottom) {
            frog.setOnGround(true);

            if (frog.getState("fall")) {
                frog.setState("jump", false);
                frog.getVelocity("move_with_jump").reset();

                frog.setState("land");
                game.setTimeout(() -> {
                    frog.setState("land", false);
                }, game.FRAME() * 4);
            }

            frog.pos.setY(overLap.bottomTilePos.getY() - frog.getHeight() + 1);
            frog.setVelocityY("move", 0);
            frog.setVelocityY("gravity", 0);
        } else {
            frog.setOnGround(false);
            frog.getVelocity("gravity").addedY(GRAVITY);
        }

        double totalVelocityY = frog.getTotalVelocity().getY();

        if (game.isPressed.leftKey) {
            if (frog.isFlip) {
                frog.setState("turn", true);
                frog.isFlip = false;
                game.setTimeout(() -> {
                    frog.setState("turn", false);
                }, game.FRAME() * 2);
            }
            frog.setVelocityX("move", -frog.SPEED);
            frog.setState("move", true);
        } else if (game.isPressed.rightKey) {
            if (!frog.isFlip) {
                frog.setState("turn", true);
                frog.isFlip = true;
                game.setTimeout(() -> {
                    frog.setState("turn", false);
                }, game.FRAME() * 2);
            }
            frog.setVelocityX("move", frog.SPEED);
            frog.setState("move", true);
        } else {
            frog.setVelocityX("move", 0);
            frog.setState("move", false);
        }

        // 차징 중이나 점프 중엔 키보드로 이동못함!
        if (frog.getState("charging") || frog.getState("jump")) {
            frog.setVelocityX("move", 0);
            frog.setState("move", false);
        }

        if (game.isPressed.spaceKey) {
            if (frog.jump_scale < 3) {
                double val = 1.5f;
//                if (frog.getFlag("energy_drink")) {
//                    val = 15f;
//                }
                frog.jump_scale += val / game.getFps();
            } else {
                frog.jump_scale = 3;
            }
        }

        if (frog.jump_scale >= 2.5) {
            frog.setState("charged");
        }

        frog.setState("up", totalVelocityY < 0);
        frog.setState("fall", totalVelocityY > 0);

        if (frog instanceof UmbrellaFrog) {
            if (frog.getState("fall")) {
                GRAVITY = 9.8 / 8;
            } else {
                GRAVITY = 9.8;
            }
        }
    }

    @Override
    public void render() {
        if (GlobalVariables.server.socket.isClosed()) {
            System.out.println("Socket is closed!");
        }

//        if (overLap.isTop) {
//            game.gc.setFill(new Color(1f, 0f, 0f, 0.5f));
//            game.gc.fillRect((overLap.topTilePos.getX() - game.camera.pos.getX()) * game.camera.scale, (overLap.topTilePos.getY() - game.camera.pos.getY()) * game.camera.scale, 48, 48);
//        }
//        if (overLap.isRight) {
//            game.gc.setFill(new Color(0f, 1f, 0f, 0.5f));
//            game.gc.fillRect((overLap.rightTilePos.getX() - game.camera.pos.getX()) * game.camera.scale, (overLap.rightTilePos.getY() - game.camera.pos.getY()) * game.camera.scale, 48, 48);
//        }
//        if (overLap.isBottom) {
//            game.gc.setFill(new Color(0f, 0f, 1f, 0.5f));
//            game.gc.fillRect((overLap.bottomTilePos.getX() - game.camera.pos.getX()) * game.camera.scale, (overLap.bottomTilePos.getY() - game.camera.pos.getY()) * game.camera.scale, 48, 48);
//        }
//        if (overLap.isLeft) {
//            game.gc.setFill(new Color(0f, 0f, 0f, 0.5f));
//            game.gc.fillRect((overLap.leftTilePos.getX() - game.camera.pos.getX()) * game.camera.scale, (overLap.leftTilePos.getY() - game.camera.pos.getY()) * game.camera.scale, 48, 48);
//        }

//        if (tilepos != null) {
//            // show hitboxes
//            game.gc.setStroke(Color.RED);
//            game.gc.strokeRect(tilepos.getX() * game.camera.scale, tilepos.getY() * game.camera.scale, tilewidth * game.camera.scale, tileheight * game.camera.scale);
//            game.gc.strokeRect(frog.pos.getX() * game.camera.scale, (frog.pos.getY()) * game.camera.scale, frog.getWidth() * game.camera.scale, frog.getHeight() * game.camera.scale);
//
//        }

//        if (overLap.is) {
//            game.gc.setFill(new Color(1f, 0f, 0f, 0.5f));
//            game.gc.fillRect(
//                    overLap.from.getX() * game.camera.scale,
//                    overLap.from.getY() * game.camera.scale,
//                    overLap.to.getX() * game.camera.scale,
//                    overLap.to.getY() * game.camera.scale
//            );
//        }
    }

    @Override
    public void start() {
        RoomServer server = GlobalVariables.server;
//        game.showHitbox = true;
        server.game = game;

        // 총 5개의 랜덤 맵을 가져옴
        Random random = new Random();
        int[] levels = new int[5];

        if (GlobalVariables.isHost) {
            for (int i = 0; i < 5; i++) {
                levels[i] = random.nextInt(4) + 1;
            }
            drawMap(levels);
            server.sendMapInfo(levels);
        }

        new Thread(() -> server.receiveUpdates(this)).start();

        game.setInterval(() -> {
            server.sendPlayerPosition(frog);
        }, game.FRAME());
        game.backgroundColor = Color.web("#6bc6ff");

        switch (GlobalVariables.frogType) {
            case "normal": frog = new NormalFrog(150, 50, game); break;
            case "ninja": frog = new NinjaFrog(150, 50, game); break;
            case "ox": frog = new OxFrog(150, 50, game); break;
            case "space": frog = new SpaceFrog(150, 50, game); break;
            case "umbrella": frog = new UmbrellaFrog(150, 50, game); break;
            case "witch": frog = new WitchFrog(150, 50, game); break;
        }

        game.addEntity(frog);

        frog.pid = UUID.randomUUID().toString();
//        game.addEntity(new EnergyDrink(200, 40, game));
        dropItem(new EnergyDrink(200, 50, game));

        if (frog instanceof SpaceFrog) {
            GRAVITY = 1.63;
        }

        addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            KeyCode key = event.getCode();

            if (key == KeyCode.SPACE && !frog.getState("charging") && !frog.getState("jump")) {
                frog.jump_scale = 1;
                frog.setState("charging");
            }
        });

        addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            KeyCode key = event.getCode();

            if (key == KeyCode.SPACE && !frog.getState("jump")) {
                double jump_meter = 0;
                double jump_high = 0;

//                if (frog instanceof NinjaFrog) {
//                    jump_meter = frog.JUMP_METER / (frog.jump_scale * 4) - 50;
//                    jump_high = frog.JUMP_STRENGTH + frog.jump_scale * 100;
//                } else {
//                }
                jump_meter = frog.JUMP_METER;
                jump_high = frog.JUMP_STRENGTH * frog.jump_scale;

                frog.setVelocityY("move", -jump_high);
                frog.setVelocityX("move_with_jump", jump_meter * (frog.isFlip ? 1 : -1));
                /*frog.SPEED *= 3;*/
                frog.setState("charging", false);
                frog.setState("charged", false);
                frog.setState("jump");
                frog.jump_scale = 1;

                game.playSound("jump");
            }
        });
    }

    public void drawMap(int[] levels) {
        TileMap firstMap = game.tileMapList.get("first_map");
        game.placeTileMapByBottom("first_map", (game.width - firstMap.getWidth()) / 2f, 200);

        int bottomY = firstMap.getHeight();

        for (int level: levels) {
            TileMap map = game.tileMapList.get("level-" + level);
            game.placeTileMapByBottom("level-" + level, (game.width - map.getWidth()) / 2f, 175 - bottomY);
            bottomY += map.getHeight() + level * 15;
        }

        Item item = new GoldWorm((game.width - 16) / 2f, - bottomY + 170, game);
        dropItem(item);
    }

    public void dropItem(Item item) {
        droppedItems.add(item);
        game.addEntity(item);
    }
}
