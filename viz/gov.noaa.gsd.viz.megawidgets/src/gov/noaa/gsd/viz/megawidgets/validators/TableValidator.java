/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.megawidgets.validators;

import gov.noaa.gsd.viz.megawidgets.MegawidgetException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecificationException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetStateException;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: Validator used to validate table state, which is a list of
 * lists, each of the latter holding one or more objects.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Apr 03, 2015   4162     Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class TableValidator extends SingleStateValidator<List<List<Object>>> {

    // Public Constructors

    /**
     * Construct an uninitialized instance.
     */
    public TableValidator() {
    }

    // Protected Constructors

    /**
     * Construct an instance that is a copy of another already-initialized
     * instance.
     * 
     * @param other
     *            Validator to be copied.
     * @throws IllegalArgumentException
     *             If <code>other</code> has not yet been initialized.
     */
    protected TableValidator(TableValidator other) {
        super(other);
    }

    // Public Methods

    @SuppressWarnings("unchecked")
    @Override
    public <V extends StateValidator> V copyOf() {
        return (V) new TableValidator(this);
    }

    @Override
    public List<List<Object>> convertToStateValue(Object object)
            throws MegawidgetException {
        List<?> listObject = (object instanceof List ? (List<?>) object : null);
        if ((listObject != null) && (listObject.isEmpty() == false)) {
            boolean success = true;
            List<List<Object>> list = new ArrayList<List<Object>>(
                    listObject.size());
            for (Object itemObject : listObject) {
                if ((itemObject instanceof List) == false) {
                    success = false;
                    break;
                }
                List<?> sublistObject = (List<?>) itemObject;
                List<Object> sublist = new ArrayList<>(sublistObject.size());
                for (Object element : sublistObject) {
                    sublist.add(element);
                }
                list.add(sublist);
            }
            if (success) {
                return list;
            }
        }
        throw new MegawidgetStateException(getIdentifier(), getType(), object,
                "must be list of one or more non-empty lists");
    }

    // Protected Methods

    @Override
    protected void doInitialize() throws MegawidgetSpecificationException {

        /*
         * No action.
         */
    }
}
