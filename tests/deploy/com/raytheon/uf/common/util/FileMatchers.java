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
package com.raytheon.uf.common.util;

import java.io.File;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Ignore;

/**
 * File matchers for JUnit/Hamcrest, intentionally package-private.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 01, 2013 1543       djohnson     Initial creation
 * 
 * </pre>
 * 
 * @author djohnson
 * @version 1.0
 */
@Ignore
final class FileMatchers {
    /**
     * Prevent construction.
     */
    private FileMatchers() {
    }

    static class DirectoryNumberOfFilesMatcher extends
            TypeSafeMatcher<File> {

        private final int expectedNumberOfFiles;

        private int actualNumberOfFiles;

        /**
         * @param expectedNumberOfFiles
         */
        DirectoryNumberOfFilesMatcher(int numberOfFiles) {
            this.expectedNumberOfFiles = numberOfFiles;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void describeTo(Description description) {
            description.appendText("a directory with ");
            description.appendValue(expectedNumberOfFiles);
            description.appendText((expectedNumberOfFiles == 1) ? " file"
                    : " files");
            description.appendText(" not ");
            description.appendValue(actualNumberOfFiles);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean matchesSafely(File item) {
            actualNumberOfFiles = item.listFiles().length;
            return item.isDirectory()
                    && actualNumberOfFiles == expectedNumberOfFiles;
        }

    }
}
