package polygonsSWP.analysis;

import java.io.File;

import polygonsSWP.data.PolygonStatistics;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteQueue;


public class DatabaseWriter
{
  final String databasePath = "database.db";
  final String preparedInsertInto =
      "INSERT INTO Statistic "
          + "(id, used_algorithm, number_of_points, surface_area, circumference, timestamp, time_for_creating_polygon, "
          + "iterations, rejections, count_of_backtracks, radius, avg_velocity_without_collisions, initializeRejections, maximumRejections) "
          + "VALUES(null,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)";// 13
                                                                  // Variables
  SQLiteConnection con;
  SQLiteQueue queue;
  
  public DatabaseWriter() {
    con = new SQLiteConnection(new File(databasePath));
    queue = new SQLiteQueue(new File(databasePath));
    queue.start();
    try {
      con.open();
    }
    catch (SQLiteException e) {
      e.printStackTrace();
    }
  }

  protected void finalize()
    throws Throwable // destruktor
  {
    con.dispose();
    queue.stop(true);
    super.finalize();
  }

  Object lock = 0;

  public void writeToDatabase(PolygonStatistics statistics) {
    synchronized (lock) {
      PolygonStatistics st = statistics;// Make it prettier to use

      // SQLInjections ftw!!!
      String statement =
          String.format(preparedInsertInto, '\'' + st.used_algorithm + '\'',
              st.number_of_points, st.surface_area, st.circumference,
              st.timestamp, st.time_for_creating_polygon, st.iterations,
              st.rejections, st.count_of_backtracks, st.radius,
              st.avg_velocity_without_collisions, st.initializeRejections,
              st.maximumRejections);
      
      System.out.println(statement);//TODO Remove in release
      queue.execute(new MyJob(statement)).complete();
    }

  }
  
  private static class MyJob extends SQLiteJob<Object>
  {
    String statement;
    public MyJob(String statement)
    {
      this.statement = statement;
    }
    @Override
    protected Object job(SQLiteConnection connection) throws SQLiteException {
      return connection.exec(statement);
    }
    
  }
  
  public static void main(String[] args)
    throws SQLiteException {
    DatabaseWriter dw = new DatabaseWriter();
    PolygonStatistics stat = new PolygonStatistics();
    stat.used_algorithm = "kadiralgo";
    stat.initializeRejections = 5;
    stat.number_of_points = 10;
    dw.writeToDatabase(stat);
  }
}
