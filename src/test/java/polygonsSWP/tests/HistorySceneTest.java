package polygonsSWP.tests;

import java.io.PrintWriter;
import java.util.HashMap;

import org.junit.Test;

import polygonsSWP.data.History;
import polygonsSWP.data.Scene;
import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.PolygonGeneratorFactory.Parameters;
import polygonsSWP.generators.other.PermuteAndRejectFactory;

public class HistorySceneTest
{

  @Test
  public void PermuteAndRejectTest() {
    PermuteAndRejectFactory fac = new PermuteAndRejectFactory();
    HashMap<Parameters, Object> params = new HashMap<Parameters, Object>();
    params.put(Parameters.n, 7);
    params.put(Parameters.size, 600);
    History hist = new History();
    PolygonGenerator inst;
    try {
      inst = fac.createInstance(params, null, hist);
      inst.generate();
      int i = 0;
      for(Scene item : hist.getScenes()) {
        System.out.println("Save Scene " + i);
        PrintWriter out = new PrintWriter("svg"+i+".svg");
        out.print(item.toSvg());
        out.flush();
        out.close();
        ++i;
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
    
  }
}
