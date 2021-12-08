package org.keplerproject.luajava.fast;

import org.keplerproject.luajava.LuaState;
import org.keplerproject.luajava.LuaStateFactory;
import org.openjdk.jmh.annotations.Benchmark;

/**
 * @author wl
 * @version 2021.11.15
 */
public class LuajavaTest implements ILuaTest {

    private static LuaState luaState;

    static {
        System.loadLibrary("lib/luajava-1.1");

        luaState = LuaStateFactory.newLuaState();
        luaState.openLibs();
        luaState.LdoFile("lua/luajava.lua");

    }

    public static void main(String[] args) {
        LuajavaTest test = new LuajavaTest();
        System.out.println(test.sum());
        test.newInstance();
        test.methodCall();
    }

    @Benchmark
    @Override
    public long sum() {
        luaState.getGlobal("sum");
        luaState.pushInteger(count);
        if (luaState.pcall(1, 1, 0) != 0) {
            System.out.println("lua error: " + luaState.toString(-1));
            System.out.println("lua error: " + luaState.toString(-2));
        }
        long sum = (long) luaState.toNumber(-1);
        luaState.pop(1);
        return sum;
    }

    @Benchmark
    @Override
    public void newInstance() {
        luaState.getGlobal("newInstance");
        luaState.pushInteger(count);
        if (luaState.pcall(1, 0, 0) != 0) {
            System.out.println("lua error: " + luaState.toString(-1));
            System.out.println("lua error: " + luaState.toString(-2));
        }
    }

    @Benchmark
    @Override
    public void methodCall() {
        luaState.getGlobal("playerAddExp");
        luaState.pushInteger(count);
        if (luaState.pcall(1, 0, 0) != 0) {
            System.out.println("lua error: " + luaState.toString(-1));
            System.out.println("lua error: " + luaState.toString(-2));
        }
    }

    @Benchmark
    @Override
    public void sort() {
        luaState.getGlobal("sort");
        if (luaState.pcall(0, 0, 0) != 0) {
            System.out.println("lua error: " + luaState.toString(-1));
            System.out.println("lua error: " + luaState.toString(-2));
        }
    }

    @Benchmark
    @Override
    public long fib() {
        luaState.getGlobal("fib");
        luaState.pushInteger(fibNum);
        if (luaState.pcall(1, 1, 0) != 0) {
            System.out.println("lua error: " + luaState.toString(-1));
            System.out.println("lua error: " + luaState.toString(-2));
        }
        long sum = (long) luaState.toNumber(-1);
        luaState.pop(1);
        return sum;
    }
}
