# JMH Benchmarks for Klojang Check

JMH Benchmarks for some critical parts of the Klojang Check.

## How to run

- Clone this repository
- Run: mvn clean package
- Run: java -jar target/benchmarks.jar <name_of_test>

For example:

java -jar target/benchmarks.jar NotNull_100_Percent_Pass

## How and What We Tested

These benchmarks measure the performance of the three variants provided by Klojang
Check for validating arguments:

1. Klojang Check generates both the exception message and the exception itself 
  (the "prefabMessage" benchmarks)
2. The client provides the exception message and Klojang Check generates the 
  exception (the "customMessage" benchmarks)
3. The client provides both the exception message and the exception itself (the 
  "customException" benchmarks)

Each variant is again benchmarked for three scenarios:
1. The argument _always_ passes the test (the "100_Percent_Pass" benchmarks)
2. The argument passes the test in 99% percent of the cases (the "099_Percent_Pass" 
   benchmarks)
3. The argument passes the test in 50% of the cases  (the "050_Percent_Pass" 
   benchmarks)

The performance is compared with an equivalent "hand-coded" check that looks like
this:

```java
if(condition) {
    throw new IllegalArgumentException("an exception message");
}    
```
For example, for the null check, the hand-coded check looks like this:
```java
if(randomizedTestVal == null) {
    throw new IllegalArgumentException("arg must not be null");
}    
```
The Klojang Check counterparts to this check would look like this:
```java
// prefab message from Klojang Check
Check.that(arg, "arg").is(notNull());
// custom message
Check.that(arg).is(notNull(), "arg must not be null");
// custom exception
Check.that(arg).is(notNull(),
        () -> new IllegalArgumentException("arg must not be null"));
```

The argument is thrown into the compiler black hole to prevent JVM optimizations.

### Exception Handling

In the tests where the test value every now and then fails the test, the ensuing
exception is thrown into the compiler black hole as well. However, after some
preliminary tests, we decided to run the tests with JVM option
```-XX:-StackTraceInThrowable```. In other words, the JVM will not generate a stack
trace. That is not a realistic scenario because stacktrace generation is enabled by
default. However, if we don't specify this option, _all_ tests will at once run well
over 20 times slower. That's 2000%. That dwarves any subtlety in performance
differences between whatever variants we choose to measure. We would, in effect, be
testing the performance of stacktrace generation.

### Light-weight Checks Only

We deliberately tested only the most light-weight checks &#8212; like the
```notNull()``` and ```lt()``` (less-than) checks. If we had picked the
```containsKey()``` check for our benchmarks, for example, we would in effect be
testing the performance of HashMap (or whatever Map implementation we would have used
for the occasion), which obviously isn't what we were after.

### Message Interpolation

Apart from stacktrace generation, which makes everything else pale into
insignificance, the one thing that turns out to influence the performance of a check
the most, is whether the error message passed to the exception is a string constant
or dynamically generated using some form of message interpolation. The
"No MsgArgs" and "WithMsgArgs" benchmarks, respectively, measure this effect. The
benchmarks for "hand-coded" checks use```String.format``` while the benchmarks for
Klojang Check use Klojang Check's own message interpolation mechanism.

In both cases performance degrades significantly. Note though that, by definition,
this effect only kicks in once the check already finds itself on the "anomalous" 
branch - where the value has failed to pass the test and an exception needs to be 
thrown. Also note that the effect really only becomes pronounced if the check 
keeps on rejecting values. That may mean:

- You have a DDOS attack (heads up - your check is holding strong)
- A programmer calling your method is calling it the wrong way (heads up - your check
  is holding strong)
- There was something wrong with the check itself (you call home to say you won't
  make it for diner)

In all of these cases the relative sluggishness of the exception generation probably
is the least of your worries.

### Suppressing Message Parsing

The "VarArgsNull" benchmarks measure the effect of specifying null for the varargs
message arguments array. This is explicitly allowed. It signals to Klojang Check that
the message contains no message arguments and must be passed as-is to the exception.
As you can see, it does help somewhat, but it only makes sense for applications that
run with stacktrace generation disabled &#8212; and then only if you expect to
process a _whole_ lot of invalid/illegal values. Otherwise it is just silly.

## Test Results

### Benchmarks for CommonChecks.notNull (null check)

#### NotNull_100_Percent_Pass

```
Benchmark                                                     Mode  Cnt   Score   Error  Units
NotNull_100_Percent_Pass.customException                      avgt    9  11.371 ± 0.010  ns/op
NotNull_100_Percent_Pass.customMessage_NoMsgArgs              avgt    9  11.408 ± 0.080  ns/op
NotNull_100_Percent_Pass.customMessage_NoMsgArgs_VarArgsNull  avgt    9  11.295 ± 0.185  ns/op
NotNull_100_Percent_Pass.customMessage_WithMsgArgs            avgt    9  11.390 ± 0.039  ns/op
NotNull_100_Percent_Pass.handCoded_NoMsgArgs                  avgt    9  11.373 ± 0.026  ns/op
NotNull_100_Percent_Pass.handCoded_WithMsgArgs                avgt    9  11.365 ± 0.017  ns/op
NotNull_100_Percent_Pass.prefabMessage                        avgt    9  11.401 ± 0.071  ns/op
```

#### NotNull_099_Percent_Pass

```
Benchmark                                                     Mode  Cnt   Score   Error  Units
NotNull_099_Percent_Pass.customException                      avgt   15  11.756 ± 0.108  ns/op
NotNull_099_Percent_Pass.customMessageWithMsgArgs             avgt   15  12.750 ± 0.116  ns/op
NotNull_099_Percent_Pass.customMessage_NoMsgArgs              avgt   15  12.238 ± 0.329  ns/op
NotNull_099_Percent_Pass.customMessage_NoMsgArgs_VarArgsNull  avgt   15  11.943 ± 0.753  ns/op
NotNull_099_Percent_Pass.handCoded_NoMsgArgs                  avgt   15  11.672 ± 0.054  ns/op
NotNull_099_Percent_Pass.handCoded_WithMsgArgs                avgt   15  12.413 ± 0.043  ns/op
NotNull_099_Percent_Pass.prefabMessage                        avgt   15  11.782 ± 0.053  ns/op
```

#### NotNull_050_Percent_Pass

```
Benchmark                                                     Mode  Cnt   Score   Error  Units
NotNull_050_Percent_Pass.customException                      avgt   15  24.490 ± 0.135  ns/op
NotNull_050_Percent_Pass.customMessage_NoMsgArgs              avgt   15  26.021 ± 0.241  ns/op
NotNull_050_Percent_Pass.customMessage_NoMsgArgs_VarArgsNull  avgt   15  24.442 ± 0.116  ns/op
NotNull_050_Percent_Pass.customMessage_WithMsgArgs            avgt   15  55.490 ± 9.749  ns/op
NotNull_050_Percent_Pass.handCoded_NoMsgArgs                  avgt   15  24.438 ± 0.116  ns/op
NotNull_050_Percent_Pass.handCoded_WithMsgArgs                avgt   15  61.442 ± 0.700  ns/op
NotNull_050_Percent_Pass.prefabMessage                        avgt   15  28.093 ± 0.429  ns/op
```

### Benchmarks for CommonChecks.lt (less-than check)

#### LessThan_100_Percent_Pass

```
Benchmark                                                Mode  Cnt   Score   Error  Units
LessThan_100_Percent_Pass.customException                avgt   30  11.484 ± 0.049  ns/op
LessThan_100_Percent_Pass.customMessageNoMsgArgs         avgt   30  11.547 ± 0.101  ns/op
LessThan_100_Percent_Pass.customMessageNoMsgArgsWithEOM  avgt   30  11.500 ± 0.025  ns/op
LessThan_100_Percent_Pass.customMessageWithMsgArgs       avgt   30  11.456 ± 0.025  ns/op
LessThan_100_Percent_Pass.handCoded                      avgt   30  11.496 ± 0.017  ns/op
LessThan_100_Percent_Pass.handCodedStringFormatErrMsg    avgt   30  11.477 ± 0.028  ns/op
LessThan_100_Percent_Pass.prefabMessage                  avgt   30  11.476 ± 0.021  ns/op
```

#### LessThan_099_Percent_Pass

```
Benchmark                                                Mode  Cnt   Score   Error  Units
LessThan_099_Percent_Pass.customException                avgt   30  11.883 ± 0.046  ns/op
LessThan_099_Percent_Pass.customMessageNoMsgArgs         avgt   30  12.483 ± 0.025  ns/op
LessThan_099_Percent_Pass.customMessageNoMsgArgsWithEOM  avgt   30  12.300 ± 0.078  ns/op
LessThan_099_Percent_Pass.customMessageWithMsgArgs       avgt   30  13.321 ± 0.048  ns/op
LessThan_099_Percent_Pass.handCoded                      avgt   30  11.860 ± 0.015  ns/op
LessThan_099_Percent_Pass.handCodedStringFormatErrMsg    avgt   30  13.067 ± 0.164  ns/op
LessThan_099_Percent_Pass.prefabMessage                  avgt   30  12.218 ± 0.196  ns/op
```

#### LessThan_050_Percent_Pass

```
Benchmark                                                Mode  Cnt   Score   Error  Units
LessThan_050_Percent_Pass.customException                avgt   30  24.694 ± 0.029  ns/op
LessThan_050_Percent_Pass.customMessageNoMsgArgs         avgt   30  26.609 ± 0.188  ns/op
LessThan_050_Percent_Pass.customMessageNoMsgArgsWithEOM  avgt   30  24.906 ± 0.159  ns/op
LessThan_050_Percent_Pass.customMessageWithMsgArgs       avgt   30  66.449 ± 7.601  ns/op
LessThan_050_Percent_Pass.handCoded                      avgt   30  24.768 ± 0.081  ns/op
LessThan_050_Percent_Pass.handCodedStringFormatErrMsg    avgt   30  86.987 ± 2.372  ns/op
LessThan_050_Percent_Pass.prefabMessage                  avgt   30  58.565 ± 5.785  ns/op
   ```

### Benchmarks for CommonChecks.instanceOf

This check verifies that the argument is an instance of some class.

#### InstanceOf_100_Percent_Pass

```
Benchmark                                                Mode  Cnt   Score   Error  Units
InstanceOf_100_Percent_Pass.customException              avgt   24  25.275 ± 0.278  ns/op
InstanceOf_100_Percent_Pass.customMessageNoMsgArgs       avgt   24  25.368 ± 0.347  ns/op
InstanceOf_100_Percent_Pass.customMessageWithMsgArgs     avgt   24  25.433 ± 0.390  ns/op
InstanceOf_100_Percent_Pass.handCoded                    avgt   24  25.253 ± 0.330  ns/op
InstanceOf_100_Percent_Pass.handCodedStringFormatErrMsg  avgt   24  25.213 ± 0.285  ns/op
InstanceOf_100_Percent_Pass.prefabMessage                avgt   24  25.392 ± 0.214  ns/op
```

#### InstanceOf_099_Percent_Pass

```
Benchmark                                                Mode  Cnt   Score   Error  Units
InstanceOf_099_Percent_Pass.customException              avgt   24  25.321 ± 0.291  ns/op
InstanceOf_099_Percent_Pass.customMessageNoMsgArgs       avgt   24  25.565 ± 0.366  ns/op
InstanceOf_099_Percent_Pass.customMessageWithMsgArgs     avgt   24  26.609 ± 0.461  ns/op
InstanceOf_099_Percent_Pass.handCoded                    avgt   24  25.510 ± 0.219  ns/op
InstanceOf_099_Percent_Pass.handCodedStringFormatErrMsg  avgt   24  26.370 ± 0.644  ns/op
InstanceOf_099_Percent_Pass.prefabMessage                avgt   24  25.755 ± 0.366  ns/op
```

#### InstanceOf_050_Percent_Pass

```
Benchmark                                                Mode  Cnt    Score    Error  Units
InstanceOf_050_Percent_Pass.customException              avgt   16   36.857 ±  0.597  ns/op
InstanceOf_050_Percent_Pass.customMessageNoMsgArgs       avgt   16   36.326 ±  0.671  ns/op
InstanceOf_050_Percent_Pass.customMessageWithMsgArgs     avgt   16  101.200 ± 11.686  ns/op
InstanceOf_050_Percent_Pass.handCoded                    avgt   16   37.075 ±  0.585  ns/op
InstanceOf_050_Percent_Pass.handCodedStringFormatErrMsg  avgt   16   95.903 ±  2.497  ns/op
InstanceOf_050_Percent_Pass.prefabMessage                avgt   16   52.390 ±  6.755  ns/op
```
