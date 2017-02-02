/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

/**
 * Description: Customizable tab folder layout, to be used with a
 * {@link CustomizableTabFolder}.
 * <p>
 * Note that this class is a copy of the package-private class
 * <code>org.eclipse.swt.custom.CTabFolderLayout</code> changed to work with a
 * <code>CustomizableTabFolder</code> instead of a {@link CTabFolder}.
 * </p>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Feb 05, 2017   15556    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
class CustomizableTabFolderLayout extends Layout {
    @Override
    protected Point computeSize(Composite composite, int wHint, int hHint,
            boolean flushCache) {
        CustomizableTabFolder folder = (CustomizableTabFolder) composite;
        CustomizableTabItem[] items = folder.items;
        CustomizableTabFolderRenderer renderer = folder.renderer;
        // preferred width of tab area to show all tabs
        int tabW = 0;
        int selectedIndex = folder.selectedIndex;
        if (selectedIndex == -1) {
            selectedIndex = 0;
        }
        GC gc = new GC(folder);
        for (int i = 0; i < items.length; i++) {
            if (folder.single) {
                tabW = Math.max(tabW, renderer.computeSize(i, SWT.SELECTED, gc,
                        SWT.DEFAULT, SWT.DEFAULT).x);
            } else {
                int state = 0;
                if (i == selectedIndex) {
                    state |= SWT.SELECTED;
                }
                tabW += renderer.computeSize(i, state, gc, SWT.DEFAULT,
                        SWT.DEFAULT).x;
            }
        }
        tabW += 3;

        if (folder.showMax) {
            tabW += renderer.computeSize(
                    CustomizableTabFolderRenderer.PART_MAX_BUTTON, SWT.NONE,
                    gc, SWT.DEFAULT, SWT.DEFAULT).x;
        }
        if (folder.showMin) {
            tabW += renderer.computeSize(
                    CustomizableTabFolderRenderer.PART_MIN_BUTTON, SWT.NONE,
                    gc, SWT.DEFAULT, SWT.DEFAULT).x;
        }
        if (folder.single) {
            tabW += renderer.computeSize(
                    CustomizableTabFolderRenderer.PART_CHEVRON_BUTTON,
                    SWT.NONE, gc, SWT.DEFAULT, SWT.DEFAULT).x;
        }
        if (folder.topRight != null) {
            Point pt = folder.topRight.computeSize(SWT.DEFAULT,
                    folder.tabHeight, flushCache);
            tabW += 3 + pt.x;
        }

        gc.dispose();

        int controlW = 0;
        int controlH = 0;
        // preferred size of controls in tab items
        for (int i = 0; i < items.length; i++) {
            Control control = items[i].getControl();
            if (control != null && !control.isDisposed()) {
                Point size = control.computeSize(wHint, hHint, flushCache);
                controlW = Math.max(controlW, size.x);
                controlH = Math.max(controlH, size.y);
            }
        }

        int minWidth = Math.max(tabW, controlW);
        int minHeight = (folder.minimized) ? 0 : controlH;
        if (minWidth == 0) {
            minWidth = CustomizableTabFolder.DEFAULT_WIDTH;
        }
        if (minHeight == 0) {
            minHeight = CustomizableTabFolder.DEFAULT_HEIGHT;
        }

        if (wHint != SWT.DEFAULT) {
            minWidth = wHint;
        }
        if (hHint != SWT.DEFAULT) {
            minHeight = hHint;
        }

        return new Point(minWidth, minHeight);
    }

    @Override
    protected boolean flushCache(Control control) {
        return true;
    }

    @Override
    protected void layout(Composite composite, boolean flushCache) {
        CustomizableTabFolder folder = (CustomizableTabFolder) composite;
        // resize content
        if (folder.selectedIndex != -1) {
            Control control = folder.items[folder.selectedIndex].getControl();
            if (control != null && !control.isDisposed()) {
                control.setBounds(folder.getClientArea());
            }
        }
    }
}
