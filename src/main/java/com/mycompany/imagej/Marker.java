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

/**
 *
 * @author Thomas Watson
 */
public class Marker {

    private int x;
    private int y;

    /** Creates a new instance of Marker */
    public Marker() {}
    
    /** @parameters int x,y location of marker, minimum 0, maximum: window size */
    public Marker(final int x, final int y) {
        if (x<0 || y<0) {
            throw new IllegalArgumentException("x,y should be greater than 0");
        }
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(final int x) {
        if (x<0){
            throw new IllegalArgumentException("x should be greater than 0");
        }
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(final int y) {
        if (y<0) {
         throw new IllegalArgumentException("x should be greater than 0");
    }
        this.y = y;
    }

}
