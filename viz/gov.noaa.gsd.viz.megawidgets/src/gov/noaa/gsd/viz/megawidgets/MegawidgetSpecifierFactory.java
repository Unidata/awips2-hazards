/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.megawidgets;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Megawidget specifier factory, used to create megawidget specifiers from maps
 * containing said specifiers' parameters.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Oct 23, 2013   2168     Chris.Golden      Changed to allow the creation
 *                                           of a specifier to include a
 *                                           restriction on what superclass
 *                                           is expected of which the result
 *                                           should be an instance.
 * Apr 24, 2014   2925     Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see MegawidgetSpecifier
 */
public class MegawidgetSpecifierFactory implements IMegawidgetSpecifierFactory {

    // Public Methods

    @SuppressWarnings("unchecked")
    @Override
    public <S extends ISpecifier> S createMegawidgetSpecifier(
            Class<S> superClass, Map<String, Object> parameters)
            throws MegawidgetSpecificationException {

        /*
         * Determine the name of the class of megawidget to be constructed.
         */
        Object specifierName = parameters
                .get(MegawidgetSpecifier.MEGAWIDGET_TYPE);
        if ((specifierName == null)
                || ((specifierName instanceof String) && ((String) specifierName)
                        .isEmpty())) {
            throw new MegawidgetSpecificationException(
                    getIdentifierForException(parameters), null,
                    MegawidgetSpecifier.MEGAWIDGET_TYPE, null, null);
        }
        if ((specifierName instanceof String) == false) {
            throw new MegawidgetSpecificationException(
                    getIdentifierForException(parameters), null,
                    MegawidgetSpecifier.MEGAWIDGET_TYPE, specifierName,
                    "must be string");
        }
        String className = ((String) specifierName) + "Specifier";

        /*
         * Get the fully qualified class path and name.
         */
        String classPackage = (String) parameters
                .get(MegawidgetSpecifier.MEGAWIDGET_CLASS_PACKAGE);
        String classPathAndName = (classPackage == null ? MegawidgetSpecifierFactory.class
                .getPackage().getName() : classPackage)
                + "." + className;

        /*
         * Get the class.
         */
        Class<?> specifierClass = null;
        try {
            specifierClass = Class.forName(classPathAndName);
        } catch (Exception e) {
            throw new MegawidgetSpecificationException(
                    getIdentifierForException(parameters), null,
                    MegawidgetSpecifier.MEGAWIDGET_TYPE, specifierName,
                    "not a valid megawidget (cannot find class "
                            + classPathAndName + ")");
        }

        /*
         * If the class is not a subclass of MegawidgetSpecifier, complain.
         */
        if (superClass.isAssignableFrom(specifierClass) == false) {
            throw new MegawidgetSpecificationException(
                    getIdentifierForException(parameters),
                    (String) specifierName, null, null,
                    "not a valid megawidget specifier (class " + specifierClass
                            + " is not a subclass of " + superClass + ")");
        }

        /*
         * If a factory is not specified in the parameters, put this instance in
         * as the factory, so that any megawidget specifiers that need to
         * themselves construct child megawidget specifiers will be able to do
         * so.
         */
        if (parameters.get(IContainerSpecifier.MEGAWIDGET_SPECIFIER_FACTORY) == null) {
            parameters.put(IContainerSpecifier.MEGAWIDGET_SPECIFIER_FACTORY,
                    this);
        }

        /* Construct an instance of the class using the passed-in parameters. */
        Class<?>[] constructorArgTypes = { Map.class };
        Object[] constructorArgValues = { parameters };
        S megawidgetSpecifier = null;
        try {
            megawidgetSpecifier = (S) specifierClass.getConstructor(
                    constructorArgTypes).newInstance(constructorArgValues);
        } catch (Throwable e) {
            if (e instanceof NoSuchMethodException) {
                throw new MegawidgetSpecificationException(
                        getIdentifierForException(parameters),
                        (String) specifierName, null, null,
                        "not a valid megawidget (class " + specifierClass
                                + " does not have a constructor taking "
                                + parameters.getClass() + " as an argument)", e);
            } else if (e instanceof IllegalAccessException) {
                throw new MegawidgetSpecificationException(
                        getIdentifierForException(parameters),
                        (String) specifierName,
                        null,
                        null,
                        "not a valid megawidget specifier (class "
                                + specifierClass
                                + " is abstract and thus cannot be instantiated)",
                        e);
            } else if (e instanceof IllegalArgumentException) {
                throw new MegawidgetSpecificationException(
                        getIdentifierForException(parameters),
                        (String) specifierName, null, null,
                        "unexpected illegal argument error", e);
            } else if (e instanceof InstantiationException) {
                throw new MegawidgetSpecificationException(
                        getIdentifierForException(parameters),
                        (String) specifierName, null, null,
                        "not a valid megawidget (class " + specifierClass
                                + " constructor taking "
                                + parameters.getClass()
                                + " as an argument is inaccessible)", e);
            } else if (e instanceof InvocationTargetException) {
                Throwable cause = e.getCause();
                if (cause instanceof MegawidgetSpecificationException) {
                    throw (MegawidgetSpecificationException) cause;
                } else {
                    throw new MegawidgetSpecificationException(
                            getIdentifierForException(parameters),
                            (String) specifierName, null, null,
                            "unexpected constructor error", cause);
                }
            } else if (e instanceof ExceptionInInitializerError) {
                throw new MegawidgetSpecificationException(
                        getIdentifierForException(parameters),
                        (String) specifierName, null, null,
                        "unexpected static initializer error", e);
            }
        }

        /*
         * Return the result.
         */
        return megawidgetSpecifier;
    }

    // Private Methods

    /**
     * Get the specifier identifier from the given parameters map for the
     * purposes of creating an exception to indicate an error.
     * 
     * @return Identifier, or <code>null</code> if the map contains none.
     */
    private String getIdentifierForException(Map<String, Object> parameters) {
        try {
            return (String) parameters
                    .get(MegawidgetSpecifier.MEGAWIDGET_IDENTIFIER);
        } catch (Exception e) {
            return null;
        }
    }
}
