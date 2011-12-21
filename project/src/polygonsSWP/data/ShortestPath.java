package polygonsSWP.data;

import java.util.ArrayList;
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
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 */
public class ShortestPath
{
  private ArrayList<Point> _path = new ArrayList<Point>();
  private OrderedListPolygon _polygon;
  private Point [] parray = new Point[3];


  /**
   * Generates an empty shortest path for polygon.
   * 
   * @param polygon Polygon in which is shortest path.
   * @param start Start point of path.
   * @param end End point of path.
   */
  public ShortestPath(Polygon polygon, Point start, Point end) {
    _polygon = (OrderedListPolygon) polygon;
    _path.add(start);
    _path.add(end);
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
  
  public List<Point> generateShortestPath()
  {
	  	// TODO: implement
	  	List<Polygon> plist = getAllTrapezoid();
	  	
		Polygon startPolygon = null;
		for(Polygon p : plist)
		{
			if(p.containsPoint(_path.get(0), true))
			{
				if(p.containsPoint(_path.get(_path.size()-1), true))
				{
					return _path;
				}
				else
				{
					startPolygon = p;
					break;
				}
			}
			//e(t) ?
		}
				
		initVars(startPolygon);
		parray[0] = _path.get(0);
		while(_polygon.intersect(new LineSegment(parray[0],_path.get(_path.size()-1))).size() != 0)
		{
			parray = makeStep(parray[0],parray[2],parray[2]);
		}
		
		return _path;
	}
  
  	private void initVars(Polygon startPolygon)
  	{
  		// find subpolygon which contains endpoint
  		// init q1,q2 (parray[1],parray[2])
  		// Figure 9 p. 17
  	}
  		
  	/*
  	 * TODO: description
  	 */
	private Point[] makeStep(Point p, Point q1, Point q2)
	{
		reducePolygon(p,q1,q2);
		if(isConcaveVertex(q1, p, _polygon))
		{
			//TODO: cannot be null?
			Point newP = findRayPolygonIntersection(p,q1,_polygon);
			if(tLiesInSubPolygon(q1,newP))
			{
				Point [] returnArray = {q1,succ(q1),newP};
				return returnArray;
			}
			else
			{
				Point [] returnArray = {p,newP,q2};
				return returnArray;
			}
		}
		else if(isConcaveVertex(q2, p, _polygon))
		{
			Point newP = findRayPolygonIntersection(p,q2,_polygon);
			if(tLiesInSubPolygon(q2,newP))
			{
				Point [] returnArray = {q2,newP,pred(q1)};
				return returnArray;
			}
			else
			{
				Point [] returnArray = {p,q1,newP};
				return returnArray;
			}
		}
		else
		{
			if(rayLiesInWedge(p,succ(q1),q1,p,q2))
			{
				Point newP = findRayPolygonIntersection(p,succ(q1),_polygon);
				if(tLiesInSubPolygon(q1,newP))
				{
					Point [] returnArray = {p,q1,newP};
					return returnArray;
				}
				else
				{
					Point [] returnArray = {p,newP,q2};
					return returnArray;
				}
			}
			else
			{
				Point newP = findRayPolygonIntersection(p,pred(q2),_polygon);
				if(tLiesInSubPolygon(q2,newP))
				{
					Point [] returnArray = {p,newP,q2};
					return returnArray;
				}
				else
				{
					Point [] returnArray = {p,q1,newP};
					return returnArray;
				}
			}
		}
	}
	

	private boolean rayLiesInWedge(Point p, Point succP, Point q1, Point p2,Point q2) {
		Ray ray = new Ray(p,succP);
		return false;
	}

	private Point succ(Point p) {
		List<Point> list = _polygon.getPoints();
		for (int i = 0; i < list.size(); i++) {
			if(list.get(i).compareTo(p) == 0)
			{
				if(i < list.size()-1)
					return list.get(i+1);
				else
					return list.get(0);
			}
		}
		return null;
	}
	
	private Point pred(Point p) {
		List<Point> list = _polygon.getPoints();
		for (int i = 0; i < list.size(); i++) {
			if(list.get(i).compareTo(p) == 0)
			return list.get(i-1);
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
		for (int i = 0; i < polygon.getPoints().size()-1; i++) {
			Point [] intersectingPoints = ray.intersect(new LineSegment(polygon.getPoint(i), polygon.getPoint(i+1)));
			//TODO: Why does Ray.intersect(LineSegment) method return an array?
			if(intersectingPoints.length > 0)
			{
				newP = intersectingPoints[0];
				break;
			}
		}
		return newP;
	}
	
	private boolean tLiesInSubPolygon(Point q1,Point newP)
	{
		OrderedListPolygon reducedPolygon = new OrderedListPolygon();
		reducedPolygon.addPoint(q1);
		List<Point> plist = _polygon.getPoints();
		plist = sortList(q1,plist);
		
		for (int i = 0; i < plist.size()-1; i++) {
			LineSegment ls = new LineSegment(plist.get(i),plist.get(i+1));
			if(ls.containsPoint(newP))
			{
				if(plist.get(i).compareTo(newP) != 0)
					reducedPolygon.addPoint(plist.get(i));
				reducedPolygon.addPoint(newP);
				break;
			}
			else
			{
				reducedPolygon.addPoint(plist.get(i));	
			}
		}
		
		if(reducedPolygon.containsPoint(getLastPoint(), true))
		{
			//TODO: insert history stuff here
			return true; 
		}
		else
			return false;
	}
	
	/*
	 * Sorts a list of point and
	 * @return same list with given point at first position
	 */
	private List<Point> sortList(Point q1, List<Point> list) {
		int index = getIndexOfPoint(q1,list);
		
		if(index == 0)
		{
			return list;			
		}
		else if(index == list.size()-1)
		{
			List<Point> tmp = new ArrayList<Point>();
			tmp.add(list.get(list.size()-1));
			tmp.addAll(list.subList(0, list.size()-2));
			return tmp;
		}
		else
		{
			List<Point> tmp = new ArrayList<Point>();
			tmp.addAll(list.subList(index, list.size()-1));
			tmp.addAll(list.subList(0, index-1));
			return tmp;
		}
	}

	private OrderedListPolygon reducePolygon(Point p, Point q1, Point q2) {
		//TODO: may not work at start (if q1 = q2)
		OrderedListPolygon reducedPolygon = new OrderedListPolygon();
		reducedPolygon.addPoint(q1);
		List<Point> plist = _polygon.getPoints();
		
		plist = sortList(q1, plist);

		for (int i = 0; i < plist.size()-1; i++) {
			LineSegment ls = new LineSegment(plist.get(i),plist.get(i+1));
			if(ls.containsPoint(q2))
			{
				if(plist.get(i).compareTo(q2) != 0)
					reducedPolygon.addPoint(plist.get(i));
				reducedPolygon.addPoint(q2);
				reducedPolygon.addPoint(p);
				break;
			}
			else
			{
				reducedPolygon.addPoint(plist.get(i));
			}			
		}
		
		//TODO: exists a case where this will fail?
		if(reducedPolygon.containsPoint(getLastPoint(), true))
		{
			//TODO: insert history stuff here
			return (_polygon = reducedPolygon);
		}
		else
		{
			//TODO:
			return null;
		}
		
	}
	
	/*
	 * @return index of point in polygon path list, -1 if not in list
	 */
	private int getIndexOfPoint(Point q,List<Point> list) {
		int index = -1;
		for (int i = 0; i < list.size(); i++) {
			if(q.compareTo(list.get(i)) == 0)
			{
				index = i;
				break;
			}
		}
		return index;
	}

	/*
	 * This method checks whether a point is concave point of the polygon:
	 * conave => succ(q) or pred(q) (!= p) is not visible from q 
	 * convex => succ(q) or pred(q) (!= p) is visible from q 
	 * @return true if concave, false if convex
	 */
	private boolean isConcaveVertex(Point q, Point p, OrderedListPolygon polygon)
	{
		// Doubling the pointlist to prevent the three searched points to get splitted.
		List<Point> temp = new ArrayList<Point>();
		temp.addAll(polygon.getPoints());
		temp.addAll(polygon.getPoints());
		Point [] polygonPoints = (Point[]) temp.toArray();
		
		for (int i = 0; i < polygonPoints.length; i++) {
			if(polygonPoints[i].compareTo(q) == 0)
			{
				if(polygonPoints[i+1].compareTo(p) == 0)
				{
					if (MathUtils.checkOrientation(q, p, polygonPoints[i-1]) == -1)
						return false;
					if (MathUtils.checkOrientation(q, p, polygonPoints[i-1]) == 1)
						return true;
				}	
				else if(polygonPoints[i-1].compareTo(p) == 0)
				{
					if (MathUtils.checkOrientation(q, p, polygonPoints[i+1]) == -1)
						return true;
					if (MathUtils.checkOrientation(q, p, polygonPoints[i+1]) == 1)
						return false;
				}
				else
				{
					// should never happen
					System.out.println("Something went wrong here!");
				}
			}
		}
		return false;
	}

	/**
	   * This method seperates the polygon into trapezoid 
	   * @return a list of Polygons
	   */
	private List<Polygon> getAllTrapezoid()
	{
		return null;
	}
	
	/*
	 * @return the last point in _path
	 */
	private Point getLastPoint(){return _path.get(_path.size()-1);}
  
  
}
