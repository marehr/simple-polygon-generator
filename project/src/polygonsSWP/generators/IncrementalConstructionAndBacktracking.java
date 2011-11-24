package polygonsSWP.generators;

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
    Random r = new Random(System.currentTimeMillis());
    List<Point> points = GeneratorUtils.createOrUsePoints(params);
    
    OrderedListPolygon polygon = new OrderedListPolygon();
    List<Point> pp = polygon.getPoints();
    
    // Unusable edges
    EdgeSet ue = new EdgeSet(points);
    
    // Choose initial point
    polygon.addPoint(points.remove(r.nextInt(points.size())));
    
    // Add remaining points randomly, backtrack when necessary.
    while(!points.isEmpty()) {
      
      Point np = points.remove(r.nextInt(points.size()));
      Point lp = pp.get(pp.size() - 1);
      LineSegment ne = new LineSegment(lp, np);
           
      /*
      In order to reduce backtracking, we keep an in-
      ventory of those edges which still are usable for com-
      pleting the polygon. (Edges which are no longer us-
      able get marked.) Initially, all edges of the complete
      graph on S are usable. When adding point , and thus
      using some edge , all the edges that intersect are
      marked because they are no longer usable for complet-
      ing the polygon. Furthermore, if a point is adjacent
      (Footnote: connected via unmarked edge)
      to two other points that both have only two incident
      unmarked edges, we mark all the other edges incident
      upon that point. Clearly, backtracking is necessary if
      any of the following conditions is violated:
      
      1. Each point that does not yet belong to the polygo-
         nal chain under construction has at least two inci-
         dent unmarked edges. (Otherwise, it is impossible
         to add this point and still complete the polygon.)
      2. At most one point adjacent to the point last added
         has only two incident unmarked edges.
      3. Points that lie on the boundary of CH(S ) appear
         in the polygonal chain in the same relative order
         as on the hull.
      
      These conditions are checked in the same order as stated.      
      */
    }
    
    
    return polygon;
  }
  
  private static class EdgeSet {
    EdgeSet(List<Point> vertices) {
      
    }
  }

}
