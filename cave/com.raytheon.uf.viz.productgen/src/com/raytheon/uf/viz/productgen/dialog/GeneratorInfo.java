/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.viz.productgen.dialog;

/**
 * Helper class to ProductGenerationDialog. Helps identify which products to
 * group together in the dialog since there can be multiple products and
 * multiple generators that could be ran at one time.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 29, 2014            jsanchez     Initial creation
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class GeneratorInfo {

    private String productGeneratorName;

    private int start;

    private int size;

    public GeneratorInfo(String productGeneratorName, int start) {
        this.productGeneratorName = productGeneratorName;
        this.start = start;
        this.size = 1;
    }

    public String getProductGeneratorName() {
        return productGeneratorName;
    }

    public int getStart() {
        return start;
    }

    public int getSize() {
        return size;
    }

    public void increment() {
        size++;
    }

}
