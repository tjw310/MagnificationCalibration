package com.mycompany.imagej;

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
    private int dX; // difference in x between two extreme x-locations of marker vector (if there exists 4 points)
    private int dXy; // difference in x between two extreme y-locations of marker vector (if there exists 4 points)
    private int dY; // difference in y between two extreme y-locations of marker vector (if there exists 4 points)
    private int dYx; // difference in y between two extreme x-locations of marker vector (if there exists 4 points)

    /** Creates a new instance of MarkerVector */
    public MarkerVector(int traceNumber) {
        super();
        this.traceNumber = traceNumber;
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
    
    
    public void getDeltaVariables() {
        if (this.size()==4){     
            ListIterator<Marker> itr = this.listIterator();
            int xMax = itr.next().getX(); itr.previous();
            int yMax = itr.next().getY();
            int xMaxY = xMax; int yMaxX = yMax;
            int yMin = yMax; int yMinX = yMax;
            int xMin = xMax; int xMinY = xMax;
            
        
            while (itr.hasNext()) {
                int xNext = itr.next().getX(); itr.previous();
                int yNext = itr.next().getY();
      
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
                    xMaxY = xNext;
                }
                if (yMin>yNext) {
                    yMin = yNext;
                    xMinY = xNext;
                }
            }
            this.dX = xMax-xMin;
            this.dXy = xMaxY-xMinY;
            this.dY = yMax-yMin;
            this.dYx = yMaxX-yMinX;

        }
        else{
            System.out.println("not 4 points");
        }
    }
}
