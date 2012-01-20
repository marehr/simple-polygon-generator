package polygonsSWP.generators.heuristics;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import polygonsSWP.data.PolygonHistory;
import polygonsSWP.data.PolygonStatistics;
import polygonsSWP.generators.IllegalParameterizationException;
import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.PolygonGeneratorFactory;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.util.GeneratorUtils;

public class TwoOptMovesFactory 
  implements PolygonGeneratorFactory {
  
  @Override
  public boolean acceptsUserSuppliedPoints() {
    return true;
  }

  @Override
  public List<Parameters> getAdditionalParameters() {
    return new LinkedList<Parameters>();
  }
  
  @Override
  public String toString() {
    return "2-Opt moves";
  }
  
  @Override
  public PolygonGenerator createInstance(Map<Parameters, Object> params,
      PolygonStatistics stats,
      PolygonHistory steps) throws IllegalParameterizationException {
    List<Point> points = GeneratorUtils.createOrUsePoints(params, true);
    return new TwoOptMoves(points, steps, stats);
  }

  private static class TwoOptMoves implements PolygonGenerator {
    
    private boolean doStop = false;
    private List<Point> points;
    private PolygonHistory steps;
    private PolygonStatistics statistics = null;
    
    TwoOptMoves(List<Point> points, PolygonHistory steps, PolygonStatistics statistics) {
      this.points = points;
      this.steps = steps;
      this.doStop = false;
      this.statistics = statistics;
    }
    
    @Override
    public void stop() {
      doStop = true;
    }
    
    @Override
    public Polygon generate() {    
      // Step 1: Generate polygon of given set of points.
      OrderedListPolygon p = new OrderedListPolygon(points);
      
      // Step 2: Generate random permutation in case given set was ordered somehow.
      p.permute();
      
      steps.clear();
      steps.newScene();
      
      Integer[] intersection = null;
      while(!doStop && (intersection = p.findRandomIntersection()) != null) {
        // Step 3: Replace intersection (vi,vi+1),(vj,vj+1) 
        // with (vj+1,vi+1),(vj,vi)
        
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
      
      if(doStop)
        return null;
      
      return p;
    }
    
  }
}
