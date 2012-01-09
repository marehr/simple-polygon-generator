package polygonsSWP.util;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import polygonsSWP.geometry.Line;
import polygonsSWP.geometry.LineSegment;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;

public class SeidelTrapezoidationRewrite
{ 
  
  // TODO remove
  private static DecimalFormat df = new DecimalFormat("###.##");
  
  private RegionList T;
  private SearchTreeNode Z;
  private LineSegment[] S;
  
  public List<Polygon> generateTrapezoidation(Polygon polygon) {
   
    /* 
     * 1st step: Initialization. See paper for explanation of data structures.
     */
    
    // Regions. Initially there's only one region, namely the R² plane.
    T = new RegionList(polygon.size());
    int initidx = T.createRegion();
        
    // Point locating data structure. Directed, acyclic graph.
    // Initially contains only one node (SINK) pointing to the
    // initial region.
    Z = new SearchTreeNode(initidx);
    T.getRegion(initidx).sinks.add(Z);
    
    // Set of LineSegments to process.
    S = new LineSegment[polygon.size()];
    
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
    
    // TODO remove
    /*Point[] parr = {new Point(481.53, 183.167), new Point(45.713, 162.635), new Point(15.249, 382.435), new Point(310.737, 197.333), new Point(402.097, 363.839)};
    S[0] = new LineSegment(parr[0], parr[1]);
    S[1] = new LineSegment(parr[2], parr[1]);
    S[2] = new LineSegment(parr[2], parr[3]);
    S[3] = new LineSegment(parr[4], parr[3]);
    S[4] = new LineSegment(parr[4], parr[0]);*/
    for(int si = 0; si < S.length; si++) {
      System.out.println("si = " + si + ": " + S[si]);
    }
    
    /* 
     * 3nd step: Iterate through edge set and process each line.
     */
   
    for(int si = 0; si < S.length; si++) {
      LineSegment sx = S[si];
           
      // Skip horizontal edges.
      if(MathUtils.doubleEquals(sx._a.y, sx._b.y))
        continue;
      
      // For each end point, find containing trapezoid and split if necessary.      
      int topMostRegionIdx = findAndSplitRegion(sx._a, sx._b, true);
      int bottomMostRegionIdx = findAndSplitRegion(sx._b, sx._a, false);
       
      // Thread segment si through T.      
      threadSegmentThroughTrapezoidalMap(si, topMostRegionIdx, bottomMostRegionIdx);     
      
      // TODO remove
      T.debug();
      System.out.println(debugSearchTree(Z));
      System.out.println();
    }
    
    /*
     * 3rd step: Construct polygons of limited regions. 
     */
    List<Polygon> retval = new LinkedList<Polygon>();
    List<Integer> markedRegions = new LinkedList<Integer>();
    double high = Double.MAX_VALUE;
    double low = -Double.MAX_VALUE;
    extractValidPolygons(Z, retval, markedRegions, high, low);
   
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
  
  // TODO remove
  private String debugSearchTree(SearchTreeNode s) {
    String retval = null;
    switch(s.type) {
    case XNODE:
      retval = "(XNODE[" + s.segmentIdx + "] " + debugSearchTree(s.leftOrAbove) + " " + debugSearchTree(s.rightOrBelow) + ")";
      break;
    case YNODE:
      retval = "(YNODE[" + df.format(s.y) + "] " + debugSearchTree(s.leftOrAbove) + " " + debugSearchTree(s.rightOrBelow) + ")";
      break;
    case SINK:
      retval = "SINK[" + s.regionIdx + "]";
      break;
    }
    return retval;
  }
  
  private void extractValidPolygons(SearchTreeNode node, List<Polygon> polygons, List<Integer> markedRegions, double high, double low) {
    switch(node.type) {
    case XNODE:
      extractValidPolygons(node.leftOrAbove, polygons, markedRegions, high, low);
      extractValidPolygons(node.rightOrBelow, polygons, markedRegions, high, low);
      break;
    case YNODE:
      extractValidPolygons(node.leftOrAbove, polygons, markedRegions, high, node.y);
      extractValidPolygons(node.rightOrBelow, polygons, markedRegions, node.y, low);
      break;
    case SINK:
      Region region = T.getRegion(node.regionIdx);
      if(high < Double.MAX_VALUE 
          && low > -Double.MAX_VALUE 
          && region.leftSegmentIdx != -1 
          && region.rightSegmentIdx != -1 
          && !markedRegions.contains(node.regionIdx)) {
        markedRegions.add(node.regionIdx);
        
        OrderedListPolygon olp = new OrderedListPolygon();
        Line upperHorizontal = new Line(new Point(0, high), new Point(1, high));
        Point upperLeft = upperHorizontal.intersect(S[region.leftSegmentIdx])[0];
        Point upperRight = upperHorizontal.intersect(S[region.rightSegmentIdx])[0];
        
        Line lowerHorizontal = new Line(new Point(0, low), new Point(1, low));
        Point lowerLeft = lowerHorizontal.intersect(S[region.leftSegmentIdx])[0];
        Point lowerRight = lowerHorizontal.intersect(S[region.rightSegmentIdx])[0];
        
        olp.addPoint(upperLeft);
        if(!upperRight.equals(upperLeft))
          olp.addPoint(upperRight);
        
        olp.addPoint(lowerRight);
        if(!lowerLeft.equals(lowerRight))
          olp.addPoint(lowerLeft);
        
        polygons.add(olp);
      }
      break;
    }
  }
  
  /**
   * Locates the containing region r for a point p. If point lies
   * in the interior of r, r is split horizontally at p.y. 
   * 
   * @param p the point
   * @param po the other point of the segment
   * @param upper_point true if p is the upper point of a line segment.
   * @return index of a region, lower region the point if upper_point is set,
   *         upper region otherwise.
   */
  private int findAndSplitRegion(Point p, Point po, boolean upper_point) {
    
    boolean doSplit = true;
    
    // Locate region containing the point.
    SearchTreeNode cur = Z;
    while(cur.type != SearchTreeNodeType.SINK) {
      switch(cur.type) {
      
        case XNODE:
          // Region is further split by line segment. Test whether
          // point is left or right of the segment.
          LineSegment vs = S[cur.segmentIdx];
          int oriented = MathUtils.checkOrientation(vs._b, vs._a, p); 
          if(oriented == 1) {
            cur = cur.leftOrAbove;
          } else if(oriented == -1) {
            cur = cur.rightOrBelow;
          } else {
            // Check for common end points.
            if(p.equals(vs._a)) {
              if(upper_point) {
                oriented = MathUtils.checkOrientation(vs._b, vs._a, po);
                if(oriented == 1) {
                  // The other point of the segment is on the left of the segment.
                  cur = cur.leftOrAbove;
                } else if(oriented == -1) {
                  cur = cur.rightOrBelow;
                } else
                  assert(false); // coincident edges.
              } else {
                // Should not happen, as the segment that splits the region does not contain the lower point.
                assert(false);
              }
            } else if(p.equals(vs._b)) {
              if(!upper_point) {
                oriented = MathUtils.checkOrientation(vs._b, vs._a, po);
                if(oriented == 1) {
                  // The other point of the segment is on the left of the segment.
                  cur = cur.leftOrAbove;
                } else if(oriented == -1) {
                  cur = cur.rightOrBelow;
                } else
                  assert(false); // coincident edges.
              } else {
                // Should not happen, as the segment that splits the region does not contain the upper point.
                assert(false);
              }
            } else
              assert(false); // Intersecting edges!
          }
          break;
          
        case YNODE:
          // Region is horizontally split by a point. Test whether
          // p lies above or below the horizontal line.
          if(p.y < cur.y - MathUtils.EPSILON) {
            cur = cur.rightOrBelow;
          } else if(p.y > cur.y + MathUtils.EPSILON) {
            cur = cur.leftOrAbove;
          } else {
            // Okay, we found a point which has the same y coordinate
            // as some other point before. This can either be a common
            // end point or a point which has the same y-coordinate as 
            // as previously added point. Either way, we don't 
            // have to split the containing region anymore. Still, we
            // need to continue looking for the first (last) region the
            // line segment cuts through.
            doSplit = false;
            if(upper_point)
              cur = cur.rightOrBelow;
            else
              cur = cur.leftOrAbove;
          }
          break;
          
        default:
          assert(false); // Can not happen.
      }
    }
    
    if(doSplit) {
      // Horizontally split the region, use the original one as the upper.
      int upper_idx = cur.regionIdx; 
      int lower_idx = T.createRegion();
      
      Region upper_region = T.getRegion(upper_idx);
      Region lower_region = T.getRegion(lower_idx);
      
      lower_region.upperBoundsIdx[0] = upper_idx;
      lower_region.lowerBoundsIdx[0] = upper_region.lowerBoundsIdx[0];
      lower_region.lowerBoundsIdx[1] = upper_region.lowerBoundsIdx[1];
      lower_region.leftSegmentIdx = upper_region.leftSegmentIdx;
      lower_region.rightSegmentIdx = upper_region.rightSegmentIdx;
      
      upper_region.lowerBoundsIdx[0] = lower_idx;
      upper_region.lowerBoundsIdx[1] = -1;
      
      // Fix pointers from below to us.
      if(lower_region.lowerBoundsIdx[0] != -1) {
        Region lowerLowerLeft = T.getRegion(lower_region.lowerBoundsIdx[0]);
        if(lowerLowerLeft.upperRegions() == 1)
          lowerLowerLeft.upperBoundsIdx[0] = lower_idx;
        else if(lowerLowerLeft.upperRegions() == 2)
          lowerLowerLeft.upperBoundsIdx[1] = lower_idx;
        else
          assert(false);
        
        if(lower_region.lowerBoundsIdx[1] != -1) {
          T.getRegion(lower_region.lowerBoundsIdx[1]).upperBoundsIdx[0] = lower_idx;
        }
      }

      // Update the search tree. Current node becomes a YNODE, insert two
      // new sinks as its children.
      cur.type = SearchTreeNodeType.YNODE;
      cur.y = p.y;
      cur.leftOrAbove = new SearchTreeNode(upper_idx);
      upper_region.sinks.clear();
      upper_region.sinks.add(cur.leftOrAbove);
      cur.rightOrBelow = new SearchTreeNode(lower_idx);
      lower_region.sinks.add(cur.rightOrBelow);
      
      if(upper_point)
        return lower_idx;
      else
        return upper_idx;
      
    } else {
      
      // As we are not supposed to split the area, we did the upper/lower point
      // check above and can safely return the current region.
      return cur.regionIdx;
      
    }    
  }
  
  private void threadSegmentThroughTrapezoidalMap(int segmentIdx, int topMostRegionIdx, int bottomMostRegionIdx) {
    LineSegment segment = S[segmentIdx];
      
    /*
     * TODO comment
     */
    
    int[] round = {topMostRegionIdx, -1, -1};
    boolean firstRound = true;
    boolean lastRound = false;
    do {
      int curRegionIdx = round[0];
      lastRound = (curRegionIdx == bottomMostRegionIdx); 
      
      /*
       *  Split region. Use original region as left region.
       */
      
      int leftRegionIdx = curRegionIdx;
      int rightRegionIdx = T.createRegion();
      
      Region leftRegion = T.getRegion(leftRegionIdx);
      Region rightRegion = T.getRegion(rightRegionIdx);
           
      rightRegion.leftSegmentIdx = segmentIdx;
      rightRegion.rightSegmentIdx = leftRegion.rightSegmentIdx;
      leftRegion.rightSegmentIdx = segmentIdx;
      
      // Update SearchTree.
      List<SearchTreeNode> oldsinks = new LinkedList<SearchTreeNode>(leftRegion.sinks);
      SearchTreeNode newleftsink = new SearchTreeNode(leftRegionIdx);
      SearchTreeNode newrightsink = new SearchTreeNode(rightRegionIdx);
      leftRegion.sinks.clear();
      leftRegion.sinks.add(newleftsink);
      rightRegion.sinks.add(newrightsink);
      for(SearchTreeNode oldsink : oldsinks) {
        oldsink.type = SearchTreeNodeType.XNODE;
        oldsink.segmentIdx = segmentIdx;
        oldsink.leftOrAbove = newleftsink;
        oldsink.rightOrBelow = newrightsink;
      }
      
      /*
       *  Connect upper regions.
       */
      
      if(firstRound) {
        connectUpperBoundsFirstRound(leftRegionIdx, rightRegionIdx, segment);
        firstRound = false;
      } else {
        connectUpperBounds(leftRegionIdx, rightRegionIdx, round);
        
        // Try to merge left side.
        int tmp = merge(leftRegionIdx);
        if(tmp != -1) {
          // Update SearchTree.
          for(SearchTreeNode sink : leftRegion.sinks) {
            sink.regionIdx = tmp;
            T.getRegion(tmp).sinks.add(sink);
          }
          
          leftRegionIdx = tmp;
        }
        
        // Try to merge right side.
        tmp = merge(rightRegionIdx);
        if(tmp != -1) {
          // Update SearchTree.
          for(SearchTreeNode sink : rightRegion.sinks) {
            sink.regionIdx = tmp;
            T.getRegion(tmp).sinks.add(sink);
          }
          
          rightRegionIdx = tmp;
        }
      }
               
      /*
       *  Find next region and connect lower regions.
       */
      
      round = findNextRegionAndConnectLowerBounds(leftRegionIdx, rightRegionIdx, segment);
            
    } while(!lastRound);
  }
  
  private void connectUpperBoundsFirstRound(int leftRegionIdx, int rightRegionIdx, LineSegment segment) {
    Region leftRegion = T.getRegion(leftRegionIdx);
    Region rightRegion = T.getRegion(rightRegionIdx);
    
    if(leftRegion.upperRegions() == 1) {
      
      // If there was only one region above, it's pretty easy.
      rightRegion.upperBoundsIdx[0] = leftRegion.upperBoundsIdx[0];
      T.getRegion(leftRegion.upperBoundsIdx[0]).lowerBoundsIdx[1] = rightRegionIdx;

    } else if (leftRegion.upperRegions() == 2) {
      
      int upperLeftIdx = leftRegion.upperBoundsIdx[0];
      int upperRightIdx = leftRegion.upperBoundsIdx[1];
      Region upperLeft = T.getRegion(leftRegion.upperBoundsIdx[0]);
      Region upperRight = T.getRegion(upperRightIdx);
      
      // Find point lying on horizontal line.
      LineSegment upperLeftRightSegment = S[upperLeft.rightSegmentIdx];
      Point hp = upperLeftRightSegment._b;
      
      // Test whether horizontal point is left or right of our segment.
      int oriented = MathUtils.checkOrientation(segment._b, segment._a, hp);
      if(oriented == -1) {
        // 1st case: Point is right of segment.
        // --> Right region has two upper neighbors.
        
        rightRegion.upperBoundsIdx[0] = upperLeftIdx;
        rightRegion.upperBoundsIdx[1] = upperRightIdx;
        leftRegion.upperBoundsIdx[1] = -1;
        
        upperLeft.lowerBoundsIdx[0] = leftRegionIdx;
        upperLeft.lowerBoundsIdx[1] = rightRegionIdx;
        upperRight.lowerBoundsIdx[0] = rightRegionIdx;
        
      } else if(oriented == 1) {
        // 2nd case: Point is left of segment.
        // --> Left region has two upper neighbors.
        
        rightRegion.upperBoundsIdx[0] = upperRightIdx;
        
        if(upperLeft.lowerRegions() == 1)
          upperLeft.lowerBoundsIdx[0] = leftRegionIdx;
        else
          upperLeft.lowerBoundsIdx[1] = leftRegionIdx;
        upperRight.lowerBoundsIdx[0] = leftRegionIdx;
        upperRight.lowerBoundsIdx[1] = rightRegionIdx;
        
      } else {
        // 3rd case: Segment's upper point is equal to upper point.
        // --> Common end point --> Left region has left upper region as neighbor
        // and right region has right upper region as neighbor.
        
        rightRegion.upperBoundsIdx[0] = upperRightIdx;
        leftRegion.upperBoundsIdx[1] = -1;
        
        if(upperLeft.lowerRegions() == 1)
          upperLeft.lowerBoundsIdx[0] = leftRegionIdx;
        else
          upperLeft.lowerBoundsIdx[1] = leftRegionIdx;
        upperRight.lowerBoundsIdx[0] = leftRegionIdx;
      }
    } else {
      // Defensive programming. We must have a upper neighbor.
      assert(false);
    }
  }
  
  private void connectUpperBounds(int leftRegionIdx, int rightRegionIdx, int[] round) {
    Region leftRegion = T.getRegion(leftRegionIdx);
    Region rightRegion = T.getRegion(rightRegionIdx);
    
    if(round[1] != -1) {
      // Last round we had three lower neighbors. See findNextRegionAndConnectLowerBounds.
      rightRegion.upperBoundsIdx[0] = leftRegion.upperBoundsIdx[0];
      rightRegion.upperBoundsIdx[1] = leftRegion.upperBoundsIdx[1];
      leftRegion.upperBoundsIdx[0] = round[1];
      leftRegion.upperBoundsIdx[1] = -1;
      T.getRegion(rightRegion.upperBoundsIdx[0]).lowerBoundsIdx[0] = rightRegionIdx;
    } else if(round[2] != -1) {
      // Same here.
      rightRegion.upperBoundsIdx[0] = round[2];
      T.getRegion(round[2]).lowerBoundsIdx[0] = rightRegionIdx;
    } else {
      
      // Default case: Above us there are exactly two regions (those that we're split
      // in the last iteration.
      
      if(leftRegion.upperRegions() == 2) {
        rightRegion.upperBoundsIdx[0] = leftRegion.upperBoundsIdx[1];
        leftRegion.upperBoundsIdx[1] = -1; 
        T.getRegion(leftRegion.upperBoundsIdx[0]).lowerBoundsIdx[0] = leftRegionIdx;
        T.getRegion(rightRegion.upperBoundsIdx[0]).lowerBoundsIdx[0] = rightRegionIdx;
      } else if(leftRegion.upperRegions() == 1) {
        System.out.println("LEFT REGION HAS ONLY ONE UPPER NEIGHBOR OH OH");
        assert(false);
      } else {
        assert(false); // Defensive programming.
      }
      
    }
  }
  
  private int merge(int regionIdx) {
    Region region = T.getRegion(regionIdx);
    if(region.upperRegions() > 1)
      return -1;
    
    assert(region.upperRegions() == 1);
    
    int upperIdx = region.upperBoundsIdx[0];
    Region upper = T.getRegion(upperIdx);
    
    if(upper.leftSegmentIdx == region.leftSegmentIdx && upper.rightSegmentIdx == region.rightSegmentIdx) {
      upper.lowerBoundsIdx[0] = region.lowerBoundsIdx[0];
      upper.lowerBoundsIdx[1] = region.lowerBoundsIdx[1];
      return upperIdx;
    }
    
    return -1;
  }
  
  /**
   * Find the next region the segment goes through and connect lower regions with current regions.
   * 
   * @param leftRegionIdx index of current left region
   * @param rightRegionIdx index of current right region
   * @param segment the segment
   * @return array of size 3, where array[0] is the index of the next region, array[1] is the additional
   *         region for the future right region or -1 if there is none, array[2] contains the index of 
   *         the right additional region or -1.
   */
  private int[] findNextRegionAndConnectLowerBounds(int leftRegionIdx, int rightRegionIdx, LineSegment segment) {
    Region leftRegion = T.getRegion(leftRegionIdx);
    Region rightRegion = T.getRegion(rightRegionIdx);
    int nextRegionIdx = -1;
    int addLeft = -1;
    int addRight = -1;
    
    if(leftRegion.lowerRegions() == 1) {
      
      rightRegion.lowerBoundsIdx[0] = leftRegion.lowerBoundsIdx[0];
      T.getRegion(rightRegion.lowerBoundsIdx[0]).upperBoundsIdx[1] = rightRegionIdx;
      nextRegionIdx = leftRegion.lowerBoundsIdx[0];
      
    } else if (leftRegion.lowerRegions() == 2) {
      int lowerLeftIdx = leftRegion.lowerBoundsIdx[0];
      int lowerRightIdx = leftRegion.lowerBoundsIdx[1];
      Region lowerLeft = T.getRegion(lowerLeftIdx);
      Region lowerRight = T.getRegion(lowerRightIdx);
      
      // Find point lying on horizontal line.
      LineSegment lowerLeftRightSegment = S[lowerLeft.rightSegmentIdx];
      Point hp = lowerLeftRightSegment._a;
      
      // Test whether horizontal point lies to the left or to the right of segment.
      int oriented = MathUtils.checkOrientation(segment._b, segment._a, hp);
      if(oriented == -1) {
        // Point is right of segment. --> Right region has two lower neighbors.
        nextRegionIdx = lowerLeftIdx;
        
        rightRegion.lowerBoundsIdx[0] = lowerLeftIdx;
        rightRegion.lowerBoundsIdx[1] = lowerRightIdx;
        leftRegion.lowerBoundsIdx[1] = -1;
        
        if(lowerLeft.upperRegions() == 2) {
          lowerLeft.upperBoundsIdx[1] = leftRegionIdx; 
          addRight = rightRegionIdx;
        } else {
          lowerRight.upperBoundsIdx[0] = rightRegionIdx;
          lowerLeft.upperBoundsIdx[0] = leftRegionIdx;
          lowerLeft.upperBoundsIdx[1] = rightRegionIdx; 
        }
        
        // Return parents
      } else if(oriented == 1) {
        // Point is left of segment. --> Left region has two lower neighbors.
        nextRegionIdx = lowerRightIdx;
        
        rightRegion.lowerBoundsIdx[0] = lowerRightIdx;
        
        if(lowerRight.upperRegions() == 2) {
          lowerRight.upperBoundsIdx[0] = rightRegionIdx;
          addLeft = leftRegionIdx;
        } else {
          lowerRight.upperBoundsIdx[0] = leftRegionIdx;
          lowerRight.upperBoundsIdx[1] = rightRegionIdx;
        }
        
      } else {
        System.out.println("LAST ROUND");
        // Point is on the segment. --> Last round.
        leftRegion.lowerBoundsIdx[1] = -1;
        rightRegion.lowerBoundsIdx[0] = lowerRightIdx;
        lowerRight.upperBoundsIdx[0] = rightRegionIdx;        
      }
      
    } else {
      // We should have exited the loop by now.
      assert(false);
    }
    
    return new int[] {nextRegionIdx, addLeft, addRight};
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
    List<SearchTreeNode> sinks;
    int[] upperBoundsIdx = {-1, -1};
    int[] lowerBoundsIdx = {-1, -1};
    int leftSegmentIdx = -1;
    int rightSegmentIdx = -1;
    
    {
      sinks = new LinkedList<SearchTreeNode>();
    }
    
    int upperRegions() {
      int res = (upperBoundsIdx[0] == -1) ? 0 : 1;
      res += (upperBoundsIdx[1] == -1) ? 0 : 1;
      return res;
    }
    
    int lowerRegions() {
      int res = (lowerBoundsIdx[0] == -1) ? 0 : 1;
      res += (lowerBoundsIdx[1] == -1) ? 0 : 1;
      return res;
    }
    
    // TODO remove
    @Override
    public String toString() {
      return "upperBounds = {" + upperBoundsIdx[0] + ", " + upperBoundsIdx[1] + "}, lowerBounds = {" + lowerBoundsIdx[0] + ", " + lowerBoundsIdx[1] + "}, leftSegmentIdx = " + leftSegmentIdx + ", rightSegmentIdx = " + rightSegmentIdx;
    }
  }
  
  private static class RegionList {
    private Region[] regions;
    private int rcount;
    RegionList(int nvertices) {
      // TODO Implement RegionList as a RegionPool.
      // Maybe we could reuse disloged Regions here and only allocate the maximum number
      // of resulting regions? Maybe not.
      regions = new Region[nvertices * 6];
      rcount = 0;
    }
    
    Region getRegion(int idx) {
      return regions[idx];
    }
    
    int createRegion() {
      int idx = rcount++;
      regions[idx] = new Region();
      return idx;
    }
    
    // TODO remove
    public void debug() {
      System.out.println("RegionList (" + rcount + " regions):");
      for (int i = 0; i < rcount; i++) {
        System.out.println("Region " + i + ": " + regions[i]);
      }
    }
  }
}
