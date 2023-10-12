package lk.ijse.dep.service;

import java.util.*;

public class AiPlayer extends Player {

    public AiPlayer(Board board) {
        super(board);
    }

    @Override
    public void movePiece(int col) {

        /*do {
            // *6 bcz Math.random() generates random decimal no.s between 0.0 and 1.0
            col =  (int) (Math.random() * 6);

        } while (!(col > -1 && col < 6) || !(board.isLegalMove(col)));*/

        ////////////-> M C T S ALGORITHM <-/////////////

                             //return the boardImpl object
        Mcts mcts = new Mcts((BoardImpl) board);
        col = mcts.startMCTS();

        ////////////-> M C T S ALGORITHM <-/////////////

        if (board.isLegalMove(col)) {
            int row = board.findNextAvailableSpot(col);
            board.updateMove(col, row, Piece.GREEN);
            board.getBoardUI().update(col, false);

            if (board.findWinner().getWinningPiece() != Piece.EMPTY || !board.existLegalMoves()) {

                    board.getBoardUI().notifyWinner(board.findWinner());
            }
        }
    }

    ////////////-> M C T S ALGORITHM <-/////////////

    static class Mcts {
        //inorder to catch boardImpl 's board (2D array)
        private final BoardImpl board;

        //assign boardImpl 's board
        public Mcts(BoardImpl board) {
            this.board = board;
        }

        //initialize MCTS
        public int startMCTS(){
            System.out.println("MCTS Started...");
            int count=0;

            //Creates New node object
            Node tree= new Node(board);

            //Recurring Amount that MCTS checking(s) need to be carry-on
            while (count<4000){
                count++;

                //Selected Node
                Node promisingNode = selectPromisingNode(tree);

                //Expand Node
                Node selected=promisingNode;

                if (selected.board.getStatus()){
                    selected= expandNodeAndReturnRandom(promisingNode);
                }

                //Simulate
                Piece resultPiece=simulateLightPlayout(selected);

                //BackPropagation
                backPropagation(resultPiece,selected);
            }

            Node best= tree.getChildWithMaxScore();

            System.out.println("Best move scored " + best.score + " and was visited " + best.visits + " times\nMCTS Ended...");

            return best.board.cols;
        }
        //backpropagation step
        private void backPropagation(Piece resultPiece, Node selected) {

            Node node=selected;

            while (node!=null){
                node.visits++;

                if (node.board.piece==resultPiece){
                    node.score++;
                }
                node = node.parent;
            }
        }
        //simulate to decide whether the best node inorder to select for the move
        private Piece simulateLightPlayout(Node promisingNode) {

            Node node=new Node(promisingNode.board);
            node.parent=promisingNode.parent;

            Winner winner=node.board.findWinner();

            if (winner.getWinningPiece()==Piece.BLUE){
                node.parent.score=Integer.MIN_VALUE;

                return node.board.findWinner().getWinningPiece();
            }

            while (node.board.getStatus()){
                BoardImpl nextMove=node.board.getRandomLegalNextMove();
                Node child = new Node(nextMove);
                child.parent=node;
                node.addChild(child);
                node=child;
            }
            return node.board.findWinner().getWinningPiece();
        }
        //Expands the tree via creating child nodes by dividing the parent node
        private Node expandNodeAndReturnRandom(Node node) {

            BoardImpl board= node.board;
            List<BoardImpl> legalMoves= board.getAllLegalNextMoves();

            for (BoardImpl move : legalMoves) {
                Node child = new Node(move);
                child.parent = node;
                node.addChild(child);
            }

            //generates a Random int number (within children array length) in-order-to return a random Node
            int random = new Random().nextInt(node.children.size());

            return node.children.get(random);
        }
        //select & return the best node with highest UCT value after backpropagation
        private Node selectPromisingNode(Node tree) {
            Node node=tree;
            while (node.children.size()!=0){
                node=UCT.findBestNodeWithUCT(node);
            }
            return node;
        }
    }
    static class Node {
        //catch the boardImpl's current board (2D array)
        public BoardImpl board;
        //counts the no.of visits via backpropagation phase
        public int visits;
        //generate a score (according to best selection path) via backpropagation phase
        public int score;
        //children array holds each & every possible moves as nodes
        List<Node> children= new ArrayList<>();
        //every child has a parent child's reference (so via backpropagation it selects the highest UCT valued path to drop the ball)
        Node parent=null;
        //catches boardimpl's board
        public Node(BoardImpl board) {
            this.board = board;
        }
        //return the highest valued child via backpropagation (if Ai won +1 ; fail -1)
        Node getChildWithMaxScore() {
            Node result = children.get(0);
            for (int i = 1; i < children.size(); i++) {
                if (children.get(i).score > result.score) {
                    result = children.get(i);
                }
            }
            return result;
        }
        //add new node to children arrayList
        void addChild(Node node) {
            children.add(node);
        }
    }
    //use-case test (UCT) / UpperConfidenceBound - test the quality characteristics of sustainability, effectivity
    static class UCT {
        //UCT Value calculate via selection phase
        public static double uctValue(
            int totalVisit, double nodeWinScore, int nodeVisit) {
            if (nodeVisit == 0) {
                return Integer.MAX_VALUE;
            }
            return (nodeWinScore / (double) nodeVisit)
                    + 1.41 * Math.sqrt(Math.log(totalVisit) / (double) nodeVisit);
        }
        //Find the best node with highest UCT value
        public static Node findBestNodeWithUCT(Node node) {
            int parentVisit = node.visits;
            return Collections.max(
                    node.children,
                    Comparator.comparing(c -> uctValue(parentVisit,
                            c.score, c.visits)));
        }
    }
}

