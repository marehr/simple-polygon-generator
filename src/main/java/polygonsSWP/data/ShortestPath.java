package polygonsSWP.data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import polygonsSWP.geometry.LineSegment;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.geometry.Ray;
import polygonsSWP.util.MathUtils;

/**
 * Implementation of the shortest path object. Assumption is, that every
 * shortest path needs an polygon to which it is associated and that it needs to
 * no the start, end and every point on the path. So the path is saved in an
 * ordered list starting with start and always ending on the end point.
 *
 **/
public class ShortestPath
{
  private ArrayList<Point> _path = new ArrayList<Point>();
  private OrderedListPolygon _polygon;
  private Point[] parray = new Point[3];
  private History _history = null;
  private Point _start = null;
  private Point _end = null;
  private OrderedListPolygon _org_polygon;
  private boolean debug = false;

  /**
   * Generates an empty shortest path for polygon.
   *
   * @param polygon Polygon in which is shortest path.
   * @param start Start point of path.
   * @param end End point of path.
   * @param history
   */
  public ShortestPath(Polygon polygon, Point start, Point end, History history) {
      _polygon = (OrderedListPolygon) polygon;
      _org_polygon = _polygon.clone();
      _start = start;
      _end = end;
      _path.add(end);
      _history = history;
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
   * @return Polygon.
   **/
  public Polygon getPolygon() {
    return _polygon;
  }

  public List<Point> generateShortestPath() {

	if(debug){
		puts("Startpoint " + _start.x + " | " + _start.y);
		puts("Endpoint " + _path.get(_path.size()-1).x + " | " + _path.get(_path.size()-1).y);
		puts("Starting with Polygon:");
		puts(_polygon);
	}

	// We need a polygon with counter clockwise orientation!

	if(_polygon.isClockwise() == 1)
	  _polygon.reverse();

	init(_polygon);

    while (!existsDirectConnection()) {
	  if(debug){
		  puts("-------------------");
		  puts("Current Path:");
		  puts(_path);
		  puts("-------------------");
	  }
	  parray = makeStep(parray[0], parray[1], parray[2]);
    }

    return _path;
  }

  /*
   *  Initialize the wedge s,q1,q2 that t lies in the subpolygon
   */
  private void init(OrderedListPolygon p) {

	  List<Point> list = p.getPoints();
	  List<Point> visiblePoints = new LinkedList<Point>();

	  if(debug){
	    puts("Init q1 ,q2 ...");
	    puts("Visible points:");
	  }

	  for(Point point : list)
	  {
	    List<Point[]> intersects = _polygon.intersect(new LineSegment(_start,point));
	    if(intersects.size() > 1)
	      continue;

	    if(intersects.size() > 0)
	    {
	      if(intersects.get(0)[0] != null)
		    {
	        visiblePoints.add(intersects.get(0)[0]);
	        if(debug){puts(intersects.get(0)[0].x + " " + intersects.get(0)[0].y);}
		    }
	    }
	  }

	  parray[0] = _start;

	  for(int i=0;i < visiblePoints.size();i++)
	  {
	    parray[0] = _start;
	    parray[1] = visiblePoints.get(i);
	    parray[2] = visiblePoints.get((i+1) % visiblePoints.size());
	    if(reducePolygon(parray[0], parray[1], parray[2], false) != null)
	    {
		    if(debug){
		      puts("Correct init:");
		      puts("q1= " + pto_s(parray[1]));
		      puts("q2= " + pto_s(parray[2]));
		    }
		    break;
	    }
	  }
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
   * This function analyzes the current situation and decides how to reduce the polygon
   * @return new triple of points
   */
  private Point[] makeStep(Point p, Point q1, Point q2) {
    reducePolygon(p, q1, q2,true);
    if (isConcaveVertex(p, q1, _polygon)) {
      Point newP = findRayPolygonIntersection(p, q1, _polygon);
      if (tLiesInSubPolygon(q1, newP)) {
        Point[] returnArray = { q1, succ(q1), newP };
        addPointToPath(p);
        return returnArray;
      }
      else {
        Point[] returnArray = { p, newP, q2 };
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
        return returnArray;
      }
    }
    else {
      if (rayLiesInWedge(p, succ(q1), q1, p, q2)) {
        Point newP = findRayPolygonIntersection(p, succ(q1), _polygon);
        if (tLiesInSubPolygon(q1, newP)) {
          Point[] returnArray = { p, q1, newP };
          return returnArray;
        }
        else {
          Point[] returnArray = { p, newP, q2 };
          return returnArray;
        }
      }
      else {
        Point newP = findRayPolygonIntersection(p, pred(q2), _polygon);
        if (tLiesInSubPolygon(q2, newP)) {
          Point[] returnArray = { p, newP, q2 };
          return returnArray;
        }
        else {
          Point[] returnArray = { p, q1, newP };
          return returnArray;
        }
      }
    }
  }

  private boolean rayLiesInWedge(Point p, Point succP, Point q1, Point p2, Point q2) {
    return ((MathUtils.checkOrientation(p, q2, succP) < 0) && (MathUtils.checkOrientation(p, q1, succP) > 0));
  }

  /**
   * @return the next polygon point
   * **/
  private Point succ(Point p) {
    List<Point> list = _polygon.getPoints();
    for (int i = 0; i < list.size(); i++) {
      if (list.get(i).compareTo(p) == 0) {
        return list.get((i + 1)%list.size());
      }
    }
    return null;
  }

  /**
   * @return the previous polygon point
   * **/
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
    Ray ray = new Ray(p,q1);
	Point newP = null;
	List<Point[]> res = _polygon.intersect(ray);

	if(res.size() == 2)
	  return res.get(1)[0];

	if(res.size() > 0)
	{
	  Point tri [] = res.get(res.size()-1);
	  if((tri[0] != null) && (tri[1] != null) && (tri[2] != null))
	    return tri[0];
	  else if((tri[0] != null) && (tri[1] == null) && (tri[2] == null))
	    return tri[0];
	  else
	  {
	    for(int i = res.size()-1; i >= 0;i--)
		{
		  tri = res.get(i);
		  if((tri[0] != null) && (tri[1] != null) && (tri[2] != null))
		    return tri[0];
		  }
		}
	  }
	  else
	  {
		  if(debug){puts("Something is wrong here");}
		  return null;
	  }
	  return newP;
  }

  /**
   * Reduce the polygon and
   * @return if we are moving towards t
   * **/

  private boolean tLiesInSubPolygon(Point q1, Point newP) {
	  List<Point []> tri_list =_polygon.intersect(new LineSegment(q1, newP));
	  for(int i = 0;i < tri_list.size() ;i++)
	  {
	    Point [] tri = tri_list.get(i);
	    if((tri[0] == null) && (tri[1].equals(q1) || tri[1].equals(newP)) && (tri[2].equals(q1) || tri[2].equals(newP)))
	      return false;
	  }

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
      return true;
    }
    else return false;
  }

  /*
   * Sorts a list of point and
   * @return same list with given point at first position
   */
  public List<Point> sortList(Point q1, List<Point> list) {
    int index = getIndexOfPoint(q1, list);
    if (index == 0) {
      return list;
    }
    else
    {
      List<Point> tmp = new ArrayList<Point>();
	  tmp.addAll(list.subList(index, list.size()));
	  tmp.addAll(list.subList(0, index));
      return tmp;
    }

  }

  /**
   * Reduce the polygon
   * @param force: if true the current polygon will be overwritten by the reduced one
   * **/
  private OrderedListPolygon reducePolygon(Point p, Point q1, Point q2, boolean force){

	if(debug){
	  puts("--------------------");
	  puts("Reducing polygon");
	  puts("p= " + pto_s(p));
	  puts("q1= " + pto_s(q1));
	  puts("q2= " + pto_s(q2));
    }

	boolean correct = false;
    OrderedListPolygon reducedPolygon = new OrderedListPolygon();
    List<Point> plist = _polygon.getPoints();
    plist = sortList(q1, plist);
    for (int i = 0; i < plist.size() - 1; i++) {
      LineSegment ls = new LineSegment(plist.get(i), plist.get((i+1)%plist.size()));
      if (ls.containsPoint(q2)) {
        if (plist.get(i).compareTo(q2) != 0){
          reducedPolygon.addPoint(plist.get(i));
          reducedPolygon.addPoint(q2);
          reducedPolygon.addPoint(p);
          correct = true;
          break;
        }
      }
      else {
        reducedPolygon.addPoint(plist.get(i));
      }
    }

    if(!correct)
    {
    	if(debug){puts("Swapping q1,q2");}
    	reducePolygon(p,q2,q1,force);
    }

    if (reducedPolygon.containsPoint(getLastPoint(), true)) {
      if(force)
         return (_polygon = reducedPolygon);
      else
    	 return reducedPolygon;
    }
    else {
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
    	if(debug){puts("Polygon has not been reduced correctly");}
        return false;
    }

  }

  /*
   * @return the last point in _path
   */
  private Point getLastPoint() {
    return _path.get(_path.size() - 1);
  }

  private void puts(Polygon p)
  {
	for(Point o : p.getPoints())
	{
	  System.out.println(o.x + " | " + o.y);
	}
  }

  private void puts(String s)
  {
    System.out.println(s);
  }

  private void puts(List<Point> l)
  {
	for(Point p : l)
	  puts(p.x + "|" + p.y + " ; ");

	puts("");
  }

  private String pto_s(Point p)
  {
    return Double.toString(p.x) + "|" + Double.toString(p.y);
  }

}
