/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
#include "edu_mit_sketch_toolkit_Timer.h"
#include <jni.h>
/* Header for class edu_mit_sketch_toolkit_Timer */
#include <stdio.h> 
#include <stdlib.h> 
#include <assert.h> 
#include <windows.h> 
#include <string.h> 
#include <memory.h> 
#include <process.h> 
#include <tchar.h>
#include <time.h>
#include <dos.h>
#include <conio.h>

/* Special globals for NT performance timers */
double freq;
long start;
int initialized = 0;

/* Initialize everything to 0 */
void sec_init(void)
{
	LARGE_INTEGER lFreq, lCnt;
	QueryPerformanceFrequency(&lFreq);
	freq = (double)lFreq.LowPart;
	/*freq = freq/10;*/
	QueryPerformanceCounter(&lCnt);
	start = lCnt.LowPart;
	initialized = 1;
}
/* return number of seconds since sec_init was called with
** a gross amount of detail
*/
long sec(void)
{
	LARGE_INTEGER lCnt;
	long tcnt;
	QueryPerformanceCounter(&lCnt);
 	tcnt = lCnt.LowPart - start;
 	return tcnt;/*/(freq);*/
}
/* End of NT special stuff */	


JNIEXPORT jlong JNICALL Java_edu_mit_sketch_toolkit_Timer_getTics
  (JNIEnv *d, jobject ds)
  {
	long time = 0l;
	if ( initialized == 0 ) {
		sec_init();
	}
	
	return (jlong)(sec());  
  }