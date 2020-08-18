package com.codewithphiliks.gameOfLife;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class GameOfLifeVisualizer extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private final int PIXEL_DIM = 15;
	private final int WIDTH = 1000 - 1000 % PIXEL_DIM;
	private final int HEIGHT = 600 - 600 % PIXEL_DIM;
	private final int TICK_SPEED = 100;
	private int[][] cells 	= new int[HEIGHT / PIXEL_DIM][WIDTH / PIXEL_DIM];
	private int[][] output	= new int[HEIGHT / PIXEL_DIM][WIDTH / PIXEL_DIM];
	private Timer loop;
	private JFrame frame;

	private GameOfLifeVisualizer() {
		setBackground(Color.BLACK);
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		initFrame();
		initMouseInput();
		initKeyInput();
		initTimer();
	}
	
	private void initFrame() {
		frame = new JFrame("Game of Life Visualization");
		frame.setResizable(false);
		frame.setContentPane(this);
		frame.validate();
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		requestFocusInWindow();
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				int result = JOptionPane.showConfirmDialog(
								frame, 
								"Are you sure you want to quit?",
								"Wanna go bye bye?",
								JOptionPane.OK_CANCEL_OPTION);
				if(result == JOptionPane.OK_OPTION) {
					if(loop.isRunning()) {
						loop.stop();
						JOptionPane.showMessageDialog(
								frame, 
								"You left the timer running! Fine I'll handle it.", 
								"Don't be lazy next time :)", 
								JOptionPane.INFORMATION_MESSAGE);
					}
					
					frame.setVisible(false);
					frame.dispose();
					System.exit(0);
				}
			}
		});
	}
	
	private void initMouseInput() {
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				processInput(e.getPoint(), e);
			}
		});
		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				Point p = e.getPoint();
				
				if(p.x < 0 || p.x > WIDTH || p.y < 0 || p.y > HEIGHT)	return;
				
				processInput(p, e);
			}
		});
	}
	
	private void initKeyInput() {
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				switch(key) {
					case KeyEvent.VK_S:
						if(countLivingCells() == 0) {
							JOptionPane.showMessageDialog(
									frame, 
									"Where's the drawing? What? You want me to stare in nothing? Not gonna do that :P", 
									"Nope! Not gonna run that :P", 
									JOptionPane.INFORMATION_MESSAGE);
							return;
						}
						
						if(loop.isRunning()) {
							JOptionPane.showMessageDialog(
									frame, 
									"The timer is still running. Either you (R)estart or (P)ause the simulation.", 
									"Have patience.", 
									JOptionPane.INFORMATION_MESSAGE);
							return;
						}
						
						JOptionPane.showMessageDialog(
								frame, 
								"Initiating Game of Life Visualizer", 
								"You Pressed Start :)", 
								JOptionPane.INFORMATION_MESSAGE);
						loop.start();
						break;
					case KeyEvent.VK_P:
						if(!loop.isRunning()) {
							JOptionPane.showMessageDialog(
									frame, 
									"The simulation doesn't even run.", 
									"What do I pause? You?", 
									JOptionPane.INFORMATION_MESSAGE);
							return;
						}
						
						loop.stop();
						JOptionPane.showMessageDialog(
								frame, 
								"You gave the program some time to breathe. Thanks! :)",
								"Visualization paused :)",
								JOptionPane.INFORMATION_MESSAGE);
						break;
					case KeyEvent.VK_R:
						doneVisualization();
						JOptionPane.showMessageDialog(
								frame, 
								"Restarting...", 
								"Want to draw more?", 
								JOptionPane.INFORMATION_MESSAGE);
						break;
					case KeyEvent.VK_Q:
						JOptionPane.showMessageDialog(
								frame, 
								"I [the program] will populate the grid since you're too lazy!", 
								"Randomizing Cells", 
								JOptionPane.INFORMATION_MESSAGE);
						randomizeCells();
						repaint();
						break;
				}
			}
		});
	}
	
	private void initTimer() {
		loop = new Timer(TICK_SPEED, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(countLivingCells() == 0) {
					JOptionPane.showMessageDialog(
							frame, 
							"Your drawing sucks and those poor cells paid the prize. Thanks I guess :(",
							"Your visualization is done!",
							JOptionPane.INFORMATION_MESSAGE);
					doneVisualization();
				}
				
				updateOutput();
				updateState();
				
				repaint();
			}
		});
	}
	
	private void initCells() {
		for(int row = 0; row < cells.length; row++)
			for(int col = 0; col < cells[row].length; col++) {
				cells[row][col] = 0;
				output[row][col] = 0;
			}
	}
	
	private void randomizeCells() {
		for(int row = 1; row < cells.length - 1; row++)
			for(int col = 1; col < cells[row].length - 1; col++) {
				output[row][col] = 0;
				cells[row][col] = (int)(Math.random() * 2);
			}
	}
	
	private void processInput(Point p, MouseEvent mouseEvent) {
		if(loop.isRunning()) return;
		
		int x = p.x - p.x % PIXEL_DIM;
		int y = p.y - p.y % PIXEL_DIM;
		
		cells[y / PIXEL_DIM][x / PIXEL_DIM] = 
				SwingUtilities.isRightMouseButton(mouseEvent) ? 0 : 1;
		repaint();
	}
	
	private int countLivingCells() {
		int livingCells = 0;
		
		for(int row = 0; row < cells.length; row++)
			for(int col = 0; col < cells[row].length; col++)
				livingCells += cells[row][col];
		
		return livingCells;
	}
	
	private void doneVisualization() {
		loop.stop();
		initCells();
		repaint();
	}
	
	private void updateOutput() {
		for(int row = 1; row < cells.length - 1; row++)
			for(int col = 1; col < cells[row].length - 1; col++) {
				int live_neighbor = 
						cells[row - 1][col - 1] + cells[row - 1][col] + cells[row - 1][col + 1] +
						cells[row][col - 1] 	+ 			0		  + cells[row][col + 1] +
						cells[row + 1][col - 1] + cells[row + 1][col] + cells[row + 1][col + 1];
				
				if(cells[row][col] == 1)
					output[row][col] = 	live_neighbor < 2 ||  // dies due to underpopulation
									  	live_neighbor > 3 ? 0 // dies due to overpopulation
									  	: 1;	// otherwise it survives
				else // cells[row][col] == 0
					if(live_neighbor == 3)	// lives due to reproduction
						output[row][col] = 1;
			}
	}
	
	private void updateState() {
		for(int row = 1; row < cells.length - 1; row++)
			for(int col = 1; col < cells[row].length - 1; col++)
				cells[row][col] = output[row][col];
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2d = (Graphics2D)g;
		
		Rectangle2D.Float rect;
		for(int row = 0; row < HEIGHT / PIXEL_DIM; row++)
			for(int col = 0; col < WIDTH / PIXEL_DIM; col++) {
				g2d.setColor(
						cells[row][col] == 0 ? Color.WHITE.darker() : Color.BLACK);
				rect = new Rectangle2D.Float(col * PIXEL_DIM, row * PIXEL_DIM, PIXEL_DIM, PIXEL_DIM);
				g2d.fill(rect);
				
				Stroke oldStroke = g2d.getStroke();
				g2d.setStroke(new BasicStroke(1.5f));
				g2d.setColor(Color.GRAY);
				g2d.draw(rect);
				g2d.setStroke(oldStroke);
			}
		
		Stroke oldStroke = g2d.getStroke();
		g2d.setStroke(new BasicStroke(2*PIXEL_DIM));
		g2d.setColor(Color.RED);
		rect = new Rectangle2D.Float(0, 0, WIDTH, HEIGHT);
		g2d.draw(rect);
		g2d.setStroke(oldStroke);
		
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new GameOfLifeVisualizer());
	}
}
