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
package org.wisdom.template.thymeleaf.impl;

import org.apache.commons.io.FilenameUtils;
import org.wisdom.api.Controller;
import org.wisdom.api.asset.Assets;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Renderable;
import org.wisdom.api.router.Router;
import org.wisdom.api.templates.Template;
import org.wisdom.template.thymeleaf.ThymeleafTemplateCollector;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

/**
 * Template implementation for ThymeLeaf template.
 */
public class ThymeLeafTemplateImplementation implements Template {
    public static final String THYME_LEAF_ENGINE_NAME = "thymeleaf";
    public static final String TEMPLATES = "/templates/";
    private final URL url;
    private final String name;
    private final Router router;
    private final Assets assets;
    private WisdomTemplateEngine templateEngine;

    public ThymeLeafTemplateImplementation(WisdomTemplateEngine templateEngine, File templateFile, Router router,
                                           Assets assets
    ) throws MalformedURLException {
        this(templateEngine, templateFile.toURI().toURL(), router, assets);
    }

    public ThymeLeafTemplateImplementation(WisdomTemplateEngine templateEngine, URL templateURL, Router router,
                                           Assets assets) {
        this.templateEngine = templateEngine;
        this.url = templateURL;
        // The name of the template is its relative path against its template root
        // For instance in bundles, it's the relative paths from /templates/
        String externalForm = templateURL.toExternalForm();
        int indexOfTemplates = externalForm.indexOf(TEMPLATES);
        if (indexOfTemplates == -1) {
            name = FilenameUtils.getBaseName(templateURL.getFile());
        } else {
            name = externalForm.substring(indexOfTemplates + TEMPLATES.length(),
                    externalForm.length() - (ThymeleafTemplateCollector.THYMELEAF_TEMPLATE_EXTENSION.length() + 1));
        }
        this.router = router;
        this.assets = assets;
    }

    public URL getURL() {
        return url;
    }

    /**
     * @return the template name, usually the template file name without the extension.
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * @return the template full name. For example, for a file, it will be the file name (including extension).
     */
    @Override
    public String fullName() {
        return url.toExternalForm();
    }

    /**
     * @return the name of the template engine, generally the name of the technology.
     */
    @Override
    public String engine() {
        return THYME_LEAF_ENGINE_NAME;
    }

    /**
     * @return the mime type of the document produced by the template.
     */
    @Override
    public String mimetype() {
        return MimeTypes.HTML;
    }

    /**
     * Renders the template.
     *
     * @param variables the parameters
     * @return the rendered object.
     */
    @Override
    public Renderable<?> render(Controller controller, Map<String, Object> variables) {
        return templateEngine.process(this, controller, router, assets, variables);
    }

    @Override
    public Renderable<?> render(Controller controller) {
        return templateEngine.process(this, controller, router, assets, Collections.<String, Object>emptyMap());
    }

    public Dictionary<String, ?> getServiceProperties() {
        Dictionary<String, String> props = new Hashtable<>();
        props.put("name", name());
        props.put("fullName", fullName());
        props.put("mimetype", mimetype());
        props.put("engine", engine());
        return props;
    }
}
