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
package com.raytheon.uf.common.hazards.hydro;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that holds information about the river and the gages on the river.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 7, 2017   28946     mpduff      Initial creation
 *
 * </pre>
 *
 * @author mpduff
 */

public class RiverMetadata {

    private List<RiverGageMetadata> riverGages = new ArrayList<>();

    private String group;

    private String groupId;

    private String hsa;

    public List<RiverGageMetadata> getRiverGages() {
        return riverGages;
    }

    public void setRiverGages(List<RiverGageMetadata> riverGages) {
        this.riverGages = riverGages;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void add(RiverGageMetadata riverGageMetadata) {
        this.riverGages.add(riverGageMetadata);

    }

    public String getHsa() {
        return hsa;
    }

    public void setHsa(String hsa) {
        this.hsa = hsa;
    }
}
