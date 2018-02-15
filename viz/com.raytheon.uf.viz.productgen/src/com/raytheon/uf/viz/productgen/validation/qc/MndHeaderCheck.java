package com.raytheon.uf.viz.productgen.validation.qc;

import java.util.regex.Matcher;

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
 * Jun 23, 2016 18215      Robert.Blum Fixed validation to check for mixed case hazard name.
 * Sep 01, 2016 21618      Kevin.Bisanz Fixed validation to check for mixed case "Issued by..."
 * </pre>
 * 
 * @author rferrel
 * @version 1.0
 */

public class MndHeaderCheck implements IQCCheck {

    @Override
    public String runQC(String header, String body, String nnn) {
        String errorMsg = "";
        if (!nnn.equalsIgnoreCase("FFW") && !nnn.equalsIgnoreCase("SVS")
                && !nnn.equalsIgnoreCase("FFS") && !nnn.equalsIgnoreCase("FLW")
                && !nnn.equalsIgnoreCase("FLS") && !nnn.equalsIgnoreCase("MWS")
                && !body.contains("EAS ACTIVATION")
                && !body.contains("IMMEDIATE BROADCAST")) {
            errorMsg += "BULLETIN line not found in the MND header.\n";
            return errorMsg;
        }

        boolean dateTested = false;
        int bulletinState = 0;

        if (nnn.equalsIgnoreCase("SVS") && nnn.equalsIgnoreCase("FFS")
                && nnn.equalsIgnoreCase("FLW") && nnn.equalsIgnoreCase("FLS")
                && nnn.equalsIgnoreCase("MWS")) {
            bulletinState = 1;
        }

        String[] separatedLines = body.split("\n");
        for (String line : separatedLines) {
            if (line.contains("EAS ACTIVATION")
                    || line.contains("IMMEDIATE BROADCAST")) {
                bulletinState = 1;
                continue;
            }

            if (bulletinState == 1) {

                String productType = QualityControl.getProductWarningType(nnn);
                if (QualityControl.getProductWarningType(nnn)
                        .startsWith("Unknown")) {
                    errorMsg += "Invalid event type in MND header";
                } else if (line.contains(productType) == false) {
                    errorMsg += "Event type in MND header does not\nmatch "
                            + nnn + ".\n";
                }
                bulletinState++;
            } else if (bulletinState == 2) {
                if (line.contains("UNLOCALIZED SITE")) {
                    errorMsg += "Unlocalized site in MND header.\n";
                }
                bulletinState++;
            } else if (bulletinState == 3 || bulletinState == 4) {
                if (line.startsWith("Issued by National Weather Service")) {
                    if (line.endsWith("UNLOCALIZED SITE")) {
                        errorMsg += "Unlocalized site in the service backup line.\n";
                    }
                } else if (!dateTested) {
                    Matcher m = datePtrn.matcher(line);
                    if (!m.find()) {
                        errorMsg += "No date and time line in MND header.\n";
                    }
                    dateTested = true;
                }
                bulletinState++;
            }

        }

        return errorMsg;
    }

}
