package polygonsSWP.analysis;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import polygonsSWP.data.PolygonStatistics;
import polygonsSWP.geometry.Polygon;

/**
 * (c) 2011-2012
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Kadir Tugan <kadir.tugan@gmail.com>
 */
public class CsvLog implements PolygonLog
{
  private boolean writeStatistics;
  private PrintWriter file;
  
	public CsvLog(OutputStream out, boolean writeHeader, boolean writeStatistics) throws IOException
	{
		this.writeStatistics = writeStatistics;
		file = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out)));
		
		if(writeHeader)
		{
		  file.print("polygon");
		  if(writeStatistics)
		    file.print(";used_algorithm;number_of_points;surface_area;circumference;timestamp;" +
		        "time_for_creating_polygon;iterations;rejections;count_of_backtracks;radius;" +
		        "avg_velocity_without_collisions;initializeRejections;maximumRejections");
		  file.println();
		}
	}
	
	@Override
	public void writeOut(Polygon polygon, PolygonStatistics statistics) {
		synchronized (file) {
		  StringBuilder sb = new StringBuilder();
			
		  // Write polygon.
			String s = polygon.toString().replaceAll("\n", ":");
			sb.append(s.substring(0, s.length()-1));
			
			// Write statistics.
			if(writeStatistics) {
		    sb.append(';');
		    sb.append(statistics.used_algorithm);
		    sb.append(';');
		    sb.append(statistics.number_of_points);
		    sb.append(';');
		    sb.append(statistics.surface_area);
		    sb.append(';');
		    sb.append(statistics.circumference);
		    sb.append(';');
		    sb.append(statistics.timestamp);
		    sb.append(';');
		    sb.append(statistics.time_for_creating_polygon);
		    sb.append(';');
		    sb.append(statistics.iterations);
		    sb.append(';');
		    sb.append(statistics.rejections);
		    sb.append(';');
		    sb.append(statistics.count_of_backtracks);
		    sb.append(';');
		    sb.append(statistics.radius);
		    sb.append(';');
		    sb.append(statistics.avg_velocity_without_collisions);
		    sb.append(';');
		    sb.append(statistics.initializeRejections);
		    sb.append(';');
		    sb.append(statistics.maximumRejections);
			}
			
			file.write(sb.toString());
			file.write('\n');
			file.flush();
		}
	}
}
