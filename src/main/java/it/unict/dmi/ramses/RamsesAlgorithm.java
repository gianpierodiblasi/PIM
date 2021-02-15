package it.unict.dmi.ramses;

import it.unict.dmi.PIM.Shape;
import it.unict.dmi.antipole.AntipoleTree;
import it.unict.dmi.antipole.ElementList;
import it.unict.dmi.antipole.ElementNode;
import it.unict.dmi.guideline.GuidelineDetector;
import it.unict.dmi.morphology.MorphologicalOperation;
import it.unict.dmi.voronoi.VoronoiAlgorithm;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;

public class RamsesAlgorithm {

  private BufferedImage image;//Immagine input
  private BufferedImage edge;//Immagine contenente le linee guida direzionali
  private BufferedImage dilatedEdge;//Immagine ottenuta dalla precedente dopo l'operazione "MorphologicalOperation.dilate" che ispessisce i bordi individuati

  private BufferedImage voronoi; //Immagine del diagramma di Voronoi su sfondo trasparente
  private BufferedImage voronoiAndDilatedEdge; //Immagine ottenuta dalla sovrapposizione di voronoi e dilatedEdge
  private BufferedImage voronoiAndDilatedEdgePIM; //Copia della precedente immagine utilizzata da PIM

  private BufferedImage voronoiAndDilatedEdgeMESSAGES;//Copia della precedente immagine utilizzata da RAMSES

  private BufferedImage voronoiAndDilatedEdgeRAMSES; //Copia della precedente immagine utilizzata da RAMSES
  private BufferedImage immagineVoronoi; //Immagine del diagramma di Voronoi su sfondo bianco

  private BufferedImage pim; //Immagine ottenuta alla fine della esecuzione di PIM

  private BufferedImage ramses; //Immagine ottenuta alla fine della esecuzione di RAMSES
  private BufferedImage ramsesMaskKey; //Immagine che sovrapposta alla precedente individua le tiles del messaggio
  private BufferedImage ramsesAndMaskKey; //Immagine ottenuta dalla sovrapposizione di ramsesMaskKey a ramses.
  private BufferedImage ramsesMaskMsg; //Immagine in cui vengono visualizzate le tiles del messaggio senza che abbiano subito lo shift del colore

  private BufferedImage pimAlphaMask; //IMMAGINE PER RICHIESTA CANADESE. Maschera dei valori di trasparenza. Visualizza i vuoti rimasti dopo PIM.

  private int wIm; //Larghezza immagine input
  private int hIm; //Altezza immagine input

  private int experimentCount; //Numero elaborazione utilizzato i risultati dell'elaborazione mostrati in output
  private AntipoleTree imagesATF; //Struttura dati per velocizzare le operazioni di ricerca nel database
  private TreeMap<File, Integer> map = new TreeMap<>(); //Albero Red-Black

  private File[] msg = null;

  public void setMessaggio(File[] msg) {
    this.msg = msg;
  }

  public File[] getMessaggio() {
    return msg;
  }

  public void setImage(BufferedImage image) {
    this.image = image;
  }

  public void setEdge(BufferedImage edge) {
    this.edge = edge;
  }

  public BufferedImage getImage() {
    return image;
  }

  public BufferedImage getEdge() {
    return edge;
  }

  public BufferedImage getVoronoi() {
    return voronoi;
  }

  public BufferedImage getVoronoiAndDilatedEdge() {
    return voronoiAndDilatedEdge;
  }

  public BufferedImage getImmagineVoronoi() {
    return immagineVoronoi;
  }

  public BufferedImage getPIM() {
    return pim;
  }

  public BufferedImage getRAMSES() {
    return ramses;
  }

  public BufferedImage getRamsesMaskKey() {
    return ramsesMaskKey;
  }

  public BufferedImage getRamsesAndMaskKey() {
    return ramsesAndMaskKey;
  }

  public BufferedImage getRamsesMaskMsg() {
    return ramsesMaskMsg;
  }

  public BufferedImage getMask() {
    return pimAlphaMask;
  }

  public AntipoleTree getAntipoleTree() {
    return imagesATF;
  }

  private JProgressBar bar;

  public void setProgressBar(JProgressBar bar) {
    this.bar = bar;
  }

  private JTextArea text, csv;//text: tempi di elaborazione; csv: tiles utilizzate e operazioni eseguite su di esse

  public void setTextAreas(JTextArea text, JTextArea csv) {
    this.text = text;
    this.csv = csv;
  }

  public void evaluate(int side, boolean doAntipole, boolean colorize, boolean mask, boolean uniformDistribution, File dir, JLabel JLEdge, JLabel JLVoronoi, JLabel JLVoronoiEdge, JLabel JLPim) {
    wIm = image.getWidth();//Larghezza immagine input
    hIm = image.getHeight();//Altezza immagine input

    //INIZIO COSTRUZIONE ANTIPOLE TREE
    long startAntipole = System.currentTimeMillis();//tempo di inizio creazione Antipole Tree
    if (doAntipole)//se doAntipole==true crea Antipole Tree
    {
      if (bar != null) {
        bar.setString("Reading Database...");
      }
      this.createAntipoleTree(dir);
      System.gc();//garbage collector
    }
    long stopAntipole = System.currentTimeMillis();//tempo di fine creazione Antipole Tree
    //FINE COSTRUZIONE ANTIPOLE TREE

    //INIZIO INDIVIDUAZIONE DEI BORDI, GENERAZIONE DEL DIAGRAMMA DI VORONOI E SOVRAPPOSIZIONE DEI DUE RISULTATI PRECEDENTI
    long startEdge = System.currentTimeMillis();//tempo inizio creazione immagini contenenti le linee guida direzionali e il diagramma di Voronoi

    if (edge == null || edge.getWidth() != wIm || edge.getHeight() != hIm) {
      edge = GuidelineDetector.evaluate(image, bar);//Immagine con le linee guida direzionali
    }
    JLEdge.setIcon(new ImageIcon(this.getEdge()));

    dilatedEdge = MorphologicalOperation.dilate(edge, false, 3);//Immagine con le linee guida direzionali ispessite

    voronoi = VoronoiAlgorithm.evaluate(wIm, hIm, side, true, bar);//Immagine con il diagramma di Voronoi su fondo trasparente

    //La BufferedImage immagineVoronoi viene creata per poter visualizzare in output il diagramma di Voronoi.
    //Non si utilizza la BufferedImage voronoi in quanto il diagramma in essa e' disegnato su uno sfondo trasparente.
    immagineVoronoi = new BufferedImage(wIm, hIm, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = immagineVoronoi.createGraphics();
    g.setPaint(Color.white);
    g.fillRect(0, 0, wIm, hIm);
    g.drawImage(voronoi, 0, 0, null);

    JLVoronoi.setIcon(new ImageIcon(this.getImmagineVoronoi()));

    dilatedEdge.createGraphics().drawImage(voronoi, 0, 0, null);//Disegna su dilatedEdge il diagramma di Voronoi(costituito dalle sole linee)

    //La BufferedImage voronoiAndDilatedEdge viene creata per poter visualizzare in output cio' che si ottiene
    //sovrapponendo al diagramma di Voronoi le linee guida direzionali individuate.
    voronoiAndDilatedEdge = new BufferedImage(wIm, hIm, BufferedImage.TYPE_INT_ARGB);
    voronoiAndDilatedEdge.createGraphics().drawImage(dilatedEdge, 0, 0, null);

    JLVoronoiEdge.setIcon(new ImageIcon(this.getVoronoiAndDilatedEdge()));

    long stopEdge = System.currentTimeMillis();//tempo fine creazione immagini contenenti le linee guida direzionali e il diagramma di Voronoi
    //FINE INDIVIDUAZIONE DEI BORDI, GENERAZIONE DEL DIAGRAMMA DI VORONOI E SOVRAPPOSIZIONE DEI DUE RISULTATI PRECEDENTI

    //Creazione della BufferedImage voronoiAndDilatedEdgePIM utilizzata da PIM
    voronoiAndDilatedEdgePIM = new BufferedImage(wIm, hIm, BufferedImage.TYPE_INT_ARGB);
    voronoiAndDilatedEdgePIM.createGraphics().drawImage(dilatedEdge, 0, 0, null);

    //Creazione della BufferedImage voronoiAndDilatedEdgeMESSAGES utilizzata da RAMSES per il posizionamento dei messaggi.
    voronoiAndDilatedEdgeMESSAGES = new BufferedImage(wIm, hIm, BufferedImage.TYPE_INT_ARGB);
    voronoiAndDilatedEdgeMESSAGES.createGraphics().drawImage(dilatedEdge, 0, 0, null);

    //Creazione della BufferedImage voronoiAndDilatedEdgeRAMSES utilizzata da RAMSES
    voronoiAndDilatedEdgeRAMSES = new BufferedImage(wIm, hIm, BufferedImage.TYPE_INT_ARGB);
    voronoiAndDilatedEdgeRAMSES.createGraphics().drawImage(dilatedEdge, 0, 0, null);

    //START ELABORATION PIM
    long startPIM = System.currentTimeMillis();
    //PIM
    if (bar != null) {
      bar.setValue(0);
    }
    if (bar != null) {
      bar.setString("Puzzle Image Mosaic...");
    }
    this.PIM(colorize, uniformDistribution, mask, JLPim);

    long stopPIM = System.currentTimeMillis();
    //STOP ELABORATION PIM

    //START ELABORATION RAMSES
    long startRAMSES = System.currentTimeMillis();
    //RAMSES
    if (bar != null) {
      bar.setValue(0);
    }
    if (bar != null) {
      bar.setString("RAMSES...");
    }
    this.RAMSES(colorize, uniformDistribution, getMessaggio());

    if (bar != null) {
      bar.setValue(0);
      bar.setString("Finished!!!");
    }
    long stopRAMSES = System.currentTimeMillis();
    //STOP ELABORATION RAMSES

    NumberFormat format = NumberFormat.getNumberInstance();
    format.setMinimumFractionDigits(3);
    format.setMaximumFractionDigits(3);
    experimentCount++;

    if (text != null) {
      text.insert("--------------------------------\n", 0);
      text.insert("\tTotal Elapsed Time                        = " + format.format((stopAntipole - startAntipole + stopEdge - startEdge + stopPIM - startPIM + stopRAMSES - startRAMSES) / 1000.0) + " seconds\n", 0);
      text.insert("\tElapsed Time for Antipole Clustering      = " + format.format((stopAntipole - startAntipole) / 1000.0) + " seconds\n", 0);
      text.insert("\tElapsed Time for Guideline Detection      = " + format.format((stopEdge - startEdge) / 1000.0) + " seconds\n", 0);
      text.insert("\tElapsed Time for RAMSES Creation          = " + format.format((stopRAMSES - startRAMSES) / 1000.0) + " seconds\n", 0);
      text.insert("\tElapsed Time for PIM Creation             = " + format.format((stopPIM - startPIM) / 1000.0) + " seconds\n", 0);
      text.insert("Results\n", 0);
      text.insert("\tSide                                      = " + side + "\n", 0);
      text.insert("Data\n", 0);
      text.insert("*** Experiment N. " + experimentCount + " ***\n", 0);
    }

    //TEST
    /*Iterator iter=map.keySet().iterator();
                                    while (iter.hasNext())
                                    {
                                        Object obj=iter.next();
                                        System.out.println(obj+" "+map.get(obj));
                                    }*/
    //FINE TEST
  }
  //FINE public void evaluate(int , boolean , boolean , boolean , boolean , File )

  private void createAntipoleTree(File dir) {
    if (dir.isDirectory())//Se dir e' una directory costruisce l'Antipole Tree con i file in essa contenuti
    {
      File[] file = dir.listFiles();//Lista dei file contenuti nella directory
      ArrayList<Shape> list = new ArrayList<>();
      for (int i = 0; i < file.length; i++)//per ogni File dell'array file
      {
        if (bar != null & i % 5 == 0) {
          bar.setValue(100 * i / file.length);
        }
        BufferedImage buffer = null;
        try {
          buffer = ImageIO.read(file[i]);//restituisce una BufferedImage dal File file[i]
        } catch (IOException io) {
        }
        if (buffer != null) {
          list.add(new Shape(buffer, file[i]));//se e' stata correttamente restituita una BufferedImage
        }        //si costruisce uno Shape e lo si inserisce nell'ArrayList list.
      }
      Shape[] tiles = new Shape[list.size()];//predispone un array di Shape che rappresentera' il dataset di tiles
      list.toArray(tiles);//riempie l'array tiles con gli elementi di list
      imagesATF = new AntipoleTree(tiles, 0.2);//costruisce l'Antipole Tree contenente le tiles create dalle immagini
    } else {
      //se dir non e' una directory che contiene delle immagini, potrebbe essere un file contenente l'Antipole precedentemente creato.
      try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(dir))) {
        imagesATF = (AntipoleTree) in.readObject();
      } catch (Exception e) {
      }
    }
  }
  //FINE private void createAntipoleTree(File dir)

  StringBuffer str;//Stringa contenente tutte le operazioni eseguite sulle immagini-tiles durante le elaborazioni di PIM e RAMSES

  //INIZIO PIM
  @SuppressWarnings({"UnusedAssignment", "null"})
  private void PIM(boolean colorize, boolean uniform, boolean mask, JLabel p) {
    str = new StringBuffer("***   ELABORATION PIM.    ***\nTile file name; X region center; Y region center; Angle of rotation; Scale factor; X image center; Y image center; Red; Green; Blue\n");//informazioni di output

    pim = new BufferedImage(wIm, hIm, BufferedImage.TYPE_INT_ARGB);

    Graphics2D gPIM = pim.createGraphics();//Crea un Graphics2D che puo' essere usato per disegnare sulla BufferedImage pim.
    gPIM.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    gPIM.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    gPIM.setPaint(Color.black);
    gPIM.fillRect(0, 0, wIm, hIm);//disegna un rettangolo nero pari alle dimensioni dell'immagine in input

    Graphics2D gMask = null;
    if (mask) {
      pimAlphaMask = new BufferedImage(wIm, hIm, BufferedImage.TYPE_INT_ARGB);//Richiesta Canadese. Visualizza in sostanza i vuoti rimasti dopo la creazione di pim
      gMask = pimAlphaMask.createGraphics();//Crea un Graphics2D che puo' essere usato per disegnare sulla BufferedImage pimAlphaMask.
      gMask.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      gMask.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }

    //Ciclo che considera tutti i pixel dell'immagine operando sui pixel "neri"(scelta del programmatore).
    //Il ciclo quindi non considera i pixel delle linee guida e quelli del diagramma di Voronoi.
    //Cerca i pixel "non neri" che individuano regioni non trattate.
    for (int y = 0; y < hIm; y++) {
      for (int x = 0; x < wIm; x++) {
        if (voronoiAndDilatedEdgePIM.getRGB(x, y) != 0xFF000000)//Si e' individuata una regione che non e' stata ancora trattata
        {
          Shape shape = new Shape(image, voronoiAndDilatedEdgePIM, x, y);//Restituisce lo Shape corrispondente alla regione che contiene il pixel(x,y)
          //Dopo questa istruzione che la regione corrispondente al pixel (x,y) viene colorata di nero
          if (shape.getScaleFactor() != 0)//Potrebbe accadere che il fattore di scala e' zero e in tal caso non fa nulla
          {
            ElementList cluster = imagesATF.nearestElementSearch(shape, false);//Restituisce una lista di elementi(i piu' vicini, cioe' quelle che si assomogliano di piu')
            //presente nella struttura Antipole Tree
            Shape nearest = null;//Shape piu' vicino
            Iterator<ElementNode> iter = cluster.iterator();//Iteratore sulla lista di elementi restituiti

            if (uniform)//Sceglie in maniera uniforme le immagini nella lista, cioe' non sempre le stesse
            {
              int nearestCounter = Integer.MAX_VALUE;
              File nearestPath = null;

              while (iter.hasNext())//Restituisce true se ci sono altri elementi
              {
                Shape shapeTest = (Shape) iter.next().getKey();//iter.next() restituisce il successivo elemento nella lista
                int counter = 0;
                File path = shapeTest.getPath();//getPath() ritorna un tipo FILE. Non e' il getPath() della Classe File

                if (!map.containsKey(path))//map e' l'albero Red-Black
                {
                  map.put(path, 0);
                } else {
                  counter = map.get(path);
                }

                if (counter < nearestCounter && shapeTest.getScaleFactor() != 0) {
                  nearest = shapeTest;
                  nearestCounter = counter;
                  nearestPath = path;
                }
              }//end while

              map.put(nearestPath, nearestCounter + 1);
            }//end if uniform
            else {
              double nearestDistance = Double.MAX_VALUE;

              while (iter.hasNext())//Restituisce true se ci sono altri elementi
              {
                Shape shapeTest = (Shape) iter.next().getKey();
                double d = shapeTest.distance(shape);//Determina la distanza di shapeTest da shape

                if (d < nearestDistance && shapeTest.getScaleFactor() != 0) {
                  nearest = shapeTest;
                  nearestDistance = d;
                }
              }//end while
            }//end else uniform

            //Inizia lo shifting del colore
            int[] colorShape = shape.getColor();//getColor() e' un metodo della classe Shape
            float[] hsbShape = new float[3];
            Color.RGBtoHSB(colorShape[0], colorShape[1], colorShape[2], hsbShape);

            @SuppressWarnings("null")
            int[] colorNearest = nearest.getColor();//getColor() e' un metodo della classe Shape
            float[] hsbNearest = new float[3];
            Color.RGBtoHSB(colorNearest[0], colorNearest[1], colorNearest[2], hsbNearest);

            float[] diff = new float[3];
            diff[0] = (hsbShape[0] - hsbNearest[0]) * 360;

            while (diff[0] < 0) {
              diff[0] += 360;
            }

            while (diff[0] > 360) {
              diff[0] -= 360;
            }

            diff[1] = hsbShape[1] - hsbNearest[1];
            diff[2] = hsbShape[2] - hsbNearest[2];

            BufferedImage buffer = null;
            File file = nearest.getPath();//getPath() ritorna un tipo FILE. Non e' il getPath() della Classe File
            try {
              if (csv != null) {
                str.append(file.getCanonicalPath()).append(";");//Aggiunge in fondo a str il percorso della tile-immagine selezionata
              }
              buffer = ImageIO.read(file);
            } catch (IOException e) {
            }

            if (buffer != null)//IF1
            {
              int wBuffer = buffer.getWidth();
              int hBuffer = buffer.getHeight();
              int[] dataBuffer = new int[wBuffer * hBuffer];
              buffer.getRGB(0, 0, wBuffer, hBuffer, dataBuffer, 0, wBuffer);

              buffer = null;
              System.gc();
              buffer = new BufferedImage(wBuffer, hBuffer, BufferedImage.TYPE_INT_ARGB);

              if (colorize)//se colorize==true shifta il colore
              {
                for (int c = 0; c < dataBuffer.length; c++) {
                  if ((dataBuffer[c] & 0xFF000000) != 0) {
                    float[] hsbPixel = new float[3];
                    Color.RGBtoHSB((dataBuffer[c] >> 16) & 0xFF, (dataBuffer[c] >> 8) & 0xFF, dataBuffer[c] & 0xFF, hsbPixel);

                    hsbPixel[0] *= 360;
                    hsbPixel[0] += diff[0];

                    while (hsbPixel[0] < 0) {
                      hsbPixel[0] += 360;
                    }

                    while (hsbPixel[0] > 360) {
                      hsbPixel[0] -= 360;
                    }

                    hsbPixel[0] /= 360;

                    hsbPixel[1] += diff[1];
                    if (hsbPixel[1] < 0) {
                      hsbPixel[1] = 0;
                    } else if (hsbPixel[1] > 1) {
                      hsbPixel[1] = 1;
                    }

                    hsbPixel[2] += diff[2];
                    if (hsbPixel[2] < 0) {
                      hsbPixel[2] = 0;
                    } else if (hsbPixel[2] > 1) {
                      hsbPixel[2] = 1;
                    }

                    dataBuffer[c] = Color.HSBtoRGB(hsbPixel[0], hsbPixel[1], hsbPixel[2]);
                  }
                }
              }

              buffer.setRGB(0, 0, wBuffer, hBuffer, dataBuffer, 0, wBuffer);

              //Ridimensiona e Trasla lo Shape posizionandolo all'interno della regione
              Point2D center = shape.getCenter();
              shape.distance(nearest);
              double xC = center.getX();
              double yC = center.getY();
              double scale = shape.getScaleFactor() / nearest.getScaleFactor();
              double rot = -Shape.getRotationAngle(shape.bestShift());
              if (csv != null) {
                str.append(xC).append(";").append(yC).append(";").append(rot).append(";").append(scale).append(";");
              }

              AffineTransform tx = AffineTransform.getTranslateInstance(xC, yC);

              tx.concatenate(AffineTransform.getRotateInstance(rot));
              tx.concatenate(AffineTransform.getScaleInstance(scale, scale));

              center = nearest.getCenter();
              xC = -center.getX();
              yC = -center.getY();

              tx.concatenate(AffineTransform.getTranslateInstance(xC, yC));

              if (csv != null) {
                str.append(xC).append(";").append(yC).append(";").append(colorShape[0]).append(";").append(colorShape[1]).append(";").append(colorShape[2]).append("\n");
              }

              gPIM.drawImage(buffer, tx, null);//Disegna l'immagine ottenuta dopo lo shifting del colore
              //e il suo ridimensionamento su pim, cioe' sulla BufferedImage
              //che mostra l'output di PIM
              if (mask) {
                gMask.drawImage(buffer, tx, null);
              }
            }//end IF1
          }//end if (shape.getScaleFactor()!=0)

          int c = y * wIm + x;
          if (bar != null && c % 5 == 0) {
            bar.setValue(100 * c / (wIm * hIm));
          }
        }//end if (voronoiAndDilatedEdgePIM.getRGB(x,y)!=0xFF000000)
      }
    }
    if (mask) {
      if (bar != null) {
        bar.setString("Creating Alpha Mask Image...");
      }
      if (bar != null) {
        bar.setValue(0);
      }
      for (int y = 0; y < hIm; y++) {
        for (int x = 0; x < wIm; x++) {
          int cc = y * wIm + x;
          if (bar != null && cc % 5 == 0) {
            bar.setValue(100 * cc / (wIm * hIm));
          }

          int c = pimAlphaMask.getRGB(x, y);
          if ((c & 0xFF000000) == 0)//0xFF000000 equivale a black
          {
            c = 0xFF000000;
          } else {
            c = 0xFFFFFFFF;//0xFFFFFFFF equivale a white
          }
          pimAlphaMask.setRGB(x, y, c);
        }
      }
      gMask.dispose();
    }

    gPIM.dispose();
    csv.setText(str.toString());

    p.setIcon(new ImageIcon(this.getPIM()));
  }
  //FINE PIM

  //INIZIO RAMSES
  @SuppressWarnings("UnusedAssignment")
  private void RAMSES(boolean colorize, boolean uniform, File[] messaggio) {
    if (csv != null) {
      str.append("\n***   ELABORATION RAMSES. ***\nTile file name; X region center; Y region center; Angle of rotation; Scale factor; X image center; Y image center; Red; Green; Blue\n");//informazioni di output
    }
    ramses = new BufferedImage(wIm, hIm, BufferedImage.TYPE_INT_ARGB);

    Graphics2D gRamses = ramses.createGraphics();//Crea un Graphics2D che puo' essere usato per disegnare sulla BufferedImage ramses.
    gRamses.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    gRamses.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    gRamses.setPaint(Color.black);
    gRamses.fillRect(0, 0, wIm, hIm);

    int regioni = VoronoiAlgorithm.getVoronoiRegionCount();

    //System.out.println("Numero di regioni del diagramma di Voronoi: "+regioni);
    if (messaggio != null) {
      ramsesMaskKey = new BufferedImage(wIm, hIm, BufferedImage.TYPE_INT_ARGB);
      ramsesMaskMsg = new BufferedImage(wIm, hIm, BufferedImage.TYPE_INT_ARGB);

      Graphics2D gRamsesMaskKey = ramsesMaskKey.createGraphics();//Crea un Graphics2D che puo' essere usato per disegnare sulla BufferedImage ramsesMaskKey.
      gRamsesMaskKey.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      gRamsesMaskKey.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

      Graphics2D gRamsesMaskMsg = ramsesMaskMsg.createGraphics();//Crea un Graphics2D che puo' essere usato per disegnare sulla BufferedImage ramsesMaskMsg.
      gRamsesMaskMsg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      gRamsesMaskMsg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      gRamsesMaskMsg.setPaint(Color.black);
      gRamsesMaskMsg.fillRect(0, 0, wIm, hIm);

      //Creazione delle tiles che rappresentano il messaggio da spedire
      ArrayList<Shape> lista = new ArrayList<>();
      for (@SuppressWarnings("LocalVariableHidesMemberVariable") File msg : messaggio) //per ogni File dell'array messaggio
      {
        BufferedImage buffer = null;
        try {
          buffer = ImageIO.read(msg); //restituisce una BufferedImage dall'oggetto File messaggio[i]
          //System.out.println("Lettura del File "+i+": "+messaggio[i].getCanonicalPath());
        } catch (IOException io) {
        }
        if (buffer != null) {
          lista.add(new Shape(buffer, msg)); //Se e' stata correttamente restituita una BufferedImage
        } //si costruisce uno Shape e lo si inserisce nell'ArrayList lista.
      }
      Shape[] MSGtiles = new Shape[lista.size()];//Predispone un array di Shape che rappresentera' il dataset di tiles del messaggio
      lista.toArray(MSGtiles);//Riempie l'array MSGtiles con gli elementi di lista

      //System.out.println("Numero di tiles-messaggio create= "+MSGtiles.length);
      //Determinazione delle regioni in cui inserire le tiles che costituiscono il messaggio, e posizionamento delle stesse
      //in ramses, ramsesMaskKey e ramsesMaskMsg.
      int i = 0;//contatore tiles-messaggio
      int regioniPerTile = regioni / MSGtiles.length;
      //System.out.println("Numero di regioni per tile= "+regioniPerTile);
      int contatoreRegioni = 0;
      int y = 0;
      int xMigliore = 0, yMigliore = 0;
      while (i < MSGtiles.length)//fino a quando non sono state posizionate tutte le tiles-messaggio
      {
        Shape tile = MSGtiles[i];//shape da posizionare

        //try{System.out.println("Tile in esame: "+tile.getPath().getCanonicalPath());}
        //catch (Exception e){}
        double miglioreDistanza = Double.MAX_VALUE;

        boolean continua = true;

        for (; y < hIm && continua; y++) {
          for (int x = 0; x < wIm && continua; x++) {
            //System.out.println("Punto in esame: x= "+x+" y= "+y);
            if (voronoiAndDilatedEdgeMESSAGES.getRGB(x, y) != 0xFF000000)//Si e' individuata una regione che non e' stata ancora trattata
            {
              //System.out.println("Regione in esame: x= "+x+" y= "+y);
              Shape shapetest = new Shape(image, voronoiAndDilatedEdgeMESSAGES, x, y);//Restituisce lo Shape corrispondente alla regione che contiene il pixel(x,y)
              //Dopo questa istruzione che la regione corrispondente al pixel (x,y) viene colorata di nero
              //System.out.println("Analisi della regione "+contatoreRegioni);

              contatoreRegioni++;

              if (contatoreRegioni >= regioniPerTile * (i + 1)) {
                continua = false;
              }

              if (shapetest.getScaleFactor() != 0)//Potrebbe accadere che il fattore di scala e' zero e in tal caso non fa nulla
              {
                double distanza = tile.distance(shapetest);//determina la distanza della tile in esame dallo shape individuato

                if (distanza < miglioreDistanza) {
                  miglioreDistanza = distanza;
                  xMigliore = x;
                  yMigliore = y;
                }
              }
            }
          }//si e' esaminata la regione corrispondente al pixel (x,y) di voronoiAndDilatedEdgeMESSAGES
        }
        //Si e' conclusa la ricerca della regione migliore per la tile MSGtiles[i]

        Shape shape = new Shape(image, voronoiAndDilatedEdgeRAMSES, xMigliore, yMigliore);

        //Inizia lo shifting del colore
        int[] colorShape = shape.getColor();//getColor() e' un metodo della classe Shape
        float[] hsbShape = new float[3];
        Color.RGBtoHSB(colorShape[0], colorShape[1], colorShape[2], hsbShape);

        int[] colorNearest = MSGtiles[i].getColor();//getColor() e' un metodo della classe Shape
        float[] hsbNearest = new float[3];
        Color.RGBtoHSB(colorNearest[0], colorNearest[1], colorNearest[2], hsbNearest);

        float[] diff = new float[3];
        diff[0] = (hsbShape[0] - hsbNearest[0]) * 360;

        while (diff[0] < 0) {
          diff[0] += 360;
        }

        while (diff[0] > 360) {
          diff[0] -= 360;
        }

        diff[1] = hsbShape[1] - hsbNearest[1];
        diff[2] = hsbShape[2] - hsbNearest[2];

        BufferedImage buffer = null;//Tile immagine per la BufferedImage ramses
        BufferedImage bufferMSG = null;//Tile immagine per la BufferedImage ramsesMaskMsg
        File file = MSGtiles[i].getPath();//getPath() ritorna un tipo FILE.Non e' il getPath() della Classe File
        try {
          if (csv != null) {
            str.append(file.getCanonicalPath()).append(";");
          }
          buffer = ImageIO.read(file);
          bufferMSG = ImageIO.read(file);
        } catch (IOException e) {
        }

        if (buffer != null && bufferMSG != null)//IF1
        {
          int wBuffer = buffer.getWidth();
          int hBuffer = buffer.getHeight();
          int[] dataBuffer = new int[wBuffer * hBuffer];
          buffer.getRGB(0, 0, wBuffer, hBuffer, dataBuffer, 0, wBuffer);

          buffer = null;
          System.gc();
          buffer = new BufferedImage(wBuffer, hBuffer, BufferedImage.TYPE_INT_ARGB);

          if (colorize) {
            for (int c = 0; c < dataBuffer.length; c++) {
              if ((dataBuffer[c] & 0xFF000000) != 0) {
                float[] hsbPixel = new float[3];
                Color.RGBtoHSB((dataBuffer[c] >> 16) & 0xFF, (dataBuffer[c] >> 8) & 0xFF, dataBuffer[c] & 0xFF, hsbPixel);

                hsbPixel[0] *= 360;
                hsbPixel[0] += diff[0];

                while (hsbPixel[0] < 0) {
                  hsbPixel[0] += 360;
                }

                while (hsbPixel[0] > 360) {
                  hsbPixel[0] -= 360;
                }

                hsbPixel[0] /= 360;

                hsbPixel[1] += diff[1];
                if (hsbPixel[1] < 0) {
                  hsbPixel[1] = 0;
                } else if (hsbPixel[1] > 1) {
                  hsbPixel[1] = 1;
                }

                hsbPixel[2] += diff[2];
                if (hsbPixel[2] < 0) {
                  hsbPixel[2] = 0;
                } else if (hsbPixel[2] > 1) {
                  hsbPixel[2] = 1;
                }

                dataBuffer[c] = Color.HSBtoRGB(hsbPixel[0], hsbPixel[1], hsbPixel[2]);
              }
            }
          }

          buffer.setRGB(0, 0, wBuffer, hBuffer, dataBuffer, 0, wBuffer);

          //Ridimensiona e Trasla lo Shape posizionandolo all'interno della regione
          Point2D center = shape.getCenter();
          shape.distance(MSGtiles[i]);
          double xC = center.getX();
          double yC = center.getY();
          double scale = shape.getScaleFactor() / MSGtiles[i].getScaleFactor();
          double rot = -Shape.getRotationAngle(shape.bestShift());
          if (csv != null) {
            str.append(xC).append(";").append(yC).append(";").append(rot).append(";").append(scale).append(";");
          }

          AffineTransform tx = AffineTransform.getTranslateInstance(xC, yC);

          tx.concatenate(AffineTransform.getRotateInstance(rot));
          tx.concatenate(AffineTransform.getScaleInstance(scale, scale));

          center = MSGtiles[i].getCenter();
          xC = -center.getX();
          yC = -center.getY();

          tx.concatenate(AffineTransform.getTranslateInstance(xC, yC));

          if (csv != null) {
            str.append(xC).append(";").append(yC).append(";").append(colorShape[0]).append(";").append(colorShape[1]).append(";").append(colorShape[2]).append("\n");
          }

          gRamses.drawImage(buffer, tx, null);
          gRamsesMaskMsg.drawImage(bufferMSG, tx, null);
          gRamsesMaskKey.drawImage(buffer, tx, null);
        }//end IF1

        i++;

        //System.out.println("Punto inserimento tile-messaggio "+i+": x= "+xMigliore+" y= "+yMigliore);
      }
      //end while(i<messaggio.length): sono state posizionate tutte le tiles del messaggio

      gRamsesMaskMsg.dispose();

      //Disegna in "trasparente" le tiles-messaggio su uno sfondo nero. Il ciclo dovrebbe realizzare la maschera di lettura
      for (int y1 = 0; y1 < hIm; y1++) {
        for (int x1 = 0; x1 < wIm; x1++) {
          int c = ramsesMaskKey.getRGB(x1, y1);
          if ((c & 0xFF000000) == 0)//0xFF000000 equivale a black
          {
            c = (new Color(0, 0, 0, 200)).getRGB();//new Color(0,0,0,200) oppure 0xFF000000
          } else {
            c = 0x00000000;//il colore del pixel (x,y) viene impostato trasparente
          }
          ramsesMaskKey.setRGB(x1, y1, c);
        }
      }
      gRamsesMaskKey.dispose();
    }

    //Ciclo che considera tutti i pixel dell'immagine operando sui pixel "neri"(scelta del programmatore).
    //Il ciclo quindi non considera i pixel delle linee guida, quelli del diagramma di Voronoi e quelli
    //contenenti le tiles del messaggio. Cerca i pixel "non neri" che individuano regioni non ancora trattate.
    for (int y = 0; y < hIm; y++) {
      for (int x = 0; x < wIm; x++) {
        if (voronoiAndDilatedEdgeRAMSES.getRGB(x, y) != 0xFF000000)//0xFF000000 equivale a black
        {
          Shape shape = new Shape(image, voronoiAndDilatedEdgeRAMSES, x, y);

          if (shape.getScaleFactor() != 0)//Potrebbe accadere che il fattore di scala e' zero e in tal caso non fa nulla
          {
            ElementList cluster = imagesATF.nearestElementSearch(shape, false);//Restituisce una lista di elementi(i piu' vicini, cioe' quelle che si assomogliano di piu')
            //presente nella struttura Antipole Tree
            Shape nearest = null;//Shape piu' vicino
            Iterator<ElementNode> iter = cluster.iterator();//Iteratore sulla lista di elementi restituiti

            if (uniform)//Sceglie in maniera uniforme le immagini nella lista, cioe' non sempre le stesse
            {
              int nearestCounter = Integer.MAX_VALUE;
              File nearestPath = null;

              while (iter.hasNext())//Restituisce true se ci sono altri elementi
              {
                Shape shapeTest = (Shape) iter.next().getKey();//iter.next() restituisce il successivo elemento nella lista
                int counter = 0;
                File path = shapeTest.getPath();//getPath() ritorna un tipo FILE.Non e' il getPath() della Classe File

                if (!map.containsKey(path))//map e' l'albero Red-Black
                {
                  map.put(path, 0);
                } else {
                  counter = map.get(path);
                }

                if (counter < nearestCounter && shapeTest.getScaleFactor() != 0) {
                  nearest = shapeTest;
                  nearestCounter = counter;
                  nearestPath = path;
                }
              }//end while

              map.put(nearestPath, nearestCounter + 1);
            }//end if uniform
            else {
              double nearestDistance = Double.MAX_VALUE;

              while (iter.hasNext())//Restituisce true se ci sono altri elementi
              {
                Shape shapeTest = (Shape) iter.next().getKey();
                double d = shapeTest.distance(shape);//Determina la distanza di shapeTest da shape

                if (d < nearestDistance && shapeTest.getScaleFactor() != 0) {
                  nearest = shapeTest;
                  nearestDistance = d;
                }
              }//end while
            }//end else uniform

            //Inizia lo shifting del colore
            int[] colorShape = shape.getColor();//getColor() e' un metodo della classe Shape
            float[] hsbShape = new float[3];
            Color.RGBtoHSB(colorShape[0], colorShape[1], colorShape[2], hsbShape);

            @SuppressWarnings("null")
            int[] colorNearest = nearest.getColor();//getColor() e' un metodo della classe Shape
            float[] hsbNearest = new float[3];
            Color.RGBtoHSB(colorNearest[0], colorNearest[1], colorNearest[2], hsbNearest);

            float[] diff = new float[3];
            diff[0] = (hsbShape[0] - hsbNearest[0]) * 360;

            while (diff[0] < 0) {
              diff[0] += 360;
            }

            while (diff[0] > 360) {
              diff[0] -= 360;
            }

            diff[1] = hsbShape[1] - hsbNearest[1];
            diff[2] = hsbShape[2] - hsbNearest[2];

            BufferedImage buffer = null;
            File file = nearest.getPath();//getPath() ritorna un tipo FILE.Non e' il getPath() della Classe File
            try {
              if (csv != null) {
                str.append(file.getCanonicalPath()).append(";");
              }
              buffer = ImageIO.read(file);
            } catch (IOException e) {
            }

            if (buffer != null)//IF1
            {
              int wBuffer = buffer.getWidth();
              int hBuffer = buffer.getHeight();
              int[] dataBuffer = new int[wBuffer * hBuffer];
              buffer.getRGB(0, 0, wBuffer, hBuffer, dataBuffer, 0, wBuffer);

              buffer = null;
              System.gc();
              buffer = new BufferedImage(wBuffer, hBuffer, BufferedImage.TYPE_INT_ARGB);

              if (colorize) {
                for (int c = 0; c < dataBuffer.length; c++) {
                  if ((dataBuffer[c] & 0xFF000000) != 0) {
                    float[] hsbPixel = new float[3];
                    Color.RGBtoHSB((dataBuffer[c] >> 16) & 0xFF, (dataBuffer[c] >> 8) & 0xFF, dataBuffer[c] & 0xFF, hsbPixel);

                    hsbPixel[0] *= 360;
                    hsbPixel[0] += diff[0];

                    while (hsbPixel[0] < 0) {
                      hsbPixel[0] += 360;
                    }

                    while (hsbPixel[0] > 360) {
                      hsbPixel[0] -= 360;
                    }

                    hsbPixel[0] /= 360;

                    hsbPixel[1] += diff[1];
                    if (hsbPixel[1] < 0) {
                      hsbPixel[1] = 0;
                    } else if (hsbPixel[1] > 1) {
                      hsbPixel[1] = 1;
                    }

                    hsbPixel[2] += diff[2];
                    if (hsbPixel[2] < 0) {
                      hsbPixel[2] = 0;
                    } else if (hsbPixel[2] > 1) {
                      hsbPixel[2] = 1;
                    }

                    dataBuffer[c] = Color.HSBtoRGB(hsbPixel[0], hsbPixel[1], hsbPixel[2]);
                  }
                }
              }

              buffer.setRGB(0, 0, wBuffer, hBuffer, dataBuffer, 0, wBuffer);

              //Ridimensiona e Trasla lo Shape posizionandolo all'interno della regione
              Point2D center = shape.getCenter();
              shape.distance(nearest);
              double xC = center.getX();
              double yC = center.getY();
              double scale = shape.getScaleFactor() / nearest.getScaleFactor();
              double rot = -Shape.getRotationAngle(shape.bestShift());
              if (csv != null) {
                str.append(xC).append(";").append(yC).append(";").append(rot).append(";").append(scale).append(";");
              }

              AffineTransform tx = AffineTransform.getTranslateInstance(xC, yC);

              tx.concatenate(AffineTransform.getRotateInstance(rot));
              tx.concatenate(AffineTransform.getScaleInstance(scale, scale));

              center = nearest.getCenter();
              xC = -center.getX();
              yC = -center.getY();

              tx.concatenate(AffineTransform.getTranslateInstance(xC, yC));

              if (csv != null) {
                str.append(xC).append(";").append(yC).append(";").append(colorShape[0]).append(";").append(colorShape[1]).append(";").append(colorShape[2]).append("\n");
              }

              gRamses.drawImage(buffer, tx, null);

            }//end IF1
          }//end if (shape.getScaleFactor()!=0)

          int c = y * wIm + x;
          if (bar != null && c % 5 == 0) {
            bar.setValue(100 * c / (wIm * hIm));
          }
        }//end if (voronoiAndDilatedEdgeRAMSES.getRGB(x,y)!=0xFF000000)
      }
    }
    gRamses.dispose();

    csv.setText(str.toString());

    if (messaggio != null) {
      ramsesAndMaskKey = new BufferedImage(wIm, hIm, BufferedImage.TYPE_INT_ARGB);
      ramsesAndMaskKey.createGraphics().drawImage(ramses, 0, 0, null);
      ramsesAndMaskKey.createGraphics().drawImage(ramsesMaskKey, 0, 0, null);
    }
  }
  //FINE RAMSES
}
