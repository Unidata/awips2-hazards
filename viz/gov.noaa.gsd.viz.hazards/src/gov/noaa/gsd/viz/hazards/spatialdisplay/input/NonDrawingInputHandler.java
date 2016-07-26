/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.input;

import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialDisplay;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;

import com.raytheon.viz.ui.VizWorkbenchManager;
import com.raytheon.viz.ui.editor.AbstractEditor;

/**
 * Description: Base class from which to derive classes used to handle input in
 * non-drawing (selection, modification, etc.) modes for the spatial display.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jul 05, 2016   19537    Chris.Godlen Initial creation (adapted from old
 *                                      NonDrawingAction inner class).
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class NonDrawingInputHandler extends BaseInputHandler {

    // Private Variables

    /**
     * Flag indicating whether the Shift key is currently down.
     */
    private boolean shiftDown;

    /**
     * Flag indicating whether the Control key is currently down.
     */
    private boolean controlDown;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param spatialDisplay
     *            Spatial display to be used.
     */
    public NonDrawingInputHandler(SpatialDisplay spatialDisplay) {
        super(spatialDisplay);
    }

    // Public Methods

    @Override
    public void reset() {
        shiftDown = false;
        controlDown = false;
    }

    @Override
    public boolean handleMouseMove(int x, int y) {

        /*
         * TODO: This is a kludge. Essentially, any time movement is detected in
         * the mouse, focus is set to the active editor. This is done so that if
         * the Control or Shift key is pressed, the mouse handler will detect
         * the keypress. If the editor does not have focus, then the keypress
         * goes unnoticed. It would be a lot better if the input adapter passed
         * all event context in its handleMouseXXX() methods, as then the state
         * of the Control and Shift keys could be queried from the event; but
         * since that's not possible, this is the route that must be taken
         * unless something better becomes possible. (The reason this is a
         * kludge is because it's annoying for the user to have focus switch to
         * the active editor when the user hovers the cursor over said editor;
         * it leads to unpleasant side effects when using the Console, for
         * example.)
         */
        AbstractEditor editor = ((AbstractEditor) VizWorkbenchManager
                .getInstance().getActiveEditor());

        editor.getActiveDisplayPane().setFocus();

        return false;
    }

    @Override
    public boolean handleKeyDown(int keyCode) {
        if (keyCode == SWT.SHIFT) {
            shiftDown = true;
        } else if (keyCode == SWT.CONTROL) {
            controlDown = true;
        }
        return true;
    }

    @Override
    public boolean handleKeyUp(int keyCode) {
        if (keyCode == SWT.SHIFT) {
            shiftDown = false;
        } else if (keyCode == SWT.CONTROL) {
            controlDown = false;
        }
        return true;
    }

    @Override
    public boolean handleMouseEnter(Event event) {
        shiftDown = ((event.stateMask & SWT.SHIFT) != 0);
        controlDown = ((event.stateMask & SWT.CTRL) != 0);
        return false;
    }

    // Protected Methods

    /**
     * Determine whether or not the Shift key is currently down.
     * 
     * @return <code>true</code> if the Shift key is down, <code>false</code>
     *         otherwise.
     */
    protected final boolean isShiftDown() {
        return shiftDown;
    }

    /**
     * Determine whether or not the Control key is currently down.
     * 
     * @return <code>true</code> if the Control key is down, <code>false</code>
     *         otherwise.
     */
    protected final boolean isControlDown() {
        return controlDown;
    }
}