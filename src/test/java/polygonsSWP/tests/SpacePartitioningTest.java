package polygonsSWP.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import polygonsSWP.generators.IllegalParameterizationException;
import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.PolygonGeneratorFactory;
import polygonsSWP.generators.PolygonGeneratorFactory.Parameters;
import polygonsSWP.generators.heuristics.SpacePartitioningFactory;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;

public class SpacePartitioningTest
{

  @Test
  public void shouldBeSimplePolygon() throws IllegalParameterizationException {
    List<Point> points = new ArrayList<Point>();
    points.add(new Point(148, 367));
    points.add(new Point(397,136));
    points.add(new Point(184,319));
    points.add(new Point(47,463));
    points.add(new Point(204,251));
    points.add(new Point(242,25));

    PolygonGeneratorFactory factory = new SpacePartitioningFactory();

    // should be simple
    for(int i = 0; i < 100; ++i){
      HashMap<Parameters, Object> params = new HashMap<Parameters, Object>();
      params.put(Parameters.points, new ArrayList<Point>(points));

      PolygonGenerator gen = factory.createInstance(params, null, null);
      OrderedListPolygon polygon = (OrderedListPolygon) gen.generate();

      assertTrue(i + ". try is not simple", polygon.isSimple());
    }
  }

  /**
   * polygon that fails isSimple test, after completion of the algorithm.
   * @throws IllegalParameterizationException 
   */
  @Test
  public void bug0() throws IllegalParameterizationException {
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

    PolygonGeneratorFactory factory = new SpacePartitioningFactory();

    // should be simple
    for(int i = 0; i < 100; ++i){
      HashMap<Parameters, Object> params = new HashMap<Parameters, Object>();
      params.put(Parameters.points, new ArrayList<Point>(points));

      PolygonGenerator gen = factory.createInstance(params, null, null);
      OrderedListPolygon polygon = (OrderedListPolygon) gen.generate();

      assertNotNull(polygon);
    }
  }

}
