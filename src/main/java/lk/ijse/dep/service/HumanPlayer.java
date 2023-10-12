package lk.ijse.dep.service;

public class HumanPlayer extends Player {

    public HumanPlayer(Board board) {
        super(board);
    }

    @Override
    public void movePiece(int col) {
        if (board.isLegalMove(col)) {
            int row = board.findNextAvailableSpot(col);

            board.updateMove(col, row, Piece.BLUE);
            board.getBoardUI().update(col, true);

            if (board.findWinner().getWinningPiece() != Piece.EMPTY || !board.existLegalMoves()) {

                    board.getBoardUI().notifyWinner(board.findWinner());
            }
        }
    }
}

/*
 * Implements Player's abstract behaviours
 * Depicts Human Player's functionality
 */