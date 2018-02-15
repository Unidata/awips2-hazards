package com.raytheon.uf.viz.productgen.validation.qc;

import java.util.regex.Matcher;

import com.raytheon.uf.viz.productgen.validation.util.VtecObject;
import com.raytheon.uf.viz.productgen.validation.util.VtecUtil;

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
 * Oct 27, 2015 6617       Robert.Blum Fixed typo in error msg.
 * </pre>
 * 
 * @author rferrel
 * @version 1.0
 */
public class WmoHeaderCheck implements IQCCheck {

    @Override
    public String runQC(String header, String body, String nnn) {
        String errorMsg = "";

        if (header == null || header.length() == 0) {
            return "\nNo text found.\n";
        }

        String[] separatedLines = header.trim().split("\n");
        if (separatedLines == null || separatedLines.length < 2) {
            return "\nIncomplete product.\n";
        }

        Matcher m = wmoHeaderPtrn.matcher(separatedLines[0]);
        if (m.matches()) {
            // Check the Type in the TTAAii.
            if ((nnn.equals("SVR") && !m.group(1).equals("WU"))
                    || (nnn.equals("TOR") && !m.group(1).equals("WF"))
                    || (nnn.equals("SMW") && !m.group(1).equals("WH"))
                    || (nnn.equals("FFW") && !m.group(1).equals("WG"))) {
                errorMsg += nnn + " doesn't match " + m.group(1)
                        + "\n in TTAAii.\n";
            }

            // Check BBB.
            if (m.group(4) != null && !m.group(4).equals("")
                    && !m.group(4).equals("RR") && !m.group(4).equals("4")
                    && !m.group(4).equals("AA") && !m.group(4).equals("CC")) {
                errorMsg += "BBB: " + m.group(4) + m.group(5) + " error.\n";
            }

        } else {
            errorMsg += "First line is not WMO header or invalid format.\n";
        }

        m = awipsIDPtrn.matcher(separatedLines[1]);
        if (m.matches() == false) {
            errorMsg += "No NNNXXX on second line.\n";
        }

        VtecObject vtec = VtecUtil.parseMessage(body);
        if (vtec == null && nnn.equals("MWS") == false) {
            errorMsg += "\nNo VTEC line found.\n";
        } else if (vtec != null && !QualityControl.match(nnn,
                vtec.getPhenomena() + "." + vtec.getSignificance())) {
            if (nnn.equals("SVS") && vtec.getPhenomena().equals("EW")
                    && vtec.getSignificance().equals("W")) {
                // Do not create error message.
            } else {
                errorMsg += "VTEC event type (" + vtec.getPhenomena() + "."
                        + vtec.getSignificance() + ") doesn't match NNN.\n";
            }
        }
        return errorMsg;
    }
}
