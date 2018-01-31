/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.tools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.util.Pair;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ToolType;

import gov.noaa.gsd.viz.hazards.display.action.ToolAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;

/**
 * Description: Dialog for gathering parameters from the user in preparation for
 * the running of a tool. In addition to the parameters specified by the
 * superclass, the JSON string passed to this dialog may contain the following
 * parameters:
 * 
 * <dt><code>buttons</code></dt>
 * <dd>Optional list of one or more dictionaries, each of the latter defining a
 * button. Each dictionary has the following entries:
 * <dl>
 * <dt><code>identifier</code>
 * <dt>
 * <dd>Unique (for the list of buttons) identifier of the button.</dd>
 * <dt><code>label</code>
 * <dt>
 * <dd>Label of the button.</dd>
 * <dt><code>close</code>
 * <dt>
 * <dd>Optional boolean indicating whether or not the button's identifier is the
 * one provided to as one of the values entries provided once the dialog is
 * dismissed under the key <code>__dismissChoice__</code> if the user chooses
 * the "X" button in the dialog's title bar. Only one of the dictionaries should
 * have this property set to <code>true</code>. If multiple ones do, only the
 * last dictionary with such a value will be considered to be the "close"
 * button. If none have this property set to <code>true</code>, the last
 * dictionary in the list will be considered the "close" button.</dd>
 * <dt><code>cancel</code>
 * <dt>
 * <dd>Optional boolean indicating whether or not the button cancels the running
 * of the recommender. None of the dictionaries need to have this property set
 * to <code>true</code>, as cancellation does not have to be an option. If more
 * than one have this property as <code>true</code>, only the last dictionary
 * with such a value will be considered to be the "close" button.</dd>
 * </dl>
 * <dt><code>default</code>
 * <dt>
 * <dd>Optional boolean indicating whether or not the button is the default for
 * the dialog. Only one of the dictionaries should have this property set to
 * <code>true</code>. If multiple ones do, only the first dictionary with such a
 * value will be considered to be the default button. If none have this property
 * set to <code>true</code>, the first dictionary in the list will be considered
 * the default button.</dd>
 * </dl>
 * </dd>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 15, 2017   22757    Chris.Golden Initial creation.
 * Sep 27, 2017   38072    Chris.Golden Changed to work with new recommender
 *                                      manager.
 * Jan 30, 2018   45994    Chris.Golden Added the option of requesting custom
 *                                      buttons at the bottom of the dialog
 *                                      in place of the usual buttons. Also
 *                                      fixed bug that could potentially stop
 *                                      future recommenders from running if
 *                                      the user closed a dialog via the X
 *                                      button in the title bar.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ToolParameterDialog extends AbstractToolDialog {

    // Private Static Constants

    /**
     * OK button text.
     */
    private static final String OK_BUTTON_LABEL = "Run";

    // Private Variables

    /**
     * List of button definitions, each holding the identifier as its first
     * element and its label as its second. If <code>null</code>, no custom
     * buttons are defined.
     */
    private final List<Pair<String, String>> customButtonIdentifiersAndLabels;

    /**
     * Index into the {@link #customButtonIdentifierAndLabels} list at which the
     * button that is equivalent to the "X" button in the title bar is found. If
     * the former is <code>null</code>, this value is irrelevant.
     */
    private final int customButtonCloseIndex;

    /**
     * Index into the {@link #customButtonIdentifierAndLabels} list at which the
     * button that is the cancel button is found. If the former is
     * <code>null</code>, this value is irrelevant.
     */
    private final int customButtonCancelIndex;

    /**
     * Index into the {@link #customButtonIdentifierAndLabels} list at which the
     * button that is the default button is found. If the former is
     * <code>null</code>, this value is irrelevant.
     */
    private final int customButtonDefaultIndex;

    /**
     * Index of the button used to dismiss the dialog, if the dialog has been
     * dismissed and the dismissal is not a cancellation.
     */
    private int dismissingCustomButtonIndex;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param presenter
     *            Presenter.
     * @param parent
     *            Parent shell.
     * @param type
     *            Type of the tool.
     * @param jsonParams
     *            JSON string giving the parameters for this dialog. Within the
     *            set of all fields that are defined by these parameters, all
     *            the fields (megawidget specifiers) must have unique
     *            identifiers.
     */
    public ToolParameterDialog(ToolsPresenter presenter, Shell parent,
            ToolType type, String jsonParams) {
        super(presenter, parent, type, jsonParams);

        /*
         * Find the custom buttons list, if one exists, within the provided
         * parameter.
         */
        Dict dictionary = null;
        List<Map<String, Object>> buttonDictionaries = null;
        try {
            dictionary = Dict.getInstance(jsonParams);
            buttonDictionaries = dictionary
                    .getDynamicallyTypedValue(HazardConstants.BUTTONS_KEY);
            if ((buttonDictionaries != null) && buttonDictionaries.isEmpty()) {
                throw new IllegalArgumentException("empty custom button list");
            }
        } catch (Exception e) {
            statusHandler.error(
                    "ToolParameterDialog.<init>: Error: Problem parsing JSON for custom "
                            + "button dictionaries; ignoring any such dictionaries.",
                    e);
        }

        /*
         * Parse the custom buttons list, compiling information to be used.
         */
        if (buttonDictionaries != null) {

            /*
             * Iterate through the list, ensuring each definition of a custom
             * button is valid.
             */
            int customButtonCloseIndex = -1;
            int customButtonCancelIndex = -1;
            int customButtonDefaultIndex = -1;
            List<Pair<String, String>> customButtonIdentifiersAndLabels = new ArrayList<>(
                    buttonDictionaries.size());
            try {
                Set<String> customButtonIdentifiers = new HashSet<>(
                        buttonDictionaries.size(), 1.0f);
                int count = 0;
                for (Map<String, Object> buttonDictionary : buttonDictionaries) {

                    /*
                     * Ensure the identifier is a non-empty string, and is
                     * unique.
                     */
                    String identifier = (String) buttonDictionary
                            .get(HazardConstants.IDENTIFIER);
                    if ((identifier == null) || identifier.isEmpty()) {
                        throw new IllegalArgumentException("custom button "
                                + count + " has null or empty identifier");
                    } else if (customButtonIdentifiers.contains(identifier)) {
                        throw new IllegalArgumentException("custom button "
                                + count + " has duplicate identifier");
                    }
                    customButtonIdentifiers.add(identifier);

                    /*
                     * Ensure the label is a non-empty string.
                     */
                    String label = (String) buttonDictionary
                            .get(HazardConstants.LABEL);
                    if ((label == null) || label.isEmpty()) {
                        throw new IllegalArgumentException("custom button "
                                + count + " has null or empty label");
                    }

                    /*
                     * Remember this button.
                     */
                    customButtonIdentifiersAndLabels
                            .add(new Pair<>(identifier, label));

                    /*
                     * If this button is to be the close, cancel, and/or default
                     * button, record it as such.
                     */
                    if (Boolean.TRUE.equals(
                            buttonDictionary.get(HazardConstants.CLOSE))) {
                        customButtonCloseIndex = count;
                    }
                    if (Boolean.TRUE.equals(
                            buttonDictionary.get(HazardConstants.CANCEL))) {
                        customButtonCancelIndex = count;
                    }
                    if ((customButtonDefaultIndex == -1) && Boolean.TRUE.equals(
                            buttonDictionary.get(HazardConstants.DEFAULT))) {
                        customButtonDefaultIndex = count;
                    }

                    count++;
                }
                if (customButtonCloseIndex == -1) {
                    customButtonCloseIndex = count - 1;
                }
                if (customButtonDefaultIndex == -1) {
                    customButtonDefaultIndex = 0;
                }
            } catch (IllegalArgumentException e) {
                statusHandler.error(
                        "ToolParameterDialog.<init>: Error: Problem parsing JSON for custom "
                                + "button dictionaries; ignoring any such dictionaries.",
                        e);
                customButtonIdentifiersAndLabels = null;
            }
            this.customButtonIdentifiersAndLabels = customButtonIdentifiersAndLabels;
            this.customButtonCloseIndex = customButtonCloseIndex;
            this.customButtonCancelIndex = customButtonCancelIndex;
            this.customButtonDefaultIndex = customButtonDefaultIndex;
        } else {
            customButtonIdentifiersAndLabels = null;
            customButtonCloseIndex = customButtonCancelIndex = customButtonDefaultIndex = -1;
        }
    }

    // Protected Methods

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        if (customButtonIdentifiersAndLabels != null) {
            int count = 0;
            for (Pair<String, String> customButtonIdentifierAndLabel : customButtonIdentifiersAndLabels) {
                createButton(parent, count,
                        customButtonIdentifierAndLabel.getSecond(),
                        (count++ == customButtonDefaultIndex));
            }
        } else {
            createButton(parent, IDialogConstants.OK_ID,
                    ToolParameterDialog.OK_BUTTON_LABEL, true);
            createButton(parent, IDialogConstants.CANCEL_ID,
                    IDialogConstants.CANCEL_LABEL, false);
        }
    }

    @Override
    protected void buttonPressed(int buttonId) {

        /*
         * If custom buttons are in use, treat this as a cancellation if the
         * button that was pressed is considered equivalent to the cancel
         * button, or treat it as an OK press if not the cancel button,
         * recording the index of the button that was pressed so that that
         * information can be passed back as part of the state. If instead
         * custom buttons are not being used, treat it as a standard button
         * press.
         */
        if (customButtonIdentifiersAndLabels != null) {
            if (buttonId == customButtonCancelIndex) {
                cancelPressed();
            } else {
                dismissingCustomButtonIndex = buttonId;
                okPressed();
            }
        } else {
            super.buttonPressed(buttonId);
        }
    }

    @Override
    protected void handleShellCloseEvent() {

        /*
         * If custom buttons are in use, treat this as a press of whatever
         * button is equivalent to the title bar close button. Otherwise, just
         * treat it as a cancel.
         */
        if (customButtonIdentifiersAndLabels != null) {
            buttonPressed(customButtonCloseIndex);
        } else {
            cancelPressed();
        }
        close();
    }

    @Override
    protected void okPressed() {
        super.okPressed();

        /*
         * Get the state, adding the dismiss choice if custom buttons are in
         * use.
         */
        Map<String, Serializable> state = getState();
        if (customButtonIdentifiersAndLabels != null) {
            state.put(HazardConstants.DIALOG_DISMISS_CHOICE,
                    customButtonIdentifiersAndLabels
                            .get(dismissingCustomButtonIndex).getFirst());
        }

        getPresenter().publish(new ToolAction(
                ToolAction.RecommenderActionEnum.RUN_RECOMMENDER_WITH_PARAMETERS,
                null, getType(), state, null));
    }

    @Override
    protected void cancelPressed() {
        super.okPressed();
        getPresenter().publish(new ToolAction(
                ToolAction.RecommenderActionEnum.RUN_RECOMMENDER_WITH_PARAMETERS,
                null, getType(), null, null));
    }
}
