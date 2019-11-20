package com.odysseusinc.arachne.portal.service.analysis.heracles.parts;

import java.util.List;
import java.util.stream.Collectors;

public class HeraclesTestUtils {

    private HeraclesTestUtils() {
    }
    
    public static List<String> renameToSqlParameter(List<String> params) {

        return params.stream()
                .map(name -> '@' + name)
                .collect(Collectors.toList());
    }
}
