package dev.snowdrop.jira;

import java.util.List;

class Item {
    Item(String name, String price, List<Feature> features) {
        this.name = name;
        this.price = price;
        this.features = features;
    }

    String name, price;
    List<Feature> features;
}
