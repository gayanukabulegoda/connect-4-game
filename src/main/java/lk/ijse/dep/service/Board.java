package lk.ijse.dep.service;

public interface Board {
    int NUM_OF_ROWS = 5;
    int NUM_OF_COLS = 6;
    BoardUI getBoardUI();
    int finalNextAvailableSpot(int col);
    boolean isLegalMove(int col);
    boolean exitLegalMoves();
    void updateMove(int col, Piece move);
    Winner findWinner();
}
