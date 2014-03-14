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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.custom.StyledText;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;

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

    private List<String> PARENT_PATH = Arrays.asList(new String[] {
            HazardConstants.SEGMENTS, HazardConstants.SEGMENT });

    public static final String SEGMENT_END = "$$";

    private String segmentID;

    public SegmentData(Map<String, Serializable> data) {
        super(data);
        segmentID = createSegmentID(data);
    }

    @Override
    public String getDescriptionName() {
        return segmentID;
    }

    /**
     * Traverses the data dictionary and creates a concatenated string of the
     * UGCs separated by commas. The UGCs will be ordered alphabetically.
     * 
     * @param data
     * @return
     */
    private String createSegmentID(Map<String, Serializable> data) {
        List<Serializable> ugcCodes = (List<Serializable>) ((Map<String, Serializable>) data
                .get(HazardConstants.UGC_CODES)).get(HazardConstants.UGC_CODE);
        ArrayList<String> ugcTextList = new ArrayList<String>(ugcCodes.size());
        for (Serializable ugcCode : ugcCodes) {
            Map<String, Serializable> ugc = (Map<String, Serializable>) ugcCode;
            ugcTextList.add(String.valueOf(ugc.get(HazardConstants.TEXT)));
        }

        Collections.sort(ugcTextList);

        return simplifyHeader(getUgcLine(ugcTextList));
    }

    /*
     * From FipsUtil.java
     */
    private static String getUgcLine(ArrayList<String> ugcs) {
        ArrayList<String> states = new ArrayList<String>();
        StringBuffer rval = new StringBuffer();

        int nlCounter = 0;
        for (String ugc : ugcs) {
            if (!states.contains(ugc.substring(0, 3))) {
                states.add(ugc.substring(0, 3));
            }
        }

        for (String state : states) {
            rval.append(state);
            nlCounter += state.length();
            for (String ugc : ugcs) {
                if (ugc.substring(0, 3).equals(state)) {
                    rval.append(ugc.substring(3) + "-");
                    nlCounter += 4;
                    if (nlCounter >= 60) {
                        nlCounter = 0;
                        rval.append("\n");
                    }
                }
            }
        }

        return rval.toString();
    }

    /*
     * From FipsUtil.java
     * 
     * TODO: This needs to be refactored in the AWIPS2 baseline into the common
     * warnings plugin
     */
    private static String simplifyHeader(String countyHeader) {
        String simplifiedCountyHeader = "";
        String[] lines = countyHeader.split("[\n]");
        countyHeader = "";
        for (String line : lines) {
            countyHeader += line;
        }
        String[] ugcList = countyHeader.split("[-]");
        int reference = -1;
        ArrayList<String> temp = new ArrayList<String>();
        for (String ugc : ugcList) {
            int fips = Integer.parseInt(ugc.substring(ugc.length() - 3));
            if (Character.isLetter(ugc.charAt(0))) {
                simplifiedCountyHeader = appendUgc(simplifiedCountyHeader, temp);
                temp.clear();
                temp.add(ugc);
            } else if (reference + 1 == fips) {
                temp.add(ugc);
            } else {
                simplifiedCountyHeader = appendUgc(simplifiedCountyHeader, temp);
                temp.clear();
                temp.add(ugc);
            }
            reference = fips;
        }

        return appendUgc(simplifiedCountyHeader, temp) + "-";
    }

    /*
     * From FipsUtil.java
     */
    private static String appendUgc(String countyHeader,
            ArrayList<String> ugcList) {
        if (ugcList.isEmpty() == false) {
            if (ugcList.size() < 3) {

                for (String t : ugcList) {
                    countyHeader += (countyHeader.length() > 0 ? "-" : "") + t;
                }
            } else {
                countyHeader += (countyHeader.length() > 0 ? "-" : "")
                        + ugcList.get(0) + ">"
                        + ugcList.get(ugcList.size() - 1);
            }
        }
        return countyHeader;
    }

    @Override
    public List<String> getPath(String editableKey) {
        List<String> path = new ArrayList<String>();
        path.addAll(PARENT_PATH);
        path.addAll(super.getPath(editableKey));

        return path;
    }

    @Override
    public String getSegmentID() {
        return segmentID;
    }

    @Override
    public void highlight(StyledText styledText) {
        // scrolls to the line
        for (int lineIndex = 0; lineIndex < styledText.getLineCount(); lineIndex++) {
            String line = styledText.getLine(lineIndex);
            if (line.contains(segmentID)) {
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

}
