package com.mycompany.imagej;

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

}
