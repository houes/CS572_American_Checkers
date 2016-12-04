package our572Project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A state of the Tic-tac-toe game is characterized by a board containing
 * symbols X and O, the next player to move, and an utility information.
 * 
 * @author Ruediger Lunde
 * 
 */
public class CheckersState implements Cloneable {
	public static final String O = "O";
	public static final String X = "X";
	public static final String K = "K";
	public static final String EMPTY = "-";
	//
	private String[] board = new String[] { EMPTY, X, EMPTY, X, EMPTY, X, EMPTY, X, X, EMPTY, X, EMPTY, X, EMPTY, X,
			EMPTY, EMPTY, X, EMPTY, X, EMPTY, X, EMPTY, X, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
			EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, O, EMPTY, O, EMPTY, O, EMPTY, O, EMPTY, EMPTY, O,
			EMPTY, O, EMPTY, O, EMPTY, O, O, EMPTY, O, EMPTY, O, EMPTY, O, EMPTY };

	private String[] kingBoard = new String[] { EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
			EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
			EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
			EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
			EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY };
	

	private String playerToMove = X;
	private int utility = -1; // 20000: win for X, 10000: win for O, 15000: draw, -1: game not end

	public String getPlayerToMove() {
		return playerToMove;
	}

	public boolean isEmpty(int col, int row) {
		return board[getAbsPosition(col, row)] == EMPTY;
	}

	public String getValue(int col, int row) {
		if(col<0||col>7||row<0||row>7)
			 //throw new IllegalArgumentException("Error in getValue(): Board position out of boundary (0-7) !");
               return EMPTY;
		return board[getAbsPosition(col, row)];
	}

	public String getValue(XYLocation pos) {
		return getValue(pos.getXCoOrdinate(), pos.getYCoOrdinate());
	}

	public String getKingValue(int col, int row) {
		return kingBoard[getAbsPosition(col, row)];
	}

	public void setKingValue(int col, int row, String value) {
		kingBoard[getAbsPosition(col, row)] = value;
	}

	public String getKingValue(XYLocation pos) {
		int col = pos.getXCoOrdinate();
		int row = pos.getYCoOrdinate();
		return getKingValue(col, row);
	}

	public void setKingValue(XYLocation pos, String value) {
		int col = pos.getXCoOrdinate();
		int row = pos.getYCoOrdinate();
		setKingValue(col, row, value);
	}

	public boolean isKing(XYLocation pos) {
		if (getKingValue(pos).equals(K))
			return true;
		else
			return false;
	}
	
	public boolean isPlayerAndKing(XYLocation pos, String player) {

		if(getValue(pos).equals(player) && isKing(pos))
			return true;
		else
			return false;
	}
	
	public boolean isPlayerAndKing(int col, int row, String player) {
		
		XYLocation pos = new XYLocation(col,row);	
		return isPlayerAndKing(pos,player);
	}

	public int getUtility() {
		return utility;
	}

	public boolean reachOpponnetHomeRow(XYLocation pos) {
		if (getPlayerToMove().equals("X")) // red
		{
			if (pos.getYCoOrdinate() == 7)
				return true;
			else
				return false;
		} else // white
		{
			if (pos.getYCoOrdinate() == 0)
				return true;
			else
				return false;

		}
	}

	public boolean updateKingBoard(CheckerAction action) {
		// if producing a new king return true, otherwise return false

		XYLocation from_pos = action.getSelNode();
		XYLocation to_pos = action.getMoveTo();

		// dismark middle king if jump
		if(action.isMultiJump()) // multi-jump
		{
			List<XYLocation> completeSequence = new ArrayList<XYLocation> (action.getMoveToSequence());
			completeSequence.add(0, action.getSelNode()); // add head
			for(int i=0;i<completeSequence.size()-1;i++)
			{
				XYLocation fromItr = completeSequence.get(i);
				XYLocation toItr = completeSequence.get(i+1);
				int midx = (fromItr.getXCoOrdinate() + toItr.getXCoOrdinate()) / 2;
				int midy = (fromItr.getYCoOrdinate() + toItr.getYCoOrdinate()) / 2;
				setKingValue(midx, midy, EMPTY);
			}	
		} else if (Math.abs(to_pos.getXCoOrdinate() - from_pos.getXCoOrdinate()) == 2) // regular jump
		{
			int midx = (from_pos.getXCoOrdinate() + to_pos.getXCoOrdinate()) / 2;
			int midy = (from_pos.getYCoOrdinate() + to_pos.getYCoOrdinate()) / 2;
			setKingValue(midx, midy, EMPTY);
		}
		
		if (reachOpponnetHomeRow(to_pos) && !isKing(from_pos)) // new king
		{
			setKingValue(to_pos, K);
			return true;
		} else if (isKing(from_pos)) // old king moves to a new empty position
		{
			setKingValue(from_pos, EMPTY);
			setKingValue(to_pos, K);
		}

		return false;
	}

	public void mark(CheckerAction action) {

		XYLocation from_pos = action.getSelNode();
		XYLocation to_pos = action.getMoveTo();

		updateKingBoard(action);

		// 1.dismark origin position
		dismark(from_pos.getXCoOrdinate(), from_pos.getYCoOrdinate(), getPlayerToMove());

		// 2.dismark middle position if jump
		String opponent = (playerToMove == X ? O : X);
		if(action.isMultiJump()) // multi-jump
		{
			List<XYLocation> completeSequence = new ArrayList<XYLocation> (action.getMoveToSequence());
			completeSequence.add(0, action.getSelNode()); // add head
			for(int i=0;i<completeSequence.size()-1;i++)
			{
				XYLocation fromItr = completeSequence.get(i);
				XYLocation toItr = completeSequence.get(i+1);
				int midx = (fromItr.getXCoOrdinate() + toItr.getXCoOrdinate()) / 2;
				int midy = (fromItr.getYCoOrdinate() + toItr.getYCoOrdinate()) / 2;
				dismark(midx, midy, opponent);
			}
				
		}else if (Math.abs(to_pos.getXCoOrdinate() - from_pos.getXCoOrdinate()) == 2)  // regular jump
		{
			int midx = (from_pos.getXCoOrdinate() + to_pos.getXCoOrdinate()) / 2;
			int midy = (from_pos.getYCoOrdinate() + to_pos.getYCoOrdinate()) / 2;
			dismark(midx, midy, opponent);
		}
		
		// 3.mark destination
		mark(to_pos.getXCoOrdinate(), to_pos.getYCoOrdinate());
	}

	public void dismark(int col, int row, String player) {
		if (utility == -1 && board[getAbsPosition(col, row)] == player) {
			board[getAbsPosition(col, row)] = EMPTY;
		}
	}

	public void mark(int col, int row) {
		if (utility == -1 && getValue(col, row) == EMPTY) {
			board[getAbsPosition(col, row)] = playerToMove;
			analyzeUtility();
			playerToMove = (playerToMove == X ? O : X);
		}
	}

	private void analyzeUtility() {
		if (opponentHasNoPiece() || opponentHasNoFeasibleMoves()) {
			utility = (playerToMove == X ? 20000 : 10000);
		} else if (isGameDraw()) {
			utility = 15000;
		}
	}

	public boolean opponentHasNoPiece() {

		String opponent = (playerToMove == X ? O : X);

		if (getNumberOfPieces(opponent) == 0)
			return true;
		else
			return false;

	}

	public boolean opponentHasNoFeasibleMoves() {

		String opponent = (playerToMove == X ? O : X);

		if (getFeasibleMoves(opponent).isEmpty())
			return true;
		else
			return false;
	}

	public boolean isGameDraw()
	{
		//TODO: has not designed a draw case yet.
		return false;
	}
	
	public int getNumberOfMarkedPositions() {
		int retVal = 0;
		for (int col = 0; col < 8; col++) {
			for (int row = 0; row < 8; row++) {
				if (!(isEmpty(col, row))) {
					retVal++;
				}
			}
		}
		return retVal;
	}

	public int getNumberOfPieces(String player) {
		// "X" for red, "O" for white
		int retVal = 0;
		for (int col = 0; col < 8; col++) {
			for (int row = 0; row < 8; row++) {
				if (getValue(col, row).equals(player)) {
					retVal++;
				}
			}
		}
		return retVal;
	}
	
	public int getNumberOfKings(String player)
	{
		int retVal = 0;
		for (int col = 0; col < 8; col++) {
			for (int row = 0; row < 8; row++) {
				if (isPlayerAndKing(col,row,player)) {
					retVal++;
				}
			}
		}
		return retVal;
	}

	public boolean isForward(XYLocation origin, XYLocation destination, String player) {
		if (player.equals(X)) // red
		{
			if (destination.getYCoOrdinate() - origin.getYCoOrdinate() > 0)
				return true;
			else
				return false;
		} else if (player.equals(O))// white
		{
			if (destination.getYCoOrdinate() - origin.getYCoOrdinate() < 0)
				return true;
			else
				return false;
		} else {
			throw new IllegalArgumentException("Error in isForward(): Not valid player!");
		}

	}

	public List<CheckerAction> getRegularMoves(XYLocation selNode)
	{
		String player = getValue(selNode);
		if(player.equals(EMPTY))
			throw new IllegalArgumentException("Error in getRegularMoves(): player being Empty");
		
		return getRegularMoves(selNode,player);
	}
			
	public List<CheckerAction> getRegularMoves(XYLocation selNode, String player)
	{
		// check the distance 1 four corners(regular moves)

		List<CheckerAction> result = new ArrayList<CheckerAction>();
		
		int col = selNode.getXCoOrdinate();
		int row = selNode.getYCoOrdinate();
		List<XYLocation> moveToPos = new ArrayList<XYLocation>();

		moveToPos.add(new XYLocation(col - 1, row - 1));
		moveToPos.add(new XYLocation(col - 1, row + 1));
		moveToPos.add(new XYLocation(col + 1, row + 1));
		moveToPos.add(new XYLocation(col + 1, row - 1));
		for (XYLocation pos : moveToPos) {
			if (pos.isWithinBoundary(0, 7))
				if (getValue(pos).equals(EMPTY))
				{
					if(isKing(selNode))
						result.add(new CheckerAction(selNode,pos));
					else if (isForward(selNode, pos, player))
						result.add(new CheckerAction(selNode,pos));
				}
		}
		
		return result;
	}
	
	public List<CheckerAction> getJumpMoves(XYLocation selNode)
	{
		String player = getValue(selNode);
		if(player.equals(EMPTY))
			throw new IllegalArgumentException("Error in getJumpMoves(): player being Empty");
		
		return getJumpMoves(selNode,player,isKing(selNode));
	}
	
	public List<CheckerAction> getJumpMoves(XYLocation selNode, String player, boolean isNodeKing)
	{
		// compute the possible jump moves if provided
		// 1. the location of the node, EMPTY is allowed
		// 2. the player, has to be either "X" or "O"
		// 3. whether the move is for a king.
		
		// check if it can jump(jump moves)  - distance 2 four corners
		List<CheckerAction> result = new ArrayList<CheckerAction>();
		
		int col = selNode.getXCoOrdinate();
		int row = selNode.getYCoOrdinate();
		List<XYLocation> moveToPos = new ArrayList<XYLocation>();
		
		String opponent = (player == X ? O : X);
		moveToPos.add(new XYLocation(col - 2, row - 2));
		moveToPos.add(new XYLocation(col - 2, row + 2));
		moveToPos.add(new XYLocation(col + 2, row + 2));
		moveToPos.add(new XYLocation(col + 2, row - 2));
		for (XYLocation pos : moveToPos) {
			int xCoord = pos.getXCoOrdinate();
			int yCoord = pos.getYCoOrdinate();
			if (pos.isWithinBoundary(0, 7))
				if (getValue(pos).equals(EMPTY)) {
					int mid_x = (col + xCoord) / 2;
					int mid_y = (row + yCoord) / 2;
					if (getValue(mid_x, mid_y).equals(opponent))
					{
						if(isNodeKing)
							result.add(new CheckerAction(selNode,pos));
						else if (isForward(selNode, pos, player))
							result.add(new CheckerAction(selNode,pos));
					}
				}
		}
		
		return result;
	}
	
	public List<CheckerAction> getFeasibleMoves(XYLocation selNode) {

		return getFeasibleMoves(selNode, getPlayerToMove());
	}

	public List<CheckerAction> getFeasibleMoves(XYLocation selNode, String player) {

		// Compute the feasible destinations for a piece

		// If the feasible moves includes jump moves, 
		//	return only jump moves.
		// Else 
		//	return regular moves or nothing.
		
		List<CheckerAction> result_regular = new ArrayList<CheckerAction>();
		List<CheckerAction> result_jump = new ArrayList<CheckerAction>();

		if (getValue(selNode).equals(player)) {

			// first, check the four corners(regular moves)
			result_regular = getRegularMoves(selNode);
			
			// second, check if it can jump(jump moves)
			result_jump = getJumpMoves(selNode);
		}
		
		ListIterator<CheckerAction> itr = result_jump.listIterator();
		List<CheckerAction> result_multi_jump = new ArrayList<CheckerAction>();
		
		while(itr.hasNext())
		{
			List<CheckerAction> multiJumpsOfANode = getMultiJumpsOfANode(itr.next());
			if(!multiJumpsOfANode.isEmpty())
			{
				result_multi_jump.addAll(multiJumpsOfANode);
				itr.previous();
				itr.remove();
			}

		}
		result_jump.addAll(result_multi_jump);
		
		if(!result_jump.isEmpty())
			return result_jump;
		else
			return result_regular;
	}
	
	public List<CheckerAction> getMultiJumpsOfANode(CheckerAction theFirstJump)
	{
		CheckerAction multiJumps = new CheckerAction(theFirstJump.getSelNode(),theFirstJump.getMoveTo());

		String player = getValue(theFirstJump.getSelNode());
		
		setNextJumps(multiJumps,player,isKing(theFirstJump.getSelNode()));
		
		List<CheckerAction> result = getDistinctMultiJumps(multiJumps);
		
/*	the following is for debug only	
 * if(!result.isEmpty() && result.size()==1)
		{
			CheckerAction act1 = new CheckerAction(new XYLocation(0,3), new XYLocation(2,5));
			if(theFirstJump.equals(act1))
			{
			CheckerAction act = result.get(0);
			List<XYLocation> pos= act.getMoveToSequence();
			XYLocation last = pos.get(pos.size()-1);
			if(last.equals(new XYLocation(0,7)))
			{
				System.out.println("debug");
			}
			if(pos.size()>=3)
			{
				XYLocation debug = new XYLocation(0,3);
				
				XYLocation p1 = pos.get(0);
				XYLocation p2 = pos.get(1);
				XYLocation p3 = pos.get(2);
				//XYLocation p4 = pos.get(3);
				if(p1.equals(debug) && p2.equals(debug) &&p3.equals(debug) )
				{
					System.out.println("debug");
				}
			}
			}

		}*/
		
		return result;
	}
	
	public List<CheckerAction> getDistinctMultiJumps(CheckerAction multiJumps)
	{
		List<CheckerAction> result = new ArrayList<CheckerAction>();
		
		if(!multiJumps.hasNextJumps()) // return empty if it is not a multi-jump
			return result;
		
		List<CheckerAction> leafNodes = getAllLeafNodes(multiJumps);

		XYLocation firstPos = multiJumps.getSelNode();
		
		for(CheckerAction leaf: leafNodes)
		{
			List<XYLocation> consectiveMoves = new ArrayList<XYLocation>();
			consectiveMoves.add(leaf.getMoveTo());
			
			CheckerAction parent = leaf.getParent();
			while(parent!=null)
			{
				consectiveMoves.add(parent.getMoveTo());
				parent = parent.getParent();
			}
			
			Collections.reverse(consectiveMoves);
			XYLocation lastPos = consectiveMoves.get(consectiveMoves.size()-1);
			result.add(new CheckerAction(firstPos,lastPos,consectiveMoves));
		}
		
		return result;
	}
	
	public List<CheckerAction> getAllLeafNodes(CheckerAction node) {
		List<CheckerAction> leafNodes = new ArrayList<CheckerAction>();
	    if (node.nextJumps == null) {
	        leafNodes.add(node);
	    } else {
	        for (CheckerAction child : node.nextJumps) {
	            leafNodes.addAll(getAllLeafNodes(child));
	        }
	    }
	    return leafNodes;
	}
	
	public void setNextJumps(CheckerAction currJump, String player, boolean isNodeKing)
	{		
		List<CheckerAction> nextJumps = getNextJump(currJump,player,isNodeKing);
		removeCircles(currJump,nextJumps);
		
		if(!nextJumps.isEmpty())
		{	
			currJump.setNextJumps(nextJumps);
			for(CheckerAction jump: nextJumps)
			{
				jump.setParentJump(currJump);
				setNextJumps(jump,player,isNodeKing);
			}
		}
			
	}
	
	public void removeCircles(CheckerAction currJump, List<CheckerAction> nextJumps)
	{
		// first, find history positions
		List<XYLocation> historyPos = new ArrayList<XYLocation>();
		
		historyPos.add(currJump.getMoveTo());
		
		CheckerAction curr   = currJump;
		CheckerAction parent = currJump.getParent();
		
		while(parent!=null)
		{
			historyPos.add(curr.getSelNode());
			curr = parent;
			parent = parent.getParent();
		}
		
		historyPos.add(curr.getSelNode());// add the root
		
		// second remove any node that form a circle
		ListIterator<CheckerAction> itr = nextJumps.listIterator();
		while(itr.hasNext())
		{
			XYLocation lastPos = itr.next().getMoveTo();
			// if the next position comes back to the history routine, 
			// then forming a circle, remove such jump
			if(historyPos.contains(lastPos)) 
			{
				itr.remove();
			}
		}
	}
	
	
	public List<CheckerAction> getNextJump(CheckerAction currJump, String player, boolean isNodeKing)
	{
		XYLocation from = currJump.getSelNode();
		XYLocation moveTo = currJump.getMoveTo();
		
		CheckerAction reverseJump = new CheckerAction(moveTo, from);
		List<CheckerAction> nextJumps = getJumpMoves(moveTo,player,isNodeKing);
		nextJumps.remove(reverseJump);
		
		return nextJumps;
	}
	
	public List<XYLocation> getFeasibleDestinations(XYLocation pieceToMove)
	{
		List<CheckerAction> feasibleMoves = getFeasibleMoves(pieceToMove);
		List<XYLocation> destinations = new ArrayList<XYLocation>();
		
		for(CheckerAction action: feasibleMoves)
		{
			destinations.add(action.getMoveTo());
		}
		
		return destinations;
	}
	
	public List<CheckerAction> getFeasibleActionsMatching(XYLocation from, XYLocation to)
	{
		List<CheckerAction> result = new ArrayList<CheckerAction>();
		
		List<CheckerAction> feasibleMoves = getFeasibleMoves(from);
		
		for(CheckerAction action: feasibleMoves)
		{
			if(action.getMoveTo().equals(to))
				result.add(action);
		}
		
		return result;
	}

	public List<CheckerAction> getJumpMoves(List<CheckerAction> allFeasibleMoves)
	{
		List<CheckerAction> jumpMoves = new ArrayList<CheckerAction>();
		
		for(CheckerAction action: allFeasibleMoves)
		{
			int x1 = action.getMoveTo().getXCoOrdinate();
			int x2 = action.getSelNode().getXCoOrdinate();
			if( Math.abs(x1-x2)==2    // one step jump
				|| action.isMultiJump() ) // multi-jump: distance could be 1 or more than 2
			{
				jumpMoves.add(action);
			}
		}
		
		return jumpMoves;
	}
	
	public List<XYLocation> getFeasibleMovesFirstNodes()
	{
		List<XYLocation> allFirstNodes = new ArrayList<XYLocation>();
		List<CheckerAction> allFeasibleMoves = getFeasibleMoves();
		
		for(CheckerAction action: allFeasibleMoves)
		{
			allFirstNodes.add(action.getSelNode());
		}
		
		return allFirstNodes;
	}
	
	public List<CheckerAction> getFeasibleMoves() {

		return getFeasibleMoves(getPlayerToMove());
	}

	public List<CheckerAction> getFeasibleMoves(String player) {

		// Compute feasible moves for a player
		// If there is any jump move, return all jump moves, otherwise return regular moves.
		
		List<CheckerAction> allFeasibleMoves = new ArrayList<CheckerAction>();

		for (int col = 0; col < 8; col++) {
			for (int row = 0; row < 8; row++) {
				if (getValue(col, row).equals(player)) {
					XYLocation currNode = new XYLocation(col, row);
					allFeasibleMoves.addAll(getFeasibleMoves(currNode, player));	
				}
			}
		}

		List<CheckerAction> jumpMoves = getJumpMoves(allFeasibleMoves);
		
		List<CheckerAction> result = new ArrayList<CheckerAction>();
		
		if(!jumpMoves.isEmpty())
			result =  jumpMoves;
		else
			result = allFeasibleMoves; // all regular moves
		
		return result;
	}
	
	public List<CheckerAction> getFeasibleMovesSorted() {
	
		// this function may cause infinite loop, please use ONLY outside CheckerState.
		
		List<CheckerAction> result = getFeasibleMoves();
		
		// sort the feasible moves by evaluation0, this affects branching factor
		for(CheckerAction action: result)
		{
			CheckersState stateAfterAction = CheckersGame.getResultStatic(this,action);
			double evalValue = CheckersGame.evalFunc0(stateAfterAction,playerToMove);
			action.setEvalValue(evalValue);
		}
		
		Collections.sort(result);
		
		return result;
	}

	@Override
	public CheckersState clone() {
		CheckersState copy = null;
		try {
			copy = (CheckersState) super.clone();
			copy.board = Arrays.copyOf(board, board.length);
			copy.kingBoard = Arrays.copyOf(kingBoard, kingBoard.length);
		} catch (CloneNotSupportedException e) {
			e.printStackTrace(); // should never happen...
		}
		return copy;
	}

	@Override
	public boolean equals(Object anObj) {
		if (anObj != null && anObj.getClass() == getClass()) {
			CheckersState anotherState = (CheckersState) anObj;
			for (int i = 0; i < 64; i++) {
				if (board[i] != anotherState.board[i]) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		// Need to ensure equal objects have equivalent hashcodes (Issue 77).
		return toString().hashCode();
	}

	@Override
	public String toString() {
		StringBuilder strBuilder = new StringBuilder();
		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				strBuilder.append(getValue(col, row) + " ");
			}
			strBuilder.append("\n");
		}
		return strBuilder.toString();
	}

	//
	// PRIVATE METHODS
	//

	private int getAbsPosition(int col, int row) {
		return row * 8 + col;
	}
}
