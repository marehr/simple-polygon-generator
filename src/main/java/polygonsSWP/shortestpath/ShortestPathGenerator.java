package polygonsSWP.shortestpath;

import polygonsSWP.data.History;
import polygonsSWP.data.Scene;
import polygonsSWP.geometry.LineSegment;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.geometry.Ray;
import polygonsSWP.util.MathUtils;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

/**
 * (c) 2011-2012
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Kadir Tugan <kadir.tugan@gmail.com>
 */
public class ShortestPathGenerator
{ 
  /**
   * Generates the shortest path from point s to point t in polygon p.
   * 
   * @param p a simple polygon.
   * @param s start point inside the polygon or on the boundary.
   * @param t end point, alike.
   * @param history 
   * @return shortest path as a list of points, start and end point included.
   */
  public static List<Point> generateShortestPath(OrderedListPolygon polygon, Point s, Point t, History history) {
    // s & t must lie in the _interior_ of polygon.
    assert(polygon.containsPoint(s, false) && polygon.containsPoint(t, false));
    
    if(history != null)
      history.clear();
    
    // Step 1: We need the polygon counterclockwise.
    if(polygon.isClockwise() == 1)
      polygon.reverse();
    
    // Step 2: Initialize {p,q1,q2} triple.
    Point[] parray = init(polygon, s, t);
    
    // Step 3: While we can not see t from current start point, make a step.
    List<Point> path = new LinkedList<Point>();
    while(!existsDirectConnection(polygon, parray[0], t)) {

      if(history != null) {
        Scene scene = history.newScene();
        scene.addPolygon(polygon, true);
        for(int i = 0; i < path.size() - 1; i++)
          scene.addLineSegment(new LineSegment(path.get(i), path.get(i + 1)), false);
        scene.addLineSegment(new LineSegment(parray[0], parray[1]), true);
        scene.addLineSegment(new LineSegment(parray[0], parray[2]), true);
        scene.save();
      }

      parray = makeStep(polygon, path, parray, t);
    }
    
    // Add the last point + the target point.
    path.add(parray[0]);
    path.add(t);

    if(history != null) {

      Scene scene = history.newScene();
      scene.addPolygon(polygon, true);
      for(int i = 0; i < path.size() - 1; i++)
        scene.addLineSegment(new LineSegment(path.get(i), path.get(i + 1)), Color.GREEN);
      scene.save();
    }

    return path;
  }

  private static Point[] init(OrderedListPolygon polygon, Point s, Point t) {
    
    // Find all visible vertices of polygon.
    List<Point> visiblePoints = new LinkedList<Point>();
    for(Point point : polygon.getPoints()) {
      List<Point[]> intersects = polygon.intersect(new LineSegment(s, point));
      if(intersects.size() > 1)
        continue;
      
      if(intersects.size() > 0) {
        if(intersects.get(0)[0] != null) {
          visiblePoints.add(intersects.get(0)[0]);
        }     
      }   
    }
    
    assert(visiblePoints.size() >= 2);
    
    // Initialize q1,q2 to CCW neighbouring points of visiblePoints.
    for(int i = 0, j = visiblePoints.size() - 1; i < visiblePoints.size(); j = i++) {
      Point q1 = visiblePoints.get(j);
      Point q2 = visiblePoints.get(i);
      
      Polygon remaining = reducePolygon(polygon, s, q1, q2);
      
      // If subpolygon contains t, we have found the correct initialization.
      if(remaining.containsPoint(t, true)) {
        return new Point[] {s, q1, q2};
      }
    }
    
    assert(false);
    return null;
  }

  private static OrderedListPolygon reducePolygon(OrderedListPolygon polygon, Point p,
      Point q1, Point q2) {
    // Remark: We assume that q1 and q2 both lie on the boundary of polygon, either
    // being vertices or within the interior of polygon edges, AND
    // may also lie on the same polygon edge.
    
    OrderedListPolygon reducedPolygon = new OrderedListPolygon();
    
    // Points p and q1 make the start.
    reducedPolygon.addPoint(p);
    reducedPolygon.addPoint(q1);
        
    // Find index of first vertex after q1.
    int idx = -1;
    for(int i = 0; i < polygon.size(); i++) {
      if(polygon.getPoint(i).equals(q1)) {
        idx = polygon.getIndexInRange(i + 1);
        break;
      } 
      
      if((i + 1 < polygon.size()) && polygon.getPoint(i + 1).equals(q1)) {
        idx = polygon.getIndexInRange(i + 2);
        break;
      }
      
      LineSegment ls = new LineSegment(polygon.getPoint(i), polygon.getPointInRange(i + 1));
      if(ls.containsPoint(q1)) {
        idx = polygon.getIndexInRange(i + 1);
        break;
      }
    }
    
    assert(idx != -1);
    
    // Early out: Check whether q2 lies on the same edge as q1 or q1 is a vertex and q2 lies
    // on the succeeding edge, but q2 is NOT the vertex before q1.
    // NOTE: This way we can guarantee that we get the whole polygon IF q2 actually is
    // the vertex in front of the edge where q1 resides. What we can not guarantee is the right
    // order of q1 and q2 if they lie on the same edge. However, this is not necessary for the
    // ShortestPath algorithm, so we skip the additional orientation test.
    LineSegment succEdge = new LineSegment(polygon.getPointInRange(idx - 1), polygon.getPointInRange(idx));
    if(succEdge.containsPoint(q2) && !polygon.getPointInRange(idx - 1).equals(q2)) {
      reducedPolygon.addPoint(q2);
      return reducedPolygon;
    }
    
    // Add all points until we hit q2.
    // Remark: This loop could as well be a while(true) loop, but we defensively avoid infinite looping here.
    boolean found = false;
    for(int i = idx; i < polygon.size() + idx; i++) {  
      if(polygon.getPointInRange(i).equals(q2)) {
        found = true;
        break;
      }
      
      reducedPolygon.addPoint(polygon.getPointInRange(i));
      
      if(polygon.getPointInRange(i + 1).equals(q2)) {
        found = true;
        break;
      }
            
      LineSegment ls = new LineSegment(polygon.getPointInRange(i), polygon.getPointInRange(i + 1));
      if(ls.containsPoint(q2)) {
        found = true;
        break;
      }
    }
            
    assert(found);
    
    // Put q2 at the end.
    reducedPolygon.addPoint(q2);
    
    return reducedPolygon;
  }
  
  private static boolean existsDirectConnection(OrderedListPolygon polygon,
      Point point, Point t) {
    LineSegment ls = new LineSegment(point, t);
    List<Point[]> isects = polygon.intersect(ls);
    
    // Case 1: size() == 0 means s & t can see each other directly.
    // Case 2: The other case means we have reached a polygon vertex from which we
    //         can see t.
    return (isects.size() == 0) ||
        ((isects.size() == 1) && (isects.get(0)[0].equals(point) &&
         (isects.get(0)[1] == null) && (isects.get(0)[2] == null))); 
  }
  

  private static Point[] makeStep(OrderedListPolygon polygon, List<Point> path,
      Point[] parray, Point t) {
    // Case 1: q1 is a concave vertex of P, that is, p, q1, succ(q1) is a clockwise turn.
    if(MathUtils.checkOrientation(parray[0], parray[1], succ(polygon, parray[1])) == -1) {
      Point q1n = findRayPolygonIntersection(polygon, parray[0], parray[1], false);
      // Check whether t lies in the right subpolygon.
      Polygon reducedPolygon = reducePolygon(polygon, parray[1], succ(polygon, parray[1]), q1n);
      if(reducedPolygon.containsPoint(t, false)) {
        // Output p.
        path.add(parray[0]);
          
        return new Point[] { parray[1], succ(polygon, parray[1]), q1n };
      
      } else {
        // So t must lie in the left subpolygon.
        return new Point[] { parray[0], q1n, parray[2] };
      }
    } 
    
    // Case 2: q2 is a concave vertex of P, that is, p, q2, pred(q2) constitutes a counterclockwise
    // turn.
    else if(MathUtils.checkOrientation(parray[0], parray[2], pred(polygon, parray[2])) == 1) { 
      Point q2n = findRayPolygonIntersection(polygon, parray[0], parray[2], false);
      
      // Chech whether t lies in the left subpolygon.
      Polygon reducedPolygon = reducePolygon(polygon, parray[2], q2n, pred(polygon, parray[2]));
      if(reducedPolygon.containsPoint(t, false)) {
        // Output p.
        path.add(parray[0]);
        
        return new Point[] { parray[2], q2n, pred(polygon, parray[2]) };
        
      } else {
        // t lies in the right subpolygon.
        return new Point[] { parray[0], parray[1], q2n };
      }
    } 
    
    // Case 3: The polygon P makes a convex turn at both q1 and q2. In particular, q1 and/or
    // q2 could lie in the interior of edges of P.
    else {
      
      // Check whether the ray p_succ(q1) lies within the wedge q1-p-q2.
      Point succ_q1 = succ(polygon, parray[1]);
      if((MathUtils.checkOrientation(parray[0], parray[1], succ_q1) == 1) 
          && MathUtils.checkOrientation(parray[0], parray[2], succ_q1) == -1) {
        
        // Find nearest intersection of p_succ(q1) ray with polygon (could be succ(q1)!).
        Point succq1n = findRayPolygonIntersection(polygon, parray[0], succ_q1, true);
        
        // If we hit succ(q1) first, we do not need to check if t lies in the right polygon.
        if(succq1n.equals(succ_q1))
          return new Point[] { parray[0], succq1n, parray[2] };
        
        // If we didn't hit succ(q1) first, check whether t lies in the right polygon.
        OrderedListPolygon reducedPolygon = reducePolygon(polygon, parray[0], parray[1], succq1n);
        if(reducedPolygon.containsPoint(t, false))
          return new Point[] { parray[0], parray[1], succq1n };
        else
          return new Point[] { parray[0], succq1n, parray[2] };
        
      } else {
        // Ray p_pred(q2) _must_ lie inside the wedge.
        
        Point pred_q2 = pred(polygon, parray[2]);
        Point predq2n = findRayPolygonIntersection(polygon, parray[0], pred_q2, true);
        
        if(predq2n.equals(pred_q2))
          return new Point[] { parray[0], parray[1], predq2n }; 
        
        // Check whether t lies in the left polygon.
        OrderedListPolygon reducedPolygon = reducePolygon(polygon, parray[0], predq2n, parray[2]);
        if(reducedPolygon.containsPoint(t, false)) {
          return new Point[] { parray[0], predq2n, parray[2] };
        } else {
          return new Point[] { parray[0], parray[1], predq2n }; 
        }
      }
    }
  }
  
  private static Point succ(OrderedListPolygon polygon, Point x) {
    for(int i = 0; i < polygon.size(); i++) {
      if(polygon.getPoint(i).equals(x))
        return polygon.getPointInRange(i + 1);
      
      LineSegment ls = new LineSegment(polygon.getPointInRange(i - 1), polygon.getPoint(i));
      if(ls.containsPoint(x))
        return polygon.getPoint(i);
    }
    
    assert(false);
    return null;
  }
  
  private static Point pred(OrderedListPolygon polygon, Point x) {
    for(int i = 0; i < polygon.size(); i++) {
      if(polygon.getPoint(i).equals(x))
        return polygon.getPointInRange(i - 1);
      
      LineSegment ls = new LineSegment(polygon.getPoint(i), polygon.getPointInRange(i + 1));
      if(ls.containsPoint(x))
        return polygon.getPoint(i);
    }
    
    assert(false);
    return null;
  }
  
  private static Point findRayPolygonIntersection(OrderedListPolygon polygon, Point base, Point support, boolean supportAllowed) {
    List<Point[]> isects = polygon.intersect(new Ray(base, support));
    
    double minDistance = Double.MAX_VALUE;
    Point nearestPoint = null;
    
    // Find nearest intersection.
    for(Point[] is : isects) {
      
      // Colinear edges can not happen as p is always a vertex of the polygon
      // or lies inside the interior of polygon (if p == start point).
      if(is[0] == null) {
        // BUT they can happen when p_q1 or p_q2 is an edge of the polygon.
//        if((is[1].equals(base) && is[2].equals(support)) || 
//           (is[1].equals(support) && is[2].equals(base))) {
        
        // TODO fix Polygon.intersect, as it returns [x, null, null] AND [null, x, otherVertex]
        // The above condition is more precise.
        if(is[1].equals(base) || is[1].equals(support) || is[2].equals(base) || is[2].equals(support))
          continue;
        else
           throw new RuntimeException("Not in general position.");
      }
        
      // p is always a vertex of the polygon.
      if(is[0].equals(base) && is[1] == null && is[2] == null)
        continue;
      
      // Same for q1.
      if(is[0].equals(support) && is[1] == null && is[2] == null && !supportAllowed)
        continue;
      
      double distance = base.distanceTo(is[0]);
      if(distance < minDistance) {
        minDistance = distance;
        nearestPoint = is[0];
      }
    }
    
    assert(nearestPoint != null);
    
    return nearestPoint;
  }
}
