package our572Project;

import java.util.ArrayList;
import java.util.List;


/**
 * Provides an implementation of the Checkers game which can be used for
 * experiments with the Minimax algorithm.
 * 
 * @author Guangyu Hou, Wandi Xiong, Xiaoqian Mu, modified from AIMA book
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
			return new String("Red    ");
		else
			return new String("White");
	}

	@Override
	public List<CheckerAction> getActions(CheckersState state, boolean ascendingOrder) {
		return state.getFeasibleMovesSorted(ascendingOrder);
	}

	@Override
	public CheckersState getResult(CheckersState state, CheckerAction action) {
		CheckersState result = state.clone();
		result.mark(action);
		return result;
	}
	
	public static CheckersState getResultStatic(CheckersState state, CheckerAction action) {
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
		int utility = state.getUtility();
		if (utility != -1) {
			if(player==CheckersState.X)
			{
				if(utility == 20000)
					return utility;
				else
					return -utility;
			} else if(player==CheckersState.O)
			{
				if(utility == 10000)
					return utility;
				else
					return -utility;
			}
			else
				throw new IllegalArgumentException("isWinner(): invalid player");
		} else {
			throw new IllegalArgumentException("State is not terminal.");
		}
	}
	
	public boolean isWinner(CheckersState state, String player)
	{
		int utility = state.getUtility();
		
		if(utility==-1)
			throw new IllegalArgumentException("isWinner():State is not terminal.");
		
		if(player==CheckersState.X)
		{
			if(utility == 20000)
				return true;
			else
				return false;
		} else if(player==CheckersState.O)
		{
			if(utility == 10000)
				return true;
			else
				return false;
		}
		else
		{
			throw new IllegalArgumentException("isWinner(): invalid player");
		}
	}
	
	@Override
	public int getEvaluation(CheckersState state, String player, int num)
	{
		if (num < 0 || num > 5)
			throw new IllegalArgumentException("Evaluation function version out of bound (0-3)");
		
		int result = 0;
		
		if (num == 0)
		{
			//The naive evaluation function
			result = evalFunc0(state, player);
		}
		else if (num == 1)
		{
			//Evaluation function 1: weighted board
			result = evalFunc1(state, player);
		}
		else if (num == 2)
		{
			//Evaluation function 2: 8F linear heuristics
			result = evalFunc2(state, player);
		}
		else if (num == 3)
		{
			//Evaluation function 3: 25F linear heuristics
			result = evalFunc3(state, player);
		}
		else if (num == 4)
		{
			//Evaluation function 4: 3Phase
			result = evalFunc4(state, player);
		}
		else
		{
			//Evaluation function 4: Expert 3Phase
			result = evalFunc5(state, player);
		}
		
		return result;
	}
		
	public static int evalFunc0(CheckersState state, String player)
	{
		int res = 0;
		
		int num_red = state.getNumberOfPieces("X");
		int num_white = state.getNumberOfPieces("O");
		
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
		
		//System.out.println("Current player: " + player);
		//System.out.println("Score: " + res);
		
		return res;
	}
	
	public int evalFunc2(CheckersState state, String player)
	{
		//8F linear heuristics
		
		return getSimpleFeature(state, player);
	}
	
	public int evalFunc3(CheckersState state, String player)
	{
		//25F linear heuristics
		
		int res = getSimpleFeature(state, player) + getLayoutFeature(state, player) +
				getPatternFeature(state, player);
				
		return res;
	}
	
	public int evalFunc4(CheckersState state, String player)
	{
		//3Phase (Using feature 1-8)
		int res = 0;
		int phase = getPhase(state);
		int nPawns = getPawns(state, player);
		int nKings = getKings(state, player);
		int nSafePawns = getSafePawns(state, player);
		int nSafeKings = getSafeKings(state, player);
		int nMoveablePawns = getMoveablePawns(state, player);
		int nMoveableKings = getMoveableKings(state, player);
		int nProDistance = getProDistance(state, player);
		int nUnoccupied = getUnoccupied(state, player);
		
		//I'll change the parameters later. Now I just assign all parameters to 1.
		if (phase == 1)
		{	
			res = nPawns + nKings + nSafePawns + nSafeKings + nMoveablePawns + nMoveableKings + 
					nProDistance + nUnoccupied;
		}
		else if (phase == 2)
		{
			res = nPawns + nKings + nSafePawns + nSafeKings + nMoveablePawns + nMoveableKings + 
					nProDistance + nUnoccupied;
		}
		else
		{
			res = nPawns + nKings + nSafePawns + nSafeKings + nMoveablePawns + nMoveableKings + 
					nProDistance + nUnoccupied;
		}
		
		return res;
	}
	
	public int evalFunc5(CheckersState state, String player)
	{
		//Expert 3Phase: Feature (3), (10)-(12), (16), (20)-(23) and (25).
		int res = 0;
		int phase = getPhase(state);
		int nSafePawns = getSafePawns(state, player);	//(3)
		int nAttackers = getAttackers(state, player);	//(10)
		int nCentralPawn = getCentralPawn(state, player);	//(11)
		int nCentralKing = getCentralKing(state, player); 	//(12)
		int nDoubleDiagKing = getDoubleDiagKing(state, player);	//(16)
		int nTriangle = getTriangle(state, player);	//(20)
		int nOreo = getOreo(state, player);	//(21)
		int nBridge = getBridge(state, player);	//(22)
		int nDog = getDog(state, player);	//(23)
		int nCornerKing = getCornerKing(state, player);	//(25)
		
		
		//I'll change the parameters later. Now I just assign all parameters to 1.
		if (phase == 1)
		{	
			//The parameter for kings should be low or zero. There doesn't exist a king.
			res = nSafePawns + nAttackers + nCentralPawn + nCentralKing + nDoubleDiagKing + 
					nTriangle + nOreo + nBridge + nDog + nCornerKing;
		}
		else if (phase == 2)
		{
			res = nSafePawns + nAttackers + nCentralPawn + nCentralKing + nDoubleDiagKing + 
					nTriangle + nOreo + nBridge + nDog + nCornerKing;
		}
		else
		{
			res = nSafePawns + nAttackers + nCentralPawn + nCentralKing + nDoubleDiagKing + 
					nTriangle + nOreo + nBridge + nDog + nCornerKing;
		}
		
		return res;
	}
	
	
	public int getPhase(CheckersState state)
	{
		int i, j;
		int nRed = 0;
		int nWhite = 0;
		int nKing = 0;
		
		//Beginning: each player has more than 3 pawns and no kings are present on the board
		//Kings: both players have more than 3 pieces and at least one king is present
		//Ending: one or both players have 3 pieces or less. 
		
		for (i = 0; i < 8; i++)
			for (j = 0; j < 8; j++)
			{
				String cell = state.getValue(j, i);
				if (cell.equals("X"))
					nRed++;
				else if (cell.equals("O"))
					nWhite++;
				
				String king = state.getKingValue(j, i);
				if (king.equals("K"))
					nKing++;
			}
		
		if ((nRed > 3) && (nWhite > 3))
		{
			if (nKing == 0)
				return 1;
			else
				return 2;
		}
		else
			return 3;
	}
	
	public int getSimpleFeature(CheckersState state, String player)
	{	
		int score = 0;
		int nPawns = getPawns(state, player);
		int nKings = getKings(state, player);
		int nSafePawns = getSafePawns(state, player);
		int nSafeKings = getSafeKings(state, player);
		int nMoveablePawns = getMoveablePawns(state, player);
		int nMoveableKings = getMoveableKings(state, player);
		int nProDistance = getProDistance(state, player);
		int nUnoccupied = getUnoccupied(state, player);
		
		score = nPawns + nKings + nSafePawns + nSafeKings + nMoveablePawns + nMoveableKings + 
				nProDistance + nUnoccupied;
		
//		System.out.println("Current player: " + player);
//		System.out.println("Number of pawns: " + nPawns);
//		System.out.println("Number of kings: " + nKings);
//		System.out.println("Number of safe pawns: " + nSafePawns);
//		System.out.println("Number of safe kings: " + nSafeKings);
//		System.out.println("Number of moveable pawns: " + nMoveablePawns);
//		System.out.println("Number of moveable kings: " + nMoveableKings);
//		System.out.println("Number of promotion distance: " + nProDistance);
//		System.out.println("Number of unoccupied promotion line: " + nUnoccupied);
//		System.out.println("**********************************");
		
		
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
		List<CheckerAction> moves = new ArrayList<CheckerAction> ();	//Record the feasible moves of given pawn
		List<XYLocation> record = new ArrayList<XYLocation> (); //Record all moveable pawns 
		
		for (int col = 0; col < 8; col++)
			for (int row = 0; row < 8; row++)
			{
				XYLocation current = new XYLocation(col, row);
				if ((state.getValue(current).equals(player)) && (!state.isKing(current)))
				{		
					moves = state.getFeasibleMoves(current);
					if (moves.size() > 0)
						record.add(current);
				}
			}
		
		return record.size();
	}
	
	//Feature 6
	public int getMoveableKings(CheckersState state, String player)
	{		
		List<CheckerAction> moves = new ArrayList<CheckerAction> ();
		List<XYLocation> record = new ArrayList<XYLocation> (); //Record all moveable kings 
		
		for (int col = 0; col < 8; col++)
			for (int row = 0; row < 8; row++)
			{
				XYLocation current = new XYLocation(col, row);
				if (state.isPlayerAndKing(current, player))
				{		
					moves = state.getFeasibleMoves(current);
					if (moves.size() > 0)
						record.add(current);
				}
			}
		
		return record.size();
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
					if ((state.getValue(j, i).equals(CheckersState.X)) && (!state.getKingValue(j, i).equals("K")))
						distance += (7 - i);
				}
		}
		else	//White starts on the bottom
		{
			for (int i = 0; i < 8; i++)
				for (int j = 0; j < 8; j++)
				{
					if ((state.getValue(j, i).equals(CheckersState.O)) && (!state.getKingValue(j, i).equals("K")))
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
		int num_Defenders=getDefenders(state,player);
		int num_Attackers=getAttackers(state,player);
		int num_CentralPawn=getCentralPawn(state,player);
		int num_CentralKing=getCentralKing(state,player);
		int num_MainDiagPawn=getMainDiagPawn(state,player);
		int num_MainDiagKing=getMainDiagKing(state,player);
		int num_DoubleDiagPawn=getDoubleDiagPawn(state,player);
		int num_DoubleDiagKing=getDoubleDiagKing(state,player);
		int num_LonerPawn=getLonerPawn(state,player);
		int num_LonerKing=getLonerKing(state,player);
		int num_Holes=getHoles(state,player);
		
		score=num_Defenders
				+num_Attackers
				+num_CentralPawn
				+num_CentralKing
				+num_MainDiagPawn
				+num_MainDiagKing
				+num_DoubleDiagPawn
				+num_DoubleDiagKing
				+num_LonerPawn
				+num_LonerKing
				+num_Holes;
		
//		System.out.println("Get features for "+player);
//        System.out.println("Number of Defenders:"+num_Defenders);
//        System.out.println("Number of Attackers:"+num_Attackers);
//        System.out.println("Number of CentralPawn:"+num_CentralPawn);
//        System.out.println("Number of CentralKing:"+num_CentralKing);
//        System.out.println("Number of MainDigPawn:"+num_MainDiagPawn);
//        System.out.println("Number of MainDiagKing:"+num_MainDiagKing);
//        System.out.println("Number of DoubleDiagPawn:"+num_DoubleDiagPawn);
//        System.out.println("Number of DoubleDiagKing:"+num_DoubleDiagKing);
//        System.out.println("Number of LonerPawn:"+num_LonerPawn);
//        System.out.println("Number of LonerKing:"+num_LonerKing);
//        System.out.println("Number of Holes:"+num_Holes);
		return score;
	}
	
	//Feature 9 - Layout feature 1:get number of defender pieces(pawns and Kings)
	public int getDefenders(CheckersState state, String player){
		int num=0;
		
        if(player.equals("O")){
        	for(int j=6;j<=7;j++)
        		for(int i=0;i<=7;i++)
        			if(state.getValue(i, j).equals(player))
        				num++;
        }
        
        if(player.equals("X")){
        	for(int j=0;j<=1;j++)
        		for(int i=0;i<=7;i++)
        			if(state.getValue(i, j).equals(player))
        				num++;
        }

		return num;
	}
	
	//Feature 10 - Layout feature 2: get number of attacking pawns
	public int getAttackers(CheckersState state, String player){
		
		// for Xiaoqian: is this function finished?
		//               seems you need to first distinguish the player in order to get where the "3 topmost rows" are
		//				 and then distinguish whether it is pawn.
		
		int num=0;
	    if(player.equals("O")){
	       for(int j=0;j<=2;j++)
	         for(int i=0;i<=7;i++)
	        	if(state.getValue(i, j).equals(player)&&!state.isPlayerAndKing(i, j, player))
	        	   num++;
	    }
	    
	    if(player.equals("X")){
        	for(int j=5;j<=7;j++)
        		for(int i=0;i<=7;i++)
        			if(state.getValue(i, j).equals(player)&&!state.isPlayerAndKing(i, j, player))
        				num++;
        }
		 
		return num;
	}
	
	//Feature 11 - Layout feature 3: get number of common pawns in the central part
	public int getCentralPawn(CheckersState state, String player){
		int num=0;
		for(int i=2,j=5;i<6;i++,j--)
			if(state.getValue(i, j).equals(player)&&!state.isPlayerAndKing(i, j, player))
				num++;
					
		if(state.getValue(2, 3).equals(player)&&!state.isPlayerAndKing(2, 3, player))
			num++;
			
		if(state.getValue(3, 2).equals(player)&&!state.isPlayerAndKing(3, 2, player))
			num++;
			
		if(state.getValue(4, 5).equals(player)&&!state.isPlayerAndKing(4, 5, player))
			num++;
		
		if(state.getValue(5, 4).equals(player)&&!state.isPlayerAndKing(5, 4, player))
			num++;
		
		return num;
	}
	
	//Feature 12 - Layout feature 4: get number of Kings in the central part
	public int getCentralKing(CheckersState state, String player){
		int num=0;
		for(int i=2,j=5;i<6;i++,j--)
			if(state.isPlayerAndKing(i, j, player))
				num++;
					
		if(state.isPlayerAndKing(2, 3, player))
			num++;
			
		if(state.isPlayerAndKing(3, 2, player))
			num++;
			
		if(state.isPlayerAndKing(4, 5, player))
			num++;
		
		if(state.isPlayerAndKing(5, 4, player))
			num++;
		
		return num;
	}
	
	//Feature 13 - Layout feature 5: get number of common pawns at the main diagonal line
	public int getMainDiagPawn(CheckersState state, String player){
		int num=0;
		for(int i=0,j=7;i<8;i++,j--)
			if(state.getValue(i, j).equals(player)&&!state.isPlayerAndKing(i, j, player))
				num++;
		
		return num;
	}
	
	//Feature 14 - Layout feature 6: get number of Kings at the main diagonal line
	public int getMainDiagKing(CheckersState state, String player){
		int num=0;
		for(int i=0,j=7;i<8;i++,j--)
			if(state.isPlayerAndKing(i, j, player))
				num++;
		
		return num;
	}
	
	//Feature 15 - layout feature 7: get number of common pawns at the double diagonal lines
	public int getDoubleDiagPawn(CheckersState state, String player){
		int num=0;
		for(int i=1,j=0;i<8;i++,j++)
			if(state.getValue(i, j).equals(player)&&!state.isPlayerAndKing(i, j, player))
				num++;
				
		for(int i=0,j=1;i<7;i++,j++)
			if(state.getValue(i, j).equals(player)&&!state.isPlayerAndKing(i, j, player))
				num++;
	
		return num;
	}
	
	//Feature 16 - layout feature 8: get number of Kings at the double diagonal lines
	public int getDoubleDiagKing(CheckersState state, String player){
		int num=0;
		for(int i=1,j=0;i<8;i++,j++)			
			if(state.isPlayerAndKing(i, j, player))
				num++;
				
		for(int i=0,j=1;i<7;i++,j++)
			if(state.isPlayerAndKing(i, j, player))
				num++;
		
		return num;
	}
	
	//Feature 17  - layout feature 9: get number of loner common pawns
	public int getLonerPawn(CheckersState state, String player){
		int num=0;
		
		for(int i=0;i<=7;i++)
			for(int j=0;j<=7;j++)
				if(state.getValue(i, j).equals(player)&&!state.isPlayerAndKing(i, j, player))
					if(state.getValue(i-1, j-1).equals("-")&&state.getValue(i-1, j+1).equals("-")&&state.getValue(i+1, j+1).equals("-")&&state.getValue(i+1, j-1).equals("-"))
						num++;					
	
		return num;
	}
	
	//Feature 18  - layout feature 10: get number of loner common pawns
	public int getLonerKing(CheckersState state, String player){
		int num=0;
		for(int i=0;i<=7;i++)
			for(int j=0;j<=7;j++)
				if(state.isPlayerAndKing(i, j, player))
					if(state.getValue(i-1, j-1).equals("-")&&state.getValue(i-1, j+1).equals("-")&&state.getValue(i+1, j+1).equals("-")&&state.getValue(i+1, j-1).equals("-"))
						num++;
		
		return num;
	}

	//Feature 19 - layout feature 11: get number of holes
	public int getHoles(CheckersState state, String player){
		int num=0;
		for(int i=0;i<=7;i++){
			for(int j=0;j<=7;j++){
				if(state.getValue(i, j).equals("-")){
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
		int num_Triangle=getTriangle(state,player);
		int num_Oreo=getOreo(state,player);
		int num_Bridge=getBridge(state,player);
		int num_Dog=getDog(state,player);
		int num_CornerPawn=getCornerPawn(state,player);
		int num_CornerKing=getCornerKing(state,player);
				
         int score=num_Triangle
        		 +num_Oreo
        		 +num_Bridge
        		 +num_Dog
        		 +num_CornerPawn
           		 +num_CornerKing;
         
//         System.out.println("Number of Triangle:"+num_Triangle);
//         System.out.println("Number of Oreo:"+num_Oreo);
//         System.out.println("Number of Bridge:"+num_Bridge);
//         System.out.println("Number of Dog:"+num_Dog);
//         System.out.println("Number of ConerPawn:"+num_CornerPawn);
//         System.out.println("Number of CornerKing:"+num_CornerKing);
//         System.out.println("**********************************");
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
		    	score=1;
			
		return score;
	}
}

