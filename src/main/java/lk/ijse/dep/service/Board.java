package lk.ijse.dep.service;

import java.util.Random;

public interface Board {
    int NUM_OF_ROWS = 5;
    int NUM_OF_COLS = 6;
    Random RANDOM_GENERATOR = new Random();

    BoardUI getBoardUI();
    int findNextAvailableSpot(int col);
    boolean isLegalMove(int col);
    boolean existLegalMoves();
    void updateMove(int col, Piece move);
    void updateMove(int col, int row, Piece move);
    Winner findWinner();
    BoardImpl getBoardImpl();
}
