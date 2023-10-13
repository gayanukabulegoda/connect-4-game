package lk.ijse.dep.service;

import java.util.*;

public class AiPlayer extends Player {

    public AiPlayer(Board board) {
        super(board);
    }

    @Override
    public void movePiece(int col) {

        /*
         * do {
            // *6 bcz Math.random() generates random decimal no.s between 0.0 and 1.0
            col =  (int) (Math.random() * 6);

           } while (!(col > -1 && col < 6) || !(board.isLegalMove(col)));
        */

        //-----------> MCTS ALGORITHM (Initialization) <-----------//

        /*
         * initializes an instance of Mcts class
         * calls startMCTS() to determine next move,
         * based on current state of the game board (BoardImpl type).
         */
                    //return the boardImpl object
        Mcts mcts = new Mcts((BoardImpl) board);
        col = mcts.startMCTS();

        //---------------------------------------------------------//

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
        //inorder to catch BoardImpl's board (2D array)
        private final BoardImpl board;

        //assign BoardImpl 's board
        public Mcts(BoardImpl board) {
            this.board = board;
        }

        /**
         * Implements MCTS algorithm.
         * To make decisions in the game;
         * repeatedly selecting promising nodes,expanding tree,
         * simulating game outcomes and updating tree's nodes.
         * Selects best move based on tree's evaluation
         * and, returns column index of selected move.
         */
        public int startMCTS(){
            System.out.println("MCTS Algorithm Started...\n");

            //In-order to keep track on number of iterations of MCTS Algorithm
            int count = 0;

            //Creates new Node object and, initialize it with the board
            Node tree= new Node(board);

            /*
             * Main loop of MCTS Algorithm
             * Continues until count is less than 4000
             * (Recurring Amount that MCTS checking(s) need to be carry-on)
             */
            while (count < 4000){
                count++;

                /*
                 * In each iteration,
                 * it selects promising node from the tree.
                 * (promisingNode = selected node)
                 * (Selection Phase - Node selection)
                 */
                Node promisingNode = selectPromisingNode(tree);

                //The selected Node initially set to the promising node
                Node selected = promisingNode;

                /*
                 * Via getStatus();
                 * it checks whether the game has reached its terminal state or not.
                 * Calls expandNodeAndReturnRandom() to expand the node with random moves,
                 * simulating game further.
                 * (Expansion Phase - Expands the Node)
                 */
                if (selected.board.getStatus()){
                    selected = expandNodeAndReturnRandom(promisingNode);
                }

                /*
                 * Simulates the selected Node
                 * and, assign winnigPiece to resultPiece.
                 * (Simulation Phase - Simulates the Node)
                 */
                Piece resultPiece = simulateLightPlayout(selected);

                /*
                 * After simulation, the results are backpropagated.
                 * That results are used to update scores and visit counts of nodes of the tree,
                 * affecting decision-making process in future iterations.
                 * (Backpropagation Phase - Backpropagates the simulated results)
                 */
                backPropagation(resultPiece,selected);
            }

            /*
             * Selects the best child node of the tree upon UCT value.
             * getChildWithMaxScore() method returns the child node having highest UCT value.
             */
            Node best = tree.getChildWithMaxScore();

            System.out.println("Best move Scored " + best.score + " | was Visited " + best.visits + " times\n\nMCTS Algorithm Ended...");

            /*
             * Returns column index of the best move to be made,
             * based on MCTS Algorithms evaluation.
             */
            return best.board.cols;
        }

        /**
         * Updates visit count and score(win count) of nodes in the tree,
         * based on outcome of simulated games.
         * @param resultPiece - assigned outcome of simulation phase
         * @param selected - assigned outcome of selection phase
         * (Backpropagation Step)
         */
        private void backPropagation(Piece resultPiece, Node selected) {

            //Initializes a Node object to selected node
            Node node = selected;

            /*
             * Iterates through the noddes in the tree
             * starting form selected node
             * and, going up towards root node.
             * Loop continues until it reaches the root node.
             */
            while (node != null){

                //tracks how many times a particular node has visited
                node.visits++;

                if (node.board.piece == resultPiece){
                    node.score++;
                }
                node = node.parent;
            }
        }

        /**
         * Simulates the game starting from a given node in the MCTS tree.
         * Explores random moves, updates tree structure
         * and, determines outcome of the simulated game.
         * @param promisingNode - represents the node in MCTS tree from which simulation will start.
         * @return - the winning piece.
         * (simulate to decide whether the best node inorder to select for the move)
         * (Simulation Step)
         */
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

        /**
         * Expands a given node by creating child nodes for each legal move
         * and, randomly selects one of these child nodes for further exploration in MCTS.
         * @param node - Node that need to be expands
         * (Expands the tree via creating child nodes by dividing the parent node)
         * (Expansion Step)
         */
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

        /**
         * Identify promising nodes within the tree based on UCT values
         * balancing exploration and exploitation.
         * Those promising nodes represent game states,
         * that are expected to be most informative for making next decision in the game.
         * @param tree - root node of MCTS tree
         * @return - selected promising node
         * (select & return the best node with highest UCT value after backpropagation)
         * (Selection Step)
         */
        private Node selectPromisingNode(Node tree) {
            Node node = tree;
            while (node.children.size() != 0){
                node = UCT.findBestNodeWithUCT(node);
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

        /**
         * getChildWithMaxScore() find and return
         * the child node with maximum score
         * among all the children of a particular node.
         * (return the highest valued child via backpropagation (if Ai won +1 ; fail -1))
         */
        Node getChildWithMaxScore() {
            Node result = children.get(0);
            for (int i = 1; i < children.size(); i++) {
                if (children.get(i).score > result.score) {
                    result = children.get(i);
                }
            }
            return result;
        }

        /**
         * The child node (node) is added to a list of child nodes associated with the parent node.
         * @param node - child node added to the parent node
         * (add new node to children arrayList)
         */
        void addChild(Node node) {
            children.add(node);
        }
    }
    //use-case test (UCT) / UpperConfidenceBound - test the quality characteristics of sustainability, effectivity
    static class UCT {

        /**
         * Calculates the UCT value for a node, balancing exploitation(known good moves)
         * and, exploration(exploring new moves).
         * Make the algorithm to explore less-visited nodes,
         * especially when they are attached to a well-visited parent node.
         * @param totalVisit - The total number of times the parent node (root node) has been visited.
         * @param nodeWinScore - The number of wins for the node.
         * @param nodeVisit - The number of times the node itself has been visited.
         * (UCT Value calculate via selection phase)
         */
        public static double uctValue(
            int totalVisit, double nodeWinScore, int nodeVisit) {
            if (nodeVisit == 0) {
                return Integer.MAX_VALUE;
            }
            return (nodeWinScore / (double) nodeVisit)
                    + 1.41 * Math.sqrt(Math.log(totalVisit) / (double) nodeVisit);
        }
        //Find the best node with highest UCT value

        /**
         * Identify the child node with the highest UCT value among a parent node's children.
         * Child node having highest UCT value;
         * considered the most promising one to explore further.
         * @param node - represents the parent node;
         * (for which you want to find the best child node based on the UCT value).
         */
        public static Node findBestNodeWithUCT(Node node) {
            int parentVisit = node.visits;

            return Collections.max (node.children,
                    Comparator.comparing(c -> uctValue(parentVisit, c.score, c.visits)));
        }
    }
}

