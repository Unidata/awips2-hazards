/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.config.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;

/**
 * Class representing a specification for the command buttons within a dialog.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * May 18, 2018    3782    Chris.Golden  Initial creation.
 *
 * </pre>
 *
 * @author Chris.Golden
 */
public class DialogButtonsSpecifier {

    // Private Variables

    /**
     * Identifiers of the specified buttons, in the order specified.
     */
    private final List<String> buttonIdentifiers;

    /**
     * Map of button identifiers to the labels they should display.
     */
    private final Map<String, String> labelsForButtonIdentifiers;

    /**
     * Index of the button within {@link #buttonIdentifiers} that is considered
     * the same as the "X" button in the dialog's title bar.
     */
    private final int closeButtonIndex;

    /**
     * Index of the button within {@link #buttonIdentifiers} that is considered
     * to be the "cancel" button for the dialog. If <code>-1</code>, no button
     * has this function.
     */
    private final int cancelButtonIndex;

    /**
     * Index of the button within {@link #buttonIdentifiers} that is to be the
     * default for the dialog.
     */
    private final int defaultButtonIndex;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param buttonIdentifiers
     *            Identifiers of the specified buttons, in the order specified.
     *            Note that all identifiers must be unique with the list.
     * @param labelsForButtonIdentifiers
     *            Map of button identifiers to the labels they should display.
     * @param closeButtonIndex
     *            Index of the button within <code>buttonIdentifiers</code> that
     *            is considered the same as the "X" button in the dialog's title
     *            bar.
     * @param cancelButtonIndex
     *            Index of the button within <code>buttonIdentifiers</code> that
     *            is considered to be the "cancel" button for the dialog. If
     *            <code>-1</code>, no button has this function.
     * @param defaultButtonIndex
     *            Index of the button within <code>buttonIdentifiers</code> that
     *            is to be the default for the dialog.
     */
    public DialogButtonsSpecifier(List<String> buttonIdentifiers,
            Map<String, String> labelsForButtonIdentifiers,
            int closeButtonIndex, int cancelButtonIndex,
            int defaultButtonIndex) {
        this.buttonIdentifiers = buttonIdentifiers;
        this.labelsForButtonIdentifiers = labelsForButtonIdentifiers;
        this.closeButtonIndex = closeButtonIndex;
        this.cancelButtonIndex = cancelButtonIndex;
        this.defaultButtonIndex = defaultButtonIndex;
    }

    /**
     * Construct an instance based upon the specified list of maps, with each
     * map representing a button specification. Each map has the following
     * entries:
     * 
     * <dl>
     * <dt><code>identifier</code>
     * <dt>
     * <dd>Unique (for the list of buttons) identifier of the button.</dd>
     * <dt><code>label</code>
     * <dt>
     * <dd>Label of the button.</dd>
     * <dt><code>close</code>
     * <dt>
     * <dd>Optional boolean indicating whether or not the button is considered
     * to be equivalent to the "X" button in the dialog's title bar. Only one of
     * the maps should have this property set to <code>true</code>. If multiple
     * ones do, only the last dictionary with such a value will be considered to
     * be equivalent to the "X" button. If none have this property set to
     * <code>true</code>, the last map in the list will be considered equivalent
     * to the "X" button.</dd>
     * <dt><code>cancel</code>
     * <dt>
     * <dd>Optional boolean indicating whether or not the button is considered
     * the "cancel" button. None of the maps need to have this property set to
     * <code>true</code>, as cancellation does not have to be an option. If more
     * than one have this property as <code>true</code>, only the last map with
     * such a value will be considered to be the "cancel" button.</dd>
     * </dl>
     * <dt><code>default</code>
     * <dt>
     * <dd>Optional boolean indicating whether or not the button is the default
     * for the dialog. Only one of the maps should have this property set to
     * <code>true</code>. If multiple ones do, only the first map with such a
     * value will be considered to be the default button. If none have this
     * property set to <code>true</code>, the first map in the list will be
     * considered the default button.</dd>
     * </dl>
     * 
     * @param rawSpecifier
     *            List of maps to be parsed to create the specifier.
     * @throws IllegalArgumentException
     *             If <code>rawSpecifier</code> does not hold a valid
     *             specification.
     */
    public DialogButtonsSpecifier(List<? extends Map<String, ?>> rawSpecifier)
            throws IllegalArgumentException {

        /*
         * Iterate through the list, ensuring each definition of a custom button
         * is valid.
         */
        int closeIndex = -1;
        int cancelIndex = -1;
        int defaultIndex = -1;
        List<String> identifiers = new ArrayList<>(rawSpecifier.size());
        Map<String, String> labelsForIdentifiers = new HashMap<>(
                rawSpecifier.size(), 1.0f);
        Set<String> identifierSet = new HashSet<>(rawSpecifier.size(), 1.0f);
        int count = 0;
        for (Map<String, ?> buttonMap : rawSpecifier) {

            /*
             * Get the identifier, ensuring it is a non-empty string and is
             * unique.
             */
            String identifier = null;
            try {
                identifier = (String) buttonMap.get(HazardConstants.IDENTIFIER);
                if ((identifier == null) || identifier.isEmpty()) {
                    throw new IllegalArgumentException();
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("button " + count
                        + " has null, non-string, or empty identifier");
            }
            if (identifierSet.contains(identifier)) {
                throw new IllegalArgumentException("button " + count
                        + " has duplicate identifier \"" + identifier + "\"");
            }
            identifierSet.add(identifier);
            identifiers.add(identifier);

            /*
             * Get the label, ensuring that it is a non-empty string.
             */
            String label = null;
            try {
                label = (String) buttonMap.get(HazardConstants.LABEL);
                if ((label == null) || label.isEmpty()) {
                    throw new IllegalArgumentException();
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("button " + count
                        + " has null, non-string, or empty label");
            }
            labelsForIdentifiers.put(identifier, label);

            /*
             * If this button is to be the close, cancel, and/or default button,
             * record it as such.
             */
            if (Boolean.TRUE.equals(buttonMap.get(HazardConstants.CLOSE))) {
                closeIndex = count;
            }
            if (Boolean.TRUE.equals(buttonMap.get(HazardConstants.CANCEL))) {
                cancelIndex = count;
            }
            if ((defaultIndex == -1) && Boolean.TRUE
                    .equals(buttonMap.get(HazardConstants.DEFAULT))) {
                defaultIndex = count;
            }

            count++;
        }

        /*
         * If not close button was specified, make the last button the close
         * button.
         */
        if (closeIndex == -1) {
            closeIndex = count - 1;
        }

        /*
         * If no default button was specified, make the first button the default
         * button.
         */
        if (defaultIndex == -1) {
            defaultIndex = 0;
        }

        this.buttonIdentifiers = ImmutableList.copyOf(identifiers);
        this.labelsForButtonIdentifiers = ImmutableMap
                .copyOf(labelsForIdentifiers);
        this.closeButtonIndex = closeIndex;
        this.cancelButtonIndex = cancelIndex;
        this.defaultButtonIndex = defaultIndex;
    }

    /**
     * Get the identifiers of the specified buttons, in the order specified.
     * 
     * @return Identifiers.
     */
    public List<String> getButtonIdentifiers() {
        return buttonIdentifiers;
    }

    /**
     * Get the map of button identifiers to the labels they should display.
     * 
     * @return Labels for button identifiers.
     */
    public Map<String, String> getLabelsForButtonIdentifiers() {
        return labelsForButtonIdentifiers;
    }

    /**
     * Get the index of the button within the list returned by
     * {@link #getButtonIdentifiers()} that is considered the same as the "X"
     * button in the dialog's title bar.
     * 
     * @return Index of the equivalent of the "X" button.
     */
    public int getCloseButtonIndex() {
        return closeButtonIndex;
    }

    /**
     * Get the index of the button within the list returned by
     * {@link #getButtonIdentifiers()} that is considered to be the "cancel"
     * button for the dialog. If <code>-1</code>, no button has this function.
     * 
     * @return Index of the "cancel" button.
     */
    public int getCancelButtonIndex() {
        return cancelButtonIndex;
    }

    /**
     * Get the index of the button within the list returned by
     * {@link #getButtonIdentifiers()} that is to be the default for the dialog.
     * 
     * @return Index of the default button.
     */
    public int getDefaultButtonIndex() {
        return defaultButtonIndex;
    }
}
