package polygonsSWP.data;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import polygonsSWP.geometry.Circle;
import polygonsSWP.geometry.Line;
import polygonsSWP.geometry.LineSegment;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.geometry.Ray;


/**
 * The whole idea is: you add arbitrary geometry objects to the scene and they
 * are painted to a SVG in the order polygon -> line -> linesegment -> ray ->
 * point. So from bit to small. Not highlighted objects are painted black,
 * higlighted objects are painted according to their class.
 * 
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 */

public class HistoryScene
  implements Scene
{
  /**
   * Inner class to put highlighted and highlighted object in one box.
   * 
   * @author Steve Dierker <dierker.steve@fu-berlin.de>
   * @param <T> Specifies which type is boxed in the box.
   */
  private class Box<T>
  {
    private boolean _highlight;
    private T _object;

    public Box(T object, boolean highlight) {
      _highlight = highlight;
      _object = object;
    }

    public boolean isHighlighted() {
      return _highlight;
    }

    public T openBox() {
      return _object;
    }
  }

  private LinkedList<Box<Polygon>> _polyList;
  private LinkedList<Box<Line>> _lineList;
  private LinkedList<Box<LineSegment>> _lineSegmentList;
  private LinkedList<Box<Ray>> _rayList;
  private LinkedList<Box<Point>> _pointList;
  private History _history = null;
  private SVGGraphics2D svg;
  private HistoryScene _self;
  private Polygon _boundingBox;

  /**
   * Initializes the scene and bind it to the history object.
   * 
   * @param history
   */
  public HistoryScene(History history) {
    _polyList = new LinkedList<Box<Polygon>>();
    _lineList = new LinkedList<Box<Line>>();
    _lineSegmentList = new LinkedList<Box<LineSegment>>();
    _rayList = new LinkedList<Box<Ray>>();
    _pointList = new LinkedList<Box<Point>>();
    _history = history;
    _self = this;
  }

  /**
   * Generates SVG from current state and add the scene to the history object.
   */
  @Override
  public void safe() {
    this.toSvg();
    _history.addScene(this);
  }

  /**
   * Doodling.
   * 
   * @param g2d
   */
  public void paint(Graphics2D g2d) {
    
    // light blue for polygons
    Color polyColor = new Color(0xa2cdfd);
    // red for lines (rays, linesegment...)
    Color lineColor = new Color(0xae0000);
    // green for points
    Color pointColor = new Color(0x007426);
    
    // First of all draw bounding Box:
    g2d.setColor(Color.BLACK);
    if (_boundingBox instanceof OrderedListPolygon) {
      int[] xcoords = new int[_boundingBox.size()];
      int[] ycoords = new int[_boundingBox.size()];
      for (int i = 0; i < _boundingBox.size(); i++) {
        xcoords[i] = (int) _boundingBox.getPoints().get(i).x;
        ycoords[i] = (int) _boundingBox.getPoints().get(i).y;
      }

      g2d.setColor(Color.BLACK);
      g2d.drawPolygon(xcoords, ycoords, _boundingBox.size());
    }
    else {
      g2d.drawOval(0, 0, 
          (int) (2 * ((Circle) _boundingBox).getRadius()), 
          (int) (2 * ((Circle) _boundingBox).getRadius()));
    }

    // Afterwards every polygon:
    for (Box<Polygon> item : _polyList) {
      List<Point> p = item.openBox().getPoints();
      int[] xcoords = new int[p.size()];
      int[] ycoords = new int[p.size()];
      for (int i = 0; i < p.size(); i++) {
        xcoords[i] = (int) p.get(i).x;
        ycoords[i] = (int) p.get(i).y;
      }

      if (item.isHighlighted()) {
        g2d.setColor(polyColor);
        g2d.fillPolygon(xcoords, ycoords, p.size());
      }
      g2d.setColor(Color.BLACK);
      g2d.drawPolygon(xcoords, ycoords, p.size());
    }
    
    // Every Line
    for (Box<Line> item : _lineList) {
      if (item.isHighlighted()) g2d.setColor(lineColor);
      else g2d.setColor(Color.BLACK);
      // Calculate intersections to keep line inside of bounding box
      List<Point[]> returnList;
      returnList = _boundingBox.intersect(item.openBox());
      g2d.drawLine((int) returnList.get(0)[0].x,
          (int) returnList.get(0)[0].y,
          (int) returnList.get(1)[0].x,
          (int) returnList.get(1)[0].y);
    }
    // Every LineSegment
    for (Box<LineSegment> item : _lineSegmentList) {
      LineSegment tmp = item.openBox();
      if (item.isHighlighted()) g2d.setColor(lineColor);
      else g2d.setColor(Color.BLACK);
      // We assume all geometry elements are chosen to be inside the box if
      // they are not infinite to one side
      g2d.drawLine((int) tmp._a.x, (int) tmp._a.y,
          (int) tmp._b.x,
          (int) tmp._b.y);
    }
    
    // Every Ray
    for (Box<Ray> item : _rayList) {
      Ray tmp = item.openBox();
      if (item.isHighlighted()) g2d.setColor(lineColor);
      else g2d.setColor(Color.BLACK);
      // Calculate intersection
      List<Point[]> returnList;
      returnList = _boundingBox.intersect(item.openBox());
      if (returnList.size() == 1) {
        g2d.drawLine((int) tmp._base.x, (int) tmp._base.y, 
            (int) returnList.get(0)[0].x,
            (int) returnList.get(0)[0].y);
      }
      else {
        g2d.drawLine((int) returnList.get(0)[0].x,
            (int) returnList.get(0)[0].y,
            (int) returnList.get(1)[0].x,
            (int) returnList.get(1)[0].y);
      }
    }
    
    // Every Point
    for (Box<Point> item : _pointList) {
      Point tmp = item.openBox();
      if (item.isHighlighted()) g2d.setColor(pointColor);
      else g2d.setColor(Color.BLACK);
      g2d.drawLine((int) (tmp.x + 2), (int) (tmp.y + 2), 
          (int) (tmp.x - 2), (int) (tmp.y - 2));
      g2d.drawLine((int) (tmp.x - 2), (int) (tmp.y + 2), 
          (int) (tmp.x + 2), (int) (tmp.y - 2));
    }
  }

  @Override
  public Scene addPolygon(Polygon polygon, Boolean highlight) {
    _polyList.add(new Box<Polygon>(polygon.clone(), highlight));
    return _self;
  }

  @Override
  public Scene addLine(Line line, Boolean highlight) {
    _lineList.add(new Box<Line>(line.clone(), highlight));
    return _self;
  }

  @Override
  public Scene addLineSegment(LineSegment linesegment, Boolean highlight) {
    _lineSegmentList.add(new Box<LineSegment>(linesegment.clone(), highlight));
    return _self;
  }

  @Override
  public Scene addRay(Ray ray, Boolean highlight) {
    _rayList.add(new Box<Ray>(ray.clone(), highlight));
    return _self;
  }

  @Override
  public Scene addPoint(Point point, Boolean highlight) {
    _pointList.add(new Box<Point>(point.clone(), highlight));
    return _self;
  }

  @Override
  public String toSvg() {
    // Create a DOM implementation
    DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
    Document document =
        domImpl.createDocument("http://www.w3.org/2000/svg", "svg", null);
    svg = new SVGGraphics2D(document);
    this.paint(svg);
    Writer out = new StringWriter();
    try {
      svg.stream(out, true);
    }
    catch (SVGGraphics2DIOException e) {
      e.printStackTrace();
    }
    return out.toString();
  }

  @Override
  public Scene setBoundingBox(int height, int width) {
    List<Point> tmpLst = new LinkedList<Point>();
    tmpLst.add(new Point(0, 0));
    tmpLst.add(new Point(0, height));
    tmpLst.add(new Point(width, height));
    tmpLst.add(new Point(width, 0));
    _boundingBox = new OrderedListPolygon(tmpLst);
    return _self;
  }

  @Override
  public Scene setBoundingBox(int radius) {
    _boundingBox = new Circle(radius, new Point(radius, radius));
    return _self;
  }
}
