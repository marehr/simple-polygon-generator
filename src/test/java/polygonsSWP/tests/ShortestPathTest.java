package polygonsSWP.tests;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import polygonsSWP.geometry.LineSegment;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Ray;
import polygonsSWP.util.MathUtils;

/**
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 */
public class ShortestPathTest {
	
  @Test
  public void getLastPoint()
  {
	  ArrayList<Point> list = new ArrayList<Point>();
	  list.add(new Point(1.0,1.0));
	  list.add(new Point(2.0,2.0));
	  list.add(new Point(3.0,3.0));
	  list.add(new Point(4.0,4.0));
	  
	  Point o = list.get(list.size() - 1);
	  assertTrue(o.x == 4.0);
	  assertTrue(o.y == 4.0); 
  }
  
  @Test
  public void getIndexOfPoint() 
  {
	  ArrayList<Point> list = new ArrayList<Point>();
	  list.add(new Point(1.0,1.0));
	  Point q = new Point(2.0,2.0);
	  list.add(q);
	  list.add(new Point(3.0,3.0));
	  list.add(new Point(4.0,4.0));
	  
      int index = -1;
      for (int i = 0; i < list.size(); i++) {
        if (q.compareTo(list.get(i)) == 0) {
          index = i;
          break;
        }
      }
      assertTrue(index == 1);
  }
  
  @Test
  public void sortList() {
	  
	  ArrayList<Point> list = new ArrayList<Point>();
	  list.add(new Point(1.0,1.0));
	  list.add(new Point(2.0,2.0));
	  Point q1 = new Point(3.0,3.0);
	  list.add(q1);
	  list.add(new Point(4.0,4.0));
	  list.add(new Point(5.0,5.0));
	  list.add(new Point(6.0,6.0));
	  
	  int index = 2;
	  List<Point> tmp = new ArrayList<Point>();
	  
	  tmp.addAll(list.subList(index, list.size()));
	  tmp.addAll(list.subList(0, index));
	  
	  assertTrue(tmp.get(0).x == q1.x);
  }
  
  @Test
  public void reducePolygon()
  {
	  OrderedListPolygon poly = new OrderedListPolygon();
	  Point q1 = new Point(294.0,535.0);
	  Point q2 = new Point(258.0,587.0);
	  Point p = new Point(90.0,330.0);
	  
	  poly.addPoint(new Point(38.0,260.0));
	  poly.addPoint(q1);
	  poly.addPoint(new Point(346.0,533.0));
	  poly.addPoint(new Point(318.0,525.0));
	  poly.addPoint(new Point(190.0,186.0));
	  poly.addPoint(new Point(536.0,207.0));
	  poly.addPoint(new Point(409.0,538.0));
	  poly.addPoint(q2);
	  

	  OrderedListPolygon reducedPolygon = new OrderedListPolygon();
	  reducedPolygon.addPoint(q1);
	  List<Point> plist = poly.getPoints();

	  Collections.sort(plist);
	  
      for (int i = 1; i < plist.size() - 1; i++) {
	    LineSegment ls = new LineSegment(plist.get(i), plist.get(i + 1));
	    if (ls.containsPoint(q2)) {
	      if (plist.get(i).compareTo(q2) != 0){
	        reducedPolygon.addPoint(plist.get(i));
	        reducedPolygon.addPoint(q2);
	        reducedPolygon.addPoint(p);
	        break;
	      }
	    } else {
	      reducedPolygon.addPoint(plist.get(i));
	    }
      }
      
//      for(Point o : reducedPolygon.getPoints())
//    	  System.out.println(o.x + " " + o.y);
  }
  
  @Test
  public void findRayPolygonIntersection() {
	  
	  OrderedListPolygon poly = new OrderedListPolygon();
	  Point q1 = new Point(294.0,535.0);
	  Point q2 = new Point(258.0,587.0);
	  Point p = new Point(90.0,330.0);
	  
	  poly.addPoint(new Point(38.0,260.0));
	  poly.addPoint(q1);
	  poly.addPoint(new Point(346.0,533.0));
	  poly.addPoint(new Point(318.0,525.0));
	  poly.addPoint(new Point(190.0,186.0));
	  poly.addPoint(new Point(536.0,207.0));
	  poly.addPoint(new Point(409.0,538.0));
	  poly.addPoint(q2);
	  
	  
	    Ray ray = new Ray(p, q1);
	    Point newP = null;
	    List<Point> pointList = poly.getPoints();
	    Collections.sort(pointList);
	    for (int i = 1; i < pointList.size() - 1; i++) {
	      Point[] intersectingPoints = ray.intersect(new LineSegment(pointList.get(i),pointList.get(i + 1)));
	      // TODO: Why does Ray.intersect(LineSegment) method return an array?
	      if ((intersectingPoints != null) && (intersectingPoints.length > 0)) {
	        newP = intersectingPoints[0];
	        break;
	      }
	    }
//	    System.out.println(newP.x + " " + newP.y);
	  }
  
  @Test
  public void isConcaveVertex()
  {
	  OrderedListPolygon poly = new OrderedListPolygon();
	  Point q1 = new Point(294.0,535.0);
	  Point q2 = new Point(258.0,587.0);
	  Point p = new Point(90.0,330.0);
	  
	  poly.addPoint(p);
	  poly.addPoint(q1);
	  poly.addPoint(new Point(346.0,533.0));
	  poly.addPoint(new Point(318.0,525.0));
	  poly.addPoint(new Point(190.0,186.0));
	  poly.addPoint(new Point(536.0,207.0));
	  poly.addPoint(new Point(409.0,538.0));
	  poly.addPoint(q2);
	  

	    List<Point> temp = poly.getPoints();
	    Collections.sort(temp);
	    if(temp.get(1).compareTo(q2) == 0)
	    	System.out.println((MathUtils.checkOrientation(p, q2, temp.get(2)) == 1));
	    else if(temp.get(temp.size()-1).compareTo(q2) == 0)
	    	System.out.println((MathUtils.checkOrientation(p, q2, temp.get(temp.size()-2)) == -1));
	    else
	    {
	        System.out.println("Polygon has not been reduced correctly");
	        System.out.println("false");
	    }
  }
  
  @Test
  public void polygonIntersection()
  {
	  OrderedListPolygon poly = new OrderedListPolygon();
	  Point q1 = new Point(294.0,535.0);
	  Point q2 = new Point(258.0,587.0);
	  Point p = new Point(346.0,533.0);
	  
	  poly.addPoint(new Point(90.0,330.0));
	  poly.addPoint(q1);
	  poly.addPoint(p);
	  poly.addPoint(new Point(318.0,525.0));
	  poly.addPoint(new Point(190.0,186.0));
	  poly.addPoint(new Point(536.0,207.0));
	  poly.addPoint(new Point(409.0,538.0));
	  poly.addPoint(q2);
	  
	  List<Point[]> list = poly.intersect(new LineSegment(p, new Point(220.0,210.0)));
	  for(Point[] i : list)
	  {
		  for (int j = 0; j < i.length; j++) {
			  System.out.println(i[j].x + " " + i[j].y);
		  }
	  }
  }
	
//  @Test
//  public void reducePolygon(){
//	  OrderedListPolygon p = new OrderedListPolygon();
//	  p.addPoint(new Point(38.0,260.0));
//	  p.addPoint(new Point(318.0,525.0));
//	  p.addPoint(new Point(346.0,533.0));
//	  p.addPoint(new Point(294.0,535.0));
//	  p.addPoint(new Point(190.0,186.0));
//	  p.addPoint(new Point(536.0,207.0));
//	  p.addPoint(new Point(409.0,538.0));
//	  p.addPoint(new Point(258.0,587.0));
//	  
//	  38 260
//	  318 525
//	  346 533
//	  294 535
//	  190 186
//	  536 207
//	  409 538
//	  258 587
//	  
//	  Point startPoint = new Point(90.0,330.0);
//	  Point endPoint = new Point(220.0,210.0);
//	  
//	  Point p = startPoint;
//	  Point q1 = new Point(258.0,587.0);
//	  Point q2 = new Point(294.0,535.0);
// 
//      OrderedListPolygon reducedPolygon = new OrderedListPolygon();
//      reducedPolygon.addPoint(q1);
//      List<Point> plist = p.getPoints();
//
//      plist = sortList(q1, plist);
//
//      for (int i = 0; i < plist.size() - 1; i++) {
//        LineSegment ls = new LineSegment(plist.get(i), plist.get(i + 1));
//        if (ls.containsPoint(q2)) {
//          if (plist.get(i).compareTo(q2) != 0)
//            reducedPolygon.addPoint(plist.get(i));
//          reducedPolygon.addPoint(q2);
//          reducedPolygon.addPoint(p);
//          break;
//        }
//        else {
//          reducedPolygon.addPoint(plist.get(i));
//        }
//      }
//
//    // TODO: exists a case where this will fail?
//      if (reducedPolygon.containsPoint(getLastPoint(), true)) {
//      // TODO: insert history stuff here
//        return (p = reducedPolygon);
//      }
//      else {
//      // TODO:
//        return null;
//      }
//  }

}
