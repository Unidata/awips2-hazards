/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.megawidgets;

import java.util.Map;

import org.eclipse.swt.widgets.Composite;

/**
 * Description: Hidden field megawidget, which much like a hidden input field in
 * an HTML document allows the tracking of state, with such state being modified
 * only programmatically (directly or via side effects). This megawidget may
 * only track one state value.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Oct 22, 2014    5050    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see HiddenFieldSpecifier
 */
public class HiddenFieldMegawidget extends StatefulMegawidget implements
        IControl {

    // Private Variables

    /**
     * State.
     */
    private Object state;

    // Protected Constructors

    /**
     * Construct a standard instance.
     * 
     * @param specifier
     *            Specifier.
     * @param parent
     *            Parent of the megawidget; this is unused by this object, but
     *            is accepted as a parameter because all concrete megawigdet
     *            subclasses must have a constructor taking these three
     *            parameters.
     * @param paramMap
     *            Hash table mapping megawidget creation time parameter
     *            identifiers to values.
     */
    protected HiddenFieldMegawidget(HiddenFieldSpecifier specifier,
            Composite parent, Map<String, Object> paramMap) {
        super(specifier, paramMap);
        state = specifier.getStartingState(specifier.getIdentifier());
    }

    // Public Methods

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public void setEditable(boolean editable) {

        /*
         * No action.
         */
    }

    @Override
    public int getLeftDecorationWidth() {
        return 0;
    }

    @Override
    public void setLeftDecorationWidth(int width) {

        /*
         * No action.
         */
    }

    @Override
    public int getRightDecorationWidth() {
        return 0;
    }

    @Override
    public void setRightDecorationWidth(int width) {

        /*
         * No action.
         */
    }

    // Protected Methods

    @Override
    protected Object doGetState(String identifier) {
        return state;
    }

    @Override
    protected void doSetState(String identifier, Object state)
            throws MegawidgetStateException {
        this.state = state;
    }

    @Override
    protected String doGetStateDescription(String identifier, Object state)
            throws MegawidgetStateException {
        return (state == null ? null : state.toString());
    }

    @Override
    protected void doSynchronizeComponentWidgetsToState() {

        /*
         * No action.
         */
    }

    @Override
    protected void doSetEnabled(boolean enable) {

        /*
         * No action.
         */
    }
}
