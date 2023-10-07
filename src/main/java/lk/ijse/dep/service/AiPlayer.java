package lk.ijse.dep.service;

public class AiPlayer extends Player {

    public AiPlayer(Board board) {
        super(board);
    }

    @Override
    public void movePiece(int colRandom) {

        do {
            // *6 bcz Math.random() generates random decimal no.s between 0.0 and 1.0
            colRandom =  (int) (Math.random() * 6);

        } while (!(colRandom > -1 && colRandom < 6) || !(board.isLegalMove(colRandom)));

        if (board.isLegalMove(colRandom)) {
            board.updateMove(colRandom, Piece.GREEN);
            board.getBoardUI().update(colRandom, false);

            if (board.findWinner().getWinningPiece() == Piece.EMPTY) {

                if (!board.existLegalMoves()) {
                    board.getBoardUI().notifyWinner(board.findWinner());
                }
            }
            else board.getBoardUI().notifyWinner(board.findWinner());
        }
    }
}
