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
  * $Id: VariableLengthDataManager.java,v 1.1 2006-11-22 22:54:36 hammond Exp $     
  * $Name:  $   
  * $Locker:  $
  * $Source: /cvsroot/tamudrg/code/src/edu/mit/sketch/toolkit/VariableLengthDataManager.java,v $
  *  
  **/

import edu.mit.sketch.util.Util;
import java.util.ArrayList;

/**
  *
  * This class manages the training data.
  *
  **/

public
class VariableLengthDataManager
{
    public int        input_lengths[][];
    public ArrayList  reorganized_training_data[];

    /**
    *
    * The constructor. The segmentation of objects is passed using two
    * arguments: ending_indices (which is the ending index+1 for each object in
    * the input_objects ArrayList) and object id. 0 for the first object type (Butterfly,
    * 1 for the second (motorized unit), etc... ending_indices.length == object_ids.length
    * and they are both the same as the number of sketch objects in the input_objects (not
    * the number of strokes).
    *
    * number_of_classes is the number of actual sketch object classes (ie. rectangles, stick figures...)
    * Same as class_ranges.length.
    *
    * The input training data is grouped based on their class. If the data for 
    * a single class includes examples of different length, they are lumped 
    * together as opposed to separating them by their lengths.
    * 
    * The two dimensional input_lengths field keeps the lengths of the data
    * for each class. It is indexed as [class][class_length_j]
    **/
    public
    VariableLengthDataManager( ArrayList input_objects, 
                 int       ending_indices[], 
                 int       object_ids[],
                 int       number_of_classes )
    {
        ArrayList input_lengths_arraylists[] = new ArrayList[number_of_classes];

        reorganized_training_data = new ArrayList[number_of_classes];
    
        for ( int i=0; i<number_of_classes; i++ ){
            reorganized_training_data[i] = new ArrayList();
            input_lengths_arraylists[i]  = new ArrayList();
        }

        for ( int i=0; i<ending_indices.length; i++ ) {
            ArrayList data = new ArrayList();
            for ( int k = (i==0) ? 0 : ending_indices[i-1]; k<ending_indices[i]; k++ ) {
                data.addAll( DataManager.getEncodingAtIndex2( input_objects, k ) );
            }
            reorganized_training_data[object_ids[i]].add( data );
            if ( !DataManager.containsNumber( input_lengths_arraylists[object_ids[i]], data.size() ) ) {
                input_lengths_arraylists[object_ids[i]].add( new Integer( data.size() ) );
            }
        }
        
        input_lengths = new int[number_of_classes][0];
        
        for ( int i=0; i<input_lengths.length; i++ ) {
            input_lengths[i] = Util.arrayListToIntArray( input_lengths_arraylists[i] );
        }
    }



    /**
    * Expand data set by merging the data from data_manager to the current data
    **/
    public void
    expandDataSet( VariableLengthDataManager data_manager )
    {
        int number_of_classes = input_lengths.length;
        ArrayList input_lengths_arraylists[] = new ArrayList[number_of_classes];

        for ( int i=0; i<number_of_classes; i++ ){
            input_lengths_arraylists[i]  = new ArrayList();
        }
        
        for ( int i=0; i<number_of_classes; i++ ){
            for ( int j=0; j<input_lengths[i].length; j++ ) {
                input_lengths_arraylists[i].add( new Integer( input_lengths[i][j] ) );
            }
            for ( int j=0; j<data_manager.input_lengths[i].length; j++ ) {
                if ( !DataManager.containsNumber( input_lengths_arraylists[i], data_manager.input_lengths[i][j] ) ) {
                    input_lengths_arraylists[i].add( new Integer( data_manager.input_lengths[i][j] ) );
                }
            }
        }

        for ( int i=0; i<number_of_classes; i++ ) {
            reorganized_training_data[i].addAll( data_manager.reorganized_training_data[i] );
        }
        
        input_lengths = new int[number_of_classes][0];
        
        for ( int i=0; i<input_lengths.length; i++ ) {
            input_lengths[i] = Util.arrayListToIntArray( input_lengths_arraylists[i] );
        }
    }
    
        
    /**
    *
    * Return training data in a new format using and input_objects 
    * segmentation 
    *
    **/    
    public ArrayList[]
    getReorganizedTrainingData()
    {
        return reorganized_training_data;
    }
    
    
    /**
    *
    * return EndingIndices and ObjectIDs in a 2d array. 
    * 0->ending_indices 1->object_ids
    *
    **/
    public static int[][]
    getEndingIndicesAndObjectIDs2( int in_segmentation[],
				   int shortest_path[],
				   int in_input_lengths[][], 
				   ArrayList objects )
    {
        ArrayList ids          = new ArrayList();
        ArrayList indices      = new ArrayList();
        ArrayList observations = new ArrayList();
        
        int length = 0;
        int i      = 0;
        for ( int stroke=0; stroke<objects.size(); stroke++ ) {
            observations.addAll( DataManager.getEncodingAtIndex2( objects, stroke ) );
            

            System.out.println( "stroke = " + stroke );

	    if ( length + observations.size() == shortest_path[i+1] ) {
		System.out.println( "i = " + i );
		System.out.println( "length = " + length );
		System.out.println( "segmentation[length] = " + in_segmentation[length] );
		System.out.println( "Adding ending index " + ( stroke + 1 ) + ",  id " + in_segmentation[length] + " \n" );

		indices.add( new Integer( stroke + 1 ) );
		ids.add( new Integer( in_segmentation[length] ) );
		length = shortest_path[i+1];
		observations.clear();
		i++;
            }
        }
        
        if ( ((Integer)(indices.get( indices.size() -1 ))).intValue() != objects.size() ) {
            System.out.println( "getEndingIndices() -> got a partial object at the end" );
            ids.add( new Integer( in_segmentation[in_segmentation.length-1] ) );
            indices.add( new Integer( objects.size() ) );
        }
        
	int result[][] = new int[2][0]; 
        result[0]      = Util.arrayListToIntArray( indices );
        result[1]      = Util.arrayListToIntArray( ids );

	return result;
    }
    
    
    /**
    *
    * return EndingIndices and ObjectIDs in a 2d array. 
    * 0->ending_indices 1->object_ids
    *
    **/
    public static int[][]
    getEndingIndicesAndObjectIDs( int in_segmentation[], 
				   int in_input_lengths[][], 
				   ArrayList objects )
    {
        ArrayList ids          = new ArrayList();
        ArrayList indices      = new ArrayList();
        ArrayList observations = new ArrayList();
        
        int length = 0;
        int i      = 0;
        for ( int stroke=0; stroke<objects.size(); stroke++ ) {
            observations.addAll( DataManager.getEncodingAtIndex2( objects, stroke ) );
            i = observations.size()-1;
            
            System.out.println( "i = " + i );
            System.out.println( "stroke = " + stroke );
            System.out.println( "length = " + length );
            System.out.println( "segmentation[i] = " + in_segmentation[i] );
            System.out.println( "input_lengths[segmentation[i]] = " + in_input_lengths[in_segmentation[i]] );
	    
            for ( int k=0; k<in_input_lengths[in_segmentation[i]].length; k++ ) {
                if ( observations.size() == length + in_input_lengths[in_segmentation[i]][k] ) {
                    length += in_input_lengths[in_segmentation[i]][k];
                
                    System.out.println( "Adding " + ( stroke + 1 ) + "\n" );
                    indices.add( new Integer( stroke + 1 ) );
                    ids.add( new Integer( in_segmentation[i] ) );
                }
            }
        }
        
        if ( ((Integer)(indices.get( indices.size() -1 ))).intValue() != objects.size() ) {
            System.out.println( "getEndingIndices() -> got a partial object at the end" );
            ids.add( new Integer( in_segmentation[in_segmentation.length-1] ) );
            indices.add( new Integer( objects.size() ) );
        }
        
	int result[][] = new int[2][0]; 
        result[0]      = Util.arrayListToIntArray( indices );
        result[1]      = Util.arrayListToIntArray( ids );

	return result;
    }
    
    
    /**
    *
    * return ending_indices for segmented sketch objects
    *
    **/
    public static int[]
    getEndingIndices( int in_segmentation[], int in_input_lengths[][], ArrayList objects )
    {
        ArrayList result       = new ArrayList();
        ArrayList observations = new ArrayList();
        
        int length = 0;
        int i      = 0;
        for ( int stroke=0; stroke<objects.size(); stroke++ ) {
            observations.addAll( DataManager.getEncodingAtIndex2( objects, stroke ) );
            i = observations.size()-1;
            
            System.out.println( "i = " + i );
            System.out.println( "stroke = " + stroke );
            System.out.println( "length = " + length );
            System.out.println( "segmentation[i] = " + in_segmentation[i] );
            System.out.println( "input_lengths[segmentation[i]] = " + in_input_lengths[in_segmentation[i]] );
            for ( int k=0; k<in_input_lengths[in_segmentation[i]].length; k++ ) {
                if ( observations.size() == length + in_input_lengths[in_segmentation[i]][k] ) {
                    length += in_input_lengths[in_segmentation[i]][k];
                
                    System.out.println( "Adding " + ( stroke + 1 ) + "\n" );
                    result.add( new Integer( stroke + 1 ) );
                }
            }
        }
        
        if ( ((Integer)(result.get( result.size() -1 ))).intValue() != objects.size() ) {
            System.out.println( "getEndingIndices() -> got a partial object at the end" );
            result.add( new Integer( objects.size() ) );
        }
        
        return Util.arrayListToIntArray( result );
    }


    /**
    *
    * return object_ids for the input segmentation 
    * Caveat: THIS METHOD CURRENTLY CAN'T DEAL WITH TWO CONSECUTIVE 
    * OBJECTS OF THE SAME TYPE (i.e. doesn't repeat object id for multiple
    * consecutive occurences. This must be fixed if callee requires object
    * ids to be repeated. )
    *
    **/
    public static int[]
    getObjectIds( int in_segmentation[] )
    {
        ArrayList result = new ArrayList();

        int last_added = -1;
        for ( int i=0; i<in_segmentation.length; i++ ) {
            if ( in_segmentation[i] != last_added ) {
                result.add( new Integer( in_segmentation[i] ) );
                last_added = in_segmentation[i];
            }
        }
        
        return Util.arrayListToIntArray( result );
    }
}


/** 
  * 
  * $Log: VariableLengthDataManager.java,v $
  * Revision 1.1  2006-11-22 22:54:36  hammond
  * mit's drg code, will extract and refactor to get ladder code
  *
  * Revision 1.3  2005/01/27 22:15:18  hammond
  * organized imports
  *
  * Revision 1.2  2003/10/13 19:46:37  moltmans
  * Removed bad line endings.
  *
  * Revision 1.1  2003/06/17 21:18:13  mtsezgin
  * Adding  VariableLengthDataManager.java.
  *
  * Revision 1.1  2002/07/22 21:58:14  mtsezgin
  * *** empty log message ***
  *
  *  
  **/
