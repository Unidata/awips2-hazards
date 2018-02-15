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
package gov.noaa.gsd.viz.hazards.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IServiceLocator;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import gov.noaa.gsd.viz.hazards.display.HazardServicesActivator;
import gov.noaa.gsd.viz.hazards.display.HazardServicesAppBuilder;

/**
 * This class listens for changes related to hazard services views and keeps the
 * hazard services views synchronized across all perspectives. When a view is
 * closed in one perspective, it is closed in other perspectives as well.
 *
 * The class is based on
 * com.raytheon.uf.viz.collaboration.ui.CollaborationPerspectiveListener with
 * IPerspectiveListener2 methods removed because
 * {@link HazardServicesAppBuilder} implements those methods.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer      Description
 * ------------- -------- ---------     -------------------------------------
 * Oct 05, 2016  22300    Kevin.Bisanz  Initial creation
 *                                      (copied from
 *                                      CollaborationPerspectiveListener)
 *
 * </pre>
 *
 * @author kbisanz (copied from bsteffen creation)
 */
public class HazardServicesPerspectiveListener
        implements IPageListener, EventHandler {

    private static Map<IWorkbenchPage, HazardServicesPerspectiveListener> listeners = new HashMap<>(
            4);

    private HazardServicesPerspectiveListener() {

    }

    @Override
    public void handleEvent(Event event) {
        String att = (String) event.getProperty(UIEvents.EventTags.ATTNAME);
        if (UIEvents.UIElement.TOBERENDERED.equals(att)) {
            handleToBeRenderedEvent(event);
        } else if (UIEvents.UIElement.VISIBLE.equals(att)) {
            handleVisibleEvent(event);
        } else {
            /*
             * This should never happen because specific topics/tags are
             * subscribed to in initializeListener(IWorkbenchPage).
             */
            throw new IllegalStateException("Unexpected event type of " + att);
        }
    }

    /**
     * Handle the case of a hazard services view setting toBeRendered as false.
     * This occurs when a view is closed, this method will then close the view
     * in all other perspectives as well.
     */
    protected void handleToBeRenderedEvent(Event event) {
        MUIElement element = (MUIElement) event
                .getProperty(UIEvents.EventTags.ELEMENT);
        if (element == null) {
            return;
        }
        if (element.getElementId() == null) {
            return;
        }
        if (!element.getElementId()
                .startsWith(HazardServicesActivator.PLUGIN_ID)) {
            return;
        }
        if (element.isToBeRendered()) {
            return;
        }
        IServiceLocator services = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        MWindow window = services.getService(MWindow.class);
        EModelService modelService = services.getService(EModelService.class);
        List<MPlaceholder> placeholders = modelService.findElements(window,
                element.getElementId(), MPlaceholder.class, null);
        for (MPlaceholder placeholder : placeholders) {
            if (placeholder.isToBeRendered()) {
                placeholder.setToBeRendered(false);
            }
        }
    }

    /**
     * Handle the case of an MPartStack that has a hazard services view selected
     * becoming visible. This happens when the part is restored from a minimized
     * state. In that case nothing notifies the view that it has become active
     * so call {@link IWorkbenchPart#setFocus()} to let the view know it is
     * active.
     */
    protected void handleVisibleEvent(Event event) {
        Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
        if (element == null) {
            return;
        }
        if (!(element instanceof MPartStack)) {
            return;
        }
        Object newValue = event.getProperty(UIEvents.EventTags.NEW_VALUE);
        if (Boolean.FALSE.equals(newValue)) {
            return;
        }
        MPartStack partStack = (MPartStack) element;
        MUIElement selected = partStack.getSelectedElement();

        if (selected == null) {
            return;
        }

        if (!selected.getElementId()
                .startsWith(HazardServicesActivator.PLUGIN_ID)) {
            return;
        }
        if (selected instanceof MPlaceholder) {
            selected = ((MPlaceholder) selected).getRef();
        }
        if (selected instanceof MPart) {
            ((MPart) selected).getContext().get(IWorkbenchPart.class)
                    .setFocus();
        }
    }

    @Override
    public void pageClosed(IWorkbenchPage page) {
        IWorkbenchWindow window = page.getWorkbenchWindow();
        window.removePageListener(this);
        IEventBroker broker = window.getService(IEventBroker.class);
        broker.unsubscribe(this);
        listeners.remove(page);
    }

    @Override
    public void pageActivated(IWorkbenchPage page) {
        /* Don't need to know about this particular event. */
    }

    @Override
    public void pageOpened(IWorkbenchPage page) {
        /* Don't need to know about this particular event. */
    }

    /**
     * Add a {@link HazardServicesPerspectiveListener} to the provided page if
     * it does not already have one. If the page already has a
     * HazardServicesPerspectiveListener then this method will do nothing.
     */
    public static void initializeListener(IWorkbenchPage page) {
        if (!listeners.containsKey(page)) {
            HazardServicesPerspectiveListener listener = new HazardServicesPerspectiveListener();
            IWorkbenchWindow window = page.getWorkbenchWindow();
            window.addPageListener(listener);
            IEventBroker broker = window.getService(IEventBroker.class);
            broker.subscribe(UIEvents.UIElement.TOPIC_TOBERENDERED, listener);
            broker.subscribe(UIEvents.UIElement.TOPIC_VISIBLE, listener);
            listeners.put(page, listener);
        }
    }
}
