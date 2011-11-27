package polygonsSWP.generators;

import java.util.Map;
import java.util.Random;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.RegularExpression;

import polygonsSWP.data.PolygonHistory;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;

public class VelocityVirmani implements PolygonGenerator {

	private Random rand;
	
	public VelocityVirmani()
	{
		rand = new Random();
	}
	
	//Die mit ausrufezeichen sind "must-have"
	@Override
	public String[] getAcceptedParameters() {
		String[] params = {"!radius", "n", "maxVelocity", "size", "runs"};
		return params;
	}
	
	
	
	@Override
	public Polygon generate(Map<String, Object> params, PolygonHistory steps) {
	    if(!params.containsKey("!radius") || !params.containsKey("n") || !params.containsKey("size") || !params.containsKey("runs"))
	    {
	    	throw new RuntimeException("Unsufficient Paramters");
	    }	    
	    
	    long radius = (long) params.get("!radius");
	    int n = (int) params.get("n");
	    int unitAmount = (int) params.get("!unitAmount");
	    long bound = (long) params.get("size");
	    int maxVelo = (int) (params.containsKey("maxVelocity") ? params.get("maxVelocity") : Integer.MAX_VALUE - 1) ;
	    if(radius * 2 > bound)
	    {
	    	throw new RuntimeException("Radius must be smaller than the Bounds allow. (Pre: Radius * 2 < bound)");
	    }
	    
		OrderedListPolygon poly = regularPolygon(n, radius, bound);
		
		int velox, veloy;
		boolean loop;
		while(unitAmount > 0) //Macht die Iterationen an zufälligen Bewegungen
		{
			for(int i = 0; i < n; i++) // Geht durch alle Punkte
			{
				loop = true;
				do //Überprüft ob die Punkte dann noch in den Boundaries(size) sind + ob sie immernoch simple sind
				{
					velox = rand.nextBoolean()? rand.nextInt(maxVelo) : - rand.nextInt(maxVelo); //Kann auch negative Geschwindigkeiten annehmen
					veloy = rand.nextBoolean()? rand.nextInt(maxVelo) : - rand.nextInt(maxVelo);
					
					if(poly.getPoint(i).x + velox > bound || poly.getPoint(i).y + veloy > bound ||     // Checkt ob es noch in den Boundaries(size) ist
							poly.getPoint(i).x + velox < 0 || poly.getPoint(i).y + veloy < 0)          //oder kleiner null
						continue;
					
					poly.getPoint(i).x += velox;//Geschwindigkeit auf Punkt addieren
					poly.getPoint(i).y += veloy;
					
					if(poly.isSimple())
						loop = false;
					else//Wieder Rückgängig machen da nicht simple
					{
						poly.getPoint(i).x += velox;
						poly.getPoint(i).y += veloy;
					}
						
				}while(loop);
			}
		}
		
		
		return null;
	}
	
	
	private OrderedListPolygon regularPolygon(int n, long radius, long bound)
	{
		double winkel = (Math.PI * 2) / n;
		long x = radius;
		long y = 0;
		long x1,y1;
		double tmpWinkel;
		OrderedListPolygon poly = new OrderedListPolygon();
		for(int i = 0; i < n; i++)
		{
			tmpWinkel = winkel*i;
			x1 = (long) (x * Math.cos(tmpWinkel) - y * Math.sin(tmpWinkel)) + (bound /2); //Verschiebung damit der Nullpunkt oben links landet
			y1 = - (long) (x * Math.sin(tmpWinkel) + y * Math.cos(tmpWinkel)) + (bound /2);/* y muss negativ sein weil vorher die y achse andersherum gerichtet war
			 																				je größer y wurde, desto mehr ging es nach "oben", wenn nullcord oben links liegt es andersherum*/
			poly.addPoint(new Point(x1,y1));
		}
		return poly;
	}
	
	
	public static void main(String[] args)
	{
		new VelocityVirmani().regularPolygon(4, 100, 200);
	}
	

}
