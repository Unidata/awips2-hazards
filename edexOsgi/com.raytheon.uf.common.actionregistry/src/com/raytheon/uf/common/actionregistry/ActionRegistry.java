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
package com.raytheon.uf.common.actionregistry;

import java.util.Collection;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Allows actions to be registered and run when an event is posted to this
 * registry.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 20, 2013            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class ActionRegistry {

    private Multimap<String, IActionable> actionableMap;

    private static ActionRegistry instance;

    /**
     * 
     */
    private ActionRegistry() {
        actionableMap = ArrayListMultimap.create();
    }

    public static ActionRegistry getInstance() {
        if (instance == null) {
            instance = new ActionRegistry();
        }
        return instance;
    }

    /**
     * Registers text and an action with the ActionRegistry
     * 
     * @param action
     * @param actionable
     * @return
     */
    public IActionable register(String action, IActionable actionable) {
        actionableMap.put(action, actionable);
        return actionable;
    }

    /**
     * Takes an action name and executes every action associated with that name
     * 
     * @param action
     * @param arguments
     */
    public void postAction(String action, Object[] arguments) {
        Collection<IActionable> actions = actionableMap.get(action);
        for (IActionable actionable : actions) {
            actionable.handleAction(arguments);
        }
    }
}
