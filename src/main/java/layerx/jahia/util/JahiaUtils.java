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

package layerx.jahia.util;

import org.apache.commons.io.IOUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.render.*;
import org.jahia.services.render.scripting.Script;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import static layerx.Constants.HTML;
import static layerx.jahia.Constants.*;

/**
 * Jahia Utils
 *
 * @author      neozilon
 * @version     1.0.0
 * @since       2016-09-15
 */
public class JahiaUtils {

    private static Logger LOG = LoggerFactory.getLogger(JahiaUtils.class);

    /**
     * Check if it has configuration
     *
     * @param resource The resource to check for xk config node
     * @return true or false
     */
    public static boolean hasXKConfigNode(Resource resource){
        boolean response = false;
        if (resource != null && resource.getNode() != null){
            String path = getNodeTypePathInsideBundle(resource);
            Bundle bundle = getBundle(resource);
            response = hasXKConfigNode(bundle, path);
        }
        return response;
    }

    /**
     * Check if it has configuration
     *
     * @param extendedNodeType The ExtendedNodeType to check for xk config node
     * @return true or false
     */
    public static boolean hasXKConfigNode(ExtendedNodeType extendedNodeType){
        boolean response = false;
        if (extendedNodeType != null ){
            String path = getNodeTypePathInsideBundle(extendedNodeType);
            Bundle bundle = getBundle(extendedNodeType);
            response = hasXKConfigNode(bundle, path);
        }
        return response;
    }

    /**
     * Check if it has configuration
     *
     * @param view The View to check for xk config node
     * @return true or false
     */
    public static boolean hasXKConfigNode(View view){
        boolean response = false;

        if (view != null ){
            String path = getXKConfigPathFromScriptView(view);
            Bundle bundle = getBundle(view);
            response = hasXKConfigNode(bundle, path);
        }
        return response;
    }

    /**
     * Check if it has configuration
     *
     * @param bundle The Bundle to fetch resource
     * @param path THe path to check for xk config node
     * @return true or false
     */
    private static boolean hasXKConfigNode(Bundle bundle, String path){
        boolean response = false;
        if (bundle != null && path != null){
            URL url = bundle.getResource(path);
            if ( url != null ){
                response = true;
            }
        }
        return response;
    }

    /**
     * Get the node type path inside a bundle
     *
     * @param resource The resource to fetch the node type path
     * @return nodeTypePath
     */
    private static String getNodeTypePathInsideBundle(Resource resource){
        String nodePath = null;
        if (resource != null && resource.getNode() != null) {
            ExtendedNodeType extendedNodeType = null;
            try {
                extendedNodeType = resource.getNode().getPrimaryNodeType();
                nodePath = getNodeTypePathInsideBundle(extendedNodeType);
            } catch (RepositoryException e) {
                LOG.error(LOG_PRE + " Error getting getNodeTypePathInsideBundle: {}", resource.getPath(), e);
            }
        }
        return nodePath;
    }

    /**
     * Get the node type path inside a bundle
     *
     * @param extendedNodeType The ExtendedNodeType to fetch the node type path
     * @return nodeTypePath
     */
    private static String getNodeTypePathInsideBundle(ExtendedNodeType extendedNodeType){
        String nodePath = null;
        if (extendedNodeType !=null){
                String nodeTypeName = extendedNodeType.toString();
                nodeTypeName = nodeTypeName.replace(NODE_COLON_SEPARATOR, NODE_COLON_TO_FILE_SEPARATOR);
                nodePath = FILE_SEPARATOR + nodeTypeName + FILE_SEPARATOR + XK_CONFIG_FILE;
        }
        return nodePath;
    }

    /**
     * Get xk config path from script view
     *
     * @param view The View to fetch the xk config path from
     * @return xkConfigPath
     */
    private static String getXKConfigPathFromScriptView(View view){
        String path = null;
        if (view != null){
            String viewPath = view.getPath();

            Properties p = view.getDefaultProperties();

            try {
                Bundle bundle = JahiaUtils.getBundle(view);
                String bundleSymbolicName = bundle.getSymbolicName();

                // This assumes that the path is of the form /modules/<bundle_symbolic_name>/<template_node>/<view>/<renderer_script>
                // And we intend to get the path  /<bundle_symbolic_name>/<template_node>/xk-config.json
                String pathWithoutRenderScript = viewPath.substring(0, viewPath.lastIndexOf(FILE_SEPARATOR));
                String pathWithoutViewType = pathWithoutRenderScript.substring(0, pathWithoutRenderScript.lastIndexOf(FILE_SEPARATOR));
                String[] pathWithoutSymbolicName = pathWithoutViewType.split(bundleSymbolicName);
                String processedPath = "";
                if (pathWithoutSymbolicName.length >1 ){
                    processedPath = pathWithoutViewType.substring( pathWithoutViewType.indexOf(pathWithoutSymbolicName[1]) );
                }

                path = processedPath + FILE_SEPARATOR + XK_CONFIG_FILE;
            }catch (Exception e){
                LOG.error("Error getting parentPath from View Path: "+viewPath,e);
            }
        }
        return path;
    }

    /**
     * Get Jahia Templates Package from Node Type
     *
     * @param extendedNodeType The ExtendedNodeType to fetch the templates package from
     * @return templatesPackage
     */
    public static JahiaTemplatesPackage getJahiaTemplatesPackageFromNodeType(ExtendedNodeType extendedNodeType){
        JahiaTemplatesPackage jahiaTemplatesPackage = null;
        if (extendedNodeType != null){
            jahiaTemplatesPackage = extendedNodeType.getTemplatePackage();
        }
        return jahiaTemplatesPackage;
    }

    /**
     * Get Jahia Templates Package from Resource
     *
     * @param resource The Resource to fetch the templates package from
     * @return templatesPackage
     */
    public static JahiaTemplatesPackage getJahiaTemplatesPackageFromResource(Resource resource){
        JahiaTemplatesPackage jahiaTemplatesPackage = null;
        if (resource != null && resource.getNode() != null){
            try {
                jahiaTemplatesPackage = getJahiaTemplatesPackageFromNodeType(resource.getNode().getPrimaryNodeType());
            }catch (Exception e){
                LOG.error("Error getting JahiaTemplatePackageFrom Resource: {}",resource.getPath(),e);
            }
        }
        return jahiaTemplatesPackage;
    }

    /**
     * Get Jahia Templates Package from View
     *
     * @param view The View to fetch the templates package from
     * @return templatesPackage
     */
    public static JahiaTemplatesPackage getJahiaTemplatesPackageFromView(View view){
        JahiaTemplatesPackage jahiaTemplatesPackage = null;
        if (view != null){
            try {
                jahiaTemplatesPackage = view.getModule();

            }catch (Exception e){
                LOG.error("Error getting JahiaTemplatePackageFrom Resource: {}",view.getPath(),e);
            }
        }
        return jahiaTemplatesPackage;
    }

    /**
     * Get bundle by resource
     *
     * @param resource The Resource to get the bundle from
     * @return bundle The bundle that the resource is contained in
     */
    public static Bundle getBundle(Resource resource){
        Bundle bundle = null;
        JahiaTemplatesPackage jahiaTemplatesPackage = getJahiaTemplatesPackageFromResource(resource);
        if (jahiaTemplatesPackage != null) {
             bundle = jahiaTemplatesPackage.getBundle();
        }
        return bundle;
    }

    /**
     * Get bundle by node type
     *
     * @param extendedNodeType The ExtendedNodeType to get the bundle from
     * @return bundle The bundle that the ExtendedNodeType is contained in
     */
    public static Bundle getBundle(ExtendedNodeType extendedNodeType){
        Bundle bundle = null;
        JahiaTemplatesPackage jahiaTemplatesPackage = getJahiaTemplatesPackageFromNodeType(extendedNodeType);
        if (jahiaTemplatesPackage != null) {
            bundle = jahiaTemplatesPackage.getBundle();
        }
        return bundle;
    }

    /**
     * Get bundle by view
     *
     * @param view The View to get the bundle from
     * @return bundle The bundle that the View is contained in
     */
    public static Bundle getBundle(View view){
        Bundle bundle = null;
        JahiaTemplatesPackage jahiaTemplatesPackage = getJahiaTemplatesPackageFromView(view);
        if (jahiaTemplatesPackage != null) {
            bundle = jahiaTemplatesPackage.getBundle();
        }
        return bundle;
    }

    /**
     * Get input stream by resource
     *
     * @param resource The resource to get input stream from
     * @return inputStream
     */
    public static InputStream getInputStream(Resource resource){
        Bundle bundle = getBundle(resource);
        String path = getNodeTypePathInsideBundle(resource);
        InputStream inputStream = getInputStream(bundle,path);
        return inputStream;
    }

    /**
     * Get input stream by view
     *
     * @param view The View to get input stream from
     * @return inputStream
     */
    public static InputStream getInputStream(View view){
        Bundle bundle = getBundle(view);
        String path = getXKConfigPathFromScriptView(view);
        InputStream inputStream = getInputStream(bundle,path);
        return inputStream;
    }

    /**
     * Get input stream by node type
     *
     * @param extendedNodeType The NodeType to get input stream from
     * @return inputStream
     */
    public static InputStream getInputStream(ExtendedNodeType extendedNodeType){
        Bundle bundle = getBundle(extendedNodeType);
        String path = getNodeTypePathInsideBundle(extendedNodeType);
        InputStream inputStream = getInputStream(bundle,path);
        return inputStream;
    }

    /**
     * Get input stream by view
     *
     * @param bundle The bundle used to obtain resource
     * @param path The path to get resource
     * @return inputStream
     */
    public static InputStream getInputStream(Bundle bundle, String path){
        InputStream inputStream = null;
        if (bundle != null && path != null){
            URL url = bundle.getResource(path);
            if ( url != null ){
                try {
                    inputStream = url.openStream();
                }catch (IOException ioException){
                    LOG.error(LOG_PRE+ " Error getting InputStream For URL: {}: ",url.getPath(), ioException);
                }
            }
        }
        return inputStream;
    }

    /**
     * Get resource string
     *
     * @param resource The resource to get string from
     * @return resourceString
     */
    public static String getStringFromResource(Resource resource){
        String resourceString = null;
        InputStream is = getInputStream(resource);
        resourceString = getString(is);
        return resourceString;
    }

    /**
     * Get xk config string from view
     *
     * @param view The view to fetch xk config string from
     * @return xkConfigString
     */
    public static String getXKConfigStringFromView(View view){
        String xkConfigString = null;
        InputStream is = getInputStream(view);
        xkConfigString = getString(is);
        return xkConfigString;
    }

    /**
     * Get string from node type
     *
     * @param extendedNodeType The Node Type to get string from
     * @return nodeTypeString
     */
    public static String getStringFromNodeType(ExtendedNodeType extendedNodeType){
        String resourceString = null;
        InputStream is = getInputStream(extendedNodeType);
        resourceString = getString(is);
        return resourceString;
    }

    private static String getString(InputStream is){
        String resourceString = null;
        if (is != null){
            try {
                resourceString = IOUtils.toString(is);
                is.close();
            } catch (IOException e) {
                LOG.error(LOG_PRE+ "Error Getting getStringFromResource: ",e);
            }
        }
        return resourceString;
    }

    /**
     * Will remove the /modules/<symbolic_name>/... from String
     * The main usage for this will be to remove it from the bundle path.
     * @param symbolicName
     * @param path
     * @return
     */
    public static String removeBundleSymbolicNameFromModule(String symbolicName, String path){
	String[] pathWithoutSymbolicName = path.split(symbolicName);
	String processedPath = null;
	if (pathWithoutSymbolicName.length >1 ){
	    processedPath = path.substring( path.indexOf(pathWithoutSymbolicName[1]) );
	}
	return processedPath;
    }

    /**
     * Resolve Template Resource by View
     * @param request This is a HttpServletRequest
     * @param resource This is a resource to resolve for template
     * @param renderContext This is a RenderContext
     * @return view
     * @throws Exception
     */
    public static View resolveTemplateResourceView(HttpServletRequest request, Resource resource, RenderContext renderContext)
            throws Exception {
        View view = null;
        Script script = (Script) request.getAttribute(JAHIA_SCRIPT);
        if (script != null){
            view = script.getView();
        } else {
            RenderService service = RenderService.getInstance();
            Resource pageResource = new Resource(resource.getNode(), HTML, null, Resource.CONFIGURATION_PAGE);
            Template template = service.resolveTemplate(pageResource, renderContext);
            pageResource.setTemplate(template.getView());

            JCRNodeWrapper templateNode = pageResource.getNode().getSession().getNodeByIdentifier(template.node);
            Resource wrapperResource = new Resource(templateNode, pageResource.getTemplateType(), template.view,
                    Resource.CONFIGURATION_WRAPPER);
            script = service.resolveScript(wrapperResource, renderContext);
            if (script != null){
                view = script.getView();
            }
        }

        return view;
    }
}
