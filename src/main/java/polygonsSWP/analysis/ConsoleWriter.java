package polygonsSWP.analysis;


import polygonsSWP.data.PolygonStatistics;
import polygonsSWP.geometry.Polygon;

public class ConsoleWriter extends OutputInterface
{
	private Object _lock = new Object();
	private boolean writeStatistics;
	
	public ConsoleWriter(boolean writeHeader, boolean writeStatistics)
	{
		if(writeHeader)
			System.out.println(getCsvHeader());
		this.writeStatistics = writeStatistics;
	}
	
	@Override
	public void writeOut(Polygon polygon, PolygonStatistics statistics) {
		synchronized (_lock) {
			StringBuilder sb = new StringBuilder();
			String s = polygon.toString().replaceAll("\n", ":");
			//write polygon
			sb.append(s.substring(0, s.length()-1));
			//write Header
			if(writeStatistics)
			{
				sb.append(getStatisticsCsvData(statistics));
			}
			else
			{
				sb.append(getEmptyStatisticsData());//add 13 ';' so the csv is complete
			}
			System.out.println(sb.toString());
		}
	}
	

}
