// (c) MIT 2003.  All rights reserved.

////////////////////////////////////////////////////////////////////////////////
//                                                                            //
// AUTHOR:      Tevfik Metin Sezgin                                           //
//              Massachusetts Institute of Technology                         //
//              Department of Electrical Engineering and Computer Science     //
//              Artificial Intelligence Laboratory                            //
//                                                                            //
// E-MAIL:        mtsezgin@ai.mit.edu, mtsezgin@mit.edu                       //
//                                                                            //
// COPYRIGHT:   Tevfik Metin Sezgin                                           //
//              All rights reserved. This code can not be copied, modified,   //
//              or distributed in whole or partially without the written      //
//              permission of the author. Also see the COPYRIGHT file.        //
//                                                                            //
////////////////////////////////////////////////////////////////////////////////
package cornerfinders.toolkit;

/** 
  * 
  * See the end of the file for the log of changes.
  * 
  * $Author: hammond $
  * $Date: 2006-11-22 22:54:36 $   
  * $Revision: 1.1 $
  * $Headers$
  * $Id: Timer.java,v 1.1 2006-11-22 22:54:36 hammond Exp $     
  * $Name:  $   
  * $Locker:  $
  * $Source: /cvsroot/tamudrg/code/src/edu/mit/sketch/toolkit/Timer.java,v $
  *  
  **/


public class
Timer
{
    static Timer timer;
    
    public native long
    getTics();

    static {
        System.loadLibrary( "timer" );
    }
    
    public static long 
    tics()
    {
        if ( timer == null ) {
            timer = new Timer();
        }
        
        return timer.getTics();
    }
}


/** 
  * 
  * $Log: Timer.java,v $
  * Revision 1.1  2006-11-22 22:54:36  hammond
  * mit's drg code, will extract and refactor to get ladder code
  *
  * Revision 1.4  2003/03/06 01:08:52  moltmans
  * Added copyright to all the files.
  *
  * Revision 1.3  2001/10/12 22:25:53  mtsezgin
  * This is a commit of all files.
  * Shoapid
  * vi sux:q
  *
  * Revision 1.2  2001/04/03 16:08:01  uid6752
  * Minor changes to make the timing capture work.
  *
  * Revision 1.1.1.1  2001/03/29 16:25:01  moltmans
  * Initial directories for DRG
  *
  * Revision 1.5  2000/09/06 22:41:08  mtsezgin
  * Combinations of curves and polygons are successfully approximated
  * by straight lines and Bezier curves as appropriate. System works
  * quite reliably.
  *
  * Revision 1.1  2000/06/10 18:56:16  mtsezgin
  *
  * This class provides the native method call interface to the real time
  * clock in lousy win9X operating systems. This is needed because jvm in
  * windows uses the equivalent of the standart clock() function call, and
  * the OS implementation of this function uses interrupt 1Ch which is fired
  * approximately 18.2 times per second. This is not sufficient for most
  * purposes.
  *
  *  
  **/
