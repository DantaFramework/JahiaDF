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

package layerx.jahia.contextprocessors.extended;

import com.google.common.collect.Sets;
import layerx.api.ExecutionContext;
import layerx.api.exceptions.ProcessException;
import layerx.core.contextprocessors.AbstractCheckComponentCategoryContextProcessor;
import layerx.jahia.templating.TemplateContentModel;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.jahia.services.render.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import java.util.*;

import static layerx.Constants.*;
import static layerx.jahia.Constants.JAHIA_RESOURCE;

/**
 * Context processor to handle list children nodes
 *
 * @author      neozilon
 * @version     1.0.0
 * @since       2016-09-20
 */
@Component
@Service
public class ListChildrenNodeContextProcessor
        extends AbstractCheckComponentCategoryContextProcessor<TemplateContentModel> {

    private static Logger LOG = LoggerFactory.getLogger(ListChildrenNodeContextProcessor.class);

    private static final String ROOT_NODE_PATH = "childrenListLink";
    private static final String LIST_CONTENTE_KEY = "childrenList";

    @Override
    public Set<String> anyOf() {
        return Sets.newHashSet(CONTENT_CATEGORY);
    }

    @Override
    public int priority() {
        // This processor must be one of the first processors executed.
        return MEDIUM_PRIORITY;
    }

    @Override
    public void process(final ExecutionContext executionContext, final TemplateContentModel contentModel)throws ProcessException {

        Map<String, Object> childrenListMap = new HashMap<>();
        Resource resource = (Resource) executionContext.get(JAHIA_RESOURCE);
        if ( contentModel.has(RESOURCE_CONTENT_KEY) ){
            Object obj = contentModel.get(RESOURCE_CONTENT_KEY);

            if(obj instanceof Map ){
                Map<String,Object> contentMap = (Map) obj;
                if (contentMap.containsKey(ROOT_NODE_PATH)){
                    String path = (String)contentMap.get(ROOT_NODE_PATH);
                    try {
                        Session jcrSession = resource.getNode().getSession();
                        Node rootNode = jcrSession.getNode(path);
                        List<Map<String, String>> list = new LinkedList<>();

                        // TODO Fix this
                        if (rootNode.getPrimaryNodeType().toString().equals("jnt:page")) {
                            Map<String, String> values = this.getValuesFromNode(rootNode);
                            list.add(values);
                        }

                        if (rootNode != null && rootNode.hasNodes()) {
                            NodeIterator iterator = rootNode.getNodes();
                            while (iterator.hasNext()) {
                                Node n = iterator.nextNode();
                                if (n.getPrimaryNodeType().toString().equals("jnt:page")) {
                                    Map<String, String> values = this.getValuesFromNode(n);
                                    list.add(values);
                                }
                            }
                            LOG.info("SETTING CONTENT NODE: "+ list);
                            contentModel.set(LIST_CONTENTE_KEY, list);
                        }
                    }catch (Exception e){
                        LOG.error("Exception : ",e);
                    }

                }


            }else{
                LOG.info("NOT Instance of MAP ....");
            }
        }else{
            LOG.info("NOT has {} MAP "+RESOURCE_CONTENT_KEY);
        }
    }

    private Map<String,String> getValuesFromNode(Node n)throws Exception{

        Map<String, String> values = new HashMap<>();
        String nodePath = n.getPath();
        String nodeName = n.getName();

        // TODO fix how I'm getting the Node Name
        if (n.hasNode("j:translation_en")){
            Node translationNode = n.getNode("j:translation_en");
            nodeName = translationNode.getProperty("jcr:title").getString();
        }
        values.put("link", nodePath);
        values.put("name", nodeName);

        return values;
    }
}
