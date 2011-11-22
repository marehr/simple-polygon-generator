package polygonsSWP.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.List;

import javax.swing.JPanel;

import polygonsSWP.data.Point;
import polygonsSWP.data.Polygon;


class PaintPanel extends JPanel{
    
	private static final long serialVersionUID = 503199267086439422L;
	private final int size;
  private Polygon polygon = null;
    
    public PaintPanel(int size) {
        this.size = size;
        setSize(new Dimension(this.size, this.size));
        setBackground(Color.white);
    }
    
  public void paintComponent(Graphics g) {
    	g.clearRect(0, 0, 1000, 1000);
    	
    	if(polygon != null)
    	{
    	  List<Point> p = polygon.getPoints();
    	  
    	  int[] xcoords = new int[p.size()];
    	  int[] ycoords = new int[p.size()];
    	  for(int i = 0; i < p.size(); i++) {
    	    xcoords[i] = (int) p.get(i).x;
    	    ycoords[i] = (int) p.get(i).y;
    	  }
    	  g.drawPolygon(xcoords, ycoords, p.size());
    	}
    }
    
  public void setPolygon(Polygon p) {
    this.polygon = p;
  }
}
