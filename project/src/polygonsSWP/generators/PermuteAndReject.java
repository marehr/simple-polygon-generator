package polygonsSWP.generators;

import java.util.Map;

import polygonsSWP.data.OrderedListPolygon;
import polygonsSWP.data.PGenerator;
import polygonsSWP.data.PHistory;
import polygonsSWP.data.Polygon;
import polygonsSWP.util.GeneratorUtils;

public class PermuteAndReject
  implements PGenerator
{

  @Override
  public String[] getAcceptedParameters() {
    return new String[] { "n", "size", "points" };
  }

  @Override
  public Polygon run(Map<String, Object> params, PHistory steps) {

    // Step 1: Generate n points in the plane or use the given set of points
    OrderedListPolygon p = new OrderedListPolygon(GeneratorUtils.createOrUsePoints(params));

    while(true) {
      // Step 2: Permute those n points to construct a Polygon
      p.permute();
      
      // Step 3: Accept or reject
      if(p.isSimple())
        break;
    }
    
    return p;
  }

}