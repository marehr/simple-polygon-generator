package polygonsSWP.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import polygonsSWP.data.Point;
import polygonsSWP.data.Polygon;
import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.TwoOptMoves;

class PaintPanel extends JPanel{
    
	private static final long serialVersionUID = 503199267086439422L;
	private static final int SIZE = 600;
	private int n = 10;
    
    public PaintPanel() {
        setPreferredSize(new Dimension(SIZE, SIZE));
        setBackground(Color.white);
    }
    
  public void paintComponent(Graphics g) {
    	g.clearRect(0, 0, 1000, 1000);
    	
    	PolygonGenerator polygen = new TwoOptMoves();
    	Map<String,Object> params = new HashMap<String,Object>();
    	params.put("size", SIZE);
    	params.put("n", n);
    	Polygon p = polygen.generate(params,null);
    	List<Point> ps = p.getPoints();
    	
    	int [] xcoords = new int[ps.size()];
    	int [] ycoords = new int[ps.size()];
    	
    	for (int i = 0; i < ps.size(); i++) {
			xcoords[i] = (int) ps.get(i).x;
			ycoords[i] = (int) ps.get(i).y;
		}
    	g.drawPolygon(xcoords,ycoords,ps.size());
    	
    }
    
    public void setN(int n){this.n = n;}

}