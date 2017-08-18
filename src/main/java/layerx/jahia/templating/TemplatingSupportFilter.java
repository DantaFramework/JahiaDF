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

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.google.common.collect.Sets;
import layerx.Constants;
import layerx.api.ContextProcessorEngine;
import layerx.api.DOMProcessor;
import layerx.api.configuration.Configuration;
import layerx.api.configuration.ConfigurationProvider;
import layerx.api.configuration.Mode;
import layerx.core.execution.ExecutionContextImpl;
import layerx.core.util.ContextProcessorPriorityComparator;
import layerx.core.util.DOMProcessorPriorityComparator;
import org.apache.felix.scr.annotations.*;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.View;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;

import org.jahia.services.render.scripting.Script;
import org.jahia.services.render.scripting.bundle.BundleScriptEngineManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.*;

import static layerx.Constants.*;
import static layerx.core.Constants.XK_COMPONENT_CATEGORY;
import static layerx.jahia.Constants.*;

/**
 * Templating support filter
 *
 * @author      jarriola
 * @version     1.0.0
 * @since       2016-08-11
 */
public class TemplatingSupportFilter extends AbstractFilter{

    private static Logger LOG = LoggerFactory.getLogger(TemplatingSupportFilter.class);
    private static final String LOG_PRE = "|LAYER-X->TF|-> ";

    public static final String TEMPLATE_CONTENT_MODEL_ATTR_NAME = "template__contentmodel";
    public static final String IGNORE_PATTERN_LIST_PROPERTY_NAME = "ignorePatterns";
    public static final int MAX_CACHE_SIZE = 20;

    static final ContextProcessorPriorityComparator PRIORITY_ORDER = new ContextProcessorPriorityComparator();
    static final DOMProcessorPriorityComparator DOM_PROCESSOR_PRIORITY_COMPARATOR = new DOMProcessorPriorityComparator();


    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        /*
        LOG.info(LOG_PRE +"** Templating Suppport Filter BEGINS **: ");

        ScriptEngineManager mgr = BundleScriptEngineManager.getInstance();
        List<ScriptEngineFactory> factories = mgr.getEngineFactories();

        for (ScriptEngineFactory factory : factories) {

            LOG.info(LOG_PRE +"ScriptEngineFactory Info");

            String engName = factory.getEngineName();
            String engVersion = factory.getEngineVersion();
            String langName = factory.getLanguageName();
            String langVersion = factory.getLanguageVersion();

            LOG.info(LOG_PRE +"\tScript Engine: {} ( {} )", engName, engVersion);

            List<String> engNames = factory.getNames();
            for(String name : engNames) {
                LOG.info(LOG_PRE +"\tEngine Alias: {}", name);
            }

            LOG.info(LOG_PRE +"\tLanguage: {} ( {} )", langName, langVersion);

            LOG.info(LOG_PRE + " Extensions: {} ", factory.getExtensions() );

        }


        LOG.info(LOG_PRE +"---- Templating Support Filter END ----");
        */
        return previousOut;
    }

}
