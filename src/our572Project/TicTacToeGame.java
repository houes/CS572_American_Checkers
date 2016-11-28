package our572Project;

import java.util.List;


/**
 * Provides an implementation of the Tic-tac-toe game which can be used for
 * experiments with the Minimax algorithm.
 * 
 * @author Ruediger Lunde
 * 
 */
public class TicTacToeGame implements Game<TicTacToeState, XYLocation, String> {

	TicTacToeState initialState = new TicTacToeState();

	@Override
	public TicTacToeState getInitialState() {
		return initialState;
	}

	@Override
	public String[] getPlayers() {
		return new String[] { TicTacToeState.X, TicTacToeState.O };
	}

	@Override
	public String getPlayer(TicTacToeState state) {
		return state.getPlayerToMove();
	}

	@Override
	public List<XYLocation> getActions(TicTacToeState state) {
		return state.getUnMarkedPositions();
	}

	@Override
	public TicTacToeState getResult(TicTacToeState state, XYLocation action) {
		TicTacToeState result = state.clone();
		result.mark(action);
		return result;
	}

	@Override
	public boolean isTerminal(TicTacToeState state) {
		return state.getUtility() != -1;
	}

	@Override
	public boolean needsCutOff(TicTacToeState state)
	{
		int depth = state.getNumberOfMarkedPositions();
		int rest_moves = 64-depth;
		
		if( rest_moves >= 10)//15 is good enough
			return true;
		else
			return false;		
	}
	
	@Override
	public double getUtility(TicTacToeState state, String player) {
		double result = state.getUtility();
		if (result != -1) {
			if (player == TicTacToeState.O)
				result = 1 - result;
		} else {
			throw new IllegalArgumentException("State is not terminal.");
		}
		return result;
	}
	
	@Override
	public int getEvaluation(TicTacToeState state, String player)
	{
		int result;
		
		int markedPositions = state.getNumberOfMarkedPositions();
		int num_black = state.getNumberOfBlackPieces();
		int num_white = markedPositions- num_black;
		
		if (player == TicTacToeState.X)
			result = num_black - num_white;
		else
			result = num_white - num_black;
		
		return result;
	}
}
