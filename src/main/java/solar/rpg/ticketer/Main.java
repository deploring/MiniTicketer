package solar.rpg.ticketer;

import solar.rpg.ticketer.views.MainView;

import javax.swing.*;
import java.awt.*;

/**
 * This Main class initialises the JFrame, MainView.
 */
public class Main {

    public static void main(String[] args) {
        // Use a cross-platform look-and-feel so Mac doesn't look cross.
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        MainView displayFrame = new MainView();
        // Set JFrame attributes
        displayFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        displayFrame.setSize(740, 680);
        displayFrame.setMinimumSize(new Dimension(740, 680));
        displayFrame.setResizable(true);
        displayFrame.setVisible(true);
        // Set the frame to display in the center of the screen.
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        displayFrame.setLocation(dim.width / 2 - displayFrame.getSize().width / 2, dim.height / 2 - displayFrame.getSize().height / 2);
    }
}
