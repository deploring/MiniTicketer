package solar.rpg.ticketer.views;

import javax.swing.*;

/**
 * Views implementing IView must return a Panel when asked.
 */
public interface IView {

    void generate();

    void reset();

    JPanel getPanel();
}
