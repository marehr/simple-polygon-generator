package polygonsSWP.generators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import polygonsSWP.data.PolygonHistory;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.util.GeneratorUtils;

public class TwoOptMoves implements PolygonGenerator 
{

  @Override
  public String[] getAcceptedParameters() {
    return new String[] {"n", "size", "points"};
  }

  @Override
  public Polygon generate(Map<String, Object> params, PolygonHistory steps) {
    
    // Step 1: Generate n points in the plane or use the given set of points.
    OrderedListPolygon p = new OrderedListPolygon(GeneratorUtils.createOrUsePoints(params));
    
    // Step 2: Generate random permutation in case given set was ordered somehow.
    p.permute();
    
    Integer[] intersection = null;
    while((intersection = p.findRandomIntersection()) != null) {
      // Step 3: Replace intersection (vi,vi+1),(vj,vj+1) 
      // with (vj+1,vi+1),(vj,vi)
      
      // TODO need to understand Helds code completely and handle all
      // his special cases (eg. edge j lying on top of edge i, one vertex
      // lying on the other edge, or vertex vj+1 being the same as vi). 
      // Do we have to consider wrap-arounds?
      
      int vi = intersection[0];
      int vj = intersection[1];
      
      List<Point> op = p.getPoints();
      List<Point> np = new ArrayList<Point>(op.size());
      
      // first add all points prior to vi (including vi)
      for(int i = 0; i <= vi; i++)
        np.add(op.get(i));
      
      // second add vj to vi+1 in reverse order (both included)
      for(int i = vj; i >= vi + 1; i--)
        np.add(op.get(i));
      
      // third add vj+1 plus all remaining points
      for(int i = vj + 1; i < op.size(); i++)
        np.add(op.get(i));
      
      p.setPoints(np);
    }
    
    return p;
  }

}
