package rahulshettyacademy.helpers;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.MimetypesFileTypeMap;

/**
 * The Class IOUtils.
 */
public class IOUtils {

    /**
     * The default buffer size to use for  {@link #copyLarge(InputStream, OutputStream)} and {@link #copyLarge(Reader, Writer)}.
     */
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    /**
     * the log
     */
    private static final Logger LOGGER = Logger.getLogger(IOUtils.class.getName());

    /**
     * Convert the specified string to an input stream, encoded as bytes
     * using the default character encoding of the platform.
     *
     * @param input the string to convert
     * @return an input stream
     * @since Commons IO 1.1
     */
    public static InputStream toInputStream(String input) {
        byte[] bytes = input.getBytes();
        return new ByteArrayInputStream(bytes);
    }

    /**
     * Convert the specified string to an input stream, encoded as bytes
     * using the specified character encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     *
     * @param input    the string to convert
     * @param encoding the encoding to use, null means platform default
     * @return an input stream
     * @throws IOException if the encoding is invalid
     * @since Commons IO 1.1
     */
    public static InputStream toInputStream(String input, String encoding) throws IOException {
        byte[] bytes = encoding != null ? input.getBytes(encoding) : input.getBytes();
        return new ByteArrayInputStream(bytes);
    }

    /**
     * Copy bytes from a large (over 2GB) <code>InputStream</code> to an
     * <code>OutputStream</code>.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream</code>.
     *
     * @param input  the <code>InputStream</code> to read from
     * @param output the <code>OutputStream</code> to write to
     * @return the number of bytes copied
     * @throws IOException if an I/O error occurs
     * @since Commons IO 1.3
     */
    public static long copyLarge(InputStream input, OutputStream output)
            throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    /**
     * Copy chars from a large (over 2GB) <code>Reader</code> to a <code>Writer</code>.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedReader</code>.
     *
     * @param input  the <code>Reader</code> to read from
     * @param output the <code>Writer</code> to write to
     * @return the number of characters copied
     * @throws IOException if an I/O error occurs
     * @since Commons IO 1.3
     */
    public static long copyLarge(Reader input, Writer output) throws IOException {
        char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

// read toByteArray
//-----------------------------------------------------------------------

    /**
     * Get the contents of an <code>InputStream</code> as a <code>byte[]</code>.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream</code>.
     *
     * @param input the <code>InputStream</code> to read from
     * @return the requested byte array
     * @throws IOException if an I/O error occurs
     */
    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copyLarge(input, output);
        return output.toByteArray();
    }

    /**
     * Get the contents of a <code>Reader</code> as a <code>byte[]</code>
     * using the default character encoding of the platform.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedReader</code>.
     *
     * @param input the <code>Reader</code> to read from
     * @return the requested byte array
     * @throws IOException if an I/O error occurs
     */
    public static byte[] toByteArray(Reader input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        OutputStreamWriter out = new OutputStreamWriter(output);
        copyLarge(input, out);
        out.flush();
        return output.toByteArray();
    }

    /**
     * Get the contents of a <code>Reader</code> as a <code>byte[]</code>
     * using the specified character encoding.
     * <p>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedReader</code>.
     *
     * @param input    the <code>Reader</code> to read from
     * @param encoding the encoding to use, null means platform default
     * @return the requested byte array
     * @throws IOException if an I/O error occurs
     * @since Commons IO 1.1
     */
    public static byte[] toByteArray(Reader input, String encoding)
            throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        OutputStreamWriter out = (encoding == null) ? new OutputStreamWriter(output) : new OutputStreamWriter(output, encoding);
        copyLarge(input, out);
        out.flush();
        return output.toByteArray();
    }

    /**
     * File2 string.
     *
     * @param f the f
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static String file2String(File f) throws IOException {
        FileInputStream inputStream = new FileInputStream(f);
        try {
            return new String(toByteArray(inputStream), "UTF8");
        } finally {
            inputStream.close();
        }
    }

    /**
     * Gets the file name.
     *
     * @param path the path
     * @return the file name
     */
    public static String getFileName(String path) {
        int lastIndexOf = path.replace('/', '\\').lastIndexOf("\\");
        if (lastIndexOf >= 0) {
            return path.substring(lastIndexOf + 1);
        }
        return path;
    }

    /**
     * Gets the mime type.
     *
     * @param fileName the file name
     * @return the mime type
     */
    public static String getMimeType(String fileName) {
        String mimetype = "application/octet-stream";
        try {
            String contentType = new MimetypesFileTypeMap().getContentType(fileName);
            if (contentType != null) {
                mimetype = contentType;
            }
        } catch (Exception e) {
            //swallow
        }
        return mimetype;
    }

    /**
     * find resource in classpath.
     *
     * @param resourceName
     * @return
     */
    public static InputStream findResourceInClasspath(final String resourceName) {
        return findResourceInClasspath(resourceName, null);
    }

    /**
     * find resource in classpath.
     *
     * @param resourceName
     * @param predicateToFilter filter resources first.
     * @return
     */
    public static InputStream findResourceInClasspath(final String resourceName,
                                                      final Predicate<URL> predicateToFilter) {

        Predicate<URL> filter = predicateToFilter != null ? predicateToFilter : (u -> true);
        InputStream openStream = null;
        List<URL> resources = Collections.emptyList();
        try {
            resources = Collections.list(Thread.currentThread().getContextClassLoader().getResources(resourceName));
        } catch (Exception e) {
            LOGGER.log(Level.FINEST, "Error looking to class path!", e);
        }
        if (resources.isEmpty()) {
            try {
                resources = Collections.list(IOUtils.class.getClassLoader().getResources(resourceName));
            } catch (Exception e) {
                LOGGER.log(Level.FINEST, "Error looking to class path!", e);
            }
        }
        URL url = resources.stream().filter(filter).findFirst().orElse(null);
        if (url != null) {
            try {
                return url.openStream();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error loading resorce:" + url.toExternalForm(), e);
            }
        }
        return null;
    }

    /**
     * get temp folder.
     *
     * @return
     */
    public static String getTmpFolder() {
        return System.getProperty("java.io.tmpdir");
    }

    /**
     * copy file.
     *
     * @param resourceName
     * @param targetFile
     * @throws IOException
     */
    public static void extractFromClassPathAndCopy(final String resourceName,
                                                   final File targetFile) throws IOException {
        InputStream resourceInClasspath = findResourceInClasspath(resourceName);
        if (resourceInClasspath != null) {
            OutputStream os = new BufferedOutputStream(new FileOutputStream(targetFile));
            try {
                copyLarge(resourceInClasspath, os);
            } finally {
                os.close();
            }
        }
    }
}