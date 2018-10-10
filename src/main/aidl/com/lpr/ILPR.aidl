// ILPR.aidl
package com.lpr;

// Declare any non-default types here with import statements

interface ILPR {

   int Init(int roileft,int roitop,int roiright,int roibottom,int nwidth,int nheight);
   byte[]  VideoRec(int width, int height,int imgflag);
   int Release();
   boolean canUse();
}
