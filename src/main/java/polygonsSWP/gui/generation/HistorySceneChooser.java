package polygonsSWP.gui.generation;

import javax.swing.JComboBox;

public class HistorySceneChooser extends JComboBox
{
  private static final long serialVersionUID = 1L;

  public static enum HistorySceneMode{
    CREATE_AND_SHOW, CREATE, LAST_SCENE, DONT_CREATE;

    public static HistorySceneMode standard(){
      return CREATE_AND_SHOW;
    }

    public String toString() {
      switch (this) {
      case CREATE_AND_SHOW:
        return "create & show";
      case CREATE:
        return "create";
      case LAST_SCENE:
        return "only show";
      case DONT_CREATE:
        return "disable";
      }
      return "";
    }

    public boolean inCreateOnlyMode(){
      return this == CREATE;
    }

    public boolean inCreateAndShowMode(){
      return this == CREATE_AND_SHOW || this == LAST_SCENE;
    }

    public boolean shouldHistoryBeCreated(){
      return this != DONT_CREATE;
    }

    public boolean onlyShowLastScene() {
      return this == LAST_SCENE;
    }
  }

  public HistorySceneChooser() {
    super(new HistorySceneMode[]{
        HistorySceneMode.CREATE_AND_SHOW,
        HistorySceneMode.CREATE,
        HistorySceneMode.LAST_SCENE,
        HistorySceneMode.DONT_CREATE
    });
  }

  public HistorySceneMode getSelectedItem() {
    return (HistorySceneMode) super.getSelectedItem();
  }
}
