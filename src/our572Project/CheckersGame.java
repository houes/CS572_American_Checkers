package our572Project;

import java.util.List;


/**
 * Provides an implementation of the Tic-tac-toe game which can be used for
 * experiments with the Minimax algorithm.
 * 
 * @author Ruediger Lunde
 * 
 */
public class CheckersGame implements Game<CheckersState, CheckerAction, String> {

	CheckersState initialState = new CheckersState();

	@Override
	public CheckersState getInitialState() {
		return initialState;
	}

	@Override
	public String[] getPlayers() {
		return new String[] { CheckersState.X, CheckersState.O };
	}

	@Override
	public String getPlayer(CheckersState state) {
		return state.getPlayerToMove();
	}
	
	public String getPlayerByColor(CheckersState state) {
		if(state.getPlayerToMove().equals("X"))
			return new String("Red");
		else
			return new String("White");
	}

	@Override
	public List<CheckerAction> getActions(CheckersState state) {
		return state.getFeasiblePositions();
	}

	@Override
	public CheckersState getResult(CheckersState state, CheckerAction action) {
		CheckersState result = state.clone();
		result.mark(action);
		return result;
	}

	@Override
	public boolean isTerminal(CheckersState state) {
		return state.getUtility() != -1;
	}

	@Override
	public boolean needsCutOff(CheckersState state)
	{
		int depth = state.getNumberOfMarkedPositions();
		int rest_moves = 64-depth;
		
		if( rest_moves >= 10)//15 is good enough
			return true;
		else
			return false;		
	}
	
	@Override
	public double getUtility(CheckersState state, String player) {
		double result = state.getUtility();
		if (result != -1) {
			if (player == CheckersState.O)
				result = 1 - result;
		} else {
			throw new IllegalArgumentException("State is not terminal.");
		}
		return result;
	}
	
	@Override
	public int getEvaluation(CheckersState state, String player)
	{
		//This is a comment
		int result;
		
		int markedPositions = state.getNumberOfMarkedPositions();
		int num_red = state.getNumberOfPieces("X");
		int num_white = markedPositions- num_red;
		
		if (player == CheckersState.X)
			result = num_red - num_white;
		else
			result = num_white - num_red;
		
		return result;
	}
}

