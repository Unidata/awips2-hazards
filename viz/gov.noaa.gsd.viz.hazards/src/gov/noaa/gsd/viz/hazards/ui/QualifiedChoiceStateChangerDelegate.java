/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.ui;

import gov.noaa.gsd.common.utilities.IRunnableAsynchronousScheduler;
import gov.noaa.gsd.viz.mvp.widgets.IQualifiedChoiceStateChanger;

import java.util.List;

import org.eclipse.swt.widgets.Widget;

/**
 * A qualified choice state changer delegate, used to provide thread-safe access
 * to qualified choice state changers that are {@link Widget SWT widgets}, or
 * are composed of SWT widgets. The generic parameter <code>Q</code> provides
 * the type of state changer qualifier to be used, <code>I</code> provides the
 * type of state changer identifier to be used, <code>S</code> provides the type
 * of state, <code>C</code> provides the type of a choice, <code>D</code>
 * provides the type of displayable representations for the choices, and
 * <code>W</code> is the type of principal this delegate is to represent.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 15, 2014    4243    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class QualifiedChoiceStateChangerDelegate<Q, I, S, C, D, W extends IQualifiedChoiceStateChanger<Q, I, S, C, D>>
        extends QualifiedStateChangerDelegate<Q, I, S, W> implements
        IQualifiedChoiceStateChanger<Q, I, S, C, D> {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param helper
     *            Widget delegate helper.
     * @param handlerScheduler
     *            Handler invocation scheduler.
     */
    public QualifiedChoiceStateChangerDelegate(
            IQualifiedWidgetDelegateHelper<Q, I, W> helper,
            IRunnableAsynchronousScheduler handlerScheduler) {
        super(helper, handlerScheduler);
    }

    // Public Methods

    @Override
    public void setChoices(final Q qualifier, final I identifier,
            final List<C> choices, final List<D> choiceDisplayables,
            final S value) {
        runOrScheduleTask(new QualifiedPrincipalRunnableTask<Q, I, W>() {

            @Override
            public void run() {
                getPrincipal().setChoices(qualifier, identifier, choices,
                        choiceDisplayables, value);
            }
        });
    }
}
