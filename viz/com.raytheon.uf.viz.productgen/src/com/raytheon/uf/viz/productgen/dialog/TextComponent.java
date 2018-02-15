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

import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.raytheon.uf.viz.spellchecker.text.SpellCheckTextViewer;

/**
 * Simple Text Component to allow text input through the use of a TextViewer.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 23, 2017 29170      Robert.Blum Initial creation
 *
 * </pre>
 *
 * @author Robert.Blum
 */

public class TextComponent extends Composite {

    /**
     * Label describing the text component.
     */
    protected Label label;

    /**
     * The text for the label.
     */
    protected String labelText;

    /**
     * Identifier for this Text Component
     */
    protected String identifier;

    /**
     * The TextViewer component.
     */
    protected TextViewer textViewer;

    public TextComponent(Composite parent, String identifier, String labelText,
            String text) {
        super(parent, SWT.NONE);
        this.identifier = identifier;
        this.labelText = labelText + ":";
        setData(identifier);

        init(parent, text);
    }

    /**
     * Initialize method.
     * 
     * @param parent
     * 
     */
    protected void init(Composite parent, String text) {
        GridLayout gl = new GridLayout(1, false);
        setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.minimumHeight = 125;
        setLayoutData(gd);

        createLabelComposite();
        createTextViewer(text);
    }

    /**
     * Creates a composite containing a Label.
     */
    protected void createLabelComposite() {
        Composite labelComp = new Composite(this, SWT.NONE);
        GridLayout gl = new GridLayout(1, false);
        labelComp.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        labelComp.setLayoutData(gd);

        label = new Label(labelComp, SWT.None);
        GridData gd2 = new GridData(SWT.LEFT, SWT.BOTTOM, true, false);
        label.setLayoutData(gd2);
        label.setText(labelText);
    }

    /**
     * Creates a TextViewer to allow user edits to the text.
     * 
     * @param text
     * @param identifier
     */
    protected void createTextViewer(String text) {
        textViewer = new SpellCheckTextViewer(this,
                SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        final StyledText styledText = textViewer.getTextWidget();
        styledText.setLayoutData(gd);
        styledText.setAlwaysShowScrollBars(false);
        setText(text);

        layout();
        pack();
    }

    /**
     * Set the text in the TextViewer.
     * 
     * @param text
     */
    public void setText(String text) {
        textViewer.getTextWidget().setText(text);
    }

    /**
     * Get the text in the TextViewer.
     * 
     * @return
     */
    public String getText() {
        return textViewer.getTextWidget().getText();
    }
}
