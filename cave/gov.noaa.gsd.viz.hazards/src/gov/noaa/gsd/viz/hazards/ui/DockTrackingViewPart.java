/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.ui;

import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ViewPart;

/**
 * Description: View part that tracks whether it is currently docked or
 * undocked.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jul 12, 2013     585    Chris.Golden Initial creation.
 * May 09, 2014    2925    Chris.Golden Moved to its own package.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public abstract class DockTrackingViewPart extends ViewPart {

    // Private Variables

    /**
     * Flag indicating whether or not this view part is currently docked.
     */
    private boolean docked;

    /**
     * Parent composite.
     */
    private Composite parent = null;

    // Public Methods

    @Override
    public void createPartControl(Composite parent) {

        /*
         * Remember the parent for use later, and keep track of resize events to
         * determine whether or not the part is currently docked.
         */
        this.parent = parent;
        parent.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {

                /*
                 * Determine whether or not the part is now docked.
                 */
                boolean docked = determineWhetherDocked();
                if (docked != DockTrackingViewPart.this.docked) {
                    DockTrackingViewPart.this.docked = docked;
                }
            }
        });

        /*
         * Determine whether or not the view is currently docked.
         */
        docked = determineWhetherDocked();
    }

    /**
     * Determine whether or not the view is currently docked.
     * 
     * @return True if the view is currently docked, false otherwise.
     */
    public final boolean isDocked() {
        return docked;
    }

    /**
     * Get the shell of the view part, or <code>null</code> if it is currently
     * docked.
     * 
     * @return Shell of the view part, or <code>null</code> if it is currently
     *         docked.
     */
    public final Shell getShell() {
        return (docked ? null : parent.getShell());
    }

    // Private Methods

    /**
     * Determine whether or not the view is now docked.
     * 
     * @return True if the view is docked, false otherwise.
     */
    private boolean determineWhetherDocked() {
        return (parent.getShell().getText().length() > 0);
    }
}
