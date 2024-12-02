package com.banuh.frologue.game.scenes;

import com.banuh.frologue.game.GlobalVariables;
import com.banuh.frologue.server.RoomServer;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import com.banuh.frologue.App;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Stage;


public class StartPage extends Application {
    private double animationTimeJumpFrog = 0; // 개구리 움직임을 위한 타이밍 변수
    private boolean isHoveringStart = false; // 버튼 상호작용을 위해
    private boolean isHoveringExplanation = false;
    private boolean isHoveringExit = false;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("개구리다");

        int canvasWidth = 900;
        int canvasHeight = 600;

        Image wallpaper1 = new Image("file:src/main/resources/img/start-page/main_wallpaper1.png"); // 배경1
        Image wallpaper2 = new Image("file:src/main/resources/img/start-page/main_wallpaper2.png"); // 배경2
        Image teamNameImage = new Image("file:src/main/resources/img/start-page/team_name.png"); // 별것도아닌데어금지
        Image mainTitleImage = new Image("file:src/main/resources/img/start-page/game_title.png"); // 개구리다
        Image jumpFrogImage = new Image("file:src/main/resources/img/frog-normal/jump.png"); // 점프 개구리
        Image startButtonImage = new Image("file:src/main/resources/img/start-page/main_game_start.png"); // 게임 시작 버튼
        Image exitButtonImage = new Image("file:src/main/resources/img/start-page/main_game_end.png"); // 게임 종료 버튼

        Canvas canvas = new Canvas(canvasWidth, canvasHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setImageSmoothing(false);

        final double[] wallpaper1X = {0};
        final double[] wallpaper2X = {0};

        // 별것도아닌데어금지 이미지 크기와 위치 설정
        double teamNameScaledWidth = teamNameImage.getWidth() * 0.2;
        double teamNameScaledHeight = teamNameImage.getHeight() * 0.2;
        double teamNameX = (canvasWidth - teamNameScaledWidth) / 2;
        double teamNameY = 50;

        // 개구리다 이미지 크기와 위치 설정
        double mainTitleScaledWidth = mainTitleImage.getWidth() * 0.3;
        double mainTitleScaledHeight = mainTitleImage.getHeight() * 0.3;
        double mainTitleX = (canvasWidth - mainTitleScaledWidth) / 2;
        double mainTitleY = teamNameY + teamNameScaledHeight + 25;

        // 점프 개구리 크기와 위치 설정
        double jumpFrogScaledWidth = jumpFrogImage.getWidth() * 6;
        double jumpFrogScaledHeight = jumpFrogImage.getHeight() * 6;
        double jumpFrogX = (canvasWidth - jumpFrogScaledWidth) / 2;
        double baseJumpFrogY = canvasHeight / 4;

        // 버튼 크기와 위치 설정
        double buttonScaleFactor = 0.1;
        double buttonWidth = startButtonImage.getWidth() * buttonScaleFactor;
        double buttonHeight = startButtonImage.getHeight() * buttonScaleFactor;

        double buttonY = 480;
        double buttonGap = 50;

        // 버튼 위치
        double buttonX1 = (canvasWidth - (buttonWidth * 3 + buttonGap * 2))/2 ;
        double buttonX2 = buttonX1 + buttonWidth + buttonGap;
        double buttonX3 = buttonX2 + buttonWidth + buttonGap;

        canvas.addEventHandler(MouseEvent.MOUSE_MOVED, event -> {
            double mouseX = event.getX();
            double mouseY = event.getY();

            isHoveringStart = isMouseHovering(mouseX, mouseY, buttonX1, buttonY, buttonWidth, buttonHeight);
            isHoveringExit = isMouseHovering(mouseX, mouseY, buttonX3, buttonY, buttonWidth, buttonHeight);
        });

        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            double mouseX = event.getX();
            double mouseY = event.getY();

            if (isHoveringStart) {
                System.out.println("게임 시작 버튼 클릭됨");
                roomSelectionWindow();// 게임 방 선택 모달 창 호출
            }
            if (isHoveringExit) {
                System.out.println("게임 종료 버튼 클릭됨");
                primaryStage.close();

            }
        });

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                animationTimeJumpFrog += 0.03;

                double srcWidth = 2700;
                double srcHeight = 1800;

                wallpaper1X[0] += 0.5;
                wallpaper2X[0] += 1;

                if (wallpaper1X[0] <= -srcWidth) {
                    wallpaper1X[0] += srcWidth  ;
                }

                if (wallpaper2X[0] <= -srcWidth) {
                    wallpaper2X[0] += srcWidth;
                }

                gc.clearRect(0, 0, 900, 600);

                gc.drawImage(wallpaper1, 0 + wallpaper1X[0], 0, srcWidth, srcHeight, 0, 0, 900, 600);
                gc.drawImage(wallpaper2, 0 + wallpaper2X[0], 0, srcWidth, srcHeight, 0, 0, 900, 600);


                gc.drawImage(teamNameImage, teamNameX, teamNameY, teamNameScaledWidth, teamNameScaledHeight);
                gc.drawImage(mainTitleImage, mainTitleX, mainTitleY, mainTitleScaledWidth, mainTitleScaledHeight);

                double jumpFrogOffsetY = Math.sin(animationTimeJumpFrog) * 8;
                gc.drawImage(jumpFrogImage, jumpFrogX, baseJumpFrogY + jumpFrogOffsetY, jumpFrogScaledWidth, jumpFrogScaledHeight);

                drawButton(gc, startButtonImage, buttonX1, buttonY, buttonWidth, buttonHeight, isHoveringStart);
                drawButton(gc, exitButtonImage, buttonX3, buttonY, buttonWidth, buttonHeight, isHoveringExit);
            }
        }.start();

        Pane layout = new Pane();
        layout.getChildren().add(canvas);
        primaryStage.setScene(new Scene(layout, canvasWidth, canvasHeight));
        primaryStage.show();
    }
    private void roomSelectionWindow() {
        // 새로운 Stage 생성
        Stage roomSelectionStage = new Stage();
        roomSelectionStage.initStyle(javafx.stage.StageStyle.TRANSPARENT); // 창 스타일 투명 설정
        roomSelectionStage.initModality(javafx.stage.Modality.APPLICATION_MODAL); // 모달 창 설정

        // 레이아웃 생성
        Pane layout = new Pane();
        layout.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null))); // 투명 배경 설정

        // Canvas 생성
        Canvas canvas = new Canvas(400, 250); // Canvas 크기를 모달 창 크기에 맞게 설정
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setImageSmoothing(false); // 이미지 확대 시 깨짐 방지

        // 배경 이미지 그리기
        Image backgroundImage = new Image("file:src/main/resources/img/ui/create_room.png");
        gc.drawImage(backgroundImage, 0, 0, 400, 250); // 400x300 크기로 배경 이미지 그리기

        // Canvas를 레이아웃에 추가
        layout.getChildren().add(canvas);

        // 닫기 버튼
        Image closeImage = new Image("file:src/main/resources/img/ui/close.png");
        ImageView closeButton = new ImageView(closeImage);
        closeButton.setFitWidth(25);
        closeButton.setFitHeight(25);
        closeButton.setLayoutX(20); // 오른쪽 위 위치
        closeButton.setLayoutY(20);
        closeButton.setOnMouseClicked(event -> roomSelectionStage.close());
        layout.getChildren().add(closeButton);

        // 대기 화면으로 가는 것
        Image startAppImage = new Image("file:src/main/resources/img/ui/create-button.png");
        gc.drawImage(startAppImage, 78, 32, 240, 90); // Canvas에 버튼 이미지를 직접 그리기

        // 숫자 입력 TextField 생성
        TextField numberField = new TextField();
        numberField.setPromptText("숫자 4자리를 입력하세요");
        numberField.setLayoutX(70); // X 좌표
        numberField.setLayoutY(150); // Y 좌표 (하단으로 이동)
        numberField.setPrefWidth(152); // 너비
        numberField.setPrefHeight(35); // 높이 설정
        numberField.setStyle(
                "-fx-background-color: #80c571; " +  // 배경색 연회색
                "-fx-border-color: #171818; " +      // 테두리 색상 회색
                "-fx-border-radius: 5; " +           // 테두리 둥글게
                "-fx-background-radius: 8; " +       // 배경 둥글게
                "-fx-padding: 5; " +                 // 내부 여백
                "-fx-font-size: 25px; " +            // 글자 크기
                "-fx-text-fill: #333333; " +         // 텍스트 색상 진회색
                "-fx-font-family: 'Courier New';"  +  // 글꼴 스타일
                "-fx-border-width: 4"
        );

// 숫자만 입력되도록 필터 추가
        numberField.textProperty().addListener((observable, oldValue, newValue) -> {
//            if (!newValue.matches("\\d*")) { // 정규식: 숫자만 허용
//                numberField.setText(newValue.replaceAll("[^\\d]", ""));
//            }
            if (numberField.getText().length() > 6) { // 최대 4자리 제한
                numberField.setText(numberField.getText().substring(0, 6));
            }
        });
        layout.getChildren().add(numberField);

        Image confirmImage = new Image("file:src/main/resources/img/ui/findroom-button.png",
                300, 156, // 요청 크기: 두 배로 로드
                true, false); // 비율 유지, 보간 비활성화

        ImageView confirmButton = new ImageView(confirmImage);
        confirmButton.setOnMouseClicked(event -> {
            String roomCode = numberField.getText();

            GlobalVariables.server = new RoomServer();
            GlobalVariables.isHost = false;
            GlobalVariables.server.connect();
            GlobalVariables.server.join(roomCode);

            Stage currentStage = (Stage) canvas.getScene().getWindow();
            waitingPage waitingPageScene = new waitingPage();
            currentStage.close();
            primaryStage.close();

            Stage waitingStage = new Stage();
            waitingPageScene.start(waitingStage);
        });

        confirmButton.setFitWidth(145); // 원하는 폭
        confirmButton.setFitHeight(75); // 원하는 높이
        confirmButton.setPreserveRatio(true); // 비율 유지
        confirmButton.setSmooth(false); // 보간 비활성화

        confirmButton.setLayoutX(170); // X 좌표
        confirmButton.setLayoutY(145); // Y 좌표

// 버튼 추가
        layout.getChildren().add(confirmButton);

        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            double mouseX = event.getX();
            double mouseY = event.getY();

            double startAppImageX = 78; // X 좌표
            double startAppImageY = 32; // Y 좌표
            double startAppImageWidth = 240; // 이미지 너비
            double startAppImageHeight = 90; // 이미지 높이

// 마우스가 이미지 영역을 클릭했는지 확인
            if (mouseX >= startAppImageX && mouseX <= startAppImageX + startAppImageWidth &&
                    mouseY >= startAppImageY && mouseY <= startAppImageY + startAppImageHeight) {
                System.out.println("Waiting Page로 이동");

                GlobalVariables.server = new RoomServer();
                GlobalVariables.isHost = true;
                GlobalVariables.server.connect();
                GlobalVariables.server.create();

                try {
                    // 현재 Stage 가져오기
                    Stage currentStage = (Stage) canvas.getScene().getWindow();

                    // waitingPage 호출
                    waitingPage waitingPageScene = new waitingPage();

                    // StartPage 닫기
                    currentStage.close();
                    primaryStage.close();

                    // waitingPage 실행
                    Stage waitingStage = new Stage(); // 새 Stage 생성
                    waitingPageScene.start(waitingStage); // waitingPage 시작
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });



        // Scene 생성 및 Stage에 추가
        Scene roomScene = new Scene(layout, 400, 300);
        roomScene.setFill(Color.TRANSPARENT); // Scene 배경을 투명으로 설정
        roomSelectionStage.setScene(roomScene);

        // 모달 창 표시
        roomSelectionStage.showAndWait();
    }

    private boolean isMouseHovering(double mouseX, double mouseY, double x, double y, double width, double height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
    private void drawButton(GraphicsContext gc, Image buttonImage, double x, double y, double width, double height, boolean isHovering) {
        if (isHovering) {
            gc.drawImage(buttonImage, x - 2, y - 2, width + 4, height + 4);
        } else {
            gc.drawImage(buttonImage, x, y, width, height);
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}