package solar.rpg.ticketer.views;

import solar.rpg.ticketer.controller.StateController;
import solar.rpg.ticketer.controller.DataController;
import solar.rpg.ticketer.views.booking.BookingView;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.Set;

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

    private DataController dataController;
    private StateController stateController;

    private IView bookingView;
    private JPanel holderPanel;

    // Menu bar UI elements.
    private JCheckBoxMenuItem[] sortGenres;

    public MainView() {
        super("MiniTicketer - v0.1");

        // Initialise dataController.
        dataController = new DataController(this);
        stateController = new StateController(this);

        setLayout(new BorderLayout());
        createUI();
    }

    private void createUI() {
        setupMenuBar();
        bookingView = new BookingView(this);
        add(bookingView.getPanel(), BorderLayout.CENTER);
    }

    /**
     * Sets up a basic JMenu bar with some helpful information & tools.
     */
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Create a few helpful menus.
        JMenu help = new JMenu("Help");
        JMenuItem guide = new JMenuItem("Quick Start Guide");
        JMenuItem context = new JMenuItem("Context");
        JMenuItem source = new JMenuItem("Source & Planning");
        guide.addActionListener((e) -> JOptionPane.showMessageDialog(null, "Welcome to Mini-Ticketer! Yes, that's a word.\nHere, you are able to view a wide array of short-films of varying genres.\nAre you interested in perhaps watching one of them? Click on one to book now!\nSpecify the date, time, and number of attendees. Please pick your venue seats.\nOnce you're ready to book your tickets, pick a username to save them under!\nGood luck, and happy watching! - Mini-Ticketer", "Welcome to Mini-Ticketer!", JOptionPane.INFORMATION_MESSAGE));
        context.addActionListener((e) -> JOptionPane.showMessageDialog(null, "This program was developed as part of an employer assessment.\nIt is intended to show off my OOP design skills with a focus on:\na. having a data-centric context; and\nb. maintaining high quality code throughout development.\nThank you!", "About This Program", JOptionPane.INFORMATION_MESSAGE));
        source.addActionListener((e) -> JOptionPane.showMessageDialog(null, "This project's code, along with all design documents are available at:\nhttps://github.com/lavuh/MiniTicketer - Please do not hesitate to ask questions!", "Source Code & Design", JOptionPane.INFORMATION_MESSAGE));
        help.add(guide);
        help.add(context);
        help.add(source);

        // Create a menu that allows the user to select what genres to see in the grid.
        JMenu sort = new JMenu("Sort Movies");
        // Only allow the user to select genres that have some screenings on them.
        Set<Map.Entry<String, Integer>> genres = dataController.getCurrentGenreSet();
        sortGenres = new JCheckBoxMenuItem[genres.size()];
        for (Map.Entry<String, Integer> entry : genres) {
            JCheckBoxMenuItem genreItem = new JCheckBoxMenuItem(entry.getKey());
            sortGenres[entry.getValue()] = genreItem;
            genreItem.addActionListener((e) -> {
                // Only allow one item to be selected at a time; so uncheck others if applicable.
                if (genreItem.isSelected()) {
                    // This genre is now the sorting target.
                    clearSortedGenres();
                    genreItem.setState(true);
                    state().changeSortBy(entry.getKey());
                } else state().changeSortBy(""); // Sorting has been disabled.
            });
            sort.add(genreItem);
        }
        menuBar.add(help);
        menuBar.add(sort);
        setJMenuBar(menuBar);
    }

    private void clearSortedGenres() {
        for (JCheckBoxMenuItem item : sortGenres)
            item.setState(false);
    }

    public DataController data() {
        return dataController;
    }

    public StateController state() {
        return stateController;
    }

    public BookingView booking() {
        return (BookingView) bookingView;
    }
}
