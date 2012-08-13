package polygonsSWP.data;

/**
 * PolygonStatistics.
 * 
 * (c) 2011-2012
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Kadir Tugan <kadir.tugan@gmail.com>
 */
public class PolygonStatistics
{
  //All
  public Integer number_of_points = null;
  public Double surface_area = null;
  public Double circumference = null; //umfang
  public String used_algorithm = null;
  public Long timestamp = null;//System.currentTimeMillis();, use at start of algorithm
  public Long time_for_creating_polygon = null; //in Millisecs
  
  //Permute & Reject + IC&BT + Virmani + SteadyGrowth
  public Integer iterations = null;
 
  //Permute & Reject + Virmani + SteadyGrowth
  public Integer rejections = null;
  
  //RPA
  //nothing yet
  
  
  //IC&BT
  public Integer count_of_backtracks = null;
  
  //Velocity Virmani
  public Double radius = null;
  public Double avg_velocity_without_collisions = null;
  
  // SteadyGrowth
  public Integer initializeRejections = null;
  public Integer maximumRejections = null;
}
