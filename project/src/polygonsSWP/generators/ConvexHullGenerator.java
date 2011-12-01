package polygonsSWP.generators;

import java.util.List;
import java.util.Map;

import polygonsSWP.data.PolygonHistory;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.util.GeneratorUtils;

public class ConvexHullGenerator implements PolygonGenerator {

  @Override
  public String[] getAcceptedParameters() {
    return new String[] { "n", "size", "points" };
  }

  @Override
  public Polygon generate(Map<String, Object> params, PolygonHistory steps) {
    List<Point> points = GeneratorUtils.createOrUsePoints(params);
    return GeneratorUtils.convexHull(points);
  }

  public String toString(){
    return "ConvexHull";
  }
}
