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
  * $Date: 2006-11-22 22:54:35 $
  * $Revision: 1.1 $
  * $Headers$
  * $Id: ParseSupervisor.java,v 1.1 2006-11-22 22:54:35 hammond Exp $
  * $Name:  $
  * $Locker:  $
  * $Source: /cvsroot/tamudrg/code/src/edu/mit/sketch/toolkit/ParseSupervisor.java,v $
  *
  **/


/**
  *
  * This subclass of Thread automatically calls the parser.
  *
  **/


class   ParseSupervisor
extends Thread
{
    
    /**
    *
    * The spatial parser 
    *
    **/    
    private ObjectManager object_manager;
    
    /**
    *
    * The constructor.
    *
    **/    
    ParseSupervisor( ObjectManager object_manager )
    {
        this.object_manager = object_manager;
    }


    /**
    *
    * run
    *
    **/
    public void
    run()
    {
        while ( true ) {
            try {
                sleep( 200000l );
            } catch ( Exception e ) {
                System.out.println( "ParseSupervisor got interrupted exception" );
                e.printStackTrace();
            }
            System.out.println( "Supervisor calling parse..." );
            object_manager.parse();
        }
    }
}


/** 
  * 
  * $Log: ParseSupervisor.java,v $
  * Revision 1.1  2006-11-22 22:54:35  hammond
  * mit's drg code, will extract and refactor to get ladder code
  *
  * Revision 1.4  2003/10/13 19:46:37  moltmans
  * Removed bad line endings.
  *
  * Revision 1.3  2003/03/06 01:08:52  moltmans
  * Added copyright to all the files.
  *
  * Revision 1.2  2001/11/23 03:22:56  mtsezgin
  * Major reorganization
  *
  * Revision 1.1.1.1  2001/03/29 16:25:01  moltmans
  * Initial directories for DRG
  *
  * Revision 1.8  2000/09/06 22:40:47  mtsezgin
  * Combinations of curves and polygons are successfully approximated
  * by straight lines and Bezier curves as appropriate. System works
  * quite reliably.
  *
  * Revision 1.5  2000/06/08 03:17:14  mtsezgin
  * *** empty log message ***
  *
  * Revision 1.13  2000/06/03 01:52:13  mtsezgin
  *
  **/
