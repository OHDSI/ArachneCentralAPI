package com.odysseusinc.arachne.portal.util;

import com.odysseusinc.arachne.portal.exception.ArachneSystemRuntimeException;
import org.apache.commons.io.IOUtils;

import java.nio.charset.StandardCharsets;

public class ResourcesUtils {

    private ResourcesUtils() {
    }

    public static String loadStringResource(String resourcePath) {

        try {
            return IOUtils.toString(ResourcesUtils.class.getResourceAsStream(resourcePath), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new ArachneSystemRuntimeException("Cannot load resource: " + resourcePath, e);
        }
    }

    public static byte[] loadResource(String resourcePath) {

        try {
            return IOUtils.toByteArray(ResourcesUtils.class.getResourceAsStream(resourcePath));
        } catch (Exception e) {
            throw new ArachneSystemRuntimeException("Cannot load resource: " + resourcePath, e);
        }
    }
}
