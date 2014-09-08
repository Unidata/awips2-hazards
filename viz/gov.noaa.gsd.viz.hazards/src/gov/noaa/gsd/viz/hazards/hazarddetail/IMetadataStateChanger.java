/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.hazarddetail;

import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;
import gov.noaa.gsd.viz.mvp.widgets.IQualifiedStateChanger;

import java.io.Serializable;
import java.util.Map;

/**
 * Description: Interface describing the methods required in an HMI component
 * that manipulates metadata. The qualifier is the hazard event, while the
 * identifier is that of a particular element of metadata.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * May 09, 2014    2925    Chris.Golden Initial creation.
 * Aug 15, 2014    4243    Chris.Golden Modified to take a qualifier so as
 *                                      to allow a thread-safe way to determine
 *                                      what hazard event a change is intended
 *                                      for.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IMetadataStateChanger extends
        IQualifiedStateChanger<String, String, Serializable> {

    // Public Methods

    /**
     * Set the megawidget specifier manager to be used to represent the metadata
     * within widgets for the currently visible event.
     * 
     * @param qualifier
     *            Event identifier for which the megawidget specifiers are
     *            intended.
     * @param specifierManager
     *            Megawidget specifier manager to be used.
     * @param metadataStates
     *            States for the metadata.
     */
    public void setMegawidgetSpecifierManager(String qualifier,
            MegawidgetSpecifierManager specifierManager,
            Map<String, Serializable> metadataStates);
}
