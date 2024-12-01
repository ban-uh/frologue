package com.banuh.frologue.server;

import com.banuh.frologue.core.Game;
import com.banuh.frologue.core.utils.Vector2D;
import com.banuh.frologue.game.frog.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Objects;

public class RoomServer {
    private Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;
    private String playerId;
    private int playerX = 100, playerY = 100;
    private String playerState = "idle";  // 플레이어 상태 추가
    private String roomCode;
    private GraphicsContext gc;  // GraphicsContext 객체 클래스 멤버로 선언
    private Image idleImage, moveImage;   // 플레이어 이미지들
    public Game game;

    public void join(String code) {
        try {
            writer.write("JoinRoom " + code);
            writer.newLine();
            writer.flush();
            String response = reader.readLine();
            if (response.startsWith("Joined Room")) {
                roomCode = response.split(": ")[1];
                JOptionPane.showMessageDialog(null, "Joined room: " + roomCode);
            } else {
                JOptionPane.showMessageDialog(null, "Failed to join room: " + response);
                System.exit(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            reconnectToServer();
        }
    }

    public void create() {
        try {
            writer.write("CreateRoom");
            writer.newLine();
            writer.flush();
            String response = reader.readLine();
            roomCode = response.split(": ")[1];
            JOptionPane.showMessageDialog(null, "Created room: " + roomCode);
        } catch (Exception e) {
            e.printStackTrace();
            reconnectToServer();
        }
    }

//    public void connect() {
//        try {
//            socket = new Socket("127.0.0.1", 1444);
//            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
//            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
//        } catch (Exception e) {
//            e.printStackTrace();
//            reconnectToServer();
//        }
//    }

    public void connect() {
        try {
            socket = new Socket("127.0.0.1", 1444);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

            System.out.println("check");
            String roomInput = JOptionPane.showInputDialog(null,
                    "Enter room code to join, or leave blank to create new room:");

            if (roomInput != null && !roomInput.trim().isEmpty()) {
                writer.write("JoinRoom " + roomInput.trim());
                writer.newLine();
                writer.flush();
                String response = reader.readLine();
                if (response.startsWith("Joined Room")) {
                    roomCode = response.split(": ")[1];
                    JOptionPane.showMessageDialog(null, "Joined room: " + roomCode);
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to join room: " + response);
                    System.exit(1);
                }
            } else {
                writer.write("CreateRoom");
                writer.newLine();
                writer.flush();
                String response = reader.readLine();
                roomCode = response.split(": ")[1];
                JOptionPane.showMessageDialog(null, "Created room: " + roomCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            reconnectToServer();
        }
    }

    public void reconnectToServer() {
        try {
            System.out.println("Lost connection, reconnecting...");
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            connect();
        } catch (Exception e) {
            System.out.println("Reconnection failed: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Failed to reconnect to server");
            System.exit(1);
        }
    }

    public void sendMapInf(int[] levels, String[] items) {

    }

    public void sendPlayerPosition(Frog frog) {
        try {
            Vector2D velocity = frog.getTotalVelocity();

            String positionUpdate =
                    "game;"
                    + frog.pid + ";"
                    + (Math.round(frog.pos.getX() * 100)/100) + ";"
                    + (Math.round(frog.pos.getY() * 100)/100) + ";"
                    + frog.type + ";"
                    + frog.activeSprite.name + ";"
                    + (frog.isFlip ? "1" : "0") + ";"
                    + (Math.round(velocity.getX() * 100)/100) + ";"
                    + (Math.round(velocity.getY() * 100)/100);
            writer.write(positionUpdate);
            writer.newLine();
            writer.flush();
        } catch (Exception e) {
            System.out.println("Position sending error: " + e.getMessage());
            reconnectToServer();
        }
    }

    public void receiveMapInfo() {

    }

    public void receiveUpdates(HashMap<String, Frog> playerList) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"))) {
            String message;
            while ((message = reader.readLine()) != null && message.startsWith("game;")) {
                String[] data = message.split(";");
                String pid = data[1].trim();

                double x = Double.parseDouble(data[2].trim());
                double y = Double.parseDouble(data[3].trim());
                String type = data[4].trim();
                String spriteName = data[5].trim();
                boolean isFlip = Objects.equals(data[6].trim(), "1");
                double vx = Double.parseDouble(data[7].trim());
                double vy = Double.parseDouble(data[8].trim());

                if (playerList.containsKey(pid)) {
                    Frog frog = playerList.get(pid);
                    frog.pos.setX(x);
                    frog.pos.setY(y);
//                    frog.setVelocityX("dummy", vx);
//                    frog.setVelocityY("dummy", vy);
                    frog.setVelocityX("dummy", 0);
                    frog.setVelocityY("dummy", 0);
                    frog.activeSprite = game.getSprite(spriteName);
                    frog.isFlip = isFlip;
                } else {
                    Frog frog;

                    switch (type) {
                        case "man": frog = new ManFrog(x, y, game); break;
                        case "ninja": frog = new NinjaFrog(x, y, game); break;
                        case "normal": frog = new NormalFrog(x, y, game); break;
                        case "ox": frog = new OxFrog(x, y, game); break;
                        case "space": frog = new SpaceFrog(x, y, game); break;
                        case "umbrella": frog = new UmbrellaFrog(x, y, game); break;
                        case "witch": frog = new WitchFrog(x, y, game); break;
                        default: frog = null;
                    }

                    frog.activeSprite = game.getSprite(spriteName);
                    frog.isFlip = isFlip;
                    frog.pid = pid;
                    frog.isDummy = true;
                    frog.addVelocity("dummy");
                    playerList.put(pid, frog);

                    game.addEntity(frog);
                }
            }
        } catch (Exception e) {
            System.out.println("Update receiving error: " + e.getMessage());
            reconnectToServer();
        }
    }
}
