package polygonsSWP.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.generators.ConvexHullGenerator;
import polygonsSWP.generators.IncrementalConstructionAndBacktracking;
import polygonsSWP.generators.PermuteAndReject;
import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.RandomPolygonAlgorithm;
import polygonsSWP.generators.TwoOptMoves;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;


public class MainFrame extends JFrame {

	private static final long serialVersionUID = 313119639927682997L;
	
	private final int DEFAULTSIZE = 600;
	private String generationMethod = "generate";
	private ArrayList<polygonsSWP.geometry.Point> pointList;
	
	// main components
	private PaintPanel _canvas = new PaintPanel(DEFAULTSIZE);
	private InfoFrame infoframe;
	private MainFrame self;
	
	// menu components
	private JLabel l_polygon_generation, l_shortest_path, l_edge_count;
	private JComboBox cb_polygon_algorithm_chooser;
	
	private PolygonGenerator[] polygon_algorithm_list = {
	    new PermuteAndReject(), 
	    new TwoOptMoves(),
	    new RandomPolygonAlgorithm(),
	    new IncrementalConstructionAndBacktracking(),
	    new ConvexHullGenerator()
	  }; 
	
	private JButton b_set_points,b_generate_polygon,b_calc_shortest_path,b_save_polygon;
	private JSlider sl_edges;
	
	private ButtonGroup bg_shortest_path, polygon_menu;
	private JRadioButton rb_set_points, rb_generate_points, rb_polygonByUser, rb_polygonByPoints, rb_polygonByGenerator;
	
	//panels
	private JPanel p_polygon_generation,p_shortest_path,p_menu,p_polygon_settings,
	p_button_group,p_polygon_menu,p_wrapper,p_generate_save_polygon;
	
	// class variables
	
  public static void main(String[] args) {
    JFrame frame = new MainFrame();
    frame.setTitle("PolygonGen");
    frame.setSize(1000, 650);
    frame.setBackground(Color.white);
    frame.setLocationRelativeTo(null); // center window
    frame.addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){System.exit(0);}});
    frame.setVisible(true);
  }
  
  public MainFrame()
  {
	  self = this;
//	  infoframe = new InfoFrame();  
//	  infoframe.setTitle("Polygon Info");
//	  infoframe.setSize(400,300);
//	  infoframe.setLocationRelativeTo(null);
	 
	  // init labels
	  
	  l_polygon_generation = new JLabel("Polygon Generation");
	  l_shortest_path = new JLabel("Shortest Path");
	  l_edge_count = new JLabel("Edges: 10");
	  
	  // init combobox
	  
	  cb_polygon_algorithm_chooser = new JComboBox(polygon_algorithm_list);
	  
	  // init slider
	  
	  sl_edges = new JSlider(3,100,10);
	  
	  // init buttons
	  
	  b_calc_shortest_path = new JButton("Calculate Shortest Path");
	  b_set_points = new JButton("Set Polygon Points");
	  b_set_points.setEnabled(false);
	  b_generate_polygon = new JButton("Generate Polygon");
	  b_save_polygon = new JButton("Save Polygon");
	  
	  //init RadioButtons and Groups
	  polygon_menu = new ButtonGroup();
	  rb_polygonByGenerator = new JRadioButton("Generate");	
	  rb_polygonByUser = new JRadioButton("Draw");
	  rb_polygonByPoints = new JRadioButton("Set Points");
	  rb_polygonByGenerator.setSelected(true);
	  polygon_menu.add(rb_polygonByGenerator);
	  polygon_menu.add(rb_polygonByUser);
	  polygon_menu.add(rb_polygonByPoints);
	  
	  bg_shortest_path = new ButtonGroup();
	  rb_generate_points = new JRadioButton("Generate Points");
	  rb_generate_points.setSelected(true);
	  rb_set_points = new JRadioButton("Set Points");
	  
	  
	  // building interface
  
	  p_polygon_generation = new JPanel();
	  p_polygon_generation.setLayout(new BorderLayout(5,5));
	  p_polygon_generation.add(l_polygon_generation, BorderLayout.NORTH);
	  
	  p_polygon_menu = new JPanel();
	  //p_polygon_menu.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
	  p_polygon_menu.setLayout(new GridLayout(1,3));
	  p_polygon_menu.add(rb_polygonByGenerator);
	  p_polygon_menu.add(rb_polygonByUser);
	  p_polygon_menu.add(rb_polygonByPoints);
	  p_polygon_settings = new JPanel();
	  //p_polygon_settings.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
	  p_polygon_settings.setLayout(new GridLayout(2,2));
	  p_polygon_settings.add(cb_polygon_algorithm_chooser);
	  p_polygon_settings.add(b_set_points);
	  p_polygon_settings.add(l_edge_count);
	  p_polygon_settings.add(sl_edges);
	  
	  p_wrapper = new JPanel();
	  p_wrapper.setLayout(new GridLayout(2,1));
	  p_wrapper.add(p_polygon_menu);
	  p_wrapper.add(p_polygon_settings);
	  p_polygon_generation.add(p_wrapper, BorderLayout.CENTER);
	  p_generate_save_polygon = new JPanel();
	  p_generate_save_polygon.setLayout(new GridLayout(1,2));
	  p_generate_save_polygon.add(b_generate_polygon);
	  p_generate_save_polygon.add(b_save_polygon);
	  p_polygon_generation.add(p_generate_save_polygon, BorderLayout.SOUTH);
	  
	  p_shortest_path = new JPanel();
	  p_shortest_path.setLayout(new BorderLayout(5,5));
	  p_shortest_path.add(l_shortest_path, BorderLayout.NORTH);
	  bg_shortest_path.add(rb_generate_points);
	  bg_shortest_path.add(rb_set_points);
	  p_button_group = new JPanel();
	  p_button_group.setLayout(new GridLayout(1,2));
	  p_button_group.add(rb_generate_points);
	  p_button_group.add(rb_set_points);
	  p_shortest_path.add(p_button_group);
	  p_shortest_path.add(b_calc_shortest_path, BorderLayout.SOUTH);
	  
	  //p_polygon_generation.setBorder(BorderFactory.createLineBorder(Color.black));
	  //p_shortest_path.setBorder(BorderFactory.createLineBorder(Color.black));
	  
	  p_menu = new JPanel();
	  p_menu.setLayout(new GridLayout(2,1));
	  p_menu.add(p_polygon_generation);
	  p_menu.add(p_shortest_path);
	  
	  // adding all panels to main window
	  
	  setLayout(new BorderLayout(5,5));
	  add(p_menu, BorderLayout.WEST);
	  add(_canvas, BorderLayout.CENTER);
	  
	  
	  // action listener
	  
	  // Slider
	  sl_edges.addMouseMotionListener(new MouseMotionListener() {
		public void mouseDragged(MouseEvent arg0) 
		{
			l_edge_count.setText("Edges: " + sl_edges.getValue());
		}
		public void mouseMoved(MouseEvent arg0) {}		
	  });
	  sl_edges.addMouseListener(new MouseListener(){
		public void mouseClicked(MouseEvent e) {
			l_edge_count.setText("Edges: " + sl_edges.getValue());
		}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
	  });
	  
	  // Combobox
	  
	  cb_polygon_algorithm_chooser.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		  // TODO react on PolygonGenerator accepted parameters
		  // remember deactivateNonSupportedParamComponents
		}

	  });
	  
	  // Buttons
	  
	  b_calc_shortest_path.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			// method stubs
		}
	  });
	  
	  b_generate_polygon.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if(checkParamCombination())
			{
				runGenerator();
				_canvas.repaint();
			}
		}
	  });
	  
	  b_set_points.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			cb_polygon_algorithm_chooser.getSelectedItem();
			JFrame f = new PolygonPointFrame(self);
			f.setTitle("Set Polygon Points");
			f.setSize(400,300);
			f.setLocationRelativeTo(null);
			f.setVisible(true);
		}
	  });
	  
	  b_save_polygon.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent arg0) {
			if(_canvas.getPolygon() != null)
			{
				FileDialog fd = new FileDialog(new Frame(),"Save Polygon To",FileDialog.SAVE);
				fd.setVisible(true);
				if(fd.getDirectory() != null)
					savePolygonToFile(fd.getDirectory() + File.separator + fd.getFile());
			}else{
				JOptionPane.showMessageDialog (null, "There is no polygon to save", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	  });
	  
	  // RadioButtons
	  
	  rb_polygonByGenerator.addMouseListener(new MouseListener() {
		public void mouseReleased(MouseEvent arg0) {}
		public void mousePressed(MouseEvent arg0) {}
		public void mouseExited(MouseEvent arg0) {}
		public void mouseEntered(MouseEvent arg0) {}
		public void mouseClicked(MouseEvent arg0) {
			_canvas.setDrawMode(false);
			generationMethod = "generate";
			b_set_points.setEnabled(false);
			sl_edges.setEnabled(true);
			l_edge_count.setEnabled(true);
			cb_polygon_algorithm_chooser.setEnabled(true);
			b_generate_polygon.setEnabled(true);
			pointList = null;
		}
	  });
	  
	  rb_polygonByPoints.addMouseListener(new MouseListener() {
		public void mouseReleased(MouseEvent arg0) {}
		public void mousePressed(MouseEvent arg0) {}
		public void mouseExited(MouseEvent arg0) {}
		public void mouseEntered(MouseEvent arg0) {}
		public void mouseClicked(MouseEvent arg0) {
			generationMethod = "points";
			_canvas.setDrawMode(false);
			b_set_points.setEnabled(true);
			sl_edges.setEnabled(false);
			l_edge_count.setEnabled(false);
			cb_polygon_algorithm_chooser.setEnabled(true);
			b_generate_polygon.setEnabled(false);
		}
	  });
	  
	  rb_polygonByUser.addMouseListener(new MouseListener() {
		public void mouseReleased(MouseEvent arg0) {}
		public void mousePressed(MouseEvent arg0) {}
		public void mouseExited(MouseEvent arg0) {}
		public void mouseEntered(MouseEvent arg0) {}
		public void mouseClicked(MouseEvent arg0) {
			generationMethod = "draw";
			_canvas.setDrawMode(true);
			b_set_points.setEnabled(false);
			cb_polygon_algorithm_chooser.setEnabled(false);
			sl_edges.setEnabled(false);
			l_edge_count.setEnabled(false);
			b_generate_polygon.setEnabled(false);
			pointList = null;
		}
	  });
	  

  }
  
  // we may dont need this method
  protected boolean checkParamCombination() {
	return true;
  }

public void setPoints(ArrayList<polygonsSWP.geometry.Point> pointList)
  {
	  b_generate_polygon.setEnabled(true);
	  this.pointList = pointList;
	  if (checkParamCombination())
		  runGenerator();
  }
    
  private void runGenerator()
  {
    PolygonGenerator pg = (PolygonGenerator) cb_polygon_algorithm_chooser.getSelectedItem();
    String[] availableParams = pg.getAcceptedParameters();
    Map<String, Object> params = new HashMap<String, Object>();
    
    // TODO: remove this hard code
    params.put("size", DEFAULTSIZE);
    
    if(rb_polygonByGenerator.isSelected())
    {
    	for (int i = 0; i < availableParams.length; i++) {
    		if(availableParams[i].equals("n"))
    		{
    			params.put("n", sl_edges.getValue());
    			Polygon p = pg.generate(params, null);
    		    _canvas.setPolygon(p);
    		}
    	}
    }
    else if(rb_polygonByPoints.isSelected())
    {
    	for (int i = 0; i < availableParams.length; i++) {
    		if(availableParams[i].equals("points"))
    		{
    			if(pointList.size() >= 3)
    			{
    				params.put("points",pointList);
    				Polygon p = pg.generate(params, null);
        		    _canvas.setPolygon(p);
    			}
    		}
    	}
    	// TODO: refactor or move this validation to another place
    	if (params.get("points") == null)
    		JOptionPane.showMessageDialog (null, "The selected polygon generation algorithm do not support the \"Set Points\" option", "Error", JOptionPane.ERROR_MESSAGE);
    	
    }
    else if(rb_polygonByUser.isSelected())
    {
    	// check if user has created a valid polygon
    }
    else
    {
    	JOptionPane.showMessageDialog (null, "A strange error occured :(", "Error", JOptionPane.ERROR_MESSAGE);
    }

  }
  
  private void savePolygonToFile(String filePath)
  {
	  File f = new File(filePath);
	  try {
		  BufferedWriter bw = new BufferedWriter(new FileWriter(f));
		  List<Point> plist = _canvas.getPolygon();
		  for (int i = 0; i < plist.size(); i++) {
			  Point p = plist.get(i);
			  bw.write(p.x + " " + p.y + "\n");
		  }
		  bw.close();
	  } catch (Exception e) {e.printStackTrace();}
  }
  
  private void setPanelComponentsActive(JPanel panel,boolean state)
  {
	  for(Component c : panel.getComponents())
	  {
		  c.setEnabled(state);
	  }
  }
  
  public void generateInfo()
  {
		infoframe.clearText();
		infoframe.setVisible(true);
		infoframe.addLine("Polygon Edges:");
		infoframe.addLine("-------------");
		infoframe.addLine("|    X     |     Y    |");
		infoframe.addLine("-------------");

		infoframe.addLine("TEST");
		
  }
    
}




