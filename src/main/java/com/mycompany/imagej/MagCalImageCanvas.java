package com.mycompany.imagej;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.measure.Calibration;
import ij.process.ImageProcessor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.util.ListIterator;
import java.util.Vector;

/**
 * TODO
 *
 * @author Thomas Watson
 */
public class MagCalImageCanvas extends ImageCanvas implements KeyListener {

    private Vector<MarkerVector> typeVector;
    private MarkerVector currentMarkerVector;
    private final MagnificationCalibration classMagnificationCalibration;
    private final ImagePlus img;
    private final Font font = new Font("SansSerif", Font.PLAIN, 10);
    private double[] opticCentre = new double[2];
    private boolean drawTraces = false;
    private double globalEffectiveSourceDistance = 0;

    /** Creates a new instance of MagCalImageCanvas */
    public MagCalImageCanvas(final ImagePlus img,
        final Vector<MarkerVector> typeVector, final MagnificationCalibration classMagnificationCalibration)
    {
        super(img);
        this.img = img;
        this.typeVector = typeVector;
        this.classMagnificationCalibration = classMagnificationCalibration;
        this.addKeyListener(this);
    }
    
    @Override
    public void mousePressed(final MouseEvent e) {
        if (IJ.spaceBarDown() || Toolbar.getToolId() == Toolbar.MAGNIFIER ||
            Toolbar.getToolId() == Toolbar.HAND)
        {
            super.mousePressed(e);
            return;
        }

        if (currentMarkerVector == null) {
            IJ.error("Select a counter type first!");
            return;
        }
        float xcentre = (float) img.getWidth()/2;
        float ycentre = (float) img.getHeight()/2;
        
        final float x = super.offScreenX(e.getX())-xcentre;
        final float y = super.offScreenY(e.getY())-ycentre;
        final Marker markedPoint = new Marker(x, y);
        currentMarkerVector.addMarker(markedPoint);
        currentMarkerVector.getAverageYPosition();
        repaint();
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
        super.mouseReleased(e);
    }

    @Override
    public void mouseMoved(final MouseEvent e) {
        super.mouseMoved(e);
    }

    @Override
    public void mouseExited(final MouseEvent e) {
        super.mouseExited(e);
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
        super.mouseEntered(e);
        if (!IJ.spaceBarDown() | Toolbar.getToolId() != Toolbar.MAGNIFIER |
            Toolbar.getToolId() != Toolbar.HAND) setCursor(Cursor
            .getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    @Override
    public void mouseDragged(final MouseEvent e) {
        super.mouseDragged(e);
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
        super.mouseClicked(e);
    }

    private Rectangle srcRect = new Rectangle(0, 0, 0, 0);
    
    /** Paints java Canvas, with ovals and number at Marker locations. If marker vector has 4 markers,
     * draws an GeneralPath that represents the trace.
     */
    @Override
    public void paint(final Graphics g) {
        super.paint(g);
        final Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(1f));
        g2.setFont(font);
        final ListIterator<MarkerVector> it = typeVector.listIterator();
        
        srcRect = getSrcRect();
        double xM = 0;
        double yM = 0;
        while (it.hasNext()) {
            final MarkerVector mv = it.next();
            final int traceNumber = mv.getTraceNumber();
            g2.setColor(Color.yellow);
            final ListIterator<Marker> mit = mv.listIterator();
            while (mit.hasNext()) {
                final Marker m = mit.next();
                    xM = ((m.getX()+(float)img.getWidth()/2 - srcRect.x) * magnification);
                    yM = ((m.getY()+(float)img.getHeight()/2  - srcRect.y) * magnification);
                    g2.fillOval((int) xM - 2, (int) yM - 2, 4, 4);
                    g2.drawString(Integer.toString(traceNumber),
                        (int) xM + 3, (int) yM - 3);
            }
            if (mv.size()==4 && this.drawTraces==true) {
                mv.getDeltaVariables();
                AffineTransform tForm = new AffineTransform();
                GeneralPath tracePath = new GeneralPath();
                if (this.globalEffectiveSourceDistance==0) {
                    tracePath = mv.getTrace(opticCentre, 32);
                }
                else {
                    tracePath = mv.getTrace(opticCentre, 32,this.globalEffectiveSourceDistance);
                }
                tForm.scale(magnification, magnification);
                tForm.translate((double)img.getWidth()/2- srcRect.x, (double)img.getHeight()/2- srcRect.y);
                tracePath.transform(tForm);
                g2.setColor(Color.red);
                g2.draw(tracePath);
            }
        }
    }
    
    public void paintSingleMarkerPoint(final int x,final int y) {
        Graphics g = this.getGraphics();
        final Graphics2D g2 = (Graphics2D) g;
        Color normalColor = g2.getColor();
        srcRect = getSrcRect();
        double xM = ((double) x- srcRect.x) * magnification;
        double yM = ((double) y  - srcRect.y) * magnification;
        g2.setColor(Color.blue);
        g2.fillOval((int) xM - 1, (int) yM - 1, 2, 2);
        g2.setColor(normalColor);
    }
    
    

    public void removeLastMarker() {
        currentMarkerVector.removeLastMarker();
        repaint();
    }

    public ImagePlus imageWithMarkers() {
        final Image image = this.createImage(img.getWidth(), img.getHeight());
        final Graphics gr = image.getGraphics();

        double xM = 0;
        double yM = 0;

        try {
            if (imageUpdated) {
                imageUpdated = false;
                img.updateImage();
            }
            final Image image2 = img.getImage();
            gr.drawImage(image2, 0, 0, img.getWidth(), img.getHeight(), null);
        }
        catch (final OutOfMemoryError e) {
            IJ.outOfMemory("Paint " + e.getMessage());
        }

        final Graphics2D g2r = (Graphics2D) gr;
        g2r.setStroke(new BasicStroke(1f));

        final ListIterator<MarkerVector> it = typeVector.listIterator();
        while (it.hasNext()) {
            final MarkerVector mv = it.next();
            final int traceNumber = mv.getTraceNumber();
            g2r.setColor(Color.yellow);
            final ListIterator<Marker> mit = mv.listIterator();
            while (mit.hasNext()) {
                final Marker m = mit.next();
                    xM = m.getX();
                    yM = m.getY();
                    g2r.fillOval((int) xM - 2, (int) yM - 2, 4, 4);
                    g2r.drawString(Integer.toString(traceNumber),
                        (int) xM + 3, (int) yM - 3);
            }
        }

        return new ImagePlus("Markers_" + img.getTitle(), image);
    }

    public Vector<MarkerVector> getTypeVector() {
        return typeVector;
    }

    public void setTypeVector(final Vector<MarkerVector> typeVector) {
        this.typeVector = typeVector;
    }

    public MarkerVector getCurrentMarkerVector() {
        return currentMarkerVector;
    }

    public void setCurrentMarkerVector(
        final MarkerVector currentMarkerVector)
    {
        this.currentMarkerVector = currentMarkerVector;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (e.getKeyChar()=='+'|| e.getKeyChar()=='='){
            Point cursorLoc = super.getCursorLoc();
            final int sx = (int) cursorLoc.getX();
            final int sy = (int) cursorLoc.getY();   
            //System.out.println("(x,y) = ("+this.screenX(sx)+","+this.screenY(sy)+")");
            this.zoomIn(this.screenX(sx), this.screenY(sy));
        }
        
        if (e.getKeyChar()=='-'){
            Point cursorLoc = super.getCursorLoc();
            //System.out.println("(x,y) = ("+(int)cursorLoc.getX()+","+(int)cursorLoc.getY()+")");
            final int sx = (int) cursorLoc.getX();
            final int sy = (int) cursorLoc.getY();           
            this.zoomOut(this.screenX(sx), this.screenY(sy));
        }
        
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Do nothing       
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Do nothing   
    }
    
    public void setOpticCentre(double opticCentre[]) {
        this.opticCentre = opticCentre;
    }
    
    public void setDrawTraces(boolean b) {
        this.drawTraces = b;
    }
    
    public boolean getDrawTraces() {
        return this.drawTraces;
    }
    
    public void setGlobalEffectiveSourceDistance(double R) {
        this.globalEffectiveSourceDistance = R;
    }
}
