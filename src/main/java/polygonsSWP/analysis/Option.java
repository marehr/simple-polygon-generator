package polygonsSWP.analysis;

import polygonsSWP.generators.PolygonGeneratorFactory.Parameters;

class Option
{
  private Option(){}
  
  Parameters param;
  int current;
  
  public static class StaticParameter extends Option
  {
    public StaticParameter(){}
    public StaticParameter(Parameters param, int value)
    {
      this.param = param;
      current = value;
    }
  }
  
  static class DynamicParameter extends Option
  {
    public DynamicParameter(Parameters param, int min, int max, int stepAmount)
    {
      this.min = min;
      this.max = max;
      this.stepAmount = stepAmount;
      this.param = param;
      this.current = min;
    }
    
    int min;
    int max;
    int stepAmount;
    
    /**
     * Returns the next Number. If its maxed already it returns null;
     * @return
     */
    Integer next()
    {      
      if(current + stepAmount <= max)
      {
        current += stepAmount;
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
