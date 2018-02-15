package com.raytheon.uf.viz.productgen.validation.qc;

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
 * </pre>
 * 
 * @author rferrel
 * @version 1.0
 */
public class TwoDollarCheck implements IQCCheck {

    @Override
    public String runQC(String header, String body, String nnn) {
        String errorMsg = "";
        boolean hasDollar = false;
        String[] separtedLines = body.split("\n");
        for (int i = separtedLines.length - 1; i >= 15; i--) {
            String line = separtedLines[i];
            if (line.startsWith("*") || line.startsWith("LAT...LON")) {
                break;
            }

            if (line.equals("$$")) {
                hasDollar = true;
                break;
            }
        }

        if (separtedLines.length > 15 && !hasDollar) {
            errorMsg += "No $$ found at the bottom.\n";
        }
        return errorMsg;
    }

}
