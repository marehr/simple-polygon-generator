package polygonsSWP.util;

import java.util.List;
import java.util.Map;

import polygonsSWP.data.Point;

public class GeneratorUtils
{
  @SuppressWarnings("unchecked")
  public static List<Point> createOrUsePoints(Map<String, Object> params) {
    Integer n = (Integer) params.get("n");
    Integer size = (Integer) params.get("size");
    List<?> s = (List<?>) params.get("points");
    
    // TODO remove
    assert(s != null || (n != null && size != null));

    if(s == null)
      s = MathUtils.createRandomSetOfPointsInSquare(n, size);

    return (List<Point>) s;
  }
}
