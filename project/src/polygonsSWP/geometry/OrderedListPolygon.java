package polygonsSWP.geometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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

  public void permute() {
    Collections.shuffle(_coords);
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
    for (int i = 0; i < size - 1; i++) {
      LineSegment a = new LineSegment(_coords.get(i), _coords.get(i + 1));
      for (int j = i + 1; j < size; j++) {
        LineSegment b = new LineSegment(_coords.get(j), _coords.get((j + 1) % size));

        if (a.isIntersecting(b)) retval.add(new Integer[] { i, j });
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

  @Override
  public Polygon clone() {
    List<Point> nList = new ArrayList<Point>();
    nList.addAll(_coords);
    return new OrderedListPolygon(nList);
  }

  /**
   * Tests if p is inside the given Polygon.
   * <http://geosoft.no/software/geometry/Geometry.java.html> Added a test to
   * check if Point is on line.
   * 
   * @author Steve Dierker <dierker.steve@fu-berlin.de>
   * @param p Point to be checked if it is in polygon
   * @param onLine whether a point lying on an edge is counted as in or out of
   *          polygon
   * @return True if Point is in/on Polygon, otherwise false
   */
  public boolean containsPoint(Point p, boolean onLine) {
    List<Point> pList = this.getPoints();
    boolean isInside = false;
    int nPoints = pList.size();
    Point first = pList.get(pList.size() - 1);

    int j = 0;
    for (int i = 0; i < nPoints; i++) {
      j++;
      if (j == nPoints) j = 0;

      if (pList.get(i).y < p.y && pList.get(j).y >= p.y ||
          pList.get(j).y < p.y && pList.get(i).y >= p.y) {
        if (pList.get(i).x + (double) (p.y - pList.get(i).y) /
            (double) (pList.get(j).y - pList.get(i).y) *
            (pList.get(j).x - pList.get(i).x) < p.y) {
          isInside = !isInside;
        }
      }
      if (onLine)
        if (MathUtils.checkOrientation(first, pList.get(i), p) == 0) { return true; }
      first = pList.get(i);
    }
    return isInside;
  }

  /**
   * Calculates the Surface Area of a given Polygon. TODO: currently uses the
   * Triangularization of the given Polygon to calculate and add resulting
   * Triangles. Gaussian Formula should be more effective.
   * 
   * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
   * @param polygon Polygon to calculate Size of.
   * @return Surface Area of given Polygon2
   */
  public double getSurfaceArea() {
    List<Point> polygonPoints = this.getPoints();
    if (polygonPoints.size() == 3) {
      return this.calcualteSurfaceAreaOfTriangle(this);
    }
    else {
      double surfaceArea = 0;
      List<Polygon> triangularization = this.triangulate();
      for (Polygon polygon2 : triangularization) {
        surfaceArea += this.calcualteSurfaceAreaOfTriangle(polygon2);
      }
      return surfaceArea;
    }
  }

  /**
   * Triangulate Polygon with O(n^2) algorithm TODO: implement at least O(n log
   * n ) algorithm The algorithm assumes that the polygon is ordered clockwise.
   * Since our assumption is counter-clockwise I reverse the order first.
   * 
   * @author Steve Dierker <dierker.steve@fu-berlin.de>
   * @see http://wiki.delphigl.com/index.php/Ear_Clipping_Triangulierung
   * @category Ear-Clipping-Algorithm
   * @param poly Polygon to triangulate
   * @return List of triangulars
   */
  public List<Polygon> triangulate() {
    OrderedListPolygon triPo = (OrderedListPolygon) this.clone();
    System.out.println("Polygon: " + triPo.getPoints());
    List<Polygon> triangles = new ArrayList<Polygon>();
    int i = 0, orientation = -1;
    boolean isConvex;
    while (triPo.size() != 3) {
      // Search three neighbors in polygon list
      Point pR = triPo.getPoints().get(triPo.getIndexInRange(i - 1)), pM =
          triPo.getPoints().get(triPo.getIndexInRange(i)), pL =
          triPo.getPoints().get(triPo.getIndexInRange(i + 1));
      // Check if convex or concave
      if (MathUtils.checkOrientation(pR, pL, pM) == orientation) {
        isConvex = true;
        if (orientation == 1) orientation = -1;
      }
      else isConvex = false;
      if (isConvex) {
        // Check if any point of the polygon intersects with the chosen
        // triangle.
        boolean inTriangle = false;
        // Create Triangle
        List<Point> triPoint = new ArrayList<Point>();
        triPoint.add(pR);
        triPoint.add(pM);
        triPoint.add(pL);
        OrderedListPolygon triangle = new OrderedListPolygon(triPoint);
        for (Point item : triPo.getPoints()) {
          if (triPoint.contains(item)) continue;
          if (triangle.containsPoint(item, false)) {
            inTriangle = true;
            break;
          }
        }
        // If no point is in Triangle
        if (!inTriangle) {
          triangles.add(new OrderedListPolygon(triPoint));
          // Delete middle vertex
          triPo.getPoints().remove(pM);
          --i;
        }
      }
      if (++i > triPo.getPoints().size()) {
        i = 0;
        orientation = 1;
      }
    }
    // If only 3 points are left add them to triangle list
    Point pR = triPo.getPoints().get(triPo.getIndexInRange(0)), pM =
        triPo.getPoints().get(triPo.getIndexInRange(1)), pL =
        triPo.getPoints().get(triPo.getIndexInRange(2));
    List<Point> triPoint = new ArrayList<Point>();
    triPoint.add(pR);
    triPoint.add(pM);
    triPoint.add(pL);
    triangles.add(new OrderedListPolygon(triPoint));
    return triangles;
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
    // Triangulate given Polygon.
    List<Polygon> triangularization = this.triangulate();
    // Randomly choose one Triangle of Triangularization weighted by their
    // Surface Area.
    Polygon chosenPolygon = this.selectRandomTriangleBySize(triangularization);
    // Randomly choose Point in choosen Triangle.
    Point randomPoint = this.createRandomPointInTriangle(chosenPolygon);
    return randomPoint;
  }
}
