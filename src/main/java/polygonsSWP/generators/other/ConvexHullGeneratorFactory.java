package polygonsSWP.generators.other;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import polygonsSWP.data.PolygonHistory;
import polygonsSWP.data.PolygonStatistics;
import polygonsSWP.data.Scene;
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
      PolygonStatistics stats, PolygonHistory steps)
    throws IllegalParameterizationException {

    List<Point> points = GeneratorUtils.createOrUsePoints(params);
    return new ConvexHullGenerator(points, steps, stats);
  }


  private static class ConvexHullGenerator
    implements PolygonGenerator
  {

    private List<Point> points;
    private PolygonHistory steps = null;
    private int size;
    private PolygonStatistics statistics = null;

    ConvexHullGenerator(List<Point> points, PolygonHistory steps,
        PolygonStatistics statistics) {

      this.points = points;
      this.steps = steps;
      // FIXME: Hard-coded boundingBox size should be passed as parameter
      this.size = 600;
      this.statistics = statistics;
    }

    @Override
    public Polygon generate() {

      if (steps != null) {
        steps.clear();
        Scene initial = steps.newScene().setBoundingBox(size, size);
        for (Point item : points) {
          initial.addPoint(item, true);
        }
        initial.save();
      }

      Polygon poly = GeneratorUtils.convexHull(points);

      if (steps != null)
        steps.newScene().setBoundingBox(size, size).addPolygon(poly, true).save();

      return poly;
    }

    @Override
    public void stop() {
      // Do nothing. ConvexHull is fast.
    }
  }
}
