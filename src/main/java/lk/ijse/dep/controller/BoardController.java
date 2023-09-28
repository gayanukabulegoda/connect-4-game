package lk.ijse.dep.controller;

import com.jfoenix.controls.JFXButton;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lk.ijse.dep.service.*;

public class BoardController implements BoardUI {

    private static final int RADIUS = 42;

    public Label lblStatus;
    public Group grpCols;
    public AnchorPane root;
    public Pane pneOver;
    public JFXButton btnPlayAgain;

    private String playerName;
    private boolean isAiPlaying;
    private boolean isGameOver;

    private Player humanPlayer;
    private Player aiPlayer;

    private void initializeGame() {
        Board newBoard = new BoardImpl(this);
        humanPlayer = new HumanPlayer(newBoard);
        aiPlayer = new AiPlayer(newBoard);
    }

    public void initialize() {
        initializeGame();
        grpCols.getChildren().stream().map(n -> (VBox) n).forEach(vbox -> vbox.setOnMouseClicked(mouseEvent -> colOnClick(vbox)));
    }

    private void colOnClick(VBox col) {
        if (!isAiPlaying && !isGameOver) humanPlayer.movePiece(grpCols.getChildren().indexOf(col));
    }

    public void initData(String playerName) {
        this.playerName = playerName;
    }

    @Override
    public void update(int col, boolean isHuman) {
        if (isGameOver) return;
        VBox vCol = (VBox) grpCols.lookup("#col" + col);
        if (vCol.getChildren().size() == 5)
            throw new RuntimeException("Double check your logic, no space available within the column: " + col);
        if (!isHuman) {
            vCol.getStyleClass().add("col-ai");
        }
        Circle circle = new Circle(RADIUS);
        circle.getStyleClass().add(isHuman ? "circle-human" : "circle-ai");
        vCol.getChildren().add(0, circle);
        if (vCol.getChildren().size() == 5) vCol.getStyleClass().add("col-filled");
        TranslateTransition tt = new TranslateTransition(Duration.millis(250), circle);
        tt.setFromY(-50);
        tt.setToY(circle.getLayoutY());
        tt.playFromStart();
        lblStatus.getStyleClass().clear();
        lblStatus.getStyleClass().add(isHuman ? "ai" : "human");
        if (isHuman) {
            isAiPlaying = true;
            grpCols.getChildren().stream().map(n -> (VBox) n).forEach(vbox -> vbox.getStyleClass().remove("col-human"));
            KeyFrame delayFrame = new KeyFrame(Duration.millis(300), actionEvent -> {
                if (!isGameOver) lblStatus.setText("Wait, AI is playing");
            });
            KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.5), actionEvent -> {
                if (!isGameOver) aiPlayer.movePiece(-1);
            });
            new Timeline(delayFrame, keyFrame).playFromStart();
        } else {
            KeyFrame delayFrame = new KeyFrame(Duration.millis(300), actionEvent -> {
                grpCols.getChildren().stream().map(n -> (VBox) n).forEach(vbox -> {
                    vbox.getStyleClass().remove("col-ai");
                    vbox.getStyleClass().add("col-human");
                });
            });
            new Timeline(delayFrame).playFromStart();
            isAiPlaying = false;
            lblStatus.setText(playerName + ", it is your turn now!");
        }
    }

    @Override
    public void notifyWinner(Winner winner) {
        isGameOver = true;
        lblStatus.getStyleClass().clear();
        lblStatus.getStyleClass().add("final");
        switch (winner.getWinningPiece()) {
            case BLUE:
                lblStatus.setText(String.format("%s, you have won the game !", playerName));
                break;
            case GREEN:
                lblStatus.setText("Game is over, AI has won the game !");
                break;
            case EMPTY:
                lblStatus.setText("Game is tied !");
        }
        if (winner.getWinningPiece() != Piece.EMPTY) {
            VBox vCol = (VBox) grpCols.lookup("#col" + winner.getCol1());
            Rectangle rect = new Rectangle((winner.getCol2() - winner.getCol1() + 1) * vCol.getWidth(),
                    (winner.getRow2() - winner.getRow1() + 1) * (((RADIUS + 2) * 2)));
            rect.setId("rectOverlay");
            root.getChildren().add(rect);
            rect.setLayoutX(vCol.localToScene(0, 0).getX());
            rect.setLayoutY(vCol.localToScene(0, 0).getY() + (4 - winner.getRow2()) * ((RADIUS + 2) * 2));
            rect.getStyleClass().add("winning-rect");
        }
        pneOver.setVisible(true);
        pneOver.toFront();
        Platform.runLater(btnPlayAgain::requestFocus);
    }

    public void btnPlayAgainOnAction(ActionEvent actionEvent) {
        initializeGame();
        isAiPlaying = false;
        isGameOver = false;
        pneOver.setVisible(false);
        lblStatus.getStyleClass().clear();
        lblStatus.setText("LET'S PLAY !");
        grpCols.getChildren().stream().map(n -> (VBox) n).forEach(vbox -> {
            vbox.getChildren().clear();
            vbox.getStyleClass().remove("col-ai");
            vbox.getStyleClass().remove("col-filled");
            vbox.getStyleClass().add("col-human");
        });
        root.getChildren().remove(root.lookup("#rectOverlay"));
    }
}
