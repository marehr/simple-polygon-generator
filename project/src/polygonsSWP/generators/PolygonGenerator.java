package polygonsSWP.generators;

import polygonsSWP.geometry.Polygon;

public interface PolygonGenerator
{
  /**
   * Generates a simply polygon.
   * 
   * @return a Polygon, or null if the generation was cancelled.
   */
  public Polygon generate();
  
  /**
   * Stop method. Used to stop running generators. Implementing classes should react
   * on this after a short while.
   */
  public void stop();
}
