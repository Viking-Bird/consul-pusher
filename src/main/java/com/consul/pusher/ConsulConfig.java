package com.consul.pusher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cfg4j.source.context.propertiesprovider.*;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 提供加载配置到consul中的操作
 * <p>
 * Created by dapeng
 * Date: 2018/6/4
 * Time: 14:42
 */
@Component
public class ConsulConfig {

    private static final Logger log = LogManager.getLogger(ConsulConfig.class);

    @Autowired
    private ConsulService consulServiceImpl;

    @Value("${spring.cloud.consul.config.cover: false}") // 默认不覆盖, 覆盖: true / 不覆盖: false
    private Boolean cover;


    private Map<String, Object> formatMap(Map<String, Object> map) {
        Map<String, Object> newMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue().getClass() == LinkedHashMap.class) {
                Map<String, Object> subMap = formatMap((Map<String, Object>) entry.getValue());
                newMap.put(entry.getKey(), subMap);
            } else if (entry.getValue().getClass() == ArrayList.class) {
                JSONArray jsonArray = new JSONArray((ArrayList) entry.getValue());
                newMap.put(entry.getKey(), jsonArray);
            } else {
                newMap.put(entry.getKey(), entry.getValue().toString());
            }
        }
        return newMap;
    }

    private void visitProps(String key, Object value, List<String> keyList) {
        if (value.getClass() == String.class || value.getClass() == JSONArray.class) {
            if (cover) {  // 覆盖
                this.consulServiceImpl.setKVValue(key, value.toString());
            } else { // 不覆盖
                if (keyList != null && !keyList.contains(key)) {
                    this.consulServiceImpl.setKVValue(key, value.toString());
                }
            }
        } else if (value.getClass() == LinkedHashMap.class) {
            Map<String, Object> map = (LinkedHashMap) value;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                visitProps(key + "." + entry.getKey(), entry.getValue(), keyList);
            }
        } else if (value.getClass() == HashMap.class) {
            Map<String, Object> map = (HashMap) value;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                visitProps(key + "." + entry.getKey(), entry.getValue(), keyList);
            }
        }
    }

    private Map<String, Object> paserYml(InputStream inputStream) {
        Map<String, Object> newMap = new HashMap<>();
        try {
            Yaml yaml = new Yaml();
            Map map = (Map) yaml.load(inputStream);
            newMap = formatMap(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newMap;
    }

    @PostConstruct
    private void init() {
        PropertiesProviderSelector propertiesProviderSelector = new PropertiesProviderSelector(
                new PropertyBasedPropertiesProvider(), new YamlBasedPropertiesProvider(), new JsonBasedPropertiesProvider()
        );

        ClassPathResource resource = new ClassPathResource("application.yml");
        String fileName = resource.getFilename();
        String path = null;
        try (InputStream input = resource.getInputStream()) {
            log.info("Found config file: " + resource.getFilename() + " in context " + resource.getURL().getPath());
            path = resource.getURL().getPath();
            List<String> keyList = this.consulServiceImpl.getKVKeysOnly();
            log.info("Found keys : {}", keyList);
            Map<String, Object> props = new HashMap<>();

            if (fileName.endsWith(".properties")) {
                PropertiesProvider provider = propertiesProviderSelector.getProvider(fileName);
                props = (Map) provider.getProperties(input);

            } else if (fileName.endsWith(".yml")) {
                props = paserYml(resource.getInputStream());
            }
            for (Map.Entry<String, Object> prop : props.entrySet()) {
                visitProps(prop.getKey(), prop.getValue(), keyList);
            }
        } catch (IOException e) {
            log.warn("Unable to load properties from file: {},message: {} ", path, e.getMessage());
        }
    }

}
