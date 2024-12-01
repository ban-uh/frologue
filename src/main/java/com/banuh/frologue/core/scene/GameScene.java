package com.banuh.frologue.core.scene;

import com.banuh.frologue.core.Game;
import com.banuh.frologue.core.entity.Entity;
import com.banuh.frologue.core.input.InputEvent;
import com.banuh.frologue.core.tilemap.OverLap;
import com.banuh.frologue.core.tilemap.PlacedTileMap;
import com.banuh.frologue.core.utils.Vector2D;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;

import java.util.ArrayList;
import java.util.List;

public class GameScene {
    private Runnable startCallback = () -> {};
    private Runnable endCallback = () -> {};

    public String name;
    public Game game;
    public ArrayList<Entity> entityList = new ArrayList<>();
    public ArrayList<InputEvent> eventList = new ArrayList<>();
    public ArrayList<PlacedTileMap> placedTilemapList = new ArrayList<>();

    public GameScene(Game game, String name) {
        this.game = game;
        this.name = name;
    }

    public GameScene(Game game, String name, Runnable start) {
        this.game = game;
        this.name = name;
        this.startCallback = start;
    }

    public GameScene(Game game, String name, Runnable start, Runnable end) {
        this.game = game;
        this.name = name;
        if (start != null) {
            this.startCallback = start;
        }
        this.endCallback = end;
    }

    public void start() {

    }

    public void end() {

    }

    public void defaultStart() {
        for (InputEvent event: eventList) {
            event.apply();
        }
    }

    public void update() {

    }

    public void render() {

    }

    public void defaultEnd() {
        for (InputEvent event: eventList) {
            event.cancel();
        }
    }

    public <T extends Event> void addEventHandler(EventType<T> eventType, EventHandler<T> eventHandler) {
        InputEvent<T> event = new InputEvent<>(game, this, eventType, eventHandler);
        eventList.add(event);
    }

    public Runnable getStartCallback() {
        return startCallback;
    }

    public Runnable getEndCallback() {
        return endCallback;
    }

    public void runStartCallback() {
        start();
        startCallback.run();
        defaultStart();
    }

    public void runEndCallback() {
        end();
        endCallback.run();
        defaultEnd();
    }

    public OverLap isCollision(Vector2D pos, double width, double height, List<Entity> entities) {
        OverLap overLap = new OverLap(false);

        for (PlacedTileMap map: placedTilemapList) {
            OverLap collision = map.isCollision("solider", pos, width, height);
            if (collision.is) {
                overLap.apply(collision);
            }
        }

        for (Entity entity: entities) {
            double overlapX1 = Math.max(pos.getX(), entity.pos.getX());
            double overlapY1 = Math.max(pos.getY(), entity.pos.getY());
            double overlapX2 = Math.min(pos.getX() + width, entity.pos.getX() + entity.getWidth());
            double overlapY2 = Math.min(pos.getY() + height, entity.pos.getY() + entity.getHeight());

            // 겹친 영역의 폭과 높이 계산
            double overlapWidth = overlapX2 - overlapX1;
            double overlapHeight = overlapY2 - overlapY1;

            // 충돌 여부 확인
            if (overlapWidth <= 0 || overlapHeight <= 0) {
                continue;
            }

            // 충돌 방향 결정
            if (overlapWidth < overlapHeight) {
                // 가로 축에서 겹침: 좌우 충돌
                if (pos.getX() < entity.pos.getX()) {
                    overLap.isRight = true;
                    overLap.rightTilePos = entity.pos;
                } else {
                    overLap.isLeft = true;
                    overLap.leftTilePos = entity.pos;
                }
            } else {
                // 세로 축에서 겹침: 상하 충돌
                if (pos.getY() < entity.pos.getY()) {
                    overLap.isBottom = true;
                    overLap.bottomTilePos = entity.pos;
                } else {
                    overLap.isTop = true;
                    overLap.topTilePos = entity.pos;
                }
            }
        }
        return overLap;
    }
}
