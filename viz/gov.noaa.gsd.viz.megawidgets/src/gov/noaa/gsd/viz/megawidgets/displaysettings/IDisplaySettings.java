/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.megawidgets.displaysettings;

import gov.noaa.gsd.viz.megawidgets.IMegawidget;

/**
 * Description: Base interface describing display settings objects, used to
 * capture the display properties (scroll position, etc.) of a megawidget.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Oct 20, 2014    4818    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IDisplaySettings {

    // Public Static Constants

    /**
     * Null instance of this class, to be used for megawidgets that have no
     * display settings.
     */
    public static final IDisplaySettings NULL_DISPLAY_SETTINGS = new IDisplaySettings() {

        @Override
        public Class<? extends IMegawidget> getMegawidgetClass() {
            return IMegawidget.class;
        }
    };

    // Public Methods

    /**
     * Get the class of the megawidget to which these display settings apply.
     * 
     * @return Class of the megawidget.
     */
    public Class<? extends IMegawidget> getMegawidgetClass();
}
