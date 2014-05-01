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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.hazards.productgen.KeyInfo;
import com.raytheon.uf.common.hazards.productgen.editable.ProductTextUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.productgen.dialog.data.AbstractProductGeneratorData;
import com.raytheon.uf.viz.productgen.dialog.data.ProductLevelData;
import com.raytheon.uf.viz.productgen.dialog.data.SegmentData;
import com.raytheon.uf.viz.productgen.dialog.formats.AbstractFormatTab;
import com.raytheon.uf.viz.productgen.dialog.formats.TextFormatTab;

/**
 * Helper utility method to perform some value replacements in various maps used
 * by the product generation dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 7, 2014            jsanchez     Initial creation
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class ProductGenerationDialogUtility {

    private static final IUFStatusHandler handler = UFStatus
            .getHandler(ProductGenerationDialogUtility.class);

    /*
     * String appended to dictionary keys indicating the value is editable
     */
    public static final String EDITABLE = ":editable";

    private static final KeyInfo SEGMENT_ID = KeyInfo
            .createBasicKeyInfo("segmentID");

    private ProductGenerationDialogUtility() {
    }

    /**
     * Updates the dataList with the new eventData.
     * 
     * @param path
     * @param data
     * @param segmentNumber
     * @param eventData
     */
    public static void updateData(List<KeyInfo> path,
            Map<KeyInfo, Serializable> data, int segmentNumber,
            Serializable eventData) {
        Map<KeyInfo, Serializable> currentDataMap = data;
        for (int i = 0; i < path.size(); i++) {
            KeyInfo key = path.get(i);
            Serializable obj = currentDataMap.get(key);
            if (i == path.size() - 1) {
                currentDataMap.put(key, eventData);
            } else if (obj instanceof Map<?, ?>) {
                currentDataMap = (Map<KeyInfo, Serializable>) obj;
            } else if (obj instanceof ArrayList<?>) {
                // need to increment - going down another
                // level
                KeyInfo newKey = path.get(++i);
                List<?> list = (ArrayList<?>) obj;
                if (list != null && !list.isEmpty()
                        && list.get(segmentNumber - 1) instanceof Map<?, ?>) {
                    Map<KeyInfo, Serializable> map = (Map<KeyInfo, Serializable>) list
                            .get(segmentNumber - 1);

                    if (i == path.size() - 1) {
                        // done - found what we're
                        // trying to replace
                        if (map.containsKey(newKey)) {
                            map.put(newKey, eventData);
                        }
                        break;
                    } else if (map.get(path.get(i)) instanceof Map<?, ?>) {
                        // found map to use
                        currentDataMap = (Map<KeyInfo, Serializable>) map
                                .get(newKey);
                    }

                }
            }
        }
    }

    /**
     * Moves the format tab to the select segmentID
     * 
     * @param segmentID
     * @param formatTabMap
     */
    public static void selectSegmentInTabs(
            AbstractProductGeneratorData productGeneratorData,
            Map<String, AbstractFormatTab> formatTabMap) {
        clearHighlighting(formatTabMap);
        for (AbstractFormatTab tab : formatTabMap.values()) {
            if (tab instanceof TextFormatTab) {
                TextFormatTab textTab = (TextFormatTab) tab;
                StyledText styledText = textTab.getText();
                productGeneratorData.highlight(styledText);
            }
        }
    }

    /**
     * Selects the appropriate tab that matches the format and highlights the
     * editable value in that format tab.
     * 
     * @param format
     * @param formatFolder
     * @param formatTabMap
     * @param textAreaMap
     */
    public static void selectFormatTab(String format, CTabFolder formatFolder,
            Map<String, AbstractFormatTab> formatTabMap,
            Map<String, Text> textAreaMap) {
        // selects the format in the list of format tab folder
        int counter = 0;
        for (CTabItem tabItem : formatFolder.getItems()) {
            if (tabItem.getText().equals(format)) {
                formatFolder.setSelection(counter);
                break;
            }
            counter++;
        }

        String highlightedText = textAreaMap.get(format).getText();
        AbstractFormatTab tab = formatTabMap.get(format);
        if (tab instanceof TextFormatTab) {
            TextFormatTab textTab = (TextFormatTab) tab;
            highlight(textTab.getText(), highlightedText);
        }
    }

    public static void highlight(StyledText styledText, String text) {
        String currentText = styledText.getText();
        String oldText = String.valueOf(styledText.getData());
        int startIndex = StringUtils.indexOfDifference(currentText, oldText);
        int length = text.trim().length();
        if (startIndex == -1) {
            startIndex = 0;
            length = 0;
        }

        // performs highlighting
        styledText.setSelectionRange(startIndex - 1, length);

        // scrolls to the line
        int counter = 0;
        for (int lineIndex = 0; lineIndex < styledText.getLineCount()
                && startIndex != -1; lineIndex++) {
            String line = styledText.getLine(lineIndex);
            counter += line.length();
            if (counter >= startIndex) {
                styledText.setTopIndex(lineIndex);
                break;
            }

        }
    }

    public static void save(List<AbstractProductGeneratorData> decodedData) {
        for (AbstractProductGeneratorData segment : decodedData) {

            for (KeyInfo editableKey : segment.getEditableKeys()) {
                Serializable value = segment
                        .getModifiedValue(editableKey, true);
                if (value != null) {
                    ProductTextUtil.createOrUpdateProductText(
                            editableKey.getName(),
                            editableKey.getProductCategory(),
                            editableKey.getProductID(),
                            editableKey.getSegment(),
                            editableKey.getEventIDs(), value);
                }
            }
            segment.clearModifiedValues();
        }

    }

    /**
     * Decodes the product generator data by separating the data into segments
     * and a non-segment called product level
     * 
     * @param data
     * @return
     */
    public static List<AbstractProductGeneratorData> decodeProductGeneratorData(
            LinkedHashMap<KeyInfo, Serializable> data) {
        List<AbstractProductGeneratorData> decodedData = new ArrayList<AbstractProductGeneratorData>();

        LinkedHashMap<KeyInfo, Serializable> tempData = (LinkedHashMap<KeyInfo, Serializable>) data
                .clone();
        tempData.remove(SegmentData.PARENT_PATH.get(0));
        ProductLevelData productLevel = new ProductLevelData(tempData);
        decodedData.add(productLevel);

        List<Serializable> list = (List<Serializable>) data
                .get(SegmentData.PARENT_PATH.get(0));

        int counter = 0;
        for (Serializable item : list) {

            LinkedHashMap<KeyInfo, Serializable> segmentData = (LinkedHashMap<KeyInfo, Serializable>) item;
            String segmentID = null;
            if (segmentData.get(SEGMENT_ID) == null) {
                segmentID = "Unidentified Segment " + counter;
                counter++;
            } else {
                segmentID = String.valueOf(segmentData.get(SEGMENT_ID));
            }

            SegmentData segment = new SegmentData(segmentData, segmentID);
            decodedData.add(segment);
        }

        return decodedData;
    }

    /**
     * Removes the highlighting in each of the format tabs
     * 
     * @param formatTabMap
     */
    public static void clearHighlighting(
            Map<String, AbstractFormatTab> formatTabMap) {
        if (formatTabMap != null) {
            for (AbstractFormatTab tab : formatTabMap.values()) {
                if (tab instanceof TextFormatTab) {
                    TextFormatTab textTab = (TextFormatTab) tab;
                    StyledText styledText = textTab.getText();
                    styledText.setSelectionRange(0, 0);
                }
            }
        }
    }

    /**
     * Method to return the number of editable fields. This method also
     * traverses down into all levels of the LinkedHashMap, such as a
     * LinkedHashMap within a LinkedHashMap. However, this method count sub
     * levels as editble if the parent is editable.
     * 
     * @return
     */
    public static int countEditables(Map<KeyInfo, Serializable> data) {
        int count = 0;
        if (data != null) {
            for (Entry<KeyInfo, Serializable> entry : data.entrySet()) {
                if (entry.getKey().isEditable()) {
                    count++;
                }

                // if (entry.getValue() instanceof LinkedHashMap<?, ?>) {
                if (entry.getValue() instanceof Map<?, ?>) {
                    count += countEditables((Map<KeyInfo, Serializable>) entry
                            .getValue());
                } else if (entry.getValue() instanceof ArrayList) {
                    ArrayList<?> list = (ArrayList<?>) entry.getValue();
                    for (Object item : list) {
                        if (item instanceof Map<?, ?>) {
                            count += countEditables((Map<KeyInfo, Serializable>) item);
                        }
                    }
                }
            }

        }

        return count;
    }

    /**
     * Parses the "editable" part of the key out to give the "pretty" text
     * 
     * @param key
     * @return
     */
    public static String parseEditable(String key) {
        String returnKey = key;
        if (key.contains(EDITABLE.substring(1))) {
            returnKey = key
                    .substring(0, key.indexOf(EDITABLE.substring(1)) - 1);
        }
        return returnKey;
    }

}
