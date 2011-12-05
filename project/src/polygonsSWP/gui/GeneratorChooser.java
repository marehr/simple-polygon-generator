package polygonsSWP.gui;

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.PolygonGenerator.Parameters;

/**
 * Simple JComboBox extension able to enable/disable algorithms
 * based on their abilities.
 * 
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 */
public class GeneratorChooser
  extends JComboBox
{
  private static final long serialVersionUID = 1L;
  private AlgoCellRenderer acr;

  public GeneratorChooser(PolygonGenerator[] polygon_algorithm_list,
      boolean randomPoints) {
    super(polygon_algorithm_list);

    acr = new AlgoCellRenderer();
    setRenderer(acr);
    
    switchPointGenerationMode(randomPoints);
  }

  public void switchPointGenerationMode(boolean randomPoints) {
    acr.switchPointGenerationMode(randomPoints);
    validate();
    repaint();
  }


  private static class AlgoCellRenderer
    extends JLabel
    implements ListCellRenderer
  {
    private static final long serialVersionUID = 1L;
    private boolean randomPoints = true;

    void switchPointGenerationMode(boolean randomPoints) {
      this.randomPoints = randomPoints;
    }
    
    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {
      setText(value.toString());

      PolygonGenerator pg = (PolygonGenerator) value;
      
      boolean can_handle_this = false;
      for(Parameters[] s : pg.getAcceptedParameters()) {
        for(Parameters p : s) {
          if((randomPoints && p.equals(Parameters.n)) ||
              (!randomPoints && p.equals(Parameters.points))) {
            can_handle_this = true;
            break;
          }
        }
      }
      
      setEnabled(can_handle_this);
      setFocusable(can_handle_this);
      
      return this;
    }
  }

}
