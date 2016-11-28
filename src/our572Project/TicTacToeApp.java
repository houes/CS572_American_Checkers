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
public class TicTacToeApp {

	/** Used for integration into the universal demo application. */
	public JFrame constructApplicationFrame() {
		JFrame frame = new JFrame();
		JPanel panel = new TicTacToePanel();
		frame.add(panel, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		return frame;
	}

	/** Application starter. */
	public static void main(String[] args) {
		JFrame frame = new TicTacToeApp().constructApplicationFrame();
		frame.setSize(800, 800);
		frame.setVisible(true);
	}

	/** Simple panel to control the game. */
	private static class TicTacToePanel extends JPanel implements
			ActionListener {
		private static final long serialVersionUID = 1L;
		JComboBox<String> strategyCombo;
		JButton clearButton;
		JButton proposeButton;
		JButton[] squares;
		JLabel statusBar;

		TicTacToeGame game;
		TicTacToeState currState;
		Metrics searchMetrics;
		
		ImageIcon icon_red;
		ImageIcon icon_white;
		
		/** Standard constructor. */
		TicTacToePanel() {
			this.setLayout(new BorderLayout());
			JToolBar tbar = new JToolBar();
			tbar.setFloatable(false);
			strategyCombo = new JComboBox<String>(new String[] { "Minimax",
					"Alpha-Beta", "Iterative Deepening Alpha-Beta",
					"Iterative Deepening Alpha-Beta (log)" });
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
			try{
				int ICON_WIDTH  = 60;
				int ICON_HEIGHT = 60;
				Image img_red = ImageIO.read(getClass().getResource("resources/red.BMP")).getScaledInstance( ICON_WIDTH, ICON_HEIGHT,  java.awt.Image.SCALE_SMOOTH ) ;;
				Image img_white = ImageIO.read(getClass().getResource("resources/white.BMP")).getScaledInstance( ICON_WIDTH, ICON_HEIGHT,  java.awt.Image.SCALE_SMOOTH ) ;;
				icon_red = new ImageIcon(img_red);
				icon_white = new ImageIcon(img_white);
			} catch (IOException ex) 
			{
				
			}
			for (int i = 0; i < 64; i++) {
				
				JButton square = new JButton("");
				
				boolean evenRow = false;
				if((Math.floor(i/8))%2==0) 
					evenRow = !evenRow;
				int myInt = (evenRow) ? 1 : 0;
			
				square.setFont(f);
				//square.setIcon(icon_red);
				if((i+myInt)%2==0)
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

			game = new TicTacToeGame();
			actionPerformed(null);
		}

		/** Handles all button events and updates the view. */
		@Override
		public void actionPerformed(ActionEvent ae) {
			searchMetrics = null;
			if (ae == null || ae.getSource() == clearButton)
				currState = game.getInitialState();
			else if (!game.isTerminal(currState)) {
				if (ae.getSource() == proposeButton)
					proposeMove();
				else {
					for (int i = 0; i < 64; i++)
						if (ae.getSource() == squares[i])
							currState = game.getResult(currState,
									new XYLocation(i % 8, i / 8));
				}
			}
			for (int i = 0; i < 64; i++) {
				String val = currState.getValue(i % 8, i / 8);
				if (val == TicTacToeState.EMPTY)
					val = "";
				//squares[i].setText(val);
				if(val.equals("X"))
					squares[i].setIcon(icon_red);
				else if(val.equals("O"))
					squares[i].setIcon(icon_white);
			}
			updateStatus();
		}

		/** Uses adversarial search for selecting the next action. */
		private void proposeMove() {
			AdversarialSearch<TicTacToeState, XYLocation> search;
			XYLocation action;
			switch (strategyCombo.getSelectedIndex()) {
			case 0:
				search = MinimaxSearch.createFor(game);
				break;
			case 1:
				search = AlphaBetaSearch.createFor(game);
				break;
			case 2:
				search = IterativeDeepeningAlphaBetaSearch.createFor(game, 0.0,
						1.0, 1000);
				break;
			default:
				search = IterativeDeepeningAlphaBetaSearch.createFor(game, 0.0,
						1.0, 1000);
				((IterativeDeepeningAlphaBetaSearch<?, ?, ?>) search)
						.setLogEnabled(true);
			}
			action = search.makeDecision(currState);
			searchMetrics = search.getMetrics();
			currState = game.getResult(currState, action);
		}

		/** Updates the status bar. */
		private void updateStatus() {
			String statusText;
			if (game.isTerminal(currState))
				if (game.getUtility(currState, TicTacToeState.X) == 1)
					statusText = "X has won :-)";
				else if (game.getUtility(currState, TicTacToeState.O) == 1)
					statusText = "O has won :-)";
				else
					statusText = "No winner...";
			else
				statusText = "Next move: " + game.getPlayer(currState);
			if (searchMetrics != null)
				statusText += "    " + searchMetrics;
			statusBar.setText(statusText);
		}
	}
}
