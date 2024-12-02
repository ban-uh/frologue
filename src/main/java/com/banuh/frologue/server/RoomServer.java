package com.banuh.frologue.server;

import com.banuh.frologue.core.Game;
import com.banuh.frologue.core.utils.Vector2D;
import com.banuh.frologue.game.frog.*;
import com.banuh.frologue.game.scenes.PlayScene;
import com.banuh.frologue.game.scenes.waitingPage;
import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Objects;

public class RoomServer {
    public Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;
    private String roomCode;
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

    public String create() {
        try {
            writer.write("CreateRoom");
            writer.newLine();
            writer.flush();
            String response = reader.readLine();
            roomCode = response.split(": ")[1];
            JOptionPane.showMessageDialog(null, "Created room: " + roomCode);

            return roomCode;
        } catch (Exception e) {
            e.printStackTrace();
            reconnectToServer();

        }
        return null;
    }

    public void connect() {
        try {
            socket = new Socket("127.0.0.1", 1444);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            reconnectToServer();
        }
    }

    public void pingStart() {
        try {
            writer.write("start;");
            writer.newLine();
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
            reconnectToServer();
        }
    }

    public void pongStart(waitingPage page) {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                if (message.startsWith("start;")) {
                    Platform.runLater(page::startGame);
                    break; // 메시지 읽기 종료
                }
            }
        } catch (Exception e) {
            System.out.println("Pong Start: " + e.getMessage());
            reconnectToServer();
        }
    }

//    public void connect() {
//        try {
//            socket = new Socket("127.0.0.1", 1444);
//            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
//            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
//
//            System.out.println("check");
//            String roomInput = JOptionPane.showInputDialog(null,
//                    "Enter room code to join, or leave blank to create new room:");
//
//            if (roomInput != null && !roomInput.trim().isEmpty()) {
//                writer.write("JoinRoom " + roomInput.trim());
//                writer.newLine();
//                writer.flush();
//                String response = reader.readLine();
//                if (response.startsWith("Joined Room")) {
//                    roomCode = response.split(": ")[1];
//                    JOptionPane.showMessageDialog(null, "Joined room: " + roomCode);
//                } else {
//                    JOptionPane.showMessageDialog(null, "Failed to join room: " + response);
//                    System.exit(1);
//                }
//            } else {
//                writer.write("CreateRoom");
//                writer.newLine();
//                writer.flush();
//                String response = reader.readLine();
//                roomCode = response.split(": ")[1];
//                JOptionPane.showMessageDialog(null, "Created room: " + roomCode);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            reconnectToServer();
//        }
//    }

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

    public void sendMapInfo(int[] levels) {
        try {
            String levelsText = "game:map:" + levels[0] + "," + levels[1] + "," + levels[2] + "," + levels[3] + "," + levels[4];
            writer.write(levelsText);
            writer.newLine();
            writer.flush();
        } catch (Exception e) {
            System.out.println("Position sending error: " + e.getMessage());
            reconnectToServer();
        }
    }

//    public void receiveMapInfo(int[] levels) {
//        try {
//            String levelsText = levels[0] + ";" + levels[1] + ";" + levels[2] + ";" + levels[3] + ";" + levels[4];
//            writer.write(levelsText);
//            writer.newLine();
//            writer.flush();
//        } catch (Exception e) {
//            System.out.println("Position sending error: " + e.getMessage());
//            reconnectToServer();
//        }
//    }

    public void sendPlayerPosition(Frog frog) {
        try {
            Vector2D velocity = frog.getTotalVelocity();

            String positionUpdate =
                    "game:update:"
                    + frog.pid + ","
                    + (Math.round(frog.pos.getX() * 100)/100) + ","
                    + (Math.round(frog.pos.getY() * 100)/100) + ","
                    + frog.type + ","
                    + frog.activeSprite.name + ","
                    + (frog.isFlip ? "1" : "0") + ","
                    + (Math.round(velocity.getX() * 100)/100) + ","
                    + (Math.round(velocity.getY() * 100)/100);
            writer.write(positionUpdate);
            writer.newLine();
            writer.flush();
        } catch (Exception e) {
            System.out.println("Position sending error: " + e.getMessage());
            reconnectToServer();
        }
    }

    public void receiveUpdates(PlayScene scene) {
        HashMap<String, Frog> playerList = scene.playerList;
        try {
            String message;
            while ((message = reader.readLine()) != null && message.startsWith("game:")) {
                String[] command = message.split(":");
                String opcode = command[1].trim();

                if (opcode.equals("update")) {
                    String[] data = command[2].split(",");
                    String pid = data[0].trim();

                    double x = Double.parseDouble(data[1].trim());
                    double y = Double.parseDouble(data[2].trim());
                    String type = data[3].trim();
                    String spriteName = data[4].trim();
                    boolean isFlip = Objects.equals(data[5].trim(), "1");
                    double vx = Double.parseDouble(data[6].trim());
                    double vy = Double.parseDouble(data[7].trim());

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
                } else if (opcode.equals("map")) {
                    String[] data = command[2].split(",");
                    int[] intData = new int[data.length];
                    for (int i = 0; i < data.length; i++) {
                        intData[i] = Integer.parseInt(data[i]);
                    }
                    scene.drawMap(intData);
                }
            }
        } catch (Exception e) {
            System.out.println("Update receiving error: " + e.getMessage());
            reconnectToServer();
        }
    }
}
