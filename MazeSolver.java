package com.company;
import java.util.*;
import java.util.Arrays;
import java.util.Collections;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

class mazeSolver extends JPanel {
    private final int width;
    private final int height;
    private int[][] maze;
    private BufferedImage backgroundImage;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {
            int cellSize = 20;
            int mazeWidth = width * cellSize;
            int mazeHeight = height * cellSize;
            g.drawImage(backgroundImage, (mazeWidth - backgroundImage.getWidth()) / 2,
                    (mazeHeight - backgroundImage.getHeight()) / 2, this);
        }
        drawMaze(g);
        ArrayList<MazeCells> shortestPath = solveMazeBFS(); //Store the shortest path
        printShortestPath(shortestPath, g, 20, 10, 10); //Color the shortest path on the frame
    }
    // Add this new method to draw the maze using the Graphics object
    private void drawMaze(Graphics g) {
        // (use g to draw the maze based on the printMaze() method)
        int cellSize = 20; // Change this value to adjust the size of the maze cells
        g.setColor(Color.cyan); //Some color looks better than others

        int blockSize = cellSize / 2;
        int blockCenter = (cellSize - blockSize) / 2;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                int cellX = x * cellSize;
                int cellY = y * cellSize;

                if ((maze[x][y] & DIRECTIONS.NORTH.cell) == 0) {
                    g.drawLine(cellX, cellY, cellX + cellSize, cellY);
                }
                if ((maze[x][y] & DIRECTIONS.SOUTH.cell) == 0) {
                    g.drawLine(cellX, cellY + cellSize, cellX + cellSize, cellY + cellSize);
                }
                if ((maze[x][y] & DIRECTIONS.EAST.cell) == 0) {
                    g.drawLine(cellX + cellSize, cellY, cellX + cellSize, cellY + cellSize);
                }
                if ((maze[x][y] & DIRECTIONS.WEST.cell) == 0) {
                    g.drawLine(cellX, cellY, cellX, cellY + cellSize);
                }
            }
        }
        // Draw start and end points
        g.setColor(Color.red);
        g.fillRect(blockCenter, blockCenter, blockSize, blockSize); // Draw start point

        g.setColor(Color.green);
        g.fillRect((width - 1) * cellSize + blockCenter, (height - 1) * cellSize +
                blockCenter, blockSize, blockSize); // Draw end point

        drawShortestPathOnMaze(g);
    }
    public void drawShortestPathOnMaze(Graphics g) {
        int cellSize = 20;
        int blockCenter = (cellSize - (cellSize / 2)) / 2;
        int blockSize = cellSize / 2;

        ArrayList<MazeCells> shortestPath = solveMazeBFS();
        printShortestPath(shortestPath, g, cellSize, blockCenter, blockSize);
    }
    private ArrayList<MazeCells> solveMazeBFS() {
        // Define the possible directions to move in the maze
        int[][] directions = {{0, -1}, {0, 1}, {1, 0}, {-1, 0}};

        //Linked list to store the cells to visit next
        LinkedList<MazeCells> nextMazeCell = new LinkedList<>();

        // Create a set to store the visited cells
        Set<MazeCells> visitedCells = new HashSet<>();

        // Start the search from the top-left cell of the maze
        MazeCells startCell = new MazeCells(0, 0);
        nextMazeCell.add(startCell);
        visitedCells.add(startCell);

        // Keep searching until there are no more cells to visit
        while (!nextMazeCell.isEmpty()) {
            // Remove the first cell from the list of cells to visit next
            MazeCells currentCell = nextMazeCell.remove();

            // Check if the current cell is the bottom-right cell of the maze
            if (currentCell.getXValue() == width - 1 && currentCell.getYValue() == height - 1) {
                // If it is, return the path from the start cell to this cell
                return backtrackPath(currentCell);
            }

            // Iterate over the possible directions to move in the maze
            for (int[] direction : directions) {
                int newX = currentCell.getXValue() + direction[0];
                int newY = currentCell.getYValue() + direction[1];
                MazeCells nextCell = new MazeCells(newX, newY, currentCell);

                // Check if the next cell is within the boundaries of the maze
                if (isTrue(newX, newY) && !visitedCells.contains(nextCell)) {
                    int dx = direction[0], dy = direction[1];
                    boolean canMove = false;

                    // Check if there's a wall in the current direction
                    if (dx == 1 && (maze[currentCell.getXValue()][currentCell.getYValue()] & DIRECTIONS.EAST.cell) != 0) {
                        canMove = true;
                    } else if (dx == -1 && (maze[currentCell.getXValue()][currentCell.getYValue()] & DIRECTIONS.WEST.cell) != 0) {
                        canMove = true;
                    } else if (dy == 1 && (maze[currentCell.getXValue()][currentCell.getYValue()] & DIRECTIONS.SOUTH.cell) != 0) {
                        canMove = true;
                    } else if (dy == -1 && (maze[currentCell.getXValue()][currentCell.getYValue()] & DIRECTIONS.NORTH.cell) != 0) {
                        canMove = true;
                    }

                    // If there's no wall in the current direction, add the next cell to the list of cells to visit next
                    if (canMove) {
                        nextMazeCell.add(nextCell);
                        visitedCells.add(nextCell);
                    }
                }
            }
        }
        //return empty list if no path
        return new ArrayList<MazeCells>();
    }
    private ArrayList<MazeCells> backtrackPath(MazeCells currentCell) {
        ArrayList<MazeCells> path = new ArrayList<>(); //Stores the path from the currentCell
        MazeCells iteration = currentCell;

        while(iteration != null) {//If the cell still has a parent,
            path.add(iteration); //Add the currentCell to the path
            iteration = iteration.getParentCell(); //Get the parent of the currentCell
        }
        return path; //Return the path form the start cell to the end cell
    }
    public void printShortestPath(ArrayList<MazeCells> shortestPath, Graphics g, int cellSize, int blockCenter, int blockSize) {
        //Iterate through every cell in the maze found in the shortest path and color it blue
        g.setColor(Color.blue);
        for(MazeCells cell: shortestPath) {
            if(!(cell.getXValue() == 0 && cell.getYValue() == 0) && !(cell.getXValue() == width-1 && cell.getYValue() == height-1) ) {
                int cellX = cell.getXValue() * cellSize;
                int cellY = cell.getYValue() * cellSize;
                g.fillRect(cellX + blockCenter, cellY + blockCenter, blockSize, blockSize);
            }
        }
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
        private final int cell, x, y;
        // bits and direction
        DIRECTIONS(int cell, int x, int y) {
            this.cell = cell;
            // refer to the change in x and y when moving in a specified direction
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
    // dimensions of the maze
    public mazeSolver(int width, int height) {
        this.width = width;
        this.height = height;
        maze = new int[this.width][this.height];
        generateMaze(0, 0);

        try {
            // Replace this URL with the URL or path of your image
            File imageFile = new File("C:\\Users\\soka\\Documents\\school\\Design and Algorithms\\pacbackground.jpg");
            backgroundImage = ImageIO.read(imageFile);
        } catch (IOException e) {
            System.err.println("Error loading background image");
            e.printStackTrace();
        }
        int cellSize = 20; // Change this value to adjust the size of the maze cells
        setPreferredSize(new Dimension(width * cellSize, height * cellSize));
        // Add this block of code to the mazeSolver constructor
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
            int new_xcoord = x_cord + dir.x;
            int new_ycoord = y_cord + dir.y;

            if (isTrue(new_xcoord, new_ycoord) && isNotVisited(new_xcoord, new_ycoord)) {
                // performing bitwise or operation between the value at the position and
                // direction
                maze[x_cord][y_cord] |= dir.cell;
                maze[new_xcoord][new_ycoord] |= dir.opposite.cell;
                generateMaze(new_xcoord, new_ycoord);
            }
        }
    }
    // checks if coordinates are within the maze
    private boolean isTrue(int x, int y) {
        return (x >= 0 && y >= 0 && x < width && y < height);
    }
    // valid to move if there's no wal
    private boolean isNotVisited(int x, int y) {
        int empty = 0;
        return maze[x][y] == empty;
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Maze Generator");
            mazeSolver maze = new mazeSolver(35, 35);
            frame.add(maze);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            maze.playBackgroundMusic();
        });
    }
}
class MazeCells {
    private MazeCells parentCell;
    private final int xCoordinate;
    private final int yCoordinate;
    private boolean visited;

    //Maze cell constructor
    public MazeCells(int x, int y) {
        this.visited = false;
        this.parentCell = null;
        this.xCoordinate = x;
        this.yCoordinate = y;
        boolean n = false;
        boolean s = false;
        boolean e = false;
        boolean w = false;
    }
    public MazeCells(int x, int y, MazeCells parentCell) {
        this.parentCell = parentCell;
        this.xCoordinate = x;
        this.yCoordinate = y;
    }
    public boolean checkForWalls(int x, int y, MazeCells currentCell, int[][] maze) {
        int xCoord = currentCell.getXValue();
        int yCoord = currentCell.getYValue();

        if(x == 0 && y == -1) {
            return (maze[xCoord][yCoord] & 1) == 0;
        }
        if(x == 0 && y == 1) {
            return (maze[xCoord][yCoord] & 2) == 0;
        }
        if(x == 1 && y == 0) {
            return (maze[xCoord][yCoord] & 4) == 0;
        }
        if(x == -1 && y == 0) {
            return (maze[xCoord][yCoord] & 8) == 0;
        }
        return false;
    }
    public void setVisited() {
        this.visited = true;
    }
    public boolean isVisited() {
        return this.visited;
    }
    public MazeCells getParentCell() {
        return this.parentCell;
    }
    public void setParentCell(MazeCells parent) {
        this.parentCell = parent;
    }
    public int getXValue() {
        return this.xCoordinate;
    }
    public int getYValue() {
        return this.yCoordinate;
    }

    @Override//the equals' method to compare two MazeCells based on their x and y coordinates
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MazeCells that = (MazeCells) o;
        return xCoordinate == that.xCoordinate && yCoordinate == that.yCoordinate;
    }
    @Override //the hashCode method to generate a unique hash code for each MazeCell based on its x and y coordinates
    public int hashCode() {
        return Objects.hash(xCoordinate, yCoordinate);
    }
}
