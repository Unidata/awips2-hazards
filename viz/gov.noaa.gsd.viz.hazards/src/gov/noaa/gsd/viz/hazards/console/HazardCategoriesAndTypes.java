/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.console;

import gov.noaa.gsd.common.utilities.JsonConverter;

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardCategoryAndTypes;

/**
 * Description: Encapsulation of a list of hazard categories and types, used for
 * conversion between lists of maps and {@link List}&lt;
 * {@link HazardCategoryAndTypes}&gt; objects.
 * <p>
 * TODO: This class is a standalone class only because the {@link JsonConverter}
 * appeared unable to deserialize JSON to instances of this class when the
 * latter was an inner class of {@link ConsoleTree}.
 * </p>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jan 13, 2017   15556    Chris.Golden Initial creation.
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
@SuppressWarnings("serial")
class HazardCategoriesAndTypes extends ArrayList<HazardCategoryAndTypes> {

    // Public Constructors

    /**
     * Construct an empty instance.
     */
    public HazardCategoriesAndTypes() {

        /**
         * No action.
         */
    }
}