package com.mycompany.imagej;

//import static org.junit.Assert.*;

import org.junit.Test;

public class MarkerTest extends Marker {
    
    private Marker classMarker;
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor() {
        classMarker = new Marker(-5,0);
        classMarker = new Marker(0,-5);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSetX() {
        classMarker = new Marker();
        classMarker.setX(-5);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSetY() {
        classMarker = new Marker();
        classMarker.setY(-5);
    }
}
