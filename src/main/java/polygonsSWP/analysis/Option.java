package polygonsSWP.analysis;

import polygonsSWP.generators.PolygonGeneratorFactory.Parameters;

class Option
{
  private Option(){}
  
  Parameters param;
  Number current;
  
  public static class StaticParameter extends Option
  {
    public StaticParameter(){}
    public StaticParameter(Parameters param, Number value)
    {
      this.param = param;
      current = value;
    }
  }
  
  static class DynamicParameter extends Option
  {
    public DynamicParameter(Parameters param, Number min, Number max, Number stepAmount)
    {
      this.min = min;
      this.max = max;
      this.stepAmount = stepAmount;
      this.param = param;
      this.current = min;
    }
    
    Number min;
    Number max;
    Number stepAmount;
    
    /**
     * Returns the next Number. If its maxed already it returns null;
     * @return
     */
    Number next()
    {
      if(current.doubleValue() + stepAmount.doubleValue() <= max.doubleValue())
      {
        current = current.doubleValue() + stepAmount.doubleValue();
        return current;
      }
      else
      {
        return null;
      }
    }
    
    void resetToMin()
    {
      current = min;
    }
  }
  
}
