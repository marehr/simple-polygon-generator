package polygonsSWP.generators;

import java.util.ArrayList;
import java.util.Collections;
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
  public Polygon generate(Map<String, Object> params, PolygonHistory steps) {
    List<Point> points = GeneratorUtils.createOrUsePoints(params);
    
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
      polygon = recursivelyAddPoint(ue, pp, idx);
      
      // If no success -> add back fp
      if(polygon == null)
        idx.add(fp);
      
      // TODO remove
      assert(used.size() < idx.size());
    
    } while(polygon == null);
    
    // Create polygon from index list.
    OrderedListPolygon olp = new OrderedListPolygon();
    while(!polygon.isEmpty())
      olp.addPoint(points.get(polygon.remove(0)));
    
    return olp;
  }
  
  private List<Integer> recursivelyAddPoint(EdgeSet unusable, 
      List<Integer> chain, 
      List<Integer> remaining) {
    
    Random r = new Random(System.currentTimeMillis());
    
    // Remember used points (elements in 'remaining' list)
    List<Integer> used = new ArrayList<Integer>();
    
    List<Integer> polygon = null;
    while(polygon == null && used.size() < remaining.size() ) {
      
      // Grab next unused index
      int idx = -1;
      do {
        idx = r.nextInt(remaining.size());
      } while(used.contains(remaining.get(idx)));
      used.add(remaining.get(idx));
      
      // Clone everything for a fresh start
      EdgeSet ue = unusable.clone();
      List<Integer> rem = new ArrayList<Integer>();
      Collections.copy(remaining, rem);
      List<Integer> pp =  new ArrayList<Integer>();
      Collections.copy(chain, pp);
      
      // Remove point from remaining points
      Integer np = rem.remove(idx);

      // Last point
      Integer lp = pp.get(pp.size() - 1);
      
      // Add point to polygon chain
      pp.add(np);
      
      // Mark all edges intersecting edge lp-np
      ue.markRule1(lp, np);
      
      // Mark all incident unmarked edges, if there are 
      // two neighbors with only 2 unmarked edges.
      ue.markRule2(np);
      
      // Is backtracking necessary?
      boolean do_backtracking = false;
      
      // First condition:
      // Each point that does not yet belong to the polygo-
      // nal chain under construction has at least two inci-
      // dent unmarked edges. 
      do_backtracking = !ue.test1stCond(rem);
      
      // Second condition:
      // At most one point adjacent to the point last added
      // has only two incident unmarked edges.
      do_backtracking = do_backtracking || !ue.test2ndCond(np);
      
      // Third condition:
      // Points that lie on the boundary of CH(S) appear
      // in the polygonal chain in the same relative order
      // as on the hull.
      do_backtracking = do_backtracking || !ue.test3rdCond();
      
      // TODO Fourth condition: connect last point to first point?
      
      // Now, if all conditions are satisfied, try to add another point.
      if(!do_backtracking)
        polygon = recursivelyAddPoint(ue, pp, rem);
    }
    
    return polygon;    
  }
  
  private static class EdgeSet {
    boolean s[][];
    List<Point> v;
    
    EdgeSet(List<Point> vertices) {
      v = vertices;
      s = new boolean[v.size()][v.size()];
    }
    
    private EdgeSet(List<Point> vertices, boolean[][] set) {
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
    
    void markRule2(int i) {
      // TODO Auto-generated method stub
    }
    
    boolean test1stCond(List<Integer> rem) {
      // Each point that does not yet belong to the polygo-
      // nal chain under construction has at least two inci-
      // dent unmarked edges.     
      for(Integer point : rem) {
        
        int count = 0;
        for(int i = 0; i < v.size(); i++) {
          if(i != point && !isMarked(point, i))
            count++;
        }
        
        if(count < 2)
          return false;
      }
      
      return true;
    }
    boolean test2ndCond(int np) {
      // At most one point adjacent to the point last added
      // has only two incident unmarked edges.
      boolean found_one = false;
      
      // Iterate through all points adjacent to np.
      for(int i = 0; i < v.size(); i++) {
        if(i != np && !isMarked(i, np)) {
            
          // Count incident unmarked edges of point i
          int count = 0;
          for(int j = 0; j < v.size(); j++) {
            if(i != j) {
              if(!isMarked(i, j)) {
                count++;
                
                // Small optimization: 3 is enough.
                if(count > 2)
                  break;
              }
            }
          }
          
          // If only two unmarked edges
          if(count <= 2) {
            if(!found_one)
              found_one = true;
            else
              return false;
          }
        }
      }
      
      return true;
    }

    boolean test3rdCond() {
      // TODO Auto-generated method stub
      return false;
    }
    
    private boolean isMarked(int i, int j) {
      assert(i != j); // TODO remove
      return (i < j) ? s[i][j] : s[j][i];
    }
    
    private void markEdge(int i, int j) {
      assert(i != j); // TODO remove
      if(i < j)
        s[i][j] = true;
      else
        s[j][i] = true;
    }
  }

}
