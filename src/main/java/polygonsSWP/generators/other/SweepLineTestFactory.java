package polygonsSWP.generators.other;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import polygonsSWP.data.History;
import polygonsSWP.data.PolygonStatistics;
import polygonsSWP.generators.IllegalParameterizationException;
import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.PolygonGeneratorFactory;
import polygonsSWP.generators.heuristics.SpacePartitioningFactory;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Polygon;


public class SweepLineTestFactory
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
    return "SweepLineTest";
  }

  @Override
  public PolygonGenerator createInstance(Map<Parameters, Object> params,
      PolygonStatistics stats, final History steps)
    throws IllegalParameterizationException {

    final OrderedListPolygon polygon, constPoly = OrderedListPolygon.sweepLineTestPolygon;

    if(constPoly == null || constPoly.size() < 3)
      polygon = (OrderedListPolygon) new SpacePartitioningFactory().createInstance(params, null, null).generate();
    else
      polygon = constPoly;

    return new PolygonGenerator(){

      @Override
      public Polygon generate() {
        int size = 600;

        if (steps != null) {
          steps.clear();
          steps.newScene().setBoundingBox(size, size).addPolygon(polygon, true).save();
        }

        try{
          polygon.sweepLine();
        }catch(Exception e){
          e.printStackTrace();
        }

        return polygon;
      }

      @Override
      public void stop() {
      }
      
    };
  }
}
