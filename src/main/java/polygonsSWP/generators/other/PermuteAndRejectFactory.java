package polygonsSWP.generators.other;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import polygonsSWP.data.History;
import polygonsSWP.data.PolygonStatistics;
import polygonsSWP.generators.IllegalParameterizationException;
import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.PolygonGeneratorFactory;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.util.GeneratorUtils;


public class PermuteAndRejectFactory
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

  @Override
  public String toString() {
    return "Permute & Reject";
  }

  @Override
  public PolygonGenerator createInstance(Map<Parameters, Object> params,
      PolygonStatistics stats, History steps)
    throws IllegalParameterizationException {

    List<Point> points = GeneratorUtils.createOrUsePoints(params, true);
    return new PermuteAndReject(points, steps, stats);
  }


  private static class PermuteAndReject
    implements PolygonGenerator
  {

    private boolean doStop = false;
    private List<Point> points;

    final private History steps;
    final private PolygonStatistics statistics;
    
    PermuteAndReject(List<Point> points, History steps, PolygonStatistics statistics) {
      this.points = points;
      this.steps = steps;
      this.statistics = statistics;
    }

    @Override
    public Polygon generate() {
      // Initialize History
      if (steps != null) steps.clear();
      
      // Step 1: Generate polygon of given point set.
      OrderedListPolygon p = new OrderedListPolygon(points);
      

      // Add a new scene and add the polygon.
      if (steps != null)
        steps.newScene().addPolygon(p, true).save();

      if(statistics != null)
        statistics.iterations = 0;

      do {
        // Step 3: Accept only simple polygons
        if (p.isSimple()){
          //if not in counterclockwise orientation, reverse orientation
          if(p.isClockwise() == 1) p.reverse();

          break;
        }

        // Step 2: Permute those n points to construct a Polygon
        p.permute();

        // Create a new scene for every polygon which is created.
        if (steps != null)
          steps.newScene().addPolygon(p, true).save();

        if(statistics != null)
          statistics.iterations++;

      } while (!doStop);

      if (doStop) return null;
      else return p;
    }

    @Override
    public void stop() {
      doStop = true;
    }
  }
}
