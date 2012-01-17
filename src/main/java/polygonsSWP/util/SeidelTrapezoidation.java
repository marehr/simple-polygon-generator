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

public class SeidelTrapezoidation
{
  public static List<OrderedListPolygon> generateTrapezoidation(Polygon polygon) {
   
    /* 
     * 1st step: Initialization. 
     */
    
    Random r = new Random(System.currentTimeMillis());
    SearchTree S = new SearchTree();
    List<LineSegment> E = new LinkedList<LineSegment>();
    
    // Remember used points as we don't want to add them twice.
    List<Point> usedPoints = new LinkedList<Point>();
    
    // Create random list of line segments from polygon.
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
    while(!tmp.isEmpty())
      E.add(tmp.remove(r.nextInt(tmp.size())));
    
    /* 
     * 2nd step: Iterate through edge set and construct
     * search tree incrementally.
     */
   
    while(!E.isEmpty()) {
      LineSegment l = E.remove(0);
      Point[] ab = new Point[] {
          l._a, // Upper point of l.
          l._b  // Lower point of l.
      };
           
      // Skip horizontal edges.
      if(l._a.y == l._b.y)
        continue;
      
      // For each point, horizontally split containing trapezoid.
      for(Point x : ab) {
        if(usedPoints.contains(x))
          continue;
        usedPoints.add(x);
        
        S.processPoint(x);
      }
       
      // Vertically split all regions intersecting with edge l.
      S.processLineSegment(l);
    }
    
    /*
     * 3rd step: Construct polygons of limited regions. 
     */
    
    final List<OrderedListPolygon> retval = new LinkedList<OrderedListPolygon>();
    S.visitAllRegions(new SearchTreeVisitor() {
      @Override
      public void visit(SearchTreeNode n) {
        if(n.t.left != null & n.t.right != null) {
          OrderedListPolygon trapezoid = new OrderedListPolygon();
          trapezoid.addPoint(n.t.left._b);
          trapezoid.addPoint(n.t.left._a);
          if(!n.t.left._a.equals(n.t.right._a))
            trapezoid.addPoint(n.t.right._a);
          if(!n.t.left._b.equals(n.t.right._b))
            trapezoid.addPoint(n.t.right._b);

          retval.add(trapezoid);
        }
      }
    });
    
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
  
  private static class SearchTree {
    private SearchTreeNode root;
    
    SearchTree() {
      // Initially, the search tree contains only one 
      // unlimited Region which is equal to the R² plane.
      root = new SearchTreeNode(new Region());
    }

    void processPoint(Point x) {
      SearchTreeNode cur = root;
      
      // Search trapezoid containing x.
      while(cur.type != SearchTreeNodeType.SINK) {
        if(cur.type == SearchTreeNodeType.YNODE) {
          // Region is horizontally split by point.
          Point hs = cur.p;
          
          // Is x below or above horizontal line?
          if(x.y > hs.y) {
            // Go above.
            cur = cur.leftOrAbove;
          } else if (x.y < hs.y) {
            // Go below.
            cur = cur.rightOrBelow;
          } else {
            // Okay, we found a point which has the same y coordinate
            // as some other point before. Seemingly, the region
            // is still split horizontally by that point.
            // Do nothing.
            return;
          }
          
        } else if (cur.type == SearchTreeNodeType.XNODE) {
          // Region is vertically split by line segment.
          LineSegment vs = cur.l;

          // Is x left or right of the line segment?
          int oriented = MathUtils.checkOrientation(vs._b, vs._a, x); 
          assert(oriented != 0); // Intersecting edges!
          if(oriented == 1) {
            // Go left.
            cur = cur.leftOrAbove;
          } else {
            // Go right.
            cur = cur.rightOrBelow;
          }

        } else {
          // Cannot happen.
          assert(false);
        }
      }
      
      // Split trapezoid horizontally.
      Region[] newT = cur.t.splitByPoint(x);
      
      // Change cur node into point node and attach new
      // trapezoid as above and below children.
      cur.type = SearchTreeNodeType.YNODE;
      cur.p = x;
      cur.t = null;
      cur.leftOrAbove = new SearchTreeNode(newT[0]);
      cur.rightOrBelow = new SearchTreeNode(newT[1]);
    }
    
    /**
     * Splits all regions intersected by l vertically,
     * merges contiguous regions, and changes the search
     * tree accordingly.
     */
    public void processLineSegment(final LineSegment l) {
      
      // Find all regions intersected by l.
      final List<Region> isctngRgns = new LinkedList<Region>();
      visitAllRegions(new SearchTreeVisitor() {
        @Override
        public void visit(SearchTreeNode n) {
          if(n.t.intersects(l))
            isctngRgns.add(n.t);
        }
      });
      
      // Split horizontally.
      final SearchTreeNode[][] newNodes = new SearchTreeNode[isctngRgns.size()][2];
      for(int i = 0; i < isctngRgns.size(); i++) {
        Region[] newRegions = isctngRgns.get(i).splitByLineSegment(l);
        newNodes[i][0] = new SearchTreeNode(newRegions[0]);
        newNodes[i][1] = new SearchTreeNode(newRegions[1]);
      }
      
      // Merge. This is tricky.
      for(int side = 0; side < 2; side++) {
        for(int i = 0; i < isctngRgns.size() - 1; i++) {
          for(int j = i + 1; j < isctngRgns.size(); j++) {         
            SearchTreeNode ni = newNodes[i][side];
            SearchTreeNode nj = newNodes[j][side];
            
            // NOTE: Our left side is the regions' right side.
            Region merged = ni.t.merge(nj.t, (side + 1) % 2); 
            
            if(merged != null) {             
              SearchTreeNode mergedNode = new SearchTreeNode(merged);
              
              // Now we need to update all node references pointing
              // to any of the two merged regions.
              for(int k = 0; k < isctngRgns.size(); k++) {
                if(newNodes[k][side] == ni || newNodes[k][side] == nj)
                  newNodes[k][side] = mergedNode;
              }
            }
          }
        }
      }
      
      // Replace old region by new SearchTreeNode + 2 children.
      visitAllRegions(new SearchTreeVisitor() {
        private int i = 0;
        @Override
        public void visit(SearchTreeNode n) {
          // NOTE: We expect the same order here as above.
          if(n.t.intersects(l)) {
            n.type = SearchTreeNodeType.XNODE;
            n.t = null;
            n.l = l;
            n.leftOrAbove = newNodes[i][0];
            n.rightOrBelow = newNodes[i][1];
            i++;
          }
        }
      });
    }

    private void visitAllRegions(SearchTreeVisitor v) {
      // Traverse tree.
      
      /*
       *  NOTE:
       *  As some nodes may be reachable via multiple paths,
       *  we remember visited nodes in a simple list here
       *  (matching the references). When we merge regions
       *  together and thus change the nodes' internals (e.g.
       *  turning a leaf node into a LineSegment node), the
       *  reference should hopefully stay the same and the 
       *  node should not be re-visited.
       *  This is extremly fragile and maybe should be
       *  reconsidered.
       */
      
      Stack<SearchTreeNode> s = new Stack<SearchTreeNode>();
      List<SearchTreeNode> visited = new LinkedList<SearchTreeNode>();
      s.push(root);   
      
      while(!s.isEmpty()) {
        SearchTreeNode n = s.pop();
        switch(n.type) {
        case SINK:
          v.visit(n);
          break;
        case XNODE:
        case YNODE:
          if(!visited.contains(n.leftOrAbove)) {
            s.push(n.leftOrAbove);
            visited.add(n.leftOrAbove);
          }
          if(!visited.contains(n.rightOrBelow)) {
            s.push(n.rightOrBelow);
            visited.add(n.rightOrBelow);
          }
          break;
        default:
          assert(false);
        }
      }
    }
  }
  
  private interface SearchTreeVisitor
  {
    void visit(SearchTreeNode n);
  }
  
  private static class SearchTreeNode {
    SearchTreeNodeType type;
    Region t;
    Point p;
    LineSegment l;
    SearchTreeNode leftOrAbove;
    SearchTreeNode rightOrBelow;
    
    SearchTreeNode(Region t) {
      this.t = t;
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
    double upperBound;
    double lowerBound;
    LineSegment left;
    LineSegment right;
    
    Region() {
      upperBound = Double.MAX_VALUE;
      lowerBound = -Double.MAX_VALUE;
    }

    /**
     * Splits the region horizontally at a line defined by
     * the points y-coordinate.
     * @param x the point
     * @return array of two new regions in order [upper, lower].
     */
    Region[] splitByPoint(Point x) {
      // Left intersection, if any
      Point leftIsect = null;
      if(left != null) {
        Ray leftRay = new Ray(x, new Point(x.x - 1, x.y));
        leftIsect = leftRay.intersect(left)[0];
      }
      
      // Right intersection, if any
      Point rightIsect = null;
      if(right != null) {
        Ray rightRay = new Ray(x, new Point(x.x + 1, x.y));
        rightIsect = rightRay.intersect(right)[0];
      }
      
      Region[] retval = {
          new Region(),
          new Region()
      };
      
      // Upper region.
      retval[0].upperBound = upperBound;
      retval[0].lowerBound = x.y;
      if(left != null)
        retval[0].left = new LineSegment(left._a, leftIsect);
      if(right != null)
        retval[0].right = new LineSegment(right._a, rightIsect);
      
      // Lower region.
      retval[1].upperBound = x.y;
      retval[1].lowerBound = lowerBound;
      if(left != null)
        retval[1].left = new LineSegment(leftIsect, left._b);
      if(right != null)
        retval[1].right = new LineSegment(rightIsect, right._b);
      
      return retval;
    }
    
    /**
     * Splits the region vertically along the LineSegment.
     * 
     * @param l the LineSegment
     * @return array of two new regions in order [left, right]
     */
    public Region[] splitByLineSegment(LineSegment l) {
      Region[] retval = new Region[2];
      
      // Contract LineSegment.
      Point[] ab = getVerticalIntersections(l);
      LineSegment newL = new LineSegment(ab[0], ab[1]);
      
      // Left region.
      retval[0] = new Region();
      retval[0].left = left;
      retval[0].right = newL;
      retval[0].upperBound = upperBound;
      retval[0].lowerBound = lowerBound;
      
      // Right region.
      retval[1] = new Region();
      retval[1].left = newL;
      retval[1].right = right;
      retval[1].upperBound = upperBound;
      retval[1].lowerBound = lowerBound;
      
      return retval;
    }
    
    /**
     * @return the intersections of l with horizontal lines at y ==
     * upperBound, y == lowerBounds or null, if none or only
     * one of them is intersected.
     */
    private Point[] getVerticalIntersections(LineSegment l) {
      Point[] retval = new Point[2];
      
      Line topRule = new Line(new Point(0, upperBound), new Point(1, upperBound));
      Point[] isect = topRule.intersect(l);
      if(isect == null)
        return null;
      retval[0] = isect[0];
      
      Line bottomRule = new Line(new Point(0, lowerBound), new Point(1, lowerBound));
      isect = bottomRule.intersect(l);
      if(isect == null)
        return null;
      retval[1] = isect[0];
      
      return retval;
    }
    
    /**
     * Tests whether a LineSegment intersects a region or 
     * is contained in the region.
     */
    public boolean intersects(LineSegment l) {
      // 1st test: l intersects vertically, i.e. intersects
      // both upperBound and lowerBound horizontal lines.
      Point[] vertIsects = getVerticalIntersections(l);
      boolean intersects = (vertIsects != null);
      
      // 2nd test: If this region is left bounded,
      // intersections with upper/lower boundaries must be on the
      // on the right of 'left'.
      if(intersects && left != null) {
        intersects = left._a.x <= vertIsects[0].x && left._b.x <= vertIsects[1].x;
      }
      
      // 3rd test: Same for right boundary.
      if(intersects && right != null) {
        intersects = right._a.x >= vertIsects[0].x && right._b.x >= vertIsects[1].x;
      }
      
      return intersects;
    }
    
    /**
     * Merges two regions (if possible), that just have been
     * created during processing of an edge.
     * 
     * @param newSegmentsSide Used to specify the side of the
     *        edge that has just been added. 0 for left,
     *        1 for right.
     * @return merged region or null, if not mergable.
     */
    public Region merge(Region tj, int newSegmentsSide) {
      /*
       * All regions that are compared here are just divided
       * by a newly added LineSegment. This means that either
       * one of right or left boundaries of the Regions form a 
       * straight LineSegment (part of the newly added one). 
       * 
       * Regions can only be merged, if they share upperBound
       * or lowerBound and both horizontal directions
       * are either not limited/specified _or_ are bounded by 
       * a straight line. As we know the newSegmentsSide, we only
       * have to test one direction.
       */
      
      // 1st option: ti is above tj.
      if(MathUtils.doubleEquals(lowerBound, tj.upperBound)) {
        Region r = new Region();
        r.lowerBound = tj.lowerBound;
        r.upperBound = upperBound;

        if(newSegmentsSide == 0) {
          // Left side is the newly added edge and thus must form
          // a straight line.
          r.left = new LineSegment(left._a, tj.left._b);
          
          // Check whether right side is either unbounded or
          // a straight line.
          if(right == null && tj.right == null) {
            r.right = null;
          } else if(right != null && tj.right != null) {

            LineSegment newR = new LineSegment(right._a, tj.right._b);
            if(newR.containsPoint(right._b))
              r.right = newR;
            else
              // No match.
              r = null;
            
          } else {
            // No match.
            r = null;
          }
        } else {
          // Right side is the newly added edge.
          r.right = new LineSegment(right._a, tj.right._b);
          
          // Check whether left side is either unbounded or
          // a straight line.
          if(left == null && tj.left == null) {
            r.left = null;
          } else if(left != null && tj.left != null) {

            LineSegment newL = new LineSegment(left._a, tj.left._b);
            if(newL.containsPoint(left._b))
              r.left = newL;
            else
              // No match.
              r = null;
            
          } else {
            // No match.
            r = null;
          }
        }
        
        return r;
      }
      
      // 2nd option: ti is below tj. Analog.
      if(MathUtils.doubleEquals(upperBound, tj.lowerBound)) {
        Region r = new Region();
        r.lowerBound = lowerBound;
        r.upperBound = tj.upperBound;
        
        if(newSegmentsSide == 0) {
          r.left = new LineSegment(tj.left._a, left._b);
          
          if(right == null && tj.right == null) {
            r.right = null;
          } else if (right != null && tj.right != null) {

            LineSegment newR = new LineSegment(tj.right._a, right._b);
            if(newR.containsPoint(tj.right._b))
              r.right = newR;
            else
              r = null;
            
          } else {
            r = null;
          }
          
        } else {
          r.right = new LineSegment(tj.right._a, right._b);
          
          if(left == null && tj.left == null) {
            r.left = null;
          } else if (left != null && tj.left != null) {

            LineSegment newL = new LineSegment(tj.left._a, left._b);
            if(newL.containsPoint(tj.left._b))
              r.left = newL;
            else
              r = null;
            
          } else {
            r = null;
          }
        }
        
        return r;
      }
      
      return null;
    }
    
    @Override
    public String toString() {
      return "Region [upper: " + upperBound + ", lower: " 
         + lowerBound + ", left: " + left + ", right: " + right + "]";
    }
    
  }
}
