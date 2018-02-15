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

import java.io.Serializable;
import java.util.Map;

import com.raytheon.uf.common.util.Pair;

import gov.noaa.gsd.common.utilities.IRunnableAsynchronousScheduler;
import gov.noaa.gsd.viz.hazards.ui.QualifiedPrincipalRunnableTask;
import gov.noaa.gsd.viz.hazards.ui.QualifiedStateChangerDelegate;
import gov.noaa.gsd.viz.hazards.ui.ViewPartQualifiedWidgetDelegateHelper;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;

/**
 * A metadata state changer delegate, used by a {@link HazardDetailView} object
 * to represent metadata state changers within its associated
 * {@link HazardDetailViewPart}.
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
 * Sep 16, 2014    4753    Chris.Golden Changed to allow setting of mutable
 *                                      properties.
 * Oct 04, 2016   22736    Chris.Golden Added flag indicating whether or not
 *                                      metadata has its interdependency script
 *                                      reinitialize if unchanged, so that when
 *                                      a hazard event is selected, it triggers
 *                                      the reinitialization.
 * Feb 03, 2017   15556    Chris.Golden Added editability parameter.
 * Dec 20, 2017   20739    Chris.Golden Added code to allow the removal of 
 *                                      megawidget specifier managers that were
 *                                      cached so that they can be forced to be
 *                                      regenerated whenever a hazard event is
 *                                      reselected.
 * May 01, 2018   15561    Chris.Golden Removed obsolete method.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
class MetadataStateChangerDelegate extends
        QualifiedStateChangerDelegate<Pair<String, Integer>, String, Serializable, IMetadataStateChanger>
        implements IMetadataStateChanger {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param helper
     *            Widget delegate helper.
     * @param handlerScheduler
     *            Handler invocation scheduler.
     */
    public MetadataStateChangerDelegate(
            ViewPartQualifiedWidgetDelegateHelper<Pair<String, Integer>, String, HazardDetailViewPart, IMetadataStateChanger> helper,
            IRunnableAsynchronousScheduler handlerScheduler) {
        super(helper, handlerScheduler);
    }

    // Public Methods

    @Override
    public void setMegawidgetSpecifierManager(
            final Pair<String, Integer> eventVersionIdentifier,
            final MegawidgetSpecifierManager specifierManager,
            final Map<String, Serializable> metadataStates,
            final boolean editable, final boolean reinitializeIfUnchanged) {
        runOrScheduleTask(
                new QualifiedPrincipalRunnableTask<Pair<String, Integer>, String, IMetadataStateChanger>() {

                    @Override
                    public void run() {
                        getPrincipal().setMegawidgetSpecifierManager(
                                eventVersionIdentifier, specifierManager,
                                metadataStates, editable,
                                reinitializeIfUnchanged);
                    }
                });
    }

    @Override
    public void removeMegawidgetSpecifierManager(
            final Pair<String, Integer> eventVersionIdentifier) {
        runOrScheduleTask(
                new QualifiedPrincipalRunnableTask<Pair<String, Integer>, String, IMetadataStateChanger>() {

                    @Override
                    public void run() {
                        getPrincipal().removeMegawidgetSpecifierManager(
                                eventVersionIdentifier);
                    }
                });
    }
}
