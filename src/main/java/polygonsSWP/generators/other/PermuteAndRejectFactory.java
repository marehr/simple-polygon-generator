package polygonsSWP.generators.other;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import polygonsSWP.data.PolygonHistory;
import polygonsSWP.generators.IllegalParameterizationException;
import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.PolygonGeneratorFactory;
import polygonsSWP.generators.PolygonGeneratorFactory.Parameters;
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
      PolygonHistory steps)
    throws IllegalParameterizationException {
    List<Point> points = GeneratorUtils.createOrUsePoints(params, true);
    return new PermuteAndReject(points, steps,
        (Integer) params.get(Parameters.size));
  }


  private static class PermuteAndReject
    implements PolygonGenerator
  {

    private boolean doStop = false;
    private PolygonHistory steps;
    private List<Point> points;
    private int _size;

    PermuteAndReject(List<Point> points, PolygonHistory steps, int size) {
      this.points = points;
      this.steps = steps;
      this.doStop = false;
      this._size = size;
    }

    @Override
    public Polygon generate() {
      // Initiailize History
      if (steps != null) steps.clear();
      // Step 1: Generate polygon of given point set.
      OrderedListPolygon p = new OrderedListPolygon(points);
      // TODO: Do we have always the same BoundingBox with float?
      // Add a new scene, set Bounding Box and add the polygon.
      if (steps != null)
        steps.newScene().setBoundingBox(_size, _size).addPolygon(p, false).safe();

      while (!doStop) {
        // Step 2: Permute those n points to construct a Polygon
        p.permute();
        // Create a new scene for every polygon which is created.
        if (steps != null)
          steps.newScene().setBoundingBox(_size, _size).addPolygon(p, true).safe();
        // Step 3: Accept only simple polygons in counterclockwise orientation.
        if (p.isSimple() && (p.isClockwise() == -1)) break;
      }

      if (doStop) return null;
      else return p;
    }

    @Override
    public void stop() {
      doStop = true;
    }
  }
}
