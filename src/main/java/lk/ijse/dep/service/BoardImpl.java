package lk.ijse.dep.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BoardImpl implements Board {
    private final Piece[][] pieces;
    private final BoardUI boardUI;

    //Variables Created For MCTS Alogorithm's function
    public Piece piece;
    public int cols;

    public BoardImpl(BoardUI boardUI) {
        this.boardUI = boardUI;

        //Initialize 2D Array
        pieces = new Piece[NUM_OF_COLS][NUM_OF_ROWS];

        //Emptying all the spots of the board
        for (int i = 0;i < NUM_OF_COLS;i++) {
            for (int j = 0;j < NUM_OF_ROWS;j++){
                pieces[i][j] = Piece.EMPTY;
            }
        }
    }

    /**
     * @return the memory location of BoardUI
     */
    @Override
    public BoardUI getBoardUI() {
        return this.boardUI;
    }

    /**
     * @return first empty space in the specified column
     */
    @Override
    public int findNextAvailableSpot(int col) {
        //Check whether any row is Empty and return row's value
        for (int i = 0;i < NUM_OF_ROWS;i++){
            if ( pieces[col][i] == Piece.EMPTY ){
                return i;
            }
        }
        //if no any Empty row return -1
        return -1;
    }

    /**
     * @return boolean representing whether current move is a legal move or not
     * (of the specified column)
     */
    @Override
    public boolean isLegalMove(int col) {
        return this.findNextAvailableSpot(col) > -1;
    }

    /**
     * @return boolean representing whether any empty spot available in the whole board
     */
    @Override
    public boolean existLegalMoves() {
        for (int i = 0; i < NUM_OF_COLS; i++) {
            if (this.isLegalMove(i)) {
                return true;
            }
        }
        return false;
    }

    /**
     * update the board by putting Piece(BLUE/GREEN) in the first free row of specified column
     * @param col - specified column
     * @param move - BLUE Piece / GREEN Piece
     */
    @Override
    public void updateMove(int col, Piece move) {
        //Assign specified column, move(piece) for cols,piece variables respectively for alogirthm's fuction
        this.cols=col;
        this.piece=move;

        pieces[col][findNextAvailableSpot(col)] = move;
    }

    /**
     * update the board by putting Piece(BLUE/GREEN) in the first free row of specified column & row
     * @param col - specified column
     * @param row - specified row
     * @param move - BLUE Piece / GREEN Piece
     */
    @Override
    public void updateMove(int col, int row, Piece move) {
        pieces[col][row] = move;
    }

    /**
     * check if either player has already connected four pieces Horizontally / Vertically
     * @return Winner instance with winning piece (& co-ordinates)
     */
    @Override
    public Winner findWinner() {

        for (int col = 0; col < NUM_OF_COLS; col++) {
            for (int row = 0; row < NUM_OF_ROWS; row++) {
                Piece currentPiece = pieces[col][row];

                //Check Current Piece is EMPTY or not
                if (currentPiece != Piece.EMPTY) {

                    // Check Vertically
                    if (row + 3 < NUM_OF_ROWS &&
                            currentPiece == pieces[col][row + 1] &&
                            currentPiece == pieces[col][row + 2] &&
                            currentPiece == pieces[col][row + 3]) {

                        return new Winner(currentPiece, col, row, col, row + 3);
                    }

                    // Check Horizontally
                    if (col + 3 < NUM_OF_COLS &&
                            currentPiece == pieces[col + 1][row] &&
                            currentPiece == pieces[col + 2][row] &&
                            currentPiece == pieces[col + 3][row]) {

                        return new Winner(currentPiece, col, row, col + 3, row);
                    }
                }
            }
        }
        // Match Tied (No winner yet)
        return new Winner(Piece.EMPTY);
    }

    ////////////-> M C T S ALGORITHM <-/////////////

    /**
     * consrtructor for creating new BoardImpl objects via getAllLegalNextMoves method
     * @param pieces - existing (pieces) 2D array
     * @param boardUI - catches BoardUI interface arributes & behaviours
     */
    public BoardImpl(Piece[][] pieces, BoardUI boardUI){
        //New 2D array (new board) created
        this.pieces = new Piece[NUM_OF_COLS][NUM_OF_ROWS];

        //Copies existing 2D array to newly created array here (initializes new 2D array)
        for (int i = 0;i < NUM_OF_COLS;i++){
            for (int j = 0;j < NUM_OF_ROWS;j++){
                this.pieces[i][j] = pieces[i][j];
            }
        }
        this.boardUI = boardUI;
    }

    //checks the all next legal moves while expanding the tree (creating child nodes)
    public List<BoardImpl> getAllLegalNextMoves() {
        Piece nextPiece = piece == Piece.BLUE? Piece.GREEN : Piece.BLUE;

        List<BoardImpl> nextMoves = new ArrayList<>();
        for (int col = 0; col < NUM_OF_COLS; col++) {
            if (findNextAvailableSpot(col) > -1) {
                BoardImpl legalMove = new BoardImpl(this.pieces,this.boardUI);
                legalMove.updateMove(col, nextPiece);
                nextMoves.add(legalMove);
            }
        }
        return nextMoves;
    }

    //randomly select child node just after expanding the parent node
    public BoardImpl getRandomLegalNextMove(){
        final  List<BoardImpl> legalMoves = getAllLegalNextMoves();
        if (legalMoves.isEmpty()) {
            return null;
        }
        //generates a Random int number (within legalMoves array length) in-order-to get a random move
        final int random;
        random = new Random().nextInt(legalMoves.size());
        return legalMoves.get(random);
    }

    //decide whether there's any empty piece or not
    public boolean getStatus(){
        if (!existLegalMoves()) return false;

        Winner winner = findWinner();
        return winner.getWinningPiece() == Piece.EMPTY;
    }
}

