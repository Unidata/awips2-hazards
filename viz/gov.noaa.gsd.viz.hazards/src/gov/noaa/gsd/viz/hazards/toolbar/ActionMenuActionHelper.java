/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.toolbar;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * Abstract class from which to derive a helper for a subclass of type
 * {@link ActionMenuAction}, with said type specified by the generic parameter
 * <code>A</code>, corresponding to the main button on the toolbar next to the
 * <code>ActionMenuAction</code>'s drop-down arrow button. The generic parameter
 * <code>H</code> is the subclass's type, while the generic parameter
 * <code>P</code> is the type of the principal action that may be assigned to
 * instances of this class. (The principal set at any given moment is the action
 * that an instance of this class is to emulate at that moment.)
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * Jan 6, 2018    33428    Chris.Golden  Initial creation.
 * </pre>
 *
 * @author golden
 */
public abstract class ActionMenuActionHelper<A extends ActionMenuAction<A, H, P>, H extends ActionMenuActionHelper<A, H, P>, P extends BasicAction>
        extends BasicAction {

    // Private Variables

    /**
     * Action that is currently the one being wrapped by this action.
     */
    private P principal;

    /**
     * Property change listener for the principal.
     */
    private final IPropertyChangeListener principalPropertyChangeListener = new IPropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent event) {
            String propertyName = event.getProperty();
            if (propertyName.equals(CHECKED)) {
                if (isChecked() != principal.isChecked()) {
                    setChecked(principal.isChecked());
                }
            } else if (propertyName.equals(ENABLED)) {
                if (isEnabled() != principal.isEnabled()) {
                    setEnabled(principal.isEnabled());
                }
            } else if (propertyName.equals(IMAGE)) {
                if (((getImageDescriptor() == null)
                        && (principal.getImageDescriptor() != null))
                        || ((getImageDescriptor() != null)
                                && (getImageDescriptor().equals(principal
                                        .getImageDescriptor()) == false))) {
                    setImageDescriptor(principal.getImageDescriptor());
                }
            } else if (propertyName.equals(TEXT)) {
                if (((getText() == null) && (principal.getText() != null))
                        || ((getText() != null) && (getText()
                                .equals(principal.getText()) == false))) {
                    setText(principal.getText());
                }
            } else if (propertyName.equals(TOOL_TIP_TEXT)) {
                if (((getToolTipText() == null)
                        && (principal.getToolTipText() != null))
                        || ((getToolTipText() != null)
                                && (getToolTipText().equals(principal
                                        .getToolTipText()) == false))) {
                    setToolTipText(principal.getToolTipText());
                }
            }
        }
    };

    /**
     * Property change listener for this wrapper.
     */
    private final IPropertyChangeListener wrapperPropertyChangeListener = new IPropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent event) {
            if (principal == null) {
                return;
            }
            String propertyName = event.getProperty();
            if (propertyName.equals(CHECKED)) {
                if (isChecked() != principal.isChecked()) {
                    principal.setChecked(isChecked());
                }
            }
        }
    };

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param principal
     *            Action that is currently the one being wrapped by this action.
     */
    public ActionMenuActionHelper(P principal) {
        super(principal.getText(), principal.getStyle(),
                principal.getImageDescriptor(), principal.getToolTipText());
        setEnabled(principal.isEnabled());
        setPrincipal(principal);
        addPropertyChangeListener(wrapperPropertyChangeListener);
    }

    // Public Methods

    /**
     * Get the principal.
     * 
     * @return Principal.
     */
    public P getPrincipal() {
        return principal;
    }

    /**
     * Set the principal, to be wrapped by this action.
     * 
     * @param principal
     *            Action that is to be the one being wrapped by this action.
     */
    public void setPrincipal(P principal) {

        /*
         * Do nothing if the principal is unchanged.
         */
        if (this.principal == principal) {
            return;
        }

        /*
         * If the old and new principal do not have the same style, an error has
         * occurred.
         */
        if ((this.principal != null)
                && (this.principal.getStyle() != principal.getStyle())) {
            throw new IllegalArgumentException(
                    "new principal does not have same style as old principal");
        }

        /*
         * Remove the property change listener from the previous principal.
         */
        if (this.principal != null) {
            this.principal.removePropertyChangeListener(
                    principalPropertyChangeListener);
        }

        /*
         * Remember the new principal, and set up this action to look just like
         * it, and to respond to the principal's property changes by changing
         * this action to match.
         */
        this.principal = principal;
        setChecked(principal.isChecked());
        setEnabled(principal.isEnabled());
        setImageDescriptor(principal.getImageDescriptor());
        setText(principal.getText());
        setToolTipText(principal.getToolTipText());
        this.principal
                .addPropertyChangeListener(principalPropertyChangeListener);
    }

    @Override
    public abstract void run();
}
