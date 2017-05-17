package com.mycompany.imagej;

import static org.junit.Assert.*;

import java.util.ArrayList;

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
        int traceNumber = 0;
        int dX = 1000;
        int dY = 250;
        int dXy = 0;
        int dYx = 0;
        double yPos = 2000;
        
        classMarkerVector = new MarkerVector(traceNumber,dX,dY,dXy,dYx,yPos);
        
        double opticCentre[] = {0,0};
        int Rguess= (int) Math.round(classMarkerVector.getR(opticCentre));
        int Rguess2 = (int) Math.round(classMarkerVector.getR());
        //System.out.println(Double.toString(Rguess));
        //System.out.println(Double.toString(Rguess2));
        assertEquals(Rguess, 8016);    
        assertEquals(Rguess2, 8016);    
        
        opticCentre[0] = 250; opticCentre[1] = 500;
        Rguess= (int) Math.round(classMarkerVector.getR(opticCentre));
        //System.out.println(Double.toString(Rguess));
        assertEquals(Rguess, 6015);
        
        opticCentre[0] = -800; opticCentre[1] = -1000;
        Rguess= (int) Math.round(classMarkerVector.getR(opticCentre));
        //System.out.println(Double.toString(Rguess));
        assertEquals(Rguess, 11984);
        
        classMarkerVector = new MarkerVector(traceNumber,dX,dY,dXy,dYx,-yPos);
        
        opticCentre[0] = 0; opticCentre[1] = 0;
        Rguess= (int) Math.round(classMarkerVector.getR(opticCentre));
        //System.out.println(Double.toString(Rguess));
        assertEquals(Rguess, 8016);    
        
        opticCentre[0] = 250; opticCentre[1] = 500;
        Rguess= (int) Math.round(classMarkerVector.getR(opticCentre));
        //System.out.println(Double.toString(Rguess));
        assertEquals(Rguess, 10009);
        
        opticCentre[0] = -800; opticCentre[1] = -1000;
        Rguess= (int) Math.round(classMarkerVector.getR(opticCentre));
        //System.out.println(Double.toString(Rguess));
        assertEquals(Rguess, 3949);
    }

    /** Test for calculation of trace radius, r **/
    @Test
    public void testGetTraceRadius() {
        int traceNumber = 0;
        int dX = 1000;
        int dY = 250;
        int dXy = 0;
        int dYx = 0;
        double yPos = 2000;
        
        classMarkerVector = new MarkerVector(traceNumber,dX,dY,dXy,dYx,yPos);
        double opticCentre[] = {0,0};
        double rGuess = classMarkerVector.getTraceRadius(opticCentre);
        String rGuessString = String.format("%.2f", rGuess);
        assertEquals(rGuessString, "499.03");   
        
        opticCentre[0] = 250; opticCentre[1] = -300;
        rGuess = classMarkerVector.getTraceRadius(opticCentre);
        rGuessString = String.format("%.2f", rGuess);
        assertEquals(rGuessString, "499.08"); 

        classMarkerVector.setAverageY(-2000);
        rGuess = classMarkerVector.getTraceRadius(opticCentre);
        rGuessString = String.format("%.2f", rGuess);
        assertEquals(rGuessString, "498.32"); 
        
    }
    
    @Test
    public void testGetDetectorCoordinates() {
        double opticCentre[] = {0,0};
        double y = 2000;
        double x = 250;
        int beadPixelSize = 1;
        int traceNumber = 0;
        int dX = 1000;
        int dY = 250;
        int dXy = 0;
        int dYx = 0;
        
        classMarkerVector = new MarkerVector(traceNumber,dX,dY,dXy,dYx,y);
        double R = classMarkerVector.getR(opticCentre);
        
        ArrayList<double[]> guesses = classMarkerVector.getDetectorLocations(opticCentre, x, y, R, beadPixelSize);
        double[] firstguess = guesses.get(0);
        String xGuess = String.format("%.2f", firstguess[0]);
        String yGuess = String.format("%.2f", firstguess[1]);
        assertEquals(xGuess,"237.22");
        assertEquals(yGuess,"1897.75");
        
        opticCentre[0] = 400; opticCentre[1] = -300;
        R =  classMarkerVector.getR(opticCentre);
        guesses = classMarkerVector.getDetectorLocations(opticCentre, x, y, R, beadPixelSize);
        firstguess = guesses.get(0);
        xGuess = String.format("%.2f", firstguess[0]);
        yGuess = String.format("%.2f", firstguess[1]);
        assertEquals(xGuess,"256.72");
        assertEquals(yGuess,"1896.98");
        
        y = -2000;
        classMarkerVector = new MarkerVector(traceNumber,dX,dY,dXy,dYx,y);
        R =  classMarkerVector.getR(opticCentre);
        guesses = classMarkerVector.getDetectorLocations(opticCentre, x, y, R, beadPixelSize);
        firstguess = guesses.get(0);
        xGuess = String.format("%.2f", firstguess[0]);
        yGuess = String.format("%.2f", firstguess[1]);
        assertEquals(xGuess,"258.92");
        assertEquals(yGuess,"-1898.88");
    }
    
}
