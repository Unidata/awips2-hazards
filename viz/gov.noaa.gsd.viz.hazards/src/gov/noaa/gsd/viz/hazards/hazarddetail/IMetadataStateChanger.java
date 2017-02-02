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

import com.raytheon.uf.common.util.Pair;

/**
 * Description: Interface describing the methods required in an HMI component
 * that manipulates metadata. The qualifier is the hazard event version, while
 * the identifier is that of a particular element of metadata.
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
 * Sep 16, 2014    4753    Chris.Golden Changed to support setting of mutable
 *                                      properties.
 * Oct 04, 2016   22736    Chris.Golden Added flag indicating whether or not
 *                                      metadata has its interdependency script
 *                                      reinitialize if unchanged, so that when
 *                                      a hazard event is selected, it triggers
 *                                      the reinitialization.
 * Feb 03, 2017   15556    Chris.Golden Changed the event identifier to be an
 *                                      event version identifier, and added
 *                                      editability parameter.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IMetadataStateChanger extends
        IQualifiedStateChanger<Pair<String, Integer>, String, Serializable> {

    // Public Methods

    /**
     * Set the megawidget specifier manager to be used to represent the metadata
     * within widgets for the currently visible event.
     * 
     * @param qualifier
     *            Event version identifier for which the megawidget specifiers
     *            are intended.
     * @param specifierManager
     *            Megawidget specifier manager to be used.
     * @param metadataStates
     *            States for the metadata.
     * @param editable
     *            Flag indicating whether or not the widgets to be created based
     *            upon the new metadata specifier manager should be editable.
     * @param reinitializeIfUnchanged
     *            Flag indicating whether or not the metadata manager, if
     *            unchanged as a result of this call, should reinitialize its
     *            components.
     */
    public void setMegawidgetSpecifierManager(Pair<String, Integer> qualifier,
            MegawidgetSpecifierManager specifierManager,
            Map<String, Serializable> metadataStates, boolean editable,
            boolean reinitializeIfUnchanged);

    /**
     * Change the metadata megawidget mutable properties to include those
     * specified.
     * 
     * @param qualifier
     *            Event version identifier for which to change the mutable
     *            properties.
     * @param mutableProperties
     *            Mutable properties to be changed.
     */
    public void changeMegawidgetMutableProperties(
            Pair<String, Integer> qualifier,
            Map<String, Map<String, Object>> mutableProperties);
}
