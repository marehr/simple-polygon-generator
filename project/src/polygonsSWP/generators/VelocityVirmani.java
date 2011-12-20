package polygonsSWP.generators;

import java.util.Map;
import java.util.Random;

import polygonsSWP.data.PolygonHistory;
import polygonsSWP.generators.PolygonGenerator.Parameters;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;

public class VelocityVirmani implements PolygonGenerator {

	private Random rand;
	private long radius;
	private int runs;
	private int velocity;
	
	public VelocityVirmani() {
		rand = new Random();
	}
	
	public VelocityVirmani(long radius, int runs, int velocity)
	{
	  this();
	  this.radius = radius;
	  this.runs = runs;
	  this.velocity = velocity;
	}

	
	private Parameters[][] params = new Parameters[][]
			{
				new Parameters[] {Parameters.n, Parameters.radius, Parameters.velocity, Parameters.size, Parameters.runs}
			};
	
	// Die mit ausrufezeichen sind "must-have"
	@Override
	public Parameters[][] getAcceptedParameters() {
		return params;
	}

	@Override
	public Polygon generate(Map<Parameters, Object> params, PolygonHistory steps) {
	  
	  //Weil die GUI noch nicht runs oder radius provided
	  if(radius != 0 && runs != 0 && velocity !=0)
	  {
	    params.put(Parameters.radius, radius);
	    params.put(Parameters.runs, runs);
	    params.put(Parameters.velocity, velocity);
	  }//bis hierher
	  
	  
		if (!params.containsKey(Parameters.radius) || !params.containsKey(Parameters.n)
				|| !params.containsKey(Parameters.size) || !params.containsKey(Parameters.runs)) {
			throw new RuntimeException("Unsufficient Paramters");
		}

		long radius = (Long) params.get(Parameters.radius);
		int n = (Integer) params.get(Parameters.n);
		int runs = (Integer) params.get(Parameters.runs);
		long bound = (Integer)params.get(Parameters.size);
		int maxVelo = (Integer) params.get(Parameters.velocity);
		
		if (radius * 2 > bound) {
			throw new RuntimeException(
					"Radius must be smaller than the Bounds allow. (Pre: Radius * 2 < bound)");
		}

		OrderedListPolygon poly = regularPolygon(n, radius, bound);

		int velox, veloy;
		while (runs > 0) // Macht die Iterationen an zufälligen Bewegungen
		{
			for (int i = 0; i < n; i++) // Geht durch alle Punkte
			{
				  // Überprüft ob die Punkte dann noch in den Boundaries(size)
					// sind + ob sie immernoch simple sind
				  
					velox = rand.nextBoolean() ? rand.nextInt(maxVelo) : - rand.nextInt(maxVelo); // Kann auch negative Geschwindigkeiten annehmen
					veloy = rand.nextBoolean() ? rand.nextInt(maxVelo) : - rand.nextInt(maxVelo);

					if (poly.getPoint(i).x + velox > bound
							|| poly.getPoint(i).y + veloy > bound // Checkt ob es noch in den Boundaries(size) ist
							|| poly.getPoint(i).x + velox < 0
							|| poly.getPoint(i).y + veloy < 0) // oder kleiner
																// null
						continue;

					poly.getPoint(i).x += velox;// Geschwindigkeit auf Punkt
												// addieren
					poly.getPoint(i).y += veloy;

					if (!poly.isSimple())// Wieder Rückgängig machen da nicht simple
					{
						poly.getPoint(i).x -= velox;
						poly.getPoint(i).y -= veloy;
					}
					
			}
			runs--;
		}

		boolean b = poly.isSimple();
		return poly;
	}

	private OrderedListPolygon regularPolygon(int n, long radius, long bound) {
		double winkel = (Math.PI * 2) / n;
		long x = radius;
		long y = 0;
		long x1, y1;
		double tmpWinkel;
		OrderedListPolygon poly = new OrderedListPolygon();
		for (int i = 0; i < n; i++) {
			tmpWinkel = winkel * i;
			x1 = (long) (x * Math.cos(tmpWinkel) - y * Math.sin(tmpWinkel))
					+ (bound / 2); // Verschiebung damit der Nullpunkt oben
									// links landet
			y1 = -(long) (x * Math.sin(tmpWinkel) + y * Math.cos(tmpWinkel))
					+ (bound / 2);/*
								 * y muss negativ sein weil vorher die y achse
								 * andersherum gerichtet war je größer y wurde,
								 * desto mehr ging es nach "oben", wenn nullcord
								 * oben links liegt es andersherum
								 */
			poly.addPoint(new Point(x1, y1));
		}
		return poly;
	}
	
	public String toString()
	{
		return "Velocity Virmani";
	}

	// {"!radius", "n", "maxVelocity", "size", "runs"};
	public static void main(String[] args) {
		// new VelocityVirmani().regularPolygon(4, 100, 200);
		Map<Parameters, Object> params = new java.util.HashMap<Parameters, Object>();
		params.put(Parameters.n, 5);
		params.put(Parameters.velocity, 5);
		params.put(Parameters.size, (long) 100);
		params.put(Parameters.runs, 10);
		params.put(Parameters.radius, (long) 20);
		new VelocityVirmani().generate(params, null);
	}

}
