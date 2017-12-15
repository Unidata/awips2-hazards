/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.productgen.dialog;

import java.util.Map;

import com.raytheon.uf.common.hazards.productgen.KeyInfo;

/**
 * Description: Listener for parameters editor events.
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
 * Aug 31, 2015    9617    Chris.Golden      The ParametersEditorFactory library class has
 *                                           been littered with product-editor-specific
 *                                           code, so it has now been copied to the product
 *                                           editor package, along with this interface, and
 *                                           the original parameters editor factory has
 *                                           reverted to be more generic. This is not a
 *                                           permanent solution to the problem, but it
 *                                           allows the megawidget framework to remain more 
 *                                           generic than it was becoming.
 * Dec 17, 2017   20739    Chris.Golden      Refactored away access to directly mutable
 *                                           session events.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IProductParametersEditorListener {

    /**
     * Receive notification of a parameter value change.
     * 
     * @param label
     *            Label identifier of the parameter.
     * @param value
     *            New value of the parameter.
     */
    public void parameterValueChanged(KeyInfo label, Object value);

    /**
     * Receive notification of multiple simultaneous parameter value changes.
     * 
     * @param valuesForLabels
     *            Map pairing parameter label identifiers with their new values.
     */
    public void parameterValuesChanged(Map<KeyInfo, Object> valuesForLabels);

    /**
     * Receive notification that a parameters editor has experienced a size
     * change.
     * 
     * @param label
     *            Label identifier of the parameter that precipitated the
     *            change.
     */
    public void sizeChanged(KeyInfo label);
}
