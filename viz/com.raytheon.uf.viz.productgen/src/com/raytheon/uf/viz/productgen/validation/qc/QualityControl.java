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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXB;

import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.localization.exception.LocalizationException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

/**
 * Imported and integrated from Warn Gen: com.raytheon.viz.texteditor
 * 
 * AWIPS2_baseline/cave/com.raytheon.viz.texteditor/src/com/raytheon
 * /viz/texteditor/qc
 * <p>
 * 
 * TODO #6617 The QualityControl code has been brought over (mostly intact) from
 * Warn Gen. There are several Hazard Types (FAA, etc.) that are not part of
 * Warn Gen, but are part of Hazard Services. Validation for these Hazard Types
 * will need to be added.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 29, 2015 6617       Chris.Cody  Initial Import. Integrate WarnGen Product Validation.
 * Oct 27, 2015 6617       Robert.Blum Fixed validation to only validate WarnGen hazards at this time.
 * Nov 17, 2015 3473       Robert.Blum Moved all python files under HazardServices localization dir.
 * Nov 14, 2016 25641      Robert.Blum Load countyTypes.txt from Hazard Services not WarnGen.
 * </pre>
 * 
 * @author rferrel
 * @version 1.0
 */
public class QualityControl {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(QualityControl.class);

    // TODO #6617 These are only WarnGen Product Types.
    private static final List<String> productTypesToValidate = Arrays
            .asList(new String[] { "TOR", "SVR", "SMW", "FFW", "EWW", "SVS",
                    "FFS", "FLS", "FLW", "MWS" });

    /** Maps warning PILs to appropriate warning text */
    private static Map<String, String> productTypeMap;

    private static Map<String, String> followupNNN;

    private static Map<String, String> nnnOfIdent;

    private static Map<String, String> countyTypes;

    public static ArrayList<String> segmentedNNN;

    private static String[] immediateCause;

    private String errorMsg;

    static {
        loadQualityControlCfg();
        try {
            loadCountyType();
        } catch (Exception ex) {
            statusHandler.handle(Priority.PROBLEM,
                    "Unable to read/load countyTypes.txt for QC check\n", ex);
        }
    }

    private static void loadQualityControlCfg() {
        IPathManager pm = PathManagerFactory.getPathManager();
        File path = pm.getStaticFile(
                "HazardServices/validation/QualityControlCfg.xml");
        QualityControlCfg qcCfg = null;
        qcCfg = JAXB.unmarshal(path, QualityControlCfg.class);
        immediateCause = qcCfg.getImmediateCause();
        followupNNN = qcCfg.getFollowupNNN();
        nnnOfIdent = qcCfg.getNnnOfIdent();
        productTypeMap = qcCfg.getProductTypeMap();
        segmentedNNN = qcCfg.getSegmentedNNN();
    }

    private static void loadCountyType()
            throws LocalizationException, IOException {
        IPathManager pm = PathManagerFactory.getPathManager();
        LocalizationFile file = pm.getStaticLocalizationFile(
                "HazardServices/validation/countyTypes.txt");
        List<String> lines = Files.readAllLines(file.getFile().toPath(),
                StandardCharsets.US_ASCII);

        countyTypes = new HashMap<String, String>();
        for (String line : lines) {
            String[] parts = line.split("\\\\");
            String key = parts[0].trim();
            String value = parts.length < 2 || parts[1] == null ? "" : parts[1];
            countyTypes.put(key, value);
        }
    }

    /**
     * 
     * @param header
     * @param body
     * @param pil
     * @param phensig
     * @return true, if it passes the QC check
     */
    public boolean checkWarningInfo(String header, String body, String pil,
            String phensig) {
        if (!productTypesToValidate.contains(pil)) {
            return true;
        } else if (pil.equals("FLW") || pil.equals("FLS")) {
            // Need to check phensigs because these pils
            // have both RiverPro and WarnGen hazards.
            if (phensig.equals("FL.W") || phensig.equals("FL.Y")
                    || phensig.equals("HY.S")) {
                // RiverPro hazards do not validate at this time
                return true;
            }
        }

        IQCCheck[] checks = new IQCCheck[] { new WmoHeaderCheck(),
                new MndHeaderCheck(), new TextSegmentCheck(),
                new TimeConsistentCheck(), new CtaMarkerCheck(),
                new TwoDollarCheck(), new WarningDecoderQCCheck() };

        errorMsg = "";
        for (IQCCheck check : checks) {
            errorMsg = check.runQC(header, body, pil);
            if (errorMsg.length() > 0) {
                return false;
            }
        }

        return true;
    }

    public static String getProductWarningType(String nnn) {

        String warningType = null;

        if (productTypeMap.containsKey(nnn)) {
            warningType = productTypeMap.get(nnn);
        } else {
            warningType = "Unknown Warning";
        }

        return warningType;
    }

    public static boolean match(String nnn, String phensig) {
        String mappedNnn = "";

        if (segmentedNNN.contains(nnn)) {
            mappedNnn = followupNNN.get(phensig);
        } else {
            mappedNnn = nnnOfIdent.get(phensig);
        }

        return mappedNnn != null && mappedNnn.equals(nnn);
    }

    public static String[] getImmediateCauses() {
        return immediateCause;
    }

    public static Map<String, String> getCountyTypeMap() {
        return countyTypes;
    }

    public String getErrorMessage() {
        return errorMsg;
    }

    public static void main(String[] args) {
        int pairs = 0;
        String latLon = "LAT...LON 4149 9717 4149 9678 4121 9679 4121 9716";
        Pattern p = Pattern.compile("(\\d{3,4})\\s(\\d{3,5})");

        Matcher m = p.matcher(latLon.substring(9));
        while (m.find()) {
            pairs++;
            double lat = Double.parseDouble(m.group(1));
            double lon = Double.parseDouble(m.group(2));
            if (lat > 9000 || lon > 18000) {
                // Data error in the LAT...LON line.\n";
                break;
            }
        }

        if (pairs <= 2 || pairs > 20) {
            // LAT...LON line missing or malformed.\n";
        }
    }
}
