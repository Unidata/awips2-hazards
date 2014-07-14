package gov.noaa.gsd.common.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;

/**
 * Description: General class of static utilities.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 04, 2013            daniel.s.schaffer Initial creation
 * Nov 25, 2013    2336    Chris.Golden      Moved to gov.noaa.gsd.common.utilities,
 *                                           and removed unused methods and variables.
 * Apr 09, 2014    2925    Chris.Golden      Fixed typo in method name.
 * </pre>
 * 
 * @author daniel.s.schaffer
 * @version 1.0
 */
public class Utils {

    /**
     * The string S with all sequences of adjacent blanks replaced by a single
     * blank. Thus, squeeze("This  is     a test") is "This is a test".
     */
    public static String squeeze(String s) {
        return s.replaceAll("\\s+", " ");
    }

    public static String removeBlanks(String s) {
        return s.replaceAll("\\s+", "");
    }

    /**
     * Reads a text file from the local/network file system and returns it as a
     * String
     * 
     * @param filepath
     *            String
     * @return String
     * @throws IOException
     */
    public static String textFileAsString(String filepath) {
        return textFileAsString(filepath, false);
    }

    /**
     * Reads a text file from the local/network file system and returns it as a
     * String
     * 
     * @param filepath
     *            String
     * @return String
     * @throws IOException
     */
    public static String textFileAsString(String filepath,
            boolean retainNewlines) {

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filepath));
            String s = readBufferedTextReader(reader, retainNewlines);
            return s;
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            runtimeWrap(e);
            return null;
        }

    }

    public static String textFileAsString(File file) {

        return textFileAsString(file.getPath());

    }

    /**
     * Reads a text from a buffered reader and returns it as a String
     * 
     * @param reader
     *            BufferedReader
     * @return String
     * @throws IOException
     */
    public static String readBufferedTextReader(BufferedReader reader,
            boolean retainNewlines) throws IOException {
        StringBuilder text = new StringBuilder();

        try {
            String s = null;
            while ((s = reader.readLine()) != null) {
                text.append(s);
                if (retainNewlines) {
                    text.append("\n");
                }
            }
            if (retainNewlines && text.length() > 0) {
                text.setLength(text.length() - 1);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        return (text.toString());
    }

    /**
     * Returns the directory from a filepath. For example, if filepath =
     * "/p10/somedir/run.ksh" this method returns "/p10/somedir"
     * 
     * @param filepath
     *            String
     * @return String
     */
    public static String dirName(String filepath) {
        int index;
        for (index = filepath.lastIndexOf(File.separator); index > 0; index--) {
            if ((filepath.charAt(index) == File.separatorChar)) {
                break;
            }
        }
        String dir = filepath.substring(0, index);
        return (dir);
    }

    public static String hostName() {
        try {
            InetAddress localMachine = InetAddress.getLocalHost();
            return localMachine.getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void runtimeWrap(Throwable e) {
        throw new RuntimeException("Unexpected exception: " + e.getMessage(), e);
    }

    public static String stackTraceAsString(Throwable e) {
        StringBuilder sb = new StringBuilder();
        StackTraceElement[] stackTrace = e.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            sb.append(stackTraceElement.toString() + "\n");
        }
        return sb.toString();
    }

    /**
     * Determine whether or not the specified value is an instance of at least
     * one of the specified classes or interfaces.
     * 
     * @param value
     *            Value to have its class checked.
     * @param classes
     *            Set of classes, at least one of which which the value must
     *            implement.
     * @return True if the value is an instance of at least one of the classes,
     *         false otherwise.
     */
    public static boolean isValueInstanceOfAtLeastOneClass(Object value,
            Collection<Class<?>> classes) {
        for (Class<?> valueClass : classes) {
            if (valueClass.isAssignableFrom(value.getClass())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the number of generations between the specified subclass or
     * subinterface and superclass or superinterface. It is assumed that the
     * latter is indeed assignable from the former.
     * 
     * @param ancestor
     *            Superclass or superinterface.
     * @param descendant
     *            Subclass or subinterface.
     * @return Number of generations (1 or more) between the classes.
     * @throws IllegalArgumentException
     *             If the supposed descendant does not actually have the
     *             supposed ancestor as a superclass/superinterface.
     */
    public static int getGenerationsBetweenClasses(Class<?> ancestor,
            Class<?> descendant) {

        // If both are classes, simply iterate through the gener-
        // ations separating them, counting said generations.
        // Otherwise, iterate through the interfaces implemented
        // by the descendant and its ancestor classes, and re-
        // cursively through the interfaces extended by those
        // those interfaces, finding the shortest generational
        // path between the ancestor and the descendant.
        if (!ancestor.isInterface() && !descendant.isInterface()) {
            int generationDelta = 0;
            for (Class<?> aClass = descendant; aClass != null; aClass = aClass
                    .getSuperclass(), generationDelta++) {
                if (aClass.equals(ancestor)) {
                    return generationDelta;
                }
            }
        } else {
            int generationDelta = 0;
            int smallestGenerationDelta = -1;
            for (Class<?> aClass = descendant; aClass != null; aClass = aClass
                    .getSuperclass(), generationDelta++) {
                if (aClass.equals(ancestor)
                        || (smallestGenerationDelta == generationDelta)) {
                    return generationDelta;
                }
                for (Class<?> superInterface : aClass.getInterfaces()) {
                    if (ancestor.isAssignableFrom(superInterface)) {
                        int thisGenerationDelta = generationDelta
                                + 1
                                + getGenerationsBetweenClasses(ancestor,
                                        superInterface);
                        if (thisGenerationDelta == generationDelta) {
                            return generationDelta;
                        } else if ((smallestGenerationDelta == -1)
                                || (thisGenerationDelta < smallestGenerationDelta)) {
                            smallestGenerationDelta = thisGenerationDelta;
                        }
                    }
                }

            }
            if (smallestGenerationDelta > -1) {
                return smallestGenerationDelta;
            }
        }

        // If no familial relationship between the two was found,
        // they are not related.
        throw new IllegalArgumentException(descendant
                + " is not a descendant of " + ancestor);
    }

    @SuppressWarnings("unchecked")
    public static <T> T listAsArray(List<?> list) {
        try {
            T r = (T) (T[]) Array.newInstance(list.get(0).getClass(),
                    list.size());
            Object[] result = (Object[]) r;
            for (int i = 0; i < list.size(); i++) {
                result[i] = list.get(i);
            }
            return (T) result;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new RuntimeException("Cannot convert and empty list");
        }
    }

}
