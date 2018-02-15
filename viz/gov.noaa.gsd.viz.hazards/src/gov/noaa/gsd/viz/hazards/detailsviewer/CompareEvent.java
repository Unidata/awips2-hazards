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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.compare.BufferedContent;
import org.eclipse.compare.IEditableContent;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;

/**
 * Holds a string representation of the Hazard Event and a label defining the
 * specific event. This is used when comparing hazard events.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 27, 2017 33189      Robert.Blum Initial creation
 *
 * </pre>
 *
 * @author Robert.Blum
 */

public class CompareEvent extends BufferedContent
        implements ITypedElement, IEditableContent {

    private String eventData;

    private String eventLabel;

    public CompareEvent(String label, String data) {
        eventLabel = label;
        eventData = data;
    }

    @Override
    public String getName() {
        return eventLabel;
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public ITypedElement replace(ITypedElement dest, ITypedElement src) {
        return null;
    }

    @Override
    public Image getImage() {
        return null;
    }

    @Override
    public String getType() {
        // Compare as text
        return TEXT_TYPE;
    }

    @Override
    protected InputStream createStream() throws CoreException {
        return new ByteArrayInputStream(
                eventData.getBytes(StandardCharsets.US_ASCII));
    }

}
