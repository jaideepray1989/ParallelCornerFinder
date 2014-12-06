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
  * $Id: DataManager.java,v 1.1 2006-11-22 22:54:35 hammond Exp $     
  * $Name:  $   
  * $Locker:  $
  * $Source: /cvsroot/tamudrg/code/src/edu/mit/sketch/toolkit/DataManager.java,v $
  *  
  **/

import edu.mit.sketch.geom.Arc;
import edu.mit.sketch.geom.Ellipse;
import edu.mit.sketch.geom.GeneralPath;
import edu.mit.sketch.geom.GeometricObject;
import edu.mit.sketch.geom.Line;
import edu.mit.sketch.geom.Polygon;
import edu.mit.sketch.geom.Rectangle;
import edu.mit.sketch.util.Util;
import java.util.ArrayList;

/**
  *
  * This class manages the training data.
  *
  **/

public
class DataManager
{
    ArrayList input_objects;
    int       segmentation[]; 
    public int input_lengths[];
    public int class_ranges[];
    ArrayList  reorganized_training_data[];
    

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
    **/
    public
    DataManager( ArrayList input_objects, 
                 int       ending_indices[], 
                 int       object_ids[],
                 int       number_of_classes )
    {
        this.input_objects = input_objects;
    
        class_ranges = new int[number_of_classes];
        for ( int i=0; i<number_of_classes; i++ ){
            class_ranges[i] = i+1;
        }

        Util.printArray( class_ranges,   "class_ranges_before" );
        Util.printArray( object_ids,     "object_ids" );
        Util.printArray( ending_indices, "ending_indices" );

        segmentation = new int[input_objects.size()];
        for ( int i=0; i<ending_indices.length; i++ ) {
            for ( int j = (i==0) ? 0 : ending_indices[i-1]; j<ending_indices[i]; j++ ) {
                segmentation[j] = object_ids[i];
            }
        }
    
        reorganizeTrainingData( prepareTrainingData( number_of_classes, ending_indices, object_ids ) );
        Util.printArray( class_ranges,   "class_ranges_after" );
    }


    /**
    *
    * The constructor.
    *
    **/
    public
    DataManager( ArrayList input_objects, 
                 int       segmentation[], 
                 int       input_lengths[],
                 int       class_ranges[] )
    {
        this.input_objects = input_objects;
        this.segmentation  = segmentation;
        this.input_lengths = input_lengths;
        this.class_ranges  = class_ranges;
        
        reorganizeTrainingData( prepareTrainingData() );
    }

    /**
    *
    * The constructor, merger data from input managers
    *
    **/
    public
    DataManager( DataManager manager1, 
                 DataManager manager2 )
    {
        this.input_objects = null;
        this.segmentation  = null;
        
        // manager1.class_ranges.length == manager2.class_ranges.length
        ArrayList merged_input_lengths[] = new ArrayList[manager1.class_ranges.length];
        
        for ( int i=0; i<merged_input_lengths.length; i++ ) {
            merged_input_lengths[i] = new ArrayList();
            for ( int j = ( i==0) ? 0 : manager1.class_ranges[i-1]; j<manager1.class_ranges[i]; j++ ) {  
                if ( manager1.input_lengths[j] !=0  ) {
                    merged_input_lengths[i].add( new Integer( manager1.input_lengths[j] ) );
                    System.out.println( "Adding input length " + manager1.input_lengths[j] );
                }
            }
            
            for ( int j = ( i==0) ? 0 : manager2.class_ranges[i-1]; j<manager2.class_ranges[i]; j++ ) {
                if ( !containsNumber( merged_input_lengths[i], manager2.input_lengths[j] ) &&
                      manager2.input_lengths[j] != 0 ) {
                    merged_input_lengths[i].add( new Integer( manager2.input_lengths[j] ) );
                    System.out.println( "Adding input length " + manager2.input_lengths[j] );
                }
            }
        }
        
        class_ranges = new int[manager1.class_ranges.length];
        int previous_length    = 0;
        for ( int index=0; index<class_ranges.length; index++ ) {
            // System.out.println( "class_lengths[index].size()" + class_lengths[index].size() );
            class_ranges[index] = merged_input_lengths[index].size() + previous_length;
            previous_length = class_ranges[index];
        }
        
        input_lengths = getLengthsArray( merged_input_lengths, 
                                         class_ranges );

        reorganized_training_data = new ArrayList[input_lengths.length];
        for ( int i=0; i<reorganized_training_data.length; i++ ) {
            reorganized_training_data[i] = new ArrayList();
        }
        
        Util.printArray( class_ranges, "merged_class_ranges" );
        Util.printArray( input_lengths,"merged_input_lengths" );

        DataManager managers[] = {manager1, manager2};

        for ( int manager_id=0; manager_id<2; manager_id++ ) {
            for ( int index=0; index<managers[manager_id].class_ranges.length; index++ ) {
            
                for ( int i = ( index == 0 ) ? 0 : managers[manager_id].class_ranges[index-1]; i<managers[manager_id].class_ranges[index]; i++ ) {
                    ArrayList all_data_for_ith_hmm = (ArrayList)managers[manager_id].reorganized_training_data[i];
    
                    for ( int j=0; j<all_data_for_ith_hmm.size(); j++ ) {
                        ArrayList a_data_for_ith_hmm = (ArrayList)all_data_for_ith_hmm.get( j );
                        int data_length = a_data_for_ith_hmm.size();
                        if ( data_length != 0 ) {
                            int HMM_number  = getHMMNumberInNewScheme( class_ranges, index, data_length, input_lengths );
                    
                            reorganized_training_data[HMM_number].add( a_data_for_ith_hmm );
                        }
                    }
                }
            }
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
    * return ending_indices for segmented sketch objects
    *
    **/
    public static int[]
    getEndingIndices( int in_segmentation[], int in_input_lengths[], ArrayList objects )
    {
        ArrayList result       = new ArrayList();
        ArrayList observations = new ArrayList();
        
        int length = 0;
        int i      = 0;
        for ( int stroke=0; stroke<objects.size(); stroke++ ) {
            observations.addAll( getEncodingAtIndex2( objects, stroke ) );
            i = observations.size()-1;
            
            System.out.println( "i = " + i );
            System.out.println( "stroke = " + stroke );
            System.out.println( "length = " + length );
            System.out.println( "segmentation[i] = " + in_segmentation[i] );
            System.out.println( "input_lengths[segmentation[i]] = " + in_input_lengths[in_segmentation[i]] );
            if ( observations.size() == length + in_input_lengths[in_segmentation[i]] ) {
                length += in_input_lengths[in_segmentation[i]];
                
                System.out.println( "Adding " + ( stroke + 1 ) + "\n" );
                result.add( new Integer( stroke + 1 ) );
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
    *
    **/
    public static int[]
    getObjectIds( int in_segmentation[], int in_input_lengths[], int in_class_ranges[] )
    {
        ArrayList result = new ArrayList();

        for ( int i=0; i<in_segmentation.length; i += in_input_lengths[in_segmentation[i]] ) {
            result.add( new Integer( DataManager.getObjectIdForHMM( in_segmentation[i], in_class_ranges ) ) );
        }
        
        return Util.arrayListToIntArray( result );
    }


    /**
    *
    * return object_ids for segmented sketch objects
    *
    **/
    public static int
    getObjectIdForHMM( int hmm_number, int in_class_ranges[] )
    {
        if ( hmm_number < in_class_ranges[0] ) {
            return 0;
        }
        
        for ( int i=1; i<in_class_ranges.length; i++ ) {
            if ( ( in_class_ranges[i-1] <= hmm_number ) &&
                 ( in_class_ranges[i]   >  hmm_number ) ) {
                return i;     
            }
        }
        
        System.out.println( "INVARIANT VIOLATED IN getObjectIdForHMM( , ) for hmm " + hmm_number );
        
        return -1;
    }


    /**
    *
    * return object_ids for segmented sketch objects
    *
    **/
    public int
    getObjectIdForHMM( int hmm_number )
    {
        if ( hmm_number < class_ranges[0] ) {
            return 0;
        }
        
        for ( int i=1; i<class_ranges.length; i++ ) {
            if ( ( class_ranges[i-1] <= hmm_number ) &&
                 ( class_ranges[i]   >  hmm_number ) ) {
                return i;     
            }
        }
        
        System.out.println( "INVARIANT VIOLATED IN getObjectIdForHMM for hmm " + hmm_number );
        
        return -1;
    }
            
    
    /**
    *
    * Return training data using new encoding.
    * The returned training_data is a triply embedded ArrayList whose ultimate
    * data is of type Integer. Length of training_data is the same as the 
    * number of hmms in the original encoding scheme.
    *
    * number_of_classes is the number of actual sketch object classes (ie. rectangles, stick figures...)
    * Same as class_ranges.length.
    * 
    **/    
    private ArrayList
    prepareTrainingData( int number_of_classes, int ending_indices[], int object_ids[] )
    {
        // Each entry in the training_data is an ArrayList of 
        // ArrayLists of equal length, corresponding to the encoding
        // of an object in the new format
        // input_lengths keeps the input lengths for each 
        // hmm using the original encoding.
        // training_data is a triply embedded ArrayList whose ultimate
        // data is of type Integer
        ArrayList training_data = new ArrayList();
        
        for ( int i=0; i<number_of_classes; i++ ) {
            ArrayList all_data_for_ith_hmm = new ArrayList();
            for ( int j=0; j<ending_indices.length; j++ ) {
                ArrayList a_data_for_ith_hmm   = new ArrayList();

                for ( int k = (j==0) ? 0 : ending_indices[j-1]; k<ending_indices[j]; k++ ) {
                    if ( i == object_ids[j] ) {
                        a_data_for_ith_hmm.addAll( 
                            getEncodingAtIndex2( input_objects, k ) );
                    }
                }
                all_data_for_ith_hmm.add( a_data_for_ith_hmm );
            }
            training_data.add( all_data_for_ith_hmm );
        }
        
        return training_data;
    }
    
        
    
    /**
    *
    * Return training data using new encoding.
    * The returned training_data is a triply embedded ArrayList whose ultimate
    * data is of type Integer. Length of training_data is the same as the 
    * number of hmms in the original encoding scheme.
    *
    **/    
    private ArrayList
    prepareTrainingData()
    {
        // Each entry in the training_data is an ArrayList of 
        // ArrayLists of equal length, corresponding to the encoding
        // of an object in the new format
        // input_lengths keeps the input lengths for each 
        // hmm using the original encoding.
        // training_data is a triply embedded ArrayList whose ultimate
        // data is of type Integer
        ArrayList training_data = new ArrayList();
        for ( int i=0; i<input_lengths.length; i++ ) {
            ArrayList all_data_for_ith_hmm = new ArrayList();
            ArrayList a_data_for_ith_hmm   = new ArrayList();
            int       count                = 0;
            for ( int j=0; j<segmentation.length; j++ ) {
                if ( j > 0                 && 
                     segmentation[j-1] == i && 
                     count >= input_lengths[i] ) {
                    all_data_for_ith_hmm.add( a_data_for_ith_hmm );
                    a_data_for_ith_hmm   = new ArrayList();
                    count = 0;
                }
                if ( segmentation[j] == i ) {
                    a_data_for_ith_hmm.addAll( 
                        getEncodingAtIndex2( input_objects, j ) );
                    count++;
                }
            }
            all_data_for_ith_hmm.add( a_data_for_ith_hmm );
            training_data.add( all_data_for_ith_hmm );
        }
        
        return training_data;
    }

    /**
    *
    * Return training data in a new format using and input_objects 
    * segmentation 
    *
    **/    
    private void
    reorganizeTrainingData( ArrayList nonseparated_training_data )
    {
        ArrayList class_lengths[] = new ArrayList[class_ranges.length];

        for ( int i=0; i<class_lengths.length; i++ ) {
            class_lengths[i] = new ArrayList();
        }        
        
        // compute the class_lengths
        for ( int index=0; index<class_lengths.length; index++ ) {
            for ( int i = ( index == 0 ) ? 0 : class_ranges[index-1]; i<class_ranges[index]; i++ ) {
                ArrayList all_data_for_ith_hmm = (ArrayList)nonseparated_training_data.get(i);
                for ( int j=0; j<all_data_for_ith_hmm.size(); j++ ) {
                    ArrayList a_data_for_ith_hmm = (ArrayList)all_data_for_ith_hmm.get( j );
                    int data_length = a_data_for_ith_hmm.size();
                    
                    if ( ( !containsNumber( class_lengths[index], data_length ) ) && 
                         ( data_length != 0                                     ) ) { // Don't count 0
                        class_lengths[index].add( new Integer( data_length ) );
                        System.out.println( "Adding new class length of " + data_length + " for object "  + index );
                    }
                }
            }
        }
        
        // unpack the class_lengths to full size
        for ( int index=0; index<class_lengths.length; index++ ) {
            if ( class_lengths[index].size() == 0 )
                class_lengths[index].add( new Integer( 0 ) );
        }
        
        // compute class_ranges
        int new_class_ranges[] = new int[class_ranges.length];
        int previous_length    = 0;
        for ( int index=0; index<class_ranges.length; index++ ) {
            System.out.println( "class_lengths[index].size()" + class_lengths[index].size() );
            new_class_ranges[index] = class_lengths[index].size() + previous_length;
            previous_length = new_class_ranges[index];
        }
        // Compute class_lengths as an array[]
        int lengths_array[] = getLengthsArray( class_lengths, 
                                               new_class_ranges );

        Util.printArray( new_class_ranges, "new_class_ranges" );
        Util.printArray( lengths_array,    "lengths_array" );

        reorganized_training_data = new ArrayList[lengths_array.length];
        for ( int i=0; i<reorganized_training_data.length; i++ ) {
            reorganized_training_data[i] = new ArrayList();
        }
        
        for ( int index=0; index<class_ranges.length; index++ ) {
        
            for ( int i = ( index == 0 ) ? 0 : class_ranges[index-1]; i<class_ranges[index]; i++ ) {
                ArrayList all_data_for_ith_hmm = (ArrayList)nonseparated_training_data.get(i);  // in old scheme

                for ( int j=0; j<all_data_for_ith_hmm.size(); j++ ) {
                    ArrayList a_data_for_ith_hmm = (ArrayList)all_data_for_ith_hmm.get( j );
                    int data_length = a_data_for_ith_hmm.size();
                    if ( data_length != 0 ) {
                        int HMM_number  = getHMMNumberInNewScheme( new_class_ranges, index, data_length, lengths_array );
                    
                        reorganized_training_data[HMM_number].add( a_data_for_ith_hmm );
                    }
                }
            }
        }
        
        class_ranges  = new_class_ranges;
        input_lengths = lengths_array;
    }
    
    
    /**
    *
    * Return an array that holds the lengths for HMMs' data.
    *
    **/    
    private int
    getHMMNumberInNewScheme( int new_class_ranges[], int index, int data_length, int lengths_array[] )
    {
        //System.out.println( "looking for HMM number for object " + index + 
        //                    " with data_length "                 + data_length +
        //                    " with new_class_ranges and lengths_array " );
        for ( int i = ( index == 0 ) ? 0 : new_class_ranges[index-1]; i<new_class_ranges[index]; i++ ) {
            if ( lengths_array[i] == data_length ) {
                //System.out.println( "Returning " + i );
                return i;
            }
        }
        
        System.out.println( "FATAL ERROR: INVARIANT VIOLATED!!!" );
        return -1;
    }
    
    
    /**
    *
    * Return an array that holds the lengths for HMMs' data.
    *
    **/    
    private int[]
    getLengthsArray( ArrayList class_lengths[], int new_class_ranges[] )
    {
        int lengths_array[] = new int[new_class_ranges[new_class_ranges.length-1]];
        
        int index = 0; 
        for ( int i=0; i<class_lengths.length; i++ ) {
            ArrayList class_lengths_i = class_lengths[i];
            for ( int j=0; j<class_lengths_i.size(); j++ ) {
                lengths_array[index] = ((Integer)class_lengths_i.get( j )).intValue();
                index++;
            }
        }
        
        return lengths_array;
    }

    
    /**
    *
    * Return true if number is in the list as an Integer
    *
    **/    
    public static boolean
    containsNumber( ArrayList list, int number )
    {
        for ( int i=0; i<list.size(); i++ ) {
            if ( ((Integer)list.get(i)).intValue() == number )
                return true;
        }
        
        return false;
    }
    
    
    /**
    *
    * Get the encoding symbols for the input object at the input 
    * position
    *
    **/    
    public static String
    getEncoding2( ArrayList input_objects )
    {
        ArrayList observations = new ArrayList();
        for ( int i=0; i<input_objects.size(); i++ ) {
            observations.addAll( getEncodingAtIndex2( input_objects, i ) );
        }
        
        String result = "";
        for ( int i=0; i<observations.size(); i++ ) {
            result += ((Integer)observations.get(i)).intValue() + " ";
        }
        
        return result;        
    }
    
    
    /**
    *
    * Get the encoding symbols for the input object at the input 
    * position
    *
    **/    
    public static ArrayList
    getEncodingAtIndex( ArrayList input_objects, 
                        int       position  )
    {
        Object    object;
        ArrayList observation = new ArrayList();
        
        object = input_objects.get( position );

        if ( object instanceof Line ) {
            Line line = (Line)object;
            double x = line.x2 - line.x1;
            double y = line.y2 - line.y1;
            
            double angle = Math.atan2( y, x );
            
            if ( ( angle >  7*Math.PI/18 ) &&
                 ( angle <  11*Math.PI/18 ) ) {
                observation.add( new Integer( 3 ) );
                return observation;
            }
            if ( ( angle <  -7*Math.PI/18 ) &&
                 ( angle >  -11*Math.PI/18 ) ) {
                observation.add( new Integer( 3 ) );
                return observation;
            }
            if ( ( angle <  2*Math.PI/18 ) &&
                 ( angle > -2*Math.PI/18 ) ) {
                observation.add( new Integer( 4 ));
                return observation;
            }
            if ( ( angle >  16*Math.PI/18 ) ||
                 ( angle < -16*Math.PI/18 ) ) {
                observation.add( new Integer( 4 ));
                return observation;
            }
            if ( x*y >= 0 ) {
                observation.add( new Integer( 2 ));  // 1 and 2 swapped due to the flip in y axis
                return observation;
            } else {
                observation.add( new Integer( 1 )); // 1 and 2 swapped due to the flip in y axis
                return observation;
            }
        }
        if ( object instanceof Arc ) {
            Rectangle bounds = ((Arc)object).getRectangularBounds();
            if ( bounds.getWidth()/bounds.getHeight() > 1.33 ) {
                observation.add( new Integer( 6 ));
                return observation;
            }
            if ( bounds.getWidth()/bounds.getHeight() < 1.0/1.33 ) {
                observation.add( new Integer( 7 ));
                return observation;
            }
            observation.add( new Integer( 5 ) );
            return observation;
	}
        if ( object instanceof Ellipse ) {
            Rectangle bounds = ((Ellipse)object).getRectangularBounds();
            if ( bounds.getWidth()/bounds.getHeight() > 1.33 ) {
                observation.add( new Integer( 6 ));
                return observation;
            }
            if ( bounds.getWidth()/bounds.getHeight() < 1.0/1.33 ) {
                observation.add( new Integer( 7 ));
                return observation;
            }
            observation.add( new Integer( 5 ) );
            return observation;
        }
        if ( object instanceof GeneralPath ) {
            observation.add( new Integer( 8 ));
            return observation;
        }
        if ( object instanceof Polygon ) {
            Polygon polygon = (Polygon)object;
            if ( polygon.npoints == 3 ) {
                observation.add( new Integer( 9 ));
                return observation;
            }
            if ( polygon.npoints == 4 ) {
                observation.add( new Integer( 10 ));
                return observation;
            }
            if ( polygon.npoints == 5 ) {
                observation.add( new Integer( 11 ));
                return observation;
            }
            if ( polygon.npoints == 6 ) {
                observation.add( new Integer( 12 ));
                return observation;
            }
            if ( polygon.npoints >= 7 ) {
                observation.add( new Integer( 13 ));
                return observation;
            }
        }
        
        
        return observation;
    }
    
    /**
    *
    * Get the encoding for the input object.
    *
    **/    
    public static int
    getEncoding( Object input_object )
    {
        Object    object;
        ArrayList observation = new ArrayList();
        
        object = input_object;

        if ( object instanceof Line ) {
            Line line = (Line)object;
            double x = line.x2 - line.x1;
            double y = line.y2 - line.y1;
            
            double angle = Math.atan2( y, x );
            
            if ( ( angle >  7*Math.PI/18 ) &&
                 ( angle <  11*Math.PI/18 ) ) {
                return 3;
            }
            if ( ( angle <  -7*Math.PI/18 ) &&
                 ( angle >  -11*Math.PI/18 ) ) {
                return 3;
            }
            if ( ( angle <  2*Math.PI/18 ) &&
                 ( angle > -2*Math.PI/18 ) ) {
                return 4;
            }
            if ( ( angle >  16*Math.PI/18 ) ||
                 ( angle < -16*Math.PI/18 ) ) {
                return 4;
            }
            if ( x*y >= 0 ) {
		return 2; // 1 and 2 swapped due to the flip in y axis
            } else {
		return 1; // 1 and 2 swapped due to the flip in y axis
            }
        }
        if ( object instanceof Arc ) {
            Rectangle bounds = ((Arc)object).getRectangularBounds();
            if ( bounds.getWidth()/bounds.getHeight() > 1.33 ) {
                return 6;
            }
            if ( bounds.getWidth()/bounds.getHeight() < 1.0/1.33 ) {
                return 7;
            }
            return 5;
	}
        if ( object instanceof Ellipse ) {
            Rectangle bounds = ((Ellipse)object).getRectangularBounds();
            if ( bounds.getWidth()/bounds.getHeight() > 1.33 ) {
		return 6;
            }
            if ( bounds.getWidth()/bounds.getHeight() < 1.0/1.33 ) {
                return 7;
            }
            return 5;
        }
        if ( object instanceof GeneralPath ) {
            return 8;
        }
        if ( object instanceof Polygon ) {
            Polygon polygon = (Polygon)object;
            if ( polygon.npoints == 3 ) {
                return 9;
            }
            if ( polygon.npoints == 4 ) {
                return 10;
            }
            if ( polygon.npoints == 5 ) {
                return 11;
            }
            if ( polygon.npoints == 6 ) {
                return 12;
            }
            if ( polygon.npoints >= 7 ) {
                return 13;
            }
        }        
        
        return -1;
    }
    
    
    /**
    *
    * Get the encoding for the input object.
    *
    **/    
    public static int[]
    getEncodingWithSize( Object input_object )
    {
        Object    object;
        ArrayList observation = new ArrayList();
        
	int result[];
	result = new int[2];

	result[0] = getEncoding( input_object );

        object = input_object;

        if ( object instanceof Line ) {
	    result[1] = (int)((Line)object).length();
        } else {
            Rectangle bb = ((GeometricObject)object).getRectangularBounds();
	    result[1] = bb.getWidth() > bb.getHeight() ? 
		        (int)bb.getWidth() : (int)bb.getHeight();
	}
	
	return result;
    }
    
    
    /**
    *
    * Get the encoding symbols for the input object at the input 
    * position. A different encoding scheme
    *
    **/    
    private static ArrayList
    getEncodingAtIndex3( ArrayList input_objects, 
                        int        position  )
    {
        Object    object;
        Object    previous_object;
        ArrayList observation;
        
        if ( position == 0 ) {
            return getEncodingAtIndex( input_objects, position );
        }
        
        object          = input_objects.get( position );
        previous_object = input_objects.get( position - 1 );
        
        observation     = getEncodingAtIndex( input_objects, position ); 
        
        if ( ((GeometricObject)object).touches( (GeometricObject)previous_object ) ) {
            observation.add( new Integer( 14 ) );
        }
        
        return observation;
    }
    
    
    /**
    *
    * Get the encoding symbols for the input object at the input 
    * position. A different encoding scheme
    *
    **/    
    public static ArrayList
    getEncodingAtIndex2( ArrayList input_objects, 
                        int        position  )
    {
        return getEncodingAtIndex3( input_objects, position );
    }
}


/** 
  * 
  * $Log: DataManager.java,v $
  * Revision 1.1  2006-11-22 22:54:35  hammond
  * mit's drg code, will extract and refactor to get ladder code
  *
  * Revision 1.6  2005/03/08 21:07:34  mtsezgin
  * Fixed bugs in SimpleClassifier2.java SimpleClassifier3.java
  *
  * Added features to StrokeData.java DataManager.java ObjectManager.java
  *
  * Revision 1.5  2005/01/27 22:15:12  hammond
  * organized imports
  *
  * Revision 1.4  2003/10/13 19:46:37  moltmans
  * Removed bad line endings.
  *
  * Revision 1.3  2003/05/07 20:58:54  mtsezgin
  *
  * Fixed some problems with the arc recognition in general, and recognition
  * on the acer tablet in particular.
  *
  * Revision 1.2  2003/03/06 01:08:51  moltmans
  * Added copyright to all the files.
  *
  * Revision 1.1  2002/07/22 21:58:14  mtsezgin
  * *** empty log message ***
  *
  *  
  **/
