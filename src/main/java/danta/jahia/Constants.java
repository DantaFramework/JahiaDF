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

package danta.jahia;

import static javax.jcr.NamespaceRegistry.PREFIX_JCR;
import static javax.jcr.NamespaceRegistry.PREFIX_MIX;
import static javax.jcr.NamespaceRegistry.PREFIX_NT;
import static danta.Constants.*;
import static org.apache.jackrabbit.spi.Name.*;

/**
 * Constants for Jahia specific.
 *
 * @author      neozilon
 * @version     1.0.0
 * @since       2016-09-30
 */
public class Constants {

    public static final String JAHIA_RENDER_CONTEXT = "jahiaRenderContext";
    public static final String JAHIA_RESOURCE = "jahiaResource";
    public static final String JAHIA_SCRIPT_VIEW = "jahiaScriptView";
    public static final String JAHIA_SCRIPT = "script";

    public static final String FILE_SEPARATOR = "/";
    public static final String XK_CONFIG_FILE = "xk-config.json";

    public static final String NODE_COLON_SEPARATOR = ":";
    public static final String NODE_COLON_TO_FILE_SEPARATOR = "_";

    public static final String LOG_PRE = "DANTA -> ";

    public static final String HTTP_REQUEST = "general_http_request";

    // Jahia Workspaces

    public static final String IS_PREVIEW_MODE = "jahia_preview_mode";
    public static final String IS_LIVE_MODE = "jahia_live_mode";
    public static final String IS_AJAX_REQUEST = "jahia_is_ajax_request";
    public static final String JAHIA_WORKSPACE = "workspace";

    // Jahia Default Properties Page
    public static final String JAHIA_PAGE_KEYWORDS = "j:keywords";
    public static final String JAHIA_PAGE_TAGS = "j:tagList";

    public static final String JCR_NODE_UUID = "jcr_uuid";

    public static final String JCR_DESCRIPTION = "jcr:description";
    public static final String JCR_CREATED = "jcr:created";
    public static final String JCR_TITLE = "jcr:title";

    // Jahia URLs
    public static final String FILES_PATH = "/files/";

    // LX Extension
    public static final String LX = "d";
    public static final String LX_EXT = danta.Constants.DOT + LX;

    // Danta Configuration Global Properties and Page Properties
    public static final String DANTA_CONFIGURATION_GLOBAL_NODE_NAME = "global";
    public static final String DANTA_CONFIGURATION_PAGE_NODE_NAME = "pages";
    public static final String DANTA_CONFIGURATION_PAGE_PATH_PROPERTY = "dantaPagePath";
    public static final String DANTA_CONFIGURATION_SITE_SUFFIX_PATH = "contents";
    public static final String DANTA_CONFIGURATION_NODE_NAME = "danta";
    public static final String DANTA_CONFIGURATION_DEFAULT_SUFFIX_PATH = SLASH + DANTA_CONFIGURATION_SITE_SUFFIX_PATH + SLASH + DANTA_CONFIGURATION_NODE_NAME;

    // JAHIA BINDINGS KEY
    public static final String JAHIA_BINDING_CURRENT_RESOURCE = "currentResource";
    public static final String JAHIA_BINDING_RENDER_CONTEXT = "renderContext";

    // JAHIA TEMPLATE NODE TYPES
    public static final String JAHIA_JNT_TEMPLATE = "jnt:template";
    public static final String JAHIA_JNT_PAGE_TEMPLATE = "jnt:pageTemplate";
    public static final String JAHIA_JNT_CONTENT_TEMPLATE = "jnt:contentTemplate";
    public static final String JAHIA_JNT_CONTENT_LIST = "jnt:contentList";
    public static final String JAHIA_JNT_PAGE = "jnt:page";

    public static final String[] LIST_TEMPLATE_OPTION_TYPES = new String[]{
            JAHIA_JNT_TEMPLATE,
            JAHIA_JNT_PAGE_TEMPLATE,
            JAHIA_JNT_CONTENT_TEMPLATE,
            JAHIA_JNT_PAGE
    };

    public static final String JAHIA_J_NODE_NAME = "j:nodename";
    public static final String JAHIA_J_ORIGINS = "j:originWS";
    public static final String JAHIA_LAST_PUBLISHED = "j:lastPublished";
    public static final String JAHIA_PUBLISHED = "j:published";

    public static final String[] RESERVED_SYSTEM_NAME_PREFIXES = new String[]{
            makeNS(PREFIX_JCR),
            makeNS(PREFIX_NT),
            makeNS(SLING_NAMESPACE_PREFIX),
            NS_CQ,
            makeNS(NS_REP_PREFIX),
            makeNS(PREFIX_MIX),
            makeNS(NS_SV_PREFIX),
            makeNS(NS_XML_PREFIX),
            makeNS(NS_XMLNS_PREFIX),
            NS_VLT,
            JAHIA_J_NODE_NAME,
            JAHIA_J_ORIGINS,
            JAHIA_LAST_PUBLISHED,
            JAHIA_PUBLISHED
    };

    // Web services
    public static final String SERVER_RESPONSE_CONTENT_TYPE = "application/json; charset=utf-8";

    // Generic
    public static final String HTML_EXT = ".html";

}
