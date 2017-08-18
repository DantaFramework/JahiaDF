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

import layerx.jahia.templating.AbstractJahiaHelperFunction;
import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.TemplatePackageRegistry;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.jsp.tagext.BodyTagSupport;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Add resources tag to Jahia Templates Package
 *
 * @author      jarriola
 * @version     1.0.0
 * @since       2017-06-29
 */
public class AddResourcesTag extends BodyTagSupport {

    protected AbstractJahiaHelperFunction helper;
    public void setCallerHelper(AbstractJahiaHelperFunction helper) {
        this.helper = helper;
    }

    private transient static Logger logger = LoggerFactory.getLogger(AddResourcesTag.class);

    private boolean insert;
    private String type;
    private String resources;
    private String title;
    private String key;
    private String targetTag;
    private String rel;
    private String media;
    private String condition;

    /**
     * Default processing of the end tag returning EVAL_PAGE.
     *
     * @return EVAL_PAGE
     * @see org.jahia.taglibs.template.include.AddResourcesTag#doEndTag
     */
    @Override
    public int doEndTag() throws JspException {
        // JSP VERSION: org.jahia.services.render.Resource currentResource =
        //        (org.jahia.services.render.Resource) pageContext.getAttribute("currentResource", PageContext.REQUEST_SCOPE);
        Resource currentResource = (Resource) this.helper.getBindings().get("currentResource");

        boolean isVisible = true;

        RenderContext renderContext = (RenderContext) this.helper.getBindings().get("renderContext");
        try {
            isVisible = renderContext.getEditModeConfig() == null || renderContext.isVisible(currentResource.getNode());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        if (isVisible) {
            addResources(renderContext);
        }
        resetState();
        return super.doEndTag();
    }

    /**
     * Add resources
     *
     * @param renderContext This is a RenderContext
     * @return true or false
     */
    protected boolean addResources(RenderContext renderContext) {
        if (logger.isDebugEnabled()) {
            logger.debug("Site : " + renderContext.getSite() + " type : " + type + " resources : " + resources);
        }
        if (renderContext == null) {
            logger.warn("No render context found. Unable to add a resoure");
            return false;
        }

        final Map<String, String> mapping = getStaticAssetMapping();

        Set<String> strings = new LinkedHashSet<String>();
        for (String sourceResource : Patterns.COMMA.split(resources)) {
            String replacement = mapping.get(sourceResource);
            if (replacement != null) {
                for (String r : StringUtils.split(replacement, " ")) {
                    strings.add(r);
                }
            } else {
                strings.add(sourceResource);
            }
        }

        Set<JahiaTemplatesPackage> packages = new TreeSet<JahiaTemplatesPackage>(TemplatePackageRegistry.TEMPLATE_PACKAGE_COMPARATOR);
        final JCRSiteNode site = renderContext.getSite();
        JahiaTemplateManagerService templateManagerService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();
        if (site.getPath().startsWith("/sites/")) {
            for (String s : site.getInstalledModulesWithAllDependencies()) {
                final JahiaTemplatesPackage templatePackageById = templateManagerService.getTemplatePackageById(s);
                if (templatePackageById != null) {
                    packages.add(templatePackageById);
                }
            }
        } else if (site.getPath().startsWith("/modules/")) {
            JahiaTemplatesPackage aPackage = templateManagerService.getTemplatePackageById(site.getName());
            if (aPackage != null) {
                packages.add(aPackage);
                for (JahiaTemplatesPackage depend : aPackage.getDependencies()) {
                    if (!packages.contains(depend)) {
                        packages.add(depend);
                    }
                }
            }

        }

        for (String resource : strings) {
            resource = resource.trim();
            if (resource.startsWith("/") || resource.startsWith("http://") || resource.startsWith("https://")) {
                writeResourceTag(type, resource, resource);
            } else {
                String relativeResourcePath = "/" + type + "/" + resource;
                for (JahiaTemplatesPackage pack : packages) {
                    if (pack.resourceExists(relativeResourcePath)) {
                        // we found it
                        String path = pack.getRootFolderPath() + relativeResourcePath;
                        String contextPath = renderContext.getRequest().getContextPath();
                        String pathWithContext = contextPath.isEmpty() ? path : contextPath + path;

                        // apply mapping
                        String mappedPath = mapping.get(path);
                        if (mappedPath != null) {
                            for (String mappedResource : StringUtils.split(mappedPath, " ")) {
                                path = mappedResource;
                                pathWithContext = !path.startsWith("http://") && !path.startsWith("https://") ? (contextPath
                                        .isEmpty() ? path : contextPath + path) : path;
                                writeResourceTag(type, pathWithContext, resource);
                            }
                        } else {
                            writeResourceTag(type, pathWithContext, resource);
                        }

                        break;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Set type
     *
     * @param type This is a resource type
     */
    public void setType(String type) {
        this.type = type != null ? type.toLowerCase() : null;
    }

    /**
     * Set Resource
     *
     * @param resources The resources
     */
    public void setResources(String resources) {
        this.resources = resources;
    }

    /**
     * Set insert
     *
     * @param insert
     */
    public void setInsert(boolean insert) {
        this.insert = insert;
    }

    /**
     * Set target tag
     *
     * @param targetTag
     */
    public void setTargetTag(String targetTag) {
        this.targetTag = targetTag;
    }

    /**
     * Set rel attribute
     *
     * @param rel
     */
    public void setRel(String rel) {
        this.rel = rel;
    }

    /**
     * Set media attribute
     *
     * @param media
     */
    public void setMedia(String media) {
        this.media = media;
    }

    /**
     * Set condition attribute
     *
     * @param condition
     */
    public void setCondition(String condition) {
        this.condition = condition;
    }

    /**
     * Reset state
     */
    protected void resetState() {
        insert = false;
        resources = null;
        type = null;
        title = null;
        targetTag = null;
        rel = null;
        media = null;
        condition = null;
        key = null;
    }

    @SuppressWarnings("unchecked")
    protected Map<String, String> getStaticAssetMapping() {
        return (Map<String, String>) SpringContextSingleton.getBean(
                "org.jahia.services.render.StaticAssetMappingRegistry");
    }

    /**
     * Create resource tag
     *
     * @param type This is a type of the resource tag
     * @param path This is a path for the resource tag
     * @param resource This is the resource
     */
    private void writeResourceTag(String type, String path, String resource) {
        StringBuilder builder = new StringBuilder();
        builder.append("<jahia:resource type=\"");
        builder.append(type != null ? type : "").append("\"");
        boolean isTypeInline = StringUtils.equals(type,"inline");
        if (!isTypeInline) {
            try {
                builder.append(" path=\"").append(URLEncoder.encode(path != null ? path : "", "UTF-8")).append("\"");
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
            }
        }
        builder.append(" insert=\"").append(insert).append("\"");
        if (targetTag != null) {
            builder.append(" targetTag=\"").append(targetTag).append("\"");
        }
        if (rel != null) {
            builder.append(" rel=\"").append(rel).append("\"");
        }
        if (media != null) {
            builder.append(" media=\"").append(media).append("\"");
        }
        if (condition != null) {
            builder.append(" condition=\"").append(condition).append("\"");
        }
        builder.append(" resource=\"").append(resource != null ? resource : "").append("\"");
        builder.append(" title=\"").append(title != null ? title : "").append("\"");
        builder.append(" key=\"").append(key != null ? key : "").append("\"");
        if (!isTypeInline) {
            builder.append(" />\n");
        } else {
            builder.append(">");
            builder.append(path);
            builder.append("</jahia:resource>\n");
        }
        /* JSP VERSION: try {
            pageContext.getOut().print(builder.toString());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } */
        this.helper.builder.append(builder.toString());
    }
}
