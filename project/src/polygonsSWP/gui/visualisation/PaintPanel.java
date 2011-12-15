package polygonsSWP.gui.visualisation;

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
  private Polygon polygon;
  private List<Point> points;

  private double zoom = 1.0d;
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

  /* API */

  void setPolygon(Polygon p) {
    polygon = p;
    repaint();
  }

  void setDrawMode(boolean d, List<Point> p) {
    drawMode = d;
    points = p;
  }

  void resetView() {
    zoom = 1.0f;
    offsetX = 0;
    offsetY = 0;
    repaint();
  }

  /* Painting */

  @Override
  public void paintComponent(Graphics g) {
    g.setColor(Color.WHITE);
    g.fillRect(0, 0, 1000, 1000);

    // Paint the polygon
    if (polygon != null) {
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

      g.setColor(Color.GREEN);
      for (Point p : points) {
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

      long x = (long) ((e.getX() - offsetX) / zoom);
      long y = (long) ((e.getY() - offsetY) / zoom);
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
        
        if(nz < 0.1)
          nz = 0.1;
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
