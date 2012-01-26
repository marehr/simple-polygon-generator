package polygonsSWP.geometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import polygonsSWP.util.MathUtils;


/**
 * This is a class just for trapezoids, so they have 4 or 3 points! max!!!!!!
 * 
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 */

public class Trapezoid
  extends OrderedListPolygon
{
  public Trapezoid(List<Point> coords) {
    super(coords);
    assert (coords.size() < 5 && coords.size() > 2);
  }

  @Override
  public List<Triangle> triangulate() {

    List<Triangle> retList = new ArrayList<Triangle>();
    if (this.size() == 3) {
      retList.add(new Triangle(_coords));
      return retList;
    }
    else {
      retList.add(new Triangle(_coords.get(0), _coords.get(1), _coords.get(3)));
      retList.add(new Triangle(_coords.get(1), _coords.get(2), _coords.get(3)));
      return retList;
    }
  }
}
