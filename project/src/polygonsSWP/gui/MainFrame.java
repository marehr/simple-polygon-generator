package polygonsSWP.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

import polygonsSWP.generators.ConvexHullGenerator;
import polygonsSWP.generators.IncrementalConstructionAndBacktracking;
import polygonsSWP.generators.PermuteAndReject;
import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.RandomPolygonAlgorithm;
import polygonsSWP.generators.SpacePartitioning;
import polygonsSWP.generators.TwoOptMoves;
import polygonsSWP.generators.VelocityVirmani;
import polygonsSWP.gui.generation.PolygonGenerationPanel;
import polygonsSWP.gui.visualisation.PolygonView;

/**
 * MainFrame.
 * 
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 */
public class MainFrame
  extends JFrame
{
  private static final long serialVersionUID = 313119639927682997L;

  // gui components
  private final PolygonGenerationPanel p_generator;
  private final ShortestPathPanel _sp_config;
  private final PolygonView p_polygon_view;

  private PolygonGenerator[] polygon_algorithm_list = { new PermuteAndReject(),
      new TwoOptMoves(), new RandomPolygonAlgorithm(), new SpacePartitioning(),
      new IncrementalConstructionAndBacktracking(), new ConvexHullGenerator()
  , new VelocityVirmani(300,10, 150)};

  public static void main(String[] args) {
    new MainFrame();
  }

  public MainFrame() {
    // init canvas
    p_polygon_view = new PolygonView();
    
    // init generator configuration panel
    p_generator = new PolygonGenerationPanel(polygon_algorithm_list);
    p_generator.setBorder(BorderFactory.createTitledBorder("Polygon Generation"));
    p_generator.addPolygonGenerationPanelListener(p_polygon_view);
    p_generator.addPointGenerationModeListener(p_polygon_view);
    
    // init shortest path configuration panel
    _sp_config = new ShortestPathPanel();
    _sp_config.setBorder(BorderFactory.createTitledBorder("Shortest Path"));
    
    // Create a sidebar on the left.
    JPanel p_sidebarLeft = new JPanel();
    p_sidebarLeft.setLayout(new GridLayout(2, 1));
    p_sidebarLeft.add(p_generator);
    p_sidebarLeft.add(_sp_config);

    // Layout the main window.
    setLayout(new BorderLayout(5, 5));
    add(p_sidebarLeft, BorderLayout.WEST);
    add(p_polygon_view, BorderLayout.CENTER);

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
}
