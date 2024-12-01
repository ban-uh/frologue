package com.banuh.frologue.core.tilemap;

import com.banuh.frologue.core.Game;
import com.banuh.frologue.core.entity.Entity;
import com.banuh.frologue.core.utils.Direction;
import com.banuh.frologue.core.utils.Vector2D;

public class PlacedTileMap {
    String name;
    Game game;
    Vector2D pos;
    TileMap tileMap;

    public PlacedTileMap(String name, TileMap tileMap, double x, double y, Game game) {
        this.name = name;
        this.game = game;
        this.tileMap = tileMap;
        this.pos = new Vector2D(x, y);
    }

    public TileMap getData() {
        return tileMap;
    }

    public void draw() {
        tileMap.draw(pos.getX(), pos.getY());
    }

    // 오버랩된 크기를 가늠
    public OverLap isCollision(String layerName, Vector2D target, double width, double height) {
        TileMap.Layer layer = tileMap.getLayer(layerName);

        // 먼저 엔티티가 이 타일맵에 포함, 즉 충돌되는지부터 확인
        if (
            this.getX() < target.getX() + width &&
            this.getX() + this.getWidth() > target.getX() &&
            this.getY() < target.getY() + height &&
            this.getY() + this.getHeight() > target.getY()
        ) {
            // 엔티티를 맵을 기준으로 위치를 계산
            double entityX = target.getX() - this.getX();
            double entityY = target.getY() - this.getY();

            int tileSize = tileMap.getTileSize();

            // 엔티티가 타일맵 기준 어디에 속해있는지 확인
            int areaX1 = (int)(entityX + 1) / tileSize;
            int areaY1 = (int)(entityY + 1) / tileSize;

            int areaX2 = (int)(entityX + width - 1) / tileSize + 1;
            int areaY2 = (int)(entityY + height - 1) / tileSize + 1;

            OverLap overLap = new OverLap(false);

            for (int i = areaY1; i < areaY2; i++) {
                for (int j = areaX1; j < areaX2; j++) {
                    if (i < tileMap.getMapHeight() && j < tileMap.getMapWidth() && i >= 0 && j >= 0 && layer.tiles[i][j] != null) {
                        Vector2D tilePos = new Vector2D(j * tileSize, i * tileSize).added(pos);

                        double overlapX1 = Math.max(target.getX(), tilePos.getX());
                        double overlapY1 = Math.max(target.getY(), tilePos.getY());
                        double overlapX2 = Math.min(target.getX() + width, tilePos.getX() + tileSize);
                        double overlapY2 = Math.min(target.getY() + height, tilePos.getY() + tileSize);

                        double overlapWidth = overlapX2 - overlapX1;
                        double overlapHeight = overlapY2 - overlapY1;

                        if (overlapWidth < overlapHeight) {
                            if (target.getX() < tilePos.getX()) {
                                overLap.isRight = true;
                                overLap.rightTilePos = tilePos;
                            } else {
                                overLap.isLeft = true;
                                overLap.leftTilePos = tilePos;
                            }
                        } else {
                            if (target.getY() < tilePos.getY()) {
                                overLap.isBottom = true;
                                overLap.bottomTilePos = tilePos;
                            } else {
                                overLap.isTop = true;
                                overLap.topTilePos = tilePos;
                            }
                        }

                        overLap.is = true;
                    }
                }
            }

            // 부딪힌 방향을 종합하여 리턴
            return overLap;
        }

        return new OverLap(false);
    }

    public double getX() {
        return pos.getX();
    }

    public double getY() {
        return pos.getY();
    }

    public double getWidth() {
        return tileMap.getWidth();
    }

    public double getHeight() {
        return tileMap.getHeight();
    }
}
