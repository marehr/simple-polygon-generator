package polygonsSWP.gui.visualisation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

import polygonsSWP.data.Scene;
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
  implements MouseListener, MouseMotionListener, MouseWheelListener, VisualisationControlListener
{
  private static final long serialVersionUID = 1L;
  private java.awt.Point mouse = null;
  private boolean inFrame = false;
  private final DecimalFormat df = new DecimalFormat("#0.00");

  /** list for point selection */
  private List<Point> points;

  /** SVG scene from history object. */
  private Scene svgScene;
  
  /** Our own scene objects */
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

  protected void initCanvas(Graphics2D g) {
    // Clear panel
    g.setColor(Color.WHITE);
    g.fillRect(0, 0, getWidth(), getHeight());
    
    // Paint the yardstick
    g.setColor(Color.BLUE);
    g.drawRect(5, 5, 3, 9);
    g.drawRect(8, 8, 44, 3);
    g.drawRect(52, 5, 3, 9);
    g.drawString(df.format(50 / zoom), 60, 14);
        
    // Set translation & scale.
    AffineTransform tx = new AffineTransform();
    tx.translate(offsetX, offsetY);
    tx.scale(zoom, zoom);
    g.setTransform(tx);
  }

  @Override
  public void paintComponent(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;
    
    initCanvas(g2d);

    // Paint svgScene.
    if(svgScene != null) {
      svgScene.paint(g2d);
    }

    // Paint the points
    if (drawMode && points != null) {
      assert (points != null);

      g.setColor(new Color(80, 0, 90));
      for (Point p : points) {
        //g.drawOval((int) (p.x - 2), (int) (p.y - 2), 5, 5);
        //g.drawRect((int)p.x, (int)p.y, 1, 1);
        g.drawOval((int) (p.x - 1.5), (int) (p.y - 1.5), 3, 3);
      }
    }

    // Paint svgScene Points
    if(svgScene != null) {
      svgScene.paintPoints(g2d);
    }

//    // Paint polygons
//    for (Polygon polygon : polygons) {
//      List<Point> p = polygon.getPoints();
//      int[] xcoords = new int[p.size()];
//      int[] ycoords = new int[p.size()];
//      for (int i = 0; i < p.size(); i++) {
//        xcoords[i] = (int) p.get(i).x;
//        ycoords[i] = (int) p.get(i).y;
//      }
//
//      g.setColor(Color.BLACK);
//      g.drawPolygon(xcoords, ycoords, p.size());
//    }
    
    if(mouse != null)
    {
    	//TODO: display correct values with zoom
    	g.drawString("[" + mouse.x + " - " + mouse.y + "]", mouse.x-30, mouse.y+30);
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
	  inFrame = true;
  }

  @Override
  public void mouseExited(MouseEvent e) {
	  inFrame = false;
	  mouse = null;
	  repaint();
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
	  mouse = e.getPoint();
	  repaint();
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

  /* VisualisationControlListener methods. */
  
  @Override
  public void onNewScene(Scene scene) {
    svgScene = scene;
    repaint();
  }
   
}
