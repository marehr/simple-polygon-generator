package polygonsSWP.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import polygonsSWP.data.PolygonHistory;
import polygonsSWP.data.PolygonStatistics;
import polygonsSWP.data.ShortestPath;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.gui.generation.PolygonGenerationPanelListener;

/**
 * Panel which controls the shortest path calculation.
 * 
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 */
class ShortestPathPanel extends JPanel implements PolygonGenerationPanelListener{
  private static final long serialVersionUID = 1L;
  
  /* Controls. */
  private final JButton b_calc_shortest_path;
  private final JRadioButton rb_set_points, rb_generate_points;
  private Polygon currentPolygon = null;
  
  ShortestPathPanel() {
    b_calc_shortest_path = new JButton("Calculate Shortest Path");

    ButtonGroup bg_shortest_path = new ButtonGroup();
    rb_generate_points = new JRadioButton("Generate Points");
    rb_generate_points.setSelected(true);
    rb_set_points = new JRadioButton("Set Points");
    bg_shortest_path.add(rb_generate_points);
    bg_shortest_path.add(rb_set_points);
    
    
    //action listener
    b_calc_shortest_path.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
			if(currentPolygon != null)
			{
				if(rb_generate_points.isSelected())
				{
					Point startPoint = currentPolygon.createRandomPoint();
					Point endPoint = currentPolygon.createRandomPoint();
					ShortestPath sp = new ShortestPath(currentPolygon, startPoint, endPoint);
					ArrayList<Point> path = new ArrayList<Point>(sp.generateShortestPath());
					for(Point p:path)
					  System.out.println(p.x + " : " + p.y);
				}
				else
				{
					
				}				
			}
		}
	});
    
    // Build the interface.
    layoutControls();
    b_calc_shortest_path.setEnabled(false);
  }

  final private void layoutControls() {
    setLayout(new GridBagLayout());

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    add(rb_generate_points, gbc);

    gbc.gridx = 0;
    gbc.gridy = 1;
    add(rb_set_points, gbc);

    gbc.gridx = 0;
    gbc.gridy = 2;
    add(b_calc_shortest_path, gbc);
  }

  // Listener methods
  public void onPolygonGenerationStarted() {b_calc_shortest_path.setEnabled(false);}
  public void onPolygonGenerationCancelled() {b_calc_shortest_path.setEnabled(true);}
  public void onPolygonGenerated(Polygon newPolygon, PolygonStatistics stats, PolygonHistory history) {
	  b_calc_shortest_path.setEnabled(true);
	  currentPolygon = newPolygon;
  }
  
}
