package polygonsSWP.generators;

import java.util.Map;

import polygonsSWP.data.PolygonHistory;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.util.GeneratorUtils;

public class PermuteAndReject
  implements PolygonGenerator
{

  private boolean doStop = false;
  
  private Parameters[][] params = new Parameters[][] {
    new Parameters[] {Parameters.n, Parameters.size},
    new Parameters[] {Parameters.points}
  };

  @Override
  public Parameters[][] getAcceptedParameters() {
    return params;
  }

  @Override
  public Polygon generate(Map<Parameters, Object> params, PolygonHistory steps) {
    doStop = false;

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
  public String toString() {
    return "Permute & Reject";
  }

  @Override
  public void stop() {
    doStop = true;
  }

}
