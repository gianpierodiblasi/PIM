package it.unict.dmi.PIM;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;

public class PIMPanel extends JPanel {

  private static final long serialVersionUID = 1L;
  private JButton open = new JButton();
  private JMenuItem openImage = new JMenuItem();
  private JMenuItem openDB = new JMenuItem();
  private JMenuItem openEdge = new JMenuItem();
  private JButton create = new JButton();
  private JButton save = new JButton();
  private JMenuItem saveImage = new JMenuItem();
  private JMenuItem saveEdge = new JMenuItem();
  private JMenuItem saveDB = new JMenuItem();
  private JMenuItem saveCSV = new JMenuItem();
  private JScrollPane jScrollPane2 = new JScrollPane();
  private JLabel image = new JLabel();
  private JLabel pim = new JLabel();
  private JFileChooser openChooser = new JFileChooser();
  private JFileChooser saveChooser = new JFileChooser();
  private JFileChooser openChooserDB = new JFileChooser();
  private JFileChooser saveChooserDB = new JFileChooser();
  private JFileChooser saveChooserCSV = new JFileChooser();
  private JProgressBar bar = new JProgressBar();
  private JLabel edge = new JLabel();
  private JPanel jPanel4 = new JPanel();
  private BorderLayout borderLayout3 = new BorderLayout();
  private JDialog dialog = new JDialog();
  private JScrollPane jScrollPane1 = new JScrollPane();
  private JLabel preview = new JLabel();
  private JButton removeEdge = new JButton();
  private JPanel jPanel2 = new JPanel();
  private GridLayout gridLayout1 = new GridLayout();
  private JSplitPane jSplitPane1 = new JSplitPane();
  private JScrollPane jScrollPane3 = new JScrollPane();
  private JTextArea messages = new JTextArea();
  private JPanel jPanel11 = new JPanel();
  private JPanel jPanel12 = new JPanel();
  private BorderLayout borderLayout5 = new BorderLayout();
  private GridLayout gridLayout8 = new GridLayout();
  private BorderLayout borderLayout1 = new BorderLayout();
  private SettingPanel settingPanel = new SettingPanel();
  private JPopupMenu menuOpen = new JPopupMenu();
  private JPopupMenu menuSave = new JPopupMenu();
  private JSplitPane jSplitPane2 = new JSplitPane();
  private JScrollPane jScrollPane4 = new JScrollPane();
  private JTextArea csv = new JTextArea();
  private JMenuItem saveMask = new JMenuItem();

  private PIMAlgorithm pa = new PIMAlgorithm();
  private int dim = 120;
  private File file;
  private boolean doAntipole = true;
  public final static boolean MAC_OS_X = System.getProperty("os.name").toLowerCase().startsWith("mac os x");

  @SuppressWarnings("CallToPrintStackTrace")
  public PIMPanel() {
    try {
      jbInit();
    } catch (Exception e) {
      e.printStackTrace();
    }
    pa.setProgressBar(bar);
    pa.setTextAreas(messages, csv);
  }

  private ImageIcon createIcon(BufferedImage im) {
    BufferedImage icon = new BufferedImage(dim, dim, BufferedImage.TYPE_INT_ARGB);
    int w = im.getWidth();
    int h = im.getHeight();
    double scale = Math.min((double) dim / w, (double) dim / h);
    Graphics2D g2 = icon.createGraphics();
    g2.scale(scale, scale);
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    g2.drawImage(im, 0, 0, null);
    g2.dispose();
    return new ImageIcon(icon);
  }

  @SuppressWarnings("UnusedAssignment")
  private void open(boolean isImage) {
    openChooser.setSelectedFile(null);
    if (openChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      try {
        FileImageInputStream fImm = new FileImageInputStream(openChooser.getSelectedFile());
        Iterator<ImageReader> iter = ImageIO.getImageReaders(fImm);
        if (iter.hasNext()) {
          ImageReader reader = iter.next();
          reader.setInput(fImm);
          int w = reader.getWidth(0);
          int h = reader.getHeight(0);
          if (w * h <= 1960000) {
            BufferedImage im = reader.read(0);
            if (PIMPanel.MAC_OS_X) {
              int[] data = new int[w * h];
              im.getRGB(0, 0, w, h, data, 0, w);
              im = null;
              System.gc();
              im = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
              im.setRGB(0, 0, w, h, data, 0, w);
            }

            if (isImage) {
              pim.setIcon(new ImageIcon(im));
              pa.setImage(im);
              image.setIcon(this.createIcon(im));
              image.setCursor(new Cursor(Cursor.HAND_CURSOR));
              image.setToolTipText("Click to enlarge");
              if (file != null) {
                create.setEnabled(true);
              }
            } else {
              pa.setEdge(im);
              edge.setIcon(this.createIcon(im));
              edge.setCursor(new Cursor(Cursor.HAND_CURSOR));
              edge.setToolTipText("Click to enlarge");
            }
          } else {
            JOptionPane.showMessageDialog(this, "It is not possible to open the file\nThe image size is greater than 1960000 pixel", "Error", JOptionPane.ERROR_MESSAGE);
          }
          reader.setInput(null);
        }
      } catch (HeadlessException | IOException ex) {
        JOptionPane.showMessageDialog(this, "It is not possible to open the file", "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void openDB() {
    if (openChooserDB.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      doAntipole = true;
      file = openChooserDB.getSelectedFile();
      if (pa.getImage() != null) {
        create.setEnabled(true);
      }
    }
  }

  private void save(BufferedImage image) {
    File f = this.checkFile(saveChooser);
    if (f != null)
      try {
      ImageIO.write(image, saveChooser.getFileFilter().toString(), f);
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(this, "It's not possible to save the file", "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void saveDB() {
    File f = this.checkFile(saveChooserDB);
    if (f != null) {
      try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f))) {
        out.writeObject(pa.getAntipoleTree());
        out.flush();
      } catch (IOException e) {
      }
    }
  }

  private void saveCSV() {
    File f = this.checkFile(saveChooserCSV);
    if (f != null) {
      try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(f))) {
        csv.write(out);
        out.flush();
      } catch (IOException e) {
      }
    }
  }

  private File checkFile(JFileChooser chooser) {
    chooser.setSelectedFile(null);
    int res2 = JOptionPane.NO_OPTION;
    File f = null;
    while (res2 == JOptionPane.NO_OPTION) {
      int res = chooser.showSaveDialog(this);
      if (res == JFileChooser.APPROVE_OPTION) {
        f = chooser.getSelectedFile();
        if (f.exists()) {
          res2 = JOptionPane.showConfirmDialog(this, "The file already exists, overwrite?", "Save", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        } else {
          res2 = JOptionPane.YES_OPTION;
        }
      } else {
        res2 = JOptionPane.CANCEL_OPTION;
      }
    }
    return res2 == JOptionPane.YES_OPTION ? f : null;
  }

  private void setComponentsEnabled(boolean b) {
    open.setEnabled(b);
    openDB.setEnabled(b);
    openEdge.setEnabled(b);
    removeEdge.setEnabled(b);
    create.setEnabled(b);
    save.setEnabled(b);
    settingPanel.setComponentsEnabled(b);
  }

  private void jbInit() throws Exception {
    openChooser.setAcceptAllFileFilterUsed(false);
    openChooser.setFileFilter(new OpenImageFilter());
    openChooser.setCurrentDirectory(new File("/Users/giampo76/Images"));
    saveChooser.setAcceptAllFileFilterUsed(false);
    saveChooser.setFileFilter(new SaveImageFilter());
    saveChooser.setCurrentDirectory(new File("/Users/giampo76/Temporanea/JIM"));
    openChooserDB.setAcceptAllFileFilterUsed(false);
    openChooserDB.setFileFilter(new OpenSaveDBFilter());
    openChooserDB.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    openChooserDB.setCurrentDirectory(new File("/Users/giampo76/Temporanea/JIM"));
    saveChooserDB.setCurrentDirectory(new File("/Users/giampo76/Temporanea/JIM"));
    saveChooserDB.setAcceptAllFileFilterUsed(false);
    saveChooserDB.setFileFilter(new OpenSaveDBFilter());
    saveChooserCSV.setCurrentDirectory(new File("/Users/giampo76/Temporanea/JIM"));
    saveChooserCSV.setAcceptAllFileFilterUsed(false);
    saveChooserCSV.setFileFilter(new SaveCSVFilter());
    open.setFocusPainted(false);
    open.setMargin(new Insets(0, 0, 0, 0));
    open.setToolTipText("Open");
    open.setIcon(new ImageIcon(ImageIO.read(PIMPanel.class.getClassLoader().getResourceAsStream("open.gif"))));
    create.setEnabled(false);
    create.setFocusPainted(false);
    create.setMargin(new Insets(0, 0, 0, 0));
    create.setToolTipText("Create");
    create.setIcon(new ImageIcon(ImageIO.read(PIMPanel.class.getClassLoader().getResourceAsStream("start.gif"))));
    save.setEnabled(false);
    save.setFocusPainted(false);
    save.setMargin(new Insets(0, 0, 0, 0));
    save.setToolTipText("Save");
    save.setIcon(new ImageIcon(ImageIO.read(PIMPanel.class.getClassLoader().getResourceAsStream("save.gif"))));
    openImage.setText("Open Image");
    openDB.setText("Open Database");
    openEdge.setText("Open Directional Guidelines");
    saveImage.setText("Save Puzzle Image Mosaic");
    saveDB.setText("Save Database");
    saveEdge.setText("Save Directional Guidelines");
    saveCSV.setText("Save CSV");
    saveMask.setText("Save Mask");
    menuOpen.add(openImage);
    menuOpen.add(openDB);
    menuOpen.add(openEdge);
    menuSave.add(saveImage);
    menuSave.add(saveDB);
    menuSave.add(saveEdge);
    menuSave.add(saveCSV);
    menuSave.add(saveMask);
    image.setBorder(BorderFactory.createEtchedBorder());
    image.setPreferredSize(new Dimension(dim, dim));
    edge.setBorder(BorderFactory.createEtchedBorder());
    edge.setPreferredSize(new Dimension(dim, dim));
    jPanel4.setLayout(borderLayout3);
    dialog.setSize(500, 500);
    dialog.setTitle("Puzzle Image Mosaic Creator");
    removeEdge.setFocusPainted(false);
    removeEdge.setEnabled(false);
    removeEdge.setMargin(new Insets(0, 0, 0, 0));
    removeEdge.setToolTipText("Remove Directional Guidelines");
    removeEdge.setIcon(new ImageIcon(ImageIO.read(PIMPanel.class.getClassLoader().getResourceAsStream("removeEdge.gif"))));
    jPanel2.setLayout(gridLayout1);
    gridLayout1.setRows(2);
    messages.setFont(new java.awt.Font("Monospaced", 0, 12));
    messages.setEditable(false);
    jPanel11.setLayout(borderLayout5);
    jPanel12.setLayout(gridLayout8);
    this.setLayout(borderLayout1);
    bar.setString("");
    bar.setStringPainted(true);
    jPanel12.add(open, null);
    jPanel12.add(removeEdge, null);
    jPanel12.add(create, null);
    jPanel12.add(save, null);
    jPanel2.add(image, null);
    jPanel2.add(edge, null);
    jPanel4.add(jPanel2, BorderLayout.SOUTH);
    jPanel4.add(settingPanel, BorderLayout.NORTH);
    this.add(jPanel4, BorderLayout.WEST);
    this.add(jSplitPane1, BorderLayout.CENTER);
    jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
    jSplitPane1.setDividerLocation(360);
    jSplitPane1.setOneTouchExpandable(true);
    jSplitPane1.add(jScrollPane3, JSplitPane.TOP);
    jScrollPane3.getViewport().add(pim);
    jSplitPane1.add(jSplitPane2, JSplitPane.BOTTOM);
    this.add(jPanel11, BorderLayout.NORTH);
    jPanel11.add(jPanel12, BorderLayout.WEST);
    jPanel11.add(bar, BorderLayout.CENTER);
    dialog.getContentPane().add(jScrollPane1, BorderLayout.CENTER);
    jScrollPane1.getViewport().add(preview, null);
    jSplitPane2.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
    jSplitPane2.setDividerLocation(360);
    jSplitPane2.setOneTouchExpandable(true);
    jSplitPane2.add(jScrollPane2, JSplitPane.LEFT);
    jSplitPane2.add(jScrollPane4, JSplitPane.RIGHT);
    jScrollPane4.getViewport().add(csv);
    jScrollPane2.getViewport().add(messages);
    Listener listener = new Listener();
    open.addActionListener(listener);
    openImage.addActionListener(listener);
    openDB.addActionListener(listener);
    openEdge.addActionListener(listener);
    create.addActionListener(listener);
    save.addActionListener(listener);
    saveImage.addActionListener(listener);
    saveEdge.addActionListener(listener);
    saveDB.addActionListener(listener);
    saveCSV.addActionListener(listener);
    saveMask.addActionListener(listener);
    image.addMouseListener(listener);
    edge.addMouseListener(listener);
    removeEdge.addActionListener(listener);
  }

  private class Listener extends MouseAdapter implements ActionListener {

    @Override
    public void mousePressed(MouseEvent e) {
      Object source = e.getSource();
      Image im = null;
      if (source == image) {
        im = pa.getImage();
      } else if (source == edge) {
        im = pa.getEdge();
      }
      if (im != null) {
        preview.setIcon(new ImageIcon(im));
        dialog.setVisible(true);
      }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      Object source = e.getSource();
      if (source == openImage) {
        open(true);
      } else if (source == openEdge) {
        open(false);
      } else if (source == openDB) {
        openDB();
      } else if (source == create) {
        (new Thread() {
          @Override
          public void run() {
            setComponentsEnabled(false);
            boolean mask = settingPanel.isCreateAlphaMaskImage();
            saveMask.setEnabled(mask);
            pa.evaluate(settingPanel.getSide(), doAntipole, settingPanel.isColorize(), mask, settingPanel.isUniformDistribution(), file);
            pim.setIcon(new ImageIcon(pa.getPIM()));
            setComponentsEnabled(true);
            edge.setIcon(createIcon(pa.getEdge()));
            edge.setCursor(new Cursor(Cursor.HAND_CURSOR));
            edge.setToolTipText("Click to enlarge");
            doAntipole = false;
          }
        }).start();
      } else if (source == open) {
        menuOpen.show(open, 0, open.getHeight());
      } else if (source == save) {
        menuSave.show(save, 0, save.getHeight());
      } else if (source == saveImage) {
        save(pa.getPIM());
      } else if (source == saveDB) {
        saveDB();
      } else if (source == saveCSV) {
        saveCSV();
      } else if (source == saveEdge) {
        save(pa.getEdge());
      } else if (source == saveMask) {
        save(pa.getMask());
      } else if (source == removeEdge) {
        pa.setEdge(null);
        edge.setIcon(null);
        edge.setCursor(null);
        edge.setToolTipText(null);
      }
    }
  }

  private class OpenImageFilter extends FileFilter {

    @Override
    public boolean accept(File f) {
      String str = f.getName().toLowerCase();
      return str.endsWith(".gif") || str.endsWith(".jpg") || str.endsWith(".jpeg")
              || str.endsWith(".png") || f.isDirectory();
    }

    @Override
    public String getDescription() {
      return "Image File";
    }
  }

  private class OpenSaveDBFilter extends FileFilter {

    @Override
    public boolean accept(File f) {
      String str = f.getName().toLowerCase();
      return str.endsWith(".pim") || f.isDirectory();
    }

    @Override
    public String getDescription() {
      return "Puzzle Image Mosaic DB";
    }
  }

  private class SaveImageFilter extends FileFilter {

    @Override
    public boolean accept(File f) {
      String str = f.getName().toLowerCase();
      return str.endsWith(".png") || f.isDirectory();
    }

    @Override
    public String getDescription() {
      return "PNG";
    }

    @Override
    public String toString() {
      return "png";
    }
  }

  private class SaveCSVFilter extends FileFilter {

    @Override
    public boolean accept(File f) {
      String str = f.getName().toLowerCase();
      return str.endsWith(".csv") || f.isDirectory();
    }

    @Override
    public String getDescription() {
      return "CSV";
    }

    @Override
    public String toString() {
      return "csv";
    }
  }
}
