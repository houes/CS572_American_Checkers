package our572Project;

import java.awt.BorderLayout;
import java.awt.Color;
//import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

/**
 * Simple graphical Checkers game application. It demonstrates the Minimax
 * algorithm for move selection as well as alpha-beta pruning.
 * 
 * @author Guangyu Hou, Wandi Xiong, Xiaoqian Mu, modified from AIMA book
 *
 */
public class CheckersApp {

	public static int MaxSearchDepth=6;
	public static int EvalFuncVersion=0; // 0,1,2,3,4,5 version of evaluation functions
	
	/** Used for integration into the universal demo application. */
	public JFrame constructApplicationFrame() {
		JFrame frame = new JFrame();
		JPanel panel = new CheckersPanel();
		frame.add(panel, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		return frame;
	}

	/** Application starter. */
	public static void main(String[] args) {
		JFrame frame = new CheckersApp().constructApplicationFrame();
		frame.setSize(800, 800);
		frame.setVisible(true);
	}

	/** Simple panel to control the game. */
	private static class CheckersPanel extends JPanel implements ActionListener {
		private static final long serialVersionUID = 1L;
		JComboBox<String> strategyCombo;
		JComboBox<String> strategyCombo2;
		JButton clearButton;
		JButton proposeButton;
		JButton[] squares;
		JLabel statusBar;

		CheckersGame game;
		CheckersState currState;
		Metrics searchMetrics;

		ImageIcon icon_red;
		ImageIcon icon_white;
		ImageIcon icon_red_king;
		ImageIcon icon_white_king;
		boolean select_mode = true;// if true, the current action is to select
									// a
									// piece, if false, the current action is to
									// move to a position
		XYLocation selected_piece; // record the selected piece
		List<XYLocation> multiJumpInternalPath; // record the multijump path, only interval pos
		boolean boardColorChanged = false;
		double effectiveBranchingFactor =-1;
		int num_proposemove=0;
		double Branchfactor=0.0;
		
		/** Standard constructor. */
		CheckersPanel() {
			this.setLayout(new BorderLayout());
			JToolBar tbar = new JToolBar();
			tbar.setFloatable(false);
			strategyCombo = new JComboBox<String>(new String[] { "EvalFunc0", "EvalFunc1",
					"EvalFunc2", "EvalFunc3", "EvalFunc4", "EvalFunc5" });
			strategyCombo.setSelectedIndex(0);
			tbar.add(strategyCombo);
			tbar.add(Box.createHorizontalGlue());
			
			strategyCombo2 = new JComboBox<String>(new String[] { "Not set","EvalFunc0", "EvalFunc1",
					"EvalFunc2", "EvalFunc3", "EvalFunc4", "EvalFunc5" });
			strategyCombo2.setSelectedIndex(0);
			tbar.add(strategyCombo2);
			tbar.add(Box.createHorizontalGlue());
			
			clearButton = new JButton("Clear");
			clearButton.addActionListener(this);
			tbar.add(clearButton);
			proposeButton = new JButton("Propose Move");
			proposeButton.addActionListener(this);
			tbar.add(proposeButton);

			add(tbar, BorderLayout.NORTH);
			JPanel spanel = new JPanel();
			spanel.setLayout(new GridLayout(8, 8));
			add(spanel, BorderLayout.CENTER);
			squares = new JButton[64];
			
			try {
				int ICON_WIDTH = 60;
				int ICON_HEIGHT = 60;
				Image img_red = ImageIO.read(getClass().getResource("resources/red.BMP")).getScaledInstance(ICON_WIDTH,
						ICON_HEIGHT, java.awt.Image.SCALE_SMOOTH);
				Image img_white = ImageIO.read(getClass().getResource("resources/white.BMP"))
						.getScaledInstance(ICON_WIDTH, ICON_HEIGHT, java.awt.Image.SCALE_SMOOTH);
				Image img_red_king = ImageIO.read(getClass().getResource("resources/red_king.BMP"))
						.getScaledInstance(ICON_WIDTH, ICON_HEIGHT, java.awt.Image.SCALE_SMOOTH);
				Image img_white_king = ImageIO.read(getClass().getResource("resources/white_king.BMP"))
						.getScaledInstance(ICON_WIDTH, ICON_HEIGHT, java.awt.Image.SCALE_SMOOTH);
				icon_red = new ImageIcon(img_red);
				icon_white = new ImageIcon(img_white);
				icon_red_king = new ImageIcon(img_red_king);
				icon_white_king = new ImageIcon(img_white_king);
			} catch (IOException ex) {
			}
			for (int i = 0; i < 64; i++) {

				JButton square = new JButton("");

				boolean evenRow = false;
				if ((Math.floor(i / 8)) % 2 == 0)
					evenRow = !evenRow;
				int myInt = (evenRow) ? 1 : 0;

				if ((i + myInt) % 2 == 0)
					square.setBackground(new Color(182, 155, 76));
				else
					square.setBackground(new Color(0, 153, 76));

				square.setOpaque(true);
				square.setBorderPainted(false);
				
				square.addActionListener(this);
				squares[i] = square;
				spanel.add(square);
			}
			statusBar = new JLabel(" ");
			statusBar.setBorder(BorderFactory.createEtchedBorder());
			add(statusBar, BorderLayout.SOUTH);

			game = new CheckersGame();
			actionPerformed(null);
		}

		/** Handles all button events and updates the view. */
		@Override
		public void actionPerformed(ActionEvent ae) {
			searchMetrics = null;
			boolean repick_piece = false;
			boolean isProposedMove = false;
			if (ae == null || ae.getSource() == clearButton)
			{
				currState = game.getInitialState();
				multiJumpInternalPath =null;
				setDefaultColor();
				num_proposemove =0;
			}
			else if (!game.isTerminal(currState)) {
				if (ae.getSource() == proposeButton) {
					proposeMove();
					isProposedMove = true;
				} else {
					if (select_mode) {
						// first click: select which piece to move
						for (int i = 0; i < 64; i++)
							if (ae.getSource() == squares[i]) {
								selected_piece = new XYLocation(i % 8, i / 8);
								break;
							}
						String nextPlayer = game.getPlayer(currState);
						String nextPlayerColor = game.getPlayerByColor(currState);
						String currSeletPiece = currState.getValue(selected_piece);
						if (currSeletPiece.equals("EMPTY") || !currSeletPiece.equals(nextPlayer)) {
							repick_piece = true;
							statusBar.setText("Invalid piece, You must select a " + nextPlayerColor + " piece!");
						} else if (currState.getFeasibleDestinations(selected_piece).isEmpty()) {
							repick_piece = true;
							statusBar.setText("Invalid " + nextPlayerColor + " piece, it cannot move, repick!");
						} else if (!currState.getFeasibleMovesFirstNodes().contains(selected_piece)) {
							repick_piece = true;
							statusBar.setText("You have mandatory jump move, repick! current turn: " + nextPlayerColor);
						} else
							statusBar.setText(nextPlayerColor + " was selected ");

					} else {
						// second click: select move to position
						for (int i = 0; i < 64; i++)
							if (ae.getSource() == squares[i]) {
								XYLocation destination = new XYLocation(i % 8, i / 8);
								//CheckerAction cAction = new CheckerAction(selected_piece, destination);

								if (currState.getFeasibleDestinations(selected_piece).contains(destination)) {
									List<CheckerAction> possibleActions = currState.getFeasibleActionsMatching(selected_piece, destination);
									CheckerAction firstAction = possibleActions.get(0);
									CheckerAction chosenAction;
									if(possibleActions.size()==1)
										chosenAction = firstAction;
									else
									{	// TODO: change later for the user to choose one of the possible actions.
										chosenAction = firstAction;
									}
									if(chosenAction.isMultiJump())
										multiJumpInternalPath = chosenAction.getCompleteMultiJumpPath();
									else
										multiJumpInternalPath = null;
									
									currState = game.getResult(currState, chosenAction);
								} else {
									if(currState.getFeasibleMovesFirstNodes().contains(destination)) // user choose a new piece to move
									{
										selected_piece = destination; 
										statusBar.setText("user repick feasible piece, current turn: "+game.getPlayerByColor(currState));
									}
									else // user choose a infeasible position to move to.
									{
										statusBar.setText(
											"Invalid position, You must select a feasible destination(jump is mandatory)! current piece move: "
													+ game.getPlayerByColor(currState)+" "+ selected_piece.toString1());
									}
									repick_piece = true;
								}
								break;
							}
					}
				}
			}

			if (ae == null || ae.getSource() == clearButton || isProposedMove || !select_mode) {
				for (int i = 0; i < 64; i++) {
					String val = currState.getValue(i % 8, i / 8);
					String king_val = currState.getKingValue(i % 8, i / 8);
					if (val == CheckersState.EMPTY)
						val = "";

					if (val.equals("X")) {
						if (king_val.equals("K"))
							squares[i].setIcon(icon_red_king);
						else
							squares[i].setIcon(icon_red);
					} else if (val.equals("O")) {
						if (king_val.equals("K"))
							squares[i].setIcon(icon_white_king);
						else
							squares[i].setIcon(icon_white);
					} else
						squares[i].setIcon(null);
				}
				
				if(multiJumpInternalPath!=null) // we have multi-jump, draw trace
				{
					if(boardColorChanged == true) // clear previous trace if any
					{
						setDefaultColor();
						boardColorChanged = false;
					}
					
					for(XYLocation pos: multiJumpInternalPath)
					{
						int col = pos.getXCoOrdinate();
						int row = pos.getYCoOrdinate();
						int absPos = 8*row+col;
						squares[absPos].setBackground(new Color(102, 0, 204));
						squares[absPos].setOpaque(true);
						squares[absPos].setBorderPainted(false);
					}
					boardColorChanged = true;
				}
				else if(boardColorChanged) // if don't need to draw trace and there are previous trace to clear
				{
					// clear previous trace
					setDefaultColor();
					boardColorChanged = false;
				}

				if (!repick_piece)
					updateStatus();
			}

			if (ae != null && !isProposedMove && !repick_piece)
				select_mode = !select_mode;

			if (ae != null && ae.getSource() == clearButton)
				select_mode = true;
		}

		/** Uses adversarial search for selecting the next action. */
		private void proposeMove() {
			AdversarialSearch<CheckersState, CheckerAction> search;
			search = AlphaBetaSearch.createFor(game);
			
			CheckerAction action;
			switch (strategyCombo.getSelectedIndex()) {
			case 0:
				EvalFuncVersion = 0;
				break;
			case 1:
				EvalFuncVersion = 1;
				break;
			case 2:
				EvalFuncVersion = 2;
				break;
			case 3:
				EvalFuncVersion = 3;
				break;
			case 4:
				EvalFuncVersion = 4;
				break;
			case 5:
				EvalFuncVersion = 5;
				break;
			default:
				EvalFuncVersion = 0;
			}
			
			if(strategyCombo2.getSelectedIndex()!=0) // the user wants to use two evaluation functions alternatively.
			{
				if(num_proposemove%2==1)
				{
					switch (strategyCombo2.getSelectedIndex()) {

					case 1:
						EvalFuncVersion = 0;
						break;
					case 2:
						EvalFuncVersion = 1;
						break;
					case 3:
						EvalFuncVersion = 2;
						break;
					case 4:
						EvalFuncVersion = 3;
						break;
					case 5:
						EvalFuncVersion = 4;
						break;
					case 6:
						EvalFuncVersion = 5;
						break;
					default:
						throw new IllegalArgumentException("strategyCombo2 value illegal!");
					}
				}
				
			}
			
			action = search.makeDecision(currState);
			searchMetrics = search.getMetrics();
			currState = game.getResult(currState, action);
			
			if(action.isMultiJump())
				multiJumpInternalPath = action.getCompleteMultiJumpPath();
			else
				multiJumpInternalPath = null;

			int numNodeExpanded = searchMetrics.getInt(AlphaBetaSearch.METRICS_NODES_EXPANDED);
			effectiveBranchingFactor = Math.pow((double)numNodeExpanded, 1.0/MaxSearchDepth);
			num_proposemove+=1;
			Branchfactor+=effectiveBranchingFactor;
			//for test the feature functions
			//String player=game.getPlayer(currState);
			//game.getEvaluation(currState, player, 1);
		}

		/** Updates the status bar. */
		private void updateStatus() {
			String statusText;
			if (game.isTerminal(currState))
				if (game.isWinner(currState, CheckersState.X))
					statusText = "Red has won :-)"; // X
				else if (game.isWinner(currState, CheckersState.O))
					statusText = "White has won :-)"; // O
				else
					statusText = "No winner...";
			else
				statusText = "Next move: " + game.getPlayerByColor(currState);
					
			DecimalFormat df = new DecimalFormat("#.00"); 
			
			if (searchMetrics != null)
				statusText += "    {Search Depth= " + MaxSearchDepth+"}    "+
							  searchMetrics + "    {Effective Braching Factor: "+ df.format(effectiveBranchingFactor)+"}";
			
			if (game.isTerminal(currState)){
				Branchfactor/=num_proposemove;
				statusText+="   Avg BranchingFactor:"+ df.format(Branchfactor);
				Branchfactor=0;
				num_proposemove=0;
			}
			
			statusText += "  { EvalFunc version="+ EvalFuncVersion+" }";
			
			statusBar.setText(statusText);
		}
		
		private void setDefaultColor()
		{
			for (int i = 0; i < 64; i++) 
			{
				boolean evenRow = false;
				if ((Math.floor(i / 8)) % 2 == 0)
					evenRow = !evenRow;
				int myInt = (evenRow) ? 1 : 0;
				if ((i + myInt) % 2 == 0)
				{
					squares[i].setBackground(new Color(182, 155, 76));
					squares[i].setOpaque(true);
					squares[i].setBorderPainted(false);
				}
			}
		}
	}
}
