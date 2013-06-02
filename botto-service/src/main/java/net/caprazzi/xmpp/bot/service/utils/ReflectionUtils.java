package net.caprazzi.xmpp.bot.service.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ReflectionUtils {

    public static Set<Method> methods(Class<?> clazz) {
        HashSet<Method> methods = new HashSet<Method>();
        methods.addAll(Arrays.asList(clazz.getMethods()));
        methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
        return methods;
    }

    public static Set<Field> fields(Class<?> clazz) {
        HashSet<Field> fields = new HashSet<Field>();
        fields.addAll(Arrays.asList(clazz.getFields()));
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        return fields;
    }
}
