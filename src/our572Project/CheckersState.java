package our572Project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
	private double utility = -1; // 1: win for X, 0: win for O, 0.5: draw

	public String getPlayerToMove() {
		return playerToMove;
	}

	public boolean isEmpty(int col, int row) {
		return board[getAbsPosition(col, row)] == EMPTY;
	}

	public String getValue(int col, int row) {
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

	public double getUtility() {
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
		if (Math.abs(to_pos.getXCoOrdinate() - from_pos.getXCoOrdinate()) == 2) {
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
		if (Math.abs(to_pos.getXCoOrdinate() - from_pos.getXCoOrdinate()) == 2) {
			int midx = (from_pos.getXCoOrdinate() + to_pos.getXCoOrdinate()) / 2;
			int midy = (from_pos.getYCoOrdinate() + to_pos.getYCoOrdinate()) / 2;
			String opponent = (playerToMove == X ? O : X);
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
		if (opponentHasNoPiece() || oppnentHasNoFeasibleMoves()) {
			utility = (playerToMove == X ? 1 : 0);
		} else if (getNumberOfMarkedPositions() == 64) {
			utility = 0.5;
		}
	}

	public boolean opponentHasNoPiece() {

		String opponent = (playerToMove == X ? O : X);

		if (getNumberOfPieces(opponent) == 0)
			return true;
		else
			return false;

	}

	public boolean oppnentHasNoFeasibleMoves() {

		String opponent = (playerToMove == X ? O : X);

		if (getFeasibleMoves(opponent).isEmpty())
			return true;
		else
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
			System.out.println("Error in isForward()!");
			return false;
		}

	}

	public List<XYLocation> getFeasiblePositions(XYLocation selNode) {

		return getFeasiblePositions(selNode, getPlayerToMove());
	}

	public List<XYLocation> getFeasiblePositions(XYLocation selNode, String player) {

		// Compute the feasible destinations for a piece
		// If the feasible moves includes only regular moves, output it.
		// If the feasible moves includes both regular and jump moves, output only jump moves.
		// If the feasible moves includes only jump moves, output it.
		
		List<XYLocation> result_regular = new ArrayList<XYLocation>();
		List<XYLocation> result_jump = new ArrayList<XYLocation>();

		if (getValue(selNode).equals(player)) {
			int col = selNode.getXCoOrdinate();
			int row = selNode.getYCoOrdinate();
			List<XYLocation> moveToPos = new ArrayList<XYLocation>();
			// first, check the four corners(regular moves)
			moveToPos.add(new XYLocation(col - 1, row - 1));
			moveToPos.add(new XYLocation(col - 1, row + 1));
			moveToPos.add(new XYLocation(col + 1, row + 1));
			moveToPos.add(new XYLocation(col + 1, row - 1));
			for (XYLocation pos : moveToPos) {
				if (pos.isWithinBoundary(0, 7))
					if (getValue(pos).equals(EMPTY))
					{
						if(isKing(selNode))
							result_regular.add(pos);
						else if (isForward(selNode, pos, player))
							result_regular.add(pos);
					}
			}
			moveToPos.clear();
			// second, check if it can jump(jump moves)
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
							if(isKing(selNode))
								result_jump.add(pos);
							else if (isForward(selNode, pos, player))
								result_jump.add(pos);
						}
					}
			}
		}

		if(!result_jump.isEmpty())
			return result_jump;
		else
			return result_regular;
	}

	public List<CheckerAction> getJumpMoves(List<CheckerAction> allFeasibleMoves)
	{
		List<CheckerAction> jumpMoves = new ArrayList<CheckerAction>();
		
		for(CheckerAction action: allFeasibleMoves)
		{
			int x1 = action.getMoveTo().getXCoOrdinate();
			int x2 = action.getSelNode().getXCoOrdinate();
			if( Math.abs(x1-x2)==2 )
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
					List<XYLocation> feasibleLocOfNode = getFeasiblePositions(currNode, player);
					for (XYLocation moveToPos : feasibleLocOfNode) {
						allFeasibleMoves.add(new CheckerAction(currNode, moveToPos));
					}
				}
			}
		}

		List<CheckerAction> jumpMoves = getJumpMoves(allFeasibleMoves);
		
		if(!jumpMoves.isEmpty())
			return jumpMoves;
		else
			return allFeasibleMoves; // all regular moves
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
