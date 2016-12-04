package our572Project;

import java.awt.BorderLayout;
import java.awt.Color;
//import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
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
 * Simple graphical Tic-tac-toe game application. It demonstrates the Minimax
 * algorithm for move selection as well as alpha-beta pruning.
 * 
 * @author Ruediger Lunde
 */
public class CheckersApp {

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

		/** Standard constructor. */
		CheckersPanel() {
			this.setLayout(new BorderLayout());
			JToolBar tbar = new JToolBar();
			tbar.setFloatable(false);
			strategyCombo = new JComboBox<String>(new String[] { "Minimax", "Alpha-Beta",
					"Iterative Deepening Alpha-Beta", "Iterative Deepening Alpha-Beta (log)" });
			strategyCombo.setSelectedIndex(1);
			tbar.add(strategyCombo);
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
			// Font f = new java.awt.Font(Font.SANS_SERIF, Font.PLAIN, 32);
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
				//square.setBorderPainted(false);
				
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
				currState = game.getInitialState();
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
						squares[absPos].setBackground(new Color(255, 255, 204));
						squares[absPos].setOpaque(true);
						//squares[absPos].setBorderPainted(false);
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
			CheckerAction action;
			switch (strategyCombo.getSelectedIndex()) {
			case 0:
				search = MinimaxSearch.createFor(game);
				break;
			case 1:
				search = AlphaBetaSearch.createFor(game);
				break;
			case 2:
				search = IterativeDeepeningAlphaBetaSearch.createFor(game, 0.0, 1.0, 1000);
				break;
			default:
				search = IterativeDeepeningAlphaBetaSearch.createFor(game, 0.0, 1.0, 1000);
				((IterativeDeepeningAlphaBetaSearch<?, ?, ?>) search).setLogEnabled(true);
			}
			action = search.makeDecision(currState);
			searchMetrics = search.getMetrics();
			currState = game.getResult(currState, action);
			
			if(action.isMultiJump())
				multiJumpInternalPath = action.getCompleteMultiJumpPath();
			else
				multiJumpInternalPath = null;
			
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

			if (searchMetrics != null)
				statusText += "    " + searchMetrics;
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
					//squares[i].setBorderPainted(false);
				}
			}
		}
	}
}
