package polygonsSWP.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import polygonsSWP.data.History;
import polygonsSWP.data.PolygonStatistics;
import polygonsSWP.generators.PolygonGeneratorFactory.Parameters;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.gui.generation.PointGenerationModeListener;
import polygonsSWP.gui.generation.PolygonGenerationPanelListener;
import polygonsSWP.gui.generation.ShortestPathGenerationListener;
import polygonsSWP.shortestpath.ShortestPathGenerator;

/**
 * Panel which controls the shortest path calculation.
 * 
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 */
class ShortestPathPanel extends JPanel implements PolygonGenerationPanelListener, GUIModeListener{
  private static final long serialVersionUID = 1L;
  private final List<PointGenerationModeListener> observers;
  private final List<ShortestPathGenerationListener> sp_observers;
  
  /* Controls. */
  private final JButton b_calc_shortest_path;
  private final JRadioButton rb_set_points, rb_generate_points;
  private Polygon currentPolygon;
  private List<Point> pointList = null;
  
  ShortestPathPanel() {
	observers = new LinkedList<PointGenerationModeListener>();
	sp_observers = new LinkedList<ShortestPathGenerationListener>();
    b_calc_shortest_path = new JButton("Calculate Shortest Path");

    ButtonGroup bg_shortest_path = new ButtonGroup();
    rb_generate_points = new JRadioButton("Generate Points");
    rb_generate_points.setSelected(true);
    rb_set_points = new JRadioButton("Set Points");
    bg_shortest_path.add(rb_generate_points);
    bg_shortest_path.add(rb_set_points);
    
    
    rb_set_points.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent arg0) {
  			pointList = new LinkedList<Point>();
  			emitPointGenerationModeSwitched(false,pointList);			
  		}
  	});
    
    rb_generate_points.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent arg0) {
  			pointList = null;
  			emitPointGenerationModeSwitched(true,null);			
  		}
  	});
    
    //action listener
    b_calc_shortest_path.addActionListener(new ActionListener() {
  		public void actionPerformed(ActionEvent arg0) {
  			if(currentPolygon != null)
  			{
  				if(rb_generate_points.isSelected())
  				{
  					Point startPoint = currentPolygon.createRandomPoint();
  					Point endPoint = currentPolygon.createRandomPoint();
            History history = new History(600); // TODO
  					List<Point> path = ShortestPathGenerator.generateShortestPath((OrderedListPolygon) currentPolygon, startPoint, endPoint, history);
  					for(Point p:path)
  					  System.out.println(p.x + " : " + p.y);
  				}
  				else
  				{
  					if(pointList != null && pointList.size() == 2)
  					{
  						final Point startPoint = pointList.get(0);
  						final Point endPoint = pointList.get(1);
  						final History history = new History(600); // TODO
  						emitSPGenerationStarted(history);
  						Thread t = new Thread(new Runnable() {

                @Override
                public void run() {
                  List<Point> path = ShortestPathGenerator.generateShortestPath((OrderedListPolygon) currentPolygon, startPoint, endPoint, history);
                  emitSPGenerationFinished();
                  for(Point p:path)
                    System.out.println(p.x + " : " + p.y);    
                }
  						  
  						});
  						t.start();
										
  					}
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
  
  public void addShortestPathGenerationListener(ShortestPathGenerationListener listener) {
	  sp_observers.add(listener);
  }
  
  public void addPointGenerationModeListener(PointGenerationModeListener listener) {
	  observers.add(listener);
  }
  
  protected void emitPointGenerationModeSwitched(boolean randomPoints, List<Point> points) {
	    for (PointGenerationModeListener pgml : observers)
	      pgml.onPointGenerationModeSwitched(randomPoints, points);
  }
  
  protected void emitSPGenerationStarted(History history) {
    for(ShortestPathGenerationListener spgl : sp_observers)
      spgl.onSPStarted(history);
  }
  
  protected void emitSPGenerationFinished() {
	  for(ShortestPathGenerationListener spgl : sp_observers)
		  spgl.onSPFinished();
  }

  // Listener methods

  @Override
  public void onPolygonGenerationStarted(PolygonStatistics stats,
      History steps,  Map<Parameters, Object> params) {
    b_calc_shortest_path.setEnabled(false);
  }

  @Override
  public void onPolygonGenerationCancelled() {
    b_calc_shortest_path.setEnabled(true);
  }

  @Override
  public void onPolygonGenerated(Polygon newPolygon, PolygonStatistics stats,
      History history, Map<Parameters, Object> params) {

    b_calc_shortest_path.setEnabled(true);
    currentPolygon = newPolygon;
  }

  @Override
  public void onGUIModeChanged(boolean generatorMode) {
    if(!generatorMode) {
      if(rb_generate_points.isSelected()) {
        emitPointGenerationModeSwitched(true, null);
      } else {
        assert(pointList != null);
        emitPointGenerationModeSwitched(false, pointList);
      }
    }
  }

}
