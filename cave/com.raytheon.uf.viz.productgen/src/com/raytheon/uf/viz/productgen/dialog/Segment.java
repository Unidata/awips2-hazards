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

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;

/**
 * Helper class to the ProductGenerationDialog to help separate the data
 * dictionary into segments based on the "segments" key in the python
 * dictionary.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 14, 2014            jsanchez     Initial creation
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class Segment {

    private String segmentID;

    private Map<String, Serializable> data;

    public Segment(Map<String, Serializable> data) {
        this.data = data;
        segmentID = createSegmentID(data);
    }

    public String getSegmentID() {
        return segmentID;
    }

    public List<String> getPath() {
        return Arrays.asList(new String[] { HazardConstants.SEGMENTS,
                HazardConstants.SEGMENT });
    }

    public Map<String, Serializable> getData() {
        return data;
    }

    private String createSegmentID(Map<String, Serializable> data) {
        String segmentID = null;

        List<Serializable> vtecRecords = (List<Serializable>) data
                .get(HazardConstants.VTEC_RECORDS);
        for (Serializable vtecRecord : vtecRecords) {
            Map<String, Serializable> map = (Map<String, Serializable>) vtecRecord;
            if (map.get(HazardConstants.VTEC_RECORD_TYPE).equals(
                    HazardConstants.PVTEC_RECORD)) {
                StringBuilder sb = new StringBuilder();
                sb.append(map.get(HazardConstants.ACTION));
                sb.append(".");
                sb.append(map.get(HazardConstants.SITE));
                sb.append(".");
                sb.append(map.get(HazardConstants.PHENOMENON));
                sb.append(".");
                sb.append(map.get(HazardConstants.SIGNIFICANCE));
                sb.append(".");
                sb.append(map.get(HazardConstants.ETN));
                segmentID = sb.toString();
            }
        }

        return segmentID;
    }

}
