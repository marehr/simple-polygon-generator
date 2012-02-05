package polygonsSWP.gui.visualisation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

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
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
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
    
//    Point p = null;
//    if((p = checkForPoints()) != null)
//    {
//    	
//    }
    // Commented out as we may need this again (while hovering over a point)
    /*
    int y = mouse.y+30 < getHeight() ? mouse.y+30 : mouse.y-10;
    g.drawString("[" + (int)coords[0] + " - " + (int)coords[1] + "]", mouse.x-30, y);
    */
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
      g.setColor(new Color(80, 0, 90));
      for (Point p : points) {
        g2d.draw(new Ellipse2D.Double(p.x - 1.5, p.y - 1.5, 3, 3));
      }
    }

    // Paint svgScene Points
    if (svgScene != null) {
      g2d.setStroke(new TransformedStroke(new BasicStroke(2.5f), tx));
      svgScene.paintPoints(g2d);
    }
    
    if(pointInRange != null)
    {
    	g.setColor(Color.PINK);
    	g.fillOval((int) (pointInRange.x - 1.5), (int) (pointInRange.y - 1.5), 3, 3);
    	g.drawString("[" + pointInRange.x + "|" + pointInRange.y + "]",(int)(pointInRange.x),(int)(pointInRange.y));
    }

    // Draw additional stuff.
    finishCanvas(g2d);
  }
  
  private Point getPointInRange() {
	  HistoryScene s = (HistoryScene) svgScene;
	  if(s != null)
	  {
		  LinkedList<Box<Polygon>> list = s.getPointList();
		  for(Box<Polygon> box : list)
		  {
			  Polygon poly = box.openBox();
			  for(Point poi : poly.getPoints())
			  {
				  double distance = currentMousePoint.distanceTo(poi);
				  //System.out.println(distance);
				  if(distance <= 15.0)
					  return poi;  
			  }


		  }		  
	  }
	return null;
  }

/**
   * Translates (x,y) coordinates on screen into double (x,y) coordinates
   * in the polygon plane.
   * 
   * @param x the x-coordinate on screen as returned by event.getX()
   * @param y dto.
   * @return translated coordinates
   */
  private double[] coords(int x, int y){
    return new double[] {
      (x - offsetX) / zoom,
      (getHeight() - y + offsetY) / zoom
    };
  }

  /*
   * MouseListener methods. Used for zooming and manually setting points.
   */

  @Override
  public void mouseClicked(MouseEvent e) {
    // Set point if in draw mode and right mouse button clicked.
    if (drawMode && e.getButton() == MouseEvent.BUTTON3) {
      assert (points != null);

      double coords[] = coords(e.getX(), e.getY());
      Point newPoint = new Point(coords[0], coords[1]);

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
	  if(inFrame)
	  {
		  double[] coords = coords(e.getX(), e.getY());
		  if(currentMousePoint != null)
		  {
			  currentMousePoint.x = coords[0];
			  currentMousePoint.y = coords[1];			  
		  }
		  else
		  {
			  currentMousePoint = new Point(coords[0],coords[1]);
		  }
		  
		  pointInRange = getPointInRange();
		  if(pointInRange != null)
		  {
			  System.out.println("THERE IS A POINT " + pointInRange.x + " " + pointInRange.y);
			  repaint();
		  }			  
		  
		  statusbar.setStatusMsg("[" + (int)coords[0] + " - " + (int)coords[1] + "]");		  
	  }
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
