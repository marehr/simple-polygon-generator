package polygonsSWP.generators;

import java.util.Map;

import polygonsSWP.data.PolygonHistory;
import polygonsSWP.geometry.Polygon;


public interface PolygonGenerator
{

  /**
   * Returns the parameters this generator is aware of and
   * able to use.
   * 
   * @return list of parameters the generator accepts.
   */
  public Parameters[][] getAcceptedParameters();

  /**
   * Generates a simply polygon.
   * 
   * @param params the generator's parameters. Known parameters are:
   *          'n'->Integer: Number of vertices of the polygon 
   *          'points'->List<Point>: Set of points to construct the 
   *                             polygon of 
   *          'runs'->Integer: Number of iterations a generator should 
   *                           do to construct the polygon
   *          'size'->Integer: Edge length of the surrounding square.
   * @param steps history object to store the algorithm's step in. This might be
   *          null, in which case the algorithm obviously should not use it.
   * @return a Polygon
   */
  public Polygon generate(Map<Parameters, Object> params, PolygonHistory steps);
  
  public enum Parameters{
	  n, points, runs, size, radius, velocity
  }
}
