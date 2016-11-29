package our572Project;

public class CheckerAction {

	XYLocation selNode;
	XYLocation moveTo;

	public CheckerAction(XYLocation selNode_, XYLocation moveTo_) {
		selNode = selNode_;
		moveTo = moveTo_;
	}

	public XYLocation getSelNode() {
		return selNode;
	}

	public XYLocation getMoveTo() {
		return moveTo;
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
