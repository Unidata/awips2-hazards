/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.display;

import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;

import java.util.Calendar;
import java.util.List;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.viz.core.drawables.IDescriptor.FramesInfo;
import com.raytheon.viz.ui.VizWorkbenchManager;
import com.raytheon.viz.ui.editor.AbstractEditor;

/**
 * Description: TODO Move these methods to better places.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 30, 2013     752       daniel.s.schaffer@noaa.gov      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class HazardServicesEditorUtilities {

    /**
     * Constants representing CAVE frame information.
     */
    public static final String FRAME_TIMES = "frameTimeList";

    public static final String FRAME_INDEX = "frameIndex";

    public static final String FRAME_COUNT = "frameCount";

    public static final String CURRENT_FRAME = "currentFrame";

    /**
     * Convenience method for retrieving the current editor. Each perspective
     * has its own editor. So, when a reference to the current editor is needed,
     * it is safer to query for the current editor than to rely on a stored
     * editor reference.
     * 
     * @return Reference to the current CAVE editor.
     */
    public static AbstractEditor getCurrentEditor() {
        return ((AbstractEditor) VizWorkbenchManager.getInstance()
                .getActiveEditor());
    }

    public static Dict buildFrameInformation() {
        AbstractEditor editor = HazardServicesEditorUtilities.getCurrentEditor();
        FramesInfo framesInfo = editor.getActiveDisplayPane().getDescriptor()
                .getFramesInfo();
        DataTime currentFrame = framesInfo.getCurrentFrame();

        Dict frameDict = new Dict();
        frameDict.put(CURRENT_FRAME, currentFrame);
        if (framesInfo != null) {
            final int frameCount = framesInfo.getFrameCount();
            final int frameIndex = framesInfo.getFrameIndex();
            DataTime[] dataFrames = framesInfo.getFrameTimes();

            if (frameIndex >= 0) {
                final List<Long> dataTimeList = Lists.newArrayList();

                if (dataFrames != null) {
                    for (DataTime dataTime : dataFrames) {
                        Calendar cal = dataTime.getValidTime();
                        dataTimeList.add(cal.getTimeInMillis());
                    }
                }

                frameDict.put(FRAME_COUNT, frameCount);
                frameDict.put(FRAME_INDEX, frameIndex);
                frameDict.put(FRAME_TIMES, dataTimeList);
            }
        }
        return frameDict;
    }

    @SuppressWarnings("unchecked")
    public static List<Long> frameTimes() {
        Dict frameInfo = buildFrameInformation();
        return (List<Long>) frameInfo.get(FRAME_TIMES);
    }

    public static DataTime currentFrame() {
        return (DataTime) buildFrameInformation().get(CURRENT_FRAME);
    }
}
