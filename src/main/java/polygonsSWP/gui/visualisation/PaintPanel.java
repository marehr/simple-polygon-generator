package polygonsSWP.gui.visualisation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JPanel;

import polygonsSWP.data.HistoryScene.*;
import polygonsSWP.data.HistoryScene;
import polygonsSWP.data.Scene;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;

/**
 * Component for drawing our geometry objects. This one features drawing a
 * polygon as well as points plus zooming and dragging stuff around.
 * 
 * (c) 2011-2012
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Kadir Tugan <kadir.tugan@gmail.com>
 */
class PaintPanel
  extends JPanel
  implements MouseListener, MouseMotionListener, MouseWheelListener,
  VisualisationControlListener
{
  private static final long serialVersionUID = 1L;
  private boolean inFrame = false;
  private boolean inGenerationMode = true;
  private Point currentMousePoint = null;
  private Point pointInRange = null;

  private final DecimalFormat df = new DecimalFormat("#0.00");
  private final AffineTransform tx = new AffineTransform();

  /** list for point selection */
  private List<Point> points;

  /** SVG scene from history object. */
  private Scene svgScene;

  /** the current polygon (used for point-in-polygon tests) */
  private Polygon polygon;

  /** Reference to our status bar. */
  private final PaintPanelStatusBar statusbar;
  
  /* current display offsets & co. */
  protected double zoom = 1.0d;
  protected int offsetX = 0;
  protected int offsetY = 0;
  private int dragOffsetX = -1;
  private int dragOffsetY = -1;

  /** DrawMode indicates whether we're allowed to select points. */
  protected boolean drawMode;

  public PaintPanel(PaintPanelStatusBar ppsb) {
    statusbar = ppsb;
    addMouseListener(this);
    addMouseMotionListener(this);
    addMouseWheelListener(this);
  }

  /* API */

  void setCurrentPolygon(Polygon p) {
    polygon = p;

    // show points on random polygons
    if(points == null){
      setDrawMode(drawMode, p.getPoints());
    }
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
  
  void setGUIinGenerationMode(boolean b) {
    inGenerationMode = b;
  }
  
  /* Painting */

  protected void initCanvas(Graphics2D g) {
    // Clear panel
    g.setColor(Color.WHITE);
    g.fillRect(0, 0, getWidth(), getHeight());

    // Set translation & scale.
    tx.translate(offsetX, getHeight() + offsetY);
    tx.scale(zoom, -zoom);

    g.setTransform(tx);
    g.setStroke(new TransformedStroke(new BasicStroke(1f), tx));
  }

  protected void finishCanvas(Graphics2D g) {
    // Reset translation & scale.
    tx.setToIdentity();
    g.setTransform(tx);
    g.setStroke(new BasicStroke(1f));

    // Paint the yardstick
    g.setColor(Color.BLUE);
    g.drawRect(5, 5, 3, 9);
    g.drawRect(8, 8, 44, 3);
    g.drawRect(52, 5, 3, 9);
    g.drawString(df.format(50 / zoom), 60, 14);
  }

  @Override
  public void paintComponent(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;

    initCanvas(g2d);

    // Paint svgScene.
    if (svgScene != null) {
      svgScene.paint(g2d);
    }

    // Paint the points
    if (points != null) {
      AffineTransform origin = new AffineTransform(tx);

      tx.setToIdentity();
      g2d.setTransform(tx);

      g2d.setColor(new Color(80, 0, 90));
      for (Point p : points) {
        Point2D p1 = screenCoords(new Point2D.Double(p.x, p.y), origin);
        g2d.fill(new Ellipse2D.Double(p1.getX() - 2, p1.getY() - 2, 4, 4));
      }

      tx.setTransform(origin);
      g2d.setTransform(tx);
    }

    // Paint svgScene Points
    if (svgScene != null) {
      g2d.setStroke(new TransformedStroke(new BasicStroke(2.5f), tx));
      svgScene.paintPoints(g2d);
    }

    if(pointInRange != null)
    {
      double x = pointInRange.x, y = pointInRange.y;
      Point2D p = screenCoords(new Point2D.Double(x, y), tx);

      tx.setToIdentity();
      g2d.setTransform(tx);

      g2d.setColor(Color.RED);
      g2d.fill(new Ellipse2D.Double(p.getX() - 2, p.getY() - 2, 4, 4));

      g2d.setColor(new Color(0, 26, 51));
      g2d.drawString("[" + df.format(x) + "|" + df.format(y) + "]", (int)p.getX()+5, (int)p.getY()-5);
    }

    // Draw additional stuff.
    finishCanvas(g2d);
  }
  
  private Point getPointInRange(Point currentMousePoint) {
    HistoryScene s = (HistoryScene) svgScene;
    if(s == null) return null;

    double minDistance = Double.MAX_VALUE;
    Point minPoint = null;
    LinkedList<Box<Polygon>> list = s.getPointList();

    for(Box<Polygon> box : list) {
      Polygon poly = box.openBox();
      for(Point poi : poly.getPoints()) {
        double distance = currentMousePoint.distanceTo(poi);
        if(distance <= 15 && distance < minDistance){
          minDistance = distance;
          minPoint = poi;
        }
      }
    }

    return minPoint;
  }

/**
   * Translates (x,y) coordinates on screen into double (x,y) coordinates
   * in the polygon plane.
   * 
   * @param x the x-coordinate on screen as returned by event.getX()
   * @param y dto.
   * @return translated coordinates
   */
  private Point coords(int x, int y){
    return new Point((x - offsetX) / zoom,
        (getHeight() - y + offsetY) / zoom);
  }

  /**
   * Translates (x,y) coordinates in the polygon plane into double (x,y)
   * coordinates on screen.
   * 
   * @param x the x-coordinate in polygon plane as returned by all polygon
   * algorithms
   * @param y dto.
   * @return translated coordinates
   */
  public Point2D screenCoords(Point2D base, AffineTransform tx){
    Point2D p = new Point2D.Double(0, 0);
    p = tx.deltaTransform(base, p);
    return new Point2D.Double(p.getX() + offsetX, p.getY()+ getHeight() + offsetY);
  }

  /*
   * MouseListener methods. Used for zooming and manually setting points.
   */

  @Override
  public void mouseClicked(MouseEvent e) {
    // Set point if in draw mode and right mouse button clicked.
    if (drawMode && e.getButton() == MouseEvent.BUTTON3) {
      assert (points != null);

      Point newPoint = coords(e.getX(), e.getY());

      if(inGenerationMode) {
        points.add(newPoint);
      } else {

        if(polygon.containsPoint(newPoint, true)) {
          if(points.size() == 2) {
            points.set(0, points.get(1));
            points.set(1, newPoint);
          } else {
            points.add(newPoint);
          }
        }
      }
      repaint();
    }
    else if (e.getButton() == MouseEvent.BUTTON2) {
      // Reset view on middle button click
      resetView();
    }
  }

  @Override
  public void mouseEntered(MouseEvent e) {
	  inFrame = true;
  }

  @Override
  public void mouseExited(MouseEvent e) {
	  inFrame = false;
	  statusbar.setStatusMsg("");
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
    if(!inFrame) return;

    currentMousePoint = coords(e.getX(), e.getY());

    pointInRange = getPointInRange(currentMousePoint);
    if(pointInRange != null)
      repaint();

    statusbar.setStatusMsg("[" + (int)currentMousePoint.x + " - " + 
        (int)currentMousePoint.y + "]");
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
        int x = e.getX(), y = getHeight() - e.getY();

        offsetX = (int) (x - (x - offsetX) / zoom * nz);
        offsetY = (int) (-y + (y + offsetY) / zoom * nz);
        zoom = nz;
        repaint();
      }
    }
  }

  /* 
   * VisualisationControlListener methods. 
   */

  @Override
  public void onNewScene(Scene scene) {
    svgScene = scene;
    repaint();
  }
}
