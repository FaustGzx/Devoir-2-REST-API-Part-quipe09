// src/main/java/com/diro/ift2255/util/ResponseUtil.java
package com.diro.ift2255.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class ResponseUtil {

    public static Map<String, Object> ok(Object data) {
        return Map.of(
                "success", true,
                "data", data
        );
    }

    /**
     * Réponse OK avec un message informatif (ex: pour liste vide)
     */
    public static Map<String, Object> ok(Object data, String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("data", data);
        result.put("message", message);
        return result;
    }

    public static Map<String, Object> error(String message) {
        return Map.of(
                "success", false,
                "error", message
        );
    }

    // Compatibilité si déjà utilisé dans ton code
    public static Map<String, Object> formatError(String errorMessage) {
        return error(errorMessage);
    }
}
