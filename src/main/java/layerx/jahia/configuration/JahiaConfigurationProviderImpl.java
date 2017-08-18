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

package layerx.jahia.configuration;


import layerx.api.configuration.Configuration;
import layerx.api.configuration.ConfigurationProvider;
import layerx.api.configuration.Mode;
import layerx.jahia.util.JahiaUtils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONStyle;
import net.minidev.json.JSONValue;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.render.Resource;
import org.jahia.services.render.View;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static layerx.Constants.*;
import static layerx.jahia.Constants.LOG_PRE;

/**
 * Configuration (xk config) provider implementation for AEM
 *
 * @author      neozilon
 * @version     1.0.0
 * @since       2017-08-10
 */
@Component(immediate = true)
@Service({ConfigurationProvider.class, BundleListener.class})
public class JahiaConfigurationProviderImpl implements ConfigurationProvider<Object>, BundleListener {

    private static Logger LOG = LoggerFactory.getLogger(JahiaConfigurationProviderImpl.class);

    private static final Mode DEFAULT_MODE = Mode.INHERIT; // TODO: Make configurable COMBINE (Duplicates) MERGE: ( UNIQUE_VALUES NO REPEATED VALUES)

    /**
     * Check if the given resource type has configuration
     *
     * @param obj The object to check if it has any configuration node
     * @return true if exists; else false
     * @throws Exception
     */
    @Override
    public boolean hasConfig(Object obj)throws Exception{
        boolean response = false;
        if (obj instanceof Resource){
            Resource resource = (Resource) obj;
            response = this.hasConfig(resource);
        }else if (obj instanceof View){
            View view = (View) obj;
            response = this.hasConfig(view);
        }
        return response;
    }

    /**
     * Get configuration for the given resource type
     *
     * @param obj The object to fetch the configuration from
     * @return configuration The configuration object
     * @throws Exception
     */
    @Override
    public Configuration getFor(Object obj)
            throws Exception {
        Configuration configuration = null;
        if (obj instanceof Resource){
            Resource resource = (Resource) obj;
            configuration = new ConfigurationImpl(resource);
        }else if (obj instanceof View){
            View view = (View) obj;
            configuration = new ConfigurationImpl(view);
        }
        return configuration;
    }

    /**
     * Check if the given resource type has configuration
     *
     * @param resourceType The resource type to check if it has any configuration node
     * @return true if exists; else false
     * @throws Exception
     */
    public boolean hasConfig(Resource resourceType)throws Exception{
        boolean hasConfigNode = false;

        // TODO add CACHE LAYER TO this , even for items not found!

        if (resourceType != null && resourceType.getNode() != null && resourceType.getNode().getPrimaryNodeType() != null) {
            ExtendedNodeType extendedNodeType = resourceType.getNode().getPrimaryNodeType();
            Map<String, ExtendedNodeType> map = getAllNodeTypesForNode(extendedNodeType, new LinkedHashMap<String, ExtendedNodeType>());
            for (String keyMap : map.keySet()) {
                ExtendedNodeType nodeTypeToEvaluate = map.get(keyMap);
                if ( JahiaUtils.hasXKConfigNode(nodeTypeToEvaluate) ){
                    hasConfigNode = true;
                }
            }
        }
        return hasConfigNode;
    }

    /**
     * Check if the given resource type has configuration
     *
     * @param view The view to check if it has any configuration node
     * @return true if exists; else false
     * @throws Exception
     */
    public boolean hasConfig(View view)throws Exception{
        boolean hasConfigNode = false;
        // TODO add CACHE LAYER TO this , even for items not found!
        if (view != null) {
            if ( JahiaUtils.hasXKConfigNode(view) ){
                hasConfigNode = true;
            }
        }
        return hasConfigNode;
    }

    /**
     * Get configuration for the given resource type
     *
     * @param resource The resource type to fetch the configuration from
     * @return configuration The configuration object
     * @throws Exception
     */
    public Configuration getFor(Resource resource)
            throws Exception {
        return new ConfigurationImpl(resource);
    }

    private final Map<String, Map<String, Map<String, InertProperty>>> configCache = new HashMap<>();

    private void resetCache(){
        LOG.debug(LOG_PRE + "Clearing Configuration Cache with {} items cached: ",configCache.size());
        configCache.clear();
    }

    private Map<String,ExtendedNodeType> getAllNodeTypesForNode(ExtendedNodeType nodeType, Map<String,ExtendedNodeType> map){
        if (!map.containsKey(nodeType.getName())){
            map.put(nodeType.getName(), nodeType);
        }
        ExtendedNodeType[] extendedNodeTypesArray = nodeType.getPrimarySupertypes();
        Map<String,ExtendedNodeType> superTypesArray = new HashMap<>();

        for(ExtendedNodeType nodeType1 : extendedNodeTypesArray){
            if (!map.containsKey(nodeType1.getName())){
                map.put(nodeType1.getName(), nodeType1);
                superTypesArray.put(nodeType1.getName(), nodeType1);
            }
        }
        for(ExtendedNodeType nodeType1 : extendedNodeTypesArray){
            if (!map.containsKey(nodeType1.getName()) || (map.containsKey(nodeType1.getName()) && superTypesArray.containsKey(nodeType1.getName())) ){
                map = getAllNodeTypesForNode(nodeType1, map);
            }
        }
        return map;
    }

    /**
     * Inner class: Configuration implementer
     */
    private class ConfigurationImpl implements Configuration {

        private Resource resource;
        private Bundle bundle;
        private View view;

        private static final boolean USE_CACHE_FOR_CONFIGURATION = false; // TODO add this value to OSGi configuration

        private Map<String, Map<String, InertProperty>> configMembers = new LinkedHashMap<>();

        private Set<String> propNamesDeepCache = Collections.emptySet();

        private ConfigurationImpl(Object resourceType) throws Exception {
            if (resourceType instanceof Resource){
                this.resource = (Resource) resourceType;
                this.bundle = JahiaUtils.getBundle(resource);
                this.view = null;
                loadConfigHierarchy();
            }else if (resourceType instanceof View){
                this.resource = null;
                this.view = (View) resourceType;
                this.bundle = JahiaUtils.getBundle(view);
                loadConfigHierarchyFromView();
            }
        }

        @Override
        public Mode defaultMode() {
            return DEFAULT_MODE;
        }

        private void loadConfigHierarchy() throws Exception {
            if (resource != null && bundle != null) {
                String key = resource.getPath();

                if (USE_CACHE_FOR_CONFIGURATION && configCache.containsKey(key)) {
                    configMembers = configCache.get(key);
                } else {
                    ExtendedNodeType extendedNodeType = resource.getNode().getPrimaryNodeType();
                    Map<String, ExtendedNodeType> map = getAllNodeTypesForNode(extendedNodeType, new LinkedHashMap<String, ExtendedNodeType>());
                    if ( map.size() > 0 ){
                        for (String keyMap : map.keySet()){
                            ExtendedNodeType nodeTypeToEvaluate = map.get(keyMap);

                            JSONObject xkConfigJSONObj = getJSON( nodeTypeToEvaluate );
                            if (xkConfigJSONObj != null) {
                                Map<String, InertProperty> propsMap = new LinkedHashMap<>();
                                for (String jsonKey : xkConfigJSONObj.keySet()) {
                                    Object object = xkConfigJSONObj.get(jsonKey);
                                    InertProperty property = new InertProperty(jsonKey, object);
                                    propsMap.put(jsonKey, property);
                                }
                                configMembers.put(keyMap, propsMap);
                            } else {
                                // TODO review if LOG is needed here or not.
                                // LOG.error(LOG_PRE + "Not able to load file into JSON Object: {}, check your configuration node",nodeTypeToEvaluate);
                            }
                        }
                        if (USE_CACHE_FOR_CONFIGURATION) {
                            configCache.put(key, configMembers);
                        }
                    }
                }
            }
            propNamesDeepCache = names(false);
        }

        private void loadConfigHierarchyFromView() throws Exception {
            if (view != null && bundle != null) {
                String key = view.getPath();

                if (!configCache.containsKey(key)) {
                    JSONObject xkConfigJSONObj = getJSON( view );
                    if (xkConfigJSONObj != null) {

                        // LOAD XK CONFIG FROM A SUBSET OF A XK CONFIG KEY WITH THE VALUE OF ( VIEW KEY )
                        // SPECIAL SCENARIO
                        if (xkConfigJSONObj.containsKey(view.getKey())){
                            Object obj = xkConfigJSONObj.get(view.getKey());
                            if (obj instanceof JSONObject){
                                xkConfigJSONObj = (JSONObject) xkConfigJSONObj.get(view.getKey());
                            }else{
                                LOG.error("Check your XK-Config file for your Template, the View Key Value is not a JSON Object");
                            }
                        }

                        Map<String, InertProperty> propsMap = new LinkedHashMap<>();
                        for (String jsonKey : xkConfigJSONObj.keySet()) {
                            Object object = xkConfigJSONObj.get(jsonKey);
                            InertProperty property = new InertProperty(jsonKey, object);
                            propsMap.put(jsonKey, property);
                        }
                        String keyForMap = view.getKey().replaceAll("\\s+","");
                        configMembers.put( keyForMap, propsMap);
                    } else {
                        LOG.error(LOG_PRE + "Not able to load file into JSON Object: {}, check your configuration node for view: ",view.getPath());
                    }
                    configCache.put(key, configMembers);
                } else {
                    configMembers = configCache.get(key);
                }
            }
            propNamesDeepCache = names(false);
        }

        private Set<String> names(boolean shallow) {
            Set<String> names = new HashSet<>();
            for (String configMemberPath : configMembers.keySet()) {
                for (String propName : configMembers.get(configMemberPath).keySet()) { // TODO Review @ RUNTIME
                    names.add(propName);
                }
                if (shallow)
                    break;
            }
            return names;
        }

        @Override
        public Set<String> names(Mode mode)
                throws Exception {
            return (mode == Mode.SHALLOW) ? names(true) : propNamesDeepCache;
        }

        @Override
        public Set<String> names()
                throws Exception {
            return names(defaultMode());
        }

        private Collection<Object> valuesFor(String paramName, Mode mode)
                throws Exception {

            Collection<Object> values = Collections.emptyList();

            switch (mode) {
                case INHERIT:
                    values = getInherited(paramName);
                    break;
                case MERGE:
                    values = getMerged(paramName);
                    break;
                case COMBINE:
                    values = getCombined(paramName);
                    break;
                case SHALLOW:
                    values = getShallow(paramName);
                    break;
                default:
                    break;
            }
            return values;
        }

        private Collection<Object> getInherited(String paramName)
                throws Exception {
            Collection<Object> values = Collections.emptyList();
            for (String memberComp : configMembers.keySet()) {
                Map<String, InertProperty> props = configMembers.get(memberComp);
                if (props.containsKey(paramName)) {
                    values = toCollection(props.get(paramName));
                    break;
                }
            }
            return values;
        }

        private Collection<Object> getMerged(String paramName)
                throws Exception {
            Collection<Object> values = new ArrayList<>();
            for (String memberComp : configMembers.keySet()) {
                Map<String, InertProperty> props = configMembers.get(memberComp);
                if (props.containsKey(paramName)) {
                    for (Object object : toCollection(props.get(paramName))) {
                        if (!values.contains(object)) {
                            values.add(object);
                        }
                    }
                }
            }
            return values;
        }

        private Collection<Object> getCombined(String paramName)
                throws Exception {
            Collection<Object> values = new ArrayList<>();
            for (String memberComp : configMembers.keySet()) {
                Map<String, InertProperty> props = configMembers.get(memberComp);
                if (props.containsKey(paramName)) {
                    values.addAll(toCollection(props.get(paramName)));
                }
            }
            return values;
        }

        private Collection<Object> getShallow(String paramName)
                throws Exception {
            Collection<Object> values = Collections.emptyList();
            for (String memberComp : configMembers.keySet()) {
                Map<String, InertProperty> props = configMembers.get(memberComp);
                if (props.containsKey(paramName)) {
                    values = toCollection(props.get(paramName));
                }
                break;
            }
            return values;
        }

        private Collection<Object> toCollection(InertProperty property)
                throws Exception {
            return property.values();
        }


        public Map<String, Object> distilledMap(Mode mode, boolean flatten)
                throws Exception {
            Map<String, Object> distilledMap = new HashMap<>();
            for (String paramName : names(mode)) {
                List<Object> objs = new ArrayList<>();
                for (Object object : valuesFor(paramName, mode)) {
                    objs.add(object); // TODO fix original: JcrResourceUtil.toJavaObject(value)
                }
                Object distilledValue;
                switch (objs.size()) {
                    case 0:
                        distilledValue = (flatten) ? null : Collections.emptySet();
                        break;
                    case 1:
                        if (flatten) {
                            distilledValue = objs.get(0);
                            break;
                        }
                    default:
                        distilledValue = objs;
                }
                distilledMap.put(paramName, distilledValue);
            }
            return distilledMap;
        }

        public Map<String, Object> distilledMap(Mode mode)
                throws Exception {
            return distilledMap(mode, true);
        }

        public Map<String, Object> distilledMap()
                throws Exception {
            return distilledMap(defaultMode());
        }

        @Override
        public String asString(String paramName, Mode mode)
                throws Exception {
            Collection<Object> collection = valuesFor(paramName, mode);
            if(collection.size() > 0) {
                Object obj = collection.iterator().next();
                if (obj instanceof String) {
                    String value = (String) obj;

                    return value;
                }
            }

            return BLANK;
        }

        @Override
        public List<String> asStrings(String paramName, Mode mode) throws Exception {
            Collection<Object> collection = valuesFor(paramName, mode);
            List<String> list = new LinkedList<>();
            for (Object obj : collection) {
                if (obj instanceof JSONArray) {
                    JSONArray jsonArray = (JSONArray) obj;
                    for (int i = 0; i < jsonArray.size(); i++) {
                        Object objFinal = jsonArray.get(i);
                        if (objFinal instanceof String) {
                            String value = (String) objFinal;
                            list.add(value);
                        }
                    }
                }
            }
            return list;
        }

        @Override
        public String asString(String paramName) throws Exception {
            return asString(paramName, defaultMode());
        }

        @Override
        public List<String> asStrings(String paramName) throws Exception {
            return asStrings(paramName, defaultMode());
        }

        @Override
        public Number asNumber(String paramName, Mode mode) throws Exception {
            List<Number> numbers = asNumbers(paramName, mode);
            return numbers.isEmpty() ? 0 : numbers.get(0);
        }

        @Override
        public List<Number> asNumbers(String paramName, Mode mode) throws Exception {
            Collection<Object> collection = valuesFor(paramName, mode);
            List<Number> list = new LinkedList<>();
            for (Object obj : collection) {
                if (obj instanceof Number) {
                    Number value = (Number) obj;
                    list.add(value);
                }
            }
            return list;
        }

        @Override
        public Number asNumber(String paramName) throws Exception {
            return asNumber(paramName, defaultMode());
        }

        @Override
        public Collection<Number> asNumbers(String paramName) throws Exception {
            return asNumbers(paramName, defaultMode());
        }

        @Override
        public Date asDate(String paramName, Mode mode) throws Exception {
            List<Date> dates = asDates(paramName, mode);
            return dates.isEmpty() ? Calendar.getInstance().getTime() : dates.get(0);
        }

        @Override
        public List<Date> asDates(String paramName, Mode mode) throws Exception {
            Collection<Object> collection = valuesFor(paramName, mode);
            List<Date> list = new LinkedList<>();
            for (Object obj : collection) {
                if (obj instanceof Date) {
                    Date value = (Date) obj;
                    list.add(value);
                }
            }
            return list;
        }

        @Override
        public Date asDate(String paramName)
                throws Exception {
            return asDate(paramName, defaultMode());
        }

        @Override
        public Collection<Date> asDates(String paramName)
                throws Exception {
            return asDates(paramName, defaultMode());
        }

        @Override
        public String toString() {
            try {
                return new JSONObject(distilledMap()).toJSONString();
            } catch (Exception ew) {
                LOG.error(ERROR, ew);
            }
            return null;
        }

        public String toJSONString()
                throws Exception {
            return toJSONString(JSONStyle.NO_COMPRESS);
        }

        @Override
        public String toJSONString(JSONStyle style)
                throws Exception {
            JSONObject obj = new JSONObject();
            try {
                obj = new JSONObject(distilledMap());
            } catch (Exception ew) {
                LOG.error(ERROR, ew);
            }
            return obj.toJSONString(style);
        }


        public Map<String, Object> toMap()
                throws Exception {
            return distilledMap();
        }


        public JSONObject toJSONObject()
                throws Exception {
            return new JSONObject(distilledMap());
        }

        private JSONObject getJSON(Resource r) {
            JSONObject jsonObject = null;
            if (r != null) {
                String jsonString = JahiaUtils.getStringFromResource(r);
                if (jsonString != null && jsonString.trim().length() > 0) {
                    Object object = JSONValue.parse(jsonString);
                    if (object != null) {
                        jsonObject = (JSONObject) object;
                    }
                }
            }
            return jsonObject;
        }

        private JSONObject getJSON(ExtendedNodeType extendedNodeType) {
            JSONObject jsonObject = null;
            if (extendedNodeType != null) {
                String jsonString = JahiaUtils.getStringFromNodeType(extendedNodeType);
                if (jsonString != null && jsonString.trim().length() > 0) {
                    Object object = JSONValue.parse(jsonString);
                    if (object != null) {
                        jsonObject = (JSONObject) object;
                    }
                }
            }
            return jsonObject;
        }

        private JSONObject getJSON(View view) {
            JSONObject jsonObject = null;
            if (view != null) {
                String jsonString = JahiaUtils.getXKConfigStringFromView(view);
                if (jsonString != null && jsonString.trim().length() > 0) {
                    Object object = JSONValue.parse(jsonString);
                    if (object != null) {
                        jsonObject = (JSONObject) object;
                    }
                }
            }
            return jsonObject;
        }
    }

    /**
     * Inner class: InertProperty
     */
    private class InertProperty {

        private final String name;
        private final List<Object> values;

        private InertProperty(String key, Object object){
            name = key;
            values = new LinkedList<Object>();
            values.add(object);
        }

        private String name() {
            return name;
        }

        private List<Object> values() {
            return values;
        }
    }

    /**
     * Clear config cache
     *
     * @param event
     */
    @Override
    public void bundleChanged(BundleEvent event){
        this.resetCache();
    }

    /**
     * Activate component
     *
     * @param context
     */
    @Activate
    public void activate(BundleContext context){
        context.addBundleListener(this);
    }
}
