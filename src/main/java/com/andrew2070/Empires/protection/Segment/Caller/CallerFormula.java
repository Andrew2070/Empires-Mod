package com.andrew2070.Empires.protection.Segment.Caller;


import bsh.EvalError;
import bsh.Interpreter;
import com.google.common.base.Joiner;
import com.andrew2070.Empires.Empires;
import com.andrew2070.Empires.protection.Segment.Getter.Getter;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class CallerFormula extends Caller {

    protected Getter.Container getters;

    public void setGetters(Getter.Container getters) {
        this.getters = getters;
    }

    @Override
    public Object invoke(Object instance, Object... parameters) throws Exception {
        Object result = null;

        String[] elements = name.split(" ");

        // Replace all the getters with proper numbers, assume getters that are invalid as being numbers
        for(int i = 0 ; i < elements.length; i++) {
            if(!"+".equals(elements[i]) && !"-".equals(elements[i]) && !"*".equals(elements[i]) && !"/".equals(elements[i]) && !"^".equals(elements[i]) && getters != null && getters.contains(elements[i])) {

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