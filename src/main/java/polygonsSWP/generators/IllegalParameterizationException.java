package polygonsSWP.generators;

import polygonsSWP.generators.PolygonGeneratorFactory.Parameters;

/**
 * (c) 2011-2012
 * @author Steve Dierker <dierker.steve@fu-berlin.de>
 * @author Marcel Ehrhardt <marehr@zedat.fu-berlin.de>
 * @author Jannis Ihrig <jannis.ihrig@fu-berlin.de>
 * @author Malte Rohde <malte.rohde@inf.fu-berlin.de>
 * @author Sebastian Thobe <s.thobe@fu-berlin.de>
 * @author Kadir Tugan <kadir.tugan@gmail.com>
 */
public class IllegalParameterizationException
  extends Exception
{
  Parameters illegalParameter;
  
  public IllegalParameterizationException(String msg, Parameters illegalParameter) {
    super(msg);
    this.illegalParameter = illegalParameter;
  }
  
  public IllegalParameterizationException(String msg) {
    this(msg, null);
  }

  public Parameters getIllegalParameter() {
    return illegalParameter;
  }

  private static final long serialVersionUID = 1L;

}
