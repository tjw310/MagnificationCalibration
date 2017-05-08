package com.mycompany.imagej;

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

    /** Creates a new instance of MarkerVector */
    public MarkerVector(int traceNumber) {
        super();
        this.traceNumber = traceNumber;
    }

    public void addMarker(final Marker marker) {
        if (this.size()>3) {
            throw new IllegalArgumentException("number of points cannot exceed 4");
        }
        add(marker);
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
        super.removeElementAt(size() - 1);
    }
    public int getTraceNumber() {
        return traceNumber;
    }

    public void setType(final int traceNumber) {
        this.traceNumber = traceNumber;
    }
}