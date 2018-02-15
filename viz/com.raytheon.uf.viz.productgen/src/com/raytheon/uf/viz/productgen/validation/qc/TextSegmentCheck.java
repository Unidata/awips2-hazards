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

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXB;

import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.productgen.validation.util.VtecObject;
import com.raytheon.uf.viz.productgen.validation.util.VtecUtil;
import com.raytheon.viz.core.mode.CAVEMode;

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
 * Oct 27, 2015 6617       Robert.Blum Fixes for mixed case validation.
 * Nov 17, 2015 3473      Robert.Blum  Moved all python files under HazardServices localization dir.
 * Nov 01, 2016 14665     Roger.Ferrel Fixes to detect multiline first bullet and code cleanup.
 * Feb 01, 2017 28678     Robert.Blum  Minor punctuation fix.
 * </pre>
 * 
 * @version 1.0
 */
public class TextSegmentCheck implements IQCCheck {

    private static final Pattern ugcPtrn = Pattern.compile(
            "(((\\w{2}[CZ](\\d{3}-){1,}){1,})|(\\d{3}-){1,})(((\\d{2})(\\d{2})(\\d{2})-){0,1})");

    private static Map<String, List<String>> bulletTypeMaps;

    static {
        IPathManager pm = PathManagerFactory.getPathManager();
        File path = pm.getStaticFile(
                "HazardServices/validation/QualityControlCfg.xml");
        QualityControlCfg qcCfg = null;
        try {
            qcCfg = JAXB.unmarshal(path, QualityControlCfg.class);
            bulletTypeMaps = qcCfg.getBulletTypeMap();
        } catch (RuntimeException e) {
            IUFStatusHandler statusHandler = UFStatus
                    .getHandler(QualityControl.class);
            statusHandler.handle(Priority.PROBLEM,
                    "Unable to read/load QualityControlCfg.xml for QC check\n",
                    e);
        }
    }

    // List of immediate causes to be excluded from quality control check in the
    // first bullet
    private static List<String> firstBulletImmediateCauseQCExclusions = Arrays
            .asList("ER", "MC", "UU", "IC");

    @Override
    public String runQC(String header, String body, String nnn) {
        int countyOrZoneCounter = 0;
        int ugcLength = 0;
        int czmType = 1;
        int nb = 0;
        int segmentCount = 0;
        boolean expecHTEC = false;
        boolean expectNamesList = false;
        boolean insideFirstBullet = false;
        boolean secondBulletFound = false;
        boolean checkIC = false;
        boolean countUGC = false;
        boolean headlineFound = false;
        boolean insideLatLon = false;
        boolean countyBased = false;
        boolean validFirstBullet = true;
        VtecObject vtec = null;
        String ic = null;
        String ugc = "";
        String latLon = "";
        String tml = "";
        String headline = "";
        StringBuilder errorMsg = new StringBuilder();
        String segment = "Primary";
        Matcher m = htecPtrn.matcher(body);
        if (m.find()) {
            ic = m.group(1);
        }

        if (nnn.equalsIgnoreCase("FFW") || nnn.equalsIgnoreCase("FFS")
                || nnn.equalsIgnoreCase("FLW") || nnn.equalsIgnoreCase("FLS")) {
            /*
             * DR 11060 Need to check if the product is a hydro product hydro
             * products that are zone based do not check the first bullet area
             * count against the UGC count
             */
            czmType = 2;
        } else if (nnn.equalsIgnoreCase("SMW") || nnn.equalsIgnoreCase("MWS")) {
            czmType = 3;
        }

        Set<String> countyParishMunicipality = new HashSet<>();
        for (Entry<String, String> entry : QualityControl.getCountyTypeMap()
                .entrySet()) {
            String key = entry.getKey();
            String countyType = entry.getValue();
            if (countyType.length() > 1) {
                countyParishMunicipality.add(countyType.trim());
            } else if (key.length() > 1) {
                countyParishMunicipality.add(key);
            }
        }

        countyParishMunicipality.remove("AK");
        countyParishMunicipality.remove("DC");
        countyParishMunicipality.add("City");

        String[] separatedLines = body.split("\n");

        for (int lineIndex = 0; lineIndex < separatedLines.length; ++lineIndex) {
            String line = separatedLines[lineIndex];
            if (!CAVEMode.OPERATIONAL.equals(CAVEMode.getMode())) {
                if (line.contains(TEST_MESSAGE_LABEL)) {
                    line = line.replaceFirst(TEST_MESSAGE_LABEL, "");
                }
            }

            if (line.equals("$$")) {

                if ((ugc.length() == 0) && (vtec == null)) {
                    errorMsg.append("Badly placed segment end.\n");
                    return errorMsg.toString();
                }

                segmentCount++;
                segment = "Secondary";
                ugc = "";

                /*
                 * DR15003 - Add vtec to signature ias n order to distinguish
                 * between standalone and followup MWS a check of VTEC is
                 * needed.
                 */
                errorMsg.append(checkHeadline(headline, nnn, vtec));
                headline = "";

                if ((segmentCount > 1)
                        && !QualityControl.segmentedNNN.contains(nnn)) {
                    errorMsg.append("Segments exist in unsegmented product.\n");
                }
                continue;
            }

            m = ugcPtrn.matcher(line);
            if (m.find()) {
                ugc += line;
                countUGC = true;
                continue;
            }

            // Verify UGC line(s) syntax and get count of zones or counties.
            if (countUGC) {
                int countyZoneCnt = 0;
                if (ugc.matches("\\w{2}[CZ]\\d{3}[->].*") == false) {
                    errorMsg.append(
                            "First UGC does not specify a zone or county.\n");
                }
                if ((ugc.length() > 2) && (ugc.charAt(2) == 'C')) {
                    ++countyZoneCnt;
                    countyBased = true;
                }
                if ((ugc.length() > 2) && (ugc.charAt(2) == 'Z')) {
                    ++countyZoneCnt;
                }

                if (countyZoneCnt == 0) {
                    errorMsg.append("No zone or county specified in UGCs.\n");
                } else if (countyZoneCnt == 2) {
                    errorMsg.append("Illegal mixture of zone/county UGCs.\n");
                }

                String[] ranges = ugc.replaceFirst("\\d{6}-", "").split("-");
                for (String range : ranges) {
                    if (range.contains(">")) {
                        int index = range.indexOf(">");
                        String left = range.substring(index - 3, index);
                        String right = range.substring(index + 1);
                        int start = Integer.valueOf(left);
                        int end = Integer.valueOf(right);
                        for (int val = start; val <= end; ++val) {
                            ugcLength++;
                        }
                    } else {
                        ugcLength++;
                    }
                }
            }

            m = vtecPtrn.matcher(line);
            if (m.find()) {
                vtec = VtecUtil.parseMessage(line);
                if (vtec.getPhenomena().equals("FF")
                        || vtec.getPhenomena().equals("FL")
                        || vtec.getPhenomena().equals("FA")) {
                    expecHTEC = true;
                }
                if (QualityControl.segmentedNNN.contains(nnn)) {
                    expectNamesList = true;
                }
                countUGC = false;
                continue;
            } else if (countUGC) {
                if (VtecUtil.parseMessage(body) != null) {
                    errorMsg.append(segment)
                            .append(" VTEC not right after UGC\n");
                }
                countUGC = false;
            }

            if (expecHTEC) {
                m = htecPtrn.matcher(line);
                if (!m.find()) {
                    errorMsg.append("Hydro VTEC line must follow FF or FL.\n");
                }
                expecHTEC = false;
                continue;
            }

            if (expectNamesList) {
                m = listOfAreaNamePtrn.matcher(line);
                /*
                 * DR15006 - MWS does not have the list of marine zones names in
                 * the segment heading, so skip the check for MWS
                 */
                if (!nnn.equalsIgnoreCase("MWS")) {
                    if (!m.find()) {
                        errorMsg.append("List of county/zone names missing.\n");
                    }
                }
                expectNamesList = false;
                continue;
            }

            if (line.startsWith("...")) {
                // followup headline found
                headline = line;
                headlineFound = true;
                continue;
            }
            if (line.trim().isEmpty()) {
                // blank line?
                headlineFound = false;
                continue;
            } else if (headlineFound) {
                headline += line;
                continue;
            }

            if (line.startsWith("* ")) {
                nb++;
            }

            // third bullet
            if (line.startsWith("* ") && (nb == 3)) {
                m = thirdBulletPtrn.matcher(line);
                if (!line.substring(0, 5).equals("* At ")) {
                    errorMsg.append(
                            "Event bullet does not start with '* At '.\n");
                } else if (!m.find()) {
                    errorMsg.append(
                            "Event bullet starts with badly formatted time\n")
                            .append(" or event bullet does not start with a time.\n");
                }
            }

            // second bullet
            if (line.startsWith("* ") && (nb == 2)) {
                m = secondBulletPtrn.matcher(line);
                if (m.find() || line.contains("* Until noon")
                        || line.contains("* Until midnight")) {
                    secondBulletFound = true;
                    insideFirstBullet = false;
                    continue;
                }
            }

            // first bullet
            if (nb == 1) {
                /*
                 * Assume when the start of first bullet's does not match
                 * firstBulletPtrn it is continued on the next line. This joins
                 * the lines with single space in order for firstBulletPtrn to
                 * find a match no matter where the line break occurs.
                 */
                int appendLineIndex = lineIndex;
                if (line.startsWith("*")
                        && !firstBulletPtrn.matcher(line).matches()) {
                    StringBuilder tmpLine = new StringBuilder(line.trim());
                    while (!firstBulletPtrn.matcher(tmpLine).matches()
                            && (appendLineIndex < (separatedLines.length
                                    - 1))) {
                        tmpLine.append(" ").append(
                                separatedLines[++appendLineIndex].trim());
                    }
                    if (appendLineIndex < separatedLines.length) {
                        line = tmpLine.toString();
                    } else {
                        errorMsg.append("first bullet not valid for ")
                                .append(nnn).append("\n");
                        validFirstBullet = false;
                        continue;
                    }
                }
                m = firstBulletPtrn.matcher(line);
                if (m.find()) {
                    /*
                     * Adjust the for loop invariant to skip over any appended
                     * lines that are part of the first bullet.
                     */
                    lineIndex = appendLineIndex;
                    List<String> bulletTypesList = null;

                    if (vtec != null) {
                        String key = vtec.getPhenomena() + "."
                                + vtec.getSignificance();
                        bulletTypesList = bulletTypeMaps.get(key);
                    }

                    if (bulletTypesList != null) {
                        /*
                         * Vtec may have Bullet types that do not match the
                         * warning type.
                         */
                        boolean badBullet = true;
                        for (String bulletType : bulletTypesList) {
                            if (line.contains(bulletType)) {
                                badBullet = false;
                                break;
                            }
                        }
                        if (badBullet && validFirstBullet) {
                            errorMsg.append("first bullet not valid for ")
                                    .append(nnn).append("\n");
                        }
                    } else {
                        // bullet type and warning type should match.
                        String warningType = QualityControl
                                .getProductWarningType(nnn);
                        if (validFirstBullet && !line.contains(warningType)) {
                            errorMsg.append(nnn)
                                    .append(" does not match first bullet.\n");
                        }
                    }
                    insideFirstBullet = true;
                    checkIC = true;
                    continue;
                } else if (!insideFirstBullet && !secondBulletFound
                        && (line.contains("area...")
                                || line.contains("areas...")
                                || line.contains("area was..."))) {
                    insideFirstBullet = true;
                    continue;
                }
            }

            if (insideFirstBullet) {
                if ((ic != null)
                        && !firstBulletImmediateCauseQCExclusions.contains(ic)
                        && checkIC) {
                    boolean validIC = false;
                    for (String causes : QualityControl.getImmediateCauses()) {
                        if (causes.startsWith(ic)
                                && line.contains(causes.split("\\\\")[1])) {
                            validIC = true;
                            break;
                        }
                    }

                    if (!validIC) {
                        errorMsg.append(
                                "Immediate cause missing in first bullet\n or is inconsistent with VTEC.\n");
                        /*
                         * need to return or else it will incorrectly count
                         * counties
                         */
                        return errorMsg.toString();
                    }
                    // Immediate cause should be the line after the first bullet
                    checkIC = false;
                    continue;
                }

                if (czmType == 3) {
                    if ((line != null)
                            && line.trim().startsWith("Including ")) {
                        insideFirstBullet = false; // stop adding counties/zones
                        continue;
                    }
                } else {
                    if (line.contains("This includes")) {
                        // line indicates cities not counties/zones
                        continue;
                    }

                    boolean invalidCountyOrZone = true;
                    if ((ugc.length() > 2) && (ugc.charAt(2) == 'Z')) {
                        // zones do not use countyTypes
                        if (line.contains(" in ")) {
                            invalidCountyOrZone = false;
                        }
                    } else {
                        for (String state : QualityControl.getCountyTypeMap()
                                .keySet()) {
                            if (line.contains(state)
                                    && line.contains(QualityControl
                                            .getCountyTypeMap().get(state))) {
                                invalidCountyOrZone = false;
                                break;
                            }
                        }
                        if (invalidCountyOrZone) {
                            for (String countyType : QualityControl
                                    .getCountyTypeMap().values()) {
                                if ((!countyType.trim().isEmpty()) && line
                                        .contains(" " + countyType.trim())) {
                                    invalidCountyOrZone = false;
                                    break;
                                }
                            }
                        }
                    }
                    if (invalidCountyOrZone && !line.contains(" of ")) {
                        continue;
                    }

                }

                int cpmCounter = 0;
                for (String cpm : countyParishMunicipality) {
                    if (line.contains(cpm)) {
                        break;
                    } else {
                        cpmCounter += 1;
                        continue;
                    }
                }
                if (cpmCounter != countyParishMunicipality.size()) {
                    if (!line.trim().isEmpty()) {
                        countyOrZoneCounter++;
                        continue;
                    } else {
                        // ran into a blank line, done
                        insideFirstBullet = false;
                    }
                }
            }

            m = latLonPtrn.matcher(line);
            if (m.find()) {
                latLon = line;
                insideLatLon = true;
                continue;
            }

            if (insideLatLon) {
                m = subLatLonPtrn.matcher(line);

                if (!line.startsWith("TIME...") && m.find()) {
                    latLon += " " + line.trim();
                    continue;
                } else {
                    insideLatLon = false;
                }
            }

            m = tmlPtrn.matcher(line);
            if (m.find()) {
                tml = line;
            }

        }

        if (validFirstBullet) {
            if (ugcLength == 0) {
                errorMsg.append("No UGC text was found\n");
            } else if ((nb > 0)
                    && ((czmType == 1) || ((czmType == 2) && countyBased))
                    && (ugcLength != countyOrZoneCounter)) {
                // DR 11060 - Don't count zone areas for hydro products
                errorMsg.append(ugcLength).append(" UGCs while ")
                        .append(countyOrZoneCounter)
                        .append(" counties/zones listed.\n");
                errorMsg.append(
                        "Area descriptions count does not\n match UGC count.\n");
            }
        }

        if (body.contains("LAT...LON")) {
            errorMsg.append(checkLatLon(latLon));
        }
        if (body.contains("TIME...MOT...LOC")) {
            errorMsg.append(checkTML(tml));
        }

        return errorMsg.toString();
    }

    private String checkLatLon(String latLon) {
        String errorMsg = "";
        int pairs = 0;
        Pattern p = Pattern.compile("(\\d{3,4})\\s(\\d{3,5})");

        if (latLon.length() == 0) {
            errorMsg += "LAT...LON line is malformed.\n";
            return errorMsg;
        }

        Matcher m = p.matcher(latLon.substring(9));
        while (m.find()) {
            pairs++;
            double lat = Double.parseDouble(m.group(1));
            double lon = Double.parseDouble(m.group(2));
            if ((lat > 9000) || (lon > 18000)) {
                errorMsg += "Data error in the LAT...LON line.\n";
                return errorMsg;
            }
        }

        if ((pairs <= 2) || (pairs > 20)) {
            errorMsg += "LAT...LON line missing or malformed.\n";
        }

        return errorMsg;
    }

    private String checkTML(String tml) {
        String errorMsg = "";
        if (tml.length() == 0) {
            errorMsg += "TIME...MOT...LOC line is malformed.\n";
            return errorMsg;
        }

        return errorMsg;
    }

    private String checkHeadline(String headline, String nnn, VtecObject vtec) {
        String errorMsg = "";
        if (!QualityControl.segmentedNNN.contains(nnn) || nnn.equals("FLS")) {
            // non-follow ups do not have a head line
            return errorMsg;
        }
        /*
         * DR15003: no headline QC on standalone MWS. To distinguish between
         * standalone and follow up MWS the VTEC check is performed as
         * standalone MWS do not contain VTEC
         */
        if (nnn.equals("MWS") && (vtec == null)) {
            return "";
        }

        if (headline.length() == 0) {
            errorMsg += "Headline is missing or malformed.\n";
        } else if (!headline.endsWith("...")) {
            errorMsg += "Headline should end with '...'.\n";
        }
        return errorMsg;
    }

}
