package jmh.nl.naturalis.jmh;

import org.klojang.check.x.msg.CustomMsgFormatter;
import org.klojang.check.x.msg.MsgUtil;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.klojang.check.CommonChecks.lt;
import static org.klojang.check.relation.ComposeMethods.validInt;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(value = 5, jvmArgs = {"-Xms1G", "-Xmx1G", "-XX:-StackTraceInThrowable"})
//@Fork(value = 5, jvmArgs = {"-Xms1G", "-Xmx1G"})
@Warmup(iterations = 4, time = 3000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 3, time = 3500, timeUnit = TimeUnit.MILLISECONDS)
public class StringFormatting {

  public Random rand = new Random();
  int[] ints;
  public int counter0;
  public int counter1;

  public String argName;
  public int intSubject;
  public int intObject;

  public String stringSubject;

  public String stringObject;

  @Benchmark
  public void klojangFormatWithBuiltInArgsOnly(Blackhole bh) {
    bh.consume(MsgUtil.getCustomMessage(
        "foo12345 ${tag} foo12345 ${arg} foo12345 ${obj} foo12345",
        new Object[0], null, argName, intSubject, int.class, intObject));
  }

  @Benchmark
  public void klojangFormatWithUserArgsOnly(Blackhole bh) {
    bh.consume(MsgUtil.getCustomMessage(
        "foo12345 ${0} foo12345 ${1} foo12345 ${2} foo12345",
        new Object[] {argName, intSubject, intObject},
        null,
        null,
        null,
        null,
        null));
  }

  @Benchmark
  public void stringFormatWithPercentS(Blackhole bh) {
    bh.consume(String.format("foo12345 %s foo12345 %s foo12345 %s foo12345",
        argName,
        intSubject,
        intObject));
  }

  @Benchmark
  public void stringFormatWithPercentD(Blackhole bh) {
    bh.consume(String.format("foo12345 %s foo12345 %d foo12345 %d foo12345",
        argName,
        intSubject,
        intObject));
  }

  @Setup(Level.Trial)
  public void beforeBenchmark() {
    Random rand = new Random();
    ints = new int[1000];
    for (int i = 0; i < 1000; ++i) {
      ints[i] = rand.nextInt(1000, 2000);
    }
  }

  @Setup(Level.Invocation)
  public void beforeInvocation() {

    if (counter0++ % 2 == 0) {
      intSubject = rand.nextInt(100_000_000, 100_500_000);
    } else {
      intSubject = rand.nextInt(13, 97);
    }

    if (counter1 == 999) {
      counter1 = 0;
    } else {
      ++counter1;
    }
    intObject = ints[counter1];

    if (counter1 != 0 && counter0 % counter1 == 1) {
      argName = "arg-" + intSubject;
    } else {
      argName = "arg-" + intObject;
    }

    stringSubject = "s-" + intSubject;
    stringObject = "b-" + intObject;
  }

}
