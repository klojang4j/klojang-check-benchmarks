package jmh.nl.naturalis.jmh;

import org.apache.commons.lang3.RandomStringUtils;
import org.klojang.check.Check;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.klojang.check.CommonChecks.notNull;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(value = 5, jvmArgs = {"-Xms1G", "-Xmx1G", "-XX:-StackTraceInThrowable"})
//@Fork(value = 5, jvmArgs = {"-Xms1G", "-Xmx1G"})
@Warmup(iterations = 4, time = 3000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 3, time = 3500, timeUnit = TimeUnit.MILLISECONDS)
public class NotNull_050_Percent_Pass {

  public Object testVal;
  public int counter;

  @Benchmark
  public void plainNullTest(Blackhole bh) {
    bh.consume(testVal == null);
  }

  @Benchmark
  public void handCoded_NoMsgArgs(Blackhole bh) {
    try {
      if (testVal == null) {
        throw new IllegalArgumentException("arg must not be null");
      }
      bh.consume(testVal);
    } catch (IllegalArgumentException e) {
      bh.consume(e);
    }
  }

  @Benchmark
  public void handCoded_WithMsgArgs(Blackhole bh) {
    try {
      if (testVal == null) {
        throw new IllegalArgumentException(
            String.format("%s arg must not be %s", "arg", null));
      }
      bh.consume(testVal);
    } catch (IllegalArgumentException e) {
      bh.consume(e);
    }
  }

  @Benchmark
  public void prefabMessage(Blackhole bh) {
    try {
      bh.consume(Check.that(testVal).is(notNull()).ok());
    } catch (IllegalArgumentException e) {
      bh.consume(e);
    }
  }

  @Benchmark
  public void customMessage_NoMsgArgs(Blackhole bh) {
    try {
      bh.consume(Check.that(testVal)
          .is(notNull(), "arg must not be null")
          .ok());
    } catch (IllegalArgumentException e) {
      bh.consume(e);
    }
  }

  @Benchmark
  public void customMessage_NoMsgArgs_VarArgsNull(Blackhole bh) {
    try {
      bh.consume(Check.that(testVal)
          .is(notNull(), "arg must not be null", null)
          .ok());
    } catch (IllegalArgumentException e) {
      bh.consume(e);
    }
  }

  @Benchmark
  public void customMessage_WithMsgArgs(Blackhole bh) {
    try {
      bh.consume(Check.that(testVal)
          .is(notNull(), "${arg} must not be ${obj}")
          .ok());
    } catch (IllegalArgumentException e) {
      bh.consume(e);
    }
  }

  @Benchmark
  public void customException(Blackhole bh) {
    try {
      bh.consume(Check.that(testVal)
          .is(notNull(), () -> new IllegalArgumentException("arg must not be null"))
          .ok());
    } catch (IllegalArgumentException e) {
      bh.consume(e);
    }
  }

  @Setup(Level.Invocation)
  public void setup() {
    if (counter++ % 2 == 0) {
      testVal = null;
    } else {
      testVal = RandomStringUtils.randomAlphabetic(10, 15);
    }
  }

}
