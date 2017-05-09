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
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import net.imagej.ImageJ;

import java.io.File;
import java.nio.file.*;
import java.io.IOException;

import net.imagej.Dataset;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import io.scif.services.DatasetIOService;

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

/**
 *  Adapted from CellCounter Imagej plugin
 *
 * @author Thomas Watson
 */
@Plugin(type = Command.class)
public class MagnificationCalibration extends JFrame implements ActionListener, Command
{

    private static final String ADD = "Add";
    private static final String REMOVE = "Remove";
    private static final String INITIALIZE = "Initialize";
    private static final String DELETE = "Delete";
    private static final String TYPE_COMMAND_PREFIX = "trace";

    private Vector<MarkerVector> typeVector;
    private Vector<JRadioButton> dynRadioVector;
    private MarkerVector markerVector;
    private MarkerVector currentMarkerVector;
    private int currentMarkerIndex;

    private JPanel dynPanel;
    private JPanel dynButtonPanel;
    private JPanel statButtonPanel;
    private ButtonGroup radioGrp;
    private JSeparator separator;
    private JButton addButton;
    private JButton removeButton;
    private JButton initializeButton;
    private JButton deleteButton;

    private MagCalImageCanvas ic;

    private ImagePlus calibImg;

    private GridLayout dynGrid;
    
    static MagnificationCalibration instance;
    

    public void run() {
        //super("Magnification Calibration");
        setResizable(false);
        typeVector = new Vector<MarkerVector>();
        dynRadioVector = new Vector<JRadioButton>();
        initGUI();
        instance = this;
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
            jFrame.setLocation(1000, 200);
            jFrame.setVisible(true);
        }
    }

    private void initGUI() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        final GridBagLayout gb = new GridBagLayout();
        getContentPane().setLayout(gb);

        radioGrp = new ButtonGroup();// to group the radiobuttons

        dynGrid = new GridLayout(8, 1);
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
        gbc.ipadx = 5;
        gb.setConstraints(dynButtonPanel, gbc);
        dynPanel.add(dynButtonPanel);

        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.ipadx = 5;
        gb.setConstraints(dynPanel, gbc);
        getContentPane().add(dynPanel);

        dynButtonPanel.add(makeDynRadioButton(1));

        // create a "static" panel to hold control buttons
        statButtonPanel = new JPanel();
        statButtonPanel.setBorder(BorderFactory.createTitledBorder("Actions"));
        statButtonPanel.setLayout(gb);

        /* gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        newCheck = new JCheckBox();
        newCheck.setToolTipText("Keep original");
        newCheck.setSelected(false);
        newCheck.addItemListener(this);
        gb.setConstraints(newCheck, gbc);
        statButtonPanel.add(newCheck);
        */

        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        initializeButton = makeButton(INITIALIZE, "Initialize image to count");
        gb.setConstraints(initializeButton, gbc);
        statButtonPanel.add(initializeButton);

        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(3, 0, 3, 0);
        separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setPreferredSize(new Dimension(1, 1));
        gb.setConstraints(separator, gbc);
        statButtonPanel.add(separator);

        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        addButton = makeButton(ADD, "add a counter type");
        gb.setConstraints(addButton, gbc);
        statButtonPanel.add(addButton);

        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        removeButton = makeButton(REMOVE, "remove last counter type");
        gb.setConstraints(removeButton, gbc);
        statButtonPanel.add(removeButton);

        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.insets = new Insets(3, 0, 3, 0);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setPreferredSize(new Dimension(1, 1));
        gb.setConstraints(separator, gbc);
        statButtonPanel.add(separator);

        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER; 
        deleteButton = makeButton(DELETE, "delete last marker");
        deleteButton.setEnabled(false);
        gb.setConstraints(deleteButton, gbc);
        statButtonPanel.add(deleteButton);
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.ipadx = 5;
        gb.setConstraints(statButtonPanel, gbc);
        getContentPane().add(statButtonPanel);

        final Runnable runner = new GUIShower(this);
        EventQueue.invokeLater(runner);
    }

    private JRadioButton makeDynRadioButton(final int traceNumber) {
        final JRadioButton jrButton = new JRadioButton("Trace:  " + traceNumber);
        jrButton.setActionCommand(TYPE_COMMAND_PREFIX + traceNumber);
        jrButton.addActionListener(this);
        dynRadioVector.add(jrButton);
        radioGrp.add(jrButton);
        markerVector = new MarkerVector(traceNumber);
        typeVector.add(markerVector);
        return jrButton;
    }

    private JButton makeButton(final String name, final String tooltip) {
        final JButton jButton = new JButton(name);
        jButton.setToolTipText(tooltip);
        jButton.addActionListener(this);
        return jButton;
    }
    

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
        }
        else if (img.getStackSize() > 1) {
            throw new IllegalArgumentException("cannot operate on image stacks");
        }
        
        Calibration cal = img.getCalibration(); //  to conserve voxel size of the original image
        calibImg.setCalibration(cal);
        
        addButton.setEnabled(true);
        removeButton.setEnabled(true);
        deleteButton.setEnabled(true);
    }

    void validateLayout() {
        dynPanel.validate();
        dynButtonPanel.validate();
        statButtonPanel.validate();
        validate();
        pack();
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
        final String command = event.getActionCommand();

        if (command.equals(ADD)) {
            final int i = dynRadioVector.size() + 1;
            dynGrid.setRows(i);
            dynButtonPanel.add(makeDynRadioButton(i));
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
                typeVector.removeElementAt(typeVector.size() - 1);
            }
            validateLayout();

            if (ic != null) ic.setTypeVector(typeVector);
        }
        else if (command.equals(INITIALIZE)) {
            initializeImage();
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
        if (ic != null) ic.repaint();
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
    
    public static void main(final String... args) throws Exception {
        // Launch ImageJ as usual.
        final ImageJ ij = net.imagej.Main.launch(args);

        // Name and path of test image
        Path path = FileSystems.getDefault().getPath("MAX_rot_crop.tiff");
        System.out.println(path.toAbsolutePath().toString());
        
        // Open test image from file
        ImagePlus img = IJ.openImage(path.toAbsolutePath().toString());
        img.show();
        
        // Launch "MagnifcationCalibration" Command
        ij.command().run(MagnificationCalibration.class, true);
    }
    
    public static void setType(final String type) {
        if (instance == null || instance.ic == null || type == null) return;
        final int index = Integer.parseInt(type) - 1;
        final int buttons = instance.dynRadioVector.size();
        if (index < 0 || index >= buttons) return;
        final JRadioButton rbutton = instance.dynRadioVector.elementAt(index);
        instance.radioGrp.setSelected(rbutton.getModel(), true);
        instance.currentMarkerVector = instance.typeVector.get(index);
        instance.ic.setCurrentMarkerVector(instance.currentMarkerVector);
    }
}
