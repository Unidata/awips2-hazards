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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * Group megawidget, a megawidget that itself contains other megawidgets, and
 * that surrounds them visually with an etched border with a title label.
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
 * @version 1.0
 * @see GroupSpecifier
 */
public class GroupMegawidget extends ContainerMegawidget {

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
     *             If an error occurs while creating or initializing child
     *             megawidgets.
     */
    protected GroupMegawidget(GroupSpecifier specifier, Composite parent,
            Map<String, Object> paramMap) throws MegawidgetException {
        super(specifier);

        // Create the group panel in which to place the child
        // widgets, and give it its title if it has one.
        Group panel = new Group(parent, SWT.NONE);
        if (specifier.getLabel() != null) {
            panel.setText(specifier.getLabel());
        }
        panel.setEnabled(specifier.isEnabled());
        gridContainerPanel(panel);
        composite = panel;

        // Create its child widgets.
        children = createChildMegawidgets(panel, specifier.getColumnCount(),
                specifier.getChildMegawidgetSpecifiers(), paramMap);
    }
}
