/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.megawidgets;

import gov.noaa.gsd.common.utilities.ICurrentTimeProvider;
import gov.noaa.gsd.common.utilities.collect.IParameterInfo;

import java.util.Map;

/**
 * Description: Listener for parameters editor events. The generic parameter
 * <code>K</code> indicates the class of the labels being used, and must be
 * identical to the <code>K</code> used in the call to
 * {@link ParametersEditorFactory#buildParametersEditor(org.eclipse.swt.widgets.Composite, java.util.List, java.util.Map, long, long, ICurrentTimeProvider, IParametersEditorListener)}
 * .
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 18, 2013    2336    Chris.Golden      Initial creation
 * Apr 10, 2014    2336    Chris.Golden      Augmented by using a generic
 *                                           parameter to specify the type of
 *                                           key being used.
 * Jun 30, 2014    3512    Chris.Golden      Added method to receive
 *                                           notification of resize events,
 *                                           and of multiple simultaneous
 *                                           parameter changes.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IParametersEditorListener<K extends IParameterInfo> {

    /**
     * Receive notification of a parameter value change.
     * 
     * @param label
     *            Label identifier of the parameter.
     * @param value
     *            New value of the parameter.
     */
    public void parameterValueChanged(K label, Object value);

    /**
     * Receive notification of multiple simultaneous parameter value changes.
     * 
     * @param valuesForLabels
     *            Map pairing parameter label identifiers with their new values.
     */
    public void parameterValuesChanged(Map<K, Object> valuesForLabels);

    /**
     * Receive notification that a parameters editor has experienced a size
     * change.
     * 
     * @param label
     *            Label identifier of the parameter that precipitated the
     *            change.
     */
    public void sizeChanged(K label);
}
