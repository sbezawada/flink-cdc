package com.netapp.test.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.StringJoiner;

@JsonIgnoreProperties
public class JsonObject {

    public Product before;
    public Product after;

    public JsonObject(Product before, Product after) {
        this.before = before;
        this.after = after;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", JsonObject.class.getSimpleName() + "[", "]")
                .add("before=" + before)
                .add("after=" + after)
                .toString();
    }
}
