package polygonsSWP.gui.generation;

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import polygonsSWP.generators.PolygonGeneratorFactory;

/**
 * Simple JComboBox extension able to enable/disable algorithms
 * based on their abilities.
 * 
 * (c) 2011-2012
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Kadir Tugan <kadir.tugan@gmail.com>
 */
class GeneratorChooser
  extends JComboBox
{
  private static final long serialVersionUID = 1L;
  private AlgoCellRenderer acr;

  GeneratorChooser(PolygonGeneratorFactory[] polygon_algorithm_list,
      boolean randomPoints) {
    super(polygon_algorithm_list);

    acr = new AlgoCellRenderer();
    setRenderer(acr);
    
    switchPointGenerationMode(randomPoints);
  }

  void switchPointGenerationMode(boolean randomPoints) {
    acr.switchPointGenerationMode(randomPoints);
    validate();
    repaint();
  }

  public PolygonGeneratorFactory getSelectedItem(){
    return (PolygonGeneratorFactory) super.getSelectedItem();
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

      PolygonGeneratorFactory pgf = (PolygonGeneratorFactory) value;
      boolean can_handle_this = randomPoints || pgf.acceptsUserSuppliedPoints();     
      setEnabled(can_handle_this);
      setFocusable(can_handle_this);
      
      return this;
    }
  }

}
