package polygonsSWP.data;

import java.util.ArrayList;
import java.util.List;

import polygonsSWP.geometry.LineSegment;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;


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
		while(_polygon.intersect(new LineSegment(parray[0],_path.get(_path.size()))).size() == 0)
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
  		
	private Point[] makeStep(Point p, Point q1, Point q2)
	{
		Polygon reducedPolygon = reducePolygon(p,q1,q2);
		return null;
	}
	
	private Polygon reducePolygon(Point p, Point q1, Point q2) {
		
		OrderedListPolygon reducedPolygon = new OrderedListPolygon();
		
		reducedPolygon.addPoint(q1);
		List<Point> plist = _polygon.getPoints();

		int index = 0;
		for (int i = 0; i < plist.size(); i++) {
			if(q1.compareTo(plist.get(i)) == 0)
			{
				index = i;
				break;
			}
		}
		
		if(plist.size() > index)
		{
			List<Point> newList = new ArrayList<Point>();
			newList.addAll(plist.subList(index + 1, plist.size()-1));
			newList.addAll(plist.subList(0, index));
			plist = newList;
		}

		for (int i = 0; i < plist.size(); i++) {
			try
			{
				LineSegment ls = new LineSegment(plist.get(i),plist.get(i+1));
				if(ls.containsPoint(q2))
				{
					reducedPolygon.addPoint(plist.get(i));
					reducedPolygon.addPoint(q2);
					reducedPolygon.addPoint(p);
					break;
				}
			}catch(Exception e){}
			
			if(plist.get(i).compareTo(q2) == 0)
			{
				reducedPolygon.addPoint(q2);
				reducedPolygon.addPoint(p);
				break;
			}
			reducedPolygon.addPoint(plist.get(i));
		}
			
		return reducedPolygon;
	}
	
	/*
	 * This method checks whether a point is concave point of the polygon
	 */
	public boolean isConcaveVertex(Point p, OrderedListPolygon polygon)
	{
		
	}

	/**
	   * This method seperates the polygon into trapezoid 
	   * @return a list of Polygons
	   */
	private List<Polygon> getAllTrapezoid()
	{
		return null;
	}
  
  
}
