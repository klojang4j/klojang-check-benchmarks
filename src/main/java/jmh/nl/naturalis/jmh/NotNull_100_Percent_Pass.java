package jmh.nl.naturalis.jmh;

import org.apache.commons.lang3.RandomStringUtils;
import org.klojang.check.Check;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.klojang.check.CommonChecks.notNull;

;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(value = 30, jvmArgs = {"-Xms1G", "-Xmx1G", "-XX:-StackTraceInThrowable"})
@Warmup(iterations = 4, time = 3)
@Measurement(iterations = 3, time = 3500, timeUnit = TimeUnit.MILLISECONDS)
public class NotNull_100_Percent_Pass {

  public Object testVal;
  public int counter;

  @Benchmark
  public void handCoded(Blackhole bh) {
    try {
      if (testVal == null) {
        throw new IllegalArgumentException("arg must not be null");
      }
      bh.consume(testVal);
    } catch (IllegalArgumentException e) {
    }
  }

  @Benchmark
  public void handCodedStringFormatErrMsg(Blackhole bh) {
    try {
      if (testVal == null) {
        throw new IllegalArgumentException(
            String.format("%s arg must not be %s", "arg", null));
      }
      bh.consume(testVal);
    } catch (IllegalArgumentException e) {
    }
  }

  @Benchmark
  public void prefabMessage(Blackhole bh) {
    try {
      bh.consume(Check.that(testVal).is(notNull()).ok());
    } catch (IllegalArgumentException e) {
    }
  }

  @Benchmark
  public void customMessageWithMsgArgs(Blackhole bh) {
    try {
      bh.consume(Check.that(testVal)
          .is(notNull(), "${arg} must not be ${obj}")
          .ok());
    } catch (IllegalArgumentException e) {
    }
  }

  @Benchmark
  public void customMessageNoMsgArgs(Blackhole bh) {
    try {
      bh.consume(Check.that(testVal)
          .is(notNull(), "arg must not be null")
          .ok());
    } catch (IllegalArgumentException e) {
    }
  }

  @Benchmark
  public void customMessageNoMsgArgsWithEOM(Blackhole bh) {
    try {
      bh.consume(Check.that(testVal)
          .is(notNull(), "arg must not be null", null)
          .ok());
    } catch (IllegalArgumentException e) {
    }
  }

  @Benchmark
  public void customException(Blackhole bh) {
    try {
      bh.consume(Check.that(testVal)
          .is(notNull(), () -> new IOException("arg must not be null"))
          .ok());
    } catch (IOException e) {
    }
  }

  @Setup(Level.Invocation)
  public void setup() {
    if (counter++ % 100 == 0) {
      testVal = new Object();
    } else {
      testVal = RandomStringUtils.randomAlphabetic(10, 15);
    }
  }

}
