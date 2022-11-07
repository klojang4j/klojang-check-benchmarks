package jmh.nl.naturalis.jmh;

import org.klojang.check.Check;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.klojang.check.CommonChecks.lt;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(value = 10, jvmArgs = {"-Xms1G", "-Xmx1G", "-XX:-StackTraceInThrowable"})
@Warmup(iterations = 4, time = 3)
@Measurement(iterations = 3, time = 3500, timeUnit = TimeUnit.MILLISECONDS)
public class LessThan_100_Percent_Pass {

  public Random rand = new Random();
  int[] ints;
  public int small;
  public int big;
  public int counter0;
  public int counter1;

  @Benchmark
  public void handCoded(Blackhole bh) {
    try {
      if (small >= big) {
        throw new IllegalArgumentException("argument too big");
      }
      bh.consume(small);
    } catch (IllegalArgumentException e) {
      bh.consume(e);
    }
  }

  @Benchmark
  public void handCodedStringFormatErrMsg(Blackhole bh) {
    try {
      if (small >= big) {
        throw new IllegalArgumentException(
            String.format("%d arg must be < %d", small, big));
      }
      bh.consume(small);
    } catch (IllegalArgumentException e) {
      bh.consume(e);
    }
  }

  @Benchmark
  public void prefabMessage(Blackhole bh) {
    try {
      bh.consume(Check.that(small).is(lt(), big).ok());
    } catch (IllegalArgumentException e) {
      bh.consume(e);
    }
  }

  @Benchmark
  public void customMessageWithMsgArgs(Blackhole bh) {
    try {
      bh.consume(Check.that(small)
          .is(lt(), big, "${arg} must be < ${obj}")
          .ok());
    } catch (IllegalArgumentException e) {
      bh.consume(e);
    }
  }

  @Benchmark
  public void customMessageNoMsgArgs(Blackhole bh) {
    try {
      bh.consume(Check.that(small).is(lt(), big, "argument too big").ok());
    } catch (IllegalArgumentException e) {
      bh.consume(e);
    }
  }

  @Benchmark
  public void customMessageNoMsgArgsWithEOM(Blackhole bh) {
    try {
      bh.consume(Check.that(small).is(lt(), big, "argument too big", null).ok());
    } catch (IllegalArgumentException e) {
      bh.consume(e);
    }
  }

  @Benchmark
  public void customException(Blackhole bh) {
    try {
      bh.consume(
          Check.that(small)
              .is(lt(), big, () -> new IOException("argument too big"))
              .ok());
    } catch (IOException e) {
      bh.consume(e);
    }
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

    if (counter0++ % 100 == 0) {
      small = rand.nextInt(10, 21);
    } else {
      small = rand.nextInt(13, 97);
    }

    if (counter1 == 999) {
      counter1 = 0;
    } else {
      ++counter1;
    }
    big = ints[counter1];
  }

}
