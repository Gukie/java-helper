package com.lokia.helper;

import java.util.*;

public class CollectionHelper {

    public static <T> boolean isEquals(Collection<T> first, Collection<T> second) {
        if (first == null && second == null) {
            return true;
        }
        if (first == null || second == null) {
            return false;
        }
        if (first.size() != second.size()) {
            return false;
        }

        List<T> list1 = new ArrayList<>(first);
        List<T> list2 = new ArrayList<>(second);

        list1.sort(null);
        list2.sort(null);

        boolean isEquals = true;
        int i = 0;
        for (T item : list1) {
            if (!Objects.equals(item, list2.get(i))) {
                isEquals = false;
                break;
            }
            i++;
        }
        return isEquals;
    }

    public static <T> boolean isEmpty(Collection<T> col){
        return col == null || col.isEmpty();
    }
}
