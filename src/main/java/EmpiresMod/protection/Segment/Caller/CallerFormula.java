package EmpiresMod.protection.Segment.Caller;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.google.common.base.Joiner;

import EmpiresMod.Empires;
import EmpiresMod.protection.Segment.Getter.Getter;
import bsh.EvalError;
import bsh.Interpreter;

public class CallerFormula extends Caller {

	protected Getter.Container getters;

	public void setGetters(Getter.Container getters) {
		this.getters = getters;
	}

	@Override
	public Object invoke(Object instance, Object... parameters) throws Exception {
		Object result = null;

		String[] elements = name.split(" ");

		// Replace all the getters with proper numbers, assume getters that are
		// invalid as being numbers
		for (int i = 0; i < elements.length; i++) {
			if (!"+".equals(elements[i]) && !"-".equals(elements[i]) && !"*".equals(elements[i])
					&& !"/".equals(elements[i]) && !"^".equals(elements[i]) && getters != null
					&& getters.contains(elements[i])) {

				Object info = getters.get(elements[i]).invoke(Object.class, instance, parameters);
				// Replace all occurrences with the value that it got.
				// Spaces are needed to not replace parts of other getters.
				elements[i] = info.toString();
			}
		}

		String formula = Joiner.on(' ').join(elements);

		Interpreter interpreter = new Interpreter();
		try {
			interpreter.eval("result = " + formula);
			result = interpreter.get("result");
		} catch (EvalError ex) {
			Empires.instance.LOG.error(ExceptionUtils.getStackTrace(ex));
		}

		return result;
	}
}