package polygonsSWP.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import polygonsSWP.generators.SpacePartitioning;
import polygonsSWP.geometry.LineSegment;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;

public class SpacePartitioningTest
{

  @Test
  public void shouldBeSimplePolygon() {
    List<Point> points = new ArrayList<Point>();
    points.add(new Point(148, 367));
    points.add(new Point(397,136));
    points.add(new Point(184,319));
    points.add(new Point(47,463));
    points.add(new Point(204,251));
    points.add(new Point(242,25));

    // should be simple
    for(int i = 0; i < 100; ++i){
      HashMap<String, Object> params = new HashMap<String, Object>();
      params.put("points", new ArrayList<Point>(points));

      OrderedListPolygon polygon = (OrderedListPolygon)
        new SpacePartitioning().generate(params, null);

      assertTrue(i + ". try is not simple", polygon.isSimple());
    }
  }

}
