package it.unict.dmi.ramses;

import java.awt.BorderLayout;
import java.awt.Container;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class RamsesFrame extends JFrame {

  private static final long serialVersionUID = 1L;

  private final RamsesPanel ramses = new RamsesPanel();

  public RamsesFrame() {
    setTitle("Ramses: un sistema steganografico basato su Puzzle Image Mosaic");
    //setResizable(false);

    Container contentPane = getContentPane();

    contentPane.add(ramses, BorderLayout.CENTER);
  }

  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
    }

    RamsesFrame frame = new RamsesFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
  }
}
