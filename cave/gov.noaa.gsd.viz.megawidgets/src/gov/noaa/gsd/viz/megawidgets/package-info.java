/**
 * <p>
 * Contains megawidgets and their specifiers. Megawidgets are graphical user
 * interface elements, each of which is made up of one or more SWT widgets;
 * they offer a simple and consistent interface to other objects to allow
 * them to be enabled or disabled; made editable or read-only; where
 * appropriate, to notify a listener that they have been invoked; and, for
 * those widgets that hold state, to get or set that state. Furthermore,
 * megawidgets handle much of the grunt work that must be done when
 * configuring and laying out SWT widgets.
 * </p><p>
 * Megawidgets are not constructed directly; rather, they are built by
 * specifiers, which accept mappings of property names to values and build
 * megawidgets that conform to the specified property values. These in turn
 * are generally created and managed using the provided {@link
 * MegawidgetManager}. The latter are often created directly, but may
 * also be manufactured by {@link ParametersEditorFactory} instances. 
 * </p>
 */
package gov.noaa.gsd.viz.megawidgets;