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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.custom.StyledText;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.hazards.productgen.KeyInfo;

/**
 * The segment data of the product generator data.
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

public class SegmentData extends AbstractProductGeneratorData {

    public static final List<KeyInfo> PARENT_PATH = Arrays
            .asList(new KeyInfo[] {
                    KeyInfo.createBasicKeyInfo(HazardConstants.SEGMENTS),
                    KeyInfo.createBasicKeyInfo(HazardConstants.SEGMENT) });

    public static final String SEGMENT_END = "$$";

    public SegmentData(Map<KeyInfo, Serializable> data, String segmentID) {
        super(data, segmentID);
    }

    @Override
    public List<KeyInfo> getPath(KeyInfo editableKey) {
        List<KeyInfo> path = new ArrayList<KeyInfo>();
        path.addAll(PARENT_PATH);
        path.addAll(super.getPath(editableKey));

        return path;
    }

    @Override
    public void highlight(StyledText styledText) {
        // scrolls to the line
        for (int lineIndex = 0; lineIndex < styledText.getLineCount(); lineIndex++) {
            String line = styledText.getLine(lineIndex);
            if (line.contains(getSegmentID())) {
                styledText.setTopIndex(lineIndex);

                // performs highlighting
                // Only applicable for Legacy text
                int startIndex = styledText.getText().indexOf(line);
                if (startIndex != -1) {
                    int endOfSegmentIndex = styledText.getText().indexOf(
                            SEGMENT_END, startIndex) + 2;
                    if (endOfSegmentIndex != -1) {
                        styledText.setSelectionRange(startIndex,
                                endOfSegmentIndex - startIndex);
                    }
                }

                break;
            }
        }

    }

    @Override
    public String getDescriptionName() {
        return segmentID;
    }

    @Override
    public String getSegmentID() {
        return segmentID;
    }

}
