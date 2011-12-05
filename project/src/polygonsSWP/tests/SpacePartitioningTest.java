package polygonsSWP.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.PolygonGenerator.Parameters;
import polygonsSWP.generators.SpacePartitioning;
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
      HashMap<Parameters, Object> params = new HashMap<Parameters, Object>();
      params.put(Parameters.points, new ArrayList<Point>(points));

      OrderedListPolygon polygon = (OrderedListPolygon)
        new SpacePartitioning().generate(params, null);

      assertTrue(i + ". try is not simple", polygon.isSimple());
    }
  }

  /**
   * polygon that fails isSimple test, after completion of the algorithm.
   */
  @Test
  public void bug0() {
    List<Point> points = new ArrayList<Point>();
    points.add(new Point(546,76));
    points.add(new Point(228,51));
    points.add(new Point(247,67));
    points.add(new Point(589,355));
    points.add(new Point(286,567));
    points.add(new Point(158,243));
    points.add(new Point(6,389));
    points.add(new Point(69,121));
    points.add(new Point(83,20));
    points.add(new Point(229,4));

    // should be simple
    for(int i = 0; i < 100; ++i){
      HashMap<Parameters, Object> params = new HashMap<Parameters, Object>();
      params.put(Parameters.points, new ArrayList<Point>(points));

      PolygonGenerator gen = new SpacePartitioning();

      try{
        gen.generate(params, null);
      } catch(RuntimeException e) {
        System.err.println(i + ". try is not simple");
        throw e;
      }
    }
  }

}
