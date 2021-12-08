package org.keplerproject.luajava.fast;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.luaj.vm2.luajc.LuaJC;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * @author wl
 * @version 2021.11.16
 */
@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
@Warmup(iterations = 1, time = 3)
@Measurement(iterations = 2, time = 3)
public class LuajTest implements ILuaTest {
    private static final LuaValue sum;
    private static final LuaValue count = LuaValue.valueOf(ILuaTest.count);
    private static final LuaValue fibNum = LuaValue.valueOf(ILuaTest.fibNum);
    private static final LuaValue newInstance;
    private static final LuaValue playerAddExp;
    private static final LuaValue sort;
    private static final LuaValue fib;

    static {
        Globals globals = JsePlatform.standardGlobals();
        LuaJC.install(globals);
        globals.loadfile("lua/luajava.lua").call();
        sum = globals.get("sum");
        newInstance = globals.get("newInstance");
        playerAddExp = globals.get("playerAddExp");
        sort = globals.get("sort");
        fib = globals.get("fib");
    }

    public static void main(String[] args) {
        LuajTest test = new LuajTest();
        System.out.println(test.sum());
        test.newInstance();
        test.methodCall();
        test.sort();
        System.out.println(test.fib());
    }

    @Benchmark
    @Override
    public long sum() {
        return sum.invoke(count).tolong(1);
    }

    @Benchmark
    @Override
    public void newInstance() {
        newInstance.invoke(count);
    }

    @Benchmark
    @Override
    public void methodCall() {
        playerAddExp.invoke(count);
    }

    @Benchmark
    @Override
    public void sort() {
        sort.invoke();
    }

    @Benchmark
    @Override
    public long fib() {
        return fib.invoke(fibNum).tolong(1);
    }
}
