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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Description: Utility class providing methods to build UI elements for
 * megawidgets. As more megawidgets are created, the building of their UI
 * elements may be encapsulated within static methods within this class so as to
 * avoid duplication of code and maintain a consistent look.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 22, 2013    2168    Chris.Golden      Initial creation
 * Oct 31, 2013    2336    Chris.Golden      Changed to accommodate alteration
 *                                           of framework to include notion
 *                                           of bounded (closed set) choices
 *                                           versus unbounded (sets to which
 *                                           arbitrary user-specified choices
 *                                           can be added) choice megawidgets.
 * Dec 14, 2013    2545    Chris.Golden      Added method to check a key code
 *                                           to see if it is an increment/
 *                                           decrement invoker in CDateTime
 *                                           widgets.
 * Feb 08, 2014    2161    Chris.Golden      Broke out some code into a new
 *                                           method.
 * Apr 24, 2014    2925    Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * Jun 17, 2014    3982    Chris.Golden      Changed to use new choice button
 *                                           component.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class UiBuilder {

    // Public Static Constants

    /**
     * Index used to indicate that nothing should be selected in various SWT
     * widgets.
     */
    public static final int NO_SELECTION = -1;

    // Public Enumerated Types

    /**
     * Types of composites that may be built.
     */
    public enum CompositeType {
        SINGLE_ROW, MULTI_ROW_VERTICALLY_CONSTRAINED, MULTI_ROW_VERTICALLY_EXPANDING
    }

    // Public Static Methods

    /**
     * Build a composite in which to lay out megawidget components. The
     * composite will use a grid layout.
     * 
     * @param parent
     *            Parent composite.
     * @param numColumns
     *            Number of columns that the composite should contain.
     * @param flags
     *            Flags for the new composite.
     * @param specifier
     *            Specifier for the megawidget for which the composite is being
     *            built.
     * @param type
     *            Type of composite to be built.
     * @return New composite.
     */
    public static Composite buildComposite(Composite parent, int numColumns,
            int flags, CompositeType type, IControlSpecifier specifier) {
        Composite panel = new Composite(parent, flags);
        GridLayout gridLayout = new GridLayout(numColumns, false);
        gridLayout.marginWidth = 0;
        if (type == CompositeType.SINGLE_ROW) {
            gridLayout.marginHeight = 0;
            gridLayout.horizontalSpacing = 10;
        }
        panel.setLayout(gridLayout);
        GridData gridData = new GridData(SWT.FILL,
                (type != CompositeType.SINGLE_ROW ? SWT.FILL : SWT.CENTER),
                true, (type == CompositeType.MULTI_ROW_VERTICALLY_EXPANDING));
        gridData.horizontalSpan = specifier.getWidth();
        gridData.verticalIndent = specifier.getSpacing();
        panel.setLayoutData(gridData);
        return panel;
    }

    /**
     * Build a label for a megawidget if one is required by the specifier.
     * 
     * @param parent
     *            Parent composite.
     * @param specifier
     *            Specifier for the megawidget for which the label is to be
     *            created.
     * @return New label if appropriate, otherwise <code>null</code>.
     */
    public static Label buildLabel(Composite parent, ISpecifier specifier) {
        if ((specifier.getLabel() != null)
                && (specifier.getLabel().length() > 0)) {
            return buildLabel(parent, specifier.getLabel(), specifier);
        }
        return null;
    }

    /**
     * Build a label for a megawidget if one is required by the specifier.
     * 
     * @param parent
     *            Parent composite.
     * @param specifier
     *            Specifier for the megawidget for which the label is to be
     *            created.
     * @param columnSpan
     *            Number of columns that the label should span.
     * @return New label if appropriate, otherwise <code>null</code>.
     */
    public static Label buildLabel(Composite parent,
            MegawidgetSpecifier specifier, int columnSpan) {
        if ((specifier.getLabel() != null)
                && (specifier.getLabel().length() > 0)) {
            Label label = buildLabel(parent, specifier.getLabel(), specifier);
            ((GridData) label.getLayoutData()).horizontalSpan = columnSpan;
            return label;
        }
        return null;
    }

    /**
     * Build an arbitrary label for a megawidget.
     * 
     * @param parent
     *            Parent composite.
     * @param text
     *            Text to be displayed.
     * @param specifier
     *            Specifier for the megawidget for which the label is to be
     *            created.
     * @return New label.
     */
    public static Label buildLabel(Composite parent, String text,
            ISpecifier specifier) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(text);
        label.setEnabled(specifier.isEnabled());
        label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        return label;
    }

    /**
     * Build a GUI consisting of choice buttons, one per choice, based upon what
     * the specifier indicates is appropriate. Each choice button may optionally
     * have one or more detail megawidgets adjacent to it, and zero or more
     * detail megawidgets laid out in subsequent rows below it.
     * 
     * @param parent
     *            Parent composite in which to place the created widgets.
     * @param specifier
     *            Specifier governing what label, if any, should be used, and
     *            what choices are required.
     * @param buttonFlags
     *            Integer comprised of flags defined within {@link SWT} that are
     *            to be passed to the constructor of each button that is
     *            created.
     * @param detailChildrenManager
     *            Manager of any child detail megawidgets that are created; may
     *            be <code>null</code> if <code>specifier</code> does not
     *            specify any such megawidgets.
     * @param listener
     *            Listener to be installed in order to respond to button
     *            selection events. Note that if the buttons to be created are
     *            radio buttons, the listener must deselect any other buttons
     *            when one button is selected, since this behavior will not
     *            occur automatically.
     * @return Created choice buttons.
     * @throws MegawidgetException
     *             If an error occurs while creating or initializing any
     *             megawidgets acting as detail fields for the various choices.
     */
    public static List<ChoiceButtonComponent> buildChoiceButtons(
            Composite parent,
            FlatChoicesWithDetailMegawidgetSpecifier<?> specifier,
            int buttonFlags,
            BoundedChoicesDetailChildrenManager detailChildrenManager,
            SelectionListener listener) throws MegawidgetException {

        /*
         * For each value, add a choice button.
         */
        int buttonWidth = -1;
        boolean radioButtons = ((buttonFlags & SWT.RADIO) != 0);
        List<ChoiceButtonComponent> buttons = new ArrayList<>();
        List<Control> controls = new ArrayList<>();
        List<GridData> buttonsGridData = new ArrayList<>();
        int greatestHeight = 0;
        for (Object choice : specifier.getChoices()) {

            /*
             * If there are additional megawidgets to be placed to the right of
             * the choice button, create a composite to act as a parent for both
             * the choice button and the additional megawidgets.
             */
            String identifier = specifier.getIdentifierOfNode(choice);
            List<IControlSpecifier> detailSpecifiers = specifier
                    .getDetailFieldsForChoice(identifier);
            Composite choiceParent = getOrCreateCompositeForComponentWithDetailMegawidgets(
                    detailSpecifiers, parent,
                    (radioButtons ? SWT.NO_RADIO_GROUP : SWT.NONE));

            /*
             * Create the choice button. The grid data for the button must grab
             * excess vertical space because its minimumHeight may end up being
             * used below. If the button width has not yet been calculated,
             * compute it so that it may be used when indenting any additional
             * megawidgets that are on a row below the button.
             */
            ChoiceButtonComponent button = new ChoiceButtonComponent(
                    choiceParent, radioButtons, buttonFlags,
                    specifier.isEnabled(), identifier,
                    specifier.getNameOfNode(choice));
            if (buttonWidth == -1) {
                buttonWidth = button.getButtonNonTextWidth();
            }
            buttonsGridData.add(button.getGridData());
            buttons.add(button);

            /*
             * If there are additional megawidgets, lay out the composite in
             * which the choice button and the first row of said megawidgets are
             * found, and create the megawidgets.
             */
            if (choiceParent != parent) {
                choiceParent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
                        true, false));
                controls.add(choiceParent);
                controls.addAll(detailChildrenManager
                        .createDetailChildMegawidgetsForChoice(button,
                                choiceParent, parent, buttonWidth,
                                specifier.isEnabled(), detailSpecifiers));
                int height = choiceParent.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
                if (height > greatestHeight) {
                    greatestHeight = height;
                }
            } else {
                controls.add(button.getButton().getParent());
            }
        }

        /*
         * Determine which detail megawidgets that were created take up the full
         * width of this megawidget, and align all such megawidgets' components
         * to bring some visual order to the widget soup.
         */
        if (detailChildrenManager != null) {
            List<IControl> fullWidthDetailMegawidgets = new ArrayList<>();
            for (IControl detailMegawidget : detailChildrenManager
                    .getDetailMegawidgets()) {
                if (((IControlSpecifier) detailMegawidget.getSpecifier())
                        .isFullWidthOfDetailPanel()) {
                    fullWidthDetailMegawidgets.add(detailMegawidget);
                }
            }
            ControlComponentHelper
                    .alignMegawidgetsElements(fullWidthDetailMegawidgets);
        }

        /*
         * Set the tab ordering; this is needed because having additional
         * widgets next to any of the choice buttons messes up SWT's tab
         * ordering.
         */
        parent.setTabList(controls.toArray(new Control[controls.size()]));

        /*
         * If at least one choice has additional megawidgets, use the greatest
         * height of any one row for all rows.
         */
        if (greatestHeight > 0) {
            for (GridData buttonGridData : buttonsGridData) {
                buttonGridData.minimumHeight = greatestHeight;
            }
            for (ChoiceButtonComponent button : buttons) {
                button.parentMinimumHeightChanged(greatestHeight);
            }
        }

        /*
         * Bind each choice button selection event to trigger a change in the
         * record of the state for the widget.
         */
        for (ChoiceButtonComponent button : buttons) {
            button.addSelectionListener(listener);
        }

        /*
         * Return the list of choice buttons.
         */
        return ImmutableList.copyOf(buttons);
    }

    /**
     * Get the composite to be used to hold a megawidget component, or create
     * such a composite if the component has detail megawidgets associated with
     * it.
     * 
     * @param detailSpecifiers
     *            List of detail megawidget specifiers, if there are any
     *            required for this component, or <code>null</code> if there are
     *            no detail megawidgets.
     * @param originalComposite
     *            Composite that holds either the component directly, or if
     *            there are detail megawidgets, that holds the composite to be
     *            created.
     * @param flags
     *            Flags to be passed to the composite when building the latter;
     *            must be valid flags for building a {@link Composite}.
     * @return If there were no detail specifiers, <code>
     *         originalComposite</code>; otherwise, a newly constructed
     *         composite to be used to hold both the megawidget component and
     *         its detail megawidgets.
     */
    public static Composite getOrCreateCompositeForComponentWithDetailMegawidgets(
            List<IControlSpecifier> detailSpecifiers,
            Composite originalComposite, int flags) {

        /*
         * If there are additional megawidgets to be placed to the right of the
         * choice button, create a composite to act as a parent for both the
         * choice button and the additional megawidgets.
         */
        Composite choiceParent = originalComposite;
        int additionalMegawidgetsCount = (detailSpecifiers == null ? 0
                : detailSpecifiers.size());
        if (additionalMegawidgetsCount > 0) {
            choiceParent = new Composite(originalComposite, flags);
            GridLayout layout = new GridLayout(additionalMegawidgetsCount + 1,
                    false);
            layout.horizontalSpacing = 3;
            layout.marginWidth = layout.marginHeight = 0;
            choiceParent.setLayout(layout);
        }
        return choiceParent;
    }

    /**
     * Build the Select All and Select None buttons for the given specifier, if
     * appropriate.
     * 
     * @param parent
     *            Parent composite.
     * @param specifier
     *            Specifier for which to build the buttons.
     * @param allListener
     *            Listener to be used for invocations of the All button.
     * @param noneListener
     *            Listener to be used for invocations of the None button.
     * @return Buttons created, or an empty list if none were needed.
     */
    public static List<Button> buildAllNoneButtons(Composite parent,
            IMultiSelectableSpecifier specifier, SelectionListener allListener,
            SelectionListener noneListener) {
        if (specifier.shouldShowAllNoneButtons()) {
            Composite allNoneContainer = new Composite(parent, SWT.FILL);
            FillLayout fillLayout = new FillLayout();
            fillLayout.spacing = 10;
            fillLayout.marginWidth = 10;
            fillLayout.marginHeight = 5;
            allNoneContainer.setLayout(fillLayout);
            Button allButton = new Button(allNoneContainer, SWT.PUSH);
            allButton.setText("  All  ");
            allButton.setEnabled(specifier.isEnabled()
                    && specifier.isEditable());
            allButton.addSelectionListener(allListener);
            Button noneButton = new Button(allNoneContainer, SWT.PUSH);
            noneButton.setText("  None  ");
            noneButton.setEnabled(specifier.isEnabled()
                    && specifier.isEditable());
            noneButton.addSelectionListener(noneListener);
            allNoneContainer.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER,
                    true, false));
            return Lists.newArrayList(allButton, noneButton);
        }
        return Collections.emptyList();
    }

    /**
     * Determine whether or not the specified key event is used by a spinner
     * widget to increment or decrement its value.
     * 
     * @param event
     *            Key event to be tested.
     * @return True if the specified key code is used by a spinner widget to
     *         increment or decrement its value, false otherwise.
     */
    public static boolean isSpinnerValueChanger(KeyEvent event) {
        return ((event.keyCode == SWT.ARROW_UP)
                || (event.keyCode == SWT.ARROW_DOWN)
                || (event.keyCode == SWT.PAGE_UP) || (event.keyCode == SWT.PAGE_DOWN));
    }

    /**
     * Determine whether or not the specified key event is used by a date-time
     * widget to increment or decrement its value.
     * 
     * @param event
     *            Key event to be tested.
     * @return True if the specified key code is used by a date-time widget to
     *         increment or decrement its value, false otherwise.
     */
    public static boolean isDateTimeValueChanger(KeyEvent event) {
        return ((event.keyCode == SWT.ARROW_UP) || (event.keyCode == SWT.ARROW_DOWN));
    }
}
