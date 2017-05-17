package com.mycompany.imagej;

import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;

/*
 * #%L
 * Magnifcation Calibration Marker Point
 * %%
 * Copyright (C) 2017 - Thomas Watson, Imperial College London
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

// Created on May 8, 2017, 12:41 PM

import java.util.Vector;


/**
 * @author Thomas Watson
 * 
 **/
public class MarkerVector extends Vector<Marker> {
    
    private int traceNumber;
    private float dX; // difference in x between two extreme x-locations of marker vector (if there exists 4 points)
    private float dXy; // difference in x between two extreme y-locations of marker vector (if there exists 4 points)
    private float dY; // difference in y between two extreme y-locations of marker vector (if there exists 4 points)
    private float dYx; // difference in y between two extreme x-locations of marker vector (if there exists 4 points)
    private double averageY; //y position of trace (average);
    
    private GeneralPath tracePath = new GeneralPath();

    /** Creates a new instance of MarkerVector */
    public MarkerVector(int traceNumber) {
        super();
        this.traceNumber = traceNumber;
    }
    
    public MarkerVector(int traceNumber,int dX, int dY, int dXy, int dYx, double averageY) {
        super();
        this.traceNumber = traceNumber;
        this.dX = dX;
        this.dY = dY;
        this.dXy = dXy;
        this.dYx = dYx;
        this.averageY = averageY;
    }

    public void addMarker(final Marker marker) {
        if (this.size()<4) {
            add(marker); 
        }      
    }

    public Marker getMarker(final int n) {
        return get(n);
    }

    public int getVectorIndex(final Marker marker) {
        return indexOf(marker);
    }

    public void removeMarker(final int n) {
        remove(n);
    }

    public void removeLastMarker() {
        if (!this.isEmpty()) {
        super.removeElementAt(size() - 1);
        }
    }
    public int getTraceNumber() {
        return traceNumber;
    }

    public void setTraceNumber(final int traceNumber) {
        this.traceNumber = traceNumber;
    }
    
    public float getDx() {
        return this.dX;
    }
    
    public float getDy() {
        if (this.averageY<0 && this.dY>0) {
            return -1*this.dY;
        }
        else return this.dY;

    }
    
    public float getDxatMaxY() {
        return this.dXy;
    }
    
    public float getDyatMaxX() {
        if (this.averageY<0 && this.dYx>0) {
            return -1*this.dYx;
        }
        else return this.dYx;
    }
    
    public double getAverageYPosition(){
        ListIterator<Marker> itr = this.listIterator();
        double y = 0;
        while (itr.hasNext()) {
            y += itr.next().getY();
        }
        this.averageY = y/this.size();
        return this.averageY;
    }
    
    public void setAverageY(double y) {
        this.averageY = y;
    }
    
    /** Calculates the lengths between four point that compose MarkerVector
     * Only operate if there exist 4 points
     */
    public void getDeltaVariables() {
        if (this.size()==4){
            ListIterator<Marker> itr = this.listIterator();
            float xMax = itr.next().getX(); itr.previous();
            float yMax = itr.next().getY();
            float yMaxX = yMax;
            float yMin = yMax; 
            float yMinX = yMax;
            float xMin = xMax; 
            
        
            while (itr.hasNext()) {
                float xNext = itr.next().getX(); itr.previous();
                float yNext = itr.next().getY();
      
                if (xMax<xNext) {
                    xMax = xNext;
                    yMaxX = yNext;
                }
                if (xMin>xNext) {
                    xMin = xNext;
                    yMinX = yNext;
                }
                if (yMax<yNext) {
                    yMax = yNext;
                   // xMaxY = xNext;
                }
                if (yMin>yNext) {
                    yMin = yNext;
                   // xMinY = xNext;
                }
            }
            this.dX = xMax-xMin;
            this.dY = yMax-yMin;
            this.dYx = yMaxX-yMinX;
   
            if(this.dX==0) {
                this.dX=1;
            }
            else if(this.dY==0){
                this.dY = 1;
            }
            else if(this.dYx==0){
                this.dYx = 1;
            }
            
            //this.dXy = xMaxY-xMinY;
            //System.out.println(Integer.toString(xMaxY-xMinY));
            
            float dXy = (float) this.dY/(this.dX*this.dYx);
            this.dXy = dXy;
        }
    }
    
    /** Estimates source-detector distance
     * @param this.dY!=0 or returns NaN
     * @param opticCentre
     * @return source detector distance
     */
    public double getR(final double[] opticCentre) {
        if (this.dY==0) {
            return Double.NaN;
        }
        else {
            double R; double dY = this.dY; double dX = this.dX;
            double y = this.averageY;
            double a = 1/Math.sqrt(2);
            double b = dX*dX*(y-opticCentre[1])*(y-opticCentre[1])/(dY*dY)-opticCentre[0]*opticCentre[0];
            double c = (dY*dY+(y-opticCentre[1])*(y-opticCentre[1]))*
                    (dX*dX*(y-opticCentre[1])*(y-opticCentre[1])-dY*dY*opticCentre[0]*opticCentre[0])*
                    (dX*dX*(y-opticCentre[1])*(y-opticCentre[1])-dY*dY*opticCentre[0]*opticCentre[0]);
            double d = dY*dY*dY*dY*(y-opticCentre[1])*(y-opticCentre[1]);
            R = a*Math.sqrt(b+Math.sqrt(c/d));
            return R;
        }
    }
    
    /** gets source-detector distance when opticCentre = [0,0] */
    public double getR() {
        if (this.dY==0) {
            return Double.NaN;
        }
        else {
            double R; double dY = Math.abs(this.dY); double dX = this.dX;
            double y = this.averageY;
            double a = 1/Math.sqrt(2);
            double b = dX*dX*y*y/(dY*dY);
            double c = (dY*dY+y*y)*
                    dX*dX*y*y*
                    dX*dX*y*y;
            double d = dY*dY*dY*dY*y*y;
            R = a*Math.sqrt(b+Math.sqrt(c/d));
            return R;
        }
    }
    
    /** Gets radius of trace orbit based on this.dX, opticCentre, and local R value
     * 
     * @param opticCentre
     * @return double r, radius of trace orbit
     */
    public double getTraceRadius(final double[] opticCentre) {
        double R = this.getR(opticCentre);
        double dX = this.dX;
        if (opticCentre[0]!=0) {
            double r = Math.sqrt(dX*dX*R*R*R*R/(R*R*(dX*dX+2*(R*R+opticCentre[0]*opticCentre[0]))+2*Math.sqrt(R*R*R*R*(dX*dX*opticCentre[0]*opticCentre[0]+(R*R+opticCentre[0]*opticCentre[0])
                    *(R*R+opticCentre[0]*opticCentre[0])))));
            return r;
        }
        else {
            double r = Math.sqrt(dX*dX*R*R*R*R/(dX*dX*R*R+4*(R*R*R*R)));
            return r;
        }
    }
    
    /** Gets radius of trace orbit based on this.dX, opticCentre, and GLOBAL R
     * 
     * @param opticCentre
     * @return double r, radius of trace orbit
     */
    public double getTraceRadius(final double[] opticCentre, double R) {
        double dX = this.dX;
        if (opticCentre[0]!=0) {
            double r = Math.sqrt(dX*dX*R*R*R*R/(R*R*(dX*dX+2*(R*R+opticCentre[0]*opticCentre[0]))+2*Math.sqrt(R*R*R*R*(dX*dX*opticCentre[0]*opticCentre[0]+(R*R+opticCentre[0]*opticCentre[0])
                    *(R*R+opticCentre[0]*opticCentre[0])))));
            return r;
        }
        else {
            double r = Math.sqrt(dX*dX*R*R*R*R/(dX*dX*R*R+4*(R*R*R*R)));
            return r;
        }
    }
    
    /**Gets 2D trace as a GeneralPath object, to draw on ImageCanvas with Local R
     * @param opticCentre
     * @param numberPoints, number of points in trace to draw
     */
    public GeneralPath getTrace(double[] opticCentre, int numberPoints) {
        this.tracePath.reset();
        double R = this.getR(opticCentre);
        double r = this.getTraceRadius(opticCentre);
        double yPos = this.getAverageYPosition();
        for (int i=0;i<numberPoints;i++) {
            double angle = (double) i*(2*Math.PI)/numberPoints;
            double x = (r*R*Math.cos(angle)-R*opticCentre[0])/(R+r*Math.sin(angle))+opticCentre[0];
            double y = (R*yPos-R*opticCentre[1])/(R+r*Math.sin(angle))+opticCentre[1];
            if (i==0) this.tracePath.moveTo(x, y);
            else {
                this.tracePath.lineTo(x, y);
            }
        }
        this.tracePath.closePath();
        return this.tracePath;
    }
    
    /** Gets 2D trace as a GeneralPath object, to draw on ImageCanvas with Global R
     * @param opticCentre
     * @param numberPoints, number of points in trace to draw
     * @param R, global effective source-detector distance
     */
    public GeneralPath getTrace(double[] opticCentre, int numberPoints, double R) {
        this.tracePath.reset();
        double r = this.getTraceRadius(opticCentre,R);
        double yPos = this.getAverageYPosition();
        for (int i=0;i<numberPoints;i++) {
            double angle = (double) i*(2*Math.PI)/numberPoints;
            double x = (r*R*Math.cos(angle)-R*opticCentre[0])/(R+r*Math.sin(angle))+opticCentre[0];
            double y = (R*yPos-R*opticCentre[1])/(R+r*Math.sin(angle))+opticCentre[1];
            if (i==0) this.tracePath.moveTo(x, y);
            else {
                this.tracePath.lineTo(x, y);
            }
        }
        this.tracePath.closePath();
        return this.tracePath;
    }
    
    /**Finds two solutions of (x1,y1,x2,y2) coordinates in raw image, that correspond to coordinate (x,y)
     * in the transformed image
     * @param x, query x-position in transformed image
     * @param y, query y-position in transformed image
     * @param R
     * @param opticCentre
     * @param radius, query radius of trace orbit
     * @return length 4 double array containing two solutions for (x1,y1,x2,y2) coordinates of raw image
     */
    private double[] getDetectorCoordinateAtRadius(final double[] opticCentre,final double x,final double y,final double R, final double radius) {
        final double z = Math.sqrt(radius*radius-x*x);
        final double epsilonFirstSolution = (R*x-R*opticCentre[0])/(R+z)+opticCentre[0];
        final double epsilonSecondSolution = (R*x-R*opticCentre[0])/(R-z)+opticCentre[0];
        final double sigmaFirstSolution = (y-opticCentre[1])/(x-opticCentre[0])*(epsilonFirstSolution-opticCentre[0])+opticCentre[1];
        final double sigmaSecondSolution = (y-opticCentre[1])/(x-opticCentre[0])*(epsilonSecondSolution-opticCentre[0])+opticCentre[1];
        double[] outputCoordinates = {epsilonFirstSolution,sigmaFirstSolution,epsilonSecondSolution,sigmaSecondSolution};
        return outputCoordinates;
    }
    
    /**
     * 
     * @param opticCentre
     * @param x, query x-position in transformed image
     * @param y, query y-position in transformed image
     * @param R, effective source-detector distance
     * @param beadPixelSize, size of bead equivalent to range around bead radius that you want to transform
     * @return
     */
    public ArrayList<double[]> getDetectorLocations(final double[] opticCentre, final double x, final double y, final double R, final int beadPixelSize) {
        final double traceRadius = this.getTraceRadius(opticCentre, R);
        ArrayList<double[]> coordinateArray = new ArrayList<double[]>(1);
        for (int i=0; i<beadPixelSize; i++) {
            double radius = traceRadius-(double)(i-(beadPixelSize-1)/2);
            double[] coordinates = this.getDetectorCoordinateAtRadius(opticCentre, x, y, R, radius);
            coordinateArray.add(coordinates);
        }
        return coordinateArray;
    }
}
