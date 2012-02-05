package polygonsSWP.geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Comparator;
import java.util.TreeSet;

import polygonsSWP.util.EdgeList;
import polygonsSWP.util.MathUtils;
import polygonsSWP.util.PointType;


/**
 * Implementation of Polygon using ordered counter-clockwise list. The
 * assumption is, that the list of polygons is ordered in order of appearance.
 * So the polygon is drawn from point one to point two to .. to point n to point
 * 1. So be sure your self added point list meets the assumption otherwise it
 * won't work.
 * 
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 */
public class OrderedListPolygon
  extends Polygon
{
  List<Point> _coords;
  List<Triangle> _triangles;

  /**
   * Generates an empty polygon object which will contain no statistics or
   * history.
   */
  public OrderedListPolygon() {
    this(new ArrayList<Point>());
  }

  /**
   * Generates an polygon object which consists of the given points, carrying no
   * statistics object.
   */
  public OrderedListPolygon(List<Point> coords) {
    _coords = coords;
  }

  public List<Point> getPoints() {
    return _coords;
  }

  /**
   * Gives Point at Position "pos"
   * 
   * @param pos
   * @return
   */
  public Point getPoint(int pos) {
    return _coords.get(pos);
  }

  /**
   * @param coords Sets a new list of ordered points.
   */
  public void setPoints(List<Point> coords) {
    _coords = coords;
  }

  /**
   * Adds a point to the end of the list.
   * 
   * @param p The new point.
   */
  public void addPoint(Point p) {
    _coords.add(p);
  }

  /**
   * Adds a point to the desired position in the list.
   * 
   * @param p The new point.
   * @param pos Position in list.
   */
  public void addPoint(Point p, int pos) {
    _coords.add(pos, p);
  }

  /**
   * Deletes the given point.
   * 
   * @param p Point to delete.
   */
  public void deletePoint(Point p) {
    _coords.remove(p);
  }

  /**
   * Creates a new polygon of the same set of points by creating a random
   * permutation of the points and linking them in order. The resulting polygon
   * is very likely to be complex.
   */
  public void permute() {
    Collections.shuffle(_coords);
  }

  /**
   * Reverses the polygon's order. Polygon keeps its simplicity.
   */
  public void reverse() {
    Collections.reverse(_coords);
  }

  /**
   * Determines whether a given ordered list of points forms a simple polygon.
   * 
   * @return true, if the polygon is simple, otherwise false.
   */
  public boolean isSimple() {
    return findIntersections().size() == 0;
  }

  /**
   * Calculates the set of all intersections found in the polygon.
   * 
   * @return a list of intersections, where each item is an array of the two
   *         indices defining an intersection. [x,y] --> edge(x,x+1) intersects
   *         edge(y,y+1)
   */
  public List<Integer[]> findIntersections() {
    /*
     * Remark: The approach used here is very naive. As Held writes in his
     * paper, "The simplicity test for a polygon is not done in linear time;
     * rather we implemented a straightforward quadratic approach. (As we will
     * see later this has no influence on the test results.)", I also decided to
     * let alone [Cha91] for now and simply check for crossing lines. However,
     * since we're going to implement polygon triangulation anyway, we should
     * definitely come back here later and improve this. Maybe, instead of the
     * proposed [Cha91], we could also use the Bentley-Ottmann algorithm, see
     * http://en.wikipedia.org/wiki/Bentley%E2%80%93Ottmann_algorithm for
     * explanation.
     */

    List<Integer[]> retval = new ArrayList<Integer[]>();
    int size = _coords.size();
    Point[] isect = new Point[1];
    for (int i = 0; i < size - 1; i++) {
      LineSegment a = new LineSegment(_coords.get(i), _coords.get(i + 1));

      // Then test the remaining line segments for intersections
      for (int j = i + 1; j < size; j++) {
        LineSegment b =
            new LineSegment(_coords.get(j), _coords.get((j + 1) % size));

        isect = a.intersect(b);
        if (isect != null) {

          boolean coincident;
          boolean ab = false;
          boolean ba = false;

          // Check for coincidence (--> intersection)
          if (!(coincident = (isect.length == 0))) {

            // Check whether the intersection is a shared endpoint (--> no
            // intersection)
            ab =
                isect[0].equals(_coords.get(i)) &&
                    _coords.get(i).equals(_coords.get((j + 1) % size));
            ba =
                isect[0].equals(_coords.get(j)) &&
                    _coords.get(j).equals(_coords.get(i + 1));

          }

          if (coincident || !(ab || ba)) retval.add(new Integer[] { i, j });
        }
      }
    }

    return retval;
  }

  /**
   * Returns a randomly chosen intersection of the polygon.
   * 
   * @return 2-element array determining the intersection (see above) null, if
   *         there is no intersection
   */
  public Integer[] findRandomIntersection() {
    List<Integer[]> is = findIntersections();
    if (is.size() == 0) return null;

    return is.get(new Random(System.currentTimeMillis()).nextInt(is.size()));
  }

  /**
   * @return the number of vertices in the polygon
   */
  public int size() {
    return _coords.size();
  }

  /**
   * Simple test for equality. Should be improved if we introduce other
   * implementations of polygon.
   */
  @Override
  public boolean equals(Object obj) {
    // Is Object a Polygon?
    if (!(obj instanceof OrderedListPolygon)) return false;
    OrderedListPolygon oP = (OrderedListPolygon) obj;
    // Get starting point and compare clockwise whole polygon
    if (_coords.size() == oP.size()) {
      Point thisPoint = _coords.get(0);
      int index = oP.getPoints().indexOf(thisPoint);
      if (index == -1) return false;
      for (int i = 1; i < _coords.size(); ++i)
        if (!_coords.get(i).equals(
            oP.getPoints().get(oP.getIndexInRange(index + i)))) return false;
      return true;
    }
    else return false;
  }

  /**
   * Create an index via module which is always in range
   * 
   * @param index index to be modified
   * @return
   */
  public int getIndexInRange(final int index) {
    int result = index % _coords.size();
    return result < 0 ? result + _coords.size() : result;
  }

  /**
   * Return point with safe index.
   */
  public Point getPointInRange(final int index) {
    return getPoint(getIndexInRange(index));
  }

  @Override
  public OrderedListPolygon clone() {
    List<Point> nList = new ArrayList<Point>(_coords.size());
    for (Point item : _coords)
      nList.add(item.clone());
    return new OrderedListPolygon(nList);
  }

  /**
   * Tests if p is vertex of the given Polygon.
   * 
   * @param p point
   * @return true, if p is a vertex of the given Polygon
   */
  public boolean containsVertex(Point p) {
    return getPoints().contains(p);
  }

  /**
   * TODO: remove me after Trapzblah fertig und SweepLineTestFactory Fuer Steve,
   * damit er auf konstanten Polygonen sweepline testen kann wenn weniger als 3
   * Punkte angegeben werden, dann wird einfach SpacePartitioning auf die GUI
   * Settings ausgefuehrt
   */
  public static OrderedListPolygon sweepLineTestPolygon =
      new OrderedListPolygon(new ArrayList<Point>(Arrays.asList(new Point(
          271.186, 535.512), new Point(26.567, 502.446), new Point(166.169,
          34.714), new Point(331.105, 107.533), new Point(497.054, 261.925))));

  public List<Trapezoid> sweepLine() {
    System.out.println("Start Trapezodation:--------------------------------------");
    System.out.println(this.getPoints());
    List<Trapezoid> returnList = new ArrayList<Trapezoid>();
    // Add comparator
    TreeSet<PointType> pointTree =
        new TreeSet<PointType>(new PointType.PointComparator());
    // Calculate type and direction for every Point and them to the tree
    // they are implicitly sorted then.
    HashMap<Point, PointType> pointHash = new HashMap<Point, PointType>();
    for (Point p : _coords)
      pointHash.put(p, categorizePointForSweepLine(p));
    for (PointType tp : pointHash.values())
      pointTree.add(tp);

    EdgeList eList = new EdgeList();
    // For every Vertex in tree
    Iterator<PointType> iter = pointTree.iterator();
    while (iter.hasNext()) {
      PointType curr = iter.next();
      System.out.println("Current Point: " + curr.p + " Type: " + curr.type +
          " Direction: " + curr.direct);
      // Step 1
      // Add beginning edge of current Vertex
      // Mark ending edge of current vertex for removal
      switch (curr.type) {
      case INT:
        // mark every edge with curr.p as endpoint for deletion
        eList.markEdge(curr.p);
        // Add the edge curr.p with left or right neighbor.
        // where the neighbor with the lower y coordinate is choosen
        if (curr.left.y < curr.p.y) {
          eList.insertEdge(curr.p, curr.left);
        }
        else {
          eList.insertEdge(curr.p, curr.right);
        }
        break;
      case MAX:
        // Add both edges to right and left neighbour to edgelist
        eList.insertEdge(curr.p, curr.right);
        eList.insertEdge(curr.p, curr.left);
        break;
      case MIN:
        // Mark every edge with curr.p as endpoint for deletion
        eList.markEdge(curr.p);
        break;
      case HMAX:
        // Insert only the non horizontal edge
        if (MathUtils.doubleEquals(curr.right.x, curr.p.x)) {
          eList.insertEdge(curr.p, curr.left);
        }
        else {
          eList.insertEdge(curr.p, curr.right);
        }
        break;
      case HMIN:
        // Mark only the non horizontal edge for deletion
        eList.markEdge(curr.p);
        break;
      default:
        break;
      }
      // Step 2
      // - Get direction of sweepline.
      // - determine intersecting edges of polygon and sweepline with edge list
      // - or if no sweepline is thrown, just add points to formerIntersections
      // and/or form trapezoids

      // If it is INT
      if (curr.type == PointType.PointClass.INT) {
        // Calculate intersection point (only one is possible)
        LineSegment interEdge[] =
            eList.searchIntersectingEdges(curr.p, curr.direct);
        for (int i = 0; i < 2; ++i) {
          if (interEdge[i] != null)
            System.out.println("Edge for Intersection: " + interEdge[i]);
        }
        Point[] intersections = sweepLineIntersect(curr, interEdge);
        for (int i = 0; i < intersections.length; ++i) {
          if (intersections[i] != null)
            System.out.println("Intersection Points: " + intersections[i]);
        }

        // Form Polygon
        if (curr.direct == PointType.Direction.RIGHT) {
          formMonontonPolygon(curr.p, intersections[0],
              eList.getIntersectionByEndPoint(curr.p)[0],
              eList.getIntersectionByEndPoint(interEdge[0]._a)[0], returnList);
          // Update intersection to calculated one
        }
        else {
          formMonontonPolygon(intersections[0], curr.p,
              eList.getIntersectionByEndPoint(interEdge[0]._a)[0],
              eList.getIntersectionByEndPoint(curr.p)[0], returnList);
        }
        eList.updateIntersection(interEdge[0]._a, interEdge[0]._b,
            intersections[0]);
      }
      // If it is MAX
      else if (curr.type == PointType.PointClass.MAX) {
        if (curr.direct == PointType.Direction.BOTH) {
          // Calculate intersection points (only two are possbile)
          LineSegment interEdge[] =
              eList.searchIntersectingEdges(curr.p, curr.direct);
          for (int i = 0; i < 2; ++i) {
            if (interEdge[i] != null)
              System.out.println("Edge for Intersection: " + interEdge[i]);
          }
          Point[] intersections = sweepLineIntersect(curr, interEdge);
          for (int i = 0; i < intersections.length; ++i) {
            if (intersections[i] != null)
              System.out.println("Intersection Points: " + intersections[i]);
          }
          // Form Polygon
          formMonontonPolygon(intersections[0], intersections[1],
              eList.getIntersectionByEndPoint(interEdge[0]._a)[0],
              eList.getIntersectionByEndPoint(interEdge[1]._a)[0], returnList);
          // Update intersection to claculated ones
          eList.updateIntersection(interEdge[0]._a, interEdge[0]._b,
              intersections[0]);
          eList.updateIntersection(interEdge[1]._a, interEdge[1]._b,
              intersections[1]);
        }
      }
      // If it is MIN
      else if (curr.type == PointType.PointClass.MIN) {
        if (curr.direct == PointType.Direction.BOTH) {
          // Calculate intersection points (only two are possbile)
          LineSegment interEdge[] =
              eList.searchIntersectingEdges(curr.p, curr.direct);
          for (int i = 0; i < 2; ++i) {
            if (interEdge[i] != null)
              System.out.println("Edge for Intersection: " + interEdge[i]);
          }
          Point[] intersections = sweepLineIntersect(curr, interEdge);
          for (int i = 0; i < intersections.length; ++i) {
            if (intersections[i] != null)
              System.out.println("Intersection Points: " + intersections[i]);
          }

          // Form Polygon
          // Only form first polygon if it wasn't form before because of two MIN
          // on the same line
          // Is it the right intersection point? If not get the other one.
          Point endOne = eList.getIntersectionByEndPoint(interEdge[0]._a)[0];
          Point endTwo = eList.getIntersectionByEndPoint(interEdge[1]._a)[0];
          Point formerOne = eList.getIntersectionByEndPoint(curr.p)[0];
          Point formerTwo = eList.getIntersectionByEndPoint(curr.p)[1];
          if (!eList.getIntersectionByEndPoint(interEdge[0]._a)[0].equals(intersections[0]))
            formMonontonPolygon(curr.p, intersections[0], formerOne, endOne,
                returnList);
          formMonontonPolygon(curr.p, intersections[1], formerTwo, endTwo,
              returnList);
          // Update former intersections
          eList.updateIntersection(interEdge[0]._a, interEdge[0]._b,
              intersections[0]);
          eList.updateIntersection(interEdge[1]._a, interEdge[1]._b,
              intersections[1]);
        }
        else {
          formMonontonPolygon(curr.p,
              eList.getIntersectionByEndPoint(curr.p)[0],
              eList.getIntersectionByEndPoint(curr.p)[1],
              eList.getIntersectionByEndPoint(curr.p)[1], returnList);
        }
      }
      // If it is HMAX
      else if (curr.type == PointType.PointClass.HMAX) {
        if (curr.direct == PointType.Direction.LEFT) {
          // Calculate intersection points (only two are possbile)
          LineSegment interEdge[] =
              eList.searchIntersectingEdges(curr.p, curr.direct);
          for (int i = 0; i < 2; ++i) {
            if (interEdge[i] != null)
              System.out.println("Edge for Intersection: " + interEdge[i]);
          }
          Point[] intersections = sweepLineIntersect(curr, interEdge);
          for (int i = 0; i < intersections.length; ++i) {
            if (intersections[i] != null)
              System.out.println("Intersection Points: " + intersections[i]);
          }

          // If it is another HMAX calculate snd intersection and form polygon
          if (pointHash.get(curr.right).type == PointType.PointClass.HMAX) {
            LineSegment interEdgeSecond[] =
                eList.searchIntersectingEdges(curr.right,
                    PointType.Direction.RIGHT);
            Point[] sndIntersections =
                sweepLineIntersect(pointHash.get(curr.right), interEdgeSecond);
            formMonontonPolygon(intersections[0], sndIntersections[0],
                eList.getIntersectionByEndPoint(interEdge[0]._a)[0],
                eList.getIntersectionByEndPoint(interEdgeSecond[0]._a)[0],
                returnList);
            // Update FormerIntersections.
            eList.updateIntersection(interEdge[0]._a, interEdge[0]._b,
                intersections[0]);
            eList.updateIntersection(interEdgeSecond[0]._a,
                interEdgeSecond[0]._b, sndIntersections[0]);
          }
          else {
            formMonontonPolygon(intersections[0], curr.right,
                eList.getIntersectionByEndPoint(interEdge[0]._a)[0],
                eList.getIntersectionByEndPoint(curr.right)[0], returnList);
            // Update FormerIntersections.
            eList.updateIntersection(interEdge[0]._a, interEdge[0]._b,
                intersections[0]);
          }
        }
        else if (curr.direct == PointType.Direction.RIGHT) {
          // Calculate intersection points (only two are possbile)
          LineSegment interEdge[] =
              eList.searchIntersectingEdges(curr.p, curr.direct);
          for (int i = 0; i < 2; ++i) {
            if (interEdge[i] != null)
              System.out.println("Edge for Intersection: " + interEdge[i]);
          }
          Point[] intersections = sweepLineIntersect(curr, interEdge);
          for (int i = 0; i < intersections.length; ++i) {
            if (intersections[i] != null)
              System.out.println("Intersection Points: " + intersections[i]);
          }

          if (!eList.getIntersectionByEndPoint(interEdge[0]._a)[0].equals(intersections[0])) {
            formMonontonPolygon(intersections[0], curr.left,
                eList.getIntersectionByEndPoint(interEdge[0]._a)[0],
                eList.getIntersectionByEndPoint(curr.left)[0], returnList);
            // Update former intersections
            eList.updateIntersection(interEdge[0]._a, interEdge[0]._b,
                intersections[0]);
          }
        }
      }
      // If it is HMIN
      else {
        if (curr.direct == PointType.Direction.NONE) {
          if (pointHash.get(curr.right).type == PointType.PointClass.HMIN) {
            formMonontonPolygon(curr.p, curr.right,
                eList.getIntersectionByEndPoint(curr.p)[0],
                eList.getIntersectionByEndPoint(curr.right)[0], returnList);
          }
        }
        else {
          // Calculate intersection points (only two are possbile)
          LineSegment interEdge[] =
              eList.searchIntersectingEdges(curr.p, curr.direct);
          Point[] intersections = sweepLineIntersect(curr, interEdge);
          formMonontonPolygon(curr.p, intersections[0],
              eList.getIntersectionByEndPoint(curr.p)[0],
              eList.getIntersectionByEndPoint(interEdge[0]._a)[0], returnList);
          // Update former intersections
          eList.updateIntersection(interEdge[0]._a, interEdge[0]._b,
              intersections[0]);
        }
      }

      // Step 5
      eList.removeMarkedEdges();
    }
    return returnList;
  }

  /**
   * @author Steve Dierker <dierker.steve@fu-berlin.de>
   * @param a First Point which is usually the current sweepline point
   * @param b Second Point which is usually the current intersection point
   * @param c Fourht Point, the former intersection on the edge of the current
   *          point
   * @param d Third Point, the former intersection on the edge of the
   *          intersection Point
   * @param returnList
   */
  private void formMonontonPolygon(Point a, Point b, Point c, Point d,
      List<Trapezoid> returnList) {
    System.out.println("Point to Paint: " + a + " " + b + " " + d + " " + c);
    List<Point> tmpList = new LinkedList<Point>();
    tmpList.add(a);
    tmpList.add(b);
    tmpList.add(d);
    if (!c.equals(d)) tmpList.add(c);
    System.out.println("Formed Polygon: " + new Trapezoid(tmpList));
    returnList.add(new Trapezoid(tmpList));
  }

  /**
   * This method categorizes the points accordings to the paper and defines the
   * direction for the sweep or scanline if one is thrown. This works!
   * 
   * @param middle Point to categorize
   * @return The Point with it neighbours and type and direction.
   */
  private PointType categorizePointForSweepLine(Point middle) {
    Point left = this.getPointInRange(_coords.indexOf(middle) - 1);
    Point right = this.getPointInRange(_coords.indexOf(middle) + 1);
    // INT
    if (left.y > middle.y + MathUtils.EPSILON &&
        middle.y > right.y + MathUtils.EPSILON) {
      return new PointType(middle, left, right, PointType.PointClass.INT,
          PointType.Direction.RIGHT);
    }
    else if (left.y < middle.y - MathUtils.EPSILON &&
        middle.y < right.y - MathUtils.EPSILON) {
      return new PointType(middle, left, right, PointType.PointClass.INT,
          PointType.Direction.LEFT);
    }
    // MAX
    else if (left.y < middle.y - MathUtils.EPSILON &&
        right.y < middle.y - MathUtils.EPSILON) {
      if (MathUtils.checkOrientation(left, right, middle) == 1) return new PointType(
          middle, left, right, PointType.PointClass.MAX,
          PointType.Direction.BOTH);
      else return new PointType(middle, left, right, PointType.PointClass.MAX,
          PointType.Direction.NONE);
    }
    // MIN
    else if (left.y > middle.y + MathUtils.EPSILON &&
        right.y > middle.y + MathUtils.EPSILON) {
      if (MathUtils.checkOrientation(left, right, middle) == 1) return new PointType(
          middle, left, right, PointType.PointClass.MIN,
          PointType.Direction.BOTH);
      else return new PointType(middle, left, right, PointType.PointClass.MIN,
          PointType.Direction.NONE);

    }
    // IGNORE
    else if (MathUtils.doubleEquals(middle.y, right.y) &&
        MathUtils.doubleEquals(middle.y, left.y)) {
      return new PointType(middle, left, right, PointType.PointClass.IGNORE,
          PointType.Direction.NONE);
    }
    // H___
    else if (MathUtils.doubleEquals(middle.y, left.y)) {
      // HMAX
      if (left.y > right.y + MathUtils.EPSILON) {
        if (MathUtils.checkOrientation(left, right, middle) == 1) return new PointType(
            middle, left, right, PointType.PointClass.HMAX,
            PointType.Direction.RIGHT);
        else return new PointType(middle, left, right,
            PointType.PointClass.HMAX, PointType.Direction.NONE);
      }
      // HMIN
      else {
        if (MathUtils.checkOrientation(left, right, middle) == 1) return new PointType(
            middle, left, right, PointType.PointClass.HMIN,
            PointType.Direction.LEFT);
        else return new PointType(middle, left, right,
            PointType.PointClass.HMIN, PointType.Direction.NONE);
      }
    }
    // H___
    else if (MathUtils.doubleEquals(middle.y, right.y)) {
      // HMAX
      if (right.y > left.y + MathUtils.EPSILON) {
        if (MathUtils.checkOrientation(left, right, middle) == 1) return new PointType(
            middle, left, right, PointType.PointClass.HMAX,
            PointType.Direction.LEFT);
        else return new PointType(middle, left, right,
            PointType.PointClass.HMAX, PointType.Direction.NONE);
      }
      // HMIN
      else {
        if (MathUtils.checkOrientation(left, right, middle) == 1) return new PointType(
            middle, left, right, PointType.PointClass.HMIN,
            PointType.Direction.RIGHT);
        else return new PointType(middle, left, right,
            PointType.PointClass.HMIN, PointType.Direction.NONE);
      }
    }
    else {
      // This should never be reached!
      return null;
    }
  }

  /**
   * Calculates the intersection Points between the edges and the sweepline
   * 
   * @param p Point from where the sweepline is thrown
   * @param edges edges which are possible to intersect
   * @return if there was only one edge to intersect only the first field of the
   *         array is occupied, otherwise both
   */
  private Point[] sweepLineIntersect(PointType p, LineSegment[] edges) {
    // TODO: check if endpoints of lines count as intersections
    if (p.direct == PointType.Direction.RIGHT) {
      Ray sweepLine = new Ray(p.p, new Point(p.p.x + 1, p.p.y));
      Point[] intersections = sweepLine.intersect(edges[0]);
      return intersections;
    }
    else if (p.direct == PointType.Direction.LEFT) {
      Ray sweepLine = new Ray(p.p, new Point(p.p.x - 1, p.p.y));
      Point[] intersections = sweepLine.intersect(edges[0]);
      return intersections;
    }
    else {
      Point[] returnArray = new Point[2];
      Line sweepLine = new Line(p.p, new Point(p.p.x + 1, p.p.y));
      Point[] intersectionOne = sweepLine.intersect(edges[0]);
      Point[] intersectionTwo = sweepLine.intersect(edges[1]);
      returnArray[0] = intersectionOne[0];
      returnArray[1] = intersectionTwo[0];
      return returnArray;
    }
  }

  /**
   * @author Steve Dierker <dierker.steve@fu-berlin.de>
   * @see http://www.flipcode.com/archives/Efficient_Polygon_Triangulation.shtml
   * @category Ear-Clipping-Algorithm
   * @return List of triangulars
   */
  public List<Triangle> triangulate() {
    if(_triangles != null) return _triangles;

    assert size() >= 3;
    assert (isSimple());
    assert (isClockwise() == -1);

    _triangles = new ArrayList<Triangle>();

    if (size() == 3) {
      _triangles.add(new Triangle(getPoints()));
      return _triangles;
    }

    /* Manage a list of indices. */
    int[] V = new int[size()];
    for (int v = 0; v < size(); v++)
      V[v] = v;

    int nv = size();
    for (int v = nv - 1; nv > 2;) {
      /* Three consecutive vertices in current polygon, <u,v,w>. */
      int u = v;
      if (nv <= u) u = 0; /* previous */
      v = u + 1;
      if (nv <= v) v = 0; /* new v */
      int w = v + 1;
      if (nv <= w) w = 0; /* next */

      /* Build a triangle of the three vertices. */
      Point a = _coords.get(V[u]);
      Point b = _coords.get(V[v]);
      Point c = _coords.get(V[w]);
      Triangle triangle = new Triangle(a, b, c);

      /* Test whether triangle <u,v,w> is a snip. */
      boolean isSnip = true;

      /*
       * 1st condition: Counterclockwise rotation of triangle, which means that
       * v is a convex vertex of the polygon.
       */
      if (MathUtils.checkOrientation(a, b, c) == -1) isSnip = false;

      /*
       * 2nd condition: No other vertex lies of the polygon lies inside the
       * triangle <u,v,w>.
       */
      if (isSnip) {
        for (int p = 0; p < nv; p++) {
          if ((p == u) || (p == v) || (p == w)) continue;
          if (triangle.containsPoint(_coords.get(V[p]), true)) {
            isSnip = false;
            break;
          }
        }
      }

      if (isSnip) {
        /* output Triangle */
        _triangles.add(triangle);

        /* remove v from remaining polygon */
        for (int s = v, t = v + 1; t < nv; s++, t++)
          V[s] = V[t];
        nv--;
      }
    }

    return _triangles;
  }

  /*
   * !Doesnt change the actual values
   * @return list of coords sorted by x value of each point
   */
  public List<Point> sortByX() {
    List<Point> tmpList = new LinkedList<Point>();
    tmpList.addAll(_coords);
    Collections.sort(tmpList, new XCompare());
    return tmpList;
  }

  /*
   * !Doesnt change the actual values
   * @return list of coords sorted by y value of each point
   */
  public List<Point> sortByY() {
    List<Point> tmpList = new LinkedList<Point>();
    tmpList.addAll(_coords);
    Collections.sort(tmpList, new YCompare());
    return tmpList;
  }


  private class XCompare
    implements Comparator<Point>
  {

    @Override
    public int compare(Point o1, Point o2) {
      return o1.compareTo(o2);
    }

  }


  private class YCompare
    implements Comparator<Point>
  {

    @Override
    public int compare(Point o1, Point o2) {
      return o1.compareToByY(o2);
    }

  }

  /**
   * Creates a random Point in Polygon. Uses Triangularization, randomly chooses
   * Triangle, creates random Point in Triangle.
   * 
   * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
   * @param polygon Polygon to create random point in
   * @return random Point in given Polygon
   */
  public Point createRandomPoint() {
    assert (size() >= 3);

    Point point;

    // If polygon is a triangle
    if (this.size() == 3) {
      Triangle triangle = new Triangle(this.getPoints());
      point = triangle.createRandomPoint();
    }
    else {

      // Triangulate given Polygon.
      List<Triangle> triangularization = this.triangulate();

      // Choose one triangle of triangularization randomly weighted by their
      // Surface Area.
      Triangle chosenTriangle =
          Triangle.selectRandomTriangleBySize(triangularization);

      System.out.println(chosenTriangle);
      // Return randomly chosen Point in chosen Triangle.
      point = chosenTriangle.createRandomPoint();

    }

    return point;
  }

  public boolean areNeighbours(Point a, Point b) {
    if (_coords.contains(a)) {
      if (_coords.get(
          this.getIndexInRange((_coords.indexOf(a) + 1) % _coords.size())).equals(
          b) ||
          _coords.get(
              this.getIndexInRange((_coords.indexOf(a) - 1) % _coords.size())).equals(
              b)) return true;
      else return false;
    }
    return false;
  }
}
