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
  public static List<Polygon> generateTrapezoidation(Polygon polygon) {
    // Remark: We assume nondegenerate polygons for now.
    // TODO: Make use of lexicographic ordering.
    
    /* 
     * 1st step: Initialization. 
     */
    
    Random r = new Random(System.currentTimeMillis());
    SearchTree S = new SearchTree();
    List<LineSegment> E = new LinkedList<LineSegment>();
    List<Point> usedPoints = new LinkedList<Point>();
    
    // Create random list of line segments from polygon.
    List<Point> points = polygon.getPoints();
    List<LineSegment> tmp = new LinkedList<LineSegment>();
    for(int i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
      // Sort points by y-coordinate.
      Point a = points.get(j);
      Point b = points.get(i);
      int cmp = a.compareToByY(b);
      assert(cmp != 0); // Nondegeneracy assumption.
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
    
    final List<Polygon> retval = new LinkedList<Polygon>();
    S.visitAllRegions(new SearchTreeVisitor() {
      @Override
      public void visit(SearchTreeNode n) {
        if(n.t.left != null & n.t.right != null) {
          System.out.println(n.t);
          OrderedListPolygon trapezoid = new OrderedListPolygon();
          trapezoid.addPoint(n.t.left._b);
          trapezoid.addPoint(n.t.left._a);
          if(n.t.left._a.equals(n.t.right._a))
            trapezoid.addPoint(n.t.right._a);
          if(n.t.left._b.equals(n.t.right._b))
            trapezoid.addPoint(n.t.right._b);
          
          retval.add(trapezoid);
        }
      }
    });
    
    /*
     * 4th step: Eliminate outer polygons.
     */
    for(int i = 0; i < retval.size();) {
      Polygon p = retval.get(i);
      boolean outer = false;
      for(int j = 0, k = p.size() - 1; j < p.size(); k = j++) {
        Point x = new LineSegment(p.getPoints().get(j), p.getPoints().get(i)).getCenter();
        if(!polygon.containsPoint(x, true)) {
          outer = true;
          break;
        }
      }
      
      if(outer)
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
      while(cur.type != 0) {
        if(cur.type == 1) {
          // Region is horizontally split by point.
          Point hs = cur.p;
          assert(hs.y != x.y); // Nondegeneracy assumption.
          
          // Is x below or above horizontal line?
          if(x.y > hs.y) {
            // Go above.
            cur = cur.leftOrAbove;
          } else {
            // Go below.
            cur = cur.rightOrBelow;
          }
          
        } else { // cur.type == 2
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

        }
      }
      
      // Split trapezoid horizontally.
      Region[] newT = cur.t.splitByPoint(x);
      
      // Change cur node into point node and attach new
      // trapezoid as above and below children.
      cur.type = 1;
      cur.p = x;
      cur.t = null;
      cur.leftOrAbove = new SearchTreeNode(newT[0]);
      cur.rightOrBelow = new SearchTreeNode(newT[1]);
    }
    
    /**
     * Splits all regions intersected by l vertically,
     * merges continguous regions, and changes the search
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
            Region merged = ni.t.merge(nj.t); 
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
            n.type = 2;
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
        case 0:
          v.visit(n);
          break;
        case 1:
        case 2:
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
    int type; // 0: leaf (trapezoid), 1: point, 2: line segment
    Region t;
    Point p;
    LineSegment l;
    SearchTreeNode leftOrAbove;
    SearchTreeNode rightOrBelow;
    
    SearchTreeNode(Region t) {
      this.t = t;
      type = 0;
    }
  }
  
  private static class Region {
    long upperBound;
    long lowerBound;
    LineSegment left;
    LineSegment right;
    
    Region() {
      upperBound = Long.MAX_VALUE;
      lowerBound = Long.MIN_VALUE;
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
      
      // Left region.
      retval[0] = new Region();
      retval[0].left = left;
      retval[0].right = l;
      retval[0].upperBound = upperBound;
      retval[0].lowerBound = lowerBound;
      
      // Left region.
      retval[1] = new Region();
      retval[1].left = l;
      retval[1].right = right;
      retval[1].upperBound = upperBound;
      retval[1].lowerBound = lowerBound;
      
      return retval;
    }
    
    /**
     * Tests whether a LineSegment intersects a region or 
     * is contained in the region.
     */
    public boolean intersects(LineSegment l) {
      // 1st test: l intersects vertically, i.e. intersects
      // both upperBound and lowerBound horizontal lines.
      Line topRule = new Line(new Point(0, upperBound), new Point(1, upperBound));
      Line bottomRule = new Line(new Point(0, lowerBound), new Point(1, lowerBound));
      boolean intersects = (topRule.intersect(l) != null) &&
          (bottomRule.intersect(l) != null);
      
      // 2nd test: If this region is left bounded,
      // l must be on the right side of 'left'.
      if(intersects && left != null) {
        intersects = left._a.x <= l._a.x && left._b.x <= l._b.x;
      }
      
      // 3rdd test: If this region is right bounded,
      // l must be on the left side of 'right'.
      if(intersects && right != null) {
        intersects = right._a.x >= l._a.x && right._b.x >= l._b.x;
      }
      
      return intersects;
    }
    
    /**
     * Merges two regions (if possible).
     * @return merged region or null, if not mergable.
     */
    public Region merge(Region tj) {
      /*
       * Assumptions:
       * 
       * All regions that are compared here are just divided
       * by a newly added LineSegment. This means that either
       * both of right or left of the Regions form a straight
       * LineSegment (part of the newly added one).
       * 
       * Regions can only be merged, if they share upperBound
       * or lowerBound and either one horizontal direction
       * is not limited/specified.
       * TODO Correct?
       */
      
      // 1st option: ti is above tj.
      if(lowerBound == tj.upperBound) {
        Region r = new Region();
        r.lowerBound = tj.lowerBound;
        r.upperBound = upperBound;
        
        // Check whether either side is not bounded.
        if(left == null && tj.left == null) {
          r.left = null;
          r.right = new LineSegment(right._a, tj.right._b);
        } else if(right == null && tj.right == null) {
          r.right = null;
          r.left = new LineSegment(left._a, tj.left._b);
        } else 
          return null;
        
        return r;
      }
      
      // 2nd option: ti is below tj.
      if(upperBound == tj.lowerBound) {
        Region r = new Region();
        r.lowerBound = lowerBound;
        r.upperBound = tj.upperBound;
        
        // Check whether either side is not bounded.
        if(left == null && tj.left == null) {
          r.left = null;
          r.right = new LineSegment(tj.right._b, right._a);
        } else if(right == null && tj.right == null) {
          r.right = null;
          r.left = new LineSegment(tj.left._b, left._a);
        } else 
          return null;
        
        return r;
      }
      
      return null;
    }
    
    @Override
    public String toString() {
      return "Region [upper: " + upperBound + ", lower: " 
         + lowerBound + ", left: " + left + ", right: " + right;
    }
    
  }
}
