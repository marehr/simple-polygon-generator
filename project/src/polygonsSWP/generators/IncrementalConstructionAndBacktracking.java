package polygonsSWP.generators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import polygonsSWP.data.PolygonHistory;
import polygonsSWP.geometry.LineSegment;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.util.GeneratorUtils;

public class IncrementalConstructionAndBacktracking implements PolygonGenerator
{

  @Override
  public String[] getAcceptedParameters() {
    return new String[] {"n", "points", "size"};
  }
  
  @Override
  public String toString() {
    return "Incremental Construction & Backtracking";
  }

  @Override
  public Polygon generate(Map<String, Object> params, PolygonHistory steps) {
    List<Point> points = GeneratorUtils.createOrUsePoints(params);
    
    // Precalculate the convex hull of points
    OrderedListPolygon ch = GeneratorUtils.convexHull(points);
    
    Random r = new Random(System.currentTimeMillis());  
    
    // Keep track of unusable edges
    EdgeSet ue = new EdgeSet(points);
    
    // Polygon (represented by indices relative to points list).
    List<Integer> polygon = new ArrayList<Integer>();
    
    // List of indices to construct the polygon of.
    List<Integer> idx = new ArrayList<Integer>();
    for(int i = 0; i < points.size(); i++)
      idx.add(new Integer(i));
    
    // Remember which points we've already used
    List<Integer> used = new ArrayList<Integer>();
    do {
    
      // Choose initial point and remember choice
      int i = 0;
      do {
        i = r.nextInt(idx.size());
      } while(used.contains(idx.get(i)));
        
      Integer fp = idx.remove(i);
      used.add(fp);
      
      // Recursively create polygon
      List<Integer> pp = new ArrayList<Integer>();
      pp.add(fp);
      polygon = recursivelyAddPoint(ue, pp, idx, ch, points);
      
      // If no success -> add back fp
      if(polygon == null)
        idx.add(fp);
      
      assert(used.size() < idx.size());
    
    } while(polygon == null);
    
    // Create polygon from index list.
    OrderedListPolygon olp = new OrderedListPolygon();
    while(!polygon.isEmpty())
      olp.addPoint(points.get(polygon.remove(0)));
    
    return olp;
  }
  
  protected List<Integer> recursivelyAddPoint(EdgeSet unusable, 
      List<Integer> chain, 
      List<Integer> remaining,
      final OrderedListPolygon convexHull,
      final List<Point> points) {
    assert(remaining.size() > 0);
    
    Random r = new Random(System.currentTimeMillis());
    
    // Remember used points (elements in 'remaining' list)
    List<Integer> used = new ArrayList<Integer>();
    
    List<Integer> polygon = null;
    
    addPointLoop:
    while(polygon == null && used.size() < remaining.size() ) {
      
      // Last point
      Integer lp = chain.get(chain.size() - 1);
      
      // Grab next unused index
      int idx = -1;
      List<Integer> remtmp = new ArrayList<Integer>(remaining);
      do {
        idx = r.nextInt(remtmp.size());
        Integer i = remtmp.remove(idx);
        
        if(used.contains(i) || unusable.isMarked(lp, i))
          idx = -1;
        
      } while((idx == -1) && (remtmp.size() > 0));
      
      // We couldn't find an usable index.
      if(idx == -1)
        break addPointLoop;
      
      // Remember that we already tried idx.
      used.add(remaining.get(idx));
      
      
      // **********************************
      // Ok, this iteration: Use point idx.
      
      // Clone everything for a fresh start
      EdgeSet ue = unusable.clone();
      List<Integer> rem = new ArrayList<Integer>(remaining);
      List<Integer> pp =  new ArrayList<Integer>(chain);
      
      // Remove point from remaining points
      Integer np = rem.remove(idx);
      
      // Add point to polygon chain
      pp.add(np);
      
      // Mark all edges intersecting edge lp-np
      ue.markRule1(lp, np);
      
      // Mark all incident unmarked edges, if there are 
      // two neighbors with only 2 unmarked edges.
      ue.markRule2(np, rem);
      
      // Is backtracking necessary?
      boolean do_backtracking = false;
      
      // First condition:
      // Each point that does not yet belong to the polygo-
      // nal chain under construction has at least two inci-
      // dent unmarked edges. 
      do_backtracking = !ue.condition1(rem);
      
      // Second condition:
      // At most one point adjacent to the point last added
      // has only two incident unmarked edges.
      do_backtracking = do_backtracking || !ue.condition2(np);
      
      // Third condition:
      // Points that lie on the boundary of CH(S) appear
      // in the polygonal chain in the same relative order
      // as on the hull.    
      do_backtracking = do_backtracking || !condition3(pp, convexHull, points);
      
      // Fourth condition:
      // If last point, check that it connects to 1st point without intersections.
      if(rem.size() == 0) {
        Integer fp = pp.get(0);
        do_backtracking = do_backtracking || ue.isMarked(np, fp);
        
        // Recursion ends here.
        if(!do_backtracking)
          return pp;
      }
      
      // Now, if all conditions are satisfied, try to add another point.
      if(!do_backtracking)
        polygon = recursivelyAddPoint(ue, pp, rem, convexHull, points);
    }
    
    return polygon;    
  }
  
  protected boolean condition3(List<Integer> polygon, 
                             OrderedListPolygon convexHull, 
                             List<Point> points) {
    // Points that lie on the boundary of CH(S) appear
    // in the polygonal chain in the same relative order
    // as on the hull.   
    boolean oneway = testOrderOnHull(polygon, convexHull, points);
    convexHull.reverse();
    boolean oranother = oneway || testOrderOnHull(polygon, convexHull, points);
    return oranother;
  }
  
  protected boolean testOrderOnHull(List<Integer> polygon, 
                             OrderedListPolygon convexHull, 
                             List<Point> points) {
    int lastchidx = 0;
    int chidx = 0;
    int roundtrip = -1;
    for(Integer idx : polygon) {
      Point p = points.get(idx);
      if(convexHull.containsVertex(p)) {
        
        boolean ok = false;
        while(!ok && roundtrip < convexHull.size()) {
          if(convexHull.getPointInRange(chidx).equals(p)) {
            ok = true;
            
            if(roundtrip == -1)
              roundtrip = 0;
            else
              roundtrip += chidx - lastchidx;
              
            lastchidx = chidx;
          }
          
          chidx++;
        }
        
        if(!ok || roundtrip > convexHull.size())
          return false;
      }
    }
    
    return true;
  }
  
  protected static class EdgeSet {
    boolean s[][];
    List<Point> v;
    
    EdgeSet(List<Point> vertices) {
      v = vertices;
      s = new boolean[v.size()][v.size()];
    }
    
    protected EdgeSet(List<Point> vertices, boolean[][] set) {
      v = vertices;
      s = new boolean[v.size()][v.size()];
      for(int i = 0; i < v.size(); i++) {
        for(int j = i + 1; j < v.size() - 1; j++) {
          s[i][j] = set[i][j];
        }
      }
    }
    
    @Override
    protected EdgeSet clone() {
      return new EdgeSet(v, s);
    }
    
    void markRule1(int i, int j) {
      LineSegment ls = new LineSegment(v.get(i), v.get(j));
      Point[] isect = new Point[1];
      for(int m = 0; m < v.size() - 1; m++) {
        for(int n = m + 1; n < v.size(); n++) {
                  
          LineSegment ls2 = new LineSegment(v.get(m), v.get(n));
          
          if(ls.isIntersecting(ls2, isect, true)) {
            markEdge(m, n);
          }
        }
      }
    }
    
    void markRule2(int i, List<Integer> points) {
      // Furthermore, if a point is adjacent
      // to two other points that both have only two incident
      // unmarked edges, we mark all the other edges incident
      // upon that point.

                 
      int[] n = new int[2];
      int count = 0;

      // Iterate through all neighbors j of point i  
      for(Integer j : points) {
        if(i != j && !isMarked(i, j)) {
          
          // Has only two incident unmarked edges?
          if(degree(j) == 2) {
            count++;
                       
            assert(count < 3);
            n[count - 1] = j;
          }
        }
      }
        
      // Mark all edges apart from those leading to n[0] & n[1].
      if(count == 2) {
        for(int j = 0; j < v.size(); j++) {
          if(j != i && j != n[0] && j != n[1])
            markEdge(i, j);
        }
      }
    }
    
    protected boolean condition1(List<Integer> rem) {
      // Each point that does not yet belong to the polygo-
      // nal chain under construction has at least two inci-
      // dent unmarked edges.     
      for(Integer point : rem) {
        if(degree(point) < 2)
          return false;
      }
      
      return true;
    }
    protected boolean condition2(int np) {
      // At most one point adjacent to the point last added
      // has only two incident unmarked edges.      
      // Iterate through all points adjacent to np.
      
      int count = 0;
      for(int i = 0; i < v.size(); i++) {
        if(i != np && !isMarked(i, np)) {
                     
          // If only two unmarked edges
          if(degree(i) <= 2)
            count++;
        }
      }
      
      return count <= 1;
    }
    
    protected boolean isMarked(int i, int j) {
      assert(i != j);
      return (i < j) ? s[i][j] : s[j][i];
    }
    
    /**
     * @param i index of point
     * @return number of unmarked edges
     */
    protected int degree(int i) {
      int count = 0;
      for(int j = 0; j < v.size(); j++) {
        if(i != j && !isMarked(i, j))
          count++;
      }
      return count;
    }
    
    protected void markEdge(int i, int j) {
      assert(i != j);
      if(i < j)
        s[i][j] = true;
      else
        s[j][i] = true;
    }
  }

}
