package polygonsSWP.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class MainFrame extends JFrame {

	private static final long serialVersionUID = 313119639927682997L;
	
	private int DEFFAULTSIZE = 600;
	
	// main components
	private PaintPanel _canvas = new PaintPanel();
	private InfoFrame infoframe;
	
	// menu components
	private JLabel l_polygon_generation, l_shortest_path, l_edge_count;
	private JComboBox cb_polygon_algorithm_chooser;
	
	private String [] polygon_algorihtm_list = {"Permute and Reject","algo2","algo3","algo4","algo5"};
	
	private JButton b_set_points,b_generate_polygon,b_calc_shortest_path;
	private JSlider sl_edges;
	
	private ButtonGroup bg_shortest_path;
	private JRadioButton rb_set_points, rb_generate_points;
	
	//panels
	private JPanel p_polygon_generation,p_shortest_path,p_menu,p_polygon_settings,p_button_group;
	
  public static void main(String[] args) {
    JFrame frame = new MainFrame();
    frame.setTitle("PolygonGen");
    frame.setSize(750, 750);
    frame.setBackground(Color.white);
    frame.setLocationRelativeTo(null); // center window
    frame.addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){System.exit(0);}});
    frame.setVisible(true);
  }
  
  public MainFrame()
  {
	  infoframe = new InfoFrame();  
	  infoframe.setTitle("Polygon Info");
	  infoframe.setSize(400,300);
	  infoframe.setLocationRelativeTo(null);
	 
	  // init labels
	  
	  l_polygon_generation = new JLabel("Polygon Generation");
	  l_shortest_path = new JLabel("Shortest Path");
	  l_edge_count = new JLabel("Edges: 10");
	  
	  // init combobox
	  
	  cb_polygon_algorithm_chooser = new JComboBox(polygon_algorihtm_list); // init combobox with string array
	  
	  // init slider
	  
	  sl_edges = new JSlider(1,1000,10);
	  
	  // init buttons
	  
	  b_calc_shortest_path = new JButton("Calculate Shortest Path");
	  b_set_points = new JButton("Set Polygon Points");
	  b_generate_polygon = new JButton("Generate Polygon");
	  
	  // building interface
  
	  p_polygon_generation = new JPanel();
	  p_polygon_generation.setLayout(new BorderLayout(5,5));
	  p_polygon_generation.add(l_polygon_generation, BorderLayout.NORTH);
	  p_polygon_settings = new JPanel();
	  p_polygon_settings.setLayout(new GridLayout(2,2));
	  p_polygon_settings.add(cb_polygon_algorithm_chooser);
	  p_polygon_settings.add(b_set_points);
	  p_polygon_settings.add(l_edge_count);
	  p_polygon_settings.add(sl_edges);
	  p_polygon_generation.add(p_polygon_settings, BorderLayout.CENTER);
	  p_polygon_generation.add(b_generate_polygon, BorderLayout.SOUTH);
	  
	  p_shortest_path = new JPanel();
	  p_shortest_path.setLayout(new BorderLayout(5,5));
	  p_shortest_path.add(l_shortest_path, BorderLayout.NORTH);
	  bg_shortest_path = new ButtonGroup();
	  rb_generate_points = new JRadioButton("Generate Points");
	  rb_set_points = new JRadioButton("Set Points");
	  bg_shortest_path.add(rb_generate_points);
	  bg_shortest_path.add(rb_set_points);
	  p_button_group = new JPanel();
	  p_button_group.setLayout(new GridLayout(1,2));
	  p_button_group.add(rb_generate_points);
	  p_button_group.add(rb_set_points);
	  p_shortest_path.add(p_button_group);
	  p_shortest_path.add(b_calc_shortest_path, BorderLayout.SOUTH);
	  
	  p_menu = new JPanel();
	  p_menu.setLayout(new GridLayout(1,2));
	  p_menu.add(p_polygon_generation);
	  p_menu.add(p_shortest_path);
	  
	  // adding all panels to main window
	  
	  setLayout(new BorderLayout(5,5));
	  add(p_menu, BorderLayout.NORTH);
	  add(_canvas, BorderLayout.CENTER);
	  
	  
	  // action listener
	  
	  // Slider
	  sl_edges.addMouseMotionListener(new MouseMotionListener() {
		public void mouseDragged(MouseEvent arg0) 
		{
			l_edge_count.setText("Edges: " + sl_edges.getValue());
			//_canvas.setN(sl_edges.getValue());
		}
		public void mouseMoved(MouseEvent arg0) {}		
	  });
	  sl_edges.addMouseListener(new MouseListener(){
		public void mouseClicked(MouseEvent e) {
			l_edge_count.setText("Edges: " + sl_edges.getValue());
			//_canvas.setN(sl_edges.getValue());
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
			_canvas.repaint();
		}
	  });
	  
	  b_set_points.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			// method stubs
		}
	  });
	  

  }
  
  private void deactivateNonSupportedParamComponents(String [] params) {
		
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




