package polygonsSWP.generators.heuristics;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import polygonsSWP.data.PolygonHistory;
import polygonsSWP.generators.IllegalParameterizationException;
import polygonsSWP.generators.PolygonGeneratorFactory;
import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.PolygonGeneratorFactory.Parameters;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;

public class VelocityVirmaniFactory implements PolygonGeneratorFactory {

  @Override
  public boolean acceptsUserSuppliedPoints() {
    return false;
  }

  @Override
  public List<Parameters> getAdditionalParameters() {
    List<Parameters> addparams = new LinkedList<Parameters>();
    addparams.add(Parameters.radius);
    addparams.add(Parameters.runs);
    addparams.add(Parameters.velocity);
    return addparams;
  }
  
  @Override
  public String toString()
  {
    return "Velocity Virmani";
  }
  
  @Override
  public PolygonGenerator createInstance(Map<Parameters, Object> params,
      PolygonHistory steps) throws IllegalParameterizationException {
      
    Long radius = (Long) params.get(Parameters.radius);
    if(radius == null)
      throw new IllegalParameterizationException(
          "Radius not set.", Parameters.radius);
    
    Integer n = (Integer) params.get(Parameters.n);
    if(n == null)
      throw new IllegalParameterizationException(
          "Number of points not set.", Parameters.n);
    
    Integer runs = (Integer) params.get(Parameters.runs);
    if(runs == null)
      throw new IllegalParameterizationException(
          "Number of iterations not set.", Parameters.runs);
    
    Integer bound = (Integer)params.get(Parameters.size);
    if(bound == null)
      throw new IllegalParameterizationException(
          "Size of bounding box not set.", Parameters.size);
    
    Integer maxVelo = (Integer) params.get(Parameters.velocity);
    if(maxVelo == null)
      throw new IllegalParameterizationException(
          "Maximum velocity not set.", Parameters.velocity);
    
    if (radius * 2 > bound) {
      throw new IllegalParameterizationException(
          "Radius must be smaller than the bounds allow (Pre: Radius * 2 < bound).",
          Parameters.radius);
    }    
    
    return new VelocityVirmani(n, radius, runs, bound, maxVelo);
  }
  
	private static class VelocityVirmani implements PolygonGenerator {
	
	  private Random rand;
    private int n;
    private long radius;
    private int runs;
    private int maxVelo;
    private int bound;
	  
  	VelocityVirmani(int n, long radius, int runs, int bound, int maxVelo)
  	{
      this.rand = new Random();
  	  this.n = n;
      this.radius = radius;
  	  this.runs = runs;
  	  this.bound = bound;
  	  this.maxVelo = maxVelo;
  	}
  
  	@Override
  	public Polygon generate() {
  	     
  		OrderedListPolygon poly = regularPolygon(n, radius, bound);
  
  		int velox, veloy;
  		while (runs > 0) // Macht die Iterationen an zuf�lligen Bewegungen
  		{
  			for (int i = 0; i < n; i++) // Geht durch alle Punkte
  			{
  				  // �berpr�ft ob die Punkte dann noch in den Boundaries(size)
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
  
  					if (!poly.isSimple())// Wieder R�ckg�ngig machen da nicht simple
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
  								 * andersherum gerichtet war je gr��er y wurde,
  								 * desto mehr ging es nach "oben", wenn nullcord
  								 * oben links liegt es andersherum
  								 */
  			poly.addPoint(new Point(x1, y1));
  		}
  		return poly;
  	}

    @Override
    public void stop() {
      // TODO Auto-generated method stub
    }
	}
}
