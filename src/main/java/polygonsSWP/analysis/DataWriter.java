package polygonsSWP.analysis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import polygonsSWP.data.PolygonStatistics;
import polygonsSWP.geometry.Polygon;


public class DataWriter extends OutputInterface
{
	PrintWriter file;
	private boolean writeStatistics;
	public DataWriter(String outputfile, boolean writeHeader, boolean writeStatistics) throws IOException
	{
		this.writeStatistics = writeStatistics;
		file = new PrintWriter(new BufferedWriter(new FileWriter(outputfile)));
		if(writeHeader)
		{
			file.println(getCsvHeader());
			file.flush();
		}
		
	}
	@Override
	public void writeOut(Polygon polygon, PolygonStatistics statistics) {
		synchronized (file) {
				StringBuilder sb = new StringBuilder();
			//write polygon
			String s = polygon.toString().replaceAll("\n", ":");
			sb.append(s.substring(0, s.length()-1));
			//write Header
			if(writeStatistics)
			{
				sb.append(getStatisticsCsvData(statistics));
			}
			else
			{
				sb.append(getEmptyStatisticsData());
			}
			file.write(sb.toString());
			file.write('\n');
			file.flush();
		}
	}

}
