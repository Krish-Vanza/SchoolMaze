package Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class SchoolMaze extends JFrame implements ActionListener {

    int X;
    int Y;
    MazeMap mazeMap;
    JPanel mazePanel;
    JTextArea storyText;
    JButton[][] mazeCells;
    Student student;
    JLabel label;

    private int Minute = 0;

    public SchoolMaze() {
        setTitle("School Journey");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        showGameInstructions();

        setVisible(true);

        mazeMap = new MazeMap(10, 10);
        student = new Student();

        // Story text area
        storyText = new JTextArea("Your journey to school begins! Navigate through the maze carefully.");
        storyText.setEditable(false);
        storyText.setFont(new Font("Arial", Font.PLAIN, 16));
        add(storyText, BorderLayout.NORTH);

        mazePanel = new JPanel(new GridLayout(10, 10));
        mazeCells = new JButton[10][10];

        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                mazeCells[y][x] = new JButton();

                mazeCells[y][x].putClientProperty("x", x);
                mazeCells[y][x].putClientProperty("y", y);

                mazeCells[y][x].addActionListener(this);

                mazePanel.add(mazeCells[y][x]);
            }
        }

        add(mazePanel, BorderLayout.CENTER);

        // Status panel
        JPanel statusPanel = new JPanel();
        label = new JLabel("Energy: 100%   ||   Time: 7:00 AM");
        statusPanel.add(label);
        add(statusPanel, BorderLayout.SOUTH);

        updateMaze();
    }

    private void showGameInstructions() {
        String s = """
        Welcome to Maze School: Journey to Learning!

        Objective:
        - Your goal is to navigate through life's challenges with empathy, kindness, and resilience.
        - You start from your home, representing your personal starting point of growth.
        - Reach the school of wisdom before the day of learning begins, symbolizing your commitment to personal development.
        
        Button Colors:
        - GREEN: Your current position.
        - RED: Obstacles you cannot cross.
        - YELLOW: Special event spots that may boost or reduce your energy.
        - Bottom Right :

        Energy:
        - Moving costs 5% energy. If your energy reaches 0%, you cannot move further.
        - Special events can either increase or decrease your energy.

        Embark on your quest of personal growth and wisdom!
        
        """;

        JOptionPane.showMessageDialog(this, s, "Game Instructions", JOptionPane.INFORMATION_MESSAGE);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        // Parse the button's action command to get its coordinates
        JButton sourceButton = (JButton) e.getSource();
        X = (int) sourceButton.getClientProperty("x");
        Y = (int) sourceButton.getClientProperty("y");

        if (student.energy <= 0) {
            JOptionPane.showMessageDialog(this, "You're too exhausted to move! You failed to reach school.",
                    "Game Over", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        if (Minute == 30) {
            JOptionPane.showMessageDialog(this, "Time's up! You failed to reach school by 7:30 AM.",
                    "Game Over", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        if (mazeMap.isValidMove(student.x, student.y, X, Y)) {
            // Move player
            student.moveTo(X, Y);

            // Increment time
            Minute++;

            // Handle special events
            handleEvent(X, Y);

            updateMaze();

            // Check if player reached the school
            if (X==9 && Y==9) {
                JOptionPane.showMessageDialog(this,
                        "Congratulations! You made it to school on time!\n" +
                                "Your journey was full of exciting challenges!",
                        "Mission Accomplished",
                        JOptionPane.INFORMATION_MESSAGE);
                System.exit(0);
            }
        } else {
            storyText.setText("Oops! You can't move there. Choose a valid path.");
        }
    }

    private void updateMaze() {
        for (int y = 0; y <10; y++) {
            for (int x = 0; x < 10; x++) {
                JButton cell = mazeCells[y][x];

                // Reset cell
                cell.setText("");
                cell.setBackground(null);

                // current position
                if (student.x == x && student.y == y) {
                    cell.setBackground(Color.GREEN);
                    cell.setText("YOU");
                }

                //obstacles
                if (mazeMap.isObstacle(x, y)) {
                    cell.setBackground(Color.RED);
                    cell.setText("X");
                }

                // events
                if (mazeMap.hasSpecialEvent(x, y)) {
                    cell.setBackground(Color.YELLOW);
                    cell.setText("!!");
                }

                // home location
                if (x == 0 && y == 0) {
                    cell.setText("HOME");
                }

                // school location
                if (x==9 && y==9) {
                    cell.setText("School");
                }
            }
        }

        // Update energy and time

        label.setText("Energy: " + student.energy + "%       ||     Time: 7 : " + Minute);
    }



    private void handleEvent(int x, int y) {
        if (mazeMap.hasSpecialEvent(x, y)) {
            // a random event
            String[] events = {
                    "You found a shortcut! Energy +15%",
                    "Oh no! A small obstacle slows you down. Energy -10%",
                    "You discovered an energy boost! Energy +20%",
                    "A friendly neighbor offers you a ride part of the way. Energy +10%"
            };

            Random random = new Random();
            int k=random.nextInt(4);
            storyText.setText(events[k]);

            int energyChange = 0;
            if (k==0) energyChange = 15;
            else if (k==1) energyChange = -10;
            else if (k==2) energyChange = 20;
            else if (k==3) energyChange = 10;

            student.adjustEnergy(energyChange);

            // Remove the special event from the maze map
            mazeMap.removeSpecialEvent(x, y);
        }
    }

    public static void main(String[] args) {
        new SchoolMaze();
    }
}


class MazeMap {
    int width;
    int height;
    boolean[][] obstacles;
    boolean[][] specialEvents;

    public MazeMap(int width, int height) {
        this.width = width;
        this.height = height;

        // Initialize maze components
        obstacles = new boolean[height][width];
        specialEvents = new boolean[height][width];

        // Generate maze
        Random random = new Random();

        // Generate random obstacles
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                obstacles[y][x] = random.nextDouble() < 0.2;
                if(!obstacles[y][x]){
                    specialEvents[y][x] = random.nextDouble() < 0.1;
                }
            }
        }

        // Ensure start and end are clear
        obstacles[0][0] = false;
        obstacles[height-1][width-1] = false;
    }


    public boolean isValidMove(int fromX, int fromY, int toX, int toY) {
        int dx = Math.abs(fromX - toX);
        int dy = Math.abs(fromY - toY);
        return (dx <= 1 && dy <= 1) && (dx + dy > 0) && !obstacles[toY][toX];
    }

    public boolean isObstacle(int x, int y) {
        return obstacles[y][x];
    }

    public boolean hasSpecialEvent(int x, int y) {
        return specialEvents[y][x];
    }

    public void removeSpecialEvent(int x, int y) {
        specialEvents[y][x] = false;
    }
}

class Student {
    int x;
    int y;
    int energy;

    public Student() {
        x = 0;
        y = 0;
        energy = 100;
    }

    public void moveTo(int newX, int newY) {
        x = newX;
        y = newY;
        energy -= 5; // Moving costs energy
    }

    public void adjustEnergy(int amount) {
        if(energy + amount>=100){
            energy=100;
        } else {
            energy += amount;
        }
    }

}
