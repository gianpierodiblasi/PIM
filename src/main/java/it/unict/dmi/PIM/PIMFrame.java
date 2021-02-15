package it.unict.dmi.PIM;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class PIMFrame extends JFrame {

  private static final long serialVersionUID = 1L;
  private PIMPanel pimPanel = new PIMPanel();

  @SuppressWarnings("CallToPrintStackTrace")
  public PIMFrame() {
    try {
      jbInit();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    this.setTitle("Puzzle Image Mosaic Creator");
    this.setSize(new Dimension(800, 700));
    this.getContentPane().add(pimPanel, BorderLayout.CENTER);
  }

  @SuppressWarnings("UnusedAssignment")
  private static BufferedImage loadImage(String str) throws Exception {
    BufferedImage im = ImageIO.read(new File(str));
    int w = im.getWidth();
    int h = im.getHeight();

    if (PIMPanel.MAC_OS_X) {

      int[] data = new int[w * h];
      im.getRGB(0, 0, w, h, data, 0, w);
      im = null;
      System.gc();
      im = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
      im.setRGB(0, 0, w, h, data, 0, w);
    }

    return im;
  }

  //MAIN
  @SuppressWarnings("UseOfSystemOutOrSystemErr")
  public static void main(String[] a) {
    switch (a.length) {
      case 0:
        try {
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
      } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
      }
      PIMFrame f = new PIMFrame();
      f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      f.setVisible(true);
      f.setExtendedState(JFrame.MAXIMIZED_BOTH);
      break;
      case 10:
        PIMAlgorithm pa = new PIMAlgorithm();
        JProgressBar bar = new JProgressBar() {
          @Override
          public void setString(String str) {
            System.out.println(str);
          }

          @Override
          public void setValue(int v) {
            System.out.println("" + v);
          }
        };
        JTextArea csv = new JTextArea();
        pa.setProgressBar(bar);
        pa.setTextAreas(null, csv);
        try {
          pa.setImage(loadImage(a[0]));
          if (!a[1].equalsIgnoreCase("null")) {
            pa.setEdge(loadImage(a[1]));
          }
          boolean mask = !a[5].equalsIgnoreCase("null");
          File file = new File(a[6]);
          int side = Integer.parseInt(a[7]);
          boolean colorize = a[8].equalsIgnoreCase("true");
          boolean uniform = a[9].equalsIgnoreCase("true");

          pa.evaluate(side, true, colorize, mask, uniform, file);

          if (!a[2].equalsIgnoreCase("null")) {
            ImageIO.write(pa.getPIM(), "PNG", new File(a[2]));
          }
          if (!a[3].equalsIgnoreCase("null")) {
            try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(a[3]))) {
              csv.write(out);
              out.flush();
            }
          }
          if (!a[4].equalsIgnoreCase("null")) {
            ImageIO.write(pa.getEdge(), "PNG", new File(a[4]));
          }
          if (mask) {
            ImageIO.write(pa.getMask(), "PNG", new File(a[5]));
          }
        } catch (Exception ex) {
          bar.setString("ERROR: " + ex.toString());
        }
        break;
      default:
        System.out.println("Usage: java -Xmx512M -jar PIM.jar [OpenImageFile] [OpenEdgeFile] [SavePIMFile] [SaveCVSFile] [SaveEdgeFile] [SaveMaskFile] [database] [side] [colorize] [uniformDistribution]");
        System.out.println("\tOpenImageFile: The input image (supported formats PNG, JPG, GIF)");
        System.out.println("\tOpenEdgeFile: The Black/White edge image (it can be \"null\" (supported formats PNG, JPG, GIF)");
        System.out.println("\tSavePIMFile: The PIM image (it can be \"null\" (supported format PNG)");
        System.out.println("\tSaveCVSFile: The CVS image (it can be \"null\"");
        System.out.println("\tSaveEdgeFile: The Black/White edge image (it can be \"null\" (supported format PNG)");
        System.out.println("\tSaveMaskFile: The Black/White edge image (it can be \"null\" (supported format PNG)");
        System.out.println("\tdatabase: the PIM database");
        System.out.println("\tside: The tile side");
        System.out.println("\tcolorize: true to colorize the image, false otherwise");
        System.out.println("\tuniformDistribution: true to use an uniform distribution of the objects");
        break;
    }
  }
  //END MAIN
}
