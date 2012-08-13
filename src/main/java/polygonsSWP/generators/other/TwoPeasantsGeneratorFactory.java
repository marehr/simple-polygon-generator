package polygonsSWP.generators.other;

import java.util.Collections;
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
import polygonsSWP.util.MathUtils;

/**
 * Two Peasants
 * 
 * Found here: http://web.informatik.uni-bonn.de/I/GeomLab/RandomPolygon/index.html
 * 
 * (c) 2011-2012
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Kadir Tugan <kadir.tugan@gmail.com>
 */
public class TwoPeasantsGeneratorFactory
  implements PolygonGeneratorFactory
{

  @Override
  public PolygonGenerator createInstance(Map<Parameters, Object> params,
      PolygonStatistics stats, History steps)
    throws IllegalParameterizationException {

    List<Point> points = GeneratorUtils.createOrUsePoints(params);
    return new TwoPeasantsGenerator(points, steps, stats);
  }

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
    return "TwoPeasants";
  }
  
  private static class TwoPeasantsGenerator implements PolygonGenerator {

    private List<Point> points;
    private History steps;
    
    @SuppressWarnings("unused")
    private PolygonStatistics stats;
    
    private TwoPeasantsGenerator(List<Point> points, History steps, PolygonStatistics stats) {
      this.points = points;
      this.steps = steps;
      this.stats = stats;
    }

    @Override
    public Polygon generate() {
      List<Point> plist = new LinkedList<Point>();
      
      if(steps != null)
        steps.newScene().addPoints(points, true).save();
      
      // Sort point list by x-coordinate.
      Collections.sort(points, Point.XCompare);

      // Imagine a horizontal line going to through the polygon.
      Point first = points.get(0);
      Point last = points.get(points.size() - 1);
      
      // Add first point.
      plist.add(first);
      
      // Add upper boundary.
      addBoundary(points, first, last, plist);
      
      // Add middle point.
      plist.add(last);
      
      // Add lower boundary.
      Collections.reverse(points);
      addBoundary(points, last, first, plist);      
            
      OrderedListPolygon olp = new OrderedListPolygon(plist);
      
      if(steps != null)
        steps.newScene().addPolygon(olp, true).save();
      
      return olp;
    }

    @Override
    public void stop() {
    }
    
    private void addBoundary(List<Point> points, Point first, Point last, List<Point> plist) {
      for(int i = 1; i < points.size() - 1; i++) {
        Point p = points.get(i);
        int oriented = MathUtils.checkOrientation(first, last, p);
        switch(oriented) {
          case 1:
            plist.add(p);
            if(steps != null) {
              steps.newScene()
                .addPoint(first, true)
                .addPoint(last, true)
                .addPolygon(new OrderedListPolygon(plist), true)
                .save();
            }
            break;
          case 0:
            throw new RuntimeException("Point set not in general position.");
        }
      }
    }
  }
}
