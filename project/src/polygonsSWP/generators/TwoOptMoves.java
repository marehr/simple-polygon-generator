package polygonsSWP.generators;

import java.util.Map;

import polygonsSWP.data.Edge;
import polygonsSWP.data.GraphPolygon;
import polygonsSWP.data.PGenerator;
import polygonsSWP.data.PHistory;
import polygonsSWP.data.Polygon;
import polygonsSWP.util.GeneratorUtils;

public class TwoOptMoves implements PGenerator 
{

  @Override
  public String[] getAcceptedParameters() {
    return new String[] {"n", "size", "points"};
  }

  @Override
  public Polygon run(Map<String, Object> params, PHistory steps) {
    
    // Step 1: Generate n points in the plane or use the given set of points.
    GraphPolygon p = new GraphPolygon(GeneratorUtils.createOrUsePoints(params));
    
    // Step 2: Generate random permutation in case given set was ordered somehow.
    p.permute();
    
    Edge[] intersection = null;
    while((intersection = p.findRandomIntersection()) != null) {
      // Step 3: Replace intersection (vi,vi+1),(vj,vj+1) 
      // with (vj+1,vi+1),(vj,vi)
      Edge i = intersection[0];
      Edge j = intersection[1];
      Edge new1 = new Edge(j.a, i.a);
      Edge new2 = new Edge(j.b, i.b);
      p.removeEdge(i);
      p.removeEdge(j);
      p.addEdge(new1);
      p.addEdge(new2);
    }
    
    if(p.getEdges().size() != ((Integer) params.get("n")))
      System.out.println(p.getEdges().size());
    
    return p;
  }

}
