package polygonsSWP.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import polygonsSWP.data.Point;

import javax.swing.*;
import javax.swing.border.BevelBorder;


public class MainFrame extends JFrame {

	private static final long serialVersionUID = 313119639927682997L;
	
	private final int DEFFAULTSIZE = 600;
	private String generationMethod = "generate";
	private ArrayList<polygonsSWP.data.Point> pointList;
	
	// main components
	private PaintPanel _canvas = new PaintPanel();
	private InfoFrame infoframe;
	private MainFrame self;
	
	// menu components
	private JLabel l_polygon_generation, l_shortest_path, l_edge_count;
	private JComboBox cb_polygon_algorithm_chooser;
	
	private String [] polygon_algorihtm_list = {"Permute and Reject","Two Opt Moves","algo3","algo4","algo5"};
	
	private JButton b_set_points,b_generate_polygon,b_calc_shortest_path;
	private JSlider sl_edges;
	
	private ButtonGroup bg_shortest_path, polygon_menu;
	private JRadioButton rb_set_points, rb_generate_points, rb_polygonByUser, rb_polygonByPoints, rb_polygonByGenerator;
	
	//panels
	private JPanel p_polygon_generation,p_shortest_path,p_menu,p_polygon_settings,
	p_button_group,p_polygon_menu,p_wrapper;
	
	// class variables
	
	private boolean pointsSet = false;
	
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
	  
	  cb_polygon_algorithm_chooser = new JComboBox(polygon_algorihtm_list); // init combobox with string array
	  
	  // init slider
	  
	  sl_edges = new JSlider(1,100,10);
	  
	  // init buttons
	  
	  b_calc_shortest_path = new JButton("Calculate Shortest Path");
	  b_set_points = new JButton("Set Polygon Points");
	  b_set_points.setEnabled(false);
	  b_generate_polygon = new JButton("Generate Polygon");
	  
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
	  p_polygon_generation.add(b_generate_polygon, BorderLayout.SOUTH);
	  
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
			if (cb_polygon_algorithm_chooser.getSelectedItem().equals("Permute and Reject"))
			{
				
			}
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
				setOptions();
				_canvas.repaint();
			}
		}
	  });
	  
	  b_set_points.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			JFrame f = new PolygonPointFrame(self);
			f.setTitle("Set Polygon Points");
			f.setSize(400,300);
			f.setLocationRelativeTo(null);
			f.show();
		}
	  });
	  
	  // RadioButtons
	  
	  rb_polygonByGenerator.addMouseListener(new MouseListener() {
		public void mouseReleased(MouseEvent arg0) {}
		public void mousePressed(MouseEvent arg0) {}
		public void mouseExited(MouseEvent arg0) {}
		public void mouseEntered(MouseEvent arg0) {}
		public void mouseClicked(MouseEvent arg0) {
			if(!generationMethod.equals("generate"))
			{
				generationMethod = "generate";
				b_set_points.setEnabled(false);
				sl_edges.setEnabled(true);
				l_edge_count.setEnabled(true);
				cb_polygon_algorithm_chooser.setEnabled(true);
				b_generate_polygon.setEnabled(true);
			}
		}
	  });
	  
	  rb_polygonByPoints.addMouseListener(new MouseListener() {
		public void mouseReleased(MouseEvent arg0) {}
		public void mousePressed(MouseEvent arg0) {}
		public void mouseExited(MouseEvent arg0) {}
		public void mouseEntered(MouseEvent arg0) {}
		public void mouseClicked(MouseEvent arg0) {
			if(!generationMethod.equals("points"))
			{
				generationMethod = "points";
				//TODO: dont allow polygen algos which cannot handle points
				b_set_points.setEnabled(true);
				sl_edges.setEnabled(false);
				l_edge_count.setEnabled(false);
				cb_polygon_algorithm_chooser.setEnabled(true);
				b_generate_polygon.setEnabled(false);
			}
		}
	  });
	  
	  rb_polygonByUser.addMouseListener(new MouseListener() {
		public void mouseReleased(MouseEvent arg0) {}
		public void mousePressed(MouseEvent arg0) {}
		public void mouseExited(MouseEvent arg0) {}
		public void mouseEntered(MouseEvent arg0) {}
		public void mouseClicked(MouseEvent arg0) {
			if(!generationMethod.equals("draw"))
			{
				generationMethod = "draw";
				b_set_points.setEnabled(false);
				cb_polygon_algorithm_chooser.setEnabled(false);
				sl_edges.setEnabled(false);
				l_edge_count.setEnabled(false);
				b_generate_polygon.setEnabled(false);
			}
		}
	  });
	  

  }
  
  protected boolean checkParamCombination() {
	// TODO Auto-generated method stub
	return true;
}

public void setPoints(ArrayList<polygonsSWP.data.Point> pointList)
  {
	  b_generate_polygon.setEnabled(true);
	  this.pointList = pointList;
	  pointsSet = true;
  }
  
  private void deactivateNonSupportedParamComponents(String [] params) {
		
  }
  
  private void setOptions()
  {
	  _canvas.setGenerator((String) cb_polygon_algorithm_chooser.getSelectedItem());
	  _canvas.setN(sl_edges.getValue());
	  _canvas.setPoints(pointList);
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




