package polygonsSWP.data;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import polygonsSWP.geometry.Line;
import polygonsSWP.geometry.LineSegment;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.geometry.Ray;
import polygonsSWP.gui.visualisation.TransformedStroke;


/**
 * The whole idea is: you add arbitrary geometry objects to the scene and they
 * are painted to a SVG in the order polygon -> line -> linesegment -> ray ->
 * point. So from bit to small. Not highlighted objects are painted black,
 * higlighted objects are painted according to their class.
 * 
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
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
  public class Box<T>
  {
    private Color _highlight;
    private T _object;

    public Box(T object, Color highlight) {
      _highlight = highlight;
      _object = object;
    }

    public boolean isHighlighted() {
      return _highlight != null;
    }

    public Color getHighlight() {
      return _highlight;
    }

    public T openBox() {
      return _object;
    }
  }

  //light blue for polygons
  public static final Color POLYCOLOR = new Color(0xa2cdfd);

  // red for lines (rays, linesegment...)
  public static final Color LINECOLOR = new Color(0xae0000);

  // green for points
  public static final Color POINTCOLOR = new Color(0x007426);

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
  public void save() {
    _history.addScene(this);
  }

  @Override
  public void paintPoints(Graphics2D g2d) {
    // Every Point
    for (Box<Point> item : _pointList) {
      drawPoint(g2d, item);
    }
  }

  private void drawPoint(Graphics2D g, Box<Point> box){
    g.setColor(box.isHighlighted() ? box.getHighlight() : Color.BLACK);

    Point a = box.openBox();
    g.draw(new Line2D.Double(a.x + 2, a.y + 2, a.x - 2, a.y - 2));
    g.draw(new Line2D.Double(a.x - 2, a.y + 2, a.x + 2, a.y - 2));
  }

  private void drawPolygon(Graphics2D g, Box<Polygon> box){
    Polygon polygon = box.openBox();
    Path2D draw = new Path2D.Double(Path2D.WIND_EVEN_ODD, polygon.size() + 1);

    List<Point> points = polygon.getPoints();

    Point start = points.get(0);
    draw.moveTo(start.x, start.y);

    for(Point next: points) {
      draw.lineTo(next.x, next.y);
    }

    draw.lineTo(start.x, start.y);

    if(box.isHighlighted()) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setColor(box.getHighlight());
      g2.fill(draw);
      g2.dispose();
    }

    g.draw(draw);
  }

  private void drawLine(Graphics2D g, Box<Line> box, Polygon boundingBox){
    g.setColor(box.isHighlighted() ? box.getHighlight() : Color.BLACK);

    // Calculate intersections to keep line inside of bounding box
    List<Point[]> isecs = boundingBox.intersect(box.openBox());

    if(isecs == null || isecs.size() < 2) return;

    Point a = isecs.get(0)[0],
          b = isecs.get(1)[0];

    Graphics2D g2 = (Graphics2D) g.create();
    g2.setStroke(new TransformedStroke(new BasicStroke(2f), g2.getTransform()));
    g2.draw(new Line2D.Double(a.x, a.y, b.x, b.y));
    g2.dispose();
  }

  private void drawRay(Graphics2D g, Box<Ray> box, Polygon boundingBox){
    g.setColor(box.isHighlighted() ? box.getHighlight() : Color.BLACK);

    Ray ray = box.openBox();

    // Calculate intersections to keep ray inside of bounding box
    List<Point[]> isecs = boundingBox.intersect(ray);

    if(isecs == null || isecs.size() < 1) return;

    Point a = ray._base, b = isecs.get(0)[0];
    if (isecs.size() > 1) {
      a = b;
      b = isecs.get(1)[0];
    }

    Graphics2D g2 = (Graphics2D) g.create();
    g2.setStroke(new TransformedStroke(new BasicStroke(2f), g2.getTransform()));
    g2.draw(new Line2D.Double(a.x, a.y, b.x, b.y));
    g2.dispose();
  }

  private void drawLineSegment(Graphics2D g, Box<LineSegment> box, Polygon boundingBox){
    g.setColor(box.isHighlighted() ? box.getHighlight() : Color.BLACK);

    LineSegment lineSegment = box.openBox();
    Point a = lineSegment._a, b = lineSegment._b;

    Graphics2D g2 = (Graphics2D) g.create();
    g2.setStroke(new TransformedStroke(new BasicStroke(2f), g2.getTransform()));
    g2.draw(new Line2D.Double(a.x, a.y, b.x, b.y));
    g2.dispose();
  }

  /**
   * Doodling.
   * 
   * @param g2d
   */
  public void paint(Graphics2D g2d) {
    Polygon _boundingBox = this._boundingBox;

    // First of all draw bounding Box:
    g2d.setColor(Color.BLACK);

    if(_boundingBox != null && _boundingBox instanceof Polygon) {

      drawPolygon(g2d, new Box<Polygon>(_boundingBox, null));

    } else {
      Rectangle clip = g2d.getClipBounds();

      _boundingBox = createBoundingBox(clip.getMinX(), clip.getMinY(),
          clip.getWidth(), clip.getHeight());
    }

    // Afterwards every polygon:
    for (Box<Polygon> item : _polyList) {
      drawPolygon(g2d, item);
    }

    // Every Line
    for (Box<Line> item : _lineList) {
      drawLine(g2d, item, _boundingBox);
    }

    // Every LineSegment
    for (Box<LineSegment> item : _lineSegmentList) {
      drawLineSegment(g2d, item, _boundingBox);
    }

    // Every Ray
    for (Box<Ray> item : _rayList) {
      drawRay(g2d, item, _boundingBox);
    }
  }


  @Override
  public Scene mergeScene(Scene scene) {
    if(!(scene instanceof HistoryScene)) return _self;
    HistoryScene that = (HistoryScene)scene;

    _lineList.addAll(that._lineList);
    _lineSegmentList.addAll(that._lineSegmentList);
    _polyList.addAll(that._polyList);
    _pointList.addAll(that._pointList);
    _rayList.addAll(that._rayList);
    return _self;
  }

  @Override
  public Scene addPolygon(Polygon polygon, Boolean highlight) {
    return addPolygon(polygon, highlight ? POLYCOLOR : null);
  }

  @Override
  public Scene addPolygon(Polygon polygon, Color color) {
    _polyList.add(new Box<Polygon>(polygon.clone(), color));
    return _self;
  }

  @Override
  public Scene addLine(Line line, Boolean highlight) {
    return addLine(line, highlight ? LINECOLOR : null);
  }

  @Override
  public Scene addLine(Line line, Color color) {
    _lineList.add(new Box<Line>(line.clone(), color));
    return _self;
  }

  @Override
  public Scene addLineSegment(LineSegment linesegment, Boolean highlight) {
    return addLineSegment(linesegment, highlight ? LINECOLOR : null);
  }

  @Override
  public Scene addLineSegment(LineSegment linesegment, Color color) {
    _lineSegmentList.add(new Box<LineSegment>(linesegment.clone(), color));
    return _self;
  }

  @Override
  public Scene addRay(Ray ray, Boolean highlight) {
    return addRay(ray, highlight ? LINECOLOR : null);
  }

  @Override
  public Scene addRay(Ray ray, Color color) {
    _rayList.add(new Box<Ray>(ray.clone(), color));
    return _self;
  }

  @Override
  public Scene addPoint(Point point, Color color) {
    _pointList.add(new Box<Point>(point.clone(), color));
    return _self;
  }

  @Override
  public Scene addPoint(Point point, Boolean highlight) {
    return addPoint(point, highlight? POINTCOLOR : null);
  }

  @Override
  public Scene addPoints(List<Point> points, Boolean highlight) {
    return addPoints(points, highlight? POINTCOLOR : null);
  }

  @Override
  public Scene addPoints(List<Point> points, Color color) {
    if(points == null) return _self;
    for(Point point: points){
      addPoint(point, color);
    }
    return _self;
  }

  @Override
  public String toSvg() {
    // Create a DOM implementation
    DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
    Document document =
        domImpl.createDocument("http://www.w3.org/2000/svg", "svg", null);
    svg = new SVGGraphics2D(document);

    int size = this._history.boundingBox;

    AffineTransform tx = new AffineTransform();
    tx.translate(0, size);
    tx.scale(1, -1);
    svg.setTransform(tx);
    svg.setStroke(new BasicStroke(1f));

    svg.setSVGCanvasSize(new Dimension(size, size));
    svg.setClip(0, 0, size, size);
    this.paint(svg);
    this.paintPoints(svg);

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
  public Scene setBoundingBox(Polygon polygon){
    _boundingBox = polygon.clone();
    return _self;
  }
  
  public LinkedList<Box<Polygon>> getPointList()
  {
	  return this._polyList;
  }

  private Polygon createBoundingBox(double x, double y, double width, double height){

    List<Point> tmpLst = new ArrayList<Point>();
    tmpLst.add(new Point(x, y));
    tmpLst.add(new Point(x, y + height));
    tmpLst.add(new Point(x + width, y + height));
    tmpLst.add(new Point(x + width, y));
    return new OrderedListPolygon(tmpLst);
  }

  @Override
  public Scene setBoundingBox(int height, int width) {

    return setBoundingBox(createBoundingBox(0, 0, width, height));
  }
}
