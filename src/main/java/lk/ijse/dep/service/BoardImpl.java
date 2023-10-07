package lk.ijse.dep.service;

import lk.ijse.dep.controller.BoardController;

public class BoardImpl implements Board {
    private Piece[][] pieces;
    private BoardUI boardUI;
    public BoardImpl(BoardUI boardUI) {
        this.boardUI = boardUI;
        pieces = new Piece[NUM_OF_COLS][NUM_OF_ROWS];

        //emptying the pieces of the board
        for (int i = 0;i < NUM_OF_COLS;i++) {
            for (int j = 0;j < NUM_OF_ROWS;j++){
                pieces[i][j] = Piece.EMPTY;
            }
        }
    }

    @Override
    public BoardUI getBoardUI() {
        return this.boardUI;
    }

    @Override
    public int findNextAvailableSpot(int col) {
        //check whether any piece is Empty and return row's value
        for (int i = 0;i < NUM_OF_ROWS;i++){
            if ( pieces[col][i] == Piece.EMPTY ){
                return i;
            }
        }
        //if no any Empty piece return -1
        return -1;
    }

    @Override
    public boolean isLegalMove(int col) {
        //decide whether there is an empty piece within given column
        /*int row = findNextAvailableSpot(col);
        if (row > -1) return true;
        return false;*/
        return this.findNextAvailableSpot(col) > -1;
    }

    @Override
    public boolean existLegalMoves() {
        //checks there's any empty piece available in the whole board
        /*for (int i = 0;i < NUM_OF_COLS;i++) {
            for (int j = 0;j < NUM_OF_ROWS;j++){
                if (pieces[i][j] == Piece.EMPTY) {
                    return true;
                }
            }
        }
        return false;*/
        for (int i = 0; i < NUM_OF_COLS; i++) {
            if (this.isLegalMove(i)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void updateMove(int col, Piece move) {
        //assign BLUE/GREEN (move) if the given column's piece EMPTY
        /*for (int i = 0;i < NUM_OF_ROWS;i++) {
            if (pieces[col][i] == Piece.EMPTY) {
                pieces[col][i] = move;
                break;
            }
        }*/
        pieces[col][findNextAvailableSpot(col)] = move;
    }

    @Override
    public void updateMove(int col, int row, Piece move) {
        /*if (pieces[col][row] == Piece.EMPTY) {
            pieces[col][row] = move;
        }*/
        pieces[col][row] = move;
    }

    @Override
    public Winner findWinner() {
        //decides the winner via vertical & horizontal checking

        for (int col = 0; col < NUM_OF_COLS; col++) {
            for (int row = 0; row < NUM_OF_ROWS; row++) {
                Piece currentPiece = pieces[col][row];
                    //Check Current Piece EMPTY or not
                    if (currentPiece != Piece.EMPTY) {
                        // Check vertically
                        if (row + 3 < NUM_OF_ROWS &&
                                currentPiece == pieces[col][row + 1] &&
                                currentPiece == pieces[col][row + 2] &&
                                currentPiece == pieces[col][row + 3]) {

                            return new Winner(currentPiece, col, row, col, row + 3);
                        }

                        // Check horizontally
                        if (col + 3 < NUM_OF_COLS &&
                                currentPiece == pieces[col + 1][row] &&
                                currentPiece == pieces[col + 2][row] &&
                                currentPiece == pieces[col + 3][row]) {
                            return new Winner(currentPiece, col, row, col + 3, row);
                        }
                    }
            }
        }
        // Match tied (No winner yet)
        return new Winner(Piece.EMPTY);
    }
}

