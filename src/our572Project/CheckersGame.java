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
		
		List<CheckerAction> list = state.getFeasibleMoves(player);
		
		for (int i = 0; i < list.size(); i++)
		{
			CheckerAction action = list.get(i);
			if (state.isPlayerAndKing(action.getSelNode(), player) == true)
				continue;
			else if (state.getValue(action.getSelNode()).equals(player))
				nMoveablePawns++;
		}
		
		return nMoveablePawns;
	}
	
	//Feature 6
	public int getMoveableKings(CheckersState state, String player)
	{
		int nMoveableKings = 0;
		
		List<CheckerAction> list = state.getFeasibleMoves(player);
		
		for (int i = 0; i < list.size(); i++)
		{
			CheckerAction action = list.get(i);
			if (state.isPlayerAndKing(action.getSelNode(), player) == true)
				nMoveableKings++;
		}
		
		return nMoveableKings;
	}
	
	//Feature 7
	public int getProDistance(CheckersState state, String player)
	{
		int distance = 0;
		
		if (player == CheckersState.X)	//Red starts on the top
		{
			for (int i = 0; i < 8; i++)
				for (int j = 0; j < 8; j++)
				{
					if (state.getValue(j, i).equals(CheckersState.X))
						distance += (7 - i);
				}
		}
		else	//White starts on the bottom
		{
			for (int i = 0; i < 8; i++)
				for (int j = 0; j < 8; j++)
				{
					if (state.getValue(j, i).equals(CheckersState.X))
						distance += i;
				}
		}
			
		return distance;
	}
	
	//Feature 8
	public int getUnoccupied(CheckersState state, String player)
	{
		int nUnoccupied = 0;
		
		if (player == CheckersState.X)
		{
			for (int i = 0, j = 1; j < 8; j += 2)
			{
				if (state.isEmpty(j, i))
					nUnoccupied++;
			}
		}
		else
		{
			for (int i = 7, j = 0; j < 8; j += 2)
			{
				if (state.isEmpty(j, i))
					nUnoccupied++;
			}
		}
		
		return nUnoccupied;
	}
	
	
	public int getLayoutFeature(CheckersState state, String player)
	{
		int score = 0;
		//we should give different weights for these features
		score=getDefenders(state,player)
				+getAttackers(state,player)
				+getCentralPawnAndKing(state,player)
				+getMainDiagPawnsAndKings(state,player)
				+getDoubleDiagPawmAndKing(state,player)
				+getLonerPawnAndKing(state,player)
				+getHoles(state,player);
		return score;
	}
	
	//Feature 9 - Layout feature 1:get number of defender pieces, pawn=1 king=2
	public int getDefenders(CheckersState state, String player){
		int score=0;
		
		for(int i=1;i<8;i+=2){
			if(state.getValue(i, 0).equals(player))
				score++;
			if(state.isPlayerAndKing(i, 0, player))
				score++;
		}
		
		for(int i=0;i<8;i+=2){
			if(state.getValue(i, 7).equals(player))
				score++;
			if(state.isPlayerAndKing(i, 7, player))
				score++;
		}
		
		return score;
	}
	
	//Feature 10 - Layout feature 2: get number of attacking pieces, pawn=1, king=2
	public int getAttackers(CheckersState state, String player){
		int num=0;
		for(int i=1;i<7;i++)
			for(int j=0;j<=7;j++){
				if(state.getValue(j, i).equals(player))
					num++;
				if(state.isPlayerAndKing(j, i, player))
					num++;					
			}
		
		return num;
	}
	
	//Feature 11 & 12 - Layout feature 3: get number of pieces in the central part, pawn=1, king=2
	public int getCentralPawnAndKing(CheckersState state, String player){
		int num=0;
		for(int i=2,j=5;i<6;i++,j--){
			if(state.getValue(i, j).equals(player))
				num++;
			if(state.isPlayerAndKing(i, j, player))
				num++;
		}
		
		if(state.getValue(2, 3).equals(player))
			num++;
		if(state.isPlayerAndKing(2, 3, player))
			num++;
		
		if(state.getValue(3, 2).equals(player))
			num++;
		if(state.isPlayerAndKing(3, 2, player))
			num++;
		
		if(state.getValue(4, 5).equals(player))
			num++;
		if(state.isPlayerAndKing(4, 5, player))
			num++;
		
		if(state.getValue(5, 4).equals(player))
			num++;
		if(state.isPlayerAndKing(5, 4, player))
			num++;
		
		return num;
	}
	
	//Feature 13 & 14 - Layout feature 4: get number of pieces at the main diagonal line, pawn=1, king=2
	public int getMainDiagPawnsAndKings(CheckersState state, String player){
		int num=0;
		for(int i=0,j=7;i<8;i++,j--){
			if(state.getValue(i, j).equals(player))
				num++;
			if(state.isPlayerAndKing(i, j, player))
				num++;
		}
		return num;
	}
	
	//Feature 15 & 16 - layout feature 5: get number of pieces at the double diagonal lines, pawn=1, king=2
	public int getDoubleDiagPawmAndKing(CheckersState state, String player){
		int num=0;
		for(int i=1,j=0;i<8;i++,j++){
			if(state.getValue(i, j).equals(player))
				num++;
			if(state.isPlayerAndKing(i, j, player))
				num++;
		}
		
		for(int i=0,j=1;i<7;i++,j++){
			if(state.getValue(i, j).equals(player))
				num++;
			if(state.isPlayerAndKing(i, j, player))
				num++;
		}
		
		return num;
	}
	
	//Feature 17 & 18 - layout feature 6: get number of loner pieces, pawn=1, king=2
	public int getLonerPawnAndKing(CheckersState state, String player){
		int num=0;
		for(int i=0;i<=7;i++){
			for(int j=0;j<=7;j++){
				if(state.getValue(i, j).equals(player)){
					if(state.getValue(i-1, j-1).equals('-')&&state.getValue(i-1, j+1).equals('-')&&state.getValue(i+1, j+1).equals('-')&&state.getValue(i+1, j-1).equals('-')){
						num++;
						if(state.isPlayerAndKing(i, j, player))
							num++;
					}
				}
			}
		}
		return num;
	}

	//Feature 19 - layout feature 7: get number of holes
	public int getHoles(CheckersState state, String player){
		int num=0;
		for(int i=0;i<=7;i++){
			for(int j=0;j<=7;j++){
				if(state.getValue(i, j).equals('-')){
					int temp=0;
					if(state.getValue(i-1, j-1).equals(player))
						temp++;
					if(state.getValue(i-1, j+1).equals(player))
						temp++;
					if(state.getValue(i+1, j+1).equals(player))
						temp++;
					if(state.getValue(i+1, j-1).equals(player))
						temp++;
					if(temp>=3)
						num++;						
				}
			}
		}
		return num;
	}
	
	public int getPatternFeature(CheckersState state, String player)
	{    
		//we can add different weights for these 5 pattern functions
         int score=getTriangle(state,player)
        		 +getOreo(state,player)
        		 +getBridge(state,player)
        		 +getDog(state,player)
        		 +getCornerPawn(state,player)
           		 +getCornerKing(state,player);
         return score;
	}
	
	//Feature 20 - pattern1: A Triangle - white pawns on squares 27, 31 and 32 or red pawns on squares 6,1,2
	public int getTriangle(CheckersState state, String player){
		int score=0;
		if(player.equals("O")){			
		    if(state.getValue(5, 6).equals(player)&&state.getValue(4, 7).equals(player)&&state.getValue(6, 7).equals(player))
			score= 1;
		}
	   if(player.equals("X")){
			if(state.getValue(2, 1).equals(player)&&state.getValue(1, 0).equals(player)&&state.getValue(3, 0).equals(player))
				score= 1;
		}
	   return score;
	}
	
	//Feature 21 - Pattern2: An Oreo - white pawns on squares 26, 30 and 31 or red pawns on squares 7,2,3
	public int getOreo(CheckersState state, String player){
		int score=0;
		if(player.equals("O"))	
			if(state.getValue(3, 6).equals(player)&&state.getValue(2, 7).equals(player)&&state.getValue(4, 7).equals(player))
				   score=1;
		
		if(player.equals("X"))
			if(state.getValue(4, 1).equals(player)&&state.getValue(3, 0).equals(player)&&state.getValue(5, 0).equals(player))
		    	score=1;
			
		return score;
	}
	
	//Feature 22 - Pattern3: A Bridge - white pawns on squares 30 and 32 or red pawns on squares 1,3
	public int getBridge(CheckersState state, String player){
		int score=0;
		if(player.equals("O"))
			if(state.getValue(2, 7).equals(player)&&state.getValue(6, 7).equals(player))
				   score=1;
		
		if(player.equals("X"))
			 if(state.getValue(1, 0).equals(player)&&state.getValue(5, 0).equals( player))
			    	score=1; 
	
		return score;
	}
	
	//Feature 23 - Pattern4: A Dog - white pawns on squares 32 and red pawns on squares 28 or red pawns on squares 1 and white pawns on squares 5
	public int getDog(CheckersState state, String player){
		int num=0;
		if(player.equals("O"))
		   if(state.getValue(6, 7).equals(player)&&state.getValue(7, 6).equals("X"))
			   num++;
		
		if(player.equals("X"))
			if(state.getValue(1, 0).equals(player)&&state.getValue(0, 1).equals("O"))
				num++;
		return num;
	}
	
	//Feature 24 - Pattern5: A Pawn in the Corner - white pawn on square 29 or red pawn on square 4
	public int getCornerPawn(CheckersState state, String player){
		int score=0;
		if(player.equals("O"))
			if(state.getValue(0, 7).equals(player))
				   score=1;
		
		if(player.equals("X"))
			if(state.getValue(7, 0).equals(player))
		    	score=1;
					
		return score;
	}
	
	//Feature 25 - Pattern6: A King in the Corner - white king on square 4 or red king on square 29
	public int getCornerKing(CheckersState state, String player){
		int score=0;
		if(player.equals("O"))
			if(state.isPlayerAndKing(7, 0, player))
				   score=1;

		if(player.equals("X"))
			if(state.isPlayerAndKing(0,7,player))
		    	score+=3;
			
		return score;
	}
}

