package org.keplerproject.luajava.fast;

import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.concurrent.TimeUnit;

/**
 * Benchmark                 Mode  Cnt     Score   Error   Units
 * FjluaTest.methodCall     thrpt    2  1392.487          ops/ms
 * FjluaTest.newInstance    thrpt    2     0.012          ops/ms
 * FjluaTest.sort           thrpt    2   569.080          ops/ms
 * FjluaTest.sum            thrpt    2    13.973          ops/ms
 * LuajTest.methodCall      thrpt    2  9476.718          ops/ms
 * LuajTest.newInstance     thrpt    2     0.084          ops/ms
 * LuajTest.sort            thrpt    2    23.356          ops/ms
 * LuajTest.sum             thrpt    2     0.380          ops/ms
 * LuajavaTest.methodCall   thrpt    2   180.450          ops/ms
 * LuajavaTest.newInstance  thrpt    2     0.005          ops/ms
 * LuajavaTest.sort         thrpt    2   507.555          ops/ms
 * LuajavaTest.sum          thrpt    2    13.937          ops/ms
 * FjluaTest.methodCall      avgt    2     0.001           ms/op
 * FjluaTest.newInstance     avgt    2    79.379           ms/op
 * FjluaTest.sort            avgt    2     0.002           ms/op
 * FjluaTest.sum             avgt    2     0.071           ms/op
 * LuajTest.methodCall       avgt    2    ≈ 10⁻⁴           ms/op
 * LuajTest.newInstance      avgt    2    11.505           ms/op
 * LuajTest.sort             avgt    2     0.043           ms/op
 * LuajTest.sum              avgt    2     2.641           ms/op
 * LuajavaTest.methodCall    avgt    2     0.006           ms/op
 * LuajavaTest.newInstance   avgt    2   208.608           ms/op
 * LuajavaTest.sort          avgt    2     0.002           ms/op
 * LuajavaTest.sum           avgt    2     0.072           ms/op
 *
 * @author wl
 * @version 2021.11.24
*/
public class JMH {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()

//            .include(LuajTest.class.getSimpleName())
//            .include(LuajavaTest.class.getSimpleName())
//            .include(FjluaTest.class.getSimpleName())

            .include("fib")

            .mode(Mode.Throughput).mode(Mode.AverageTime)
            .timeUnit(TimeUnit.MILLISECONDS)
            .warmupIterations(1).warmupTime(TimeValue.seconds(3))
            .forks(1)
            .measurementIterations(2).measurementTime(TimeValue.seconds(3))
            .build();
        new Runner(opt).run();
    }

}
