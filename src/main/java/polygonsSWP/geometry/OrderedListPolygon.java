package polygonsSWP.geometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import polygonsSWP.util.GeneratorUtils;
import polygonsSWP.util.Random;
import polygonsSWP.util.EdgeList;
import polygonsSWP.util.MathUtils;
import polygonsSWP.util.PointType;
import polygonsSWP.util.PointType.Direction;
import polygonsSWP.util.SweepLineResult;


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
    
    LineSegment a = new LineSegment(new Point(0,0),new Point(0,0));
    LineSegment b = new LineSegment(new Point(0,0),new Point(0,0));
    
    for (int i = 0; i < size - 1; i++) {
      a._a = _coords.get(i);
      a._b = _coords.get(i + 1);

      // Then test the remaining line segments for intersections
      for (int j = i + 1; j < size; j++) {
        b._a = _coords.get(j);
        b._b = _coords.get((j + 1) % size);

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

    return is.get(Random.create().nextInt(is.size()));
  }

  /**
   * @return the number of vertices in the polygon
   */
  public int size() {
    return _coords.size();
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
        // Get the only intersecting edge possible for direction
        Point searchPoint = eList.getIntersectionByEndPoint(curr.p)[0];
        LineSegment interEdge[] = { null, null };
        System.out.println("Edge to search with: " + searchPoint + curr.p);
        if (curr.direct == Direction.LEFT) interEdge[0] =
            eList.getLeftEdge(searchPoint, curr.p, curr.type);
        else interEdge[0] = eList.getRightEdge(searchPoint, curr.p, curr.type);
        // TODO: remove: eList.searchIntersectingEdges(curr.p, curr.direct);
        // TODO: remove: debug statements
        for (int i = 0; i < 2; ++i) {
          if (interEdge[i] != null)
            System.out.println("Edge for Intersection: " + interEdge[i]);
        }
        SweepLineResult interSect = sweepLineIntersect(curr, interEdge);
        System.out.println(interSect);
        // Form Polygon
        if (curr.direct == PointType.Direction.RIGHT) {
          // Get right former points
          Point[] former = eList.getIntersectionByEndPoint(interEdge[0]._a);
          Point formerIntersect;
          if (former[1] == null) formerIntersect = former[0];
          else {
            if (interEdge[0].containsPoint(former[0])) formerIntersect =
                former[0];
            else formerIntersect = former[1];
          }
          formMonontonPolygon(curr.p, interSect.rightIntersect,
              eList.getIntersectionByEndPoint(curr.p)[0], formerIntersect,
              returnList);
          // Update intersection to calculated one
          eList.updateIntersection(interEdge[0]._a, interEdge[0]._b,
              interSect.rightIntersect);
        }
        else {
          // Get right former Points
          Point[] former = eList.getIntersectionByEndPoint(curr.p);
          Point formerCurr;
          if (former[1] == null) formerCurr = former[0];
          else formerCurr = former[1];
          former = eList.getIntersectionByEndPoint(interEdge[0]._a);
          Point formerIntersect;
          if (former[1] == null) formerIntersect = former[0];
          else {
            if (interEdge[0].containsPoint(former[0])) formerIntersect =
                former[0];
            else formerIntersect = former[1];
          }
          formMonontonPolygon(interSect.leftIntersect, curr.p, formerIntersect,
              formerCurr, returnList);
          eList.updateIntersection(interEdge[0]._a, interEdge[0]._b,
              interSect.leftIntersect);
        }
      }
      // If it is MAX
      else if (curr.type == PointType.PointClass.MAX) {
        if (curr.direct == PointType.Direction.BOTH) {
          // Calculate intersection points (only two are possbile)
          LineSegment interEdge[] =
              { eList.getLeftEdge(curr.p, curr.left, curr.type),
                  eList.getRightEdge(curr.p, curr.right, curr.type) };
          // TODO: remove: eList.searchIntersectingEdges(curr.p, curr.direct);
          // TODO: remove: debug
          for (int i = 0; i < 2; ++i) {
            if (interEdge[i] != null)
              System.out.println("Edge for Intersection: " + interEdge[i]);
          }
          SweepLineResult interSect = sweepLineIntersect(curr, interEdge);
          System.out.println(interSect);
          // Get right intersection points:
          Point[] former = eList.getIntersectionByEndPoint(interEdge[1]._a);
          Point formerRight;
          Point formerLeft;
          if (former[1] == null) formerRight = former[0];
          else {
            if (interEdge[1].containsPoint(former[0])) formerRight = former[0];
            else formerRight = former[1];
          }
          former = eList.getIntersectionByEndPoint(interEdge[0]._a);
          if (former[1] == null) formerLeft = former[0];
          else {
            if (interEdge[0].containsPoint(former[0])) formerLeft = former[0];
            else formerLeft = former[1];
          }
          // Form Polygon
          formMonontonPolygon(interSect.leftIntersect,
              interSect.rightIntersect, formerLeft, formerRight, returnList);
          // Update intersection to claculated ones
          eList.updateIntersection(interEdge[0]._a, interEdge[0]._b,
              interSect.leftIntersect);
          eList.updateIntersection(interEdge[1]._a, interEdge[1]._b,
              interSect.rightIntersect);
        }
      }
      // If it is MIN
      else if (curr.type == PointType.PointClass.MIN) {
        if (curr.direct == PointType.Direction.BOTH) {
          // First get intersection points
          Point[] interPoints = eList.getIntersectionByEndPoint(curr.p);
          Point left =
              interPoints[0].x < interPoints[1].x
                  ? interPoints[0]
                  : interPoints[1];
          Point right =
              interPoints[1].x < interPoints[0].x
                  ? interPoints[0]
                  : interPoints[1];
          System.out.println("Left Edge: " + left + " " + curr.p);
          System.out.println("Right Edge: " + right + " " + curr.p);
          LineSegment interEdge[] =
              { eList.getLeftEdge(left, curr.p, curr.type),
                  eList.getRightEdge(right, curr.p, curr.type) };
          // TODO: remove: eList.searchIntersectingEdges(curr.p, curr.direct);
          // TODO: remove: debug
          for (int i = 0; i < 2; ++i) {
            if (interEdge[i] != null)
              System.out.println("Edge for Intersection: " + interEdge[i]);
          }
          SweepLineResult interSect = sweepLineIntersect(curr, interEdge);
          System.out.println(interSect);

          // Form Polygon
          // Only form first polygon if it wasn't form before because of two MIN
          // on the same line
          // Is it the right intersection point? If not get the other one.
          System.out.println("Intersection Points by End Point");
          Point[] former = eList.getIntersectionByEndPoint(interEdge[1]._a);
          Point leftInt;
          if (former[1] == null) leftInt = former[0];
          else {
            if (interEdge[1].containsPoint(former[0])) leftInt = former[0];
            else leftInt = former[1];
          }
          Point rightInt;
          former = eList.getIntersectionByEndPoint(interEdge[0]._a);
          if (former[1] == null) rightInt = former[0];
          else {
            if (interEdge[0].containsPoint(former[0])) rightInt = former[0];
            else rightInt = former[1];
          }
          Point formerOne = eList.getIntersectionByEndPoint(curr.p)[0];
          Point formerTwo = eList.getIntersectionByEndPoint(curr.p)[1];
          System.out.println("Left:");
          System.out.println("  " + formerOne);
          System.out.println("  " + leftInt);
          System.out.println("Right:");
          System.out.println("  " + formerTwo);
          System.out.println("  " + rightInt);
          if (!eList.getIntersectionByEndPoint(interEdge[0]._a)[0].equals(interSect.leftIntersect))
            formMonontonPolygon(curr.p, interSect.leftIntersect, formerOne,
                rightInt, returnList);
          formMonontonPolygon(curr.p, interSect.rightIntersect, formerTwo,
              leftInt, returnList);
          // Update former intersections
          System.out.println("Update 1: " + interEdge[0]._a + " " +
              interEdge[0]._b + " " + interSect.leftIntersect);
          eList.updateIntersection(interEdge[0]._a, interEdge[0]._b,
              interSect.leftIntersect);
          System.out.println("Update 1: " + interEdge[1]._a + " " +
              interEdge[1]._b + " " + interSect.rightIntersect);
          eList.updateIntersection(interEdge[1]._a, interEdge[1]._b,
              interSect.rightIntersect);
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
          SweepLineResult interSect = sweepLineIntersect(curr, interEdge);
          System.out.println(interSect);

          // If it is another HMAX calculate snd intersection and form polygon
          if (pointHash.get(curr.right).type == PointType.PointClass.HMAX) {
            LineSegment interEdgeSecond[] =
                eList.searchIntersectingEdges(curr.right,
                    PointType.Direction.RIGHT);
            SweepLineResult sndResult =
                sweepLineIntersect(pointHash.get(curr.right), interEdgeSecond);
            formMonontonPolygon(interSect.leftIntersect,
                sndResult.rightIntersect,
                eList.getIntersectionByEndPoint(interEdge[0]._a)[0],
                eList.getIntersectionByEndPoint(interEdgeSecond[0]._a)[0],
                returnList);
            // Update FormerIntersections.
            eList.updateIntersection(interEdge[0]._a, interEdge[0]._b,
                interSect.leftIntersect);
            eList.updateIntersection(interEdgeSecond[0]._a,
                interEdgeSecond[0]._b, sndResult.rightIntersect);
          }
          else {
            formMonontonPolygon(interSect.leftIntersect, curr.right,
                eList.getIntersectionByEndPoint(interEdge[0]._a)[0],
                eList.getIntersectionByEndPoint(curr.right)[0], returnList);
            // Update FormerIntersections.
            eList.updateIntersection(interEdge[0]._a, interEdge[0]._b,
                interSect.leftIntersect);
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
          SweepLineResult interSect = sweepLineIntersect(curr, interEdge);
          System.out.println(interSect);

          if (!eList.getIntersectionByEndPoint(interEdge[0]._a)[0].equals(interSect.rightIntersect)) {
            formMonontonPolygon(interSect.rightIntersect, curr.left,
                eList.getIntersectionByEndPoint(interEdge[0]._a)[0],
                eList.getIntersectionByEndPoint(curr.left)[0], returnList);
            // Update former intersections
            eList.updateIntersection(interEdge[0]._a, interEdge[0]._b,
                interSect.rightIntersect);
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
          SweepLineResult interSect = sweepLineIntersect(curr, interEdge);
          formMonontonPolygon(curr.p, interSect.rightIntersect,
              eList.getIntersectionByEndPoint(curr.p)[0],
              eList.getIntersectionByEndPoint(interEdge[0]._a)[0], returnList);
          // Update former intersections
          eList.updateIntersection(interEdge[0]._a, interEdge[0]._b,
              interSect.rightIntersect);
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
  private SweepLineResult sweepLineIntersect(PointType p, LineSegment[] edges) {
    // TODO: check if endpoints of lines count as intersections
    SweepLineResult result = new SweepLineResult();
    System.out.println("SweepLineIntersect");
    for (LineSegment item : edges)
      if (item != null) System.out.println(item);
    if (p.direct == PointType.Direction.RIGHT) {
      Ray sweepLine = new Ray(p.p, new Point(p.p.x + 1, p.p.y));
      Point[] intersections = sweepLine.intersect(edges[0]);
      result.rightEdge = edges[0];
      result.rightIntersect = intersections[0];
      return result;
    }
    else if (p.direct == PointType.Direction.LEFT) {
      Ray sweepLine = new Ray(p.p, new Point(p.p.x - 1, p.p.y));
      Point[] intersections = sweepLine.intersect(edges[0]);
      result.leftEdge = edges[0];
      result.leftIntersect = intersections[0];
      return result;
    }
    else {
      Line sweepLine = new Line(p.p, new Point(p.p.x + 1, p.p.y));
      Point[] intersectionOne = sweepLine.intersect(edges[0]);
      Point[] intersectionTwo = sweepLine.intersect(edges[1]);
      result.leftIntersect = intersectionOne[0];
      result.rightIntersect = intersectionTwo[0];
      return result;
    }
  }

  /**
   * @author Steve Dierker <dierker.steve@fu-berlin.de>
   * @see http://www.flipcode.com/archives/Efficient_Polygon_Triangulation.shtml
   * @category Ear-Clipping-Algorithm
   * @return List of triangulars
   */
  public List<Triangle> triangulate() {
    if (_triangles != null) return _triangles;

    assert size() >= 3;
    assert (isSimple());
    if(isClockwise() != -1) reverse();
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
    return GeneratorUtils.sortPointsByX(new ArrayList<Point>(_coords));
  }

  /*
   * !Doesnt change the actual values
   * @return list of coords sorted by y value of each point
   */
  public List<Point> sortByY() {
    return GeneratorUtils.sortPointsByY(new ArrayList<Point>(_coords));
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
