import junit.framework.Test;


public class InterruptionTest
{
  private static Thread testTread = new Thread(new Runnable() {
    public void run() {
      while (true){
        System.out.print(".");
      }
    }
  });

  /**
   * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
   *
   * @param args
   */
  public static void main(String[] args) {
    testTread.run();
  }
}
