package com.raytheon.uf.viz.productgen.validation.qc;

// #6617 Taken from: AWIPS2_baseline/cave/com.raytheon.viz.texteditor/src/com/raytheon/viz/texteditor/qc

import java.util.ArrayList;

/**
 * 
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
 * Oct 19, 2015 11846      Robert.Blum Fixed to check for mixed case.
 * Oct 27, 2015 6617       Robert.Blum Removed extra space in error msg.
 * Dec 17, 2015 14037      Robert.Blum Changed CTA header to all caps.
 * 
 * </pre>
 * 
 * @author ccody
 * @version 1.0
 */
public class CtaMarkerCheck implements IQCCheck {

    @Override
    public String runQC(String header, String body, String nnn) {
        String errorMsg = "";
        int segmentCount = 0;
        int[] dollarRow = new int[] { 0, 0 };
        String[] separatedLines = body.split("\n");
        for (int i = 0; i < separatedLines.length; i++) {
            String line = separatedLines[i];
            if (line.equals("$$")) {
                dollarRow[segmentCount] = i;
                segmentCount++;
                if (segmentCount > 2) {
                    errorMsg += "There are too many $$ lines.\n";
                    return errorMsg;
                }
            }
        }

        ArrayList<Integer> startMarker = new ArrayList<Integer>();
        ArrayList<Integer> endMarker = new ArrayList<Integer>();
        if (segmentCount == 1) {
            for (int i = 0; i < separatedLines.length; i++) {
                String line = separatedLines[i].trim();
                if (line.equals("PRECAUTIONARY/PREPAREDNESS ACTIONS...")) {
                    startMarker.add(i);
                } else if (line.equals("&&")) {
                    endMarker.add(i);
                }
            }

            if (startMarker.size() == 0 && endMarker.size() != 0) {
                errorMsg += "There is no start marker.\n";
            }
            if (startMarker.size() != 0 && endMarker.size() == 0) {
                errorMsg += "There is no end marker.\n";
            }
            if (startMarker.size() > 1) {
                errorMsg += "There is more than one start marker.\n";
            }
            if (endMarker.size() > 1) {
                errorMsg += "There is more than one end marker.\n";
            }

            int instructLine = 0;
            if (startMarker.size() == 1 && endMarker.size() == 1) {
                if (startMarker.get(0) > endMarker.get(0)) {
                    errorMsg += "End marker is in front of the start marker.\n";
                } else {
                    for (int j = startMarker.get(0) + 1; j < endMarker
                            .get(0); j++) {
                        if (separatedLines[j].trim().length() != 0) {
                            instructLine++;
                            break;
                        }
                    }
                    if (instructLine == 0) {
                        errorMsg += "There is no CTA text inside CTA markers.\n";
                        errorMsg += "Please add CTA text or remove the\nmarkers.\n";
                    }
                }
            }
        } else {
            int j1 = 0;
            int j2 = dollarRow[0];
            String segmentString = "segment one";
            for (int k = 0; k < segmentCount; k++) {
                if (k != 0) {
                    j1 = dollarRow[0] + 1;
                    j2 = separatedLines.length;
                    segmentString = "segment two";
                }

                startMarker.clear();
                endMarker.clear();

                for (int i = j1; i < j2; i++) {
                    String line = separatedLines[i];
                    if (line.equals("PRECAUTIONARY/PREPAREDNESS ACTIONS...")) {
                        startMarker.add(i);
                    } else if (line.equals("&&")) {
                        endMarker.add(i);
                    }
                }

                if (startMarker.size() == 0 && endMarker.size() != 0) {
                    errorMsg += "There is no start marker in " + segmentString
                            + ".\n";
                }
                if (startMarker.size() != 0 && endMarker.size() == 0) {
                    errorMsg += "There is no end marker in " + segmentString
                            + ".\n";
                }
                if (startMarker.size() > 1) {
                    errorMsg += "There is more than one start marker in "
                            + segmentString + ".\n";
                }
                if (endMarker.size() > 1) {
                    errorMsg += "There is more than one end marker in "
                            + segmentString + ".\n";
                }

                int instructLine = 0;
                if (startMarker.size() == 1 && endMarker.size() == 1) {
                    if (startMarker.get(0) > endMarker.get(0)) {
                        errorMsg += "End marker is in front of the start marker in "
                                + segmentString + ".\n";
                    } else {
                        for (int j = startMarker.get(0) + 1; j < endMarker
                                .get(0); j++) {
                            if (separatedLines[j].trim().length() != 0) {
                                instructLine++;
                                break;
                            }
                        }
                        if (instructLine == 0) {
                            errorMsg += "There is no CTA text inside CTA markers "
                                    + segmentString + ".\n";
                            errorMsg += "Please add CTA text or remove the markers "
                                    + segmentString + ".\n";
                        }
                    }
                }
            }
        }

        return errorMsg;
    }

}
