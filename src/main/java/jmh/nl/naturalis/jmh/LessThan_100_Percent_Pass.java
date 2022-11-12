package jmh.nl.naturalis.jmh;

import org.klojang.check.Check;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.klojang.check.CommonChecks.lt;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(value = 5, jvmArgs = {"-Xms1G", "-Xmx1G", "-XX:-StackTraceInThrowable"})
//@Fork(value = 5, jvmArgs = {"-Xms1G", "-Xmx1G"})
@Warmup(iterations = 4, time = 3000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 3, time = 3500, timeUnit = TimeUnit.MILLISECONDS)
public class LessThan_100_Percent_Pass {

  public Random rand = new Random();
  int[] ints;
  public int small;
  public int big;
  public int counter0;
  public int counter1;

  @Benchmark
  public void handCoded_NoMsgArgs(Blackhole bh) {
    if (small >= big) {
      throw new IllegalArgumentException("argument too big");
    }
    bh.consume(small);
  }

  @Benchmark
  public void handCoded_WithMsgArgs(Blackhole bh) {
    if (small >= big) {
      throw new IllegalArgumentException(
          String.format("%d arg must be < %d", small, big));
    }
    bh.consume(small);
  }

  @Benchmark
  public void prefabMessage(Blackhole bh) {
    bh.consume(Check.that(small).is(lt(), big).ok());
  }

  @Benchmark
  public void customMessage_NoMsgArgs(Blackhole bh) {
    bh.consume(Check.that(small).is(lt(), big, "argument too big").ok());
  }

  @Benchmark
  public void customMessage_NoMsgArgs_VarArgsNull(Blackhole bh) {
    bh.consume(Check.that(small).is(lt(), big, "argument too big", null).ok());
  }

  @Benchmark
  public void customMessage_WithMsgArgs(Blackhole bh) {
    bh.consume(Check.that(small).is(lt(), big, "${arg} must be < ${obj}").ok());
  }

  @Benchmark
  public void customException(Blackhole bh) {
    bh.consume(Check.that(small)
        .is(lt(), big, () -> new IllegalArgumentException("argument too big"))
        .ok());
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
