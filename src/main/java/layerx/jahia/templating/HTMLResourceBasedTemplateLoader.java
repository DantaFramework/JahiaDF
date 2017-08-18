/**
 * LayerX Jahia Bundle
 * (layerx.jahia)
 *
 * Copyright (C) 2017 Tikal Technologies, Inc. All rights reserved.
 *
 * Licensed under GNU Affero General Public License, Version v3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied;
 * without even the implied warranty of MERCHANTABILITY.
 * See the License for more details.
 */

package layerx.jahia.templating;

import com.github.jknack.handlebars.io.TemplateLoader;
import com.github.jknack.handlebars.io.TemplateSource;
import layerx.jahia.util.JahiaUtils;
import org.apache.commons.io.IOUtils;
import org.jahia.services.render.Resource;
import org.jahia.services.render.View;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Calendar;

import static layerx.Constants.BLANK;
import static layerx.Constants.HTML_EXT;
import static layerx.jahia.Constants.FILE_SEPARATOR;
import static layerx.jahia.Constants.LX_EXT;

/**
 * HTML Resource Based Template Loader
 *
 * @author      neozilon
 * @version     1.0.0
 * @since       2016-08-19
 */
public class HTMLResourceBasedTemplateLoader implements TemplateLoader {

    protected final static Logger LOG = LoggerFactory.getLogger(HTMLResourceBasedTemplateLoader.class);
    protected Resource resource;
    protected View view;

    //protected Component component;
    protected String prefix = "";
    protected String suffix = "";

    public HTMLResourceBasedTemplateLoader(Resource resource, View view)
            throws Exception {
        this.resource = resource;
	    this.view = view;
    }

    @Override
    public TemplateSource sourceAt(final String location)throws IOException {
	String resolvedLocation = this.resolve(location);
	String viewPath = view.getPath();
	String pathWithoutRenderScript = viewPath.substring(0, viewPath.lastIndexOf(FILE_SEPARATOR));
	Bundle bundle = JahiaUtils.getBundle(view);


	// This will look partials inside a folder with the same name as the Base Script to use, if you pass a path "/" it will ignore it and use the provided path
	String pathToLook = (resolvedLocation.contains("/"))? pathWithoutRenderScript + FILE_SEPARATOR + resolvedLocation : pathWithoutRenderScript + FILE_SEPARATOR + view.getKey() + FILE_SEPARATOR + resolvedLocation;
    pathToLook = JahiaUtils.removeBundleSymbolicNameFromModule(bundle.getSymbolicName(), pathToLook);
	InputStream is = JahiaUtils.getInputStream(bundle, pathToLook);

    if (is == null){
        LOG.error("LAYERX | Not able to load Partial: --> "+ pathToLook);
    }

	return new HTMLFileTemplateSource(resource, is);
    }

    @Override
    public String resolve(final String location) {
	String fullLocation = (location.endsWith(LX_EXT) || location.endsWith(HTML_EXT))? location : new StringBuilder(location).append(HTML_EXT).toString();
	return fullLocation;
    }

    @Override
    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String getPrefix() {
        return BLANK;
    }

    @Override
    public void setSuffix(final String suffix) {
        this.suffix = suffix;
    }

    @Override
    public String getSuffix() {
        return BLANK;
    }

    public static class HTMLFileTemplateSource implements TemplateSource {

        private final Resource resource;
	private final InputStream is;

	public HTMLFileTemplateSource(final Resource scriptResource, final InputStream is)
                throws FileNotFoundException {
	    if (scriptResource == null ) {
		throw new FileNotFoundException("No file with the path  was found for resource (probably will be a partial): ");
	    }
            this.resource = scriptResource;
	    this.is = is;
        }

        @Override
        public String content()
                throws IOException {
	    return IOUtils.toString(this.is, "UTF-8");
        }

        protected InputStream inputStream()
                throws IOException {
            return is;
        }

        @Override
        public String filename() {
            return resource.getPath();
        }

        @Override
        public long lastModified() {
            try {
                return resource.getNode().getLastModifiedAsDate().getTime();
            } catch (Exception ew) {
                return Calendar.getInstance().getTimeInMillis();
            }
        }
    }
}
