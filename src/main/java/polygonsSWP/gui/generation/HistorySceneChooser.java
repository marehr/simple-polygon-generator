package polygonsSWP.gui.generation;

import javax.swing.JComboBox;

public class HistorySceneChooser extends JComboBox
{
  private static final long serialVersionUID = 1L;

  public static enum HistorySceneMode{
    CREATE_AND_SHOW, CREATE, DONT_CREATE;

    public static HistorySceneMode standard(){
      return CREATE_AND_SHOW;
    }

    public String toString() {
      switch (this) {
      case CREATE_AND_SHOW:
        return "create & show";
      case CREATE:
        return "create";
      case DONT_CREATE:
        return "disable";
      }
      return "";
    }

    public boolean inCreateOnlyMode(){
      return this == CREATE;
    }

    public boolean inCreateAndShowMode(){
      return this == CREATE_AND_SHOW;
    }

    public boolean shouldHistoryBeCreated(){
      return this != DONT_CREATE;
    }
  }

  public HistorySceneChooser() {
    super(new HistorySceneMode[]{HistorySceneMode.CREATE_AND_SHOW, HistorySceneMode.CREATE, HistorySceneMode.DONT_CREATE});
  }

  public HistorySceneMode getSelectedItem() {
    return (HistorySceneMode) super.getSelectedItem();
  }
}
