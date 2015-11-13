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
package com.raytheon.uf.common.hazards.configuration.types;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 
 * Entry in the hazard types table.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 10, 2013 1257       bsteffen    Initial creation
 * Oct 22, 2013 1463       blawrenc    Added fields to support
 *                                     hazard conflict checking.
 * Feb 17, 2014 2161       Chris.Golden Added defaultDuration and
 *                                      allowUntilFurtherNotice
 *                                      fields.
 * Apr 28, 2014 3556       bkowal      Relocate to a common plugin.
 * Jul 03, 2014 3512       Chris.Golden Added durationChoiceList
 *                                      field.
 * Jan 16, 2015 4959       Dan Schaffer Add/Remove UGC capability
 * Jan 21, 2015 3626       Chris.Golden Added hazard-type-first
 *                                      recommender field.
 * Feb 01, 2015 2331       Chris.Golden Added fields for flags
 *                                      indicating the constraints
 *                                      the type puts on start and
 *                                      end time editability.
 * Feb 21, 2015 4959       Dan Schaffer Improvements to add/remove UGCs
 * Mar 06, 2015 3850       Chris.Golden Added replacedBy and
 *                                      requirePointId fields.
 * Mar 26, 2015 7110       hansen       Automatically include all
 *                                      allowedHazards if "includeAll"
 * Nov 10, 2015 12762      Chris.Golden Added modifyRecommenders to
 *                                      allow recommenders to be
 *                                      specified that are to be
 *                                      triggered when an event's
 *                                      time range, status, or geometry
 *                                      changes.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class HazardTypeEntry {

    private String headline;

    private boolean combinableSegments;

    private boolean includeAll;

    private boolean allowAreaChange;

    private boolean allowTimeChange;

    private boolean startTimeIsCurrentTime;

    private boolean allowTimeExpand = true;

    private boolean allowTimeShrink = true;

    private boolean requirePointId;

    private int[] expirationTime;

    private String[] hazardConflictList;

    private boolean warngenHatching;

    private boolean pointBased;

    private String ugcType;

    private String ugcLabel;

    private String hazardClipArea;

    private String hazardTypeFirstRecommender;

    private boolean inclusionFractionTest;

    private double inclusionFraction;

    private double inclusionAreaInSqKm;

    private boolean inclusionAreaTest;

    private int hazardPointLimit;

    private String[] durationChoiceList;

    private String[] replacedBy;

    private long defaultDuration;

    private boolean allowUntilFurtherNotice;

    private Map<String, List<String>> modifyRecommenders;

    public Map<String, List<String>> getModifyRecommenders() {
        return modifyRecommenders;
    }

    public void setModifyRecommenders(
            Map<String, List<String>> modifyRecommenders) {
        this.modifyRecommenders = modifyRecommenders;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public boolean isIncludeAll() {
        return includeAll;
    }

    public void setIncludeAll(boolean includeAll) {
        this.includeAll = includeAll;
    }

    public boolean isCombinableSegments() {
        return combinableSegments;
    }

    public void setCombinableSegments(boolean combinableSegments) {
        this.combinableSegments = combinableSegments;
    }

    public boolean isAllowAreaChange() {
        return allowAreaChange;
    }

    public void setAllowAreaChange(boolean allowAreaChange) {
        this.allowAreaChange = allowAreaChange;
    }

    public boolean isAllowTimeChange() {
        return allowTimeChange;
    }

    public void setAllowTimeChange(boolean allowTimeChange) {
        this.allowTimeChange = allowTimeChange;
    }

    public boolean isStartTimeIsCurrentTime() {
        return startTimeIsCurrentTime;
    }

    public void setStartTimeIsCurrentTime(boolean startTimeIsCurrentTime) {
        this.startTimeIsCurrentTime = startTimeIsCurrentTime;
    }

    public boolean isAllowTimeExpand() {
        return allowTimeExpand;
    }

    public void setAllowTimeExpand(boolean allowTimeExpand) {
        this.allowTimeExpand = allowTimeExpand;
    }

    public boolean isAllowTimeShrink() {
        return allowTimeShrink;
    }

    public void setAllowTimeShrink(boolean allowTimeShrink) {
        this.allowTimeShrink = allowTimeShrink;
    }

    public boolean isRequirePointId() {
        return requirePointId;
    }

    public void setRequirePointId(boolean requirePointId) {
        this.requirePointId = requirePointId;
    }

    public int[] getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(int[] expirationTime) {
        this.expirationTime = expirationTime;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * @return the hazardConflictList
     */
    public List<String> getHazardConflictList() {
        return Arrays.asList(hazardConflictList);
    }

    /**
     * @param hazardConflictList
     *            the hazardConflictList to set
     */
    public void setHazardConflictList(List<String> hazardConflictList) {
        this.hazardConflictList = hazardConflictList.toArray(new String[0]);
    }

    /**
     * @return the ugcType
     */
    public String getUgcType() {
        return ugcType;
    }

    /**
     * @param ugcType
     *            the ugcType to set
     */
    public void setUgcType(String ugcType) {
        this.ugcType = ugcType;
    }

    /**
     * @return the ugcLabel
     */
    public String getUgcLabel() {
        return ugcLabel;
    }

    /**
     * @param ugcLabel
     *            the ugcLabel to set
     */
    public void setUgcLabel(String ugcLabel) {
        this.ugcLabel = ugcLabel;
    }

    /**
     * @return the hazardClipArea
     */
    public String getHazardClipArea() {
        return hazardClipArea;
    }

    /**
     * @param hazardClipArea
     *            the hazardClipArea to set
     */
    public void setHazardClipArea(String hazardClipArea) {
        this.hazardClipArea = hazardClipArea;
    }

    /**
     * @return the hazardTypeFirstRecommender
     */
    public String getHazardTypeFirstRecommender() {
        return hazardTypeFirstRecommender;
    }

    /**
     * @param hazardTypeFirstRecommender
     *            The hazard-type-first recommender.
     */
    public void setHazardTypeFirstRecommender(String hazardTypeFirstRecommender) {
        this.hazardTypeFirstRecommender = hazardTypeFirstRecommender;
    }

    /**
     * @return the hazardPointLimit
     */
    public int getHazardPointLimit() {
        return hazardPointLimit;
    }

    /**
     * @param hazardPointLimit
     *            the hazardPointLimit to set
     */
    public void setHazardPointLimit(int hazardPointLimit) {
        this.hazardPointLimit = hazardPointLimit;
    }

    public List<String> getDurationChoiceList() {
        return (durationChoiceList == null ? Collections.<String> emptyList()
                : Arrays.asList(durationChoiceList));
    }

    public void setDurationChoiceList(List<String> durationChoiceList) {
        this.durationChoiceList = (durationChoiceList == null ? new String[0]
                : durationChoiceList.toArray(new String[durationChoiceList
                        .size()]));
    }

    public List<String> getReplacedBy() {
        return (replacedBy == null ? Collections.<String> emptyList() : Arrays
                .asList(replacedBy));
    }

    public void setReplacedBy(List<String> replacedBy) {
        this.replacedBy = (replacedBy == null ? new String[0] : replacedBy
                .toArray(new String[replacedBy.size()]));
    }

    public long getDefaultDuration() {
        return defaultDuration;
    }

    public void setDefaultDuration(long defaultDuration) {
        this.defaultDuration = defaultDuration;
    }

    public boolean isAllowUntilFurtherNotice() {
        return allowUntilFurtherNotice;
    }

    public void setAllowUntilFurtherNotice(boolean allowUntilFurtherNotice) {
        this.allowUntilFurtherNotice = allowUntilFurtherNotice;
    }

    /**
     * @return the inclusionFractionTest
     */
    public boolean isInclusionFractionTest() {
        return inclusionFractionTest;
    }

    /**
     * @param inclusionFractionTest
     *            the inclusionFractionTest to set
     */
    public void setInclusionFractionTest(boolean inclusionFractionTest) {
        this.inclusionFractionTest = inclusionFractionTest;
    }

    /**
     * @return the inclusionFraction
     */
    public double getInclusionFraction() {
        return inclusionFraction;
    }

    /**
     * @param inclusionFraction
     *            the inclusionFraction to set
     */
    public void setInclusionFraction(double inclusionFraction) {
        this.inclusionFraction = inclusionFraction;
    }

    /**
     * @return the inclusionAreaInSqKm
     */
    public double getInclusionAreaInSqKm() {
        return inclusionAreaInSqKm;
    }

    /**
     * @param inclusionAreaInSqKm
     *            the inclusionAreaInSqKm to set
     */
    public void setInclusionAreaInSqKm(double inclusionAreaInSqKm) {
        this.inclusionAreaInSqKm = inclusionAreaInSqKm;
    }

    /**
     * @return the inclusionAreaTest
     */
    public boolean isInclusionAreaTest() {
        return inclusionAreaTest;
    }

    /**
     * @param inclusionAreaTest
     *            the inclusionAreaTest to set
     */
    public void setInclusionAreaTest(boolean inclusionAreaTest) {
        this.inclusionAreaTest = inclusionAreaTest;
    }

    /**
     * @return the warngenHatching
     */
    public boolean isWarngenHatching() {
        return warngenHatching;
    }

    /**
     * @param warngenHatching
     *            the warngenHatching to set
     */
    public void setWarngenHatching(boolean warngenHatching) {
        this.warngenHatching = warngenHatching;
    }

    /**
     * @return the pointBased
     */
    public boolean isPointBased() {
        return pointBased;
    }

    /**
     * @param pointBased
     *            the pointBased to set
     */
    public void setPointBased(boolean pointBased) {
        this.pointBased = pointBased;
    }

}
