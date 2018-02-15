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
package gov.noaa.gsd.viz.hazards.detailsviewer;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Input into eclipse's built-in Compare functionality that is used to compared
 * two hazard events.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 27, 2017 33189      Robert.Blum Initial creation
 * May 04, 2017 33778      Robert.Blum CompareConfiguration is now a parameter.
 *
 * </pre>
 *
 * @author Robert.Blum
 */

public class EventCompareInput extends CompareEditorInput {

    private CompareEvent event1;

    private CompareEvent event2;

    public EventCompareInput(CompareEvent event1, CompareEvent event2,
            CompareConfiguration config) {
        super(config);
        setTitle("Event Comparison");
        this.event1 = event1;
        this.event2 = event2;
        config.setLeftEditable(event1.isEditable());
        config.setRightEditable(event2.isEditable());
        config.setLeftLabel(event1.getName());
        config.setRightLabel(event2.getName());
    }

    @Override
    protected Object prepareInput(IProgressMonitor monitor)
            throws InvocationTargetException, InterruptedException {
        DiffNode node = new DiffNode(event1, event2);
        return node;
    }

}
