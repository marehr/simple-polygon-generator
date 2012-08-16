package polygonsSWP.analysis;

import java.io.File;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteQueue;
import com.almworks.sqlite4java.SQLiteStatement;

import polygonsSWP.data.PolygonStatistics;
import polygonsSWP.geometry.Polygon;


public class DatabaseLog
  implements PolygonLog
{
  private SQLiteConnection con;
  private SQLiteQueue queue;
  
  private final String sql =
      "INSERT INTO Statistic "
          + "(id, used_algorithm, number_of_points, surface_area, circumference, timestamp, time_for_creating_polygon, "
          + "iterations, rejections, count_of_backtracks, radius, avg_velocity_without_collisions, initializeRejections, maximumRejections) "
          + "VALUES(null,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)";
  
  
  public DatabaseLog(String database) throws Exception {
    File db = new File(database);
    boolean create_table = !db.exists();
    
    con = new SQLiteConnection(db);
    con.open();
    
    if(create_table) {
      con.exec("CREATE TABLE Statistic (" +
          "id integer primary key autoincrement," +
          "used_algorithm integer," +
          "number_of_points integer," +
          "surface_area decimal," +
          "circumference decimal," +
          "timestamp integer," +
          "time_for_creating_polygon," +
          "iterations integer," +
          "rejections integer," +
          "count_of_backtracks integer," +
          "radius decimal," +
          "avg_velocity_without_collisions decimal," +
          "initializeRejections integer," +
          "maximumRejections integer" +
          ")");
    }
    
    queue = new SQLiteQueue(db);
    queue.start();
  }
  
  @Override
  public void writeOut(Polygon polygon, final PolygonStatistics stats) {
    synchronized(queue) {
      queue.execute(new SQLiteJob<Object>() {
        @Override
        protected Object job(SQLiteConnection con) throws Throwable {
          SQLiteStatement stmt = con.prepare(sql);
          stmt.bind(0, stats.used_algorithm);
          stmt.bind(1, stats.number_of_points);
          stmt.bind(2, stats.surface_area);
          stmt.bind(3, stats.circumference);
          stmt.bind(4, stats.timestamp);
          stmt.bind(5, stats.time_for_creating_polygon);
          stmt.bind(6, stats.iterations);
          stmt.bind(7, stats.rejections);
          stmt.bind(8, stats.count_of_backtracks);
          stmt.bind(9, stats.radius);
          stmt.bind(10, stats.avg_velocity_without_collisions);
          stmt.bind(11, stats.initializeRejections);
          stmt.bind(12, stats.maximumRejections);
          stmt.stepThrough();          
          return null;
        }
      });
    }
  }

  @Override
  public void close() {
    try {
      queue.stop(true).join();
    }
    catch (InterruptedException e) {
      // Ignore.
    }
    con.dispose();    
  }
}
