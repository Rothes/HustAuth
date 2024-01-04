package io.github.rothes.hustauth.util;

import java.util.List;

public class CollectionUtils {

    public static <T> T getLast(List<T> collection) {
        return collection.get(collection.size() - 1);
    }

}
