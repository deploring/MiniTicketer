package solar.rpg.ticketer.views.util;

import solar.rpg.ticketer.views.MainView;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;

/**
 * Represents a View that can be displayed in the JFrame for this program.
 * Each view has a JPanel, reference to MainView, and a few abstract functions for maintaining proper state.
 *
 * @author Joshua Skinner
 * @version 1.0
 * @since 0.1
 */
public abstract class View {

    // Reference to MainView
    protected MainView main;

    // All UI elements should be placed on this panel.
    protected JPanel mainPanel;

    protected View(MainView main) {
        this.main = main;
        mainPanel = new JPanel();
        generate();
    }

    // Set some styling attributes for use with JTextPanes.
    protected static final SimpleAttributeSet ATTRIB_CENTER = new SimpleAttributeSet();

    static {
        StyleConstants.setAlignment(ATTRIB_CENTER, StyleConstants.ALIGN_CENTER);
        StyleConstants.setFontSize(ATTRIB_CENTER, 14);
    }

    /**
     * Called upon construction of a View.
     * All UI elements should be instantiated using this method.
     */
    protected abstract void generate();

    /**
     * Can be called for any reason.
     * All UI element states should be reset when using this method.
     */
    public abstract void reset();

    /**
     * Can be called for any reason.
     * All UI element states should be refreshed when using this method.
     */
    public abstract void update();

    /**
     * This method serves to reduce duplicate code in the generate() method.
     * It automatically creates a panel with the specified layout and adds a
     * JTextPane, for which it also applies the styling attributes to.
     *
     * @param layout      The layout for the JPanel.
     * @param borderTitle The border title for the JPanel.
     * @param pane        The text pane to add to the panel.
     * @param addArgs     Any additional arguments that may be supplied when adding the text pane.
     * @return The resulting panel.
     */
    protected JPanel createTextSection(LayoutManager layout, String borderTitle, JTextPane pane, Object... addArgs) {
        // Create panel and set properties.
        JPanel result = new JPanel();
        result.setLayout(layout);
        if (!borderTitle.isEmpty())
            result.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(6, 6, 6, 6),
                    BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY, 1), borderTitle, TitledBorder.CENTER, TitledBorder.TOP)));
        else result.setBorder(new EmptyBorder(6, 6, 6, 6));

        // Create text pane and add it to the panel.
        pane.setEditable(false);
        pane.setParagraphAttributes(ATTRIB_CENTER, true);
        if (addArgs.length == 1)
            result.add(pane, addArgs[0]);
        else result.add(pane);

        return result;
    }

    public JPanel getPanel() {
        return mainPanel;
    }
}
