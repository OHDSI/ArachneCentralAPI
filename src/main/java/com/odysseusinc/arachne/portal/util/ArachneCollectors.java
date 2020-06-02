package com.odysseusinc.arachne.portal.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.stream.Collector;

public final class ArachneCollectors {

    private ArachneCollectors() {
    }

    public static Collector<JsonElement, ?, JsonArray> toJsonArray() {

        return Collector.of(JsonArray::new, JsonArray::add, (l, r) -> {
            l.addAll(r);
            return l;
        });
    }

}
