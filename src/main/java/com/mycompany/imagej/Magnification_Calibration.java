package com.mycompany.imagej;


import ij.plugin.frame.PlugInFrame;

/**
 * @author Thomas Watson
 */
public class Magnification_Calibration extends PlugInFrame {

    /** Creates a new instance of Cell_Counter */
    public Magnification_Calibration() {
        super("Magnification Calibration");
        new MagnificationCalibration();
    }

    @Override
    public void run(final String arg) {}

}
