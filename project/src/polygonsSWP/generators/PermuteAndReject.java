package polygonsSWP.generators;

import java.util.List;
import java.util.Map;
import java.util.Random;

import polygonsSWP.data.PGenerator;
import polygonsSWP.data.PHistory;
import polygonsSWP.data.Point;
import polygonsSWP.data.Polygon;
import polygonsSWP.util.MathUtils;


public class PermuteAndReject
  implements PGenerator
{

  @Override
  public String[] getAcceptedParameters() {
    return new String[] { "n", "size" };
  }

  @Override
  public Polygon run(Map<String, Object> params, PHistory steps) {
    Integer n = (Integer) params.get("n");
    Integer size = (Integer) params.get("size");
    
    // TODO remove
    assert(n != null);
    assert(size != null);

    // Step 1: Generate n points in the plane
    List<Point> s = MathUtils.createRandomSetOfPointsInSquare(n, size);
    
    // Step 2: Permute those n points to construct a Polygon
    Random r = new Random(System.currentTimeMillis());
    Polygon p = new Polygon();
    while(true) {      
      while(s.size() > 0) {
        p.addPoint(s.remove(r.nextInt(s.size())));
      }
      
      // Step 3: Accept or reject
      if(p.isSimple())
        break;
      else {
        // Swap lists
        s = p.getPoints();
        p = new Polygon();
      }
    }
    
    return p;
  }

}
