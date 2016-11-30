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

		if (getFeasiblePositions(opponent).isEmpty())
			return true;
		else
			return false;
	}

	public boolean lineThroughBoard() {
		return (isAnyRowComplete() || isAnyColumnComplete() || isAnyDiagonalComplete());
	}

	private boolean isAnyRowComplete() {
		for (int row = 0; row < 8; row++) {
			String val = getValue(0, row);
			if (val != EMPTY && val == getValue(1, row) && val == getValue(2, row) && val == getValue(3, row)
					&& val == getValue(4, row) && val == getValue(5, row) && val == getValue(6, row)
					&& val == getValue(7, row)) {
				return true;
			}
		}
		return false;
	}

	private boolean isAnyColumnComplete() {
		for (int col = 0; col < 8; col++) {
			String val = getValue(col, 0);
			if (val != EMPTY && val == getValue(col, 1) && val == getValue(col, 2) && val == getValue(col, 3)
					&& val == getValue(col, 4) && val == getValue(col, 5) && val == getValue(col, 6)
					&& val == getValue(col, 7)) {
				return true;
			}
		}
		return false;
	}

	private boolean isAnyDiagonalComplete() {
		boolean retVal = false;
		String val = getValue(0, 0);
		if (val != EMPTY && val == getValue(1, 1) && val == getValue(2, 2) && val == getValue(3, 3)
				&& val == getValue(4, 4) && val == getValue(5, 5) && val == getValue(6, 6) && val == getValue(7, 7)) {
			return true;
		}
		val = getValue(0, 7);
		if (val != EMPTY && val == getValue(1, 6) && val == getValue(2, 5) && val == getValue(3, 4)
				&& val == getValue(4, 3) && val == getValue(5, 2) && val == getValue(6, 1) && val == getValue(7, 0)) {
			return true;
		}
		return retVal;
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

		List<XYLocation> result = new ArrayList<XYLocation>();

		if (getValue(selNode).equals(player)) {
			int col = selNode.getXCoOrdinate();
			int row = selNode.getYCoOrdinate();
			List<XYLocation> moveToPos = new ArrayList<XYLocation>();
			// first, check the four corners
			moveToPos.add(new XYLocation(col - 1, row - 1));
			moveToPos.add(new XYLocation(col - 1, row + 1));
			moveToPos.add(new XYLocation(col + 1, row + 1));
			moveToPos.add(new XYLocation(col + 1, row - 1));
			for (XYLocation pos : moveToPos) {
				if (pos.isWithinBoundary(0, 7))
					if (getValue(pos).equals(EMPTY))
					{
						if(isKing(selNode))
							result.add(pos);
						else if (isForward(selNode, pos, player))
							result.add(pos);
					}
			}
			moveToPos.clear();
			// second, check if it can jump
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
								result.add(pos);
							else if (isForward(selNode, pos, player))
								result.add(pos);
						}
					}
			}
		}

		return result;
	}

	public List<CheckerAction> getFeasiblePositions() {

		return getFeasiblePositions(getPlayerToMove());
	}

	public List<CheckerAction> getFeasiblePositions(String player) {

		List<CheckerAction> result = new ArrayList<CheckerAction>();

		for (int col = 0; col < 8; col++) {
			for (int row = 0; row < 8; row++) {
				if (getValue(col, row).equals(player)) {
					XYLocation currNode = new XYLocation(col, row);
					List<XYLocation> feasibleLocOfNode = getFeasiblePositions(currNode, player);
					for (XYLocation moveToPos : feasibleLocOfNode) {
						result.add(new CheckerAction(currNode, moveToPos));
					}
				}
			}
		}

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
