/**
 * Danta Jahia Bundle
 * (danta.jahia)
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

package danta.jahia.templating;

import danta.api.ContextProcessorEngine;
import danta.api.DOMProcessorEngine;
import danta.api.configuration.ConfigurationProvider;
import danta.jahia.configuration.JahiaConfigurationProviderImpl;
import org.apache.felix.scr.annotations.*;
import org.jahia.services.render.scripting.bundle.BundleScriptEngineManager;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Danta Script Engine Factory
 *
 * @author      neozilon
 * @version     1.0.0
 * @since       2016-09-17
 */
@Component(
        label = "Danta Scripting Engine",
        description = "Tikal Scripting engine for Danta",
        immediate = true,
        metatype = true
)
@Service(ScriptEngineFactory.class)
@Properties({
        @Property(name = "service.description", value = "Scripting engine for Danta"),
        @Property(name = "service.ranking", intValue = 0, propertyPrivate = false)
})
public class DantaScriptEngineFactory implements ScriptEngineFactory {

    private static Logger LOG = LoggerFactory.getLogger(DantaScriptEngineFactory.class);
    private static final String LOG_PRE = "|LAYER-X-> |-> ";

    protected final static String ENGINE_NAME = "Danta Script Engine";
    protected final static String ENGINE_VERSION = "1.0";
    protected final static String SCRIPT_NAME = "danta";
    protected final static String LANGUAGE_VERSION = "1.0";
    protected final static String LX_EXT = "d";

    private final static List<String> names = Collections.singletonList("danta");
    private final static List<String> mimeTypes = Collections.emptyList();
    private List<String> extensions;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY, policy = ReferencePolicy.STATIC)
    private ContextProcessorEngine contextProcessorEngineService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY, policy = ReferencePolicy.STATIC)
    private ConfigurationProvider configurationProviderService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY, policy = ReferencePolicy.STATIC)
    private DOMProcessorEngine domProcessorEngineService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY, policy = ReferencePolicy.STATIC)
    private HelperFunctionBind helperFunctionBindService;

    private static ContextProcessorEngine contextProcessorEngineStatic;
    private static ConfigurationProvider configurationProviderStatic;
    private static DOMProcessorEngine domProcessorEngineStatic;
    private static HelperFunctionBind helperFunctionBindStatic;

    @Activate
    protected void activate(BundleContext bundleContext)
            throws Exception {

        LOG.info(LOG_PRE + "======= DantaScriptEngineFactory Activate  ==========");


        configurationProviderStatic = configurationProviderService;
        contextProcessorEngineStatic = contextProcessorEngineService;
        domProcessorEngineStatic = domProcessorEngineService;
        helperFunctionBindStatic = helperFunctionBindService;

    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext){
        LOG.info(LOG_PRE + "======= DantaScriptEngineFactory Deactivate  ==========");
    }

    @Override
    public String getLanguageName() {
        return SCRIPT_NAME;
    }

    @Override
    public String getLanguageVersion() {
        return LANGUAGE_VERSION;
    }

    @Override
    public List<String> getExtensions() {
        ArrayList<String> extList = new ArrayList<String>();
        extList.add(LX_EXT);
        return extList;
    }

    @Override
    public String getEngineVersion() {
        return ENGINE_VERSION;
    }

    @Override
    public String getProgram(String... statements) {
        // TODO REVIEW for AEM
        return null;
    }

    @Override
    public Object getParameter(String key) {
        // TODO REVIEW for AEM
        return null;
    }

    @Override
    public String getMethodCallSyntax(String obj, String m, String... args) {
        // TODO REVIEW for AEM
        return null;
    }

    @Override
    public String getOutputStatement(String toDisplay) {
        // TODO REVIEW for AEM
        return null;
    }

    @Override
    public List<String> getNames() {
        return names;
    }

    @Override
    public List<String> getMimeTypes() {
        return mimeTypes;
    }

    @Override
    public String getEngineName() {
        return ENGINE_NAME;
    }

    @Override
    public ScriptEngine getScriptEngine() {
        //LOG.info("getScriptEngine "+ contextProcessorEngineStatic+ " "+ configurationProviderStatic+ " "+ domProcessorEngineStatic);
        return new DantaScriptEngine(this, contextProcessorEngineStatic, configurationProviderStatic, domProcessorEngineStatic, helperFunctionBindStatic);
    }

    /*
    protected void setExtensions(String... extensions) {
        if(extensions == null) {
            this.extensions = Collections.emptyList();
        } else {
            this.extensions = Arrays.asList(extensions);
        }

    }
    */
}
