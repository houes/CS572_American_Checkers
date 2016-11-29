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
	public static final String EMPTY = "-";
	//
	private String[] board = new String[] { EMPTY, X, EMPTY, X, EMPTY, X, EMPTY, X, X, EMPTY, X, EMPTY, X, EMPTY, X,
			EMPTY, EMPTY, X, EMPTY, X, EMPTY, X, EMPTY, X, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY,
			EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, O, EMPTY, O, EMPTY, O, EMPTY, O, EMPTY, EMPTY, O,
			EMPTY, O, EMPTY, O, EMPTY, O, O, EMPTY, O, EMPTY, O, EMPTY, O, EMPTY };

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

	public double getUtility() {
		return utility;
	}

	public void mark(CheckerAction action) {

		XYLocation from_pos = action.getSelNode();
		XYLocation to_pos = action.getMoveTo();

		dismark(from_pos.getXCoOrdinate(), from_pos.getYCoOrdinate());
		mark(to_pos.getXCoOrdinate(), to_pos.getYCoOrdinate());

		if (Math.abs(to_pos.getXCoOrdinate() - from_pos.getXCoOrdinate()) == 2) {
			int midx = (from_pos.getXCoOrdinate() + to_pos.getXCoOrdinate()) / 2;
			int midy = (from_pos.getYCoOrdinate() + to_pos.getYCoOrdinate()) / 2;
			dismark(midx, midy);
		}
	}

	public void dismark(int col, int row) {
		if (utility == -1 && board[getAbsPosition(col, row)] == playerToMove) {
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
		if (lineThroughBoard()) {
			utility = (playerToMove == X ? 1 : 0);
		} else if (getNumberOfMarkedPositions() == 64) {
			utility = 0.5;
		}
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

	public int getNumberOfBlackPieces() {
		int retVal = 0;
		for (int col = 0; col < 8; col++) {
			for (int row = 0; row < 8; row++) {
				if (getValue(col, row).equals("X")) {
					retVal++;
				}
			}
		}
		return retVal;
	}

	public boolean isForward(XYLocation origin, XYLocation destination)
	{
		if(getPlayerToMove().equals(X)) // red
		{
			if(destination.getYCoOrdinate() - origin.getYCoOrdinate() >0 )
				return true;
			else 
				return false;
		}
		else if (getPlayerToMove().equals(O))// white
		{
			if(destination.getYCoOrdinate() - origin.getYCoOrdinate() <0 )
				return true;
			else
				return false;
		}
		else
		{
			 System.out.println("Error in isForward()!");
			 return false;
		}
			
	}
	
	public List<XYLocation> getFeasiblePositions(XYLocation selNode) {

		List<XYLocation> result = new ArrayList<XYLocation>();

		if (getValue(selNode).equals(playerToMove)) {
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
						if(isForward(selNode, pos))
						result.add(pos);
			}
			moveToPos.clear();
			// second, check if it can jump
			String opponent = (playerToMove == X ? O : X);
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
							if(isForward(selNode, pos))
								result.add(pos);
					}
			}
		}

		return result;
	}

	public List<CheckerAction> getFeasiblePositions() {

		List<CheckerAction> result = new ArrayList<CheckerAction>();

		for (int col = 0; col < 8; col++) {
			for (int row = 0; row < 8; row++) {
				XYLocation currNode = new XYLocation(col,row);
				List<XYLocation> feasibleLocOfNode =getFeasiblePositions(currNode);
				for(XYLocation moveToPos:feasibleLocOfNode)
				{
					result.add(new CheckerAction(currNode,moveToPos));
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
