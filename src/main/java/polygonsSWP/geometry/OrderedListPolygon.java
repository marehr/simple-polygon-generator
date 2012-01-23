package polygonsSWP.geometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.TreeSet;

import polygonsSWP.util.MathUtils;


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
   * Determines whether this polygon is in clockwise orientation.
   * 
   * @return -1 if counterclockwise, 1 if clockwise, or 0 if this is not
   *         decidable
   */
  public int isClockwise() {
    int n = size();
    int i, j, k;
    int count = 0;
    double z;

    if (n < 3) return 0;

    for (i = 0; i < n; i++) {
      j = (i + 1) % n;
      k = (i + 2) % n;
      z =
          (_coords.get(i).x - _coords.get(i).x) *
              (_coords.get(k).y - _coords.get(j).y);
      z -=
          (_coords.get(j).y - _coords.get(i).y) *
              (_coords.get(k).x - _coords.get(j).x);
      if (z < 0) count--;
      else if (z > 0) count++;
    }

    if (count > 0) return -1;
    else if (count < 0) return 1;
    else return 0;
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
  public Polygon clone() {
    List<Point> nList = new ArrayList<Point>();
    nList.addAll(_coords);
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

  public List<MonotonPolygon> sweepLine() {
    List<MonotonPolygon> returnList = new ArrayList<MonotonPolygon>();
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
    HashMap<LineSegment, Point> formerIsec = new HashMap<LineSegment, Point>();
    // For every Vertex in tree
    Iterator<PointType> iter = pointTree.iterator();
    while (iter.hasNext()) {
      PointType curr = iter.next();
      // Step 1
      // Add beginning edge of current Vertex
      // Mark ending edge of current vertex for removal
      switch (curr.type) {
      case INT:
        // mark every edge with curr.p as endpoint for deletion
        eList.mark(curr.p);
        // Add the edge curr.p with left or right neighbor.
        // where the neighbor with the lower y coordinate is choosen
        if (curr.left.y < curr.p.y) {
          eList.insertEdge(new LineSegment(curr.p, curr.left));
          formerIsec.put(new LineSegment(curr.p, curr.left), curr.p);
        }
        else {
          eList.insertEdge(new LineSegment(curr.p, curr.right));
          formerIsec.put(new LineSegment(curr.p, curr.right), curr.p);
        }
        break;
      case MAX:
        // Add both edges to right and left neighbour to edgelist
        eList.insertEdge(new LineSegment(curr.p, curr.right));
        formerIsec.put(new LineSegment(curr.p, curr.right), curr.p);
        eList.insertEdge(new LineSegment(curr.p, curr.left));
        formerIsec.put(new LineSegment(curr.p, curr.left), curr.p);
        break;
      case MIN:
        // Mark every edge with curr.p as endpoint for deletion
        eList.mark(curr.p);
        break;
      case HMAX:
        // Insert only the non horizontal edge
        if (MathUtils.doubleEquals(curr.right.x, curr.p.x)) {
          eList.insertEdge(new LineSegment(curr.p, curr.left));
          formerIsec.put(new LineSegment(curr.p, curr.left), curr.p);
        }
        else {
          eList.insertEdge(new LineSegment(curr.p, curr.right));
          formerIsec.put(new LineSegment(curr.p, curr.right), curr.p);
        }
        break;
      case HMIN:
        // Mark only the non horizontal edge for deletion
        eList.mark(curr.p);
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
            eList.searchIntersectingEdges(curr.p.x, curr.direct);
        Point[] intersections = sweepLineIntersect(curr, interEdge);
        // Form Polygon
        formMonontonPolygon(curr.p, intersections[0],
            eList.getIntersectionByEndPoint(curr.p)[0],
            formerIsec.get(interEdge[0]), returnList);
        // Update intersection to calculated one
        formerIsec.put(interEdge[0], intersections[0]);
      }
      // If it is MAX
      else if (curr.type == PointType.PointClass.MAX) {
        if (curr.direct == PointType.Direction.BOTH) {
          // Calculate intersection points (only two are possbile)
          LineSegment interEdge[] =
              eList.searchIntersectingEdges(curr.p.x, curr.direct);
          Point[] intersections = sweepLineIntersect(curr, interEdge);
          // Form Polygon
          formMonontonPolygon(intersections[0], intersections[1],
              formerIsec.get(interEdge[0]), formerIsec.get(interEdge[1]),
              returnList);
          // Update intersection to claculated ones
          formerIsec.put(interEdge[0], intersections[0]);
          formerIsec.put(interEdge[1], intersections[1]);
        }
      }
      // If it is MIN
      else if (curr.type == PointType.PointClass.MIN) {
        if (curr.direct == PointType.Direction.BOTH) {
          // Calculate intersection points (only two are possbile)
          LineSegment interEdge[] =
              eList.searchIntersectingEdges(curr.p.x, curr.direct);
          Point[] intersections = sweepLineIntersect(curr, interEdge);
          // Form Polygon
          // Only form first polygon if it wasn't form before because of two MIN
          // on the same line
          if (!formerIsec.get(interEdge[0]).equals(intersections[0]))
            formMonontonPolygon(curr.p, intersections[0],
                eList.getIntersectionByEndPoint(curr.p)[0],
                formerIsec.get(interEdge[0]), returnList);
          formMonontonPolygon(curr.p, intersections[1],
              eList.getIntersectionByEndPoint(curr.p)[1],
              formerIsec.get(interEdge[1]), returnList);
          // Update former intersections
          formerIsec.put(interEdge[0], intersections[0]);
          formerIsec.put(interEdge[1], intersections[1]);
        }
      }
      // If it is HMAX
      else if (curr.type == PointType.PointClass.HMAX) {
        if (curr.direct == PointType.Direction.LEFT) {
          // Calculate intersection points (only two are possbile)
          LineSegment interEdge[] =
              eList.searchIntersectingEdges(curr.p.x, curr.direct);
          Point[] intersections = sweepLineIntersect(curr, interEdge);
          // If it is another HMAX calculate snd intersection and form polygon
          if (pointHash.get(curr.right).type == PointType.PointClass.HMAX) {
            LineSegment interEdgeSecond[] =
                eList.searchIntersectingEdges(curr.right.x,
                    PointType.Direction.RIGHT);
            Point[] sndIntersections =
                sweepLineIntersect(pointHash.get(curr.right), interEdgeSecond);
            formMonontonPolygon(intersections[0], sndIntersections[0],
                formerIsec.get(interEdge[0]),
                formerIsec.get(interEdgeSecond[0]), returnList);
            // Update FormerIntersections.
            formerIsec.put(interEdge[0], intersections[0]);
            formerIsec.put(interEdgeSecond[0], sndIntersections[0]);
          }
          else {
            formMonontonPolygon(intersections[0], curr.right,
                formerIsec.get(interEdge[0]),
                eList.getIntersectionByEndPoint(curr.right)[0],
                returnList);
            // Update FormerIntersections.
            formerIsec.put(interEdge[0], intersections[0]);
          }
        }
        else if (curr.direct == PointType.Direction.RIGHT) {
          // Calculate intersection points (only two are possbile)
          LineSegment interEdge[] =
              eList.searchIntersectingEdges(curr.p.x, curr.direct);
          Point[] intersections = sweepLineIntersect(curr, interEdge);
          if (!formerIsec.get(interEdge[0]).equals(intersections[0])) {
            formMonontonPolygon(intersections[0], curr.left,
                formerIsec.get(interEdge[0]),
                eList.getIntersectionByEndPoint(curr.left)[0],
                returnList);
            // Update former intersections
            formerIsec.put(interEdge[0], intersections[0]);
          }
        }
      }
      // If it is HMIN
      else {
        if (curr.direct == PointType.Direction.NONE) {
          if (pointHash.get(curr.right).type == PointType.PointClass.HMIN) {
            formMonontonPolygon(curr.p, curr.right,
                eList.getIntersectionByEndPoint(curr.p)[0],
                eList.getIntersectionByEndPoint(curr.right)[0],
                returnList);
          }
        }
        else {
          // Calculate intersection points (only two are possbile)
          LineSegment interEdge[] =
              eList.searchIntersectingEdges(curr.p.x, curr.direct);
          Point[] intersections = sweepLineIntersect(curr, interEdge);
          formMonontonPolygon(curr.p, intersections[0],
              eList.getIntersectionByEndPoint(curr.p)[0],
              formerIsec.get(interEdge[0]), returnList);
          // Update former intersections
          formerIsec.put(interEdge[0], intersections[0]);
        }
      }

      // Step 5
      eList.removeMarkedEdges();
    }
    return returnList;
  }

  private void formMonontonPolygon(Point a, Point b, Point c, Point d,
      List<MonotonPolygon> returnList) {
    List<LineSegment> tmpList = new LinkedList<LineSegment>();
    tmpList.add(new LineSegment(a, b));
    tmpList.add(new LineSegment(a, c));
    tmpList.add(new LineSegment(b, d));
    if (!c.equals(d)) tmpList.add(new LineSegment(c, d));
    returnList.add(new MonotonPolygon(tmpList));
  }

  /**
   * This method categorizes the points accordings to the paper and defines the
   * direction for the sweep or scanline if one is thrown.
   * 
   * @param middle Point to categorize
   * @return The Point with it neighbours and type and direction.
   */
  private PointType categorizePointForSweepLine(Point middle) {
    Point left = this.getPoint((_coords.indexOf(middle) - 1) % _coords.size());
    Point right = this.getPoint((_coords.indexOf(middle) + 1) % _coords.size());
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
          middle, left, right, PointType.PointClass.MAX,
          PointType.Direction.BOTH);
      else return new PointType(middle, left, right, PointType.PointClass.MAX,
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
      Point[] intersections = sweepLine.intersect(edges[0]);
      returnArray[0] = intersections[0];
      intersections = sweepLine.intersect(edges[1]);
      returnArray[1] = intersections[0];
      return intersections;
    }
  }


  private static class EdgeList
  {
    
    private TreeMap<Point, Point[]> endStore = new TreeMap<Point, Point[]>();
    private TreeMap<Point, Point> orderedEdges = new TreeMap<Point, Point>();
    private List<Point> markedEdges = new ArrayList<Point>();
        
    
    public EdgeList(){
      
    }

    /**
     * @param ls
     */
    public void insertEdge(LineSegment ls) {

    }

    /**
     * Mark edges, specified by endPoint for deletion
     * 
     * @param endPoint
     */
    public void mark(Point endPoint) {
      markedEdges.add(endPoint);
    }

    /**
     * @param x
     * @param direct
     * @return
     */
    public LineSegment[] searchIntersectingEdges(double x,
        PointType.Direction direct) {
      return null;

    }

    /**
     * @param endPoint
     * @return This Method returns all edges ending with 'endPoint'. This can
     *         either be one for every PointClass except MIN, which would return
     *         two edges.
     */
    public Point[] getIntersectionByEndPoint(Point endPoint) {
      return null;

    }

    /**
     * Removes all marked edges from data structure.
     */
    public void removeMarkedEdges() {
      for (Point endPoint : markedEdges) {
        Point[] isecPoints = endStore.remove(endPoint);
        for (int i = 0; i < isecPoints.length; i++) {
          if (isecPoints[i] != null) {
            orderedEdges.remove(isecPoints[i]);
          }
        }
      }
      markedEdges.clear();
    }
  }


  private static class PointType
  {
    public enum PointClass {
      INT, MAX, MIN, HMAX, HMIN, IGNORE
    }


    public enum Direction {
      RIGHT, LEFT, BOTH, NONE
    }

    public PointType(Point p, Point left, Point right, PointClass type,
        Direction direct) {

      this.type = type;
      this.p = p;
      this.left = left;
      this.right = right;
      this.direct = direct;
    }

    public Point p;
    public Point left; // index(p) - 1
    public Point right; // index(p) + 1
    public PointClass type;
    public Direction direct;


    public static class PointComparator
      implements Comparator<PointType>
    {

      @Override
      public int compare(PointType pt1, PointType pt2) {

        Point p1 = pt1.p;
        Point p2 = pt2.p;

        if (p1.y > p2.y + MathUtils.EPSILON) return 1;
        else if (p1.y < p2.y - MathUtils.EPSILON) return -1;
        else {
          if (p1.x < p2.x - MathUtils.EPSILON) return 1;
          else if (p1.x > p2.x + MathUtils.EPSILON) return -1;
          else return 0;
        }
      }

    }

  }

  /**
   * @author Steve Dierker <dierker.steve@fu-berlin.de>
   * @see http://www.flipcode.com/archives/Efficient_Polygon_Triangulation.shtml
   * @category Ear-Clipping-Algorithm
   * @return List of triangulars
   */
  public List<Triangle> triangulate() {
    assert (isSimple());
    assert (isClockwise() == -1);

    List<Triangle> returnList = new ArrayList<Triangle>();

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
        returnList.add(triangle);

        /* remove v from remaining polygon */
        for (int s = v, t = v + 1; t < nv; s++, t++)
          V[s] = V[t];
        nv--;
      }
    }

    return returnList;
  }

  /**
   * Calculates the Surface Area using the Gaussian formula.
   * 
   * @author Steve Dierker <dierker.steve@fu-berlin.de>
   * @return Surface area of the polygon
   */
  public double getSurfaceArea() {
    assert (size() >= 3);

    double result = 0.0;
    for (int p = size() - 1, q = 0; q < size(); p = q++) {
      result +=
          _coords.get(p).x * _coords.get(q).y - _coords.get(q).x *
              _coords.get(p).y;
    }
    return result / 2.0;
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
