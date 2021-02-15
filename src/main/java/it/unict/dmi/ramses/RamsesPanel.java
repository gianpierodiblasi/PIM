package it.unict.dmi.ramses;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

public class RamsesPanel extends JPanel {

  private static final long serialVersionUID = 1L;
  private final JButton open = new JButton();
  private final JButton save = new JButton();
  private final JButton start = new JButton();
  private final JButton msg = new JButton();
  private final JButton delete = new JButton();

  private final JPopupMenu menuOpen = new JPopupMenu();
  private final JPopupMenu menuSave = new JPopupMenu();

  private final JMenuItem openImage = new JMenuItem();
  private final JMenuItem openDB = new JMenuItem();

  private final JMenuItem saveMSG = new JMenuItem();
  private final JMenuItem savePIM = new JMenuItem();
  private final JMenuItem saveRAMSES = new JMenuItem();
  private final JMenuItem saveDB = new JMenuItem();
  private final JMenuItem saveEdge = new JMenuItem();
  private final JMenuItem saveVoronoi = new JMenuItem();
  private final JMenuItem saveVoronoiAndDiltedEdge = new JMenuItem();
  private final JMenuItem saveMaskKey = new JMenuItem();
  private final JMenuItem saveMaskMSG = new JMenuItem();
  private final JMenuItem saveCSV = new JMenuItem();
  private final JMenuItem saveAlphaMask = new JMenuItem();

  private final JProgressBar bar = new JProgressBar();

  private final BorderLayout borderLayoutRamsesPanel = new BorderLayout();

  private final JPanel panelPulsanti = new JPanel();
  private final GridLayout gridLayoutPulsanti = new GridLayout();

  private final JPanel panel_1 = new JPanel();
  private final BorderLayout borderLayout_1 = new BorderLayout();

  private final SettingPanelRamses settingPanel = new SettingPanelRamses();

  private final JPanel panel_2 = new JPanel();
  private final BorderLayout borderLayout_2 = new BorderLayout();

  private final JPanel pannelloMessaggio = new JPanel();//contiene il pannello con le immagini del messaggio
  private final Border bordoPannello;
  private final TitledBorder titoloBordoPannelloMessaggio;
  private final JPanel pannelloImmaginiMessaggio = new JPanel();//contiene le immagini del messaggio
  private int altezzaPannelloImmaginiMessaggio = 0;

  private final JScrollPane jScrollPaneResult = new JScrollPane();
  private final JScrollPane jScrollPaneCSV = new JScrollPane();

  private final JSplitPane jSplitPannelloImmagini = new JSplitPane();
  private final JSplitPane jSplitPannelloOutput = new JSplitPane();

  private final JPanel PannelloImmagini = new JPanel(); //pannello contenente gli 8 pannelli con le immagini di output
  private final GridLayout gridLayoutPannelloImmagini = new GridLayout(2, 4, 5, 5);

  private final BorderLayout borderLayoutInput = new BorderLayout();
  private final BorderLayout borderLayoutEdge = new BorderLayout();
  private final BorderLayout borderLayoutVoronoi = new BorderLayout();
  private final BorderLayout borderLayoutEdgeVoronoi = new BorderLayout();
  private final BorderLayout borderLayoutPIM = new BorderLayout();
  private final BorderLayout borderLayoutRAMSES = new BorderLayout();
  private final BorderLayout borderLayoutMaskKey = new BorderLayout();
  private final BorderLayout borderLayoutMaskMsg = new BorderLayout();

  private final JPanel ImmagineInput = new JPanel();
  private final JPanel ImmagineEdge = new JPanel();
  private final JPanel ImmagineVoronoi = new JPanel();
  private final JPanel ImmagineVoronoiDilatedEdge = new JPanel();
  private final JPanel ImmaginePIM = new JPanel();
  private final JPanel ImmagineRAMSES = new JPanel();
  private final JPanel ImmagineMaskKey = new JPanel();
  private final JPanel ImmagineMaskMsg = new JPanel();

  private final JScrollPane jSPImmagineInput = new JScrollPane();
  private final JScrollPane jSPImmagineEdge = new JScrollPane();
  private final JScrollPane jSPImmagineVoronoi = new JScrollPane();
  private final JScrollPane jSPImmagineVoronoiDilatedEdge = new JScrollPane();
  private final JScrollPane jSPImmaginePIM = new JScrollPane();
  private final JScrollPane jSPImmagineRAMSES = new JScrollPane();
  private final JScrollPane jSPImmagineMaskKey = new JScrollPane();
  private final JScrollPane jSPImmagineMaskMsg = new JScrollPane();

  private final JLabel input = new JLabel();
  private final JLabel edge = new JLabel();
  private final JLabel voronoi = new JLabel();
  private final JLabel voronoiAndDilatedEdge = new JLabel();
  private final JLabel pim = new JLabel();
  private final JLabel ramses = new JLabel();
  private final JLabel maskKey = new JLabel();
  private final JLabel maskMsg = new JLabel();

  private final JFileChooser openChooserImage = new JFileChooser(); //finestra di dialogo per l'apertura delle immagini di input
  private final JFileChooser saveChooserImage = new JFileChooser(); //finestra di dialogo per il salvataggio delle immagini di output
  private final JFileChooser openChooserDB = new JFileChooser(); //finestra di dialogo per l'apertura del database di immagini
  private final JFileChooser saveChooserDB = new JFileChooser(); //finestra di dialogo per il savataggio del database di immagini
  private final JFileChooser saveChooserCSV = new JFileChooser(); //finestra di dialogo per il salvataggio dei risultati dell'elaborazione
  private final JFileChooser openChooserMessages = new JFileChooser(); //finestra di dialogo per l'apertura delle immagini del messaggio

  private final JTextArea messages = new JTextArea();
  private final JTextArea csv = new JTextArea();

  private final RamsesAlgorithm RAMSES = new RamsesAlgorithm();

  private File file; //oggetto File che individua il database di immagini

  private boolean doAntipole = true;

  private final ArrayList<File> listMessages = new ArrayList<>();
  private File[] message;

  private BufferedImage ImageMessage = null;
  private int larghezzaImageMessage = 0;

  public final static boolean MAC_OS_X = System.getProperty("os.name").toLowerCase().startsWith("mac os x");

  public RamsesPanel() {
    Toolkit Tkit = Toolkit.getDefaultToolkit();
    Dimension dSchermo = Tkit.getScreenSize();
    int L_Schermo = dSchermo.width;
    int H_Schermo = dSchermo.height;

    //apertura immagini per l'elaborazione
    openChooserImage.setAcceptAllFileFilterUsed(false);
    openChooserImage.setFileFilter(new OpenImageFilter());
    openChooserImage.setCurrentDirectory(new File("C:\\SalvoPIM\\ImagesInput"));

    //salvataggio immagini ottenute dopo l'elaborazione
    saveChooserImage.setAcceptAllFileFilterUsed(false);
    saveChooserImage.setFileFilter(new SaveImageFilter());
    saveChooserImage.setCurrentDirectory(new File("C:\\SalvoPIM\\ImagesOutput"));

    //apertura database immagini per la creazione dell'Antipole Tree
    openChooserDB.setAcceptAllFileFilterUsed(false);
    openChooserDB.setFileFilter(new OpenSaveDBFilter());
    openChooserDB.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    openChooserDB.setCurrentDirectory(new File("C:\\SalvoPIM\\DB"));

    //salvataggio database con estensione .pim
    saveChooserDB.setCurrentDirectory(new File("C:\\SalvoPIM\\DB"));
    saveChooserDB.setAcceptAllFileFilterUsed(false);
    saveChooserDB.setFileFilter(new OpenSaveDBFilter());

    //salvataggio delle tiles utilizzate durante l'eleborazione di PIM e RAMSES con tutte le operazioni eseguite su ogni tiles
    saveChooserCSV.setCurrentDirectory(new File("C:\\SalvoPIM\\CSV"));
    saveChooserCSV.setAcceptAllFileFilterUsed(false);
    saveChooserCSV.setFileFilter(new SaveCSVFilter());

    //apertura immagini del messaggio
    openChooserMessages.setAcceptAllFileFilterUsed(false);
    openChooserMessages.setFileFilter(new OpenImageFilter());
    openChooserMessages.setCurrentDirectory(new File("C:\\SalvoPIM\\DB\\db_1025"));

    try {
      open.setFocusPainted(false);
      open.setMargin(new Insets(0, 0, 0, 0));
      open.setToolTipText("Open");
      open.setIcon(new ImageIcon(ImageIO.read(RamsesPanel.class.getClassLoader().getResourceAsStream("open.gif"))));

      msg.setFocusPainted(false);
      msg.setMargin(new Insets(0, 0, 0, 0));
      msg.setToolTipText("Add message");
      msg.setIcon(new ImageIcon(ImageIO.read(RamsesPanel.class.getClassLoader().getResourceAsStream("msg.gif"))));

      delete.setFocusPainted(false);
      delete.setMargin(new Insets(0, 0, 0, 0));
      delete.setToolTipText("Delete message");
      delete.setIcon(new ImageIcon(ImageIO.read(RamsesPanel.class.getClassLoader().getResourceAsStream("delete.gif"))));

      save.setEnabled(false);
      save.setFocusPainted(false);
      save.setMargin(new Insets(0, 0, 0, 0));
      save.setToolTipText("Save");
      save.setIcon(new ImageIcon(ImageIO.read(RamsesPanel.class.getClassLoader().getResourceAsStream("save.gif"))));

      start.setEnabled(false);
      start.setFocusPainted(false);
      start.setMargin(new Insets(0, 0, 0, 0));
      start.setToolTipText("Start");
      start.setIcon(new ImageIcon(ImageIO.read(RamsesPanel.class.getClassLoader().getResourceAsStream("start.gif"))));
    } catch (IOException ex) {
    }

    openImage.setText("Open Image");
    openDB.setText("Open Database");

    saveMSG.setText("Save Message");
    savePIM.setText("Save PIM Image");
    saveRAMSES.setText("Save Ramses Image");
    saveEdge.setText("Save Directional Guidelines Image");
    saveVoronoi.setText("Save Voronoi");
    saveVoronoiAndDiltedEdge.setText("Save VoronoiAndDilatedEdge Image");
    saveMaskKey.setText("Save Mask Key Image");
    saveMaskMSG.setText("Save Mask MSG Image");
    saveDB.setText("Save Database");
    saveCSV.setText("Save CSV");
    saveAlphaMask.setText("Save Alpha Mask Image");

    menuOpen.add(openImage);
    menuOpen.add(openDB);

    menuSave.add(saveMSG);
    menuSave.add(savePIM);
    menuSave.add(saveRAMSES);
    menuSave.add(saveEdge);
    menuSave.add(saveVoronoi);
    menuSave.add(saveVoronoiAndDiltedEdge);
    menuSave.add(saveMaskKey);
    menuSave.add(saveMaskMSG);
    menuSave.add(saveDB);
    menuSave.add(saveCSV);
    menuSave.add(saveAlphaMask);

    panelPulsanti.setLayout(gridLayoutPulsanti);
    panelPulsanti.add(open, null);
    panelPulsanti.add(msg, null);
    panelPulsanti.add(delete, null);
    panelPulsanti.add(save, null);
    panelPulsanti.add(start, null);

    this.setLayout(borderLayoutRamsesPanel);

    bar.setString("");
    bar.setStringPainted(true);

    panel_1.setLayout(borderLayout_1);
    panel_1.add(panelPulsanti, BorderLayout.WEST);
    panel_1.add(bar, BorderLayout.CENTER);

    panel_2.setLayout(borderLayout_2);
    panel_2.add(settingPanel, BorderLayout.WEST);

    bordoPannello = BorderFactory.createEtchedBorder(Color.white,
            new Color(165, 163, 151));
    titoloBordoPannelloMessaggio = new TitledBorder(bordoPannello, "Message ");
    pannelloMessaggio.setBorder(titoloBordoPannelloMessaggio);
    pannelloMessaggio.setLayout(new BorderLayout());

    pannelloImmaginiMessaggio.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));

    pannelloMessaggio.add(pannelloImmaginiMessaggio, BorderLayout.CENTER);

    panel_2.add(pannelloMessaggio, BorderLayout.CENTER);

    panel_1.add(panel_2, BorderLayout.SOUTH);

    PannelloImmagini.setLayout(gridLayoutPannelloImmagini);
    //PannelloImmagini.setBorder(bordoPannello);//solo bordo
    PannelloImmagini.setBorder(new TitledBorder(bordoPannello, "Elaboration ")); //bordo con titolo

    ImmagineInput.setLayout(borderLayoutInput);
    //ImmagineInput.setBorder(bordoPannello);//solo bordo
    ImmagineInput.setBorder(new TitledBorder(bordoPannello, "Input ")); //bordo con titolo
    jSPImmagineInput.getViewport().add(input);
    ImmagineInput.add(jSPImmagineInput, BorderLayout.CENTER);

    ImmagineEdge.setLayout(borderLayoutEdge);
    //ImmagineEdge.setBorder(bordoPannello);//solo bordo
    ImmagineEdge.setBorder(new TitledBorder(bordoPannello, "Edge ")); //bordo con titolo
    jSPImmagineEdge.getViewport().add(edge);
    ImmagineEdge.add(jSPImmagineEdge, BorderLayout.CENTER);

    ImmagineVoronoi.setLayout(borderLayoutVoronoi);
    //ImmagineVoronoi.setBorder(bordoPannello);//solo bordo
    ImmagineVoronoi.setBorder(new TitledBorder(bordoPannello, "Voronoi ")); //bordo con titolo
    jSPImmagineVoronoi.getViewport().add(voronoi);
    ImmagineVoronoi.add(jSPImmagineVoronoi, BorderLayout.CENTER);

    ImmagineVoronoiDilatedEdge.setLayout(borderLayoutEdgeVoronoi);
    //ImmagineVoronoiDilatedEdge.setBorder(bordoPannello);//solo bordo
    ImmagineVoronoiDilatedEdge.setBorder(new TitledBorder(bordoPannello,
            "Voronoi and Dilated Edge ")); //bordo con titolo
    jSPImmagineVoronoiDilatedEdge.getViewport().add(voronoiAndDilatedEdge);
    ImmagineVoronoiDilatedEdge.add(jSPImmagineVoronoiDilatedEdge,
            BorderLayout.CENTER);

    ImmaginePIM.setLayout(borderLayoutPIM);
    //ImmaginePIM.setBorder(bordoPannello);//solo bordo
    ImmaginePIM.setBorder(new TitledBorder(bordoPannello, "PIM ")); //bordo con titolo
    jSPImmaginePIM.getViewport().add(pim);
    ImmaginePIM.add(jSPImmaginePIM, BorderLayout.CENTER);

    ImmagineRAMSES.setLayout(borderLayoutRAMSES);
    //ImmagineRAMSES.setBorder(bordoPannello);//solo bordo
    ImmagineRAMSES.setBorder(new TitledBorder(bordoPannello, "Ramses ")); //bordo con titolo
    jSPImmagineRAMSES.getViewport().add(ramses);
    ImmagineRAMSES.add(jSPImmagineRAMSES, BorderLayout.CENTER);

    ImmagineMaskKey.setLayout(borderLayoutMaskKey);
    //ImmagineMaskKey.setBorder(bordoPannello);//solo bordo
    ImmagineMaskKey.setBorder(new TitledBorder(bordoPannello,
            "Ramses and Mask Key ")); //bordo con titolo
    jSPImmagineMaskKey.getViewport().add(maskKey);
    ImmagineMaskKey.add(jSPImmagineMaskKey, BorderLayout.CENTER);

    ImmagineMaskMsg.setLayout(borderLayoutMaskMsg);
    //ImmagineMaskMsg.setBorder(bordoPannello);//solo bordo
    ImmagineMaskMsg.setBorder(new TitledBorder(bordoPannello, "Mask Message ")); //bordo con titolo
    jSPImmagineMaskMsg.getViewport().add(maskMsg);
    ImmagineMaskMsg.add(jSPImmagineMaskMsg, BorderLayout.CENTER);

    PannelloImmagini.add(ImmagineInput);
    PannelloImmagini.add(ImmagineEdge);
    PannelloImmagini.add(ImmagineVoronoi);
    PannelloImmagini.add(ImmagineVoronoiDilatedEdge);
    PannelloImmagini.add(ImmaginePIM);
    PannelloImmagini.add(ImmagineRAMSES);
    PannelloImmagini.add(ImmagineMaskKey);
    PannelloImmagini.add(ImmagineMaskMsg);

    jScrollPaneResult.getViewport().add(messages);
    jScrollPaneCSV.getViewport().add(csv);

    jSplitPannelloOutput.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
    jSplitPannelloOutput.setDividerLocation(3 * (L_Schermo / 8));
    jSplitPannelloOutput.setOneTouchExpandable(true);
    jSplitPannelloOutput.add(jScrollPaneResult, JSplitPane.LEFT);
    jSplitPannelloOutput.add(jScrollPaneCSV, JSplitPane.RIGHT);

    jSplitPannelloImmagini.setOrientation(JSplitPane.VERTICAL_SPLIT);
    jSplitPannelloImmagini.setDividerLocation(3 * (H_Schermo / 4));
    jSplitPannelloImmagini.setOneTouchExpandable(true);
    jSplitPannelloImmagini.add(PannelloImmagini, JSplitPane.TOP);
    jSplitPannelloImmagini.add(jSplitPannelloOutput, JSplitPane.BOTTOM);

    this.add(panel_1, BorderLayout.NORTH);
    this.add(jSplitPannelloImmagini, BorderLayout.CENTER);

    Listener listener = new Listener();

    open.addActionListener(listener);
    msg.addActionListener(listener);
    delete.addActionListener(listener);
    save.addActionListener(listener);
    start.addActionListener(listener);

    openImage.addActionListener(listener);
    openDB.addActionListener(listener);

    saveMSG.addActionListener(listener);
    savePIM.addActionListener(listener);
    saveRAMSES.addActionListener(listener);
    saveEdge.addActionListener(listener);
    saveVoronoi.addActionListener(listener);
    saveVoronoiAndDiltedEdge.addActionListener(listener);
    saveMaskKey.addActionListener(listener);
    saveMaskMSG.addActionListener(listener);
    saveDB.addActionListener(listener);
    saveCSV.addActionListener(listener);
    saveAlphaMask.addActionListener(listener);

    RAMSES.setProgressBar(bar);
    RAMSES.setTextAreas(messages, csv);
  }

  private void setComponentsEnabled(boolean b) {
    open.setEnabled(b); //JButton
    msg.setEnabled(b); //JButton
    delete.setEnabled(b); //JButton
    save.setEnabled(b); //JButton
    start.setEnabled(b); //JButton

    settingPanel.setComponentsEnabled(b);
  }

  private class Listener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      Object source = e.getSource();
      if (source == openImage) {
        open(true, openChooserImage);
      } else if (source == openDB) {
        openDB();
      } else if (source == msg) {
        open(false, openChooserMessages);
      } else if (source == delete) {
        pannelloImmaginiMessaggio.removeAll();
        pannelloImmaginiMessaggio.repaint();
        listMessages.clear(); //cancella l'ArrayList
        message = null; //imposta a null l'array di File
        RAMSES.setMessaggio(null);
      } else if (source == start) {
        (new Thread() {
          @Override
          public void run() {
            setComponentsEnabled(false);

            boolean mask = settingPanel.isCreateAlphaMaskImage();
            saveAlphaMask.setEnabled(mask);

            //System.out.println("Numero di immagini del messaggio creato= "+listMessages.size());
            if (!listMessages.isEmpty()) {
              message = new File[listMessages.size()];
              listMessages.toArray(message);
              RAMSES.setMessaggio(message);
            }

            edge.setIcon(null);
            voronoi.setIcon(null);
            voronoiAndDilatedEdge.setIcon(null);
            pim.setIcon(null);
            ramses.setIcon(null);
            maskKey.setIcon(null);
            maskMsg.setIcon(null);

            RAMSES.evaluate(settingPanel.getSide(), doAntipole,
                    settingPanel.isColorize(), mask,
                    settingPanel.isUniformDistribution(), file, edge,
                    voronoi, voronoiAndDilatedEdge, pim);

            ramses.setIcon(new ImageIcon(RAMSES.getRAMSES()));

            if (RAMSES.getMessaggio() != null) {
              maskKey.setIcon(new ImageIcon(RAMSES.getRamsesAndMaskKey()));
              maskMsg.setIcon(new ImageIcon(RAMSES.getRamsesMaskMsg()));
            } else {
              maskKey.setIcon(null);
              maskMsg.setIcon(null);

              saveMSG.setEnabled(false);
              saveMaskKey.setEnabled(false);
              saveMaskMSG.setEnabled(false);
            }
            setComponentsEnabled(true);

            doAntipole = false;
          }
        }).start();
      } else if (source == open) {
        menuOpen.show(open, 0, open.getHeight());
      } else if (source == save) {
        menuSave.show(save, 0, save.getHeight());
      } else if (source == savePIM) {
        save(RAMSES.getPIM());
      } else if (source == saveRAMSES) {
        save(RAMSES.getRAMSES());
      } else if (source == saveMSG) {
        save(ImageMessage);
      } else if (source == saveEdge) {
        save(RAMSES.getEdge());
      } else if (source == saveVoronoi) {
        save(RAMSES.getVoronoi());
      } else if (source == saveVoronoiAndDiltedEdge) {
        save(RAMSES.getVoronoiAndDilatedEdge());
      } else if (source == saveMaskKey) {
        save(RAMSES.getRamsesAndMaskKey());
      } else if (source == saveMaskMSG) {
        save(RAMSES.getRamsesMaskMsg());
      } else if (source == saveDB) {
        saveDB();
      } else if (source == saveCSV) {
        saveCSV();
      } else if (source == saveAlphaMask) {
        save(RAMSES.getMask());
      }
    }
  }

  @SuppressWarnings("UnusedAssignment")
  private void open(boolean isImage, JFileChooser chooser) {
    if (altezzaPannelloImmaginiMessaggio == 0) {
      altezzaPannelloImmaginiMessaggio = pannelloImmaginiMessaggio.getHeight();
      //System.out.println("Altezza pannello immagini-messaggio= "+altezzaPannelloImmaginiMessaggio);
    }
    JFileChooser openChooser = chooser;
    openChooser.setSelectedFile(null);
    if (openChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      File tile = openChooser.getSelectedFile();
      try {
        FileImageInputStream fileImmagine = new FileImageInputStream(openChooser.getSelectedFile());
        Iterator<ImageReader> iter = ImageIO.getImageReaders(fileImmagine);
        if (iter.hasNext()) {
          ImageReader reader = iter.next();
          reader.setInput(fileImmagine);
          int w = reader.getWidth(0);
          int h = reader.getHeight(0);
          if (w * h <= 1960000) {
            BufferedImage immagine = reader.read(0);
            if (RamsesPanel.MAC_OS_X) {
              int[] data = new int[w * h];
              immagine.getRGB(0, 0, w, h, data, 0, w);
              immagine = null;
              System.gc();
              immagine = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
              immagine.setRGB(0, 0, w, h, data, 0, w);
            }

            if (isImage)//si � selezionata dal men� l'apertura di una immagine sorgente
            {
              input.setIcon(new ImageIcon(immagine));
              //Ad ogni apertura di una nuova immagine vengono resettati tutti i pannelli
              edge.setIcon(null);
              voronoi.setIcon(null);
              voronoiAndDilatedEdge.setIcon(null);
              pim.setIcon(null);
              ramses.setIcon(null);
              maskKey.setIcon(null);
              maskMsg.setIcon(null);

              RAMSES.setImage(immagine);
              RAMSES.setEdge(null);

              if (file != null) {
                start.setEnabled(true);//attiva il bottone start se � stato selezionato il DB
              }
            } else//si � selezionata l'apertura di una immagine da aggiungere al messaggio
            {
              listMessages.add(tile);//tile � un oggetto di tipo File
              //System.out.println("Immagine per il messaggio selezionata dal database: "+tile.getCanonicalPath());

              JLabel l = new JLabel();
              pannelloImmaginiMessaggio.add(l);

              int hImmagineMessaggio = immagine.getHeight();
              double scale = (double) altezzaPannelloImmaginiMessaggio / hImmagineMessaggio;
              //System.out.println("Fattore di scala immagine: "+tile.getCanonicalPath()+"="+ scale);
              int larghezza = (int) Math.round(immagine.getWidth() * scale);//larghezza nuova immagine-messaggio

              BufferedImage immagineRidimensionata = new BufferedImage(larghezza, altezzaPannelloImmaginiMessaggio, BufferedImage.TYPE_INT_ARGB);
              Graphics2D g = immagineRidimensionata.createGraphics();
              g.scale(scale, scale);
              g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
              g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
              g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
              g.drawImage(immagine, 0, 0, null);
              g.dispose();

              l.setIcon(new ImageIcon(immagineRidimensionata));

              BufferedImage temp = null;
              if (ImageMessage != null) {
                temp = new BufferedImage(larghezzaImageMessage, altezzaPannelloImmaginiMessaggio, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gtemp = temp.createGraphics();
                gtemp.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                gtemp.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                gtemp.drawImage(ImageMessage, 0, 0, null);
                gtemp.dispose();
                larghezzaImageMessage += 10;
              }

              larghezzaImageMessage += larghezza;
              //System.out.println("Larghezza nuova immagine="+larghezza+"\nLarghezza totale="+larghezzaImageMessage);

              ImageMessage = new BufferedImage(larghezzaImageMessage, altezzaPannelloImmaginiMessaggio, BufferedImage.TYPE_INT_ARGB);
              Graphics2D img = ImageMessage.createGraphics();
              img.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
              img.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
              img.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
              if (ImageMessage != null) {
                img.drawImage(temp, 0, 0, null);
              }
              img.drawImage(immagineRidimensionata, larghezzaImageMessage - larghezza, 0, null);
              img.dispose();
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
      if (RAMSES.getImage() != null) {
        start.setEnabled(true);//attiva il bottone start se � stata selezionata l'immagine
      }
    }
  }

  private void save(BufferedImage image) {
    File f = this.checkFile(saveChooserImage);
    if (f != null)
      try {
      ImageIO.write(image, saveChooserImage.getFileFilter().toString(), f);
    } catch (IOException ex) {
      JOptionPane.showMessageDialog(this, "It's not possible to save the file", "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void saveDB() {
    File f = this.checkFile(saveChooserDB);
    if (f != null) {
      try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f))) {
        out.writeObject(RAMSES.getAntipoleTree());
        out.flush();
      } catch (IOException e) {
      }
    }
  }

  private void saveCSV() {
    File f = this.checkFile(saveChooserCSV);
    if (f != null)
      try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(f))) {
      csv.write(out);
      out.flush();
    } catch (IOException e) {
    }
  }

  //Controlla durante del salvataggio se il file esiste
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
