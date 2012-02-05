package polygonsSWP.generators.other;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import polygonsSWP.data.History;
import polygonsSWP.data.PolygonStatistics;
import polygonsSWP.generators.IllegalParameterizationException;
import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.PolygonGeneratorFactory;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.util.GeneratorUtils;


public class ConvexHullGeneratorFactory
  implements PolygonGeneratorFactory
{

  @Override
  public boolean acceptsUserSuppliedPoints() {
    return true;
  }

  @Override
  public List<Parameters> getAdditionalParameters() {
    return new LinkedList<Parameters>();
  }

  public String toString() {
    return "ConvexHull";
  }

  @Override
  public PolygonGenerator createInstance(Map<Parameters, Object> params,
      PolygonStatistics stats, History steps)
    throws IllegalParameterizationException {

    List<Point> points = GeneratorUtils.createOrUsePoints(params);
    return new ConvexHullGenerator(points, steps);
  }


  private static class ConvexHullGenerator
    implements PolygonGenerator
  {

    private List<Point> points;
    final private History steps;

    ConvexHullGenerator(List<Point> points, History steps) {
      this.points = points;
      this.steps = steps;
    }

    @Override
    public Polygon generate() {

      if (steps != null) {
        steps.clear();
        steps.newScene().addPoints(points, true).save();
      }

      Polygon poly = GeneratorUtils.convexHull(points);

      if (steps != null)
        steps.newScene().addPolygon(poly, true).save();

      return poly;
    }

    @Override
    public void stop() {
      // Do nothing. ConvexHull is fast.
    }
  }
}
