/*
 * Copyright (C) 2016 Teemowork Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package teemowork.tool;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Consumer;

import kiss.I;

/**
 * @version 2016/09/25 17:08:20
 */
public class ClassWriter {

    /** The end of line. */
    public static final String EOL = "\r\n";

    /** The code buffer. */
    private final StringBuilder code = new StringBuilder();

    /** The package name. */
    public final String packageName;

    /** The class name. */
    public final String className;

    /** The import manager. */
    private final Imports imports;

    /** The import position. */
    private final int importPosition;

    /** The indent depth. */
    private int indentDepth;

    /** The line head management. */
    private boolean isHead;

    /** The indent text. */
    private String indent = "    ";

    /**
     * @param className
     */
    public ClassWriter(String packageName, String className) {
        this.packageName = packageName;
        this.className = className;
        this.imports = new Imports(packageName);

        write("/*");
        write(" * Copyright (C) 2016 Nameless Production Committee");
        write(" *");
        write(" * Licensed under the MIT License (the \"License\");");
        write(" * you may not use this file except in compliance with the License.");
        write(" * You may obtain a copy of the License at");
        write(" *");
        write(" *          http://opensource.org/licenses/mit-license.php");
        write(" */");
        write("package ", packageName, ";");
        importPosition = code.length();
        write();
    }

    public <T> void writeConstants(Iterable<T> items, Consumer<T> process) {
        for (T item : items) {
            process.accept(item);
        }

        replaceLatest(',', ';');
    }

    private void replaceLatest(char target, char replacement) {
        for (int i = code.length() - 1; 0 <= i; i--) {
            char c = code.charAt(i);

            if (Character.isWhitespace(c)) {
                // ignore
            } else if (c == target) {
                code.setCharAt(i, replacement);
                return;
            } else {
                return;
            }
        }
    }

    public <T> void write(Iterable<T> items, Consumer<T> process) {
        items.forEach(process);
    }

    /**
     * <p>
     * Write blank line.
     * </p>
     */
    public void write() {
        code.append("\r\n");

        isHead = true;
    }

    /**
     * <p>
     * Write code fragment.
     * </p>
     * 
     * @param values
     */
    public void write(Object... values) {
        for (Object value : values) {
            code(value);
        }
        write();
    }

    /**
     * <p>
     * Write code actually.
     * </p>
     * 
     * @param value
     */
    private ClassWriter code(Object value) {
        if (value instanceof CodeFragment) {
            ((CodeFragment) value).write(this);
        } else {
            String text = value.toString();

            if (text.endsWith("{")) {
                indentDepth++;
            } else if (text.startsWith("}")) {
                indentDepth--;
            }

            if (isHead) {
                for (int j = 0; j < indentDepth; j++) {
                    code.append(indent);
                }
                isHead = false;
            }

            if (value instanceof Class) {
                text = imports.add(value);
            } else if (value instanceof Float) {
                text = text + "F";
            }
            code.append(text);
        }
        return this;
    }

    /**
     * <p>
     * Write source code to the specified {@link Path}.
     * </p>
     * 
     * @param path
     */
    public void writeTo(Path path) {
        try {
            StringBuilder copy = new StringBuilder(code);
            copy.insert(importPosition, imports);

            Path file = path.resolve(packageName.replace('.', '/')).resolve(className.concat(".java"));
            byte[] contents = copy.toString().getBytes(StandardCharsets.UTF_8);

            Files.write(file, contents);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return code.toString();
    }

    /**
     * <p>
     * Helper method to write string literal.
     * </p>
     * 
     * @param values
     * @return
     */
    public static String string(Object... values) {
        StringBuilder builder = new StringBuilder();
        builder.append('"');
        for (int i = 0; i < values.length; i++) {
            builder.append(values[i].toString());
        }
        builder.append('"');

        return builder.toString();
    }

    /**
     * <p>
     * Helper method to write string literal.
     * </p>
     * 
     * @param values
     * @return
     */
    public static <E> String array(List<E> list) {
        if (list == null || list.isEmpty()) {
            return "null";
        }

        Class type = list.get(0).getClass();
        StringJoiner builder = new StringJoiner(", ", "new " + Imports.convert(type) + "[] {", "}");

        for (Object item : list) {
            builder.add(type == String.class ? string(item) : item.toString());
        }
        return builder.toString();
    }

    /**
     * <p>
     * Helper method to write string literal.
     * </p>
     * 
     * @param values
     * @return
     */
    public static <E> String array(Object[] list) {
        if (list == null) {
            return "null";
        }
        return array(Arrays.asList(list));
    }

    /**
     * <p>
     * Helper method to write array.
     * </p>
     * 
     * @param values
     * @return
     */
    public static <E> String array(int[] array) {
        List<Integer> list = new ArrayList();
        for (Integer value : array) {
            list.add(value);
        }
        return array(list);
    }

    /**
     * <p>
     * Helper method to write array.
     * </p>
     * 
     * @param values
     * @return
     */
    public static <E> String array(float[] array) {
        List<Float> list = new ArrayList();
        for (Float value : array) {
            list.add(value);
        }
        return array(list);
    }

    /**
     * <p>
     * Helper method to write array.
     * </p>
     * 
     * @param values
     * @return
     */
    public static <E> String array(long[] array) {
        List<Long> list = new ArrayList();
        for (Long value : array) {
            list.add(value);
        }
        return array(list);
    }

    /**
     * <p>
     * Helper method to write array.
     * </p>
     * 
     * @param values
     * @return
     */
    public static <E> String array(double[] array) {
        List<Double> list = new ArrayList();
        for (Double value : array) {
            list.add(value);
        }
        return array(list);
    }

    /**
     * <p>
     * Helper method to write array.
     * </p>
     * 
     * @param values
     * @return
     */
    public static <E> String array(boolean[] array) {
        List<Boolean> list = new ArrayList<>();
        for (Boolean value : array) {
            list.add(value);
        }
        return array(list);
    }

    /**
     * <p>
     * Helper method to write array.
     * </p>
     * 
     * @param values
     * @return
     */
    public static <E> String array(char[] array) {
        List<Character> list = new ArrayList<Character>();
        for (Character value : array) {
            list.add(value);
        }
        return array(list);
    }

    /**
     * <p>
     * Helper method to write array.
     * </p>
     * 
     * @param values
     * @return
     */
    public static <E> String array(byte[] array) {
        List<Byte> list = new ArrayList();
        for (Byte value : array) {
            list.add(value);
        }
        return array(list);
    }

    /**
     * <p>
     * Helper method to write parameters.
     * </p>
     * 
     * @param params
     * @return
     */
    public static Object param(Object... params) {
        // ImportableParameters importable = new ImportableParameters();
        //
        // for (Object param : params) {
        // if (param == null) {
        // param = "null";
        // } else if (param instanceof Float) {
        // param = param + "F";
        // }
        // importable.joiner.add(param.toString());
        // }

        return new ImportableParameters(params);
    }

    /**
     * <p>
     * Helper method to write parameter definition.
     * </p>
     * 
     * @param params
     * @return
     */
    public static Object arg(Object... params) {
        return new ImportableParameterDefinition(params, false);
    }

    /**
     * <p>
     * Helper method to write parameter definition wtih variable arguments.
     * </p>
     * 
     * @param params
     * @return
     */
    public static Object vararg(Object... params) {
        return new ImportableParameterDefinition(params, true);
    }

    public static Object lambda(String param, Runnable code) {
        return new Lambda(param, code);
    }

    /**
     * <p>
     * Helper method to write generic parameter definition.
     * </p>
     * 
     * @param params
     * @return
     */
    public static Object generic(Class base, Object... params) {
        return new ImportableGeneric(base, params);
    }

    /**
     * <p>
     * Helper method to write method reference.
     * </p>
     * 
     * @param params
     * @return
     */
    public static Object methodRef(Class base, String methodName) {
        return new MethodReference(base, methodName);
    }

    /**
     * <p>
     * Helper method to write method reference.
     * </p>
     * 
     * @param params
     * @return
     */
    public static Object staticField(Class base, String fieldName) {
        return new StaticField(base, fieldName);
    }

    /**
     * @version 2015/07/19 5:18:02
     */
    private static interface CodeFragment {

        /**
         * <p>
         * Write code.
         * </p>
         * 
         * @param $
         */
        void write(ClassWriter $);
    }

    /**
     * @version 2016/09/12 1:09:05
     */
    private static class StaticField implements CodeFragment {

        /** The base class. */
        private final Class base;

        /** The reference name. */
        private final String name;

        /**
         * @param base
         * @param name
         */
        private StaticField(Class base, String name) {
            this.base = base;
            this.name = name;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(ClassWriter $) {
            $.code(base).code(".").code(name);
        }
    }

    /**
     * @version 2015/07/23 12:10:56
     */
    private static class MethodReference implements CodeFragment {

        /** The base class. */
        private final Class base;

        /** The reference name. */
        private final String name;

        /**
         * @param base
         * @param name
         */
        private MethodReference(Class base, String name) {
            this.base = base;
            this.name = name;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(ClassWriter $) {
            $.code(base).code("::").code(name);
        }
    }

    /**
     * @version 2015/07/19 5:09:45
     */
    private static class Lambda implements CodeFragment {

        /** The parameters. */
        private final String param;

        /** The actual code. */
        private final Runnable code;

        /**
         * @param param
         * @param code
         */
        private Lambda(String param, Runnable code) {
            this.param = param;
            this.code = code;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(ClassWriter $) {
            $.write(param, " -> {");
            code.run();
            $.code("}");
        }
    }

    /**
     * @version 2015/07/14 10:31:33
     */
    private static class ImportableParameters implements CodeFragment {

        /** The parameters. */
        private final Object[] params;

        /**
         * @param params
         */
        private ImportableParameters(Object[] params) {
            this.params = params;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(ClassWriter $) {
            $.code("(");

            for (int i = 0, length = params.length; i < length; i++) {
                $.code(params[i]);

                if (i + 1 < length) {
                    $.code(", ");
                }
            }

            $.code(")");
        }
    }

    /**
     * @version 2015/07/14 10:31:33
     */
    private static class ImportableParameterDefinition implements CodeFragment {

        /** The parameters. */
        private final Object[] params;

        /** The vararg usage. */
        private final boolean useVarArg;

        /**
         * @param params
         */
        private ImportableParameterDefinition(Object[] params, boolean useVarArg) {
            this.params = params;
            this.useVarArg = useVarArg;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(ClassWriter $) {
            $.code("(");
            for (int i = 0, length = params.length; i < length; i++) {
                Object type = params[i++];
                Object param = params[i];

                if (useVarArg && i + 1 == length) {
                    if (type instanceof Class) {
                        Class clazz = (Class) type;

                        if (clazz.isArray()) {
                            $.code(clazz.getComponentType()).code("... ").code(param);
                        } else {
                            $.code(type).code(" ").code(param);
                        }
                    } else {
                        $.code(type).code(" ").code(param);
                    }
                } else {
                    $.code(type).code(" ").code(param);
                }

                if (i + 1 < length) {
                    $.code(", ");
                }
            }
            $.code(")");
        }
    }

    /**
     * @version 2015/07/19 4:18:32
     */
    private static class ImportableGeneric implements CodeFragment {

        /** The base type. */
        private final Class base;

        /** The parameter types. */
        private final Object[] params;

        /**
         * @param base
         * @param params
         */
        private ImportableGeneric(Class base, Object... params) {
            this.base = base;
            this.params = params;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(ClassWriter $) {
            $.code(base).code("<");
            for (int i = 0, length = params.length; i < length; i++) {
                $.code(params[i]);

                if (i + 1 < length) {
                    $.code(", ");
                }
            }
            $.code(">");
        }
    }

    /**
     * @version 2015/07/14 10:49:54
     */
    private static class Imports {

        /** The package name. */
        private final String packageName;

        /** The imported classes. */
        private final Set<Class> classes = new HashSet();

        /**
         * @param packageName
         */
        private Imports(String packageName) {
            this.packageName = packageName;
        }

        /**
         * <p>
         * Import class.
         * </p>
         * 
         * @param clazz
         */
        private String add(Object value) {
            return convert2(value);
        }

        /**
         * <p>
         * Import class.
         * </p>
         * 
         * @param clazz
         */
        private String convert2(Object value) {
            if (value == null) {
                value = "null";
            }

            if (value instanceof Class) {
                Class clazz = (Class) value;
                String full = clazz.getName();
                String simple = clazz.getSimpleName();

                if (clazz == Integer.class) {
                    return "int";
                } else if (clazz.isArray()) {
                    return convert2(clazz.getComponentType()) + "[]";
                } else if (clazz.isPrimitive() || full.startsWith("java.lang.") || packageName.equals(clazz.getPackage().getName())) {
                    return simple;
                }

                classes.add(clazz);
                return clazz.getSimpleName();
            }
            return value.toString();
        }

        /**
         * <p>
         * Import class.
         * </p>
         * 
         * @param clazz
         */
        private static String convert(Object value) {
            if (value == null) {
                value = "null";
            }

            if (value instanceof Class) {
                Class clazz = (Class) value;
                String full = clazz.getName();
                String simple = clazz.getSimpleName();

                if (clazz == Integer.class) {
                    return "int";
                } else if (clazz.isPrimitive() || full.startsWith("java.lang.")) {
                    return simple;
                } else if (clazz.isArray()) {
                    return convert(clazz.getComponentType()) + "[]";
                }
                return clazz.getName();
            }
            return value.toString();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(EOL);
            for (Class clazz : classes) {
                builder.append("import ").append(clazz.getName()).append(";").append(EOL);
            }
            return builder.toString();
        }
    }
}
