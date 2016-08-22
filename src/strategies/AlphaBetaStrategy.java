package strategies;

import java.util.ArrayList;
import java.util.Collections;

import main_components.Board;
import main_components.Color;
import main_components.Command;

/**
 * \brief
 * Decides a computer move based on "AlphaBeta" strategy.
 * @author Rodney Shaghoulian
 */
public class AlphaBetaStrategy extends Strategy{

	public int depth;			///< The depth to search the game tree. Anything above 8 is too slow.
	boolean turnBasedUtility;	///< When true, utility is a function of time
	
	/**
	 * Constructor - Calls subclasses constructor
	 * @param controller	The Othello simulation that we should run AlphaBeta on.
	 */
	public AlphaBetaStrategy(int depth, boolean turnBasedUtility){
		super();
		this.depth 			  = depth;
		this.turnBasedUtility = turnBasedUtility;
	}

	/**
	 * Does a "move" on a Board. Which "move" is done depends on AlphaBeta strategy
	 * @param board		The Board that the computer A.I. will do a move on.
	 * @return			The updated Board after the "move" is performed.
	 */
	public Board move(Board board){
		if (board.turn >= 50) // 48 can work too, but slower
			depth = 60;		  // to search rest of tree
		
		int alpha = Integer.MIN_VALUE;
		int beta = Integer.MAX_VALUE;
		
		Board successorBoard = alphaBeta(board, 0, depth, alpha, beta);
		
		/* Execute the command */
		Command command = getCommand(board, successorBoard);
		command.execute();
	
		return board;
	}
	
	/**
	 * Recursive function. Uses AlphaBeta strategy to determine the next move
	 * @param board			The Board to do a "move" on.
	 * @param currLevel		The current depth we've searched to in game tree (where 0 corresponds to current state of Board)
	 * @param maxDepth		The number of levels deep we should search the game tree
	 * @param alpha			"value of the best choice of the max player" - CS 440 lecture slides
	 * @param beta			"lowest utility choice found so far for the min player" - CS 440 lecture slides
	 * @return				The Board after the "move" is performed.
	 */
	public Board alphaBeta(Board board, int currLevel, int maxDepth, int alpha, int beta){
		if (board.gameEnded || (currLevel == maxDepth))
			return board;
		Utility utility;
		
		ArrayList<Board> successorBoards = getAdjacentBoards(board);
		Board bestBoard = null;
		
		if (board.playerTurn == Color.BLACK){
			/* Sort Boards corresponding to stronger moves first */
			if (turnBasedUtility)
				Collections.sort(successorBoards, Collections.reverseOrder(new BoardComparatorWhiteDynamic()));
			else
				Collections.sort(successorBoards, Collections.reverseOrder(new BoardComparatorWhiteStatic()));
			
			int max = Integer.MIN_VALUE;
			for (int i = 0; i < successorBoards.size(); i++){
				Board successor = successorBoards.get(i);
				Board lookaheadBoard = alphaBeta(successor, currLevel + 1, maxDepth, alpha, beta);
				utility = new Utility(lookaheadBoard);
				if (turnBasedUtility)
					utility.utilityFinal();
				else
					utility.utilityStatic();
				if (utility.value > max){
					max = utility.value;
					bestBoard = successor;
				}
				alpha = Math.max(alpha, utility.value);
				if (utility.value >= beta){
					return lookaheadBoard; // this is where we "prune", since MIN player will not allow this branch
				}
			}
		}
		else{
			/* Sort Boards corresponding to stronger moves first */
			if (turnBasedUtility)
				Collections.sort(successorBoards, new BoardComparatorWhiteDynamic());
			else
				Collections.sort(successorBoards, new BoardComparatorWhiteStatic());
			
			int min = Integer.MAX_VALUE;
			for (int i = 0; i < successorBoards.size(); i++){
				Board successor = successorBoards.get(i);
				Board lookaheadBoard = alphaBeta(successor, currLevel + 1, maxDepth, alpha, beta);
				utility = new Utility(lookaheadBoard);
				if (turnBasedUtility)
					utility.utilityFinal();
				else
					utility.utilityStatic();
				if (utility.value < min){
					min = utility.value;
					bestBoard = successor;
				}
				beta = Math.min(beta, utility.value);
				if (utility.value <= alpha){
					return lookaheadBoard; // this is where we "prune", since MAX player will not allow this branch
				}
			}
		}
		return bestBoard;
	}
}
