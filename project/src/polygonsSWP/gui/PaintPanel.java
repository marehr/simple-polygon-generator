package polygonsSWP.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.List;

import javax.swing.JPanel;

import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;

/**
 * Component for drawing our geometry objects.
 * This one features drawing a polygon as well as points
 * plus zooming and dragging stuff around.
 * 
 * @author Sebastian Thobe <sebastianthobe@googlemail.com>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 */
public class PaintPanel
  extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener
{
  private static final long serialVersionUID = 1L;
  private Polygon polygon;
  private List<Point> points;
  
  private float zoom = 1.0f;
  private int offsetX = 0;
  private int offsetY = 0;
  private int dragOffsetX = -1;
  private int dragOffsetY = -1;
  
  private boolean drawMode;
  
  public PaintPanel() {
    addMouseListener(this);
    addMouseMotionListener(this);
    addMouseWheelListener(this);
  }
  
  public void setPolygon(Polygon polygon) {
    this.polygon = polygon;
    repaint();
  }
  
  public Polygon getPolygon() {
    return polygon;
  }
  
  public void setPoints(List<Point> points) {
    this.points = points;
    repaint();
  }
  
  public List<Point> getPoints() {
    return points;
  }
  
  public void setDrawMode(boolean drawMode) {
    this.drawMode = drawMode;
  }
  
  /* Painting */
  
  @Override
  public void paintComponent(Graphics g) {
    g.clearRect(0, 0, 1000, 1000);

    // Paint the polygon
    if (polygon != null) {
      List<Point> p = polygon.getPoints();

      int[] xcoords = new int[p.size()];
      int[] ycoords = new int[p.size()];
      for (int i = 0; i < p.size(); i++) {
        xcoords[i] = (int) (p.get(i).x * zoom + offsetX);
        ycoords[i] = (int) (p.get(i).y * zoom + offsetY);
      }
      g.drawPolygon(xcoords, ycoords, p.size());
    }
    
    // Paint the points
    if(points != null) {
      g.setColor(Color.GREEN);
      for(Point p : points) {
        g.drawOval((int) (p.x * zoom + offsetX),
            (int) (p.y * zoom + offsetY), 3, 3);
      }
    }
  }
  
  /* 
   * MouseListener methods. Used for zooming and 
   * manually setting points. 
   */
  
  @Override
  public void mouseClicked(MouseEvent e) {  
    // Set point if in draw mode and left mouse button clicked.
    if(drawMode && e.getButton() == MouseEvent.BUTTON3) {
      assert(points != null);
      
      long x = (long) ((e.getX() - offsetX) / zoom);
      long y = (long) ((e.getY() - offsetY) / zoom);
      points.add(new Point(x, y));
      
      repaint();
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
   *  MouseMotionListener methods. Used for dragging stuff around. 
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
    if(e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
      /*
       * Remark:  We're ignoring platform default values 
       * here (e.g. e.getScrollAmount()).
       */
      if(e.getWheelRotation() < 0) {
        // Zoom in
        zoom += 0.1f;
      } else {
        if(zoom > 0.1f)
          zoom -= 0.1f;
      }
      
      repaint();
    }
  }
}
