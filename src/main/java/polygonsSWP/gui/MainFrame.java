package polygonsSWP.gui;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import polygonsSWP.generators.PolygonGeneratorFactory;
import polygonsSWP.gui.generation.PolygonGenerationPanel;
import polygonsSWP.gui.visualisation.PolygonView;

/**
 * MainFrame.
 * 
 * (c) 2011-2012
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Kadir Tugan <kadir.tugan@gmail.com>
 */
public class MainFrame
  extends JFrame
{
  private static final long serialVersionUID = 313119639927682997L;

  // GUI components.
  private final PolygonGenerationPanel gui_generator;
  private final ShortestPathPanel gui_shortest_path;
  private final PolygonView gui_polygon_view;
  private JTabbedPane tabpane;
  private boolean inGenerationMode = true;
  private final List<GUIModeListener> observers;

  private PolygonGeneratorFactory[] polygon_algorithm_list;

  public MainFrame(PolygonGeneratorFactory[] factories) {
    polygon_algorithm_list = factories;
    
    // init canvas
    gui_polygon_view = new PolygonView();
    
    // init shortest path configuration panel
    gui_shortest_path = new ShortestPathPanel();
    gui_shortest_path.setBorder(BorderFactory.createTitledBorder("Shortest Path"));
    gui_shortest_path.addPointGenerationModeListener(gui_polygon_view);
    gui_shortest_path.addShortestPathGenerationListener(gui_polygon_view);
    
    // init generator configuration panel
    gui_generator = new PolygonGenerationPanel(polygon_algorithm_list);
    gui_generator.setBorder(BorderFactory.createTitledBorder("Polygon Generation"));
    gui_generator.addPolygonGenerationPanelListener(gui_polygon_view);
    gui_generator.addPolygonGenerationPanelListener(gui_shortest_path);
    gui_generator.addPointGenerationModeListener(gui_polygon_view);
    gui_generator.addHistorySceneModeListener(gui_polygon_view);
  
    observers = new LinkedList<GUIModeListener>();
    observers.add(gui_polygon_view);
    observers.add(gui_shortest_path);
    observers.add(gui_generator);
    
    tabpane = new JTabbedPane();
    tabpane.add("Polygon Generation",gui_generator);
    tabpane.add("Shortest Path Generation",gui_shortest_path);
    
    tabpane.addChangeListener(new ChangeListener() {
    	public void stateChanged(ChangeEvent arg0) {
    		inGenerationMode = !inGenerationMode;
    		emitGUIModeChanged(inGenerationMode);
    	}
    });
    
    // Layout the main window.
    setLayout(new BorderLayout(5, 5));
    add(tabpane, BorderLayout.WEST);
    add(gui_polygon_view, BorderLayout.CENTER);

    setTitle("PolygonGen");
    setSize(1000, 650);
    setLocationRelativeTo(null); // center window
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    setVisible(true);
  }
  
  private void emitGUIModeChanged(boolean generatorMode)
  {
	  for(GUIModeListener l : observers)
		  l.onGUIModeChanged(generatorMode);
  }  
}
