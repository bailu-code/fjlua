package org.keplerproject.luajava.fast;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * java端缓存的java对象/方法，用于优化java虚拟机与lua虚拟机交互所产生的对象传递
 *
 * @author wl
 * @version 2021.11.11
 */
public class ForLuaClass<T> {
    /**
     * java class
     */
    private final Class<T> javaClass;

    /**
     * lua meta信息
     */
    private final int ref;

    /**
     * 动态方法
     */
    private final List<FunctionInfo<T>> functions;

    private ForLuaClass(int ref, Class<T> javaClass, List<FunctionInfo<T>> functions) {
        this.ref = ref;
        this.javaClass = javaClass;
        this.functions = functions;
    }

    public int getRef() {
        return ref;
    }

    public Class<?> getJavaClass() {
        return javaClass;
    }

    public List<FunctionInfo<T>> getFunctions() {
        return functions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ForLuaClass<?> that = (ForLuaClass<?>) o;

        return Objects.equals(javaClass, that.javaClass);
    }

    @Override
    public int hashCode() {
        return javaClass != null ? javaClass.hashCode() : 0;
    }

    public static class Builder<T> {
        private final Class<T> javaClass;
        private final int ref;

        private final List<FunctionInfo<T>> functions = new LinkedList<>();
        private final Map<String, Pair<JavaFunction<T>, JavaFunction<T>>> fieldMap = new HashMap<>();
        private final Int2ObjectMap<JavaNewFunction<T>> newFunctionMap = new Int2ObjectArrayMap<>();

        public Builder(Class<T> javaClass, int ref) {
            this.javaClass = javaClass;
            this.ref = ref;
        }

        /**
         * 添加方法
         *
         * @param funcName 方法名
         * @param function 方法
         */
        public void addFunction(String funcName, JavaFunction<T> function) {
            functions.add(new FunctionInfo<>(funcName, function));
        }

        /**
         * 添加静态方法
         *
         * @param funcName       方法名
         * @param staticFunction 方法
         */
        public void addStaticFunction(String funcName, JavaStaticFunction<T> staticFunction) {
            addFunction(funcName, staticFunction);
        }

        /**
         * 添加字段
         *
         * @param fieldName 字段名
         * @param getter    getter
         * @param setter    setter
         */
        public void addField(String fieldName, JavaFunction<T> getter, JavaFunction<T> setter) {
            fieldMap.put(fieldName, Pair.of(getter, setter));
        }

        public void addConstructor(int constructorId, JavaNewFunction<T> javaNewFunction) {
            newFunctionMap.put(constructorId, javaNewFunction);
        }

        ForLuaClass<T> build() {
            return new ForLuaClass<>(ref, javaClass, functions);
        }
    }
}
