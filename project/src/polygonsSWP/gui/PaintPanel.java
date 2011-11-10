package polygonsSWP.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import polygonsSWP.data.*;

import javax.swing.*;

import polygonsSWP.generators.PermuteAndReject;

class PaintPanel extends JPanel{
    
	private static final long serialVersionUID = 503199267086439422L;
	private static final int SIZE = 600;
	private int n = 10;
    
    public PaintPanel() {
        setPreferredSize(new Dimension(SIZE, SIZE));
        setBackground(Color.white);
    }
    
    @SuppressWarnings("unchecked")
	public void paintComponent(Graphics g) {
    	g.clearRect(0, 0, 1000, 1000);
    	
    	PermuteAndReject polygen = new PermuteAndReject();
    	Map<String,Object> params = new HashMap<String,Object>();
    	params.put("size", SIZE);
    	params.put("n", n);
    	Polygon p = polygen.run(params,null);
    	
    	int [] xcoords = new int[p.getPoints().size()];
    	int [] ycoords = new int[p.getPoints().size()];
    	
    	for (int i = 0; i < p.getPoints().size(); i++) {
			xcoords[i] = p.getPoints().get(i).x;
			ycoords[i] = p.getPoints().get(i).y;
		}
    	g.drawPolygon(xcoords,ycoords,p.getPoints().size());
    	
    }
    
    public void setN(int n){this.n = n;}

}