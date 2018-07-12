package com.lokia.helper;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.*;

public class ObjectHelper {
    private static final String STR_DOT = ".";
    private static Set<Class<?>> BASIC_CLASS_SET = new HashSet<>();
    private static Set<Class<?>> COLLECTION_CLASS_SET = new HashSet<>();

    static {
        BASIC_CLASS_SET.add(String.class);
        BASIC_CLASS_SET.add(Long.TYPE);
        BASIC_CLASS_SET.add(Integer.TYPE);
        BASIC_CLASS_SET.add(Boolean.TYPE);
        BASIC_CLASS_SET.add(Short.TYPE);
        BASIC_CLASS_SET.add(Double.TYPE);
        BASIC_CLASS_SET.add(Float.TYPE);
        BASIC_CLASS_SET.add(Long.class);
        BASIC_CLASS_SET.add(Integer.class);
        BASIC_CLASS_SET.add(Boolean.class);
        BASIC_CLASS_SET.add(Short.class);
        BASIC_CLASS_SET.add(Double.class);
        BASIC_CLASS_SET.add(Float.class);
        BASIC_CLASS_SET.add(Object.class);

        // COLLECTION
        COLLECTION_CLASS_SET.add(Set.class);
        COLLECTION_CLASS_SET.add(List.class);

    }

    protected <T> boolean isEquals(T expected, T actual, String... ignoredProps) {
        if (actual == null && expected == null) {
            return true;
        }

        if (actual == null || expected == null) {
            return false;
        }

        if (isBasicClass(actual.getClass())) {
            return Objects.deepEquals(actual, expected);
        }

        Set<String> currentIgnoredProps = getCurrentIgnoredProps(ignoredProps);
        Field[] fields = actual.getClass().getDeclaredFields();
        boolean equals = true;
        for (Field actualItem : fields) {
            try {

                actualItem.setAccessible(true);
                Object actualFieldVal = actualItem.get(actual);
                String fieldName = actualItem.getName();
                if ("serialVersionUID".equals(fieldName) || currentIgnoredProps.contains(fieldName)) {
                    continue;
                }

                String[] nextIgnoredProps = getNextIgnoredProps(fieldName,ignoredProps);
                Field expectedItem = expected.getClass().getDeclaredField(fieldName);
                expectedItem.setAccessible(true);
                Object expectedFieldVal = expectedItem.get(expected);

                if (isBasicClass(actualItem.getType())) {
                    if (!Objects.deepEquals(actualFieldVal, expectedFieldVal)) {
                        equals = false;
                    }
                } else if (isMapClass(actualItem.getType())) {
                    if (!isMapEquals(actualFieldVal, expectedFieldVal, nextIgnoredProps)) {
                        equals = false;
                    }
                } else if (isCollectionClass(actualItem.getType())) {
                    if (!isCollectionObjEquals(actualFieldVal, expectedFieldVal, nextIgnoredProps)) {
                        equals = false;
                    }
                } else { // other complicated type.
                    if (!isEquals(actualFieldVal, expectedFieldVal, nextIgnoredProps)) {
                        equals = false;
                    }
                }

                if (!equals) {
                    break;
                }

            } catch (NoSuchFieldException | SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return equals;
    }

    private static boolean isBasicClass(Class<?> cls) {
        return BASIC_CLASS_SET.contains(cls);
    }

    /**
     * 取"."之前的值
     *
     * @param ignoredProps
     *            形式为"xxx.xxx"或者"xxx"
     * @return
     */
    private Set<String> getCurrentIgnoredProps(String[] ignoredProps) {
        Set<String> result = new HashSet<>();
        if (ignoredProps == null || ignoredProps.length < 1) {
            return result;
        }

        for (String ignoreProp : ignoredProps) {
            int index = ignoreProp.indexOf(STR_DOT);
            if(index == 0){
                continue;
            }
            if (index < 0) {
                result.add(ignoreProp);
            } else{
                String currIgnoredProp = ignoreProp.substring(0, index);
                result.add(currIgnoredProp);
            }

        }
        return result;
    }

    /**
     * 取"."之后的值
     *
     * @param prefix
     * @param ignoredProps   形式为"xxx.xxx"或者"xxx"
     * @return
     */
    private String[] getNextIgnoredProps(String prefix, String[] ignoredProps) {
        List<String> result = new ArrayList<>();
        if (ignoredProps == null || ignoredProps.length < 1 || StringUtils.isBlank(prefix)) {
            return null;
        }

        String keyword = prefix+STR_DOT;
        for (String ignoreItem : ignoredProps) {
            int index = ignoreItem.indexOf(keyword);
            if (index < 0 ) {
                continue;
            }
            String nextIgnoredProp = ignoreItem.substring(index + keyword.length());
            result.add(nextIgnoredProp);
        }

        return CollectionHelper.isEmpty(result) ? null : result.toArray(new String[] {});
    }

    private boolean isMapClass(Class<?> fieldCls) {
        return Map.class == fieldCls;
    }

    private static boolean isCollectionClass(Class<?> cls) {
        return COLLECTION_CLASS_SET.contains(cls);
    }

    private boolean isMapEquals(Object actualFieldVal, Object expectedFieldVal, String... ignoredProps) {
        if (actualFieldVal == null && expectedFieldVal == null) {
            return true;
        }
        if (actualFieldVal == null || expectedFieldVal == null) {
            return false;
        }

        Map acutalMap = (Map) actualFieldVal;
        Map expectedMap = (Map) expectedFieldVal;

        Set<Map.Entry> actualEntrySet = acutalMap.entrySet();
        boolean isEquals = true;
        for (Map.Entry entry : actualEntrySet) {
            Object key = entry.getKey();
            Object actualValue = entry.getValue();
            Object expectedValue = expectedMap.get(key);
            if (!isEquals(actualValue, expectedValue, ignoredProps)) {
                isEquals = false;
                break;
            }
        }

        return isEquals;
    }

    private boolean isCollectionObjEquals(Object actual, Object expected, String... ignoredProps) {

        if (actual == null && expected == null) {
            return true;
        }
        if (actual == null || expected == null) {
            return false;
        }

        Collection<?> tmpActual = (Collection<?>) actual;
        Collection<?> tmpExpected = (Collection<?>) expected;

        if (!CollectionHelper.isEmpty(tmpActual)) {
            Object[] tmpActualArr = new Object[tmpActual.size()];
            Object[] tmpExpectedArr = new Object[tmpExpected.size()];
            tmpActual.toArray(tmpActualArr);
            tmpExpected.toArray(tmpExpectedArr);
            int index = 0;
            for (Object tmpActualItem : tmpActualArr) {
                if (!isEquals(tmpExpectedArr[index], tmpActualItem, ignoredProps)) {
                    return false;
                }
                index++;
            }
        }
        return true;
    }
}
