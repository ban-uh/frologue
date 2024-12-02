package com.banuh.frologue.game.scenes;

import com.banuh.frologue.App;
import com.banuh.frologue.game.GlobalVariables;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.IOException;

public class waitingPage extends Application {
    Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        if (!GlobalVariables.isHost) {
            new Thread(() ->
                GlobalVariables.server.pongStart(this)
            ).start();
        }

        this.primaryStage = primaryStage;
        // 창 크기
        int width = 900;
        int height = 600;

        // Pane 레이아웃 생성
        Pane layout = new Pane();

        // Canvas 생성
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setImageSmoothing(false); // 이미지 확대 시 보간 비활성화

        // 배경 이미지 그리기
        Image waitingImage = new Image("file:src/main/resources/img/background/waiting-room.png");
        gc.drawImage(waitingImage, 0, 0, width, height);

        // Start 버튼 이미지
        Image startButtonImage = new Image("file:src/main/resources/img/ui/start-button.png");
        double startButtonWidth = 200;
        double startButtonHeight = 200;
        double startButtonX = (width - startButtonWidth) / 2; // 정중앙
        double startButtonY = 400; // 하단 여백
        gc.drawImage(startButtonImage, startButtonX, startButtonY, startButtonWidth, startButtonHeight);

        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (GlobalVariables.isHost) {
                startGame();
            } else {
                JOptionPane.showMessageDialog(null, "호스트만 게임을 진행할 수 있습니다.");
            }
        });

        // Character Select 버튼 이미지
        Image characterSelectImage = new Image("file:src/main/resources/img/ui/charecter-select-button.png");
        double characterSelectWidth = 170;
        double characterSelectHeight = 120;
        double characterSelectX = 50; // 오른쪽 여백
        double characterSelectY = 480; // 하단 여백
        gc.drawImage(characterSelectImage, characterSelectX, characterSelectY, characterSelectWidth, characterSelectHeight);

        // Canvas 추가
        layout.getChildren().add(canvas);

        // Character Select 버튼 클릭 이벤트 (투명한 클릭 영역 처리)
        Pane transparentButton = new Pane();
        transparentButton.setPrefSize(characterSelectWidth, characterSelectHeight);
        transparentButton.setLayoutX(characterSelectX);
        transparentButton.setLayoutY(characterSelectY);
        transparentButton.setOnMouseClicked(event -> openCharacterSelectModal());
        layout.getChildren().add(transparentButton);

        // Scene 생성
        Scene scene = new Scene(layout, width, height);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Waiting Page");
        primaryStage.show();
    }

    // 모달 창 열기 메서드
    private void openCharacterSelectModal() {
        // 모달 창 Stage 생성
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL); // 모달 창 설정
        modalStage.initStyle(javafx.stage.StageStyle.TRANSPARENT); // 모달 창 투명 배경 설정
        modalStage.setTitle("Character Select");

        // 모달 창 크기
        int modalWidth = 750;
        int modalHeight = 450;

        // Pane 레이아웃 생성
        Pane modalLayout = new Pane();

        // Canvas 생성
        Canvas modalCanvas = new Canvas(modalWidth, modalHeight);
        GraphicsContext modalGc = modalCanvas.getGraphicsContext2D();
        modalGc.setImageSmoothing(false); // 이미지 확대 시 보간 비활성화
        modalLayout.getChildren().add(modalCanvas);

        // SelectEx 이미지 그리기
        Image selectExImage = new Image("file:src/main/resources/img/ui/charecter-select.png");
        modalGc.drawImage(selectExImage, 0, 0, modalWidth, modalHeight);

        // 개구리 이미지 위치와 크기 정의
        double[][] frogData = {
                {483, 35, 70, 70}, // nomalFrogImage
                {563, 35, 70, 70}, // ninjafrogImage
                {643, 35, 70, 70}, // oxfrogImage
                {483, 115, 70, 70}, // spacefrogImage
                {563, 115, 70, 70}, // umbrellafrogImage
                {643, 115, 70, 70}  // witchfrogImage
        };

        Image[] frogImages = {
                new Image("file:src/main/resources/img/icon/nomalfrog.png"),
                new Image("file:src/main/resources/img/icon/ninjafrog.png"),
                new Image("file:src/main/resources/img/icon/oxfrog.png"),
                new Image("file:src/main/resources/img/icon/spacefrog.png"),
                new Image("file:src/main/resources/img/icon/umbrellafrog.png"),
                new Image("file:src/main/resources/img/icon/witchfrog.png")
        };

        // 선택된 블록을 추적하는 변수
        Pane selectedBlock = new Pane();
        selectedBlock.setStyle("-fx-background-color: rgba(0, 0, 0, 0.45);");
        selectedBlock.setVisible(false);
        modalLayout.getChildren().add(selectedBlock);

        for (int i = 0; i < frogImages.length; i++) {
            double x = frogData[i][0];
            double y = frogData[i][1];
            double width = frogData[i][2];
            double height = frogData[i][3];

            // 이미지를 그리기
            modalGc.drawImage(frogImages[i], x, y, width, height);

            // 블록의 클릭 영역 정의
            Pane frogBlock = new Pane();
            frogBlock.setPrefSize(width, height);
            frogBlock.setLayoutX(x);
            frogBlock.setLayoutY(y);
            frogBlock.setStyle("-fx-background-color: transparent;"); // 투명 클릭 영역
            modalLayout.getChildren().add(frogBlock);

            // 상호작용: 마우스 모양 변경
            frogBlock.setOnMouseEntered(event -> frogBlock.setCursor(javafx.scene.Cursor.HAND));
            frogBlock.setOnMouseExited(event -> frogBlock.setCursor(javafx.scene.Cursor.DEFAULT));

            // 클릭 이벤트 처리
            frogBlock.setOnMouseClicked(event -> {
                double scaleFactor = 0.9;
                selectedBlock.setLayoutX(x + (width * (1 - scaleFactor)) / 2); // 중앙 정렬
                selectedBlock.setLayoutY(y + (height * (1 - scaleFactor)) / 2); // 중앙 정렬
                selectedBlock.setPrefSize(width * scaleFactor, height * scaleFactor); // 크기 축소
                selectedBlock.setVisible(true); // 블록 보이기
            });
        }

        // 닫기 버튼 이미지 및 위치 설정
        Image closeImage = new Image("file:src/main/resources/img/ui/close.png");
        double closeButtonWidth = 30;
        double closeButtonHeight = 30;
        double closeButtonX = 30;
        double closeButtonY = 30;
        modalGc.drawImage(closeImage, closeButtonX, closeButtonY, closeButtonWidth, closeButtonHeight);

        // 닫기 버튼 클릭 이벤트 처리
        modalCanvas.setOnMouseClicked(event -> {
            double mouseX = event.getX();
            double mouseY = event.getY();

            // 닫기 버튼 클릭 확인
            if (mouseX >= closeButtonX && mouseX <= closeButtonX + closeButtonWidth &&
                    mouseY >= closeButtonY && mouseY <= closeButtonY + closeButtonHeight) {
                modalStage.close(); // 모달 창 닫기
            }
        });

        // Scene 생성 및 배경 투명 설정
        Scene modalScene = new Scene(modalLayout, modalWidth, modalHeight);
        modalScene.setFill(javafx.scene.paint.Color.TRANSPARENT); // Scene 배경 투명 설정
        modalStage.setScene(modalScene);

        // 모달 창 표시
        modalStage.showAndWait();
    }

    public void startGame() {
        primaryStage.close();

        if (GlobalVariables.isHost) {
            GlobalVariables.server.pingStart();
        }

        App app = new App();
        Stage gameStage = new Stage();

        try {
            app.start(gameStage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}