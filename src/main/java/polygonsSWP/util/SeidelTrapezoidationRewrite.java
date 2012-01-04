package polygonsSWP.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import polygonsSWP.geometry.Line;
import polygonsSWP.geometry.LineSegment;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.geometry.Ray;

public class SeidelTrapezoidationRewrite
{ 
  public static List<Polygon> generateTrapezoidation(Polygon polygon) {
   
    /* 
     * 1st step: Initialization. See paper for explanation of data structures.
     */
    
    // Regions. Initially there's only one region, namely the R² plane.
    Region[] T = new Region[polygon.size() * 4];
    int Tcount = 0;
    T[Tcount++] = new Region();
    
    // Point locating data structure. Directed, acyclic graph.
    // Initially contains only one node (SINK) pointing to the
    // initial region.
    SearchTreeNode Z = new SearchTreeNode(0);
    T[0].sink = Z;
    
    // Set of LineSegments to process.
    LineSegment[] S = new LineSegment[polygon.size()];
    
    /* 
     * 2st step: Create random list of line segments from polygon.
     */

    Random r = new Random(System.currentTimeMillis());
    List<Point> points = polygon.getPoints();
    List<LineSegment> tmp = new LinkedList<LineSegment>();
    for(int i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
      // Sort points by y-coordinate or x-coordinate (i.e. lexicographic ordering).
      Point a = points.get(j);
      Point b = points.get(i);
      int cmp = a.compareToByY(b);
      LineSegment l = new LineSegment(
          (cmp == 1) ? a : b,
          (cmp == 1) ? b : a);
      
      // Add line to edge set.
      tmp.add(l);
    }
    // Randomize.
    for(int i = 0; i < S.length; i++)
      S[i] = tmp.remove(r.nextInt(tmp.size()));
    
    /* 
     * 3nd step: Iterate through edge set and process each line.
     */
   
    // Remember used points as we don't want to add them twice.
    List<Point> usedPoints = new LinkedList<Point>();
    for(int si = 0; si < S.length; si++) {
      LineSegment sx = S[si];
           
      // Skip horizontal edges.
      if(sx._a.y == sx._b.y)
        continue;
      
      /*
       * Step 3a: For each end point, horizontally split containing trapezoid.
       */
      
      // Remember top-most and bottom-most region the segment goes through.
      int[] endpoint_regions = new int[2];

      for(int xi = 0; xi < 2; xi++) {
        Point px = (xi == 0) ? sx._a : sx._b;
        if(usedPoints.contains(px))
          continue;
        usedPoints.add(px);
        
        // Locate region containing the point.
        SearchTreeNode cur = Z;
        while(cur.type != SearchTreeNodeType.SINK) {
          switch(cur.type) {
            case XNODE:
              // Region is further split by line segment. Test whether
              // point is left or right of the segment.
              LineSegment vs = S[cur.segmentIdx];
              int oriented = MathUtils.checkOrientation(vs._b, vs._a, px); 
              if(oriented == 1) {
                cur = cur.leftOrAbove;
              } else if(oriented == -1) {
                cur = cur.rightOrBelow;
              } else {
                assert(false); // Intersecting edges!
              }
              break;
            case YNODE:
              // Region is horizontally split by a point. Test whether
              // px lies above or below the horizontal line.
              if(px.y < cur.y) {
                cur = cur.leftOrAbove;
              } else if(px.y > cur.y) {
                cur = cur.rightOrBelow;
              } else {
                // Okay, we found a point which has the same y coordinate
                // as some other point before. With regard to the current
                // points position on its line segment (upper or lower), go
                // down or up.
                if(xi == 0) {
                  // Upper point, so go down.
                  cur = cur.leftOrAbove;
                } else {
                  cur = cur.rightOrBelow;
                }
              }
              break;
            default:
              assert(false); // Can not happen.
          }
        }
        
        // Horizontally split the region, use the original one as the upper.
        int upper_idx = cur.regionIdx; int lower_idx = Tcount++;
        Region upper = T[cur.regionIdx];
        upper.lowerBoundsIdx[0] = lower_idx;
        upper.lowerBoundsIdx[1] = -1;
        
        Region lower = (T[lower_idx] = new Region());
        lower.upperBoundsIdx[0] = upper_idx;
        lower.lowerBoundsIdx[0] = upper.lowerBoundsIdx[0];
        lower.lowerBoundsIdx[1] = upper.lowerBoundsIdx[1];
        lower.left = upper.left;
        lower.right = upper.right;
        
        // Update the search tree. Current node becomes a YNODE, insert two
        // new sinks as its children.
        cur.type = SearchTreeNodeType.YNODE;
        cur.y = px.y;
        cur.leftOrAbove = new SearchTreeNode(upper_idx);
        cur.rightOrBelow = new SearchTreeNode(lower_idx);
        
        // Remember upper region (for upper point) or lower region (for lower point).
        endpoint_regions[xi] = (xi == 0) ? upper_idx : lower_idx;
      }
       
      /*
       * Step 3b: Thread segment sx through T, vertically splitting the regions,
       *          and update the search tree accordingly. Go top-down.
       */
      List<Integer> left_contiguous = new LinkedList<Integer>();
      List<Integer> right_contiguous = new LinkedList<Integer>();
      int cur_region_idx = endpoint_regions[0];
      do {
        // Split region, update Z on-the-go.
        Region oldregion = T[cur_region_idx];
        SearchTreeNode oldsink = oldregion.sink;
        
        oldsink.type = SearchTreeNodeType.XNODE;
        oldsink.segmentIdx = si;
        
        // Create new regions, use oldregion as left one.
        Region leftregion = oldregion;
        Region rightregion = (T[Tcount++] = new Region());
        rightregion.leftSegmentIdx = si;
        rightregion.rightSegmentIdx = leftregion.rightSegmentIdx;
        leftregion.rightSegmentIdx = si;
        
        // TODO upper/lower bounds cases handling, merging, +++
        
      } while(cur_region_idx != endpoint_regions[1]);
      
    }
    
    /*
     * 3rd step: Construct polygons of limited regions. 
     */
    List<Polygon> retval = null;
   
    /*
     * 4th step: Eliminate outer polygons.
     */
    
    for(int i = 0; i < retval.size();) {
      Point com = retval.get(i).centerOfMass();
      if(!polygon.containsPoint(com, true))
        retval.remove(i);
      else
        i++;
    }
    
    return retval;
  }
      
  private static class SearchTreeNode {
    SearchTreeNodeType type;
    int regionIdx;
    double y;
    int segmentIdx;
    SearchTreeNode leftOrAbove;
    SearchTreeNode rightOrBelow;
    
    SearchTreeNode(int region) {
      regionIdx = region;
      type = SearchTreeNodeType.SINK;
    }
  }
  
  private enum SearchTreeNodeType {
    /** Regions split horizontally by a line segment. */
    XNODE,
    /** Regions split vertically by a point. */
    YNODE,
    /** Leaf node / Region */
    SINK
  }
  
  private static class Region {
    SearchTreeNode sink;
    int[] upperBoundsIdx = {-1, -1};
    int[] lowerBoundsIdx = {-1, -1};
    int leftSegmentIdx = -1;
    int rightSegmentIdx = -1;    
  }
}
