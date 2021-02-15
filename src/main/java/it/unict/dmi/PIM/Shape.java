package it.unict.dmi.PIM;

import it.unict.dmi.antipole.Element;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Stack;

public class Shape implements Element {

  private static final long serialVersionUID = 1L;

  private double[] ro;
  private double scaleFactor;
  private File file;
  private double xCenter, yCenter;
  private final static int DIVISION = 90;
  private int bestShift;
  private int rMean, gMean, bMean;

  private final static int[] ONES = {128, 64, 32, 16, 8, 4, 2, 1};

  public Shape(BufferedImage image, File f) {
    file = f;
    int w = image.getWidth();
    int h = image.getHeight();
    byte[] matrix = new byte[w * h / 8 + 1];
    int count = 0;
    for (int x = 0; x < w; x++) {
      for (int y = 0; y < h; y++) {
        int color = image.getRGB(x, y);
        if ((color & 0xFF000000) != 0) {
          set(matrix, y * w + x);
          rMean += (color >> 16) & 0xFF;
          gMean += (color >> 8) & 0xFF;
          bMean += color & 0xFF;
          count++;
        }
      }
    }
    rMean /= count;
    gMean /= count;
    bMean /= count;
    this.createRo(this.convertMatrixInArea(matrix, w, h));
  }

  public Shape(BufferedImage image, BufferedImage edges, int x, int y) {
    int w = image.getWidth();
    int h = image.getHeight();
    byte[] matrix = new byte[w * h / 8 + 1];
    this.find(image, edges, x, y, matrix);
    this.createRo(this.convertMatrixInArea(matrix, w, h));
  }

  public double getScaleFactor() {
    return scaleFactor;
  }

  public File getPath() {
    return file;
  }

  public Point2D getCenter() {
    return new Point2D.Double(xCenter, yCenter);
  }

  public int bestShift() {
    return bestShift;
  }

  public static double getRotationAngle(int shift) {
    return 2 * Math.PI * shift / DIVISION;
  }

  public int[] getColor() {
    return new int[]{rMean, gMean, bMean};
  }

  @Override
  public double distance(Element e) {
    Shape shape = (Shape) e;
    double distance = Double.MAX_VALUE;
    bestShift = 0;

    for (int shift = 0; shift < DIVISION; shift++) {
      double d = 0;
      for (int i = 0; i < DIVISION; i++) {
        double diff = ro[i] - shape.ro[(shift + i) % DIVISION];
        d += diff * diff;
      }

      if (d < distance) {
        distance = d;
        bestShift = shift;
      }
    }

    return Math.sqrt(distance);
  }

  @SuppressWarnings("LocalVariableHidesMemberVariable")
  private Point2D evaluateCenter(Area area) {
    Rectangle rect = area.getBounds();
    int count = 0;
    double xCenter = 0;
    double yCenter = 0;
    int xx = rect.x + rect.width;
    int yy = rect.y + rect.height;
    for (int x = rect.x; x < xx; x++) {
      for (int y = rect.y; y < yy; y++) {
        if (area.contains(x, y)) {
          xCenter += x;
          yCenter += y;
          count++;
        }
      }
    }
    return new Point2D.Double(xCenter / count, yCenter / count);
  }

  private void find(BufferedImage image, BufferedImage edges, int x, int y, byte[] matrix) {
    int w = image.getWidth();
    int h = image.getHeight();
    byte b = 0;
    Arrays.fill(matrix, b);
    Stack<int[]> stack = new Stack<>();
    int[] values = new int[]{x, y, -1};
    stack.push(values);

    // 0=left
    // 1=right
    // 2=top
    // 3=down
    int count = 0;

    while (!stack.isEmpty()) {
      values = stack.pop();
      x = values[0];
      y = values[1];
      int noDirection = values[2];
      if (edges.getRGB(x, y) != 0xFF000000) {
        edges.setRGB(x, y, 0xFF000000);
        int coord = y * w + x;
        set(matrix, coord);
        count++;
        int color = image.getRGB(x, y);
        rMean += (color >> 16) & 0xFF;
        gMean += (color >> 8) & 0xFF;
        bMean += color & 0xFF;
        if ((noDirection != 0) && x > 0 && (!is(matrix, coord - 1))) {
          stack.push(new int[]{x - 1, y, 1});
        }
        if ((noDirection != 1) && x < w - 1 && (!is(matrix, coord + 1))) {
          stack.push(new int[]{x + 1, y, 0});
        }
        if ((noDirection != 2) && y > 0 && (!is(matrix, coord - w))) {
          stack.push(new int[]{x, y - 1, 3});
        }
        if ((noDirection != 3) && y < h - 1 && (!is(matrix, coord + w))) {
          stack.push(new int[]{x, y + 1, 2});
        }
      }
    }
    rMean /= count;
    gMean /= count;
    bMean /= count;
  }

  private Area convertMatrixInArea(byte[] matrix, int w, int h) {
    Area area = new Area();
    for (int xxx = 0; xxx < w; xxx++) {
      for (int yyy = 0; yyy < h; yyy++) {
        int c = yyy * w + xxx;
        if (is(matrix, c)) {
          int ww = 0;
          while (xxx + ww < w && is(matrix, c + ww)) {
            reset(matrix, c + ww);
            ww++;
          }
          area.add(new Area(new Rectangle(xxx, yyy, ww, 1)));
        }
      }
    }
    return area;
  }

  private void createRo(Area area) {
    Point2D center = this.evaluateCenter(area);
    xCenter = center.getX();
    yCenter = center.getY();

    ro = new double[DIVISION];
    scaleFactor = 0;

    for (int i = 0; i < ro.length; i++) {
      double angle = 2 * Math.PI * i / DIVISION;
      double cos = Math.cos(angle);
      double sin = Math.sin(angle);
      double xx = xCenter;
      double yy = yCenter;
      while (area.contains(xx, yy)) {
        xx += cos;
        yy += sin;
      }
      ro[i] = center.distance(xx, yy);
      if (ro[i] > scaleFactor) {
        scaleFactor = ro[i];
      }
    }

    for (int i = 0; i < ro.length; i++) {
      ro[i] /= scaleFactor;
    }
  }

  private void set(byte[] matrix, int pos) {
    matrix[pos / 8] |= Shape.ONES[pos % 8];
  }

  private boolean is(byte[] matrix, int pos) {
    return (matrix[pos / 8] & Shape.ONES[pos % 8]) != 0;
  }

  private void reset(byte[] matrix, int pos) {
    matrix[pos / 8] &= ~Shape.ONES[pos % 8];
  }
}
