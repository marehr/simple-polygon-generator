package polygonsSWP.gui.visualisation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;


/**
 * Component for drawing our geometry objects. This one features drawing a
 * polygon as well as points plus zooming and dragging stuff around.
 * 
 * @author Sebastian Thobe <sebastianthobe@googlemail.com>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 */
class PaintPanel
  extends JPanel
  implements MouseListener, MouseMotionListener, MouseWheelListener
{
  private static final long serialVersionUID = 1L;
  private final DecimalFormat df = new DecimalFormat("#0.00");

  /** list for point selection */
  private List<Point> points;

  /** Scene objects */
  private List<Polygon> polygons;

  /* current display offsets & co. */
  protected double zoom = 1.0d;
  protected int offsetX = 0;
  protected int offsetY = 0;
  private int dragOffsetX = -1;
  private int dragOffsetY = -1;

  /** DrawMode indicates whether we're allowed to select points. */
  protected boolean drawMode;

  public PaintPanel() {
    polygons = new LinkedList<Polygon>();
    addMouseListener(this);
    addMouseMotionListener(this);
    addMouseWheelListener(this);
  }

  /* API */

  void clearScene() {
    polygons.clear();
  }

  void addPolygon(Polygon p) {
    polygons.add(p);
    repaint();
  }

  void addPolygons(List<? extends Polygon> ps) {
    polygons.addAll(ps);
    repaint();
  }

  void setDrawMode(boolean d, List<Point> p) {
    drawMode = d;
    points = p;
    repaint();
  }

  void resetView() {
    zoom = 1.0f;
    offsetX = 0;
    offsetY = 0;
    repaint();
  }

  /* Painting */

  protected void initPanel(Graphics g) {
    // Clear panel
    g.setColor(Color.WHITE);
    g.fillRect(0, 0, getWidth(), getHeight());

    // Paint the yardstick
    g.setColor(Color.BLUE);
    g.drawRect(5, 5, 3, 9);
    g.drawRect(8, 8, 44, 3);
    g.drawRect(52, 5, 3, 9);
    g.drawString(df.format(50 / zoom), 60, 14);
  }

  @Override
  public void paintComponent(Graphics g) {
    initPanel(g);

    // Paint polygons
    for (Polygon polygon : polygons) {
      List<Point> p = polygon.getPoints();
      int[] xcoords = new int[p.size()];
      int[] ycoords = new int[p.size()];
      for (int i = 0; i < p.size(); i++) {
        xcoords[i] = (int) (p.get(i).x * zoom + offsetX);
        ycoords[i] = (int) (p.get(i).y * zoom + offsetY);
      }

      g.setColor(Color.BLACK);
      g.drawPolygon(xcoords, ycoords, p.size());
    }

    // Paint the points
    if (drawMode) {
      assert (points != null);

      g.setColor(new Color(80, 0, 90));
      for (Point p : points) {
        g.drawOval((int) (p.x * zoom + offsetX) - 2,
            (int) (p.y * zoom + offsetY) - 2, 5, 5);
        g.drawOval((int) (p.x * zoom + offsetX) - 1,
            (int) (p.y * zoom + offsetY) - 1, 3, 3);
      }
    }
  }

  /*
   * MouseListener methods. Used for zooming and manually setting points.
   */

  @Override
  public void mouseClicked(MouseEvent e) {
    // Set point if in draw mode and right mouse button clicked.
    if (drawMode && e.getButton() == MouseEvent.BUTTON3) {
      assert (points != null);

      double x = (e.getX() - offsetX) / zoom;
      double y = (e.getY() - offsetY) / zoom;
      points.add(new Point(x, y));

      repaint();
    }
    else if (e.getButton() == MouseEvent.BUTTON2) {
      // Reset view on middle button click
      resetView();
    }
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }

  @Override
  public void mousePressed(MouseEvent e) {
    dragOffsetX = offsetX - e.getX();
    dragOffsetY = offsetY - e.getY();
  }

  @Override
  public void mouseReleased(MouseEvent e) {
  }

  /*
   * MouseMotionListener methods. Used for dragging stuff around.
   */

  @Override
  public void mouseDragged(MouseEvent e) {
    offsetX = e.getX() + dragOffsetX;
    offsetY = e.getY() + dragOffsetY;
    repaint();
  }

  @Override
  public void mouseMoved(MouseEvent e) {
  }

  /*
   * MouseWheelListener methods. Used for zooming.
   */

  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {
    if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
      /*
       * Remark: We're ignoring platform default values here (e.g.
       * e.getScrollAmount()).
       */
      final double b = 0.1;

      double nz = -1;
      if (e.getWheelRotation() < 0) {
        // Zoom in
        nz = Math.exp(b * (Math.log(zoom) / b + 1));
      }
      else {
        nz = Math.exp(b * (Math.log(zoom) / b - 1));

        if (nz < 0.1) nz = 0.1;
      }

      if (nz != -1) {
        offsetX = (int) (e.getX() - (e.getX() - offsetX) / zoom * nz);
        offsetY = (int) (e.getY() - (e.getY() - offsetY) / zoom * nz);
        zoom = nz;
        repaint();
      }
    }
  }
}
