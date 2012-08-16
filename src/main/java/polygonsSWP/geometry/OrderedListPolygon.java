package polygonsSWP.geometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import polygonsSWP.util.Random;
import polygonsSWP.util.MathUtils;


/**
 * Implementation of Polygon using ordered counter-clockwise list. The
 * assumption is, that the list of polygons is ordered in order of appearance.
 * So the polygon is drawn from point one to point two to .. to point n to point
 * 1. So be sure your self added point list meets the assumption otherwise it
 * won't work.
 * 
 * (c) 2011-2012
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Kadir Tugan <kadir.tugan@gmail.com>
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

  /**
   * @author Steve Dierker <dierker.steve@fu-berlin.de>
   * @see http://www.flipcode.com/archives/Efficient_Polygon_Triangulation.shtml
   * @category Ear-Clipping-Algorithm
   * @return List of triangulars
   */
  public List<Triangle> triangulate() {
    if (_triangles != null) return _triangles;

    assert size() >= 3;
    //assert (isSimple());
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
}
