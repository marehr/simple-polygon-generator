package polygonsSWP.analysis;

import java.util.ArrayList;
import java.util.HashMap;

import polygonsSWP.analysis.Option.DynamicParameter;
import polygonsSWP.analysis.Option.StaticParameter;
import polygonsSWP.generators.PolygonGeneratorFactory.Parameters;

public class OptionCombinator
{

  
  private ArrayList<StaticParameter> staticparams = new ArrayList<StaticParameter>();
  private ArrayList<DynamicParameter> dynparams = new ArrayList<DynamicParameter>();
  
  
  void add(DynamicParameter dynParam)
  {
    if(checkIfParamAlreadyExists(dynParam.param));
      dynparams.add(dynParam);
  }
  
  void add(StaticParameter statParam)
  {
    if(checkIfParamAlreadyExists(statParam.param));
      staticparams.add(statParam);
  }
  
  
  private boolean checkIfParamAlreadyExists(Parameters p)
  {
    for(DynamicParameter opt : dynparams)
    {
      if(p == opt.param)//Checks if the Option already exists
        throw new RuntimeException("Parameter already exists");
    }
    for(StaticParameter opt : staticparams)
    {
      if(p == opt.param)//Checks if the Option already exists
        throw new RuntimeException("Parameter already exists");
    }
    return true;
  }

  
  private boolean maxed = false;
  HashMap<Parameters, Number> next()
  {
    if(maxed)//If there is no other Combination, dont calculate further
      return null;
    HashMap<Parameters, Number> params = new HashMap<Parameters, Number>();
    
    for(StaticParameter o : staticparams)
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
          return params;
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
