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

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jep.JepException;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.hazards.productgen.editable.ProductTextUtil;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.python.PythonScript;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
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

    private ProductGenerationDialogUtility() {
    }

    /**
     * Updates the editableEntries map and updates the formatted text on the
     * right side.
     * 
     * @param segmentNumber
     * @param comboSelection
     * @param editableEntries
     * @param formatTabMap
     * @param textAreaMap
     */
    public static void updateEditableEntries(
            int segmentNumber,
            String comboSelection,
            Map<String, List<LinkedHashMap<String, Serializable>>> editableEntries,
            Map<String, AbstractFormatTab> formatTabMap,
            Map<String, Text> textAreaMap) {
        comboSelection = comboSelection + EDITABLE;
        for (Entry<String, Text> entry : textAreaMap.entrySet()) {
            String format = entry.getKey();
            Text textArea = entry.getValue();
            LinkedHashMap<String, Serializable> map = editableEntries.get(
                    format).get(segmentNumber);
            // check if there is a need for a change
            if (map != null
                    && map.get(comboSelection) != null
                    && map.get(comboSelection).equals(textArea.getText()) == false) {
                String highlightedText = String
                        .valueOf(map.get(comboSelection));
                // update the styled text
                AbstractFormatTab tab = formatTabMap.get(format);
                if (tab instanceof TextFormatTab) {
                    TextFormatTab textTab = (TextFormatTab) tab;
                    StyledText styledText = textTab.getText();
                    int startIndex = 0;
                    int length = highlightedText.length();
                    if (length > 0) {
                        startIndex = styledText.getText().indexOf(
                                highlightedText);
                    }

                    if (startIndex != -1) {
                        styledText.replaceTextRange(startIndex, length,
                                textArea.getText());
                        String formattedText = ProductGenerationDialogUtility
                                .formatFrom(format, styledText.getText());
                        styledText.setText(formattedText);

                        // update the editable entries map
                        map.put(comboSelection, textArea.getText());
                    }
                }

            }
        }
    }

    /**
     * Updates the dataList with the new eventData.
     * 
     * @param path
     * @param data
     * @param segmentNumber
     * @param eventData
     */
    public static void updateData(List<String> path,
            LinkedHashMap<String, Serializable> data, int segmentNumber,
            Serializable eventData) {
        // LinkedHashMap<String, Serializable> data = dataList.get(index);
        Map<String, Serializable> currentDataMap = data;
        for (int i = 0; i < path.size(); i++) {
            String key = path.get(i);
            Object obj = currentDataMap.get(key);
            if (i == path.size() - 1) {
                currentDataMap.put(key, eventData);
            } else if (obj instanceof Map<?, ?>) {
                currentDataMap = (Map<String, Serializable>) obj;
            } else if (obj instanceof ArrayList<?>) {
                // need to increment - going down another
                // level
                String newKey = path.get(++i);
                List<?> list = (ArrayList<?>) obj;
                if (list != null && !list.isEmpty()
                        && list.get(segmentNumber) instanceof Map<?, ?>) {
                    Map<String, Serializable> map = (Map<String, Serializable>) list
                            .get(segmentNumber);

                    if (i == path.size() - 1) {
                        // done - found what we're
                        // trying to replace
                        String editableKeyName = newKey
                                + ProductGenerationDialogUtility.EDITABLE;
                        if (map.containsKey(editableKeyName)) {
                            map.put(editableKeyName, eventData);
                        }
                        break;
                    } else if (map.get(path.get(i)) instanceof Map<?, ?>) {
                        // found map to use
                        currentDataMap = (Map<String, Serializable>) map
                                .get(newKey);
                    }

                }
            }
        }
    }

    /**
     * Populates the individual format text areas with the value associated with
     * the editableKey.
     * 
     * @param segmentCombo
     * @param textAreaMap
     * @param individualKeyCombo
     * @param folderIndex
     * @param formatTabList
     * @param products
     */
    public static void updateTextAreas(
            Combo segmentCombo,
            String formattedComboSelection,
            Map<String, List<LinkedHashMap<String, Serializable>>> editableEntries,
            Map<String, AbstractFormatTab> formatTabMap,
            Map<String, Text> textAreaMap) {
        int segmentNumber = segmentCombo.getSelectionIndex();
        String segment = segmentCombo.getItem(segmentNumber);
        for (String format : textAreaMap.keySet()) {
            if (editableEntries.get(format) != null
                    && editableEntries.get(format).get(segmentNumber) != null) {
                Serializable value = editableEntries.get(format)
                        .get(segmentNumber)
                        .get(formattedComboSelection + EDITABLE);
                String highlightedText = "";
                if (value != null) {
                    highlightedText = String.valueOf(value);
                }
                textAreaMap.get(format).setText(highlightedText);

                AbstractFormatTab tab = formatTabMap.get(format);
                if (tab instanceof TextFormatTab) {
                    TextFormatTab textTab = (TextFormatTab) tab;
                    StyledText styledText = textTab.getText();
                    int startIndex = 0;
                    int length = highlightedText.length();
                    if (length > 0) {
                        int offset = styledText.getText().indexOf(segment);
                        startIndex = styledText.getText().indexOf(
                                highlightedText, offset);
                    }

                    if (startIndex == -1) {
                        startIndex = 0;
                    }

                    // performs highlighting
                    styledText.setSelectionRange(startIndex, length);

                    // scrolls to the line
                    for (int lineIndex = 0; lineIndex < styledText
                            .getLineCount(); lineIndex++) {
                        if (highlightedText.contains(styledText
                                .getLine(lineIndex))) {
                            styledText.setTopIndex(lineIndex);
                            break;
                        }
                    }
                }
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
            StyledText styledText = textTab.getText();
            int startIndex = 0;
            int length = highlightedText.length();
            if (length > 0) {
                startIndex = styledText.getText().indexOf(highlightedText);
            }

            if (startIndex == -1) {
                startIndex = 0;
                length = 0;
            }

            // performs highlighting
            styledText.setSelectionRange(startIndex, length);

            // scrolls to the line
            for (int lineIndex = 0; lineIndex < styledText.getLineCount(); lineIndex++) {
                if (highlightedText.contains(styledText.getLine(lineIndex))) {
                    styledText.setTopIndex(lineIndex);
                    break;
                }
            }
        }
    }

    /**
     * Calls the formatFrom method in python formatters
     */
    private static String formatFrom(String format, String text) {
        String rval = text;
        try {
            IPathManager pm = PathManagerFactory.getPathManager();
            LocalizationContext baseContext = pm.getContext(
                    LocalizationType.COMMON_STATIC, LocalizationLevel.BASE);
            File baseFile = pm.getFile(baseContext, "python" + File.separator
                    + "events" + File.separator + "productgen" + File.separator
                    + "formats" + File.separator + format + ".py");

            String anIncludePath = pm.getFile(
                    baseContext,
                    "python" + File.separator + "events" + File.separator
                            + "productgen" + File.separator + "formats")
                    .getPath();

            Map<String, Object> args = new HashMap<String, Object>(1);
            args.put("text", text);

            PythonScript script = new PythonScript(baseFile.getPath(),
                    anIncludePath,
                    ProductGenerationDialog.class.getClassLoader());
            script.instantiatePythonClass("formatter", "Format", null);
            Object obj = script.execute("formatFrom", "formatter", args);
            if (obj != null) {
                rval = String.valueOf(obj);
            }
        } catch (JepException e) {
            handler.error("Error formatting the text ", e);
            rval = text;
        }
        return rval;
    }

    @Deprecated
    public static void save(String productID, List<Segment> decodedSegments,
            GeneratedProductList products) {
        IHazardEvent hazardEvent = (IHazardEvent) products.getEventSet()
                .iterator().next();
        String eventID = hazardEvent.getEventID();
        // TODO This is not correctly set
        String productCategory = productID;

        for (Segment segment : decodedSegments) {
            // TODO verify that the data in segment gets set again
            Map<String, WidgetInfo> widgetInfoMap = WidgetInfoFactory
                    .createWidgetInfoMap(segment.getData());
            for (WidgetInfo widgetInfo : widgetInfoMap.values()) {
                String key = widgetInfo.getLabel();
                Serializable value = widgetInfo.getValue();
                ProductTextUtil.createOrUpdateProductText(key, productCategory,
                        productID, segment.getSegmentID(), eventID, value);
            }
        }

    }

    public static void save(GeneratedProductList products) {

        // Need site, eventID, endTime (for purge), data
    }

    public static List<Segment> separateSegments(
            LinkedHashMap<String, Serializable> data) {
        List<Segment> segments = new ArrayList<Segment>();
        // TODO Will need to be replaced with an interface to make
        // refactoring of the python dictionary easier
        LinkedHashMap<String, Serializable> segmentsMap = (LinkedHashMap<String, Serializable>) data
                .get("segments");
        List<Serializable> list = (List<Serializable>) segmentsMap
                .get("segment");
        for (Serializable item : list) {
            Segment segment = new Segment(
                    (LinkedHashMap<String, Serializable>) item);
            segments.add(segment);
        }

        return segments;
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
    public static int countEditables(Map<String, Serializable> data) {
        int count = 0;
        if (data != null) {
            for (Entry<String, Serializable> entry : data.entrySet()) {
                if (entry.getKey().contains(EDITABLE)) {
                    count++;
                }

                // if (entry.getValue() instanceof LinkedHashMap<?, ?>) {
                if (entry.getValue() instanceof Map<?, ?>) {
                    count += countEditables((Map<String, Serializable>) entry
                            .getValue());
                } else if (entry.getValue() instanceof ArrayList) {
                    ArrayList<?> list = (ArrayList<?>) entry.getValue();
                    for (Object item : list) {
                        if (item instanceof Map<?, ?>) {
                            count += countEditables((Map<String, Serializable>) item);
                        }
                    }
                }
            }

        }
        return count;
    }
}
