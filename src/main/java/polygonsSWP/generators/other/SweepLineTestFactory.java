package polygonsSWP.generators.other;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import polygonsSWP.data.PolygonHistory;
import polygonsSWP.data.PolygonStatistics;
import polygonsSWP.data.Scene;
import polygonsSWP.generators.IllegalParameterizationException;
import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.PolygonGeneratorFactory;
import polygonsSWP.generators.heuristics.SpacePartitioningFactory;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.util.GeneratorUtils;


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
      PolygonStatistics stats, final PolygonHistory steps)
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
