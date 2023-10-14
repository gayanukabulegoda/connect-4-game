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

                /*
                 * Checks Current game state is equal to
                 * resultPiece obtained from simulated game outcome.
                 * If yes; means that the player corresponding to this node's game state
                 * won in the simulation, so the score(/win count) of the node is incremented by 1.
                 */
                if (node.board.piece == resultPiece){
                    node.score++;
                }

                /*
                 * After processing a node,
                 * the code then moves to its parent node in the tree by setting node to node.parent
                 * making method to traverse up the tree updating parent node statics
                 * (from leaf node back to the root node).
                 */
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

            /*
             * Copies game board from promisong node
             * and, create a new node from which simulation begins
             */
            Node node = new Node(promisingNode.board);

            /*
             * Parent of the new node is set to parent of promising node;
             * used to maintain relationship between nodes during simulation.
             */
            node.parent = promisingNode.parent;

            Winner winner = node.board.findWinner();

            if (winner.getWinningPiece() == Piece.BLUE){

                /*
                 * Score of parent node of the current node
                 * is set to minimum possible integer value.
                 * This action will influence the MCTS algorithm to avoid this path.
                 */
                node.parent.score = Integer.MIN_VALUE;

                /*
                 * Returns BLUE winningPiece indicating simulation has ended
                 * with a win for Human player.
                 */
                return node.board.findWinner().getWinningPiece();
            }

            //Simulates the game further(as game hasn't reached terminal state/no winner yet)
            while (node.board.getStatus()){

                //Selects a random legal next move
                BoardImpl nextMove = node.board.getRandomLegalNextMove();

                //New Node object created from the random move
                Node child = new Node(nextMove);
                child.parent = node;

                //New Node(child node) added to children arrayList
                node.addChild(child);
                node = child;
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

            //Retrieves the game board and stores in local variable board
            BoardImpl board = node.board;

            //Obtain nextMoves arrayList from getAllLegalNextMoves()
            List<BoardImpl> legalMoves = board.getAllLegalNextMoves();

            /*
             * Loop creates child nodes for each legal move(BoardImpl object) catch from nextMoves arrayList
             * and, assigned to children arrayList.
             */
            for (BoardImpl move : legalMoves) {
                Node child = new Node(move);
                child.parent = node;
                node.addChild(child);
            }

            //Generates a Random integer(within children array range) in-order-to return a random Node
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

            //Initializes a Node object to start at the root of MCTS tree
            Node node = tree;

            /*
             * Loop continues as long as there are unexplored moves from this game state.
             * Continues iteratively moving down the tree from the current node
             * to the child that is determined to be the most promising based on the UCT vlaues.
             */
            while (node.children.size() != 0){

                //Select the best child node from the current node using UCT algorithm
                node = UCT.findBestNodeWithUCT(node);
            }
            return node;
        }
    }
    static class Node {

        //Catch the boardImpl's current board (2D array)
        public BoardImpl board;
        //Counts the no.of visits via backpropagation phase
        public int visits;
        //To asiign generated score (according to best selection path) via backpropagation phase
        public int score;
        //children array holds each & every possible moves as nodes
        List<Node> children= new ArrayList<>();
        //Every child has a parent child's reference (so via selection it selects the highest UCT valued path to drop the ball)
        Node parent=null;

        //Catches BoardImpl's board
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

            //Initialize result variable by setting it to first child node in children arrayList
            Node result = children.get(0);

            /*
             * Loop to compares the scores of all the child nodes and find the one with the highest score.
             * Loop starts from the second child node (index 1) and iterates through all the child nodes.
             */
            for (int i = 1; i < children.size(); i++) {
                if (children.get(i).score > result.score) {

                    //result node is updated to point to the child node with the higher score.
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

            /*
             * If the node has never been visited(nodeVisit is zero);
             * returns Integer.MAX_VALUE.
             * Means it ensures that it gets a high UCT value to encourage exploration.
             */
            if (nodeVisit == 0) {
                return Integer.MAX_VALUE;
            }

            //1.41 is a constant that balances exploration and exploitation
            return (nodeWinScore / (double) nodeVisit)
                    + 1.41 * Math.sqrt(Math.log(totalVisit) / (double) nodeVisit);
        }

        /**
         * Identify the child node with the highest UCT value among a parent node's children.
         * Child node having highest UCT value;
         * considered the most promising one to explore further.
         * @param node - represents the parent node;
         * (To find the best child node based on the UCT value (highest UCT)).
         */
        public static Node findBestNodeWithUCT(Node node) {

            /*
             * Calculates the number of times the parent node has been visited
             * and, stores it in the parentVisit variable.
             * This value is needed to calculate the UCT value for each child node.
             */
            int parentVisit = node.visits;

            /*
             * Comparator is used to compare child nodes based on their UCT values.
             * "Collections.max" method used to find the child node with the maximum UCT value
             * among all child nodes, as determined by the Comparator.
             */
            return Collections.max (node.children,
                    Comparator.comparing(c -> uctValue(parentVisit, c.score, c.visits)));
        }
    }
}

