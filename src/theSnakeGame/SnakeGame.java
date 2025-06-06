package theSnakeGame;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.SynchronousQueue;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import javax.imageio.ImageIO;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {

	// FRAME
	static JFrame frame;
	final int SQUARE_SIZE = 25;
	final int BORDER_SIZE = 100; // the border around the board, the original was 4 i changed to 100 for more
									// space
	final int GAME_WIDTH = 500;
	final int GAME_HEIGHT = 500;
	final int GAME_UNIT_LENGTH = (GAME_WIDTH) / (SQUARE_SIZE);

	// SOUND
//	static Clip background, soundEff1, winSound, soundEff2;
//	static AudioInputStream audioStream1;
//	static AudioInputStream audioStream2;
//	static AudioInputStream audioStream3;
//	static File file1;
//	static File file2;

	// ITEMS, PLAYER, AND ORIENTATION
	int[] xPos, yPos;
	int dir = 2;
	int bodyLength = 40;
	int score = 0;

	// GAME MECHANAICS
	Timer timer; // times the game
	int delay = 75;
//	Cursor cursor; // changing cursor
	Image cursorImage, backgroundImage, appleImage, bombImage, powerUpImage;
	BufferedImage [] snakeHeadPic, snakeBodyPic, snakeTailPic;
	static BufferedImage snakeImage;

	// For drawing images offScreen (prevents Flicker)
	// These variables keep track of an off screen image object and
//	// its corresponding graphics object
//	Image offScreenImage;
//	Graphics offScreenBuffer;

	int gameOver = 0; // 0 shows that game is not over, 1 means game is over, if game over is 2 that means we are at main menu, we will implement this further down the line

	public SnakeGame() {
		// setting size of the game
		this.setPreferredSize(new Dimension(2 * BORDER_SIZE + GAME_WIDTH, 2 * BORDER_SIZE + GAME_HEIGHT));
		this.setBackground(new Color(135, 178, 0));

		xPos = new int[GAME_UNIT_LENGTH * GAME_UNIT_LENGTH]; // there are 20 squares
		yPos = new int[GAME_UNIT_LENGTH * GAME_UNIT_LENGTH]; //

		// stating the size of image arrays for snake
		snakeHeadPic = new BufferedImage[4];
		snakeBodyPic = new BufferedImage[6];
		snakeTailPic = new BufferedImage[4];
		
		MediaTracker tracker = new MediaTracker(this);
		
		// head
		for (int i = 0; i < 4; i++) {
			snakeHeadPic[i] = snakeImage.getSubimage(1 + i * SQUARE_SIZE, 1, SQUARE_SIZE - 1, SQUARE_SIZE - 1);
			tracker.addImage(snakeHeadPic[i], i);
		}
		// body index 0 and 1 are straight body, index 2 to 5 are turning
		for (int i = 0; i < 6; i++) {
			if (i < 2)
				snakeBodyPic[i] = snakeImage.getSubimage(1 + i * SQUARE_SIZE, SQUARE_SIZE + 1, SQUARE_SIZE - 1, SQUARE_SIZE - 1);
			else
				snakeBodyPic[i] = snakeImage.getSubimage(1 + (i - 2) * SQUARE_SIZE, 3 * SQUARE_SIZE + 1, SQUARE_SIZE - 1, SQUARE_SIZE - 1);
			tracker.addImage(snakeBodyPic[i], i);
		}
		// tail
		for (int i = 0; i < 4; i++) {
			snakeTailPic[i] = snakeImage.getSubimage(1 + i * 25, 2 * SQUARE_SIZE + 1, SQUARE_SIZE - 1, SQUARE_SIZE - 1);
			tracker.addImage(snakeTailPic[i], i);
		}
		
		try {
			tracker.waitForAll();
		} catch (InterruptedException e) {
		}
		
		// Set up the icon image (Tracker not needed for the icon image)
		Image iconImage = Toolkit.getDefaultToolkit().getImage("snakeIcon.png");
		frame.setIconImage(iconImage);

		// Start a new game and then make the window visible
		startGame();

		setFocusable(true); // Need this to set the focus to the panel in order to add the keyListener
		addKeyListener(this);

	} // Constructor

	@Override
	public void actionPerformed(ActionEvent event) {
		if (gameOver == 0) {
			move();
//			checkApple();
//			checkCollision();
		}
		repaint();
	}

	public void startGame() {
		newApple();
		gameOver = 0;
		timer = new Timer(delay, this);
		timer.start();
	}

	// KeyListener methods
	public void keyPressed(KeyEvent kp) {
		// using a switch case for efficiency
		switch (kp.getKeyCode()) {
		case KeyEvent.VK_LEFT:
			if (dir != 2)
				dir = 4;
			break;
		case KeyEvent.VK_RIGHT:
			if (dir != 4)
				dir = 2;
			break;
		case KeyEvent.VK_UP:
			if (dir != 3)
				dir = 1;
			break;
		case KeyEvent.VK_DOWN:
			if (dir != 1)
				dir = 3;
			break;
		}
	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}

	// Avoid flickering -- smoother graphics
	public void update(Graphics g) {
		paint(g);
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		draw(g);
	}

	public void draw(Graphics g) {
		if (gameOver == 0) { // game is playing

			g.setColor(new Color(198, 238, 0));
			g.fillRect(BORDER_SIZE, BORDER_SIZE, GAME_WIDTH, GAME_HEIGHT);

			g.setColor(new Color(194, 109, 0));
			for (int i = 0; i < GAME_WIDTH / SQUARE_SIZE + 1; i++) {
				g.drawLine(i * SQUARE_SIZE + BORDER_SIZE, BORDER_SIZE, i * SQUARE_SIZE + BORDER_SIZE,
						GAME_HEIGHT + BORDER_SIZE);
				g.drawLine(BORDER_SIZE, i * SQUARE_SIZE + BORDER_SIZE, GAME_WIDTH + BORDER_SIZE,
						i * SQUARE_SIZE + BORDER_SIZE);
			}

			for (int i = 0; i < bodyLength; i++) {
				if (i == 0) {
					switch (dir) {
					case 1:
						g.drawImage(snakeHeadPic[0], xPos[i], yPos[i], SQUARE_SIZE, SQUARE_SIZE, this);
						break;
					case 2:
						g.drawImage(snakeHeadPic[1], xPos[i], yPos[i], SQUARE_SIZE, SQUARE_SIZE, this);
						break;
					case 3:
						g.drawImage(snakeHeadPic[2], xPos[i], yPos[i], SQUARE_SIZE, SQUARE_SIZE, this);
						break;
					case 4:
						g.drawImage(snakeHeadPic[3], xPos[i], yPos[i], SQUARE_SIZE, SQUARE_SIZE, this);
						break;
					}
				} 
				else if (i == bodyLength - 1) {
					if (yPos[i] == yPos[i - 1] - SQUARE_SIZE)
						g.drawImage(snakeTailPic[2], xPos[i], yPos[i], SQUARE_SIZE, SQUARE_SIZE, this);
					else if (yPos[i] == yPos[i - 1] + SQUARE_SIZE)
						g.drawImage(snakeTailPic[0], xPos[i], yPos[i], SQUARE_SIZE, SQUARE_SIZE, this);
					else if (xPos[i] == xPos[i - 1] - SQUARE_SIZE)
						g.drawImage(snakeTailPic[1], xPos[i], yPos[i], SQUARE_SIZE, SQUARE_SIZE, this);
					else 
						g.drawImage(snakeTailPic[3], xPos[i], yPos[i], SQUARE_SIZE, SQUARE_SIZE, this);
						
				}
				else {
					if (xPos[i + 1] == xPos[i - 1])
						g.drawImage(snakeBodyPic[0], xPos[i], yPos[i], SQUARE_SIZE, SQUARE_SIZE, this);
					
					else if (yPos[i + 1] == yPos[i - 1])
						g.drawImage(snakeBodyPic[1], xPos[i], yPos[i], SQUARE_SIZE, SQUARE_SIZE, this);
					
					else if ((yPos[i] == yPos[i + 1] - SQUARE_SIZE || yPos[i] == yPos[i - 1] - SQUARE_SIZE) && (xPos[i] == xPos[i + 1] - SQUARE_SIZE || xPos[i] == xPos[i - 1] - SQUARE_SIZE))
						g.drawImage(snakeBodyPic[3], xPos[i], yPos[i], SQUARE_SIZE, SQUARE_SIZE, this);
					
					else if ((yPos[i] == yPos[i + 1] + SQUARE_SIZE || yPos[i] == yPos[i - 1] + SQUARE_SIZE) && (xPos[i] == xPos[i + 1] + SQUARE_SIZE || xPos[i] == xPos[i - 1] + SQUARE_SIZE))
						g.drawImage(snakeBodyPic[5], xPos[i], yPos[i], SQUARE_SIZE, SQUARE_SIZE, this);
					
					else if ((yPos[i] == yPos[i + 1] + SQUARE_SIZE || yPos[i] == yPos[i - 1] + SQUARE_SIZE) && (xPos[i] == xPos[i + 1] - SQUARE_SIZE || xPos[i] == xPos[i - 1] - SQUARE_SIZE))
						g.drawImage(snakeBodyPic[2], xPos[i], yPos[i], SQUARE_SIZE, SQUARE_SIZE, this);
					
					else if ((yPos[i] == yPos[i + 1] - SQUARE_SIZE || yPos[i] == yPos[i - 1] - SQUARE_SIZE) && (xPos[i] == xPos[i + 1] + SQUARE_SIZE || xPos[i] == xPos[i - 1] + SQUARE_SIZE))
						g.drawImage(snakeBodyPic[4], xPos[i], yPos[i], SQUARE_SIZE, SQUARE_SIZE, this);
				}
			}

		} else if (gameOver == 1) { // game is over
			gameOverDraw(g);
		} else { // game is in main menu
			mainMenuDraw(g);
		}
	}

	public void gameOverDraw(Graphics g) {

	}

	public void mainMenuDraw(Graphics g) {

	}

	public void newApple() {

	}

	public void move() {
		for (int i = bodyLength; i > 0; i--) {
			xPos[i] = xPos[i - 1];
			yPos[i] = yPos[i - 1];

			System.out.println("xPos at " + i + ": " + xPos[i] + " yPos at " + i + ": " + yPos[i]);
		}

		switch (dir) {
		case 1:
			yPos[0] = yPos[0] - SQUARE_SIZE;
			break;
		case 2:
			xPos[0] = xPos[0] + SQUARE_SIZE;
			break;
		case 3:
			yPos[0] = yPos[0] + SQUARE_SIZE;
			break;
		case 4:
			xPos[0] = xPos[0] - SQUARE_SIZE;
			break;
		}
	}

	public static void main(String[] args) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		// Images
		snakeImage = ImageIO.read(new File("snakeImage.png"));

		frame = new JFrame("The Snake Gmae");
		SnakeGame myPanel = new SnakeGame();

		frame.add(myPanel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

	} // main method
} // ConnectFourWorking class
