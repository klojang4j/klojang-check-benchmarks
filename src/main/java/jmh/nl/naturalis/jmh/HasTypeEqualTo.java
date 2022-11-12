package jmh.nl.naturalis.jmh;

import org.klojang.check.Check;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.klojang.check.CommonChecks.sameAs;
import static org.klojang.check.CommonProperties.type;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(value = 5, jvmArgs = {"-Xms1G", "-Xmx1G", "-XX:-StackTraceInThrowable"})
@Warmup(iterations = 4, time = 3)
@Measurement(iterations = 3, time = 3500, timeUnit = TimeUnit.MILLISECONDS)
public class HasTypeEqualTo {

  private static final Supplier<IllegalArgumentException> EXC =
      () -> new IllegalArgumentException("argument has wrong type");

  public Object testVal;

  @Benchmark
  public void handCoded(Blackhole bh) {
    try {
      if (testVal.getClass() != Double.class) {
        throw new IllegalArgumentException("argument has wrong type");
      }
      bh.consume(testVal);
    } catch (IllegalArgumentException e) {
    }
  }

  @Benchmark
  public void customException_getClassIsSameAs(Blackhole bh) {
    bh.consume(Check.that(testVal.getClass()).is(sameAs(), Double.class, EXC).ok());
  }

  @Benchmark
  public void customException_hasTypeSameAs(Blackhole bh) {
    bh.consume(Check.that(testVal).has(type(), sameAs(), Double.class, EXC).ok());
  }

  public int counter0 = 0;
  public int counter1 = 0;

  public Object[] doubles;
  public Random rand;

  @Setup(Level.Trial)
  public void init() {
    if (rand == null) {
      rand = new Random();
    }
    doubles = new Double[10000];
    for (int i = 0; i < 10000; ++i) {
      doubles[i] = rand.nextDouble();
    }
  }

  @Setup(Level.Iteration)
  public void beforeIteration() {
    //Collections.shuffle(Arrays.asList(doubles));
  }

  @Setup(Level.Invocation)
  public void setup() {
    if (counter0 < 10000) {
      testVal = doubles[counter0];
    } else {
      if (counter1++ == 100) {
        init();
        counter1 = 0;
      }
      testVal = doubles[counter0 = 0];
    }
    counter0++;
  }

}
