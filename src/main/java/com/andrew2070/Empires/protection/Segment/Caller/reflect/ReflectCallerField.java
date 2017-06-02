package com.andrew2070.Empires.protection.Segment.Caller.reflect;


import com.andrew2070.Empires.protection.Segment.Caller.Caller;

import java.lang.reflect.Field;

public class ReflectCallerField extends Caller {
    private Field field;

    @Override
    public Object invoke(Object instance, Object... parameters) throws Exception {
        return getField().get(instance);
    }

    @Override
    public Class<?> nextClass() throws Exception {
        return getField().getType();
    }

    private Field getField() throws Exception {
        // Lazy loading ftw!
        if (field == null) {
            try {
                field = checkClass.getField(name);
            } catch (NoSuchFieldException ex) {
                field = checkClass.getDeclaredField(name);
                field.setAccessible(true);
            }
        }

        return field;
    }
}