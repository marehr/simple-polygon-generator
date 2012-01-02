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

import polygonsSWP.geometry.Line;
import polygonsSWP.geometry.LineSegment;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.geometry.Ray;


public class HistoryScene
  implements Scene
{
  private LinkedList<Box<Polygon>> _polyList;
  private LinkedList<Box<Line>> _lineList;
  private LinkedList<Box<LineSegment>> _lineSegmentList;
  private LinkedList<Box<Ray>> _rayList;
  private LinkedList<Box<Point>> _pointList;
  private History _history = null;
  private SVGGraphics2D svg;
  private boolean _circle;
  private int _height, _width, _radius;

  public HistoryScene(History history) {
    _polyList = new LinkedList<Box<Polygon>>();
    _lineList = new LinkedList<Box<Line>>();
    _lineSegmentList = new LinkedList<Box<LineSegment>>();
    _rayList = new LinkedList<Box<Ray>>();
    _pointList = new LinkedList<Box<Point>>();
    _history = history;
  }

  @Override
  public void safe() {
    this.toSvg();
    _history.addScene(this);
  }

  private void paint(Graphics2D g2d) {
    // light blue for polygons
    Color polyColor = new Color(0xa2cdfd);
    // red for lines (rays, linesegment...)
    Color lineColor = new Color(0xae0000);
    // green for points
    Color pointColor = new Color(0x007426);
    // First of all draw bounding Box:
    g2d.setColor(Color.BLACK);
    if (_circle) g2d.drawOval(_radius, _radius, _radius, _radius);
    else g2d.drawRect(0, 0, _width, _height);
    // Afterwards every polygon:
    for (Box<Polygon> item : _polyList) {
      List<Point> p = item.openBox().getPoints();
      int[] xcoords = new int[p.size()];
      int[] ycoords = new int[p.size()];
      for (int i = 0; i < p.size(); i++) {
        xcoords[i] = (int) (p.get(i).x);
        ycoords[i] = (int) (p.get(i).y);
      }

      if (item.isHighlighted()) {
        g2d.setColor(polyColor);
        g2d.fillPolygon(xcoords, xcoords, p.size());
      }
      g2d.setColor(Color.BLACK);
      g2d.drawPolygon(xcoords, ycoords, p.size());
    }
    // Every Line
    // TODO: Calculate real line from it
    for (Box<Line> item : _lineList) {
      Line tmp = item.openBox();
      if (item.isHighlighted()) g2d.setColor(lineColor);
      else g2d.setColor(Color.BLACK);
      g2d.drawLine((int) tmp._a.x, (int) tmp._a.y, (int) tmp._b.x,
          (int) tmp._b.y);
    }
    // Every LineSegment
    for (Box<LineSegment> item : _lineSegmentList) {
      LineSegment tmp = item.openBox();
      if (item.isHighlighted()) g2d.setColor(lineColor);
      else g2d.setColor(Color.BLACK);
      g2d.drawLine((int) tmp._a.x, (int) tmp._a.y, (int) tmp._b.x,
          (int) tmp._b.y);
    }
    // Every Ray
    // TODO: calculate real ray from it
    for (Box<Ray> item : _rayList) {
      Ray tmp = item.openBox();
      if (item.isHighlighted()) g2d.setColor(lineColor);
      else g2d.setColor(Color.BLACK);
      g2d.drawLine((int) tmp._base.x, (int) tmp._base.y, (int) tmp._support.x,
          (int) tmp._support.y);

    }
    // Every Point
    for (Box<Point> item : _pointList) {
      Point tmp = item.openBox();
      if (item.isHighlighted()) g2d.setColor(pointColor);
      else g2d.setColor(Color.BLACK);
      g2d.drawLine((int) tmp.x + 2, (int) tmp.y + 2, (int) tmp.x - 2,
          (int) tmp.y - 2);
      g2d.drawLine((int) tmp.x - 2, (int) tmp.y + 2, (int) tmp.x + 2,
          (int) tmp.y - 2);
    }
  }

  @Override
  public void addPolygon(Polygon polygon, Boolean highlight) {
    _polyList.add(new Box<Polygon>(polygon, highlight));
  }

  @Override
  public void addLine(Line line, Boolean highlight) {
    _lineList.add(new Box<Line>(line, highlight));
  }

  @Override
  public void addLineSegment(LineSegment linesegment, Boolean highlight) {
    _lineSegmentList.add(new Box<LineSegment>(linesegment, highlight));
  }

  @Override
  public void addRay(Ray ray, Boolean highlight) {
    _rayList.add(new Box<Ray>(ray, highlight));
  }

  @Override
  public void addPoint(Point point, Boolean highlight) {
    _pointList.add(new Box<Point>(point, highlight));
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

  @Override
  public void setBoundingBox(int height, int width) {
    _height = height;
    _width = width;
    _circle = false;
  }

  @Override
  public void setBoundingBox(int radius) {
    _radius = radius;
    _circle = true;
  }
}
