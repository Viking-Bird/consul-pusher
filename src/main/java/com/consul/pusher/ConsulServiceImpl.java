package com.consul.pusher;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 提供consul配置中心业务逻辑操作
 */
@Service
public final class ConsulServiceImpl implements ConsulService {

    private static final Logger log = LogManager.getLogger(ConsulServiceImpl.class);

    @Autowired
    private ConsulClient consulClient;

    @Value("#{'${spring.cloud.consul.config.prefix}/'.concat('${spring.cloud.consul.config.name}/')}")
    private String keyPrefix; // key所在的目录前缀，格式为：config/应用名称/

    @Override
    public void setKVValue(Map<String, String> kvValue) {
        for (Map.Entry<String, String> kv : kvValue.entrySet()) {
            try {
                this.consulClient.setKVValue(keyPrefix + kv.getKey(), kv.getValue());
            } catch (Exception e) {
                log.error("SetKVValue exception: {},kvValue: {}", e.getMessage(), kvValue);
            }
        }
    }

    @Override
    public void setKVValue(String key, String value) {
        try {
            this.consulClient.setKVValue(keyPrefix + key, value);
        } catch (Exception e) {
            log.error("SetKVValue exception: {},key: {},value: {}", e.getMessage(), key, value);
        }
    }

    @Override
    public Map<String, String> getKVValues(String keyPrefix) {
        Map<String, String> map = new HashMap<>();

        try {
            Response<List<GetValue>> response = this.consulClient.getKVValues(keyPrefix);
            if (response != null) {
                for (GetValue getValue : response.getValue()) {
                    int index = getValue.getKey().lastIndexOf("/") + 1;
                    String key = getValue.getKey().substring(index);
                    String value = getValue.getDecodedValue();
                    map.put(key, value);
                }
            }
            return map;
        } catch (Exception e) {
            log.error("GetKVValues exception: {},keyPrefix: {}", e.getMessage(), keyPrefix);
        }
        return null;
    }

    @Override
    public Map<String, String> getKVValues() {
        return this.getKVValues(keyPrefix);
    }

    @Override
    public List<String> getKVKeysOnly(String keyPrefix) {
        List<String> list = new ArrayList<>();
        try {
            Response<List<String>> response = this.consulClient.getKVKeysOnly(keyPrefix);

            if (response.getValue() != null) {
                for (String key : response.getValue()) {
                    int index = key.lastIndexOf("/") + 1;
                    String temp = key.substring(index);
                    list.add(temp);
                }
            }
            return list;
        } catch (Exception e) {
            log.error("GetKVKeysOnly exception: {},keyPrefix: {}", e.getMessage(), keyPrefix);
        }
        return null;
    }

    @Override
    public List<String> getKVKeysOnly() {
        return this.getKVKeysOnly(keyPrefix);
    }
}
