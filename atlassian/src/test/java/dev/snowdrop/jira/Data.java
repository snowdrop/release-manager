package dev.snowdrop.jira;

import java.util.Arrays;
import java.util.List;

public class Data {
    String name;

    public Data(String name) {
        this.name = name;
    }

    List<Item> items() {
        return Arrays.asList(
                new Item("Item 1", "$19.99", Arrays.asList(new Feature("New!"), new Feature("Awesome!"))),
                new Item("Item 2", "$29.99", Arrays.asList(new Feature("Old."), new Feature("Ugly.")))
        );
    }
}
