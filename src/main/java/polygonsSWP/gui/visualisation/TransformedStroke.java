package polygonsSWP.gui.visualisation;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

/**
 * A implementation of {@link Stroke} which transforms another Stroke with an
 * {@link AffineTransform} before stroking with it. Found here:
 * http://stackoverflow
 * .com/questions/5046088/affinetransform-without-transforming-stroke
 * 
 * (c) 2011-2012
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Kadir Tugan <kadir.tugan@gmail.com>
 */
public class TransformedStroke
  implements Stroke
{
  private AffineTransform transform;
  private AffineTransform inverse;
  private Stroke stroke;

  public TransformedStroke(Stroke base, AffineTransform at) {
    this.transform = new AffineTransform(at);
    try {
      this.inverse = transform.createInverse();
    }
    catch (NoninvertibleTransformException e) {
      // Shouldn't happen.
      throw new RuntimeException(e);
    }
    this.stroke = base;
  }

  public Shape createStrokedShape(Shape s) {
    Shape sTrans = transform.createTransformedShape(s);
    Shape sTransStroked = stroke.createStrokedShape(sTrans);
    Shape sStroked = inverse.createTransformedShape(sTransStroked);
    return sStroked;
  }
}