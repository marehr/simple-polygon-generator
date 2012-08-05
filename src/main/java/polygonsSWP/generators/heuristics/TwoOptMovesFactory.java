package polygonsSWP.generators.heuristics;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import polygonsSWP.data.History;
import polygonsSWP.data.PolygonStatistics;
import polygonsSWP.generators.IllegalParameterizationException;
import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.PolygonGeneratorFactory;
import polygonsSWP.geometry.LineSegment;
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
      History steps) throws IllegalParameterizationException {
    List<Point> points = GeneratorUtils.createOrUsePoints(params, true);
    return new TwoOptMoves(points, steps, stats);
  }

  private static class TwoOptMoves implements PolygonGenerator {
    
    private boolean doStop = false;
    private List<Point> points;
    final private History steps;
    final private PolygonStatistics statistics;
    
    TwoOptMoves(List<Point> points, History steps, PolygonStatistics statistics) {
      this.points = points;
      this.steps = steps;
      this.statistics = statistics;
      this.doStop = false;
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
      
      // Initialize history & statistics.
      if(steps != null) {
        steps.clear();
        steps.newScene().addPolygon(p, true).save();
      }

      if(statistics != null) {
        statistics.iterations = 0;
      }

      Integer[] intersection = null;
      List<Point> op = null;
      List<Point> np = null;
      while(!doStop && (intersection = p.findRandomIntersection()) != null) {
        // Step 3: Replace intersection (vi,vi+1),(vj,vj+1) 
        // with (vj+1,vi+1),(vj,vi)
        
        int vi = intersection[0];
        int vj = intersection[1];
        
        op = p.getPoints();
        np = new ArrayList<Point>(op.size());
        
        if(steps != null){
          steps.newScene().addPolygon(p, true).addLineSegment(
            new LineSegment(op.get(vi), op.get((vi + 1) % op.size())), Color.RED
          ).addLineSegment(
            new LineSegment(op.get(vj), op.get((vj + 1) % op.size())), Color.MAGENTA
          ).save();
        }

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
        
        if(steps != null)
          steps.newScene().addPolygon(p, true).save();
        
        if(statistics != null)
          statistics.iterations++;
      }
      
      if(doStop)
        return null;

      return p;
    }
    
  }
}
