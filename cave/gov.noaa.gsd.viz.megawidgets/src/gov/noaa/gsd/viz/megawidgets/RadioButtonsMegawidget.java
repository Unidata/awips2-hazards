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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.google.common.collect.ImmutableSet;

/**
 * Radio buttons megawidget, providing a series of radio buttons from which the
 * user may choose a single selection. Each radio button may have zero or more
 * megawidgets associated with it as detail fields, allowing the input of
 * additional information related to the associated choice.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Apr 30, 2013    1277    Chris.Golden      Added support for mutable properties.
 * Sep 25, 2013    2168    Chris.Golden      Added support for optional detail
 *                                           fields next to the choice buttons,
 *                                           and changed to implement new IControl
 *                                           interface.
 * Oct 31, 2013    2336    Chris.Golden      Changed to accommodate alteration
 *                                           of framework to include notion
 *                                           of bounded (closed set) choices
 *                                           versus unbounded (sets to which
 *                                           arbitrary user-specified choices
 *                                           can be added) choice megawidgets.
 * Mar 18, 2014    2155    Chris.Golden      Fixed bug that caused radio buttons
 *                                           to erroneously allow deselection by
 *                                           the user, and changed from single-
 *                                           line to multi-line comment style.
 * Apr 24, 2014    2925    Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * Jun 17, 2014    3982    Chris.Golden      Changed to use new choices button
 *                                           component, and to disable detail
 *                                           children when it is disabled.
 * Jun 24, 2014   4010     Chris.Golden      Changed to no longer be a subclass
 *                                           of NotifierMegawidget.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see RadioButtonsSpecifier
 */
public class RadioButtonsMegawidget extends SingleBoundedChoiceMegawidget
        implements IParent<IControl>, IControl {

    // Protected Static Constants

    /**
     * Set of all mutable property names for instances of this class.
     */
    protected static final Set<String> MUTABLE_PROPERTY_NAMES;
    static {
        Set<String> names = new HashSet<>(
                MultipleBoundedChoicesMegawidget.MUTABLE_PROPERTY_NAMES_WITHOUT_CHOICES);
        names.add(IControlSpecifier.MEGAWIDGET_EDITABLE);
        MUTABLE_PROPERTY_NAMES = ImmutableSet.copyOf(names);
    };

    // Private Variables

    /**
     * Label associated with this megawidget, if any.
     */
    private final Label label;

    /**
     * Radio buttons associated with this megawidget.
     */
    private final List<ChoiceButtonComponent> radioButtons;

    /**
     * Detail child megawidget manager.
     */
    private final BoundedChoicesDetailChildrenManager childManager;

    /**
     * Control component helper.
     */
    private final ControlComponentHelper helper;

    // Protected Constructors

    /**
     * Construct a standard instance.
     * 
     * @param specifier
     *            Specifier.
     * @param parent
     *            Parent of the megawidget.
     * @param paramMap
     *            Hash table mapping megawidget creation time parameter
     *            identifiers to values.
     * @throws MegawidgetException
     *             If an error occurs while creating or initializing any
     *             megawidgets acting as detail fields for the various choices.
     */
    protected RadioButtonsMegawidget(RadioButtonsSpecifier specifier,
            Composite parent, Map<String, Object> paramMap)
            throws MegawidgetException {
        super(specifier, paramMap);
        helper = new ControlComponentHelper(specifier);

        /*
         * Create and lay out the label and radio button widgets.
         */
        Composite panel = UiBuilder.buildComposite(parent, 1,
                SWT.NO_RADIO_GROUP,
                UiBuilder.CompositeType.MULTI_ROW_VERTICALLY_CONSTRAINED,
                specifier);
        this.label = UiBuilder.buildLabel(panel, specifier);
        SelectionListener listener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button radioButton = (Button) e.widget;

                /*
                 * To prevent radio buttons from being deselected, the selection
                 * is reset to true if it was set to false. This is required
                 * because the radio buttons are not grouped by their containing
                 * composite, since they may have different composite parents
                 * due to the possible presence of detail megawidgets.
                 */
                if (radioButton.getSelection() == false) {
                    radioButton.setSelection(true);
                    return;
                }

                /*
                 * Set the other radio buttons to be unselected, since again
                 * they are not grouped by a single containing composite.
                 */
                for (ChoiceButtonComponent otherButton : RadioButtonsMegawidget.this.radioButtons) {
                    if (otherButton.getButton() != radioButton) {
                        otherButton.setChecked(false);
                    }
                }
                state = ((ChoiceButtonComponent) radioButton.getData())
                        .getChoice();
                notifyListener(getSpecifier().getIdentifier(), state);
            }
        };
        this.childManager = (specifier.getChildMegawidgetSpecifiers().size() > 0 ? new BoundedChoicesDetailChildrenManager(
                listener, paramMap) : null);
        this.radioButtons = UiBuilder.buildChoiceButtons(panel, specifier,
                SWT.RADIO, childManager, listener);

        /*
         * Make the widgets read-only if the megawidget is not editable.
         */
        if (isEditable() == false) {
            doSetEditable(false);
        }

        /*
         * Synchronize user-facing widgets to the starting state.
         */
        synchronizeComponentWidgetsToState();
    }

    // Public Methods

    @Override
    public Set<String> getMutablePropertyNames() {
        return MUTABLE_PROPERTY_NAMES;
    }

    @Override
    public Object getMutableProperty(String name)
            throws MegawidgetPropertyException {
        if (name.equals(IControlSpecifier.MEGAWIDGET_EDITABLE)) {
            return isEditable();
        }
        return super.getMutableProperty(name);
    }

    @Override
    public void setMutableProperty(String name, Object value)
            throws MegawidgetPropertyException {
        if (name.equals(IControlSpecifier.MEGAWIDGET_EDITABLE)) {
            setEditable(ConversionUtilities.getPropertyBooleanValueFromObject(
                    getSpecifier().getIdentifier(), getSpecifier().getType(),
                    value, name, null));
        } else {
            super.setMutableProperty(name, value);
        }
    }

    @Override
    public final boolean isEditable() {
        return helper.isEditable();
    }

    @Override
    public final void setEditable(boolean editable) {
        helper.setEditable(editable);
        doSetEditable(editable);
    }

    @Override
    public final int getLeftDecorationWidth() {
        return 0;
    }

    @Override
    public final void setLeftDecorationWidth(int width) {

        /*
         * No action.
         */
    }

    @Override
    public final int getRightDecorationWidth() {
        return 0;
    }

    @Override
    public final void setRightDecorationWidth(int width) {

        /*
         * No action.
         */
    }

    @Override
    public final List<IControl> getChildren() {
        return (childManager == null ? Collections.<IControl> emptyList()
                : childManager.getDetailMegawidgets());
    }

    // Protected Methods

    @Override
    protected final boolean isChoicesListMutable() {
        return false;
    }

    @Override
    protected final void prepareForChoicesChange() {
        throw new UnsupportedOperationException(
                "cannot change choices for radio buttons megawidget");
    }

    @Override
    protected void cancelPreparationForChoicesChange() {
        throw new UnsupportedOperationException(
                "cannot change choices for radio buttons megawidget");
    }

    @Override
    protected final void synchronizeComponentWidgetsToChoices() {
        throw new UnsupportedOperationException(
                "cannot change choices for radio buttons megawidget");
    }

    @Override
    protected final void doSynchronizeComponentWidgetsToState() {
        for (ChoiceButtonComponent radioButton : radioButtons) {
            radioButton.setChecked(radioButton.getChoice().equals(state));
        }
    }

    @Override
    protected final void doSetEnabled(boolean enable) {
        if (label != null) {
            label.setEnabled(enable);
        }
        for (ChoiceButtonComponent radioButton : radioButtons) {
            radioButton.setEnabled(enable);
        }
        for (IControl child : getChildren()) {
            child.setEnabled(enable);
        }
    }

    // Private Methods

    /**
     * Change the component widgets to ensure their state matches that of the
     * editable flag.
     * 
     * @param editable
     *            Flag indicating whether the component widgets are to be
     *            editable or read-only.
     */
    private void doSetEditable(boolean editable) {
        for (ChoiceButtonComponent radioButton : radioButtons) {
            radioButton.setEditable(editable);
        }
    }
}