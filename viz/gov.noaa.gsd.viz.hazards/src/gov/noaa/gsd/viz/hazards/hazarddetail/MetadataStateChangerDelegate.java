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

import gov.noaa.gsd.common.utilities.IRunnableAsynchronousScheduler;
import gov.noaa.gsd.viz.hazards.ui.QualifiedPrincipalRunnableTask;
import gov.noaa.gsd.viz.hazards.ui.QualifiedStateChangerDelegate;
import gov.noaa.gsd.viz.hazards.ui.ViewPartQualifiedWidgetDelegateHelper;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;

import java.io.Serializable;
import java.util.Map;

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
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
class MetadataStateChangerDelegate
        extends
        QualifiedStateChangerDelegate<String, String, Serializable, IMetadataStateChanger>
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
            ViewPartQualifiedWidgetDelegateHelper<String, String, HazardDetailViewPart, IMetadataStateChanger> helper,
            IRunnableAsynchronousScheduler handlerScheduler) {
        super(helper, handlerScheduler);
    }

    // Public Methods

    @Override
    public void setMegawidgetSpecifierManager(final String eventIdentifier,
            final MegawidgetSpecifierManager specifierManager,
            final Map<String, Serializable> metadataStates) {
        runOrScheduleTask(new QualifiedPrincipalRunnableTask<String, String, IMetadataStateChanger>() {

            @Override
            public void run() {
                getPrincipal().setMegawidgetSpecifierManager(eventIdentifier,
                        specifierManager, metadataStates);
            }
        });
    }
}
