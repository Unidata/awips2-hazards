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
import java.util.List;

import org.eclipse.swt.SWT;
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
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class UiBuilder {

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
    public static Label buildLabel(Composite parent,
            MegawidgetSpecifier specifier) {
        if ((specifier.getLabel() != null)
                && (specifier.getLabel().length() > 0)) {
            return buildLabel(parent, specifier.getLabel(), specifier);
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
            MegawidgetSpecifier specifier) {
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
     *            Integer comprised of flags defined within <code>SWT</code>
     *            that are to be passed to the constructor of each button that
     *            is created.
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
    public static List<Button> buildChoiceButtons(Composite parent,
            FlatChoicesWithDetailMegawidgetSpecifier specifier,
            int buttonFlags,
            ChoicesDetailChildrenManager detailChildrenManager,
            SelectionListener listener) throws MegawidgetException {

        // For each value, add a choice button.
        int buttonWidth = -1;
        boolean radioButtons = ((buttonFlags & SWT.RADIO) != 0);
        List<Button> buttons = Lists.newArrayList();
        List<Control> controls = Lists.newArrayList();
        List<GridData> buttonsGridData = Lists.newArrayList();
        int greatestHeight = 0;
        for (Object choice : specifier.getChoices()) {

            // If there are additional megawidgets to be
            // placed to the right of the choice button,
            // create a composite to act as a parent for
            // both the choice button and the additional
            // megawidgets.
            String identifier = specifier.getIdentifierOfNode(choice);
            Composite choiceParent = parent;
            List<IControlSpecifier> detailSpecifiers = specifier
                    .getDetailFieldsForChoice(identifier);
            int additionalMegawidgetsCount = (detailSpecifiers == null ? 0
                    : detailSpecifiers.size());
            if (additionalMegawidgetsCount > 0) {
                choiceParent = new Composite(parent,
                        (radioButtons ? SWT.NO_RADIO_GROUP : SWT.NONE));
                GridLayout layout = new GridLayout(
                        additionalMegawidgetsCount + 1, false);
                layout.horizontalSpacing = 3;
                layout.marginWidth = layout.marginHeight = 0;
                choiceParent.setLayout(layout);
            }

            // Create the choice button. The grid data
            // for the button must grab excess vertical
            // space because its minimumHeight may end
            // up being used below. If the button width
            // has not yet been calculated, compute it
            // so that it may be used when indenting
            // any additional megawidgets that are on
            // a row below the button.
            Button button = new Button(choiceParent, buttonFlags);
            if (buttonWidth == -1) {
                buttonWidth = button.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
            }
            button.setText(specifier.getNameOfNode(choice));
            button.setData(identifier);
            button.setEnabled(specifier.isEnabled());
            GridData buttonGridData = new GridData(SWT.LEFT, SWT.CENTER, false,
                    true);
            button.setLayoutData(buttonGridData);
            buttonsGridData.add(buttonGridData);
            buttons.add(button);

            // If there are additional megawidgets, lay
            // out the composite in which the choice
            // button and the first row of said mega-
            // widgets are found, and create the mega-
            // widgets.
            if (additionalMegawidgetsCount > 0) {
                choiceParent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
                        true, false));
                controls.add(choiceParent);
                controls.addAll(detailChildrenManager
                        .createDetailChildMegawidgetsForChoice(button,
                                choiceParent, parent, buttonWidth,
                                detailSpecifiers));
                int height = choiceParent.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
                if (height > greatestHeight) {
                    greatestHeight = height;
                }
            } else {
                controls.add(button);
            }
        }

        // Determine which detail megawidgets that were
        // created take up the full width of this mega-
        // widget, and align all such megawidgets' com-
        // ponents to bring some visual order to the
        // widget soup.
        if (detailChildrenManager != null) {
            List<IControl> fullWidthDetailMegawidgets = Lists.newArrayList();
            for (IControl detailMegawidget : detailChildrenManager
                    .getDetailMegawidgets()) {
                if (((IControlSpecifier) detailMegawidget.getSpecifier())
                        .isFullWidthOfColumn()) {
                    fullWidthDetailMegawidgets.add(detailMegawidget);
                }
            }
            ControlComponentHelper
                    .alignMegawidgetsElements(fullWidthDetailMegawidgets);
        }

        // Set the tab ordering; this is needed because
        // having additional widgets next to any of the
        // choice buttons messes up SWT's tab ordering.
        parent.setTabList(controls.toArray(new Control[controls.size()]));

        // If at least one choice has additional mega-
        // widgets, use the greatest height of any one
        // row for all rows.
        if (greatestHeight > 0) {
            for (GridData buttonGridData : buttonsGridData) {
                buttonGridData.minimumHeight = greatestHeight;
            }
        }

        // Bind each choice button selection event to
        // trigger a change in the record of the state
        // for the widget.
        for (Button button : buttons) {
            button.addSelectionListener(listener);
        }

        // Return the label, if any, and the list of
        // choice buttons.
        return ImmutableList.copyOf(buttons);
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
}
