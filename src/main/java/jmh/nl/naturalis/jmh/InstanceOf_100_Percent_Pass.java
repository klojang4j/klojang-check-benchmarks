package jmh.nl.naturalis.jmh;

import org.klojang.check.Check;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.klojang.check.CommonChecks.instanceOf;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(value = 5, jvmArgs = {"-Xms1G", "-Xmx1G", "-XX:-StackTraceInThrowable"})
//@Fork(value = 5, jvmArgs = {"-Xms1G", "-Xmx1G"})
@Warmup(iterations = 4, time = 3000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 3, time = 3500, timeUnit = TimeUnit.MILLISECONDS)
public class InstanceOf_100_Percent_Pass {

  public Object testVal;
  public Class testClass;

  @Benchmark
  public void handCoded_NoMsgArgs(Blackhole bh) {
    if (!testClass.isInstance(testVal)) {
      throw new IllegalArgumentException("argument has wrong type");
    }
    bh.consume(testVal);
  }

  @Benchmark
  public void handCoded_WithMsgArgs(Blackhole bh) {
    if (!testClass.isInstance(testVal)) {
      throw new IllegalArgumentException(
          String.format("%s must be instance of %s", testVal, testClass));
    }
    bh.consume(testVal);
  }

  @Benchmark
  public void prefabMessage(Blackhole bh) {
    bh.consume(Check.that(testVal).is(instanceOf(), testClass).ok());
  }

  @Benchmark
  public void customMessage_NoMsgArgs(Blackhole bh) {
    bh.consume(Check.that(testVal)
        .is(instanceOf(), testClass, "argument has wrong type")
        .ok());
  }

  @Benchmark
  public void customMessage_NoMsgArgs_VarArgsNull(Blackhole bh) {
    bh.consume(Check.that(testVal)
        .is(instanceOf(), testClass, "argument has wrong type", null)
        .ok());
  }

  @Benchmark
  public void customMessageWithMsgArgs(Blackhole bh) {
    bh.consume(Check.that(testVal)
        .is(instanceOf(), testClass, "${arg} must be instance of ${obj}")
        .ok());
  }

  @Benchmark
  public void customException(Blackhole bh) {
    bh.consume(Check.that(testVal)
        .is(instanceOf(),
            testClass,
            () -> new IllegalArgumentException("argument has wrong type"))
        .ok());
  }

  public int counter0;
  public int counter1;

  public Class[] types;

  @Setup(Level.Trial)
  public void init() {
    types = new Class[200];
    for (int i = 0; i < 200; ++i) {
      if (i % 5 == 0) {
        types[i] = Serializable.class;
      } else if (i % 4 == 0) {
        types[i] = Iterable.class;
      } else if (i % 3 == 0) {
        types[i] = Collection.class;
      } else {
        types[i] = Set.class;
      }
    }
  }

  @Setup(Level.Iteration)
  public void beforeIteration() {
    Collections.shuffle(Arrays.asList(types));
  }

  @Setup(Level.Invocation)
  public void setup() {
    // Ridiculous code to confuse the compiler as much as possible
    int i = counter0++;
    if (i % 100 == 0) {
      if (i % 4 == 0) {
        testVal = new HashSet<>();
      } else {
        testVal = new TreeSet<>();
      }
    } else {
      if (i % 5 == 0) {
        testVal = EnumSet.noneOf(DayOfWeek.class);
      } else if (i % 4 == 0) {
        testVal = new TreeSet<>();
      } else if (i % 3 == 0) {
        testVal = new HashSet<>();
      } else if (i % 2 == 0) {
        testVal = new LinkedHashSet<>();
      } else {
        testVal = new CopyOnWriteArraySet<>();
      }
    }

    if (counter1++ == 199) {
      counter1 = 0;
    }
    testClass = types[counter1];

  }

}
