package polygonsSWP.generators.other;

import java.util.List;
import java.util.Map;

import polygonsSWP.data.PolygonHistory;
import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.PolygonGeneratorFactory;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.util.GeneratorUtils;

public class ConvexHullGeneratorFactory implements PolygonGeneratorFactory {
  
  private static final Parameters[][] params = new Parameters[][] {
      new Parameters[] {Parameters.n, Parameters.size},
      new Parameters[] {Parameters.points}
    };
  
  @Override
  public Parameters[][] getAcceptedParameters() {
    return params;
  }
  
  public String toString() {
    return "ConvexHull";
  }
  
  @Override
  public PolygonGenerator createInstance(Map<Parameters, Object> params,
      PolygonHistory steps) {
    return new ConvexHullGenerator(params, steps);
  }
  
  private static class ConvexHullGenerator implements PolygonGenerator {

    private Map<Parameters, Object> params;
    private PolygonHistory steps;
    
    ConvexHullGenerator(Map<Parameters, Object> params, PolygonHistory steps) {
      this.params = params;
      this.steps = steps;
    }
    
    @Override
    public Polygon generate() {
      List<Point> points = GeneratorUtils.createOrUsePoints(params);
      return GeneratorUtils.convexHull(points);
    }

    @Override
    public void stop() {
      // Do nothing. ConvexHull is fast.
    }
  }
}


