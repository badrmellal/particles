package particleUniverse;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Particle Universe");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            ParticleUniverse universe = new ParticleUniverse();
            frame.add(universe);

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            universe.requestFocusInWindow();
        });
    }
}
