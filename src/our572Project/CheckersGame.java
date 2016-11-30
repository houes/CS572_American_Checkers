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
		return state.getFeasibleMoves();
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
	public int getEvaluation(CheckersState state, String player, int num)
	{
		if (num < 0 || num > 4)
		{
			return -1;
		}
		
		int result = 0;
		
		if (num == 0)
		{
			//The naive evaluation function
			result = evalFunc0(state, player);
		}
		else if (num == 1)
		{
			//Evaluation function 1
			result = evalFunc1(state, player);
		}
		else
		{
			int simpleFeature = getSimpleFeature(state, player);
			int layoutFeature = getLayoutFeature(state, player);
			int patternFeature = getPatternFeature(state, player);
			
			result = simpleFeature + layoutFeature + patternFeature;
		}
		
		return result;
	}
	

	public int getPatternVal(CheckersState state, String player, int num){
		int value=0;
		if(player.equals('O')){			
		   if(state.getValue(5, 6)==player&&state.getValue(4, 7)==player&&state.getValue(6, 7)==player)
			value+=2;
		
		   if(state.getValue(3, 6)==player&&state.getValue(2, 7)==player&&state.getValue(4, 7)==player)
			value+=3;	
		   
		   if(state.getValue(2, 7)==player&&state.getValue(6, 7)==player)
			   value+=2;
		   
		   if(state.getValue(0, 7)==player)
			   value+=2;
		   
		   if(state.getValue(7, 0)==player&&state.getKingValue(7, 0)==player)
			   value+=3;
		}
		else{
			if(state.getValue(2, 1)==player&&state.getValue(1, 0)==player&&state.getValue(3, 0)==player)
				value+=2;
			
		    if(state.getValue(4, 1)==player&&state.getValue(3, 0)==player&&state.getValue(5, 0)==player)
				value+=3;
			
		    if(state.getValue(1, 0)==player&&state.getValue(5, 0)==player)
				   value+=2;  
		    
		    if(state.getValue(7, 0)==player)
				   value+=2;
		    
		    if(state.getValue(0, 7)==player&&state.getKingValue(0, 7)==player)
				   value+=3;
		}
		return value;
	}
	
	public int evalFunc0(CheckersState state, String player)
	{
		int res = 0;
		
		int markedPositions = state.getNumberOfMarkedPositions();
		int num_red = state.getNumberOfPieces("X");
		int num_white = markedPositions - num_red;
		
		if (player == CheckersState.X)
			res = num_red - num_white;
		else
			res = num_white - num_red;
		
		return res;
	}
	
	public int evalFunc1(CheckersState state, String player)
	{
		int res = 0;
		
		
		
		return res;
	}
	
	public int getSimpleFeature(CheckersState state, String player)
	{
		int score = 0;
		
		return score;
	}
	
	public int getLayoutFeature(CheckersState state, String player)
	{
		int score = 0;
		
		return score;
	}
	
	public int getPatternFeature(CheckersState state, String player)
	{
		int score = 0;
		
		return score;
	}
}

