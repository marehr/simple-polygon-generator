package polygonsSWP.generators.heuristics;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import polygonsSWP.data.History;
import polygonsSWP.data.PolygonStatistics;
import polygonsSWP.generators.IllegalParameterizationException;
import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.PolygonGeneratorFactory;
import polygonsSWP.geometry.LineSegment;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;


public class VelocityVirmaniFactory
  implements PolygonGeneratorFactory
{

  @Override
  public boolean acceptsUserSuppliedPoints() {
    return false;
  }

  @Override
  public List<Parameters> getAdditionalParameters() {
    List<Parameters> addparams = new LinkedList<Parameters>();
    addparams.add(Parameters.radius);
    addparams.add(Parameters.runs);
    addparams.add(Parameters.velocity);
    return addparams;
  }

  @Override
  public String toString() {
    return "Velocity Virmani";
  }

  @Override
  public PolygonGenerator createInstance(Map<Parameters, Object> params, PolygonStatistics stats, History steps)
    throws IllegalParameterizationException {

    Object tmp = params.get(Parameters.radius);
    if (tmp == null) throw new IllegalParameterizationException("Radius not set.", Parameters.radius);
    Long radius = (tmp instanceof Long) ? (Long) tmp : new Long(((Integer) tmp).intValue()); 

    Integer n = (Integer) params.get(Parameters.n);
    if (n == null) throw new IllegalParameterizationException("Number of points not set.", Parameters.n);

    Integer runs = (Integer) params.get(Parameters.runs);
    if (runs == null) throw new IllegalParameterizationException("Number of iterations not set.", Parameters.runs);

    Integer bound = (Integer) params.get(Parameters.size);
    if (bound == null) throw new IllegalParameterizationException("Size of bounding box not set.", Parameters.size);

    Integer maxVelo = (Integer) params.get(Parameters.velocity);
    if (maxVelo == null) throw new IllegalParameterizationException("Maximum velocity not set.", Parameters.velocity);

    if (radius * 2 > bound) { throw new IllegalParameterizationException("Radius must be smaller than the bounds allow (Pre: Radius * 2 < bound).", Parameters.radius); }
    
    if(n<3){throw new IllegalParameterizationException("n must be greater or equal 3");}
    
    return new VelocityVirmani(n, radius, runs, bound, maxVelo, steps, stats);
  }


  private static class VelocityVirmani
    implements PolygonGenerator
  {

    private Random rand;
    private int n;
    private double radius;
    private int runs;
    private int maxVelo;
    private int bound;
    final private History steps;
    final private PolygonStatistics statistics;

    private boolean stop = false;

    VelocityVirmani(int n, long radius, int runs, int bound, int maxVelo, History steps, PolygonStatistics statistics) {
      this.rand = new Random();
      this.n = n;
      this.radius = radius;
      this.runs = runs;
      this.bound = bound;
      this.maxVelo = maxVelo;
      this.steps = steps;
      this.statistics = statistics;
    }

    @Override
    public Polygon generate() {

      OrderedListPolygon poly = regularPolygon(n, radius, bound);

      if(steps != null){
        steps.clear();

        steps.newScene()
        .addPoints(poly.getPoints(), true)
        .addPolygon(poly, true)
        .save();
      }

      double velox, veloy;

      int rejections = 0;
      int iterations = runs;
      double avgspeed_without_rejections = 0;
      
      
      while (runs > 0 && !stop) // The Loop for the Number of Iterations. Stops if "stop" is true.
      {
        
        for (int i = 0; i < n; i++) // Looping through the Points
        {

          // Calculates a Random Velocity. Can be Negative.
          velox = rand.nextBoolean() ? rand.nextDouble() * maxVelo : -rand.nextDouble() * maxVelo;
          veloy = rand.nextBoolean() ? rand.nextDouble() * maxVelo : -rand.nextDouble() * maxVelo;

          // Checks if the Polygon is in Bound
          if (poly.getPoint(i).x + velox > bound || poly.getPoint(i).y + veloy > bound || poly.getPoint(i).x + velox < 0 || poly.getPoint(i).y + veloy < 0) continue;

          // Add the Velocity to the Point
          poly.getPoint(i).x += velox;
          poly.getPoint(i).y += veloy;

          // If its not simple anymore, revert the modifications
          if (!isSimple(poly, i)) {
            poly.getPoint(i).x -= velox;
            poly.getPoint(i).y -= veloy;

            if (statistics != null) {
              avgspeed_without_rejections += velox + veloy; // To Calculate the Average velocity without collisions
              rejections++;
            }
          }
        }
        runs--;
        
        if(steps != null){
          steps.newScene()
          .addPoints(poly.getPoints(), true)
          .addPolygon(poly, true)
          .save();
        }
      }
      if (stop) return null;// If stop is called and this was finished. Give back null.

      if (statistics != null) {
        avgspeed_without_rejections = avgspeed_without_rejections / (n * iterations);
        statistics.avg_velocity_without_collisions = avgspeed_without_rejections;
        statistics.iterations = iterations;
        statistics.radius = statistics.radius;
        statistics.rejections = rejections;
      }
      
      if(steps != null)
        steps.newScene().addPolygon(poly, true).save();
      
      return poly;
    }
    
    /**
     * Check for simple. Checks only one Point with the Others.
     * Condition: Polygon is simple if the given Point on "indexToCheck" doesnt intersect with the other Lines
     * @param polygon Polygon
     * @param indexToCheck
     * @return
     */
    private boolean isSimple(OrderedListPolygon polygon, int indexToCheck)
    {
      int size = polygon.size();
      int min = (indexToCheck-1+size) % size;//Calculates the index of the previous and next Point
      int max = (indexToCheck + 1) % size;
      LineSegment l1 = new LineSegment(polygon.getPoint(min), polygon.getPoint(indexToCheck));
      LineSegment l2 = new LineSegment(polygon.getPoint(indexToCheck), polygon.getPoint(max));
      LineSegment otherLine;
      Point[] res1, res2;
      
      for(int i = 0; i < size; i++)
      {
        if(indexToCheck == i || (i+1) % size == indexToCheck) //Shouldnt compare to its own lines 
          continue;
        
        otherLine = new LineSegment(polygon.getPoint(i), polygon.getPoint((i+1)%size));
        res1 = l1.intersect(otherLine, true);
        res2 = l2.intersect(otherLine, true);
        
        if(res1 == null && res2 == null)
          continue;//continue to check
        return false;//not simple
        
      }
      return true;//every combination checked. Its simple.
    }


    /**
     * Generates a regularPolygon
     * 
     * @param n Number of Points in the regular Polygon
     * @param radius The Radius of the regular Polygon
     * @param bound Bound. To Center the Polygon itself
     * @return
     */
    private OrderedListPolygon regularPolygon(int n, double radius, double bound) {
      double winkel = (Math.PI * 2) / n;
      double x = radius;
      double y = 0;
      double x1, y1;
      double tmpWinkel;
      OrderedListPolygon poly = new OrderedListPolygon();
      for (int i = 0; i < n; i++) {
        tmpWinkel = winkel * i;
        x1 = (x * Math.cos(tmpWinkel) - y * Math.sin(tmpWinkel)) // Calculating the new Point
            +
            (bound / 2); // Translation. So the Point has a Origin of top left

        y1 = -(x * Math.sin(tmpWinkel) + y * Math.cos(tmpWinkel)) + (bound / 2);// y has to be negative because the y axe had another directions.
        poly.addPoint(new Point(x1, y1));
      }
      return poly;
    }

    @Override
    public void stop() {
      stop = true;
    }

  }
}
