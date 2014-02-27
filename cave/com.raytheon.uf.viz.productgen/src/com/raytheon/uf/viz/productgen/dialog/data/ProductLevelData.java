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
package com.raytheon.uf.viz.productgen.dialog.data;

import java.io.Serializable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.custom.StyledText;

/**
 * Non segmented data of the product generator data.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 21, 2014            jsanchez     Initial creation
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class ProductLevelData extends AbstractProductGeneratorData {

    private static final Pattern ugcPtrn = Pattern
            .compile(
                    "(^(\\w{2}[CZ]\\d{3}\\S*-\\d{6}-)$|((\\d{3}-)*\\d{6}-)$|((\\d{3}-)+))\\n",
                    Pattern.MULTILINE);

    public ProductLevelData(Map<String, Serializable> data) {
        super(data);
    }

    @Override
    public String getDescriptionName() {
        return "Product Level";
    }

    @Override
    public String getSegmentID() {
        // Since product level data is outside of the segments,
        // null will be returned
        return null;
    }

    @Override
    public void highlight(StyledText styledText) {
        Matcher m = ugcPtrn.matcher(styledText.getText());
        styledText.setTopIndex(0);
        if (m.find()) {
            String group = m.group(0);
            int index = styledText.getText().indexOf(group);

            styledText.setSelectionRange(0, index);
        }

    }

}
