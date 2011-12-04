package polygonsSWP.geometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import polygonsSWP.util.MathUtils;


/**
 * This Polygon is meant to be just a monoton polygon. THe polygon is stored in
 * a counter clock-wise ordered list of line segments. TODO: needs to get
 * optimization!
 * 
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 */

public class MonotonPolygon
  extends Polygon
{
  protected List<LineSegment> _edges;
  private List<LineSegment> _innerEdges;
  private boolean isTriangulized = false;

  public MonotonPolygon(List<LineSegment> _list) {
    _edges = _list;
    _innerEdges = new ArrayList<LineSegment>();
  }

  public MonotonPolygon() {
    this(new ArrayList<LineSegment>());
  }

  public List<Point> getSortedPoints() {
    List<Point> pointList = new ArrayList<Point>();
    for (LineSegment item : _edges) {
      if (!pointList.contains(item.a)) pointList.add(item.a);
      if (!pointList.contains(item.b)) pointList.add(item.b);
    }
    Collections.sort(pointList);
    return pointList;
  }

  public List<Point> getPoints() {
    return null;
  }

  public List<LineSegment> getEdges() {
    return _edges;
  }

  @Override
  public Polygon clone() {
    List<LineSegment> tmpList = new ArrayList<LineSegment>();
    tmpList.addAll(_edges);
    return new MonotonPolygon(tmpList);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof MonotonPolygon) {
      for (LineSegment item : ((MonotonPolygon) obj).getEdges())
        if (!this._edges.contains(item)) return false;
      return true;
    }
    return false;
  }

  @Override
  public boolean containsPoint(Point p, boolean onLine) {
    // TODO: Check for faster method.
    if (!isTriangulized) this.triangulate();
    for (Triangle item : this.getTriangles())
      if (item.containsPoint(p, onLine)) return true;
    return false;
  }

  public boolean areNeighbours(Point a, Point b) {
    for (LineSegment item : this._edges) {
      if (item.equals(new LineSegment(a, b))) return true;
    }
    if (!_innerEdges.isEmpty()) {
      for (LineSegment item : this._innerEdges) {
        if (item.equals(new LineSegment(a, b))) return true;
      }
    }
    return false;
  }

  public void addEdge(LineSegment line) {
    if (!_edges.contains(line) && !_innerEdges.contains(line)) {
      _edges.add(line);
      isTriangulized = false;
    }
  }

  public void addInnerEdge(LineSegment line) {
    if (!_edges.contains(line) && !_innerEdges.contains(line))
      _innerEdges.add(line);
  }

  @Override
  public double getSurfaceArea() {
    if (!isTriangulized) this.triangulate();
    double area = 0;
    for (Triangle item : this.getTriangles())
      area += item.getSurfaceArea();
    return area;
  }

  @Override
  public Point createRandomPoint() {
    if (!isTriangulized) this.triangulate();
    List<Triangle> triangularization = this.getTriangles();
    // Choose one triangle of triangularization randomly weighted by their
    // Surface Area.
    Triangle chosenPolygon =
        Triangle.selectRandomTriangleBySize(triangularization);

    // Return randomly chosen Point in chosen Triangle.
    return chosenPolygon.createRandomPoint();
  }

  public Point getNext(Point p) {
    return this.getSortedPoints().get(
        (this.getSortedPoints().indexOf(p) + 1) % this.getSortedPoints().size());
  }

  public boolean isConvex(Point a, Point b, Point c) {
    if (MathUtils.checkOrientation(a, b, c) == -1) return true;
    return false;
  }

  public void triangulate() {
    /* Clear old triangulation */
    _innerEdges.clear();
    /* Every List of Points is sorted by asc X-Coordinate */
    List<Point> stack = new ArrayList<Point>();
    // Add first wo points to stack
    stack.add(this.getSortedPoints().get(0));
    stack.add(this.getSortedPoints().get(1));
    // Sort one is current point
    Point tmp = this.getSortedPoints().get(2);
    while (!(this.areNeighbours(tmp, stack.get(0)) && this.areNeighbours(tmp,
        stack.get(stack.size() - 1)))) {
      if (this.areNeighbours(tmp, stack.get(0)) &&
          !this.areNeighbours(tmp, stack.get(stack.size() - 1))) {
        /*
         * Add all edges between tmp and all the points from the stack to the
         * polygon
         */
        for (Point item : stack)
          this.addInnerEdge(new LineSegment(tmp, item));
        Point head = stack.get(stack.size() - 1);
        stack.clear();
        stack.add(head);
        stack.add(tmp);
        tmp = this.getNext(tmp);
      }
      else {
        if ((stack.size() > 1) &&
            (this.isConvex(stack.get(stack.size() - 2),
                stack.get(stack.size() - 1), tmp))) {
          this.addInnerEdge(new LineSegment(tmp, stack.get(stack.size() - 1)));
          stack.remove(stack.size() - 1);
        }
        else {
          stack.add(tmp);
          tmp = this.getNext(tmp);
        }
      }
    }
    if (stack.size() > 1) {
      for (int i = 1; i < stack.size() - 2; ++i)
        this.addInnerEdge(new LineSegment(tmp, stack.get(i)));
    }
    isTriangulized = true;
  }

  public List<Triangle> getTriangles() {
    if (!isTriangulized) this.triangulate();
    List<Triangle> tmpList = new ArrayList<Triangle>();
    for (LineSegment item : _edges) {
      List<LineSegment> left = new ArrayList<LineSegment>();
      List<LineSegment> right = new ArrayList<LineSegment>();
      for (LineSegment inner : _innerEdges) {
        if (inner.contains(item.a) && !inner.contains(item.b)) left.add(inner);
        if (!inner.contains(item.a) && inner.contains(item.b))
          right.add(inner);
      }
      for (LineSegment outer : _edges) {
        if (outer.contains(item.a) && !outer.contains(item.b)) left.add(outer);
        if (!outer.contains(item.a) && outer.contains(item.b))
          right.add(outer);
      }
      boolean found = false;
      for (LineSegment l : left) {
        if (found) break;
        for (LineSegment r : left) {
          if (l.a == item.a) {
            if (r.contains(l.b)) {
              Triangle poly = new Triangle(item.a, item.b, l.b);
              tmpList.add(poly);
              found = true;
              break;
            }
          }
          else {
            if (r.contains(l.a)) {
              Triangle poly = new Triangle(item.a, item.b, l.a);
              tmpList.add(poly);
              found = true;
              break;
            }
          }
        }
      }
    }
    return tmpList;
  }
}
