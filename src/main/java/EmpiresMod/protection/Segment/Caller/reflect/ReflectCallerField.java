package EmpiresMod.protection.Segment.Caller.reflect;

import java.lang.reflect.Field;

import EmpiresMod.protection.Segment.Caller.Caller;

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