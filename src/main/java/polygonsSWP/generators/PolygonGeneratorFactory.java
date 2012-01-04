package polygonsSWP.generators;

import java.util.List;
import java.util.Map;

import polygonsSWP.data.PolygonHistory;

public interface PolygonGeneratorFactory
{
  /**
   * @return the PolygonGenerator's name.
   */
  @Override
  public String toString();
  
  /**
   * Creates a fresh instance of the generator initialized with
   * the given parameters.
   * 
   * @param params the generator's parameters. See <code>Parameters</code> enum.
   * @param steps history object to store the algorithm's step in. 
   *        This might be null, in which case the algorithm 
   *        obviously should not use it.
   *          
   * @return a PolygonGenerator
   * @throws IllegalParameterizationException in case the given parameters
   *         did not meet the PolygonGenerator's requirements.
   */
  public PolygonGenerator createInstance(Map<Parameters, Object> params, PolygonHistory steps) throws IllegalParameterizationException;
  
  /**
   * Specifies whether the generator can handle a set of
   * user-defined points (provided via the POINTS parameter).
   * 
   * @return true, in case the generator takes a set of points,
   *         false otherwise.
   */
  public boolean acceptsUserSuppliedPoints();
  
  /**
   * Returns a list of additional mandatory parameters needed
   * to initialize this generator.
   * 
   * @return list of additional parameters needed by the generator.
   */
  public List<Parameters> getAdditionalParameters();
  
  /**
   * Generator parameterization.
   */
  public enum Parameters {
    /** Number of polygon points (Integer). */
    n, 
    /** Set of points (List<Point>). */
    points, 
    /** Number of iterations (Integer). */
    runs, 
    /** Size of bounding box (Integer). */
    size, 
    /** Size of circle for initial regular polygon (Long). */
    radius, 
    /** Speed of vertex movements (Long). */
    velocity
  }
}