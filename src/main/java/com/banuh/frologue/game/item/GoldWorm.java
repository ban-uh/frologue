package com.banuh.frologue.game.item;

import com.banuh.frologue.core.Game;
import com.banuh.frologue.game.frog.Frog;

import javax.swing.*;

public class GoldWorm extends Item {
    public GoldWorm(double x, double y, Game game) {
        super("gold_worm", x, y, game);
    }

    public void use(Frog frog) {
        JOptionPane.showMessageDialog(null, "당신이 ★황금 지렁이★를 획득하고 승리하였습니다!");
    };
}
