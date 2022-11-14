package jmh.nl.naturalis.jmh;

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
public class ComposeSugarSyntax {

  public Random rand = new Random();
  int[] ints;
  public int small;
  public int big;
  public int counter0;
  public int counter1;

  @Benchmark
  public void bitter(Blackhole bh) {
    bh.consume(validInt().and(small < big));
  }

  @Benchmark
  public void sweet(Blackhole bh) {
    bh.consume(validInt().andThat(small, lt(), big));
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
      small = rand.nextInt(100_000_000, 100_500_000);
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
