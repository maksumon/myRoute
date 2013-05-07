package com.maksumon.myroute;

/**
 * Created with IntelliJ IDEA.
 * User: MAKSumon
 * Date: 5/8/13
 * Time: 1:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class Utility {

    public static double roundNumbers(double number){

        double p = Math.pow(10,2);
        number = number * p;
        number = Math.round(number);

        return number/p;
    }
}
