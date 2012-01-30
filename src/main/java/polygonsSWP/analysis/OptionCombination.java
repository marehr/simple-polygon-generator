package polygonsSWP.analysis;

import java.util.ArrayList;
import java.util.HashMap;

import polygonsSWP.analysis.Option.DynamicParameter;
import polygonsSWP.generators.PolygonGeneratorFactory.Parameters;

public class OptionCombination
{
  ArrayList<Option> options = new ArrayList<Option>();
  
  
  void add(Option option)
  {
    for(Option opt : options)
    {
      if(option.param == opt.param)//Checks if the Option already exists
        throw new RuntimeException("Parameter already exists");
    }
    options.add(option);
  }
  
  private boolean nextInitiated = false;
  ArrayList<Option> staticparams = new ArrayList<Option>();
  ArrayList<DynamicParameter> dynparams = new ArrayList<DynamicParameter>();
  
  private void initNext()
  {
    nextInitiated = true;
    for(Option opt : options)
    {
      if(opt.getClass().equals(DynamicParameter.class))
        dynparams.add((DynamicParameter) opt);
      else
        staticparams.add(opt);
    }
  }
  
  
  private boolean maxed = false;
  HashMap<Parameters, Number> next()
  {
    if(maxed)//If there is no other Combination, dont calculate further
      return null;
    if(!nextInitiated)
      initNext();
    HashMap<Parameters, Number> params = new HashMap<Parameters, Number>();
    
    for(Option o : staticparams)
      params.put(o.param, o.current);
    
    
    for(int i = 0; i < dynparams.size(); i++)//Add the Combination
    {
      params.put(dynparams.get(i).param, dynparams.get(i).current.doubleValue());
    }
    
    
    for(int i = 0; i < dynparams.size(); i++)//Increment to the next Combination
    {
      Number num = dynparams.get(i).next();
      
      if(num == null)
      {
        if(i+1 == dynparams.size())//There exist no Combination more
        {
          maxed = true;
          return null;
        }
        dynparams.get(i).resetToMin();
        continue;
      }
      else
        break;
    }
    
    return params;
  }
  
}
