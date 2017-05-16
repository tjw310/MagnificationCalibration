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

    private float x;
    private float y;

    /** Creates a new instance of Marker */
    public Marker() {}
    
    /** @parameters float x,y location of marker, minimum: -windowSize/2, maximum: windowsSize/2 */
    public Marker(final float x, final float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public void setX(final int x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(final int y) {
        this.y = y;
    }

}
