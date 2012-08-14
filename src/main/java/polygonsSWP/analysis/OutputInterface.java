package polygonsSWP.analysis;

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
public abstract class OutputInterface {
	
	protected boolean headerOutputted = false;
	protected boolean writeHeader;
	
	public abstract void writeOut(Polygon polygon, PolygonStatistics statistics);
	
	
	protected String getCsvHeader()
	{
		return "polygon;used_algorithm;number_of_points;surface_area;circumference;timestamp;" +
				"time_for_creating_polygon;iterations;rejections;count_of_backtracks;radius;" +
				"avg_velocity_without_collisions;initializeRejections;maximumRejections";
	}
	
	protected String getEmptyStatisticsData()
	{
		return ";;;;;;;;;;;;;";
	}
	
	protected String getStatisticsCsvData(PolygonStatistics statistics)
	{
		StringBuilder sb = new StringBuilder();
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
		return sb.toString();
	}
}
