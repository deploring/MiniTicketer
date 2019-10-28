package solar.rpg.ticketer.views;

import solar.rpg.ticketer.controller.TicketController;

import javax.swing.*;
import java.awt.*;

/**
 * This JFrame controls each individual sub-view on the screen.
 * It also acts as the central class where models, views, and
 * controllers can all communicate with one another.
 *
 * @author Joshua Skinner
 * @author Keagan Foster
 * @version 1.0
 * @since 0.1
 */
public final class MainView extends JFrame {

    private TicketController controller;

    private IView choiceNutritionInfo, chooseMenuItems, customerDetails, orderedItems, orderStatus, buttonPanel;
    private JPanel holderPanel;

    public MainView() {
        super("MiniTicketer - v0.1");

        setLayout(new FlowLayout(FlowLayout.LEADING, 0, 10));
        //createUI();

        // Initialise controller.
        controller = new TicketController(this);
    }
}
