package com.pasenture.map;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Created by Jeon on 2017-07-09.
 */
public class NaverMapApiObject {

    @JsonProperty("userquery")
    private String userquery;

    @JsonProperty("total")
    private int total;

    @JsonProperty("items")
    private List<Map<String, String>> items;

    public String getUserquery() {
        return userquery;
    }

    public void setUserquery(String userquery) {
        this.userquery = userquery;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<Map<String, String>> getItems() {
        return items;
    }

    public void setItems(List<Map<String, String>> items) {
        this.items = items;
    }
}
