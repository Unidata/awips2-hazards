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

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Description: Abstract base class for display settings.
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
public abstract class DisplaySettings implements IDisplaySettings {

    // Private Variables

    /**
     * Class of the megawidget to which these display settings apply.
     */
    private final Class<? extends IMegawidget> megawidgetClass;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param megawidgetClass
     *            Class of the megawidget to which these display settings apply.
     */
    public DisplaySettings(Class<? extends IMegawidget> megawidgetClass) {
        this.megawidgetClass = megawidgetClass;
    }

    // Public Methods

    @Override
    public Class<? extends IMegawidget> getMegawidgetClass() {
        return megawidgetClass;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
