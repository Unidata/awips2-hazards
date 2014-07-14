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
 * Sep 24, 2013    2168    Chris.Golden      Changed erroneous "widget"
 *                                           references to "megawidget"
 *                                           in comments and variable
 *                                           names.
 * Apr 24, 2014    2925    Chris.Golden      Changed to work with new
 *                                           validator package, updated
 *                                           Javadoc and other comments.
 * Jun 17, 2014    3982    Chris.Golden      Changed to keep children synced
 *                                           with enabled and editable state.
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

        /*
         * Create the group panel in which to place the child megawidgets, and
         * give it its title if it has one.
         */
        Group group = new Group(parent, SWT.NONE);
        if (specifier.getLabel() != null) {
            group.setText(specifier.getLabel());
        }
        group.setEnabled(specifier.isEnabled());
        gridContainerPanel(group);
        setComposite(group);

        /*
         * Create its child megawidgets.
         */
        setChildren(createChildMegawidgets(group, specifier.getColumnCount(),
                specifier.isEnabled(), specifier.isEditable(),
                specifier.getChildMegawidgetSpecifiers(), paramMap));
    }
}
