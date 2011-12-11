package polygonsSWP.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 * Panel which controls the shortest path calculation.
 * 
 * @author Sebastian Thobe <sebastianthobe@googlemail.com>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 */
class ShortestPathPanel extends JPanel {
  private static final long serialVersionUID = 1L;
  
  /* Controls. */
  private final JButton b_calc_shortest_path;
  private final JRadioButton rb_set_points, rb_generate_points;
  
  ShortestPathPanel() {
    b_calc_shortest_path = new JButton("Calculate Shortest Path");

    ButtonGroup bg_shortest_path = new ButtonGroup();
    rb_generate_points = new JRadioButton("Generate Points");
    rb_generate_points.setSelected(true);
    rb_set_points = new JRadioButton("Set Points");
    bg_shortest_path.add(rb_generate_points);
    bg_shortest_path.add(rb_set_points);
    
    // Build the interface.
    layoutControls();
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
  
}
