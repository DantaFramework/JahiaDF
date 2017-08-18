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
import layerx.api.*;
import layerx.api.configuration.Configuration;
import layerx.api.configuration.ConfigurationProvider;
import layerx.api.configuration.Mode;
import layerx.core.execution.ExecutionContextImpl;
import layerx.jahia.util.JahiaUtils;
import org.apache.commons.lang3.StringUtils;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.View;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.*;

import static layerx.Constants.*;
import static layerx.Constants.HTML_PAGE_CATEGORY;
import static layerx.core.Constants.XK_COMPONENT_CATEGORY;
import static layerx.jahia.Constants.*;

/**
 * LayerX Script Engine
 *
 * @author      neozilon
 * @version     1.0.0
 * @since       2016-09-17
 */
public class LayerXScriptEngine extends AbstractScriptEngine {

    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private static final String LOG_PRE = "|LAYER-X- SCRIPT ENGINE >|-> ";

    public static final String TEMPLATE_CONTENT_MODEL_ATTR_NAME = "template__contentmodel";

    private ConfigurationProvider configurationProvider;
    private ContextProcessorEngine contextProcessorEngine;
    private DOMProcessorEngine domProcessorEngine;
    private HelperFunctionBind helperFunctionBind;

    private final ScriptEngineFactory scriptEngineFactory;

    public LayerXScriptEngine(LayerXScriptEngineFactory layerXScriptEngineFactory, ContextProcessorEngine contextProcessorEngine, ConfigurationProvider configurationProvider,
                              DOMProcessorEngine domProcessorEngine, HelperFunctionBind helperFunctionBind) {
        super();
        this.scriptEngineFactory = layerXScriptEngineFactory;
        this.configurationProvider = configurationProvider;
        this.contextProcessorEngine = contextProcessorEngine;
        this.domProcessorEngine = domProcessorEngine;
        this.helperFunctionBind = helperFunctionBind;

    }

    public Object eval(Reader reader, ScriptContext scriptContext) throws ScriptException {
        String result = null;
        Bindings bindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);

        try {
            RenderContext renderContext = (RenderContext) bindings.get(JAHIA_BINDING_RENDER_CONTEXT);
            Resource resource = (Resource) bindings.get(JAHIA_BINDING_CURRENT_RESOURCE);

            if ( resource != null && renderContext != null) {

                HttpServletRequest request = renderContext.getRequest();
                HttpServletResponse response = renderContext.getResponse();
                String ext = ""; // TODO get Extension from URL
                String requestURI = request.getRequestURI();

                // Validation to check the XK Config in the Template Node
                // TODO check if there is a way to create templates with custom nodeTypes extending from jnt:template

                // For page resources, the configuration is obtained via the template associated to the view of the page
                boolean checkInTemplateNode = false;
                View view = null;
                if ( resource.getNode() != null && resource.getNode().getPrimaryNodeType() != null &&
                        StringUtils.startsWithAny(resource.getNode().getPrimaryNodeType().getName(), LIST_TEMPLATE_OPTION_TYPES)){
                    view = JahiaUtils.resolveTemplateResourceView(request, resource, renderContext);
                    if(view != null) {
                        checkInTemplateNode = true;
                    }
                }

                if (request.getMethod().equalsIgnoreCase(HTTP_GET) && (configurationProvider.hasConfig(resource) ||
                        ( checkInTemplateNode==true && configurationProvider.hasConfig(view))  )) {

		            Handlebars handlebars = new Handlebars(new HTMLResourceBasedTemplateLoader(resource, view));
                    handlebars.infiniteLoops(true);

                    // add helpers
                    List<HelperFunction> helpers = helperFunctionBind.getHelpers();

                    for (HelperFunction helper : helpers) {
                        handlebars.setStartDelimiter(START_DELIM);
                        handlebars.setEndDelimiter(END_DELIM);

                        // Send the entire bindings object as the tag support classes for these helpers use
                        // renderContext and resource
                        if(helper.name().equals(TemplateModuleHelperFunction.NAME)) {
                            handlebars.registerHelper(helper.name(), new TemplateModuleHelperFunction(
                                    bindings,
                                    response
                            ));
                        } else if(helper.name().equals(TemplateAreaHelperFunction.NAME)) {
                            handlebars.registerHelper(helper.name(), new TemplateAreaHelperFunction(
                                    bindings,
                                    request,
                                    response
                            ));
                        } else if(helper.name().equals(TemplateAddResourcesHelperFunction.NAME)) {
                            handlebars.registerHelper(helper.name(), new TemplateAddResourcesHelperFunction(
                                    bindings,
                                    request
                            ));
                        } else if(helper.name().equals(TemplateListHelperFunction.NAME)) {
                            handlebars.registerHelper(helper.name(), new TemplateListHelperFunction(
                                    bindings,
                                    request,
                                    response
                            ));
                        } else {
                            handlebars.registerHelper(helper.name(), helper);
                        }
                    }

                    TemplateContentModel contentModel = (TemplateContentModel) request.getAttribute(TEMPLATE_CONTENT_MODEL_ATTR_NAME);
                    if (contentModel == null) {
                        contentModel = new TemplateContentModel(request, response);
                        request.setAttribute(TEMPLATE_CONTENT_MODEL_ATTR_NAME, contentModel);
                    } else {
                        contentModel.extendScope();
                    }

                    ExecutionContextImpl executionContext = new ExecutionContextImpl();
                    executionContext.put(HTTP_REQUEST, request);
                    executionContext.put(JAHIA_RESOURCE, resource);
                    executionContext.put(JAHIA_RENDER_CONTEXT, renderContext);
                    executionContext.put(ENGINE_RESOURCE, resource);
                    if (checkInTemplateNode){
                        executionContext.put(JAHIA_SCRIPT_VIEW, view);
                        executionContext.put(ENGINE_RESOURCE, view);
                    }

                    List<String> currentProcessorChain = contextProcessorEngine.execute(executionContext, contentModel);

                    Map<String, Object> statisticsMap = new HashMap<>();
                    statisticsMap.put(PROCESSORS, currentProcessorChain);
                    contentModel.set(STATISTICS_KEY, statisticsMap); //Add to ContentModel for later inspection by Components

                    LayerXTemplateSourceReader templateSourceReader = new LayerXTemplateSourceReader();
                    String unprocessedResponse = templateSourceReader.contentReader(reader);
                    String outputHTML = unprocessedResponse;

                    Template template = handlebars.compileInline(unprocessedResponse);
                    Context handlebarsContext = contentModel.handlebarsContext();
                    outputHTML = template.apply(handlebarsContext);

                    if (hasPageAndHTMLPageCategories(renderContext, resource)) {
                        Document document = Jsoup.parse(outputHTML);
                        domProcessorEngine.execute(executionContext, document);
                        outputHTML = document.html();
                    }

                    contentModel.retractScope();
                    result = outputHTML;
                    Writer out = scriptContext.getWriter();
                    //out = (PrintWriter) bindings.get("out");

                    out.append(outputHTML);
                    //context.getWriter().append(outputHTML);

                    //return outputHTML;

                } else {
                    LOG.debug("{} is not a LayerX component", resource.getPath());
                }

            }else{
                LOG.info("RenderContext: {} Resource: {} ", renderContext, resource);
            }
        } catch (RuntimeException re) {
            LOG.error("Error Exception", re);
            throw re;
        } catch (Exception ew) {
            throw new ScriptException(ew);
        }
        return null;
    }


    public Object eval(String script, ScriptContext context) throws ScriptException {
        StringReader reader = new StringReader(script);
        Object obj = this.eval(reader, context);
        return obj;
    }

    @Override
    public Bindings createBindings() {
        return new SimpleBindings();
    }

    @Override
    public ScriptEngineFactory getFactory() {
        return scriptEngineFactory;
    }

    /* TODO REVIEW IF THIS IS STILL VALID OR NOT */
    /*
    private boolean ignoreRequest(final String requestURI) {
        boolean ignoreRequest = false;
        for (String pattern : ignorePatterns) { //loop through the patterns
            if (requestURI.contains(pattern)) { //this request has to be ignored
                ignoreRequest = true;
                break;
            }
        }
        return ignoreRequest;
    }
    */

    /**
     * This method validate if the component has or inherits the categories 'page' and 'htmlpage' in the xk.config node.
     *
     */
    private boolean hasPageAndHTMLPageCategories(RenderContext renderContext, Resource resource)throws Exception{
        boolean hasPageAndHTMLPageCategories = false;

        if (resource != null){
            Configuration configuration = configurationProvider.getFor(resource);
            if (configuration != null){
                Collection<String> compCategories = configuration.asStrings(XK_COMPONENT_CATEGORY, Mode.MERGE);

                if (compCategories.containsAll(Sets.newHashSet(PAGE_CATEGORY, HTML_PAGE_CATEGORY))) {
                    hasPageAndHTMLPageCategories = true;
                }else{
                    LOG.debug(LOG_PRE+ " Configuration NOT hasPageAndHTMLPageCategories");
                }
            }else {
                LOG.debug(LOG_PRE+ " Configuration is NULL ****");
            }
        }
        return hasPageAndHTMLPageCategories;
    }

}
