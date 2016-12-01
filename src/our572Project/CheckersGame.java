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
		int nPosFour = 0;  //Chessboard positions that has weight 4
		int nPosThree = 0;
		int nPosTwo = 0;
		int nPosOne = 0;
		int i, j;	//i for row and j for column
		
		//Count all positions that have weight 4
		nPosFour = getSafePieces(state, player);
		
		//Count all positions that have weight 3
		//The score for row 1
		for (i = 1, j = 2; j < 8; j += 2)
		{
			if (state.getValue(j, i) == player)
				nPosThree++;
		}
		
		//The score for row 6
		for (i = 6, j = 1; j < 6; j += 2)
		{
			if (state.getValue(j, i) == player)
				nPosThree++;
		}
		
		//The score for column 1
		if (state.getValue(1, 2) == player)
			nPosThree++;
		if (state.getValue(1, 4) == player)
			nPosThree++;
		
		//The score for column 6
		if (state.getValue(6, 3) == player)
			nPosThree++;
		if (state.getValue(6, 5) == player)
			nPosThree++;
		
		//Count all positions that have weight 2
		//The score for row 2 
		if (state.getValue(3, 2) == player)
			nPosTwo++;
		if (state.getValue(5, 2) == player)
			nPosTwo++;
		
		//The score for row 5
		if (state.getValue(2, 5) == player)
			nPosTwo++;
		if (state.getValue(4, 5) == player)
			nPosTwo++;
		
		//The score for column 2
		if (state.getValue(2, 3) == player)
			nPosTwo++;
		
		//The score for column 5
		if (state.getValue(5, 4) == player)
			nPosTwo++;
		
		////Count all positions that have weight 1
		if (state.getValue(4, 3) == player)
			nPosOne++;
		if (state.getValue(3, 4) == player)
			nPosOne++;
		
		res = nPosFour * 4 + nPosThree * 3 + nPosTwo * 2 + nPosOne;
		
		return res;
	}
	
	public int getSimpleFeature(CheckersState state, String player)
	{
		int score = 0;
		int nPawns = getPawns(state, player);
		int nKings = getKings(state, player);
		int nSafePawns = getSafePawns(state, player);
		
		return score;
	}
	
	//Feature 1
	public int getPawns(CheckersState state, String player)
	{
		int nPawns = state.getNumberOfPieces(player) - state.getNumberOfKings(player);
		
		return nPawns;
	}
	
	//Feature 2
	public int getKings(CheckersState state, String player)
	{	
		return state.getNumberOfKings(player);
	}
	
	//Feature 3
	public int getSafePawns(CheckersState state, String player)
	{
		int nSafePieces = getSafePieces(state, player);
		int nSafeKings = getSafeKings(state, player);
		
		return (nSafePieces - nSafeKings);
	}
	
	public int getSafePieces(CheckersState state, String player)
	{
		int nSafePieces = 0;
		int i, j;
		
		//The safe piece number in row 0 
		for (i = 0, j = 1; j < 8; j += 2)
		{
			if (state.getValue(j, i) == player)
				nSafePieces++;
		}
				
		//The safe piece number in row 7
		for (i = 7, j = 0; j < 8; j += 2)
		{
			if (state.getValue(j, i) == player)
				nSafePieces++;
		}
				
		//The safe piece number in column 0
		for (i = 1, j = 0; i < 6; i += 2)
		{
			if (state.getValue(j, i) == player)
				nSafePieces++;
		}
				
		//The safe piece number in for column 7
		for (i = 2, j = 7; i < 8; i += 2)
		{
			if (state.getValue(j, i) == player)
				nSafePieces++;
		}
		
		return nSafePieces;
	}
	
	//Feature 4
	public int getSafeKings(CheckersState state, String player)
	{
		int nSafeKings = 0;
		int i, j;
		
		//The safe king number in row 0 
		for (i = 0, j = 1; j < 8; j += 2)
		{
			if (state.isPlayerAndKing(j, i, player) == true)
				nSafeKings++;
		}
				
		//The safe king number in row 7
		for (i = 7, j = 0; j < 8; j += 2)
		{
			if (state.isPlayerAndKing(j, i, player) == true)
				nSafeKings++;
		}
				
		//The safe king number in column 0
		for (i = 1, j = 0; i < 6; i += 2)
		{
			if (state.isPlayerAndKing(j, i, player) == true)
				nSafeKings++;
		}
				
		//The safe king number in for column 7
		for (i = 2, j = 7; i < 8; i += 2)
		{
			if (state.isPlayerAndKing(j, i, player) == true)
				nSafeKings++;
		}
		
		return nSafeKings;
	}
	
	//Feature 5
	public int getMoveablePawns(CheckersState state, String player)
	{
		int nMoveablePawns = 0;
		
		return nMoveablePawns;
	}
	
	//Feature 6
	public int getMoveableKings(CheckersState state, String player)
	{
		int nMoveablePawns = 0;
		
		return nMoveablePawns;
	}
	
	public int getLayoutFeature(CheckersState state, String player)
	{
		int score = 0;
		
		return score;
	}
	
	public int getPatternFeature(CheckersState state, String player)
	{    
		//we can add different weights for these 5 pattern functions
         int score=getPattern1(state,player)
        		 +getPattern2(state,player)
        		 +getPattern3(state,player)
        		 +getPattern4(state,player)
           		 +getPattern5(state,player);
         return score;
	}
	
	//pattern1: A Triangle - white pawns on squares 27, 31 and 32 or red pawns on squares 6,1,2
	public int getPattern1(CheckersState state, String player){
		int score=0;
		if(player.equals('O')){			
		    if(state.getValue(5, 6).equals(player)&&state.getValue(4, 7).equals(player)&&state.getValue(6, 7).equals(player))
			score= 1;
		}
	   if(player.equals('X')){
			if(state.getValue(2, 1).equals(player)&&state.getValue(1, 0).equals(player)&&state.getValue(3, 0).equals(player))
				score= 1;
		}
	   return score;
	}
	
	//Pattern2: An Oreo - white pawns on squares 26, 30 and 31 or red pawns on squares 7,2,3
	public int getPattern2(CheckersState state, String player){
		int score=0;
		if(player.equals('O'))	
			if(state.getValue(3, 6).equals(player)&&state.getValue(2, 7).equals(player)&&state.getValue(4, 7).equals(player))
				   score=1;
		
		if(player.equals('X'))
			if(state.getValue(4, 1).equals(player)&&state.getValue(3, 0).equals(player)&&state.getValue(5, 0).equals(player))
		    	score=1;
			
		return score;
	}
	
	//Pattern3: A Bridge - white pawns on squares 30 and 32 or red pawns on squares 1,3
	public int getPattern3(CheckersState state, String player){
		int score=0;
		if(player.equals('O'))
			if(state.getValue(2, 7).equals(player)&&state.getValue(6, 7).equals(player))
				   score=1;
		
		if(player.equals('X'))
			 if(state.getValue(1, 0).equals(player)&&state.getValue(5, 0).equals( player))
			    	score=1; 
	
		return score;
	}
	
	//Pattern4: A Pawn in the Corner - white pawn on square 29 or red pawn on square 4
	public int getPattern4(CheckersState state, String player){
		int score=0;
		if(player.equals('O'))
			if(state.getValue(0, 7).equals(player))
				   score=1;
		
		if(player.equals('X'))
			if(state.getValue(7, 0).equals(player))
		    	score=1;
					
		return score;
	}
	
	//Pattern5: A King in the Corner - white king on square 4 or red king on square 29
	public int getPattern5(CheckersState state, String player){
		int score=0;
		if(player.equals('O'))
			if(state.isPlayerAndKing(7, 0, player))
				   score=1;

		if(player.equals('X'))
			if(state.isPlayerAndKing(0,7,player))
		    	score+=3;
			
		return score;
	}
}

