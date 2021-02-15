package it.unict.dmi.PIM;

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
import javax.swing.JProgressBar;
import javax.swing.JTextArea;

public class PIMAlgorithm {

  private BufferedImage image;
  private BufferedImage edge;
  private BufferedImage dilatedEdge;
  private BufferedImage pim;
  private BufferedImage pimAlphaMask;
  private int wIm;
  private int hIm;

  private JProgressBar bar;
  private JTextArea text, csv;
  private int experimentCount;
  private AntipoleTree imagesATF;
  private final TreeMap<File, Integer> map = new TreeMap<>();

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

  public BufferedImage getPIM() {
    return pim;
  }

  public BufferedImage getMask() {
    return pimAlphaMask;
  }

  public AntipoleTree getAntipoleTree() {
    return imagesATF;
  }

  public void setProgressBar(JProgressBar bar) {
    this.bar = bar;
  }

  public void setTextAreas(JTextArea text, JTextArea csv) {
    this.text = text;
    this.csv = csv;
  }

  public void evaluate(int side, boolean doAntipole, boolean colorize, boolean mask, boolean uniformDistribution, File dir) {
    wIm = image.getWidth();
    hIm = image.getHeight();

    long startAntipole = System.currentTimeMillis();
    if (doAntipole) {
      if (bar != null) {
        bar.setString("Reading Database...");
      }
      this.createAntipoleTree(dir);
      System.gc();
    }
    long stopAntipole = System.currentTimeMillis();

    long startEdge = System.currentTimeMillis();
    if (edge == null || edge.getWidth() != wIm || edge.getHeight() != hIm) {
      edge = GuidelineDetector.evaluate(image, bar);
    }
    dilatedEdge = MorphologicalOperation.dilate(edge, false, 3);
    dilatedEdge.createGraphics().drawImage(VoronoiAlgorithm.evaluate(wIm, hIm, side, true, bar), 0, 0, null);
    long stopEdge = System.currentTimeMillis();

    //START ELABORATION
    long start = System.currentTimeMillis();
    //PIM
    if (bar != null) {
      bar.setString("Puzzle Image Mosaic...");
    }
    this.PIM(colorize, uniformDistribution, mask);

    if (bar != null) {
      bar.setValue(0);
      bar.setString("Finished!!!");
    }

    long stop = System.currentTimeMillis();
    //STOP ELABORATION

    NumberFormat format = NumberFormat.getNumberInstance();
    format.setMinimumFractionDigits(3);
    format.setMaximumFractionDigits(3);
    experimentCount++;
    if (text != null) {
      text.insert("--------------------------------\n", 0);
      text.insert("\tTotal Elapsed Time                       = " + format.format((stopAntipole - startAntipole + stopEdge - startEdge + stop - start) / 1000.0) + " seconds\n", 0);
      text.insert("\tElapsed Time for Antipole Clustering     = " + format.format((stopAntipole - startAntipole) / 1000.0) + " seconds\n", 0);
      text.insert("\tElapsed Time for Guideline Detection     = " + format.format((stopEdge - startEdge) / 1000.0) + " seconds\n", 0);
      text.insert("\tElapsed Time for PIM Creation            = " + format.format((stop - start) / 1000.0) + " seconds\n", 0);
      text.insert("Results\n", 0);
      text.insert("\tSide                                     = " + side + "\n", 0);
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

  private void createAntipoleTree(File dir) {
    if (dir.isDirectory()) {
      File[] file = dir.listFiles();
      ArrayList<Shape> list = new ArrayList<>();
      for (int i = 0; i < file.length; i++) {
        if (bar != null & i % 5 == 0) {
          bar.setValue(100 * i / file.length);
        }
        BufferedImage buffer = null;
        try {
          buffer = ImageIO.read(file[i]);
        } catch (IOException io) {
        }
        if (buffer != null) {
          list.add(new Shape(buffer, file[i]));
        }
      }
      Shape[] tiles = new Shape[list.size()];
      list.toArray(tiles);
      imagesATF = new AntipoleTree(tiles, 0.2);
    } else
      try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(dir))) {
      imagesATF = (AntipoleTree) in.readObject();
    } catch (IOException | ClassNotFoundException e) {
    }
  }

  @SuppressWarnings({"UnusedAssignment", "null"})
  private void PIM(boolean colorize, boolean uniform, boolean mask) {
    StringBuilder str = new StringBuilder("Tile file name;X region center;Y region center;Angle of rotation;Scale factor;X image center;Y image center;Red;Green;Blue\n");
    pim = new BufferedImage(wIm, hIm, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = pim.createGraphics();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    g2.setPaint(Color.black);
    g2.fillRect(0, 0, wIm, hIm);

    Graphics2D gMask = null;
    if (mask) {
      pimAlphaMask = new BufferedImage(wIm, hIm, BufferedImage.TYPE_INT_ARGB);
      gMask = pimAlphaMask.createGraphics();
      gMask.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      gMask.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }

    for (int y = 0; y < hIm; y++) {
      for (int x = 0; x < wIm; x++) {
        if (dilatedEdge.getRGB(x, y) != 0xFF000000) {
          Shape shape = new Shape(image, dilatedEdge, x, y);
          if (shape.getScaleFactor() != 0) {
            ElementList cluster = imagesATF.nearestElementSearch(shape, false);
            Shape nearest = null;
            Iterator<ElementNode> iter = cluster.iterator();

            if (uniform) {
              int nearestCounter = Integer.MAX_VALUE;
              File nearestPath = null;

              while (iter.hasNext()) {
                Shape shapeTest = (Shape) iter.next().getKey();
                int counter = 0;
                File path = shapeTest.getPath();

                if (!map.containsKey(path)) {
                  map.put(path, 0);
                } else {
                  counter = map.get(path);
                }

                if (counter < nearestCounter && shapeTest.getScaleFactor() != 0) {
                  nearest = shapeTest;
                  nearestCounter = counter;
                  nearestPath = path;
                }
              }

              map.put(nearestPath, nearestCounter + 1);
            } else {
              double nearestDistance = Double.MAX_VALUE;

              while (iter.hasNext()) {
                Shape shapeTest = (Shape) iter.next().getKey();
                double d = shapeTest.distance(shape);

                if (d < nearestDistance && shapeTest.getScaleFactor() != 0) {
                  nearest = shapeTest;
                  nearestDistance = d;
                }
              }
            }

            int[] colorShape = shape.getColor();
            float[] hsbShape = new float[3];
            Color.RGBtoHSB(colorShape[0], colorShape[1], colorShape[2], hsbShape);

            @SuppressWarnings("null")
            int[] colorNearest = nearest.getColor();
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
            File file = nearest.getPath();
            try {
              if (csv != null) {
                str.append(file.getCanonicalPath()).append(";");
              }
              buffer = ImageIO.read(file);
            } catch (IOException e) {
            }

            if (buffer != null) {
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
              g2.drawImage(buffer, tx, null);
              if (mask) {
                gMask.drawImage(buffer, tx, null);
              }
            }
          }

          int c = y * wIm + x;
          if (bar != null && c % 5 == 0) {
            bar.setValue(100 * c / (wIm * hIm));
          }
        }
      }
    }

    if (mask) {
      if (bar != null) {
        bar.setString("Creating Alpha Mask Image...");
      }

      for (int y = 0; y < hIm; y++) {
        for (int x = 0; x < wIm; x++) {
          int cc = y * wIm + x;
          if (bar != null && cc % 5 == 0) {
            bar.setValue(100 * cc / (wIm * hIm));
          }

          int c = pimAlphaMask.getRGB(x, y);
          if ((c & 0xFF000000) == 0) {
            c = 0xFF000000;
          } else {
            c = 0xFFFFFFFF;
          }
          pimAlphaMask.setRGB(x, y, c);
        }
      }
      gMask.dispose();
    }

    g2.dispose();
    csv.setText(str.toString());
  }
}
