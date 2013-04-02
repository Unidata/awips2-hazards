package gov.noaa.gsd.common.utilities;

import static com.google.common.collect.Lists.*;
import static org.joda.time.DateTimeConstants.*;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Description: General class of static utilities.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 04, 2013            daniel.s.schaffer      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer
 * @version 1.0
 */
public class Utils {

    private static final String ZIP_SUFFIX = ".gz";

    private static Joiner joiner = Joiner.on("");

    private static Joiner spaceJoiner = Joiner.on(" ");

    private static Joiner directoryJoiner = Joiner.on(File.separator);

    private static Map<String, Class<?>> primitiveTypeMap;

    private static DateTimes dateTimes = new DateTimes();

    static {
        primitiveTypeMap = Maps.newHashMap();
        primitiveTypeMap.put(int.class.getName(), int.class);
        primitiveTypeMap.put(float.class.getName(), float.class);
        primitiveTypeMap.put(double.class.getName(), double.class);
        primitiveTypeMap.put(long.class.getName(), long.class);
        primitiveTypeMap.put(short.class.getName(), short.class);
        primitiveTypeMap.put(boolean.class.getName(), boolean.class);
        primitiveTypeMap.put(char.class.getName(), char.class);
    }

    public Utils() {
    }

    public static Class<?> primitiveTypeNameToClass(String typeName) {
        try {
            return primitiveTypeMap.get(typeName);
        } catch (NullPointerException e) {
            runtimeWrap(e, "Unsupported primitive type name " + typeName);
            return null;
        }
    }

    public static boolean fileExists(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

    public static boolean filesEqual(String file1Path, String file2Path) {
        File file1 = new File(file1Path);
        File file2 = new File(file2Path);
        return filesEqual(file1, file2);
    }

    public static boolean filesEqual(File file1, File file2) {
        try {
            InputStream stream1 = new FileInputStream(file1);
            InputStream stream2 = new FileInputStream(file2);
            return streamsEqual(stream1, stream2);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(String.format(
                    "Unexpected error comparing files %s, %s", file1.getPath(),
                    file2.getPath()), e);
        } catch (IOException e) {
            throw new RuntimeException(String.format(
                    "Unexpected error comparing files %s, %s", file1.getPath(),
                    file2.getPath()), e);
        }
    }

    /**
     * <p>
     * Efficient method for determining if the contents of two streams are
     * equal.
     * </p>
     * 
     * @param stream1
     *            InputStream
     * @param stream2
     *            InputStream
     * @return boolean
     */
    public static boolean streamsEqual(InputStream stream1, InputStream stream2)
            throws IOException {
        final int BUFFER_SIZE = 1000;
        byte[] bytes1 = new byte[BUFFER_SIZE];
        byte[] bytes2 = new byte[BUFFER_SIZE];
        ByteBuffer resultBuffer;
        ByteBuffer correctAnswerBuffer;

        for (int bytesRead; (bytesRead = stream1.read(bytes1, 0, BUFFER_SIZE)) >= 0;) {
            stream2.read(bytes2, 0, BUFFER_SIZE);
            resultBuffer = ByteBuffer.wrap(bytes1, 0, bytesRead);
            correctAnswerBuffer = ByteBuffer.wrap(bytes2, 0, bytesRead);
            if (resultBuffer.compareTo(correctAnswerBuffer) != 0) {
                return false;
            }
        }
        return true;

    }

    public static boolean equalsIgnoring(String s1, String s2,
            String... ignoreStrings) {
        return filter(s1, ignoreStrings).equals(filter(s2, ignoreStrings));
    }

    private static String filter(String string, String... filterStrings) {
        String result = string;
        for (String string2 : filterStrings) {
            result = result.replaceAll(string2, "");
        }
        return result;
    }

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
     * Constructs a directory name by appending the "Data" to the class name
     * 
     * @param c
     *            The class.
     * @return String
     */
    public static String dataDirName(Class<?> c) {
        return c.getSimpleName() + "Data/";
    }

    /**
     * Replaces all chars in the given string with the given replacement char.
     * Unlike String.replaceAll(), this method works with any characters (not
     * just regular expressions).
     * 
     * @param s
     *            String
     * @param ch
     *            char
     * @param replacementCh
     *            char
     * @return String
     */
    public static String replaceAllChars(String s, char ch, char replacementCh) {
        StringBuffer sb = new StringBuffer(s);
        for (int i = 0; i < sb.length(); i++) {
            if (sb.charAt(i) == ch) {
                sb.setCharAt(i, replacementCh);
            }
        }
        return (sb.toString());
    }

    public static String dotToFileNotation(String s) {
        return replaceAllChars(s, '.', File.separatorChar);
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

    public static String textFilePathAsString(String filePath) {
        File file = new File(filePath);
        return textFileAsString(file.getPath());

    }

    public static String[] textFilesAsLines(String filePath) throws IOException {
        return textFileAsString(filePath).split("\n");
    }

    public static List<String> textFilesAsStringList(String filePath)
            throws IOException {
        return Arrays.asList(textFilesAsLines(filePath));
    }

    /**
     * Reads text from a stream and returns it as a String
     * 
     * @param stream
     *            InputStream
     * @return String
     * @throws IOException
     */
    public static String readTextStream(InputStream stream) throws IOException {
        return readTextStream(stream, false);
    }

    /**
     * Reads text from a stream and returns it as a String
     * 
     * @param stream
     *            InputStream
     * @return String
     * @throws IOException
     */
    public static String readTextStream(InputStream stream,
            boolean retainNewlines) throws IOException {

        InputStreamReader isr = new InputStreamReader(stream);
        BufferedReader reader = new BufferedReader(isr);
        return readBufferedTextReader(reader, retainNewlines);
    }

    /**
     * Reads a text from a buffered reader and returns it as a String
     * 
     * @param reader
     *            BufferedReader
     * @return String
     * @throws IOException
     */
    public static String readBufferedTextReader(BufferedReader reader)
            throws IOException {
        return readBufferedTextReader(reader, false);
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
     * Writes a text file to the local/network file system
     * 
     * @param text
     *            String
     * @param filepath
     *            String
     * @throws IOException
     */
    public static void writeTextFile(String text, String filepath) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filepath));
            try {
                writer.write(text);
                writer.flush();
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        } catch (IOException e) {
            runtimeWrap(e);
        }
    }

    public static float max(float[] t) {
        assert t.length > 0;
        float maximum = t[0]; // start with the first value
        for (int i = 1; i < t.length; i++) {
            if (t[i] > maximum) {
                maximum = t[i]; // new maximum
            }
        }
        return maximum;
    }

    public static float min(float[] t) {
        assert t.length > 0;
        float minimum = t[0]; // start with the first value
        for (int i = 1; i < t.length; i++) {
            if (t[i] < minimum) {
                minimum = t[i]; // new minimum
            }
        }
        return minimum;
    }

    public static void validateString(String value, boolean allowEmpty) {
        if (value == null) {
            throw new NullPointerException("Null string value not allowed.");
        }
        if (!allowEmpty && value.length() == 0) {
            throw new IllegalArgumentException(
                    "Empty string value not allowed.");
        }
    }

    private static Runtime rt = Runtime.getRuntime();

    public static String shellCommand(String command) {
        try {
            Process p = rt.exec(command);
            int status = p.waitFor();
            if (status != 0) {
                throw new RuntimeException(String.format(
                        "Shell command failed with status %d.  Command is\n%s",
                        status, command));
            }
            return readTextStream(p.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("Unexpected exception running command ",
                    e);
        } catch (InterruptedException e) {
            throw new RuntimeException("Unexpected exception running command ",
                    e);
        }

    }

    public static String listTarFile(String tarPath) {
        String command = spaceJoin("tar tvf", tarPath);
        return shellCommand(command);
    }

    public static String listTarFile(File tarFile) {
        return listTarFile(tarFile.getAbsolutePath());
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

    /**
     * Returns the filename from a full filepath. For example, filepath =
     * "/p10/somedir/run.ksh" this method returns "run.ksh"
     * 
     * @param filepath
     *            String
     * @return String
     */
    public static String baseName(String filepath) {
        int index;
        for (index = filepath.length() - 1; index > 0; index--) {
            if ((filepath.charAt(index) == File.separatorChar)) {
                break;
            }
        }
        String filename = filepath.substring(index + 1);
        return (filename);
    }

    /**
     * <p>
     * Makes directory (and any parent directories) for the given filepath
     * </p>
     * 
     * @param filepath
     * @return boolean
     */
    public static boolean mkDirs(String filepath) {
        File dir = new File(filepath);

        dir.mkdirs();

        /**
         * Need to return based on exists because mkdirs will return false if it
         * tried to make a directory that already existed.
         */
        return dir.exists();
    }

    /**
     * <p>
     * Removes the directory and all contents.
     * </p>
     * 
     * @param filepath
     */
    public static String rmDirs(String filepath) {
        return shellCommand("rm -rf " + filepath);
    }

    /**
     * <p>
     * Removes the given file
     * </p>
     * 
     * @param filepath
     */
    public static String rmFile(String filepath) {
        return shellCommand("rm -f " + filepath);
    }

    /**
     * @param in
     * @param out
     * @throws IOException
     */
    public static void gunzip(InputStream in, OutputStream out) {
        try {
            byte[] buffer = new byte[8192];
            in = new GZIPInputStream(in, buffer.length);
            int count = in.read(buffer);
            while (count > 0) {
                out.write(buffer, 0, count);
                count = in.read(buffer);
            }
        } catch (IOException e) {
            runtimeWrap(e);
        }
    }

    public static void redirect(InputStream in, OutputStream out) {
        try {
            byte[] buffer = new byte[8192];
            int count = 0;
            while ((count = in.read(buffer)) >= 0) {
                out.write(buffer, 0, count);
            }
            in.close();
            out.flush();

        } catch (IOException e) {
            runtimeWrap(e);
        }
    }

    public static byte[] toByteArray(InputStream inputStream) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            redirect(inputStream, outputStream);
            byte[] result = outputStream.toByteArray();
            outputStream.close();
            return result;
        } catch (IOException e) {
            runtimeWrap(e);
            return null;
        }
    }

    public static byte[] toByteArray(String filePath) {
        try {
            FileInputStream inputStream = asInputStream(filePath);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            redirect(inputStream, outputStream);
            byte[] result = outputStream.toByteArray();
            outputStream.close();
            return result;
        } catch (IOException e) {
            runtimeWrap(e);
            return null;
        }
    }

    /**
     * @param in
     * @param out
     * @throws IOException
     */
    public static void gzip(InputStream in, OutputStream out) {
        try {
            byte[] buffer = new byte[8192];
            out = new GZIPOutputStream(out, buffer.length);
            int count = in.read(buffer);
            while (count > 0) {
                out.write(buffer, 0, count);
                count = in.read(buffer);
            }
            ((GZIPOutputStream) out).finish();
        } catch (IOException e) {
            runtimeWrap(e);
        }
    }

    public static void gzip(File inFile, File outFile) {
        try {
            InputStream inStream = asInputStream(inFile);
            OutputStream outStream = asOutputStream(outFile);
            gzip(inStream, outStream);
            inStream.close();
            outStream.close();
        } catch (IOException e) {
            runtimeWrap(e);
        }
    }

    public static void gzip(String inFileName, String outFileName) {
        try {
            InputStream inStream = asInputStream(inFileName);
            OutputStream outStream = asOutputStream(outFileName);
            gzip(inStream, outStream);
            inStream.close();
            outStream.close();
        } catch (IOException e) {
            runtimeWrap(e);
        }
    }

    public static void cp(String f1Name, String f2Name) {
        cp(new File(f1Name), new File(f2Name));
    }

    public static void cp(File sourceFile, File destFile) {
        try {
            if (!destFile.exists()) {
                destFile.createNewFile();
            }

            FileChannel source = null;
            FileChannel destination = null;
            try {
                source = new FileInputStream(sourceFile).getChannel();
                destination = new FileOutputStream(destFile).getChannel();
                destination.transferFrom(source, 0, source.size());
            } finally {
                if (source != null) {
                    source.close();
                }
                if (destination != null) {
                    destination.close();
                }
            }
        } catch (FileNotFoundException e) {
            runtimeWrap(e);
        } catch (IOException e) {
            runtimeWrap(e);
        }

    }

    @SuppressWarnings("unchecked")
    /*
     * Transposes a 2-D array. "Rows" must all be of same length
     * 
     * @param input 2-D array to be transposed
     * 
     * @returns 2-D array representing the transpose
     * 
     * @throws IllegalArgumentException if "rows" are not of same length
     */
    public static <T> T[][] transposeArray(T[][] original) {
        int inDim0 = original.length;
        int inDim1 = original[0].length;

        for (int i = 1; i < inDim0; i++) {
            if (original[i].length != inDim1) {
                throw new IllegalArgumentException(
                        "Rows must all be of same length.");
            }
        }

        T[][] transpose = (T[][]) java.lang.reflect.Array.newInstance(
                original[0].getClass().getComponentType(), new int[] { inDim1,
                        inDim0 });

        for (int i = 0; i < inDim0; i++) {
            for (int j = 0; j < inDim1; j++) {
                transpose[j][i] = original[i][j];
            }
        }

        return transpose;
    }

    public static int resultSetSize(ResultSet resultSet) {
        int numRows = 0;
        try {
            resultSet.beforeFirst();
            while (resultSet.next()) {
                numRows += 1;

            }
            return numRows;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Unexpected exception determining ResultSet size ", e);
        }
    }

    public static <E> List<E> asUnmodifiableList(Iterator<? extends E> elements) {
        return Collections.unmodifiableList(newArrayList(elements));
    }

    public static <E> List<E> asUnmodifiableList(Iterable<? extends E> elements) {
        return Collections.unmodifiableList(newArrayList(elements));
    }

    public static boolean is64BitArchitecture() {
        try {
            String command = "uname -m";
            Process proc = Runtime.getRuntime().exec(command);
            BufferedInputStream buffer = new BufferedInputStream(
                    proc.getInputStream());
            BufferedReader commandOutput = new BufferedReader(
                    new InputStreamReader(buffer));
            String line = commandOutput.readLine();
            return line.contains("_64");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean containsAll(List<?> values, List<?> otherValues) {
        for (int i = 0; i < values.size(); i++) {
            if (!otherValues.contains(values.get(i))) {
                return false;
            }
        }
        return true;
    }

    public static String getClassPath() {
        // Get the System Classloader
        ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();

        // Get the URLs
        URL[] urls = ((URLClassLoader) sysClassLoader).getURLs();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < urls.length; i++) {
            sb.append(urls[i].getFile());
        }
        return sb.toString();
    }

    public static List<String> namesOfFilesInDirectory(String dirName) {
        File dir = new File(dirName);
        failIfNotDirectory(dir);
        return Arrays.asList(dir.list());
    }

    public static List<String> namesOfSubDirectories(String dirName) {
        File dir = new File(dirName);
        return namesOfSubDirectories(dir);
    }

    public static List<String> namesOfSubDirectories(File dir) {
        failIfNotDirectory(dir);
        List<String> result = Lists.newArrayList();
        List<String> allFilePaths = pathsOfFilesInDirectory(dir
                .getAbsolutePath());
        for (String filePath : allFilePaths) {
            if (new File(filePath).isDirectory()) {
                result.add(baseName(filePath));
            }
        }
        return result;
    }

    public static List<String> pathsOfFilesInDirectory(String dirName) {
        File dir = new File(dirName);
        failIfNotDirectory(dir);
        File[] files = dir.listFiles();
        List<String> paths = Lists.newArrayList();
        for (File file : files) {
            paths.add(file.getAbsolutePath());
        }
        return paths;
    }

    public static String join(List<?> list) {
        return joiner.join(list);
    }

    public static String join(Object... items) {
        return join(Arrays.asList(items));

    }

    public static String join(String joinString, List<String> stringList) {
        Joiner joiner = Joiner.on(",");
        return joiner.join(stringList);
    }

    public static String join(String joinString, String[] stringArray) {
        Joiner joiner = Joiner.on(",");
        return joiner.join(stringArray);
    }

    public static String spaceJoin(List<?> list) {
        return spaceJoiner.join(list);

    }

    public static String spaceJoin(Object... items) {
        return spaceJoin(Arrays.asList(items));

    }

    public static String directoryJoin(Object... items) {
        return directoryJoin(Arrays.asList(items));

    }

    public static String directoryJoin(List<?> list) {
        return directoryJoiner.join(list);

    }

    public static List<String> namesOfFilesNotStartingWith(String dirName,
            String startString) {
        List<String> result = Lists.newArrayList();
        for (File file : filesNotStartingWith(dirName, startString)) {
            result.add(file.getName());
        }
        return result;
    }

    public static List<File> filesNotStartingWith(String dirName,
            final String prefix) {
        File dir = new File(dirName);
        failIfNotDirectory(dir);
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return !name.startsWith(prefix);
            }
        };
        return Arrays.asList(dir.listFiles(filter));
    }

    public static List<String> namesOfFilesEndingWith(String dirName,
            String suffix) {
        List<String> result = Lists.newArrayList();
        for (File file : filesEndingWith(dirName, suffix)) {
            result.add(file.getName());
        }
        return result;
    }

    public static File file(String fileName) {
        return new File(fileName);
    }

    public static boolean zipped(File file) {
        return file.getName().endsWith(ZIP_SUFFIX);
    }

    public static List<File> files(String dirName) {
        File dir = new File(dirName);
        failIfNotDirectory(dir);
        return Arrays.asList(dir.listFiles());
    }

    public static List<File> filesEndingWith(String dirName, final String suffix) {
        File dir = new File(dirName);
        failIfNotDirectory(dir);
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(suffix);
            }
        };
        return Arrays.asList(dir.listFiles(filter));
    }

    private static void failIfNotDirectory(File dir) {
        if (!dir.isDirectory()) {
            throw new RuntimeException(String.format("%s is not a directory",
                    dir.getName()));
        }
    }

    /**
     * This filter only returns directories
     */
    public static List<File> subdirectories(String dirName) {
        File dir = new File(dirName);
        return subdirectories(dir);
    }

    public static List<File> subdirectories(File directory) {
        failIfNotDirectory(directory);
        FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        };
        return Arrays.asList(directory.listFiles(fileFilter));
    }

    public static FileInputStream asInputStream(String fileName) {
        try {
            return new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            runtimeWrap(e);
            return null;
        }
    }

    public static FileOutputStream asOutputStream(String fileName) {
        try {
            return new FileOutputStream(fileName);
        } catch (FileNotFoundException e) {
            runtimeWrap(e);
            return null;
        }
    }

    public static FileInputStream asInputStream(File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            runtimeWrap(e);
            return null;
        }
    }

    public static FileOutputStream asOutputStream(File file) {
        try {
            return new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            runtimeWrap(e);
            return null;
        }
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

    public static String userName() {
        return System.getProperty("user.name");
    }

    public static void runtimeWrap(Throwable e) {
        throw new RuntimeException("Unexpected exception: " + e.getMessage(), e);
    }

    public static void runtimeWrap(Exception e, String msg) {
        throw new RuntimeException(msg, e);
    }

    public static int pid() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        int endPidIndex = name.indexOf("@") - 1;
        int result = Integer.parseInt(name.substring(0, endPidIndex));
        return result;
    }

    public static DateTime truncateToHour(DateTime dt) {
        return new DateTime(dt.getYear(), dt.getMonthOfYear(),
                dt.getDayOfMonth(), dt.getHourOfDay(), 0, 0, 0);
    }

    public static File createTempFile(String prefix, String suffix) {
        try {
            File temp = File.createTempFile(prefix, suffix);
            temp.deleteOnExit();
            return temp;
        } catch (IOException e) {
            runtimeWrap(e);
            return null;
        }

    }

    public static File createTempFile(String directoryPath, String prefix,
            String suffix) {
        try {
            File temp = File.createTempFile(prefix, suffix, new File(
                    directoryPath));
            temp.deleteOnExit();
            return temp;
        } catch (IOException e) {
            runtimeWrap(e);
            return null;
        }

    }

    public static URL createTempURL(String prefix, String suffix) {
        return urlFromFile(createTempFile(prefix, suffix));
    }

    public static String createTempPath(String prefix, String suffix) {
        return createTempFile(prefix, suffix).getPath();
    }

    public static URL urlFromFile(File file) {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            runtimeWrap(e);
            return null;
        }
    }

    public static URL urlFromFilepath(String filePath) {
        return urlFromFile(new File(filePath));

    }

    public static void sleep(int numSeconds) {
        try {
            Thread.sleep(numSeconds * MILLIS_PER_SECOND);
        } catch (InterruptedException e) {
            runtimeWrap(e);
        }

    }

    public static void deleteDirectory(File directoryFile) {
        deleteDirectory(directoryFile.getPath());

    }

    public static void deleteDirectory(String directoryPath) {
        shellCommand(String.format("rm -rf %s", directoryPath));
    }

    public static List<Object> asObjectList(Object... objects) {
        return Arrays.asList(objects);
    }

    public static String replaceLast(String string, String regex,
            String replacement) {
        validate(string);
        int last = string.length();
        int index = last - 1;
        while (index > 0) {
            if (string.substring(index, last).equals(regex)) {
                return string.substring(0, index) + replacement;
            }
            index -= 1;
        }
        throw new IllegalArgumentException("Could not find expression " + regex);
    }

    public static String replaceLastOccurrence(String string,
            Character oldChar, Character newChar) {
        validate(string);
        validate(oldChar);
        int index = string.lastIndexOf(oldChar);
        if (index != -1) {
            String result = string.substring(0, index) + newChar
                    + string.substring(index + 1);
            return result;
        } else {
            return string;
        }
    }

    public static String replaceFirstOccurrence(String string,
            Character oldChar, Character newChar) {
        validate(string);
        validate(oldChar);
        int index = string.indexOf(oldChar);
        if (index != -1) {
            String result = string.substring(0, index) + newChar
                    + string.substring(index + 1);
            return result;
        } else {
            return string;
        }
    }

    private static void validate(Character oldChar) {
        if (oldChar == null) {
            throw new IllegalArgumentException("Invalid string");
        }

    }

    private static void validate(String string) {
        if (string == null || string.length() == 0) {
            throw new IllegalArgumentException("Invalid string");
        }
    }

    public static String stackTraceAsString(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.getMessage() + "\n");
        StackTraceElement[] stackTrace = e.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            sb.append(stackTraceElement.toString() + "\n");
        }
        return sb.toString();
    }

    public static Class<?> forName(String name) throws ClassNotFoundException {
        Class<?> result = primitiveTypeMap.get(name);
        if (result == null) {
            result = Class.forName(name);
        }
        return result;

    }

    public static String listAsString(List<?> values) {
        StringBuilder sb = new StringBuilder();
        for (Object value : values) {
            sb.append(value);
            sb.append(" ");
        }
        return sb.toString();

    }

    public static DateMidnight dateTimeStringAsDate(String pattern,
            String dateTimeAsString) {
        DateTime dateTime = dateTimes.parse(pattern, dateTimeAsString);
        DateMidnight result = dateTimes.newDateMidnight(dateTime);
        return result;
    }

    public static LocalTime dateTimeStringAsLocalTime(String pattern,
            String dateTimeAsString) {
        DateTime dateTime = dateTimes.parse(pattern, dateTimeAsString);
        LocalTime result = dateTimes.newLocalTime(dateTime);
        return result;
    }

    public static List<Float> stringsToFloats(List<String> strings) {
        List<Float> result = Lists.newArrayList();
        for (String string : strings) {
            result.add(Float.parseFloat(string));
        }
        return result;
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
