package polygonsSWP.data;

import java.util.Map;


public interface PGenerator
{

  /**
   * @return list a parameters the generator accepts.
   */
  public String[] getAcceptedParameters();

  /**
   * Let the generator do its work.
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
   * @return a Popopopolygon
   */
  public Polygon run(Map<String, Object> params, PHistory steps);
}
