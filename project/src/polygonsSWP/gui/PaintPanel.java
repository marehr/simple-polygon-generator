package polygonsSWP.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import polygonsSWP.generators.PermuteAndReject;
import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.TwoOptMoves;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;

class PaintPanel extends JPanel{
    
	private static final long serialVersionUID = 503199267086439422L;
	private static final int SIZE = 600;
	private int n = 10;
	private ArrayList<Point> pointList = new ArrayList<Point>();
	private String generator;
	private final int size;
	private Polygon polygon = null;
	private boolean drawMode = false;
    
    public PaintPanel(int size) {
        this.size = size;
        setSize(new Dimension(this.size, this.size));
        setBackground(Color.white);
        addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent arg0) {
				if(drawMode)
				{
					
				}
			}
			public void mousePressed(MouseEvent arg0) {
				if(drawMode)
				{
					
				}
			}
			public void mouseExited(MouseEvent arg0) {
				if(drawMode)
				{
					
				}
			}
			public void mouseEntered(MouseEvent arg0) {
				if(drawMode)
				{
					
				}
			}
			public void mouseClicked(MouseEvent arg0) {
				if(drawMode)
				{
					
				}
			}
		});
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
    
    public void setN(int n){this.n = n;}
    public void setGenerator(String gen){this.generator = gen;}
    
  public List<Point> getPolygon()
  {
	  if(polygon != null)
		  return polygon.getPoints();
	  else
		  return null;
  }
  
  public void setDrawMode(boolean flag){this.drawMode = flag;}

  public void setPoints(ArrayList<polygonsSWP.geometry.Point> pl) {
    this.pointList = pl;
  };

  public void setPolygon(Polygon p) {
    this.polygon = p;
  }

}
