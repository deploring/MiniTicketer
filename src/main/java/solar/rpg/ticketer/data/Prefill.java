package solar.rpg.ticketer.data;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * This class automatically prefills all tables when the schema is first created.
 * It enables more powerful testing, and of course, makes things much much easier.
 * I do not recommend scrolling down. :)
 *
 * @author Joshua Skinner
 * @version 1.0
 * @since 0.1
 */
final class Prefill {

    /**
     * Fills all the relevant tables with necessary testing data.
     *
     * @param db The database object.
     */
    static void prefill(Database db) {
        // Show a dialog box async so it doesn't hold up the prefill.
        new Thread(() -> JOptionPane.showMessageDialog(null, "Hey there! We're just pre-loading some test data into your database.\n" +
                "This only needs to be done once and won't take very long.\nThank you for your patience and understanding!", "Uploading Data...", JOptionPane.INFORMATION_MESSAGE)).start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(Prefill.class.getResourceAsStream("/prefill")));
        reader.lines().forEach((line) -> {
            // Show progress blips to let people know the program isn't dead.
            if (line.startsWith("~")) {
                System.out.println(line.substring(1));
                return;
            }
            PreparedStatement insert = db.prepare(line);
            try {
                insert.executeUpdate();
                insert.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
