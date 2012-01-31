package polygonsSWP.data;

import java.util.ArrayList;
import java.util.List;

import polygonsSWP.geometry.LineSegment;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.geometry.Ray;
import polygonsSWP.geometry.Trapezoid;
import polygonsSWP.util.MathUtils;

/**
 * Implementation of the shortest path object. Assumption is, that every
 * shortest path needs an polygon to which it is associated and that it needs to
 * no the start, end and every point on the path. So the path is saved in an
 * ordered list starting with start and always ending on the end point.
 * 
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 */
public class ShortestPath
{
  private ArrayList<Point> _path = new ArrayList<Point>();
  private OrderedListPolygon _polygon;
  private Point[] parray = new Point[3];

  /**
   * Generates an empty shortest path for polygon.
   * 
   * @param polygon Polygon in which is shortest path.
   * @param start Start point of path.
   * @param end End point of path.
   */
  public ShortestPath(Polygon polygon, Point start, Point end) {
//    _polygon = (OrderedListPolygon) polygon;
//    _path.add(start);
//    _path.add(end);
	  OrderedListPolygon p = new OrderedListPolygon();
	  p.addPoint(new Point(38.0,260.0));
	  p.addPoint(new Point(294.0,535.0));//
	  p.addPoint(new Point(346.0,533.0));
	  p.addPoint(new Point(318.0,525.0));
	  p.addPoint(new Point(190.0,186.0));
	  p.addPoint(new Point(536.0,207.0));
	  p.addPoint(new Point(409.0,538.0));
	  p.addPoint(new Point(258.0,587.0));//
	  
	  Point startPoint = new Point(90.0,330.0);
	  Point endPoint = new Point(220.0,210.0);
      _polygon = p;
      //_path.add(startPoint);
      _path.add(endPoint);
  }

  /**
   * Adds a new point to the path. Its always in front of the end point.
   * 
   * @param p Next point on path.
   */
  public void addPointToPath(Point p) {
    _path.add(_path.size() - 1, p);
  }

  /**
   * Adds point on position.
   * 
   * @param p Point.
   * @param i Position.
   */
  public void addPointToPathOnPosition(Point p, int i) {
    _path.add(i, p);
  }

  /**
   * Deletes point on path.
   * 
   * @param p Point to delete.
   */
  public void deletePointOnPath(Point p) {
    _path.remove(p);
  }

  /**
   * Returns path.
   * 
   * @return Ordered list with points.
   */
  public ArrayList<Point> getPath() {
    return _path;
  }

  /**
   * Returns associated polygon.
   * 
   * @return Polygon.
   */
  public Polygon getPolygon() {
    return _polygon;
  }

  public List<Point> generateShortestPath() {
//    List<Trapezoid> plist = _polygon.sweepLine();
//
//    Trapezoid startTrapezoid = null;
//    for (Trapezoid p : plist) {
//      if (p.containsPoint(_path.get(0), true)) {
//        if (p.containsPoint(_path.get(_path.size() - 1), true)) {
//          return _path;
//        }
//        else {
//          startTrapezoid = p;
//          break;
//        }
//      }
//      // TODO: do we need: e(t) ?
//    }
//
//    initVars((OrderedListPolygon) startTrapezoid);
    parray[1] = new Point(294.0,535.0);
    parray[2] = new Point(258.0,587.0);
	  
    parray[0] = new Point(90.0,330.0);
    while (!existsDirectConnection()) {
      parray = makeStep(parray[0], parray[1], parray[2]);
    }

    return _path;
  }



private void initVars(OrderedListPolygon startPolygon) {
    List<Point> list = startPolygon.sortByY();
    int len = list.size();

    // TODO: init correctly

    if (tLiesInSubPolygon(list.get(0), list.get(11))) {
      parray[1] = list.get(0);
      parray[2] = list.get(1);
    }
    else if (tLiesInSubPolygon(list.get(len - 1), list.get(len - 2))) {
      parray[1] = list.get(len - 1);
      parray[2] = list.get(len - 2);
    }

    // find subpolygon which contains endpoint
    // init q1,q2 (parray[1],parray[2])
    // Figure 9 p. 17
  }

	private boolean existsDirectConnection() {
		List<Point[]> list = _polygon.intersect(new LineSegment(parray[0], _path.get(_path.size() - 1)));
		if(list.size() == 0)
		{
			addPointToPath(parray[0]);
			return true;
		}
		else if((list.size() == 1) && (list.get(0)[0].compareTo(parray[0]) == 0))
		{
			addPointToPath(parray[0]);
			return true;
		}
		else
			return false;
	}


  /*
   * TODO: description
   */
  private Point[] makeStep(Point p, Point q1, Point q2) {
    reducePolygon(p, q1, q2);
    if (isConcaveVertex(p, q1, _polygon)) {
      Point newP = findRayPolygonIntersection(p, q1, _polygon);
      if (tLiesInSubPolygon(q1, newP)) {
        Point[] returnArray = { q1, succ(q1), newP };
        addPointToPath(p);
        return returnArray;
      }
      else {
        Point[] returnArray = { p, newP, q2 };
        addPointToPath(p);
        return returnArray;
      }
    }
    else if (isConcaveVertex(p, q2, _polygon)) {
      Point newP = findRayPolygonIntersection(p, q2, _polygon);
      if (tLiesInSubPolygon(q2, newP)) {
        Point[] returnArray = { q2, newP, pred(q2) };
        addPointToPath(p);
        return returnArray;
      }
      else {
        Point[] returnArray = { p, q1, newP };
        addPointToPath(p);
        return returnArray;
      }
    }
    else {
      if (rayLiesInWedge(p, succ(q1), q1, p, q2)) {
        Point newP = findRayPolygonIntersection(p, succ(q1), _polygon);
        if (tLiesInSubPolygon(q1, newP)) {
          Point[] returnArray = { p, q1, newP };
          addPointToPath(p);
          return returnArray;
        }
        else {
          Point[] returnArray = { p, newP, q2 };
          addPointToPath(p);
          return returnArray;
        }
      }
      else {
        Point newP = findRayPolygonIntersection(p, pred(q2), _polygon);
        if (tLiesInSubPolygon(q2, newP)) {
          Point[] returnArray = { p, newP, q2 };
          addPointToPath(p);
          return returnArray;
        }
        else {
          Point[] returnArray = { p, q1, newP };
          addPointToPath(p);
          return returnArray;
        }
      }
    }
  }

  private boolean rayLiesInWedge(Point p, Point succP, Point q1, Point p2, Point q2) {
    return ((MathUtils.checkOrientation(p, q2, succP) <= 0) && (MathUtils.checkOrientation(p, q1, succP) >= 0));
  }

  private Point succ(Point p) {
    List<Point> list = _polygon.getPoints();
    for (int i = 0; i < list.size(); i++) {
      if (list.get(i).compareTo(p) == 0) {
        return list.get((i + 1)%list.size());
      }
    }
    return null;
  }

  private Point pred(Point p) {
    List<Point> list = _polygon.getPoints();
    for (int i = 0; i < list.size(); i++) {
      if (list.get(i).compareTo(p) == 0) return list.get(i - 1);
    }
    return null;
  }

  /*
   * creates a ray starting from p with direction q1 and
   * @return the intersection with given polygon or null
   */
  private Point findRayPolygonIntersection(Point p, Point q1, OrderedListPolygon polygon) {
    Ray ray = new Ray(p, q1);
    Point newP = null;
    List<Point> pointList = polygon.getPoints();
    pointList = sortList(q1,pointList);
    for (int i = 1; i < pointList.size() - 1; i++) {
      Point[] intersectingPoints = ray.intersect(new LineSegment(pointList.get(i),pointList.get(i + 1)));
      // TODO: Why does Ray.intersect(LineSegment) method return an array?
      if ((intersectingPoints != null) && (intersectingPoints.length > 0)) {
	        newP = intersectingPoints[0];
	        break;
	      }
    }
    return newP;
  }

  private boolean tLiesInSubPolygon(Point q1, Point newP) {
    OrderedListPolygon reducedPolygon = new OrderedListPolygon();
    reducedPolygon.addPoint(q1);
    List<Point> plist = _polygon.getPoints();
    plist = sortList(q1, plist);

    for (int i = 0; i < plist.size() - 1; i++) {
      LineSegment ls = new LineSegment(plist.get(i), plist.get(i + 1));
      if (ls.containsPoint(newP)) {
        if (plist.get(i).compareTo(newP) != 0)
          reducedPolygon.addPoint(plist.get(i));
        reducedPolygon.addPoint(newP);
        break;
      }
      else {
        reducedPolygon.addPoint(plist.get(i));
      }
    }

    if (reducedPolygon.containsPoint(getLastPoint(), true)) {
      // TODO: insert history stuff here
      return true;
    }
    else return false;
  }

  /*
   * Sorts a list of point and
   * @return same list with given point at first position
   */
  public static List<Point> sortList(Point q1, List<Point> list) {
    int index = getIndexOfPoint(q1, list);

    if (index == 0) {
      return list;
    } else {
      List<Point> tmp = new ArrayList<Point>();
	  tmp.addAll(list.subList(index, list.size()));
	  tmp.addAll(list.subList(0, index));
      return tmp;
    }
  }

  // This works only if q1 is counter clockwise the next point after p
  // is this sufficient?
  private OrderedListPolygon reducePolygon(Point p, Point q1, Point q2) {
    // TODO: may not work at start (if q1 = q2)
    OrderedListPolygon reducedPolygon = new OrderedListPolygon();
    reducedPolygon.addPoint(q1);
    List<Point> plist = _polygon.getPoints();

    plist = sortList(q1, plist);

    for (int i = 1; i < plist.size() - 1; i++) {
      LineSegment ls = new LineSegment(plist.get(i), plist.get(i + 1));
      if (ls.containsPoint(q2)) {
        if (plist.get(i).compareTo(q2) != 0){
          reducedPolygon.addPoint(plist.get(i));
          reducedPolygon.addPoint(q2);
          reducedPolygon.addPoint(p);
          break;
        }
      }
      else {
        reducedPolygon.addPoint(plist.get(i));
      }
    }

    // TODO: exists a case where this will fail?
    if (reducedPolygon.containsPoint(getLastPoint(), true)) {
      // TODO: insert history stuff here
      return (_polygon = reducedPolygon);
    }
    else {
      // TODO:
      return null;
    }

  }

  /*
   * @return index of point in polygon path list, -1 if not in list
   */
  private static int getIndexOfPoint(Point q, List<Point> list) {
    int index = -1;
    for (int i = 0; i < list.size(); i++) {
      if (q.compareTo(list.get(i)) == 0) {
        index = i;
        break;
      }
    }
    return index;
  }

  /*
   * This method checks whether a point is concave point of the polygon: conave
   * => succ(q) or pred(q) (!= p) is not visible from q convex => succ(q) or
   * pred(q) (!= p) is visible from q
   * @return true if concave, false if convex
   */
  private boolean isConcaveVertex(Point p, Point q, OrderedListPolygon polygon) {
    List<Point> temp = polygon.getPoints();
    temp = sortList(p,temp);
    if(temp.get(1).compareTo(q) == 0)
    	return (MathUtils.checkOrientation(p, q, temp.get(2)) == -1);
    else if(temp.get(temp.size()-1).compareTo(q) == 0)
    	return (MathUtils.checkOrientation(p, q, temp.get(temp.size()-2)) == 1);
    else
    {
        System.out.println("Polygon has not been reduced correctly");
        return false;
    }
    
  }

  /*
   * @return the last point in _path
   */
  private Point getLastPoint() {
    return _path.get(_path.size() - 1);
  }

}
