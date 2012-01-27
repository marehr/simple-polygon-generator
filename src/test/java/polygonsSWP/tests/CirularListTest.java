package polygonsSWP.tests;

import java.util.ListIterator;

import org.junit.Test;

import polygonsSWP.generators.rpa.CircularList;
import polygonsSWP.geometry.Point;

public class CirularListTest
{
  
  @Test
  public void CircularListTest(){
    CircularList<Point> list1 = new CircularList<Point>();
    list1.add(new Point(0,0));
    list1.add(new Point(1,1));
    list1.add(new Point(2,2));
    ListIterator<Point> circIter1 = list1.listIterator();
    
    System.out.println(circIter1.next());
    System.out.println(circIter1.next());
    System.out.println(circIter1.next());
    System.out.println(circIter1.next());
    System.out.println(circIter1.next());
    System.out.println(circIter1.next());
    
    System.out.println("--------------\n");
    
    list1 = new CircularList<Point>();
    list1.add(new Point(0,0));
    list1.add(new Point(1,1));
    list1.add(new Point(2,2));
    circIter1 = list1.listIterator();
    
    System.out.println(circIter1.previous());
    System.out.println(circIter1.previous());
    System.out.println(circIter1.previous());
    System.out.println(circIter1.previous());
    System.out.println(circIter1.previous());
    System.out.println(circIter1.previous());
    
    System.out.println("--------------\n");
    
    list1 = new CircularList<Point>();
    circIter1 = list1.listIterator();

    System.out.println(circIter1.next());
    circIter1.add(new Point(0, 0));
    System.out.println(circIter1.next());
    System.out.println(circIter1.previous());
    circIter1.add(new Point(1, 1));
    System.out.println(circIter1.next());
    System.out.println(circIter1.next());
    System.out.println(list1);
    circIter1.add(new Point(2, 2));
    System.out.println(list1);
    circIter1.add(new Point(3, 3));
    System.out.println(list1);
    circIter1.remove();
    System.out.println(list1.toString());
    circIter1 = list1.listIterator(1);
    circIter1.add(new Point(-1, -1));
    System.out.println(circIter1.next());
    System.out.println(list1.toString());
    circIter1 = list1.listIterator(1);
    System.out.println(circIter1.next());
    circIter1.remove();
    System.out.println(circIter1.previous());
    System.out.println("---------------\n");
    
    list1 = new CircularList<Point>();
    list1.add(new Point(0,0));
    list1.add(new Point(1,1));
    list1.add(new Point(2,2));
    circIter1 = list1.listIterator(1);
    System.out.println(circIter1.next());
  }
  


}
