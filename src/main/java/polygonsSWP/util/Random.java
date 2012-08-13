package polygonsSWP.util;

/**
 * (c) 2011-2012
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Kadir Tugan <kadir.tugan@gmail.com>
 */
public class Random extends java.util.Random
{
  private static final long serialVersionUID = 1L;

  static boolean pseudoRandom = false;
  static Random rand = null;
  static long seed;

  public Random() { super(); }
  public Random(long seed) { super(seed); }

  public static void pseudoRandom(boolean on, long seed){
    pseudoRandom = on;
    Random.seed = seed;
  }

  public static Random create(){
    if(!pseudoRandom) return new Random();

    synchronized (Random.class) {
      if(rand == null) rand = new Random(seed);
      return rand;
    }
  }

  public static Random create(long seed){
    if(!pseudoRandom) return new Random(seed);

    synchronized (Random.class) {
      if(rand == null) rand = new Random(seed);
      return rand;
    }
  }
}
