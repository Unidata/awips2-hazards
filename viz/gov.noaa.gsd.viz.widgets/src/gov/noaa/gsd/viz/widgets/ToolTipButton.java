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
package gov.noaa.gsd.viz.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * This class is a Basic SWT Button wrapped in a composite. The main purpose is
 * so that a tooltip can be added to both the button and composite. Thus
 * allowing it to always be displayed. By default if you disable a SWT Button
 * the tooltip no longer gets displayed.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 6, 2017  29996      Robert.Blum Initial creation
 *
 * </pre>
 *
 * @author Robert.Blum
 */

public class ToolTipButton {

    private Composite wrapper;

    private Button button;

    public ToolTipButton(Composite parent, int buttonStyle, String toolTip,
            String disabledToolTip) {
        wrapper = new Composite(parent, SWT.None);

        /*
         * Set the Layout margins to zero so the composite is the exact size of
         * the button.
         */
        GridLayout gl = new GridLayout(1, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        wrapper.setLayout(gl);

        button = new Button(wrapper, buttonStyle);

        /*
         * Set the tooltip on the button so it is displayed when the button is
         * enabled. Then set the disabled tooltip on the composite so it is
         * displayed when the button is disabled. Since the mouse hover event
         * will get passed up to the wrapper composite when the button is
         * disabled.
         */
        button.setToolTipText(toolTip);
        wrapper.setToolTipText(disabledToolTip);
    }

    public Composite getComposite() {
        return wrapper;
    }

    public Button getButton() {
        return button;
    }
}
