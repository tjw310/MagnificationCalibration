package com.mycompany.imagej;

import static org.junit.Assert.*;

import org.junit.Test;

public class MarkerVectorTest {
    
    private Marker classMarker;
    private MarkerVector classMarkerVector;
    
    /** Test for adding a marker to a complete vector (i.e size of vector is greater than 4) **/
    @Test
    public void testAddMarker() {
        classMarker = new Marker();
        classMarkerVector = new MarkerVector(0);
        for(int i=0;i<10;i++){
            classMarkerVector.addMarker(classMarker);
        }
    }
    
    /** Calculation of variable R **/
    @Test
    public void testR() {
        classMarkerVector = new MarkerVector(0,1000,250,0,0,2000);
        double opticCentre[] = {0,0};
        int Rguess= (int) Math.round(classMarkerVector.getR(opticCentre));
        System.out.println(Double.toString(Rguess));
        assertEquals(Rguess, 8016);    
        
        opticCentre[0] = 250; opticCentre[1] = 500;
        Rguess= (int) Math.round(classMarkerVector.getR(opticCentre));
        System.out.println(Double.toString(Rguess));
        assertEquals(Rguess, 6015);
        
        opticCentre[0] = -800; opticCentre[1] = -1000;
        Rguess= (int) Math.round(classMarkerVector.getR(opticCentre));
        System.out.println(Double.toString(Rguess));
        assertEquals(Rguess, 11984);
    }

}
