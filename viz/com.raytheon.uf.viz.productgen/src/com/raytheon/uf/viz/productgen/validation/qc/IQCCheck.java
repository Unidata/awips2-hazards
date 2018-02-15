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
package com.raytheon.uf.viz.productgen.validation.qc;

import java.util.regex.Pattern;

/**
 * Imported and integrated from Warn Gen: com.raytheon.viz.texteditor
 * 
 * AWIPS2_baseline/cave/com.raytheon.viz.texteditor/src/com/raytheon
 * /viz/texteditor/qc
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 29, 2015 6617       Chris.Cody  Initial Import. Integrate WarnGen Product Validation.
 * Oct 27, 2015 6617       Robert.Blum Fixed patterns to check for mixed case.
 * Jun 20, 2016 19135      Robert.Blum Fixed test message pattern.
 * Nov 01, 2016 14665      Roger.Ferrel Fixed firstBulletPtrn pattern in order to detect
 *                                      a multiline bullet.
 * </pre>
 * 
 * @version 1.0
 */
public interface IQCCheck {
    /** Match for "TTAAii CCCC DDHHMM BBB" line */
    public static Pattern wmoHeaderPtrn = Pattern.compile(
            "(\\w{2})\\w{2}\\d{2}\\s\\w{4}\\s\\S{6}((\\s(\\w{2})(\\w{1}))|)");

    /** AWIPS ID pattern (ex. SMWKEY) */
    public static Pattern awipsIDPtrn = Pattern.compile("(\\w{3})(\\w{3})");

    public static final Pattern datePtrn = Pattern.compile(
            "(\\d{1,2})(\\d{2})\\s(AM|PM)\\s(\\w{3,4})\\s\\w{3}\\s(\\w{3})\\s{1,}(\\d{1,2})\\s(\\d{4})");

    public static final Pattern vtecPtrn = Pattern.compile(
            "/[OTEX]\\.([A-Z]{3})\\.[A-Za-z0-9]{4}\\.[A-Z]{2}\\.[WAYSFON]\\.\\d{4}\\.\\d{6}T\\d{4}Z-\\d{6}T\\d{4}Z/");

    public static final Pattern htecPtrn = Pattern.compile(
            "/[A-Za-z0-9]{5}.[0-3NU].(\\w{2}).\\d{6}T\\d{4}Z.\\d{6}T\\d{4}Z.\\d{6}T\\d{4}Z.\\w{2}/");

    public static final Pattern listOfAreaNamePtrn = Pattern
            .compile("^([\\w\\s\\.'/]*-)");

    public static final Pattern firstBulletPtrn = Pattern.compile(
            "^\\*\\s(.*)\\s(Warning|Advisory)\\sfor(.*)\\.{3}$",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    public static final Pattern secondBulletPtrn = Pattern.compile(
            "^\\*\\sUntil\\s(\\d{1,2})(\\d{2})\\s(AM|PM)\\s(\\w{3,4})",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    public static final Pattern thirdBulletPtrn = Pattern.compile(
            "^\\*\\sAt\\s(\\d{1,2})(\\d{2})\\s(AM|PM)\\s(\\w{3,4})(.*)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    public static final Pattern latLonPtrn = Pattern
            .compile("LAT...LON+(\\s\\d{3,4}\\s\\d{3,5}){1,}");

    public static final Pattern subLatLonPtrn = Pattern.compile(
            "\\s{1,}\\d{3,4}\\s\\d{3,5}(|(\\s\\d{3,4}\\s\\d{3,5}){1,})");

    public static final Pattern tmlPtrn = Pattern.compile(
            "TIME...MOT...LOC \\d{3,4}Z\\s\\d{3}DEG\\s\\d{1,3}KT((\\s\\d{3,4}\\s\\d{3,5}){1,})");

    public static final Pattern subTMLPtrn = Pattern
            .compile("(\\d{3,5}\\s){1,}");

    public static final String TEST_MESSAGE_LABEL = "THIS IS A TEST MESSAGE. ";

    public String runQC(String header, String body, String nnn);
}
