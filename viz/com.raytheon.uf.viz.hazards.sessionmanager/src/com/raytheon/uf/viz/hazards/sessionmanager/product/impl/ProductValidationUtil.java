/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.product.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description: Utility methods used to validate a product.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 06, 2015 7747       Robert.Blum Initial creation
 * 
 * </pre>
 * 
 * @author Robert.Blum
 * @version 1.0
 */

public class ProductValidationUtil {

    private final static String FRAMED_TEXT_REGEX = "\\|\\*.*?\\*\\|";

    private final static Pattern pat = Pattern.compile(FRAMED_TEXT_REGEX,
            Pattern.DOTALL);

    private ProductValidationUtil() {
        // don't allow this class to be directly instantiated, only provides
        // static utility methods.
        throw new AssertionError();
    }

    /**
     * Checks a text product for framed text.
     * 
     * @param product
     *            The product to be checked.
     * @return Returns a List of Strings containing the framed text found or a
     *         empty List of none was found.
     */
    public static List<String> checkForFramedText(String product) {
        if (product == null) {
            product = "";
        }
        Matcher matcher = pat.matcher(product);

        List<String> framedValues = new ArrayList<String>();
        while (matcher.find()) {
            for (int i = 0; i <= matcher.groupCount(); i++) {
                String groupStr = matcher.group(i);
                framedValues.add(groupStr);
            }
        }
        return framedValues;
    }
}