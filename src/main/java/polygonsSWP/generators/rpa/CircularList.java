package polygonsSWP.generators.rpa;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

public class CircularList <E>
  extends ArrayList <E>
{
  private static class CircularIterator<E> implements ListIterator<E>{
    
    private ArrayList<E> _list;
    private int _index;
    
    public CircularIterator(ArrayList<E> list, int index) {
      _index = index;
      _list = list;
    }
    
    
    @Override
    public void add(E arg0) {
      _list.add(_index+1, arg0);
      _index = (_index+1)%_list.size();
      
    }

    @Override
    public boolean hasNext() {
      if(_list.size() > 0)
        return true;
      else
        return false;
    }

    @Override
    public boolean hasPrevious() {
      if(_list.size() > 0)
        return true;
      else
        return false;
    }

    @Override
    public E next() {
      if(_list.size() > 0){
        _index = (_index+1)%_list.size();
        return _list.get(_index);
      }
      else
        return null;
    }

    @Override
    public int nextIndex() {
      return (_index+1)%_list.size();
    }

    @Override
    public E previous() {
      if (_list.size() <= 0)
        return null;
      
      // if just initialized, return last element of list
      if (_index == -1){
       _index = _list.size()-1;
      return _list.get(_index);
      }
      
      // else, return previous
      _index = (_list.size() + (_index-1)) % _list.size();
      return _list.get(_index);
    }

    @Override
    public int previousIndex() {
      if (_list.size() <= 0)
        return -1;
      
      // if just initialized, prev element is last of list
      if (_index == -1)
        return _list.size() -1;
      
      return (_list.size() + (_index-1)) % _list.size();
    }

    @Override
    public void remove() {
      if (_list.size() > 0){
        _list.remove(_index);
        _index = (_index-1)%_list.size();
      }
    }

    @Override
    public void set(E arg0) {
      _list.set(_index, arg0);
    }
  }
  
  @Override
  public ListIterator<E> listIterator(){
    return new CircularIterator<E>(this, -1);
    
  }
  
  @Override
  public ListIterator<E> listIterator(int index){
    if (this.size() > index)
      return new CircularIterator<E>(this, index);
    else
      throw new IndexOutOfBoundsException();
    
  }
  
  @Override
  public String toString(){
    String listString = "[";
    CircularIterator<E> circIter = (CircularIterator<E>) this.listIterator();
    for (int i = 0; i < this.size()-1; i++) {
      listString += circIter.next() + ",";      
    }
    listString += circIter.next();
    listString += "]";
    return listString;
  }
}
