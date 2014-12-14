package cornerfinders.impl.rankfragmenter;

import cornerfinders.core.shapes.TPoint;

class RFNode {
    public int id;
    public TPoint corner;
    public double cost;                 // data item
    public RFNode next;                  // next link in list
    public RFNode previous;              // previous link in list

    // -------------------------------------------------------------
    public RFNode(Integer index, TPoint p, double d)                // constructor
    {
        id = index;
        corner = p;
        cost = d;
        next = previous = null;
    }

    // -------------------------------------------------------------
    public void displayLink()          // display this link
    {
        System.out.print(cost + " ");
    }
// -------------------------------------------------------------
}



