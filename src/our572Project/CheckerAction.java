package our572Project;

/**
 * @Auther Guangyu Hou
 * */

import java.util.ArrayList;
import java.util.List;

public class CheckerAction implements Comparable<CheckerAction>{

	XYLocation selNode;
	XYLocation moveTo;
	
	// the following is for multi-jumps
	CheckerAction parent;
	List<CheckerAction> nextJumps;
	boolean hasNextJumps = false;
	boolean isMultiJump = false;
	
	List <XYLocation> moveToSequence; // more multi-Jumps
	
	double evalValue = Double.NEGATIVE_INFINITY;
	
	public CheckerAction(XYLocation selNode_, XYLocation moveTo_) {
		selNode = selNode_;
		moveTo = moveTo_;
	}
	
	public CheckerAction(XYLocation selNode_, XYLocation moveTo_,List<XYLocation> moveToSequence_) {
		selNode = selNode_;
		moveTo = moveTo_;
		moveToSequence = moveToSequence_;
		isMultiJump = true;
	}

	public XYLocation getSelNode() {
		return selNode;
	}

	public XYLocation getMoveTo() {
		return moveTo;
	}

	public boolean hasNextJumps()
	{
		return hasNextJumps;
	}
	
	public boolean isMultiJump()
	{
		return isMultiJump;
	}
	
	public List<CheckerAction> getNextJumps()
	{
		return nextJumps;
	}
		
	public void setNextJumps(List<CheckerAction> nextJumps_)
	{
		hasNextJumps = true;
		nextJumps = nextJumps_;
	}
	
	public void setParentJump(CheckerAction parent_)
	{
		parent = parent_;
	}
	
	public CheckerAction getParent()
	{
		return parent;
	}
	
	public List<XYLocation> getMoveToSequence(){
		return moveToSequence;
	}
	
	public void setEvalValue(double value)
	{
		evalValue = value;
	}
	
	public List<XYLocation> getInternalPos()
	{
		List<XYLocation> internalPos = null;
		if(moveToSequence!=null && !moveToSequence.isEmpty())
			internalPos =  new ArrayList<XYLocation>(moveToSequence.subList(0, moveToSequence.size()-1));
		
		return internalPos;
	}
	
	public List<XYLocation> getCompleteMultiJumpPath()
	{
		// return all positions of the move: from first to last positions.
		List<XYLocation> historyPath = new ArrayList<XYLocation>();
		
		if(!isMultiJump)
		{
			historyPath.add(selNode);
			historyPath.add(moveTo);
		}
		else
		{
			if(moveToSequence!=null && !moveToSequence.isEmpty())
				historyPath.addAll(moveToSequence);
			historyPath.add(selNode); //add head
		}
		
		return historyPath;
	}
	@Override
	public boolean equals(Object o) {
		if (null == o || !(o instanceof CheckerAction)) {
			return super.equals(o);
		}

		CheckerAction anotherAct = (CheckerAction) o;
		return (anotherAct.getSelNode().equals(selNode) && anotherAct.getMoveTo().equals(moveTo));
	}

	@Override
    public int compareTo(CheckerAction other) {
		if( evalValue < other.evalValue)
			return -1;
		else if (evalValue == other.evalValue)
			return 0;
		else
			return 1;
    }
	
	@Override
	public String toString() {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(" selNode = " + selNode.toString() + " moveTo= " + moveTo.toString());
		if(moveToSequence!=null && !moveToSequence.isEmpty())
		{
			strBuilder.append(System.getProperty("line.separator"));
			strBuilder.append(" intermediate pos:");
			for(int i=0;i<moveToSequence.size()-1;i++)
				strBuilder.append(" "+moveToSequence.get(i).toString());
		}
		strBuilder.append(" evalVal= "+ evalValue);
		
		return strBuilder.toString();
	}

}
