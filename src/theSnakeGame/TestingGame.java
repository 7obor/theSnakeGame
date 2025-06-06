package theSnakeGame;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Minimal working Snake Game:
 * - Loads snakeImage.png safely
 * - Initializes snake in the center
 * - Draws grid with BORDER_SIZE offset
 * - Draws snake head (rotating through 4 images) and body
 * - Provides stubs for newApple(), checkApple(), checkCollision()
 */
public class TestingGame extends JPanel implements ActionListener, KeyListener {

    // === FRAME & BOARD PARAMETERS ===
    static JFrame frame;
    private final int SQUARE_SIZE = 25;
    private final int BORDER_SIZE = 100;
    private final int GAME_WIDTH = 500;
    private final int GAME_HEIGHT = 500;
    private final int GAME_UNIT_COUNT = GAME_WIDTH / SQUARE_SIZE; // 20

    // === SNAKE STATE ===
    private int[] xPos, yPos;
    private int dir = 2;         // 1=up, 2=right, 3=down, 4=left
    private int length = 4;
    private int score = 0;

    // === GAME LOOP ===
    private Timer timer;
    private final int DELAY = 75;

    // === IMAGE ASSETS ===
    private BufferedImage[] snakeHeadPic;
    private BufferedImage[] snakeBodyPic;
    private BufferedImage[] snakeTailPic;
    private static BufferedImage snakeSheet;

    // === APPLE (example stub) ===
    private int appleX, appleY;

    // === GAME STATE ===
    // 0 = playing, 1 = game over
    private int gameOver = 0;

    /**
     * Constructor: sets up panel size, initializes arrays, loads subimages
     */
    public TestingGame() {
        // Set panel size to include borders
        this.setPreferredSize(new Dimension(2 * BORDER_SIZE + GAME_WIDTH, 2 * BORDER_SIZE + GAME_HEIGHT));
        this.setBackground(new Color(135, 178, 0));

        // Initialize position arrays for up to GAME_UNIT_COUNT segments
        xPos = new int[GAME_UNIT_COUNT];
        yPos = new int[GAME_UNIT_COUNT];

        // Initialize snake images (4 head + 6 body + 4 tail)
        snakeHeadPic = new BufferedImage[4];
        snakeBodyPic = new BufferedImage[6];
        snakeTailPic = new BufferedImage[4];

        // Extract subimages from snakeSheet
        MediaTracker tracker = new MediaTracker(this);
        for (int i = 0; i < 4; i++) {
            // Head images at row 0, columns i
            snakeHeadPic[i] = snakeSheet.getSubimage(1 + i * SQUARE_SIZE, 1, SQUARE_SIZE, SQUARE_SIZE);
            tracker.addImage(snakeHeadPic[i], i);
        }
        for (int i = 0; i < 6; i++) {
            if (i < 2) {
                // Straight body images at row 1
                snakeBodyPic[i] = snakeSheet.getSubimage(1 + i * SQUARE_SIZE, SQUARE_SIZE + 1, SQUARE_SIZE, SQUARE_SIZE);
            } else {
                // Turning body at row 3
                snakeBodyPic[i] = snakeSheet.getSubimage(1 + (i - 2) * SQUARE_SIZE, 3 * SQUARE_SIZE + 1, SQUARE_SIZE, SQUARE_SIZE);
            }
            tracker.addImage(snakeBodyPic[i], 4 + i);
        }
        for (int i = 0; i < 4; i++) {
            // Tail images at row 2
            snakeTailPic[i] = snakeSheet.getSubimage(1 + i * SQUARE_SIZE, 2 * SQUARE_SIZE + 1, SQUARE_SIZE, SQUARE_SIZE);
            tracker.addImage(snakeTailPic[i], 10 + i);
        }
        try {
            tracker.waitForAll(); // Wait until all subimages are loaded
        } catch (InterruptedException e) {
            System.err.println("Interrupted while loading images");
        }

        // Set window icon (must set AFTER frame is initialized in main)
        Image icon = Toolkit.getDefaultToolkit().getImage("snakeIcon.png");
        if (icon != null) {
            frame.setIconImage(icon);
        }

        // Initialize starting snake positions (center of the board)
        int startX = ((GAME_WIDTH / 2) / SQUARE_SIZE) * SQUARE_SIZE;
        int startY = ((GAME_HEIGHT / 2) / SQUARE_SIZE) * SQUARE_SIZE;
        // Place segments horizontally to the left
        for (int i = 0; i < length; i++) {
            xPos[i] = startX - i * SQUARE_SIZE;
            yPos[i] = startY;
        }

        // Start the game loop
        setFocusable(true);
        addKeyListener(this);
        startGame();
    }

    /** Starts a new game: place an apple, reset state, and start timer */
    private void startGame() {
        newApple();
        gameOver = 0;
        timer = new Timer(DELAY, this);
        timer.start();
    }

    /** Called on each timer tick */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver == 0) {
            move();
            checkApple();
            checkCollision();
        }
        repaint();
    }

    /** Handle key presses to change direction */
    @Override
    public void keyPressed(KeyEvent kp) {
        switch (kp.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                if (dir != 2) dir = 4;
                break;
            case KeyEvent.VK_RIGHT:
                if (dir != 4) dir = 2;
                break;
            case KeyEvent.VK_UP:
                if (dir != 3) dir = 1;
                break;
            case KeyEvent.VK_DOWN:
                if (dir != 1) dir = 3;
                break;
        }
    }
    @Override public void keyReleased(KeyEvent e) { }
    @Override public void keyTyped(KeyEvent e) { }

    /** Prevent flickering */
    @Override
    public void update(Graphics g) {
        paint(g);
    }

    /** Standard Swing painting entry */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    /** Draw the grid, snake, and apple */
    private void draw(Graphics g) {
        if (gameOver == 0) {
            // Draw board background
            g.setColor(new Color(198, 238, 0));
            g.fillRect(BORDER_SIZE, BORDER_SIZE, GAME_WIDTH, GAME_HEIGHT);

            // Draw grid lines
            g.setColor(new Color(194, 109, 0));
            for (int i = 0; i <= GAME_UNIT_COUNT; i++) {
                int pos = i * SQUARE_SIZE;
                g.drawLine(BORDER_SIZE + pos, BORDER_SIZE, BORDER_SIZE + pos, BORDER_SIZE + GAME_HEIGHT);
                g.drawLine(BORDER_SIZE, BORDER_SIZE + pos, BORDER_SIZE + GAME_WIDTH, BORDER_SIZE + pos);
            }

            // Draw apple (for illustration)
            g.setColor(Color.RED);
            g.fillOval(BORDER_SIZE + appleX, BORDER_SIZE + appleY, SQUARE_SIZE, SQUARE_SIZE);

            // Draw snake: head + body
            for (int i = 0; i < length; i++) {
                int drawX = BORDER_SIZE + xPos[i];
                int drawY = BORDER_SIZE + yPos[i];
                if (i == 0) {
                    // Head rotates based on dir
                    switch (dir) {
                        case 1: g.drawImage(snakeHeadPic[0], drawX, drawY, SQUARE_SIZE, SQUARE_SIZE, this); break;
                        case 2: g.drawImage(snakeHeadPic[1], drawX, drawY, SQUARE_SIZE, SQUARE_SIZE, this); break;
                        case 3: g.drawImage(snakeHeadPic[2], drawX, drawY, SQUARE_SIZE, SQUARE_SIZE, this); break;
                        case 4: g.drawImage(snakeHeadPic[3], drawX, drawY, SQUARE_SIZE, SQUARE_SIZE, this); break;
                    }
                } else {
                    // Simple body segment
                    g.setColor(new Color(106, 205, 0));
                    g.fillRect(drawX, drawY, SQUARE_SIZE, SQUARE_SIZE);
                }
            }
        } else {
            // Game over screen
            g.setColor(Color.BLACK);
            g.setFont(new Font("SansSerif", Font.BOLD, 48));
            g.drawString("Game Over!", BORDER_SIZE + GAME_WIDTH/4, BORDER_SIZE + GAME_HEIGHT/2);
        }
    }

    /** Spawn a new apple at a random grid cell */
    private void newApple() {
        int randX = (int)(Math.random() * GAME_UNIT_COUNT);
        int randY = (int)(Math.random() * GAME_UNIT_COUNT);
        appleX = randX * SQUARE_SIZE;
        appleY = randY * SQUARE_SIZE;
    }

    /** If head meets apple, grow and place new apple */
    private void checkApple() {
        if (xPos[0] == appleX && yPos[0] == appleY) {
            length++;
            score++;
            newApple();
        }
    }

    /** Check for collisions with walls or self */
    private void checkCollision() {
        // 1) Wall collision
        if (xPos[0] < 0 || xPos[0] >= GAME_WIDTH || yPos[0] < 0 || yPos[0] >= GAME_HEIGHT) {
            gameOver = 1;
            timer.stop();
        }
        // 2) Self collision (head hits any body segment)
        for (int i = 1; i < length; i++) {
            if (xPos[0] == xPos[i] && yPos[0] == yPos[i]) {
                gameOver = 1;
                timer.stop();
            }
        }
    }

    /** Advance the snake: shift body, then move head by one SQUARE_SIZE in dir */
    private void move() {
        for (int i = length; i > 0; i--) {
            xPos[i] = xPos[i - 1];
            yPos[i] = yPos[i - 1];
        }
        switch (dir) {
            case 1: yPos[0] -= SQUARE_SIZE; break;
            case 2: xPos[0] += SQUARE_SIZE; break;
            case 3: yPos[0] += SQUARE_SIZE; break;
            case 4: xPos[0] -= SQUARE_SIZE; break;
        }
    }

    /** Main: load images, create frame, start game */
    public static void main(String[] args) {
        // 1) Load the sprite sheet
        try {
            snakeSheet = ImageIO.read(new File("snakeImage.png"));
            if (snakeSheet == null) {
                System.err.println("Error: snakeImage.png not found or invalid");
                System.exit(1);
            }
        } catch (IOException e) {
            System.err.println("IOException while loading snakeImage.png");
            e.printStackTrace();
            System.exit(1);
        }

        // 2) Create window before instantiating TestingGame
        frame = new JFrame("The Snake Game");
        TestingGame gamePanel = new TestingGame();

        // 3) Finalize window settings
        frame.add(gamePanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.pack(); 
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
