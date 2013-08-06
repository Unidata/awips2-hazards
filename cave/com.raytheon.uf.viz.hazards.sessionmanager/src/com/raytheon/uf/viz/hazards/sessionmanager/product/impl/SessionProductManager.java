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
package com.raytheon.uf.viz.hazards.sessionmanager.product.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;
import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.hazards.productgen.ProductGeneration;
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;
import com.raytheon.uf.viz.core.localization.LocalizationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.SessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.ProductGeneratorEntry;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.ProductGeneratorTable;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.ISessionNotificationSender;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ISessionProductManager;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductFailed;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGenerated;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductInformation;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;
import com.raytheon.viz.core.mode.CAVEMode;

/**
 * Implementation of ISessionProductManager
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 20, 2013 1257       bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class SessionProductManager implements ISessionProductManager {

    private final ISessionTimeManager timeManager;

    /*
     * A full configuration manager is needed to get access to the product
     * generation table, which is not exposed in ISessionConfigurationManager
     */
    private final SessionConfigurationManager configManager;

    private final ISessionEventManager eventManager;

    private final ISessionNotificationSender notificationSender;

    private final ProductGeneration productGen;

    public SessionProductManager(ISessionTimeManager timeManager,
            SessionConfigurationManager configManager,
            ISessionEventManager eventManager,
            ISessionNotificationSender notificationSender) {
        this.timeManager = timeManager;
        this.configManager = configManager;
        this.eventManager = eventManager;
        this.notificationSender = notificationSender;
        this.productGen = new ProductGeneration();
    }

    @Override
    public Collection<ProductInformation> getSelectedProducts() {
        List<ProductInformation> result = new ArrayList<ProductInformation>();
        ProductGeneratorTable pgt = configManager
                .getProductGeneratorTable();
        for (Entry<String, ProductGeneratorEntry> entry : pgt.entrySet()) {
            Set<IHazardEvent> selectedEvents = new HashSet<IHazardEvent>();
            Set<IHazardEvent> potentialEvents = new HashSet<IHazardEvent>();

            for (IHazardEvent e : eventManager.getEvents()) {
                if (e.getPhenomenon() == null || e.getSignificance() == null
                        || e.getState() == HazardState.POTENTIAL) {
                    continue;
                }
                String key = HazardEventUtilities.getPhenSigSubType(e);
                for (String[] pair : entry.getValue().getAllowedHazards()) {
                    if (pair[0].equals(key)) {
                        if (e.getHazardAttribute(
                                        ISessionEventManager.ATTR_SELECTED)
                                        .equals(true)) {
                            selectedEvents.add(e);
                        } else {
                            potentialEvents.add(e);
                        }
                    }
                }
            }
            if (!selectedEvents.isEmpty()) {
                ProductInformation info = new ProductInformation();
                info.setProductName(entry.getKey());
                info.setSelectedEvents(selectedEvents);
                info.setPotentialEvents(potentialEvents);
                // TODO actually get dialog info. Currently getting the dialog
                // info breaks the Replace Watch with Warning Story.
                // info.setDialogInfo(productGen.getDialogInfo(entry.getKey()));
                info.setDialogInfo(Collections.<String, String> emptyMap());
                info.setFormats(new String[] { "XML", "Legacy" });
                result.add(info);
            }
        }
        // TODO remove the reverse. Currently removing the reverse breaks
        // the Replace Watch with Warning Story.
        Collections.reverse(result);
        return result;
    }

    @Override
    public void generate(ProductInformation information, boolean issue) {
        EventSet<IHazardEvent> events = new EventSet<IHazardEvent>();
        events.addAttribute("currentTime", timeManager
                .getCurrentTime().getTime());
        events.addAttribute("siteID", configManager
                .getSiteID());
        events.addAttribute("backupSiteID", LocalizationManager.getInstance()
                .getCurrentSite());

        if (issue) {
            events.addAttribute("issueFlag", "True");
        } else {
            events.addAttribute("issueFlag", "False");
        }
        HashMap<String, String> sessionDict = new HashMap<String, String>();
        sessionDict.put("testMode", CAVEMode.getMode().toString());
        events.addAttribute("sessionDict", sessionDict);


        if (information.getDialogSelections() != null) {
            for (Entry<String, String> entry : information
                    .getDialogSelections().entrySet()) {
                events.addAttribute(entry.getKey(), entry.getValue());
            }
        }
        for (IHazardEvent event : information.getSelectedEvents()) {
            event = new BaseHazardEvent(event);
            for(Entry<String, Serializable> entry : event.getHazardAttributes().entrySet()){
                if(entry.getValue() instanceof Date){
                    entry.setValue(((Date) entry.getValue()).getTime());
                }
            }
            String headline = configManager.getHeadline(
                    event);
            event.addHazardAttribute("headline", headline);
            if (event.getHazardAttribute("forecastPoint") != null) {
                event.addHazardAttribute("geoType", "point");
            } else {
                event.addHazardAttribute("geoType", "area");
            }
            event.removeHazardAttribute("type");
            event.removeHazardAttribute(ISessionEventManager.ATTR_ISSUED);
            event.removeHazardAttribute(ISessionEventManager.ATTR_CHECKED);
            event.removeHazardAttribute(ISessionEventManager.ATTR_SELECTED);
            event.removeHazardAttribute(ISessionEventManager.ATTR_HAZARD_CATEGORY);

            events.add(event);
        }

        String product = information.getProductName();
        String[] formats = information.getFormats();
        IPythonJobListener<List<IGeneratedProduct>> listener = new JobListener(
                issue, notificationSender, information);
        productGen.generate(product, events, formats,
                listener);
    }

    @Override
    public void issue(ProductInformation information) {
        for (IGeneratedProduct product : information.getProducts()) {
            // TODO detect if in practice mode or operational
            // ProductUtils.disseminate(product.toString());
        }
        for (IHazardEvent event : information.getSelectedEvents()) {
            if (event.getState() != HazardState.ENDED) {
                Serializable previewState = event
                        .getHazardAttribute("previewState");
                if(previewState != null && previewState.toString().equalsIgnoreCase(HazardState.ENDED.toString())){
                    event.setState(HazardState.ENDED);
                }else{
                    event.setState(HazardState.ISSUED);
                }
            }
        }
    }

    /**
     * Listens for the completion of product generation and notifies the event
     * bus.
     */
    private class JobListener implements
            IPythonJobListener<List<IGeneratedProduct>> {

        private final boolean issue;

        private final ISessionNotificationSender notificationSender;

        private final ProductInformation info;

        public JobListener(boolean issue,
                ISessionNotificationSender notificationSender,
                ProductInformation info) {
            this.issue = issue;
            this.notificationSender = notificationSender;
            this.info = info;
        }

        @Override
        public void jobFinished(List<IGeneratedProduct> result) {
            info.setProducts(result);
            if (issue) {
                issue(info);
            }
            notificationSender.postNotification(new ProductGenerated(info));
        }

        @Override
        public void jobFailed(Throwable e) {
            info.setError(e);
            notificationSender.postNotification(new ProductFailed(info));
        }

    }

}
