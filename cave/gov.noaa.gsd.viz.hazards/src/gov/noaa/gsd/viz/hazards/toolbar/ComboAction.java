/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.toolbar;

import org.eclipse.jface.action.IToolBarManager;

/**
 * Abstract class from which may be derived classes encapsulating toolbar combo
 * boxes.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * 
 * </pre>
 * 
 * @author Chris.Golden
 */
public abstract class ComboAction extends PulldownAction {

    // Private Variables

    /**
     * Description, to be used as a prefix for the tooltip text.
     */
    private final String description;

    /**
     * Toolbar manager.
     */
    private final IToolBarManager toolBarManager;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param description
     *            Description of this action.
     * @param toolBarManager
     *            Toolbar manager to which this action will be added.
     */
    public ComboAction(String description, IToolBarManager toolBarManager) {
        super("(none)");
        this.description = description;
        this.toolBarManager = toolBarManager;
        setToolTipText(description + ": (none)");
    }

    // Public Methods

    /**
     * Set the visual state to indicate the specified choice is the current
     * choice.
     * 
     * @param choiceText
     *            Text of the choice to be shown as current.
     */
    public void setSelectedChoice(String choiceText) {

        // Set the text of the action to the newly selected choice,
        // and force the toolbar manager to update; without the
        // latter, the drop-down widget disappears from the toolbar.
        setText(choiceText);
        toolBarManager.update(true);

        // Set the tooltip text to include the choice.
        setToolTipText(description + ": " + choiceText);
    }
}
