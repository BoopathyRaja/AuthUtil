package com.br.authutil.data;

import java.util.HashMap;
import java.util.Map;

public class AuthData {

    private Map<String, Object> data;

    public AuthData() {
        this.data = new HashMap<>();
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public void add(String key, Object value) {
        this.data.put(key, value);
    }

    public void remove(String key) {
        this.data.remove(key);
    }
}
