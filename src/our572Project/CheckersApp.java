package our572Project;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

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
		boolean select_mode = true;// if true, the current action is to select
									// a
									// piece, if false, the current action is to
									// move to a position
		XYLocation selected_piece; // record the selected piece

		String WarningMsg; // warn the user if he/she clicks on a invalid
							// position

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
			Font f = new java.awt.Font(Font.SANS_SERIF, Font.PLAIN, 32);
			try {
				int ICON_WIDTH = 60;
				int ICON_HEIGHT = 60;
				Image img_red = ImageIO.read(getClass().getResource("resources/red.BMP")).getScaledInstance(ICON_WIDTH,
						ICON_HEIGHT, java.awt.Image.SCALE_SMOOTH);
				;
				Image img_white = ImageIO.read(getClass().getResource("resources/white.BMP"))
						.getScaledInstance(ICON_WIDTH, ICON_HEIGHT, java.awt.Image.SCALE_SMOOTH);
				;
				icon_red = new ImageIcon(img_red);
				icon_white = new ImageIcon(img_white);
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
						} else
							statusBar.setText(nextPlayerColor + " was selected ");

					} else {
						// second click: select move to position
						for (int i = 0; i < 64; i++)
							if (ae.getSource() == squares[i]) {
								XYLocation destination =new XYLocation(i % 8, i / 8);
								CheckerAction cAction = new CheckerAction(selected_piece, destination);
								
								if(currState.getFeasiblePositions(selected_piece).contains(destination))
								{
									currState = game.getResult(currState, cAction);
								}
								else
								{
									repick_piece=true;
									statusBar.setText("Invalid position, You must select a feasible destination!");
								}
								break;
							}
					}
				}
			}

			if (ae == null || ae.getSource() == clearButton || isProposedMove || !select_mode) {
				for (int i = 0; i < 64; i++) {
					String val = currState.getValue(i % 8, i / 8);
					if (val == CheckersState.EMPTY)
						val = "";

					if (val.equals("X"))
						squares[i].setIcon(icon_red);
					else if (val.equals("O"))
						squares[i].setIcon(icon_white);
					else
						squares[i].setIcon(null);
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
		}

		/** Updates the status bar. */
		private void updateStatus() {
			String statusText;
			if (game.isTerminal(currState))
				if (game.getUtility(currState, CheckersState.X) == 1)
					statusText = "Red has won :-)";   // X
				else if (game.getUtility(currState, CheckersState.O) == 1)
					statusText = "White has won :-)"; // O
				else
					statusText = "No winner...";
			else
				statusText = "Next move: " + game.getPlayerByColor(currState);

			if (searchMetrics != null)
				statusText += "    " + searchMetrics;
			statusBar.setText(statusText);
		}
	}
}
