package solar.rpg.ticketer.views.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * A special extension of JPanel that gives a margin to a JButton.
 * This is done by wrapping the button in a JPanel and setting an empty border.
 *
 * @author Joshua Skinner
 * @version 1.0
 * @since 0.1
 */
public class SpacedJButton extends JPanel {

    private JButton button;

    public SpacedJButton(String text, int top, int left, int bottom, int right) {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(top, left, bottom, right));
        button = new JButton(text);
        add(button, BorderLayout.CENTER);
    }

    /**
     * @return The JButton.
     */
    public JButton get() {
        return button;
    }
}
