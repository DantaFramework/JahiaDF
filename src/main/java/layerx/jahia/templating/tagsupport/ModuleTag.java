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

package layerx.jahia.templating.tagsupport;

import com.google.common.collect.Ordering;
import layerx.jahia.templating.AbstractJahiaHelperFunction;
import org.apache.commons.lang.StringUtils;
import org.apache.taglibs.standard.tag.common.core.ParamParent;
import org.jahia.api.Constants;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ConstraintsHelper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.*;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.AggregateFilter;
import org.jahia.services.render.filter.TemplateAttributesFilter;
import org.jahia.services.render.scripting.Script;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.AccessDeniedException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
//JSP VERSION: import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;

/**
 * This is the implementation of the JSP tag: <template:module /> adapted to LayerX.
 *
 * @author      jarriola
 * @version     1.0.0
 * @since       2017-08-11
 */
public class ModuleTag extends BodyTagSupport implements ParamParent {

    protected AbstractJahiaHelperFunction helper;
    public void setCallerHelper(AbstractJahiaHelperFunction helper) {
        this.helper = helper;
    }

    private static final long serialVersionUID = -8968618483176483281L;
    private static final Logger logger = LoggerFactory.getLogger(org.jahia.taglibs.template.include.ModuleTag.class);

    private static volatile AbstractFilter exclusionFilter = null;
    private static volatile boolean exclusionFilterChecked;

    protected String path;
    protected JCRNodeWrapper node;
    protected JCRSiteNode contextSite;
    protected String nodeName;
    protected String view;
    protected String templateType = null;
    protected boolean editable = true;
    protected String nodeTypes = null;
    protected int listLimit = -1;
    protected String constraints = null;
    protected String var = null;
    protected StringBuilder builder = new StringBuilder();
    protected Map<String, String> parameters = new HashMap<String, String>();
    protected boolean checkConstraints = true;
    protected boolean showAreaButton = true;
    protected boolean skipAggregation = false;

    public String getPath() {
        return path;
    }

    public String getNodeName() {
        return nodeName;
    }

    public JCRNodeWrapper getNode() {
        return node;
    }

    public String getView() {
        return view;
    }

    public String getTemplateType() {
        return templateType;
    }

    public boolean isEditable() {
        return editable;
    }

    public String getVar() {
        return var;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setNodeName(String node) {
        this.nodeName = node;
    }

    public void setNode(JCRNodeWrapper node) {
        this.node = node;
    }

    public void setView(String view) {
        this.view = view;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public void setNodeTypes(String nodeTypes) {
        this.nodeTypes = nodeTypes;
    }

    public void setListLimit(int listLimit) {
        this.listLimit = listLimit;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setContextSite(JCRSiteNode contextSite) {
        this.contextSite = contextSite;
    }

    public void setSkipAggregation(boolean skipAggregation) {
        this.skipAggregation = skipAggregation;
    }

    @Override
    public int doEndTag() throws JspException {

        try {

            // JSP VERSION: RenderContext renderContext =
            //          (RenderContext) pageContext.getAttribute("renderContext", PageContext.REQUEST_SCOPE);
            RenderContext renderContext = (RenderContext) this.helper.getBindings().get("renderContext");
            builder = new StringBuilder();

            // JSP VERSION: Resource currentResource =
            //          (Resource) pageContext.getAttribute("currentResource", PageContext.REQUEST_SCOPE);
            Resource currentResource = (Resource) this.helper.getBindings().get("currentResource");

            findNode(renderContext, currentResource);

            String resourceNodeType = null;
            if (parameters.containsKey("resourceNodeType")) {
                resourceNodeType = URLDecoder.decode(parameters.get("resourceNodeType"), "UTF-8");
            }

            if (node != null) {

                try {
                    constraints = ConstraintsHelper.getConstraints(node);
                } catch (RepositoryException e) {
                    logger.error("Error when getting list constraints", e);
                }

                if (templateType == null) {
                    templateType = currentResource.getTemplateType();
                }

                Resource resource = new Resource(node, templateType, view, getConfiguration());

                //JSP VERSION: String charset = pageContext.getResponse().getCharacterEncoding();
                String charset = this.helper.getResponse().getCharacterEncoding();
                for (Map.Entry<String, String> param : parameters.entrySet()) {
                    resource.getModuleParams().put(URLDecoder.decode(param.getKey(), charset),
                            URLDecoder.decode(param.getValue(), charset));
                }

                if (resourceNodeType != null) {
                    try {
                        resource.setResourceNodeType(NodeTypeRegistry.getInstance().getNodeType(resourceNodeType));
                    } catch (NoSuchNodeTypeException e) {
                        throw new JspException(e);
                    }
                }

                boolean isVisible = true;
                try {
                    isVisible = renderContext.getEditModeConfig() == null || renderContext.isVisible(node);
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                }

                try {

                    boolean canEdit = canEdit(renderContext) && contributeAccess(renderContext,
                            resource.getNode()) && !isExcluded(renderContext, resource);

                    boolean nodeEditable = checkNodeEditable(renderContext, node);
                    resource.getModuleParams().put("editableModule", canEdit && nodeEditable);

                    if (canEdit) {

                        String type = getModuleType(renderContext);
                        List<String> contributeTypes = contributeTypes(renderContext, resource.getNode());
                        String oldNodeTypes = nodeTypes;
                        String add = "";
                        if (!nodeEditable) {
                            add = "editable=\"false\"";
                        }
                        if (contributeTypes != null) {
                            nodeTypes = StringUtils.join(contributeTypes, " ");
                            add = "editable=\"false\"";
                        }
                        if (node.isNodeType(Constants.JAHIAMIX_BOUND_COMPONENT)) {
                            add += " bindable=\"true\"";
                        }

                        Script script = null;
                        try {
                            script = RenderService.getInstance().resolveScript(resource, renderContext);
                            printModuleStart(type, node.getPath(), resource.getResolvedTemplate(),
                                    script, add);
                        } catch (TemplateNotFoundException e) {
                            printModuleStart(type, node.getPath(), resource.getResolvedTemplate(),
                                    null, add);
                        }
                        nodeTypes = oldNodeTypes;
                        currentResource.getDependencies().add(node.getCanonicalPath());
                        if (isVisible) {
                            render(renderContext, resource);
                        }
                        //Copy dependencies to parent Resource (only for include of the same node)
                        if (currentResource.getNode().getPath().equals(resource.getNode().getPath())) {
                            currentResource.getRegexpDependencies().addAll(resource.getRegexpDependencies());
                            currentResource.getDependencies().addAll(resource.getDependencies());
                        }
                        printModuleEnd();
                    } else {
                        currentResource.getDependencies().add(node.getCanonicalPath());
                        if (isVisible) {
                            render(renderContext, resource);
                        } else {
                            //JSP VERSION: pageContext.getOut().print("&nbsp;");
                            this.helper.builder.append("&nbsp;");

                        }
                        //Copy dependencies to parent Resource (only for include of the same node)
                        if (currentResource.getNode().getPath().equals(resource.getNode().getPath())) {
                            currentResource.getRegexpDependencies().addAll(resource.getRegexpDependencies());
                            currentResource.getDependencies().addAll(resource.getDependencies());
                        }
                    }
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } catch (RenderException ex) {
            throw new JspException(ex.getCause());
        } catch (IOException ex) {
            throw new JspException(ex);
        } finally {
            if (var != null) {
                pageContext.setAttribute(var, builder.toString());
            }
            path = null;
            node = null;
            contextSite = null;
            nodeName = null;
            view = null;
            templateType = null;
            editable = true;
            nodeTypes = null;
            listLimit = -1;
            constraints = null;
            var = null;
            builder = null;
            parameters.clear();
            checkConstraints = true;
            showAreaButton = true;
            skipAggregation = false;
        }
        return EVAL_PAGE;
    }

    private boolean isExcluded(RenderContext renderContext, Resource resource) throws RepositoryException {
        AbstractFilter filter = getExclusionFilter();
        if (filter == null) {
            return false;
        }
        try {
            return filter.prepare(renderContext, resource, null) != null;
        } catch (Exception e) {
            logger.error("Cannot evaluate exclude filter", e);
        }
        return false;
    }

    private static AbstractFilter getExclusionFilter() {
        if (!exclusionFilterChecked) {
            synchronized (org.jahia.taglibs.template.include.ModuleTag.class) {
                if (!exclusionFilterChecked) {
                    try {
                        exclusionFilter = (AbstractFilter) SpringContextSingleton.getBeanInModulesContext("ChannelExclusionFilter");
                    } catch (Exception e) {
                    }
                    exclusionFilterChecked = true;
                }
            }
        }
        return exclusionFilter;
    }

    protected List<String> contributeTypes(RenderContext renderContext, JCRNodeWrapper node) {

        if (!"contributemode".equals(renderContext.getEditModeConfigName())) {
            return null;
        }
        JCRNodeWrapper contributeNode = null;
        if (renderContext.getRequest().getAttribute("areaListResource") != null) {
            contributeNode = (JCRNodeWrapper) renderContext.getRequest().getAttribute("areaListResource");
        }

        try {
            if (node.hasProperty(Constants.JAHIA_CONTRIBUTE_TYPES)) {
                contributeNode = node;
            }
            if (contributeNode != null && contributeNode.hasProperty(Constants.JAHIA_CONTRIBUTE_TYPES)) {
                LinkedHashSet<String> l = new LinkedHashSet<String>();
                Value[] v = contributeNode.getProperty(Constants.JAHIA_CONTRIBUTE_TYPES).getValues();
                if (v.length == 0) {
                    l.add("jmix:editorialContent");
                } else {
                    for (Value value : v) {
                        l.add(value.getString());
                    }
                }
                LinkedHashSet<String> subtypes = new LinkedHashSet<String>();
                final Set<String> installedModulesWithAllDependencies = renderContext.getSite().getInstalledModulesWithAllDependencies();
                for (String s : l) {
                    ExtendedNodeType nt = NodeTypeRegistry.getInstance().getNodeType(s);
                    if (nt != null) {
                        if (!nt.isAbstract() && !nt.isMixin() &&
                                (nt.getTemplatePackage() == null || installedModulesWithAllDependencies.contains(nt.getTemplatePackage().getId()))) {
                            subtypes.add(nt.getName());
                        }
                        for (ExtendedNodeType subtype : nt.getSubtypesAsList()) {
                            if (!subtype.isAbstract() && !subtype.isMixin() &&
                                    (subtype.getTemplatePackage() == null|| installedModulesWithAllDependencies.contains(subtype.getTemplatePackage().getId()))) {
                                subtypes.add(subtype.getName());
                            }
                        }
                    }
                }
                if (subtypes.size() < 10) {
                    return new ArrayList<String>(subtypes);
                }
                return new ArrayList<String>(l);
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private boolean contributeAccess(RenderContext renderContext, JCRNodeWrapper node) {

        if (!"contributemode".equals(renderContext.getEditModeConfigName())) {
            return true;
        }
        JCRNodeWrapper contributeNode;
        final Object areaListResource = renderContext.getRequest().getAttribute("areaListResource");
        if (areaListResource != null) {
            contributeNode = (JCRNodeWrapper) areaListResource;
        } else {
            contributeNode = (JCRNodeWrapper) renderContext.getRequest().getAttribute(TemplateAttributesFilter.AREA_RESOURCE);
        }

        try {
            final Boolean nodeStatus = isNodeEditableInContributeMode(node);
            final Boolean contributeNodeStatus = contributeNode != null ? isNodeEditableInContributeMode(contributeNode) : null;

            final String sitePath = renderContext.getSite().getPath();
            if (nodeStatus != null) {
                // first look at the current node's status with respect to editable in contribution mode, if it's determined, then use that
                return nodeStatus;
            } else if (contributeNodeStatus != null) {
                // otherwise, look at the contribute node's status if it exists and use that
                return contributeNodeStatus;
            } else if (node.getPath().startsWith(sitePath)) {
                // otherwise, if the property wasn't defined on the nodes we are interested in, look at the parent iteratively until we know the status of the property
                while (!node.getPath().equals(sitePath)) {
                    node = node.getParent();

                    final Boolean parentStatus = isNodeEditableInContributeMode(node);
                    if (parentStatus != null) {
                        return parentStatus;
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * Returns <code>null</code> if the node we're looking at doesn't have the editable in contribution mode property, otherwise returns the value of the property.
     * @param node the node we're interested in
     * @return <code>null</code> if the node we're looking at doesn't have the editable in contribution mode property, otherwise returns the value of the property.
     * @throws RepositoryException
     */
    private Boolean isNodeEditableInContributeMode(JCRNodeWrapper node) throws RepositoryException {
        final boolean hasProperty = node.hasProperty(Constants.JAHIA_EDITABLE_IN_CONTRIBUTION);
        if (hasProperty) {
            return node.getProperty(Constants.JAHIA_EDITABLE_IN_CONTRIBUTION).getBoolean();
        } else {
            return null;
        }
    }

    protected void findNode(RenderContext renderContext, Resource currentResource) throws IOException {
        if (nodeName != null) {
            node = (JCRNodeWrapper) pageContext.findAttribute(nodeName);
        } else if (path != null && currentResource != null) {
            try {
                if (!path.startsWith("/")) {
                    JCRNodeWrapper nodeWrapper = currentResource.getNode();
                    if (!path.equals("*") && nodeWrapper.hasNode(path)) {
                        node = (JCRNodeWrapper) nodeWrapper.getNode(path);
                    } else {
                        missingResource(renderContext, currentResource);
                    }
                } else if (path.startsWith("/")) {
                    JCRSessionWrapper session = currentResource.getNode().getSession();
                    try {
                        node = (JCRNodeWrapper) session.getItem(path);
                    } catch (PathNotFoundException e) {
                        missingResource(renderContext, currentResource);
                    }
                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    protected String getConfiguration() {
        return Resource.CONFIGURATION_MODULE;
    }

    protected boolean checkNodeEditable(RenderContext renderContext, JCRNodeWrapper node) {
        try {
            if (node != null && !renderContext.isEditable(node)) {
                return false;
            }
        } catch (RepositoryException e) {
            logger.error("Failed to check if the node " + node.getPath() + " is editable.", e);
        }
        return true;
    }

    protected boolean canEdit(RenderContext renderContext) {
        return renderContext.isEditMode() && editable &&
                !Boolean.TRUE.equals(renderContext.getRequest().getAttribute("inWrapper")) &&
                renderContext.getRequest().getAttribute("inArea") == null;
    }

    protected void printModuleStart(String type, String path, String resolvedTemplate, Script script,
                                    String additionalParameters)
            throws RepositoryException, IOException {

        builder.append("<div class=\"jahia-template-gxt\" jahiatype=\"module\" ").append("id=\"module")
                .append(UUID.randomUUID().toString()).append("\" type=\"").append(type).append("\"");

        builder.append((script != null && script.getView().getInfo() != null) ? " scriptInfo=\"" + script.getView().getInfo() + "\"" : "");

        if (script != null && script.getView().getModule().getSourcesFolder() != null) {
            String version = script.getView().getModule().getIdWithVersion();
            builder.append(" sourceInfo=\"/modules/" + version + "/sources/src/main/resources" + StringUtils.substringAfter(script.getView().getPath(), "/modules/" + script.getView().getModule().getId()) + "\"");
        }

        builder.append(" path=\"").append(path != null && path.indexOf('"') != -1 ? Patterns.DOUBLE_QUOTE.matcher(path).replaceAll("&quot;") : path).append("\"");

        if (!StringUtils.isEmpty(nodeTypes)) {
            nodeTypes = StringUtils.join(Ordering.natural().sortedCopy(Arrays.asList(Patterns.SPACE.split(nodeTypes))),' ');
            builder.append(" nodetypes=\"" + nodeTypes + "\"");
        } else if (!StringUtils.isEmpty(constraints)) {
            constraints = StringUtils.join(Ordering.natural().sortedCopy(Arrays.asList(Patterns.SPACE.split(constraints))),' ');
            builder.append(" nodetypes=\"" + constraints + "\"");
        }

        if (listLimit > -1) {
            builder.append(" listlimit=\"" + listLimit + "\"");
        }

        if (!StringUtils.isEmpty(constraints)) {
            String referenceTypes = ConstraintsHelper.getReferenceTypes(constraints, nodeTypes);
            builder.append((!StringUtils.isEmpty(referenceTypes)) ? " referenceTypes=\"" + referenceTypes + "\"" : " referenceTypes=\"none\"");
        }

        if (additionalParameters != null) {
            builder.append(" ").append(additionalParameters);
        }

        builder.append("showAreaButton=\"").append(showAreaButton).append("\"");

        builder.append(">");

        printAndClean();
    }

    protected void printModuleEnd() throws IOException {
        builder.append("</div>");
        printAndClean();
    }

    private void printAndClean() throws IOException {
        if (var == null) {
            //JSP VERSION: pageContext.getOut().print(builder);
            this.helper.builder.append(builder.toString());
            builder.delete(0, builder.length());
        }
    }

    protected void render(RenderContext renderContext, Resource resource) throws IOException, RenderException {
        HttpServletRequest request = renderContext.getRequest();
        Boolean oldSkipAggregation = (Boolean) request.getAttribute(AggregateFilter.SKIP_AGGREGATION);
        try {
            JCRSiteNode previousSite = renderContext.getSite();
            if (contextSite != null) {
                renderContext.setSite(contextSite);
            }
            if (skipAggregation) {
                request.setAttribute(AggregateFilter.SKIP_AGGREGATION, skipAggregation);
                resource.getRegexpDependencies().add(resource.getNodePath() + "/.*");
            }
            builder.append(RenderService.getInstance().render(resource, renderContext));
            renderContext.setSite(previousSite);
            printAndClean();
        } catch (TemplateNotFoundException io) {
            builder.append(io);
            printAndClean();
        } catch (RenderException e) {
            if (renderContext.isEditMode() && ((e.getCause() instanceof TemplateNotFoundException) || (e.getCause() instanceof AccessDeniedException))) {
                if (!(e.getCause() instanceof AccessDeniedException)) {
                    logger.error(e.getMessage(), e);
                }
                builder.append(e.getCause().getMessage());
                printAndClean();
            } else {
                throw e;
            }
        } finally {
            request.setAttribute(AggregateFilter.SKIP_AGGREGATION, oldSkipAggregation);
        }
    }

    protected String getModuleType(RenderContext renderContext) throws RepositoryException {
        String type = "existingNode";
        if (node.isNodeType("jmix:listContent")) {
            type = "list";
        } else if (renderContext.getEditModeConfig().isForceHeaders()) {
            type = "existingNodeWithHeader";
        }
        return type;
    }

    protected void missingResource(RenderContext renderContext, Resource currentResource)
            throws RepositoryException, IOException {

        String currentPath = currentResource.getNode().getPath();
        if (path.startsWith(currentPath + "/") && path.substring(currentPath.length() + 1).indexOf('/') == -1) {
            currentResource.getMissingResources().add(path.substring(currentPath.length() + 1));
        } else if (!path.startsWith("/")) {
            currentResource.getMissingResources().add(path);
        }

        if (!"*".equals(path) && (path.indexOf("/") == -1)) {
            // we have a named path that is missing, let's see if we can figure out it's node type.
            constraints = ConstraintsHelper.getConstraints(currentResource.getNode(), path);
        }

        if (canEdit(renderContext) && checkNodeEditable(renderContext, currentResource.getNode()) && contributeAccess(renderContext, currentResource.getNode())) {
            if (currentResource.getNode().hasPermission("jcr:addChildNodes")) {
                List<String> contributeTypes = contributeTypes(renderContext, currentResource.getNode());
                if (contributeTypes != null) {
                    nodeTypes = StringUtils.join(contributeTypes, " ");
                }
                printModuleStart("placeholder", path, null, null, null);
                printModuleEnd();
            }
        }
    }

    @Override
    public void addParameter(String name, String value) {
        parameters.put(name, value);
    }

    @Deprecated
    public void setCheckConstraints(boolean checkConstraints) {
        // constraint are now resolved by JCRFilterTag when called by list jsp
        this.checkConstraints = checkConstraints;
    }
}
