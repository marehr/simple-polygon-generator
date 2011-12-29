package polygonsSWP.generators.other;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import polygonsSWP.data.PolygonHistory;
import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.PolygonGeneratorFactory;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.util.GeneratorUtils;

public class PermuteAndRejectFactory 
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
    return "Permute & Reject";
  }
  
  @Override
  public PolygonGenerator createInstance(Map<Parameters, Object> params,
      PolygonHistory steps) {
    return new PermuteAndReject(params, steps);
  }
  
  private static class PermuteAndReject implements PolygonGenerator {
    
    private boolean doStop = false;
    private Map<Parameters, Object> params;
    private PolygonHistory steps;
    
    PermuteAndReject(Map<Parameters, Object> params, PolygonHistory steps) {
      this.params = params;
      this.steps = steps;
      this.doStop = false;
    }
    
    @Override
    public Polygon generate() {  
      // Step 1: Generate n points in the plane or use the given set of points
      OrderedListPolygon p = new OrderedListPolygon(GeneratorUtils.createOrUsePoints(params, true));
  
      while(!doStop) {
        // Step 2: Permute those n points to construct a Polygon
        p.permute();
        
        // Step 3: Accept only simple polygons in counterclockwise orientation.
        if(p.isSimple() && (p.isClockwise() == -1))
          break;
      }
      
      if(doStop)
        return null;
      else
        return p;
    }
    
    @Override
    public void stop() {
      doStop = true;
    }
  }
}
