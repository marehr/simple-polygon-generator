package polygonsSWP.data;

/**
 * Second scratch of needed data. This may be the final implementation.
 * 
 * @author bigzed
 * @author Kadir
 */
public class PolygonStatistics
{
  //All
  public Integer number_of_points = null;
  public Double surface_area = null;
  public Double circumference = null; //umfang
  public String used_algorithm = null;
  public Long timestamp = null;//System.currentTimeMillis();, use at start of algorithm
  public Double time_for_creating_polygon = null; //in Millisecs
  
  //Permute & Reject + IC&BT + Virmani  
  public Integer iterations = null;
 
  //RPA
  //nothing yet
  
  
  //IC&BT
  public Integer count_of_backtracks = null;
  
  //Velocity Virmani
  public Integer virmani_collisions = null;
  public Double radius = null;
  public Double avg_velocity_without_collisions = null;
  
}
