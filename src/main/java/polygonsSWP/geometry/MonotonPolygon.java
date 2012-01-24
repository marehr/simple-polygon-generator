package polygonsSWP.geometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
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
  private List<Point> _points;
  private boolean isTriangulized = false;

  public MonotonPolygon(List<Point> list) {
    _points = list;
  }

  // public MonotonPolygon(List<LineSegment> _list) {
  // _edges = new LinkedList<LineSegment>();
  // _edges.addAll(_list);
  // _innerEdges = new ArrayList<LineSegment>();
  // }

  // public MonotonPolygon() {
  // this(new ArrayList<LineSegment>());
  // }

  public List<Point> getSortedPoints() {
    List<Point> pointList = new ArrayList<Point>();
    for (LineSegment item : _edges) {
      if (!pointList.contains(item._a)) pointList.add(item._a);
      if (!pointList.contains(item._b)) pointList.add(item._b);
    }
    Collections.sort(pointList);
    return pointList;
  }

  @Override
  public String toString() {
    String retVal = "[";
    for (LineSegment edge : _edges)
      retVal += edge + ",";
    retVal += "]";
    return retVal;
  }

  public List<Point> getPoints() {
    return _points;
  }

  public List<LineSegment> getEdges() {
    return _edges;
  }

  @Override
  public Polygon clone() {
    List<LineSegment> tmpList = new ArrayList<LineSegment>();
    for (LineSegment item : _edges)
      tmpList.add(item.clone());
    return new MonotonPolygon(null);
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
    for (LineSegment item : this._edges)
      if (item.equals(new LineSegment(a, b))) { return true; }
    if (!_innerEdges.isEmpty()) for (LineSegment item : this._innerEdges)
      if (item.equals(new LineSegment(a, b))) return true;

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
      // for every outer edge
      List<LineSegment> left = new ArrayList<LineSegment>();
      List<LineSegment> right = new ArrayList<LineSegment>();
      List<LineSegment> used = new ArrayList<LineSegment>();
      for (LineSegment inner : _innerEdges) {
        if (inner.containsPoint(item._a) && !inner.containsPoint(item._b))
          left.add(inner);
        if (!inner.containsPoint(item._a) && inner.containsPoint(item._b))
          right.add(inner);
      }
      for (LineSegment outer : _edges) {
        if (used.contains(outer)) continue;
        if (outer.containsPoint(item._a) && !outer.containsPoint(item._b))
          left.add(outer);
        if (!outer.containsPoint(item._a) && outer.containsPoint(item._b))
          right.add(outer);
      }
      boolean found = false;
      for (int i = 0; i < left.size() - 1; ++i) {
        if (Triangle.formsTriangle(item, left.get(i),
            right.get(right.size() - 1))) {
          if (item._b == right.get(right.size() - 1)._b) {
            Triangle tmp =
                new Triangle(item._a, item._b, right.get(right.size() - 1)._a);
            if (!tmpList.contains(tmp)) tmpList.add(tmp);
          }
          else {
            Triangle tmp =
                new Triangle(item._a, item._b, right.get(right.size() - 1)._b);
            if (!tmpList.contains(tmp)) tmpList.add(tmp);
          }
          used.add(item);
          used.add(right.get(right.size() - 1));
          found = true;
          break;
        }
      }
      if (found) continue;
      for (int i = 0; i < right.size() - 1; ++i) {
        if (Triangle.formsTriangle(item, left.get(left.size() - 1),
            right.get(i))) {
          if (item._a == left.get(left.size() - 1)._a) {
            Triangle tmp =
                new Triangle(item._a, item._b, left.get(left.size() - 1)._a);
            if (!tmpList.contains(tmp)) tmpList.add(tmp);
          }
          else {
            Triangle tmp =
                new Triangle(item._a, item._b, left.get(left.size() - 1)._a);
            if (!tmpList.contains(tmp)) tmpList.add(tmp);
          }
          used.add(item);
          used.add(left.get(left.size() - 1));
          found = true;
          break;
        }
      }
      if (found) continue;
      for (int i = 0; i < left.size() - 1; ++i) {
        for (int j = 0; j < right.size() - 1; ++j) {
          if (Triangle.formsTriangle(item, left.get(i), right.get(j))) {
            if (item._a == left.get(i)._a) {
              Triangle tmp = new Triangle(item._a, item._b, left.get(i)._b);
              if (!tmpList.contains(tmp)) tmpList.add(tmp);
            }
            else {
              Triangle tmp = new Triangle(item._a, item._b, left.get(i)._a);
              if (!tmpList.contains(tmp)) tmpList.add(tmp);
            }
            found = true;
            break;
          }
          if (found) break;
        }
      }
    }
    return tmpList;
  }

  @Override
  public int size() {
    return _points.size();
  }
}
