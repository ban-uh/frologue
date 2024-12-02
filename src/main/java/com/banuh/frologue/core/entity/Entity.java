package com.banuh.frologue.core.entity;

import com.banuh.frologue.core.Game;
import com.banuh.frologue.core.camera.GameCamera;
import com.banuh.frologue.core.scene.GameScene;
import com.banuh.frologue.core.sprite.AnimationSprite;
import com.banuh.frologue.core.sprite.Sprite;
import com.banuh.frologue.core.utils.Vector2D;
import javafx.scene.canvas.GraphicsContext;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class Entity {
    public Vector2D pos = new Vector2D();
    public Game game;
    public boolean isDummy = false;

    /** 초당 가능 속도(px) */
    private HashMap<String, Vector2D> velocity = new HashMap<>();

    private Sprite defaultSprite;
    public Sprite activeSprite;
    private GameScene scene;
    private boolean stateIsChanged = true;
    final private LinkedHashMap<String, StatePair> stateList = new LinkedHashMap<>();
    private HashMap<String, Boolean> flagList = new HashMap<>();

    public boolean isFlip = false;
    public Hitbox hitbox;

    public Entity(Sprite defaultSprite, double x, double y, Game game, Hitbox hitbox) {
        this.defaultSprite = defaultSprite.clone();
        activeSprite = defaultSprite;

        this.hitbox = hitbox;

        pos.setX(x);
        pos.setY(y);

        this.game = game;
        this.scene = game.getCurrentScene();
    }

    public void draw(GraphicsContext gc, GameCamera camera) {
        Sprite currentSprite = defaultSprite;

        // 변경됐을 경우에만 체크
        if (stateIsChanged) {
            for (StatePair pair: stateList.values()) {
                if (pair.is) currentSprite = pair.sprite;
            }

            // 기존에 보여지고 있던 스프라이트 대신 다른 스프라이트가 보여지게 된다면 바껴진 스프라이트는 다시 재생되게 index를 변경해야함!
            if (activeSprite != currentSprite) {
                if (currentSprite instanceof AnimationSprite) {
                    ((AnimationSprite) currentSprite).setCurrentFrame(0);
                }
                activeSprite = currentSprite;
            }
            stateIsChanged = false;
        } else {
            currentSprite = activeSprite;
        }

        Vector2D drawPos = new Vector2D(pos.getX(), pos.getY());
        drawPos.subtracted(game.camera.pos);
        currentSprite.draw(gc, pos.getX(), pos.getY(), camera, this, isFlip);
    }

    public Entity addState(String stateName, Sprite sprite) {
        stateList.put(stateName, new StatePair(sprite, false));
        return this;
    }

    public Entity addState(String stateName, String spriteName) {
        stateList.put(stateName, new StatePair(game.getSprite(spriteName), false));
        return this;
    }

    public Entity addState(String name) {
        stateList.put(name, new StatePair(game.getSprite(name), false));
        return this;
    }

    public Entity addStates(String[] nameList) {
        for (String name: nameList) {
            stateList.put(name, new StatePair(game.getSprite(name), false));
        }

        return this;
    }

    public void setState(String stateName, Boolean onState) {
        if (stateList.get(stateName).is != onState) {
            stateList.get(stateName).is = onState;
            stateIsChanged = true;
        }
    }

    public void setState(String stateName) {
        setState(stateName, true);
    }

    public boolean getState(String stateName) {
        return stateList.get(stateName).is;
    }

    public StatePair getState() {
        StatePair currentState = null;
        for (StatePair state: stateList.values()) {
            if (state.is) {
                currentState = state;
            }
        }
        return currentState;
    }

    public double getWidth() {
        return this.hitbox.getWidth();
    }

    public double getHeight() {
        return this.hitbox.getHeight();
    }

    public Vector2D getVelocity(String name) {
        return velocity.get(name);
    }

    public Entity addVelocity(String name) {
        velocity.put(name, new Vector2D(0, 0));
        return this;
    }

    public void setVelocity(String name, Vector2D vector) {
        velocity.put(name, vector);
    }

    public void setVelocity(String name, double x, double y) {
        setVelocityX(name, x);
        setVelocityY(name, y);
    }

    public void setVelocityX(String name, double x) {
        getVelocity(name).setX(x);
    }

    public void setVelocityY(String name, double y) {
        getVelocity(name).setY(y);
    }

    public Vector2D getTotalVelocity() {
        Vector2D total = new Vector2D(0, 0);
        for (Vector2D v: velocity.values()) {
            total.added(v);
        }
        return total;
    }

    public Vector2D getNextPos() {
        return pos.add(getTotalVelocity().divide(game.getFps()));
    }

    public GameScene getScene() {
        return scene;
    }

    public boolean getFlag(String name) {
        if (flagList.containsKey(name)) {
            return false;
        } else {
            return flagList.get(name);
        }
    }

    public void setFlag(String name, boolean flag) {
        flagList.put(name, flag);
    }

    public void setFlag(String name) {
        flagList.put(name, true);
    }

    public boolean isCollision(Entity target) {
        return Vector2D.isCollision(
            pos.getX(), pos.getY(), pos.getX() + getWidth(), pos.getY() + getHeight(),
            target.pos.getX(), target.pos.getY(), target.pos.getX() + target.getWidth(), target.pos.getY() + target.getHeight()
        );
    }

    static class StatePair {
        Sprite sprite;
        boolean is;

        StatePair(Sprite sprite, boolean is) {
            this.sprite = sprite;
            this.is = is;
        }
    }
}
