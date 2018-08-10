package com.christianbenner.crispinandroid.util;

/**
 * Created by Christian Benner on 17/11/2017.
 */

public class Logger {
    public static void errorf(String errorText, Object ... args)
    {
        System.out.printf("ERROR : " + errorText + "\n", args);
    }

    public static void debugf(String debugText, Object ... args)
    {
     //   if(LoggerConfig.ON)
     //   {
            System.out.printf("DEBUG : " + debugText + "\n", args);
      //  }
    }

    public static void informationf(String infoText, Object ... args) {
        System.out.printf("INFO  : " + infoText + "\n", args);
    }
}
