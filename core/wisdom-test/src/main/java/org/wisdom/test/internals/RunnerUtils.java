/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2014 Wisdom Framework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wisdom.test.internals;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

/**
 * A couple of methods used by Runners to detect the wisdom runtime and the current application bundle.
 * Be aware that these methods make the assumption that the current execution directory if the project's base directory.
 */
public class RunnerUtils {
    /**
     * Checks if a file having somewhat the current tested application name is contained in the given directory. This
     * method follows the default maven semantic. The final file is expected to have a name compliant with the
     * following rules: <code>artifactId-version.jar</code>. If the version ends with <code>-SNAPSHOT</code>,
     * it just checks for <code>artifactId-stripped_version</code>, where stripped version is the version without the
     * <code>SNAPSHOT</code> part.
     * <p>
     * The artifactId and version are read from the <code>target/osgi/osgi.properties</code> file,
     * that should have been written by the Wisdom build process.
     *
     * @param directory the directory
     * @return the bundle file if found
     * @throws java.io.IOException if something bad happens.
     */
    public static File detectApplicationBundleIfExist(File directory) throws IOException {
        Properties properties = getMavenProperties();
        if (properties == null || directory == null || !directory.isDirectory()) {
            return null;
        }

        final String artifactId = properties.getProperty("project.artifactId");
        String version = properties.getProperty("project.version");
        final String strippedVersion;
        if (version.endsWith("-SNAPSHOT")) {
            strippedVersion = version.substring(0, version.length() - "-SNAPSHOT".length());
        } else {
            strippedVersion = version;
        }

        Iterator<File> files = FileUtils.iterateFiles(directory, new AbstractFileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile()
                        && file.getName().startsWith(artifactId + "-" + strippedVersion)
                        && file.getName().endsWith(".jar");
            }
        }, TrueFileFilter.INSTANCE);

        if (files.hasNext()) {
            return files.next();
        }
        return null;
    }

    /**
     * Gets the (Maven) artifact's file if exists.
     *
     * @param chameleonRoot the root of the chameleon
     * @return the artifact's file (the jar file) if it exists, {@code null} otherwise.
     * @throws IOException if the Maven properties cannot be read.
     */
    public static File getApplicationArtifactIfExists(File chameleonRoot) throws IOException {
        Properties properties = getMavenProperties();
        if (properties == null || chameleonRoot == null || !chameleonRoot.isDirectory()) {
            return null;
        }

        final String artifactId = properties.getProperty("project.artifactId");
        String version = properties.getProperty("project.version");

        File artifact = new File(chameleonRoot.getParentFile(), artifactId + "-" + version + ".jar");
        if (artifact.isFile()) {
            return artifact;
        }
        return null;
    }

    /**
     * We should have generated a target/osgi/osgi.properties file will all the metadata we inherit from Maven.
     *
     * @return the properties read from the file.
     */
    private static Properties getMavenProperties() throws IOException {
        File osgi = new File("target/osgi/osgi.properties");
        if (osgi.isFile()) {
            FileInputStream fis = null;
            try {
                Properties read = new Properties();
                fis = new FileInputStream(osgi);
                read.load(fis);
                return read;
            } finally {
                IOUtils.closeQuietly(fis);
            }
        }
        return null;
    }

    /**
     * Checks whether the Wisdom server was correctly unpacked. If so, return the base directory.
     *
     * @return the Wisdom base directory if there, {@literal null} otherwise.
     */
    public static File checkWisdomInstallation() {
        File directory = new File("target/wisdom");
        if (!directory.isDirectory()) {
            throw new ExceptionInInitializerError("Wisdom is not installed in " + directory.getAbsolutePath() + " - " +
                    "please check your execution directory, and that Wisdom is prepared correctly. To setup Wisdom, " +
                    "run 'mvn pre-integration-test' from your application directory");
        }
        File conf = new File(directory, "conf/application.conf");
        if (!conf.isFile()) {
            throw new ExceptionInInitializerError("Wisdom is not correctly installed in " + directory.getAbsolutePath()
                    + " - the configuration file does not exist. Please check your Wisdom runtime. To setup Wisdom, " +
                    "run 'mvn clean pre-integration-test' from your application directory");
        }
        return directory;
    }
}
