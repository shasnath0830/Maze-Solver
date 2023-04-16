//package com.company;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;

class MazeSolver extends JPanel {
    private final int width;
    private final int height;
    private int[][] maze;
    private BufferedImage backgroundImage;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {
            int cellSize = 25;
            int mazeWidth = width * cellSize;
            int mazeHeight = height * cellSize;
            g.drawImage(backgroundImage, (mazeWidth - backgroundImage.getWidth()) / 2,
                    (mazeHeight - backgroundImage.getHeight()) / 2, this);
        }
        drawMaze(g);
    }
    // Add this method to draw the maze using the Graphics object
    private void drawMaze(Graphics g) {
        int cellSize = 25; // Change this value to adjust the size of the maze cells
        g.setColor(Color.cyan);//Some color looks better than others
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int cellX = x * cellSize;
                int cellY = y * cellSize;

                if ((maze[x][y] & DIRECTIONS.NORTH.cell) == 0) {
                    g.drawLine(cellX, cellY, cellX + cellSize, cellY);
                }
                if ((maze[x][y] & DIRECTIONS.WEST.cell) == 0) {
                    g.drawLine(cellX, cellY, cellX, cellY + cellSize);
                }
                if ((maze[x][y] & DIRECTIONS.SOUTH.cell) == 0) {
                    g.drawLine(cellX, cellY + cellSize, cellX + cellSize, cellY + cellSize);
                }
                if ((maze[x][y] & DIRECTIONS.EAST.cell) == 0) {
                    g.drawLine(cellX + cellSize, cellY, cellX + cellSize, cellY + cellSize);
                }
            }
        }
        // Draw start and end points
        int blockSize = cellSize / 2;
        int blockCenter = (cellSize - blockSize) / 2;
        g.setColor(Color.red);
        g.fillRect(blockCenter, blockCenter, blockSize, blockSize); // Draw start point
        g.setColor(Color.green);
        g.fillRect((width - 1) * cellSize + blockCenter, (height - 1) * cellSize +
                blockCenter, blockSize, blockSize); // Draw end point
    }
    // all the different directions dfs can be performed
    private enum DIRECTIONS {
        NORTH(1, 0, -1),
        SOUTH(2, 0, 1),
        EAST(4, 1, 0),
        WEST(8, -1, 0);
        // stores the opposite direction of the forward references
        private DIRECTIONS opposite;

        static {
            NORTH.opposite = SOUTH;
            SOUTH.opposite = NORTH;
            WEST.opposite = EAST;
            EAST.opposite = WEST;
        }
        private final int cell, x, y; // bits and direction
        DIRECTIONS(int cell, int x, int y) {
            this.cell = cell;  // refer to the change in x and y when moving in a specified direction
            this.x = x;
            this.y = y;
        }
    }
    private void playBackgroundMusic() {
        try {
            File musicFile = new File("C:\\Users\\soka\\Documents\\school\\Design and Algorithms\\backgroundmusic.wav");
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(musicFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error loading background music");
            e.printStackTrace();
        }
    }
    //dimensions of the maze
    public MazeSolver(int width, int height) {
        this.width = width;
        this.height = height;
        maze = new int[this.width][this.height];
        generateMaze(0, 0);

        try {
            File imageFile = new File("C:\\Users\\soka\\Documents\\school\\Design and Algorithms\\pacbackground.jpg");
            backgroundImage = ImageIO.read(imageFile);
        } catch (IOException e) {
            System.err.println("Error loading background image");
            e.printStackTrace();
        }
        int cellSize = 25; // Change this value to adjust the size of the maze cells
        setPreferredSize(new Dimension(width * cellSize + 2, height * cellSize + 2));
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == 'r' || e.getKeyChar() == 'R') {
                    maze = new int[width][height];
                    generateMaze(0, 0);
                    repaint();
                }
            }
        });
    }
    private void generateMaze(int x_cord, int y_cord) {
        // creates a list of all the values in the directions
        DIRECTIONS[] neighbors = DIRECTIONS.values();
        // shuffles them because it's a randomized maze
        Collections.shuffle(Arrays.asList(neighbors));
        // loops through the different directions
        for (DIRECTIONS dir : neighbors) {
            // new coordinates when adding the direction to the current coordinates
            int new_xcord = x_cord + dir.x;
            int new_ycord = y_cord + dir.y;

            if (isTrue(new_xcord, new_ycord) && isNotVisited(new_xcord, new_ycord)) {
                // performing bitwise or operation between the value at the position and
                maze[x_cord][y_cord] |= dir.cell;
                maze[new_xcord][new_ycord] |= dir.opposite.cell;
                generateMaze(new_xcord, new_ycord);
            }
        }
    }
    // checks if coordinates are within the maze
    private boolean isTrue(int x, int y) {
        return (x >= 0 && y >= 0 && x < width && y < height);
    }
    // valid to move if there's no wall
    private boolean isNotVisited(int x, int y) {
        int empty = 0;
        return maze[x][y] == empty;
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Maze Generator");
            MazeSolver maze = new MazeSolver(35, 35);
            frame.add(maze);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            maze.playBackgroundMusic();
        });
    }
}