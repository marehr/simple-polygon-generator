package polygonsSWP.generators;

import java.util.List;
import java.util.Map;

import polygonsSWP.data.PolygonHistory;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.util.GeneratorUtils;

public class ConvexHullGenerator implements PolygonGenerator {

	
	private Parameters[][] params = new Parameters[][]
			{
				new Parameters[] {Parameters.n, Parameters.size},
				new Parameters[] {Parameters.points}
			};
	
	
  @Override
  public Parameters[][] getAcceptedParameters() {
    return params;
  }

  @Override
  public Polygon generate(Map<Parameters, Object> params, PolygonHistory steps) {
    List<Point> points = GeneratorUtils.createOrUsePoints(params);
    return GeneratorUtils.convexHull(points);
  }

  public String toString(){
    return "ConvexHull";
  }
}
