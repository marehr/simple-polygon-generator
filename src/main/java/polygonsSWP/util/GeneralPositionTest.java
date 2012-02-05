package polygonsSWP.util;

import java.util.HashSet;
import java.util.List;

import polygonsSWP.geometry.Line;
import polygonsSWP.geometry.LineSegment;
import polygonsSWP.geometry.Point;

public class GeneralPositionTest
{
  private static class IntersectionTuple {
    Point a = null;
    Point b = null;
    
    void addPoint(Point x) {
      if(a == null)
        a = x;
      else if(b == null)
        b = x;
      else
        assert(false);
    }

    @Override
    public int hashCode() {
      return a.hashCode() * 31 + b.hashCode(); 
    }
    
    @Override
    public boolean equals(Object obj) {
      IntersectionTuple other = (IntersectionTuple) obj;
      return (a.equals(other.a) && b.equals(other.b)) || (a.equals(other.b) && b.equals(other.a));
    }
  }
  
  public static boolean isInGeneralPosition(List<Point> pointSet) {
    // Build bounding box.
    double minX = Double.MAX_VALUE;
    double maxX = -Double.MAX_VALUE;
    double minY = Double.MAX_VALUE;
    double maxY = -Double.MAX_VALUE;
    for(Point p : pointSet) {
      if(p.x < minX)
        minX = p.x;
      if(p.x > maxX)
        maxX = p.x;
      if(p.y < minY)
        minY = p.y;
      if(p.y > maxY)
        maxY = p.y;
    }
    
    // Extend the bounding box a little.
    maxX++; minX--; maxY++; minY--;
    
    Point bottomleft = new Point(minX, minY);
    Point bottomright = new Point(maxX, minY);
    Point topleft = new Point(minX, maxY);
    Point topright = new Point(maxX, maxY);
    
    LineSegment[] edges = {
        new LineSegment(bottomleft, topleft),
        new LineSegment(topleft, topright),
        new LineSegment(topright, bottomright),
        new LineSegment(bottomright, bottomleft)
    };
    
    HashSet<IntersectionTuple> isects = new HashSet<IntersectionTuple>();
    
    for(int i = 0; i < pointSet.size() - 1; i++) {
      for(int j = i + 1; j < pointSet.size(); j++) {
        Line ls = new Line(pointSet.get(i), pointSet.get(j));
        IntersectionTuple tuple = new IntersectionTuple();
        
        // Expected: Every line intersects the bounding box at exactly two points.
        Point[] isect;
        for(LineSegment edge : edges) {
          if((isect = ls.intersect(edge)) != null) {
            tuple.addPoint(isect[0]);
          }
        }
        
        if(!isects.add(tuple))
          return false;
      }
    }
    
    return true;
  }
}
