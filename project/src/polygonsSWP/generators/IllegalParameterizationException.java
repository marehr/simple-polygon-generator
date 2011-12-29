package polygonsSWP.generators;

import polygonsSWP.generators.PolygonGeneratorFactory.Parameters;

public class IllegalParameterizationException
  extends Exception
{
  Parameters illegalParameter;
  
  public IllegalParameterizationException(String msg, Parameters illegalParameter) {
    super(msg);
    this.illegalParameter = illegalParameter;
  }
  
  public Parameters getIllegalParameter() {
    return illegalParameter;
  }

  private static final long serialVersionUID = 1L;

}
