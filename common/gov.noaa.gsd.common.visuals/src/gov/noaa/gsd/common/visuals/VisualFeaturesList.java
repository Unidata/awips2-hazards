/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.common.visuals;

import java.util.ArrayList;

/**
 * Description: List of visual features. Each of the elements must have an
 * identifier that is unique within the list.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Feb 16, 2016   15676    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class VisualFeaturesList extends ArrayList<VisualFeature> {

    /**
     * Serialization version UID.
     */
    private static final long serialVersionUID = -2655616243967777587L;
}
