package cornerfinders.impl.rankfragmenter;

import cornerfinders.core.shapes.TPoint;

/**
 * Created by jaideepray on 12/12/14.
 */
////////////////////////////////////////////////////////////////
class RFNode {
    public TPoint corner;
    public long cost;                 // data item
    public RFNode next;                  // next link in list
    public RFNode previous;              // previous link in list
    public RFNode nextHigherCost;

    // -------------------------------------------------------------
    public RFNode(TPoint p, long d)                // constructor
    {
        corner = p;
        cost = d;
    }

    // -------------------------------------------------------------
    public void displayLink()          // display this link
    {
        System.out.print(cost + " ");
    }
// -------------------------------------------------------------
}  // end class Link

////////////////////////////////////////////////////////////////
class RFNodeList {
    private RFNode first;               // ref to first item
    private RFNode last;                // ref to last item

    // -------------------------------------------------------------
    public RFNodeList()         // constructor
    {
        first = null;                  // no items on list yet
        last = null;
    }

    // -------------------------------------------------------------
    public boolean isEmpty()          // true if no links
    {
        return first == null;
    }

    // -------------------------------------------------------------
    public void insertFirst(TPoint point, long dd)  // insert at front of list
    {
        RFNode newRFNode = new RFNode(point, dd);   // make new link

        if (isEmpty())                // if empty list,
            last = newRFNode;             // newLink <-- last
        else
            first.previous = newRFNode;   // newLink <-- old first
        newRFNode.next = first;          // newLink --> old first
        first = newRFNode;               // first --> newLink
    }

    // -------------------------------------------------------------
    public void insertLast(TPoint p, long cost)   // insert at end of list
    {
        RFNode newRFNode = new RFNode(p, cost);   // make new link
        if (isEmpty())                // if empty list,
            first = newRFNode;            // first --> newLink
        else {
            last.next = newRFNode;        // old last --> newLink
            newRFNode.previous = last;    // old last <-- newLink
        }
        last = newRFNode;                // newLink <-- last
    }

    // -------------------------------------------------------------
    public RFNode deleteFirst()         // delete first link
    {                              // (assumes non-empty list)
        RFNode temp = first;
        if (first.next == null)         // if only one item
            last = null;                // null <-- last
        else
            first.next.previous = null; // null <-- old next
        first = first.next;            // first --> old next
        return temp;
    }

    // -------------------------------------------------------------
    public RFNode deleteLast()          // delete last link
    {                              // (assumes non-empty list)
        RFNode temp = last;
        if (first.next == null)         // if only one item
            first = null;               // first --> null
        else
            last.previous.next = null;  // old previous --> null
        last = last.previous;          // old previous <-- last
        return temp;
    }

    // -------------------------------------------------------------
    // insert dd just after key
    public boolean insertAfter(long key, TPoint pt, long dd) {                              // (assumes non-empty list)
        RFNode current = first;          // start at beginning
        while (current.cost != key)    // until match is found,
        {
            current = current.next;     // move to next link
            if (current == null)
                return false;            // didn't find it
        }
        RFNode newRFNode = new RFNode(pt, dd);   // make new link

        if (current == last)              // if last link,
        {
            newRFNode.next = null;        // newLink --> null
            last = newRFNode;             // newLink <-- last
        } else                           // not last link,
        {
            newRFNode.next = current.next; // newLink --> old next
            // newLink <-- old next
            current.next.previous = newRFNode;
        }
        newRFNode.previous = current;    // old current <-- newLink
        current.next = newRFNode;        // old current --> newLink
        return true;                   // found it, did insertion
    }

    // -------------------------------------------------------------
    public RFNode deleteKey(long key)   // delete item w/ given key
    {                              // (assumes non-empty list)
        RFNode current = first;          // start at beginning
        while (current.cost != key)    // until match is found,
        {
            current = current.next;     // move to next link
            if (current == null)
                return null;             // didn't find it
        }
        if (current == first)             // found it; first item?
            first = current.next;       // first --> old next
        else                           // not first
            // old previous --> old next
            current.previous.next = current.next;

        if (current == last)              // last item?
            last = current.previous;    // old previous <-- last
        else                           // not last
            // old previous <-- old next
            current.next.previous = current.previous;
        return current;                // return value
    }

    public RFNode deleteNode(RFNode current) {

        if (current == first)             // found it; first item?
            first = current.next;       // first --> old next
        else                           // not first
            // old previous --> old next
            current.previous.next = current.next;

        if (current == last)              // last item?
            last = current.previous;    // old previous <-- last
        else                           // not last
            // old previous <-- old next
            current.next.previous = current.previous;
        return current;                // return value
    }

    public RFNode updateAndDelete(RFNode node) {
        RFNode succ = node.next;
        RFNode prev = node.previous;
        deleteNode(node);
        return null;
    }

    public void initList() {
        RFNode current = first;
        while (current.next != null) {
            RFNode el = current.next;
            long cost_curr = current.cost;
            long maxDiffSoFar = 100000;

            while (el != null) {
                if (el.cost > cost_curr) {
                    if (el.cost - cost_curr < maxDiffSoFar) {
                        maxDiffSoFar = el.cost - cost_curr;
                        current.nextHigherCost = el;
                    }
                }
                el = el.next;
            }
        }

    }


    // -------------------------------------------------------------
    public void displayForward() {
        System.out.print("List (first-->last): ");
        RFNode current = first;          // start at beginning
        while (current != null)         // until end of list,
        {
            current.displayLink();      // display data
            current = current.next;     // move to next link
        }
        System.out.println("");
    }

    // -------------------------------------------------------------
    public void displayBackward() {
        System.out.print("List (last-->first): ");
        RFNode current = last;           // start at end
        while (current != null)         // until start of list,
        {
            current.displayLink();      // display data
            current = current.previous; // move to previous link
        }
        System.out.println("");
    }
// -------------------------------------------------------------
}  // end class DoublyLinkedList

////////////////////////////////////////////////////////////////
class DoublyLinkedApp {
    public static void main(String[] args) {                             // make a new list
        RFNodeList theList = new RFNodeList();


//        theList.insertFirst(22);      // insert at front
//        theList.insertFirst(44);
//        theList.insertFirst(66);
//
//        theList.insertLast(11);       // insert at rear
//        theList.insertLast(33);
//        theList.insertLast(55);
//
//        theList.displayForward();     // display list forward
//        theList.displayBackward();    // display list backward
//
//        theList.deleteFirst();        // delete first item
//        theList.deleteLast();         // delete last item
//        theList.deleteKey(11);        // delete item with key 11
//
//        theList.displayForward();     // display list forward
//
//        theList.insertAfter(22, 77);  // insert 77 after 22
//        theList.insertAfter(33, 88);  // insert 88 after 33

        theList.displayForward();     // display list forward
    }
}
