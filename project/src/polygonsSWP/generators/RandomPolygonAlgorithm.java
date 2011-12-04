package polygonsSWP.generators;

import java.util.List;
import java.util.Map;
import java.util.Random;

import polygonsSWP.data.PolygonHistory;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.util.GeneratorUtils;
import polygonsSWP.util.MathUtils;


public class RandomPolygonAlgorithm
  implements PolygonGenerator
{

	private Parameters[][] params = new Parameters[][]
			{
				new Parameters[] {Parameters.n, Parameters.size},
			};
	
  @Override
  public Parameters[][] getAcceptedParameters() {
    return params;
  }

  @Override
  public Polygon generate(Map<Parameters, Object> params, PolygonHistory steps) {
    
    Random random = new Random(System.currentTimeMillis());

    // 1. generate 3 rand points -> polygon P
    OrderedListPolygon polygon =
        new OrderedListPolygon(MathUtils.createRandomSetOfPointsInSquare(3,
            (Integer) params.get(Parameters.size)));
    
    List<Point> polygonPoints = polygon.getPoints();
    
    // 2. n-3 times:
    for (int i = 0; i < (Integer) params.get(Parameters.n) - 3;) {
      // 2.a select random line segment VaVb
      // (assumed that there will be less than 2^31-1 points)
      int randomIndex = random.nextInt(polygonPoints.size());
      Point Va = polygonPoints.get(randomIndex);
      Point Vb = polygonPoints.get(randomIndex+1);
      // 2.b determine visible region to VaVb -> P'
      Polygon visibleRegion = GeneratorUtils.visiblePolygonRegionFromLineSegment(polygon, Va, Vb);
      // 2.c randomly select point Vc in P'
      Point randomPoint = visibleRegion.createRandomPoint();
      // 2.d add line segments VaVc and VcVb (delete line segment VaVb)
      polygonPoints.add(randomIndex, randomPoint);
    }
    
    return polygon;
  }
  
  @Override
  public String toString() {
    return "RandomPolygonAlgorithm";
  }
}
