package com.netapp.test.data;

import java.util.StringJoiner;

public class Product {
    public int id;
    public String name;
    public String description;
    public double weight;

    public Product(int id, String name, String description, double weight) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.weight = weight;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Product.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("name='" + name + "'")
                .add("description='" + description + "'")
                .add("weight=" + weight)
                .toString();
    }
}
