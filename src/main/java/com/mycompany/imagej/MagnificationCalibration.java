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

import ij.CompositeImage; 
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.gui.StackWindow;
import ij.gui.Plot;
import ij.gui.PlotWindow;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import net.imagej.ImageJ;
import ij.process.ImageProcessor;

import java.io.File;
import java.nio.file.*;
import java.io.IOException;

import net.imagej.Dataset;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import com.kenai.jffi.Array;

import groovyjarjarantlr.collections.List;
import io.scif.services.DatasetIOService;
import javassist.bytecode.Descriptor.Iterator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;

import org.scijava.Context;
import org.scijava.command.CommandService;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.jruby.ir.instructions.Match3Instr;
import org.junit.Ignore;

/**
 *  Adapted from CellCounter Imagej plugin
 *
 * @author Thomas Watson
 */
@Plugin(type = Command.class)
public class MagnificationCalibration extends JFrame implements ActionListener, PlugInFilter
{  
    /** @string Button labels **/
    private static final String ADD = "Add";
    private static final String REMOVE = "Remove";
    private static final String INITIALIZE = "Initialize";
    private static final String DELETE = "Delete";
    private static final String TYPE_COMMAND_PREFIX = "trace";
    private static final String ANALYSE = "Analyse";
    private static final String LOAD_TEST_IMAGE = "Load Test Image";
    private static final String DRAW_TRACES = "Draw Elliptical Traces";
    
    /** @Vector variable arrays for the trace (MarkerVector) and associated radiobuttons (JRadioButton) **/
    private Vector<MarkerVector> typeVector;
    private Vector<JRadioButton> dynRadioVector;
    
    /**@MarkerVector represents the current selected trace to operate on
     * @int trace numbers range from 1-
     */
    private MarkerVector currentMarkerVector;
    private int currentMarkerIndex;
    
    /** @ImagePlus copy of image you want to use for calibration
     * @MagCalImageCanvas associated ImageCanvas for calibImg
     */
    private MagCalImageCanvas ic;
    private ImagePlus calibImg;

    /** GUI split into 3 parts: fixed control buttons (statButtonPanel), trace names and numbers 
     * (dynButtonPanel) and associated and control buttons (dynButtonPanel)
     * @JButton fixed control buttons
     * @ButtonGroup radiobuttons that represent the traces are grouped together
     * @GridLayout the dynamic elements are arranged in a grid format
     */
    private JPanel dynPanel;
    private JPanel dynButtonPanel;
    private JPanel statButtonPanel;
    private JButton addButton;
    private JButton removeButton;
    private JButton initializeButton;
    private JButton deleteButton;
    private JButton analyseButton;
    private JButton loadTestImageButton;
    private JButton drawTracesButton;
    private ButtonGroup radioGrp;
    private GridLayout dynGrid;
    
    /**@double[] represents optic centre of image (position of optic axis)
     * @int bin factor of image if original is large
     */
    private double[] opticCentre = new double[2];
    private int binFactor = 1;
    private double effectiveSourceDistance = 0;
    
    /**@Plot ImageJ Plot class objects for data plotting and analysis
     */
    private PlotWindow opticYplot;
    private PlotWindow opticXplot;
    private PlotWindow effectiveRplot;
    
    
    
    /** Plugin setup method defining what the plugin can operate on
     * in this case 8,16,32 Bit images, RGS and also that no image is required 
     * initially to run
     */
    @Override
    public int setup(String arg, ImagePlus imp) {
        if (arg.equals("about")) {
            showAbout();
            return DONE;
        }

        //image = imp;
        return DOES_8G | DOES_16 | DOES_32 | DOES_RGB | NO_IMAGE_REQUIRED;
    }
    
    /** Displays message about plugin*/
    public void showAbout() {
        IJ.showMessage("Magnification Calibration",
            "Calculate the Non-Telecentric Effective Source-Detector Distance"
        );
    }
    
    /** Method that runs plugin */
    public void run(ImageProcessor ip) {
        setResizable(false);
        typeVector = new Vector<MarkerVector>();
        dynRadioVector = new Vector<JRadioButton>();
        initGUI();
    }

    /** Show the GUI threadsafe */
    private static class GUIShower implements Runnable {

        final JFrame jFrame;

        public GUIShower(final JFrame jFrame) {
            this.jFrame = jFrame;
        }

        @Override
        public void run() {
            jFrame.pack();
            jFrame.setLocation(500, 200);
            jFrame.setVisible(true);
        }
    }
    
    /** creates GUI 
     * uses - makeDynRadioButton, makeButton, GUIshower methods
     * */
    private void initGUI() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        final GridBagLayout gb = new GridBagLayout();
        getContentPane().setLayout(gb);

        radioGrp = new ButtonGroup();// to group the radiobuttons

        dynGrid = new GridLayout(1, 1);
        dynGrid.setVgap(2);

        // this panel will keep the dynamic GUI parts
        dynPanel = new JPanel();
        dynPanel.setBorder(BorderFactory.createTitledBorder("Counters"));
        dynPanel.setLayout(gb);

        // this panel keeps the radiobuttons
        dynButtonPanel = new JPanel();
        dynButtonPanel.setLayout(dynGrid);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gb.setConstraints(dynButtonPanel, gbc);
        dynPanel.add(dynButtonPanel);

        gb.setConstraints(dynPanel, gbc);
        getContentPane().add(dynPanel);
        

        // create a "static" panel to hold control buttons
        statButtonPanel = new JPanel();
        statButtonPanel.setBorder(BorderFactory.createTitledBorder("Actions"));
        statButtonPanel.setLayout(gb);

        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
       
        
        initializeButton = makeButton(INITIALIZE, "Initialize image to count");
        gb.setConstraints(initializeButton, gbc);
        statButtonPanel.add(initializeButton);

        addButton = makeButton(ADD, "add a counter type");
        addButton.setEnabled(false);
        gb.setConstraints(addButton, gbc);
        statButtonPanel.add(addButton);

        removeButton = makeButton(REMOVE, "remove last counter type");
        removeButton.setEnabled(false);
        gb.setConstraints(removeButton, gbc);
        statButtonPanel.add(removeButton);

        deleteButton = makeButton(DELETE, "delete last marker");
        deleteButton.setEnabled(false);
        gb.setConstraints(deleteButton, gbc);
        statButtonPanel.add(deleteButton);
        
        analyseButton = makeButton(ANALYSE, "delete last marker");
        analyseButton.setEnabled(false);
        gb.setConstraints(analyseButton, gbc);
        statButtonPanel.add(analyseButton);
        
        drawTracesButton = makeButton(DRAW_TRACES, "draw elliptical traces");
        drawTracesButton.setEnabled(false);
        gb.setConstraints(drawTracesButton, gbc);
        statButtonPanel.add(drawTracesButton);
        
        loadTestImageButton = makeButton(LOAD_TEST_IMAGE, "Load Test Image");
        gb.setConstraints(loadTestImageButton, gbc);
        statButtonPanel.add(loadTestImageButton);
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        //gbc.ipadx = 5;
        gb.setConstraints(statButtonPanel, gbc);
        getContentPane().add(statButtonPanel);

        final Runnable runner = new GUIShower(this);
        EventQueue.invokeLater(runner);
    }
    
    /** Creates JRadiobutton with action listener + associated command,
     * adds button to radiobutton group, and creates new trace<MarkerVector>
     * @param int traceNumber from 1-
     * @return
     */
    private JRadioButton makeDynRadioButton(final int traceNumber) {
        final JRadioButton jrButton = new JRadioButton("Trace:  " + traceNumber);
        jrButton.setActionCommand(TYPE_COMMAND_PREFIX + traceNumber);
        jrButton.addActionListener(this);
        dynRadioVector.add(jrButton);
        radioGrp.add(jrButton);
        typeVector.add(new MarkerVector(traceNumber));
        return jrButton;
    }
    
    /** Creates fixed JButton, with tooltip and action listener
     * 
     * @param name (see initGUI for names and tips)
     * @param tooltip
     * @return
     */
    private JButton makeButton(final String name, final String tooltip) {
        final JButton jButton = new JButton(name);
        jButton.setToolTipText(tooltip);
        jButton.addActionListener(this);
        return jButton;
    }
    
    /** Validates the 3 JPanels, and packs
     */
    void validateLayout() {
        dynPanel.validate();
        dynButtonPanel.validate();
        statButtonPanel.validate();
        validate();
        pack();
    }
    
    /** Action performed on static control buttons, or array of radiobuttons that represent the active traces */
    @Override
    public void actionPerformed(final ActionEvent event) {
        final String command = event.getActionCommand();

        if (command.equals(ADD)) {
            final int i = dynRadioVector.size() + 1;
            dynGrid.setRows(i);
            dynButtonPanel.add(makeDynRadioButton(i));
            dynRadioVector.elementAt(currentMarkerIndex).setSelected(false);
            currentMarkerIndex = dynRadioVector.size()-1;
            dynRadioVector.elementAt(currentMarkerIndex).setSelected(true);
            ic.setCurrentMarkerVector(typeVector.get(currentMarkerIndex));
            validateLayout();

            if (ic != null) ic.setTypeVector(typeVector);
        }
        else if (command.equals(REMOVE)) {
            if (dynRadioVector.size() > 1) {
                final JRadioButton rbutton = dynRadioVector.lastElement();
                dynButtonPanel.remove(rbutton);
                radioGrp.remove(rbutton);
                dynRadioVector.removeElementAt(dynRadioVector.size() - 1);
                dynGrid.setRows(dynRadioVector.size());
            }
            if (typeVector.size() > 1) {
                typeVector.removeElementAt(currentMarkerIndex);
            }
            ListIterator<MarkerVector> itr = typeVector.listIterator();
            while (itr.hasNext()){
                if (itr.nextIndex()>=currentMarkerIndex){
                    itr.next().setTraceNumber(itr.nextIndex());
                }
                else { 
                    itr.next();
                }
            }
            
            if (currentMarkerIndex>=typeVector.size()){
                currentMarkerIndex = typeVector.size()-1;
            }
                      
            ic.setCurrentMarkerVector(typeVector.get(currentMarkerIndex));
            dynRadioVector.elementAt(currentMarkerIndex).setSelected(true);            
            validateLayout();

            if (ic != null) ic.setTypeVector(typeVector); 
            ic.repaint();
        }
        else if (command.equals(INITIALIZE)) {
            initializeImage();
            initializeButton.setEnabled(false);
        }
        else if (command.startsWith(TYPE_COMMAND_PREFIX)) { // COUNT
            currentMarkerIndex =
                Integer.parseInt(command.substring(TYPE_COMMAND_PREFIX.length())) - 1;
            if (ic == null) {
                IJ.error("You need to initialize first");
                return;
            }
            currentMarkerVector = typeVector.get(currentMarkerIndex);
            ic.setCurrentMarkerVector(currentMarkerVector);
        }
        else if (command.equals(DELETE)) {
            ic.removeLastMarker();
        }
        else if (command.equals(ANALYSE)) {
            this.getOpticCentre();
            this.getSourceDetectorValues();
            this.ic.repaint();
        }
        else if (command.equals(LOAD_TEST_IMAGE)) {
            //this.setBinFactor(4);
            this.openTestImage();
        }
        else if (command.equals(DRAW_TRACES)) {
            this.ic.setDrawTraces(!this.ic.getDrawTraces());
            this.ic.repaint();
        }
    }
    
    /** INITIALISE button method
     * creates copy of current image, creates imagecanvas to place points
     */
    private void initializeImage() {
        ImagePlus img = WindowManager.getCurrentImage();
        if (img == null) {
            IJ.noImage();
        }
        else if (img.getStackSize() == 1) {
            ImageProcessor ip = img.getProcessor();
            ip.resetRoi();   
            ip = ip.crop();
            calibImg = new ImagePlus("Calibration Window - " + img.getTitle(), ip);   
            ic = new MagCalImageCanvas(calibImg, typeVector, this);
            new ImageWindow(calibImg,ic);
            
            Calibration cal = img.getCalibration(); //  to conserve voxel size of the original image
            calibImg.setCalibration(cal);
            
            addButton.setEnabled(true);
            removeButton.setEnabled(true);
            deleteButton.setEnabled(true);
            analyseButton.setEnabled(true);
            drawTracesButton.setEnabled(true);
            
            dynButtonPanel.add(makeDynRadioButton(1));
            currentMarkerIndex = 0;
            dynRadioVector.elementAt(currentMarkerIndex).setSelected(true);
            ic.setCurrentMarkerVector(typeVector.get(currentMarkerIndex));
            validateLayout();
        }
        else if (img.getStackSize() > 1) {
            throw new IllegalArgumentException("cannot operate on image stacks");
        }
    }
        
    /** LOAD_TEST_IMAGE button method. Opens and displays test image from file, and bins image with bin factor. Image is represented
     * by an ImagePlus object
     */
    private void openTestImage() {
        // Name and path of test image
        Path path = FileSystems.getDefault().getPath("MAX_rot_crop.tiff");
        //System.out.println(path.toAbsolutePath().toString());
        
        // Open test image from file
        ImagePlus img = IJ.openImage(path.toAbsolutePath().toString());
        if (this.binFactor>1) {
            img.setProcessor(img.getChannelProcessor().bin(this.binFactor));
        }
        img.show();
     
    }
    
    
     /** Requires: optic centre, otherwise displays error message
      * Trace dY parameter must exceed 10/binFactor pixels
      *  **/
    private void getSourceDetectorValues() {
        if (this.opticCentre != null) {
            ListIterator<MarkerVector> itr = typeVector.listIterator();
            final Plot effectiveR = new Plot("Effective Source-Detector Distance","Average Trace Y-Position (pixels) ","Source-Detector Distance (pixels)");
            ArrayList<Double> xVals = new ArrayList<Double>(1);
            ArrayList<Double> yVals = new ArrayList<Double>(1);
            double sourceDetDistTotal=0;
            
            while (itr.hasNext()) {
                final MarkerVector currentTrace = itr.next();
                if (Math.abs(currentTrace.getDy())>(float)10/this.binFactor) {
                    double sourceDetDist = currentTrace.getR(this.opticCentre);
                    xVals.add(currentTrace.getAverageYPosition());
                    yVals.add(sourceDetDist);
                    sourceDetDistTotal += sourceDetDist;
                }
            }
            
            if (xVals.size()>1) {
                double mean = sourceDetDistTotal/yVals.size();
                double error = 0;
                for (double index : yVals) {
                    error += (index-mean)*(index-mean);
                }
                effectiveR.addLabel(0, 0, "Av. Source-Detector Dist = " + String.format("%.0f", sourceDetDistTotal/yVals.size())+"  Standard Deviation = " +
                        String.format("%.0f", Math.sqrt(error/(yVals.size()-1))));
                //this.effectiveSourceDistance = sourceDetDistTotal/yVals.size();
                this.ic.setGlobalEffectiveSourceDistance(sourceDetDistTotal/yVals.size());
                effectiveR.setLineWidth(2);
                effectiveR.setColor(Color.red);
                effectiveR.addPoints(xVals, yVals, PlotWindow.X);
                effectiveR.setLineWidth(1);
                if (this.effectiveRplot!=null) {
                    if (!this.effectiveRplot.isClosed()) {
                        this.effectiveRplot.dispose();
                    }
                }
                this.effectiveRplot = effectiveR.show();
            }   
            else {
                IJ.error("Not enough data points");
            }
            effectiveR.dispose();
        }
        else {
            IJ.error("Set / Get Optic Centre First");
        }
    }
    
    /** Calculates optic centre using linear regression over multiple trace values.
     * Value is returned in binned coordinates. True values is opticCentre*binFactor.
     */
    private void getOpticCentre(){
        final SimpleRegression regressionClass = new SimpleRegression();
        final Plot opticYplot = new Plot("Optic Centre Y-Fit","Average Trace Y-Position (pixels)","dY/dX (pixels)");
        final Plot opticXplot = new Plot("Optic Centre X-Fit","Average Trace Y-Position (pixels)","Optic Centre X Position (pixels)");
        
        ArrayList<Double> xVals = new ArrayList<Double>(1);
        ArrayList<Double> yVals = new ArrayList<Double>(1);
        Double xValMin = Double.NaN;
        Double xValMax = Double.NaN;
        
        for (MarkerVector currentTrace : typeVector) {
            if (currentTrace.size()==4) {
                currentTrace.getDeltaVariables();
                if (currentTrace.getDxatMaxY()!=0) {
                    Double xVal = (double) currentTrace.getAverageYPosition();
                    Double yVal = (double) currentTrace.getDy()/currentTrace.getDx();
                    yVals.add(yVal);
                    xVals.add(xVal);
                    regressionClass.addData(xVal,yVal);
                    if (xValMin.isNaN() || xVal<xValMin) {
                        xValMin = xVal;
                    }
                    if (xValMax.isNaN() || xVal>xValMax) {
                        xValMax = xVal;
                    }
                }
            }
        }
        if (xVals.size()>1) {
            double intercept = regressionClass.getIntercept();
            double gradient = regressionClass.getSlope();
            opticCentre[1] = -intercept/gradient;
            System.out.println("Rguess = "+1/gradient);
            opticYplot.setLineWidth(2);
            opticYplot.setColor(Color.red);
            opticYplot.addPoints(xVals, yVals, PlotWindow.X);
            opticYplot.setLineWidth(1);
            opticYplot.setColor(Color.blue);
            opticYplot.drawLine(xValMin, gradient*xValMin+intercept, xValMax, gradient*xValMax+intercept);
            opticYplot.setColor(Color.black);
            opticYplot.addLabel(0, 0, "Optic Centre Y = "+String.format("%.0f", this.opticCentre[1]));
            if (this.opticYplot!=null) {
                if (!this.opticYplot.isClosed()) {
                    this.opticYplot.dispose();
                }
            }
            this.opticYplot = opticYplot.show();  
        }
        
        
        final SimpleRegression regressionOpticCentreX = new SimpleRegression();
        ArrayList<Double> opticCentreXguesses= new ArrayList<Double>(1);
        double sum =0;
        for (MarkerVector currentTrace : typeVector) {
            if (currentTrace.size()==4) {
                double opX = (double) (this.opticCentre[1]-currentTrace.getAverageYPosition())*currentTrace.getDxatMaxY()/currentTrace.getDy();
                opticCentreXguesses.add(opX);
                regressionOpticCentreX.addData(currentTrace.getAverageYPosition(), opX);
                sum += (double) (this.opticCentre[1]-currentTrace.getAverageYPosition())*currentTrace.getDxatMaxY()/currentTrace.getDy();
            }
        }
        opticCentre[0] = sum/opticCentreXguesses.size();
        

        if (opticCentreXguesses.size()>1) {
            double intercept = regressionOpticCentreX.getIntercept();
            double gradient = regressionOpticCentreX.getSlope();
            opticXplot.setLineWidth(2);
            opticXplot.setColor(Color.red);
            opticXplot.addPoints(xVals, opticCentreXguesses, PlotWindow.X);
            opticXplot.setLineWidth(1);
            opticXplot.setColor(Color.blue);
            opticXplot.drawLine(xValMin, gradient*xValMin+intercept, xValMax, gradient*xValMax+intercept);
            opticXplot.setColor(Color.black);
            opticXplot.addLabel(0, 0, "Optic Centre X = "+String.format("%.0f", this.opticCentre[0]));
            if (this.opticXplot!=null) {
                if (!this.opticXplot.isClosed()) {
                    this.opticXplot.dispose();
                }
            }
            this.opticXplot = opticXplot.show();  
        }
        opticYplot.dispose();
        opticXplot.dispose();
        this.ic.setOpticCentre(opticCentre);
    }
    
    
    public void setOpticCentre(double x, double y) {
        this.opticCentre[0] = x;
        this.opticCentre[1] = y;
    }
    
    public Vector<JRadioButton> getButtonVector() {
        return dynRadioVector;
    }

    public void setButtonVector(final Vector<JRadioButton> buttonVector) {
        this.dynRadioVector = buttonVector;
    }

    public MarkerVector getCurrentMarkerVector() {
        return currentMarkerVector;
    }

    public void setCurrentMarkerVector(
        final MarkerVector currentMarkerVector)
    {
        this.currentMarkerVector = currentMarkerVector;
    }
    
    private void setBinFactor(int binFactor) {
        this.binFactor = binFactor;
    }
    
    /** Used for testing plugin from Eclipse. Sets plugins directory, find class name, opens ImageJ and runs 
     * MagnificationCalibration.class
     * @param args are not required
     */
    public static void main(final String... args) throws Exception {       
        // set the plugins.dir property to make the plugin appear in the Plugins menu
        Class<?> clazz = MagnificationCalibration.class;
        String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
        String pluginsDir = url.substring("file:".length(), url.length() - clazz.getName().length() - ".class".length());
        System.setProperty("plugins.dir", pluginsDir);

        // start ImageJ
        new ImageJ();

        // run the plugin
        IJ.runPlugIn(clazz.getName(),"");
    }
}
