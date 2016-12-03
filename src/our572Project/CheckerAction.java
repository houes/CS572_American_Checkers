package our572Project;

import java.util.List;

public class CheckerAction {

	XYLocation selNode;
	XYLocation moveTo;
	List <XYLocation> moveToKingRow;

	public CheckerAction(XYLocation selNode_, XYLocation moveTo_) {
		selNode = selNode_;
		moveTo = moveTo_;
	}
	
	public CheckerAction(XYLocation selNode_, XYLocation moveTo_,List<XYLocation> moveToKingRow_) {
		selNode = selNode_;
		moveTo = moveTo_;
		moveToKingRow = moveToKingRow_;
	}

	public XYLocation getSelNode() {
		return selNode;
	}

	public XYLocation getMoveTo() {
		return moveTo;
	}

	public List<XYLocation> getmvoeToKingRow(){
		return moveToKingRow;
	}
	
	@Override
	public boolean equals(Object o) {
		if (null == o || !(o instanceof XYLocation)) {
			return super.equals(o);
		}

		CheckerAction anotherAct = (CheckerAction) o;
		return (anotherAct.getSelNode().equals(selNode) && anotherAct.getMoveTo().equals(moveTo));
	}

	@Override
	public String toString() {
		return " selNode = " + selNode.toString() + " moveTo= " + moveTo.toString();
	}

}
