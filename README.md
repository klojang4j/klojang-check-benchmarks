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
if(condition){
    throw new IllegalArgumentException("an exception message");
}    
```

For example, for the null check, the hand-coded check looks like this:

```java
if(randomizedTestVal==null){
    throw new IllegalArgumentException("arg must not be null");
}    
```

The Klojang Check counterparts to this check would look like this:

```java
// prefab message from Klojang Check
Check.that(arg,"arg").is(notNull());
// custom message
Check.that(arg).is(notNull(),"arg must not be null");
// custom exception
Check.that(arg).is(notNull(),
    ()->new IllegalArgumentException("arg must not be null"));
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
thrown. Also note that the effect really only becomes pronounced if the check keeps
on rejecting values. That may mean:

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
Benchmark                                                      Mode  Cnt   Score   Error  Units
LessThan_100_Percent_Pass.customException                      avgt   15  11.514 ± 0.040  ns/op
LessThan_100_Percent_Pass.customMessage_NoMsgArgs              avgt   15  11.511 ± 0.041  ns/op
LessThan_100_Percent_Pass.customMessage_NoMsgArgs_VarArgsNull  avgt   15  11.483 ± 0.056  ns/op
LessThan_100_Percent_Pass.customMessage_WithMsgArgs            avgt   15  11.462 ± 0.046  ns/op
LessThan_100_Percent_Pass.handCoded_NoMsgArgs                  avgt   15  11.503 ± 0.052  ns/op
LessThan_100_Percent_Pass.handCoded_WithMsgArgs                avgt   15  11.467 ± 0.045  ns/op
LessThan_100_Percent_Pass.prefabMessage                        avgt   15  11.485 ± 0.017  ns/op
```

#### LessThan_099_Percent_Pass

```
Benchmark                                                      Mode  Cnt   Score   Error  Units
LessThan_099_Percent_Pass.customException                      avgt   15  11.861 ± 0.046  ns/op
LessThan_099_Percent_Pass.customMessage_NoMsgArgs              avgt   15  12.423 ± 0.123  ns/op
LessThan_099_Percent_Pass.customMessage_NoMsgArgs_VarArgsNull  avgt   15  11.805 ± 0.097  ns/op
LessThan_099_Percent_Pass.customMessage_WithMsgArgs            avgt   15  14.348 ± 0.174  ns/op
LessThan_099_Percent_Pass.handCoded_NoMsgArgs                  avgt   15  11.859 ± 0.044  ns/op
LessThan_099_Percent_Pass.handCoded_WithMsgArgs                avgt   15  12.923 ± 0.212  ns/op
LessThan_099_Percent_Pass.prefabMessage                        avgt   15  12.514 ± 0.263  ns/op
```

#### LessThan_050_Percent_Pass

```
Benchmark                                                      Mode  Cnt    Score    Error  Units
LessThan_050_Percent_Pass.customException                      avgt   15   24.735 ±  0.078  ns/op
LessThan_050_Percent_Pass.customMessage_NoMsgArgs              avgt   15   26.554 ±  0.144  ns/op
LessThan_050_Percent_Pass.customMessage_NoMsgArgs_VarArgsNull  avgt   15   24.759 ±  0.043  ns/op
LessThan_050_Percent_Pass.customMessage_WithMsgArgs            avgt   15  114.104 ± 12.687  ns/op
LessThan_050_Percent_Pass.handCoded_NoMsgArgs                  avgt   15   26.350 ±  2.546  ns/op
LessThan_050_Percent_Pass.handCoded_WithMsgArgs                avgt   15   87.720 ±  2.741  ns/op
LessThan_050_Percent_Pass.prefabMessage                        avgt   15   58.398 ±  0.777  ns/op
```

### Benchmarks for CommonChecks.instanceOf

This check verifies that the argument is an instance of some class.

#### InstanceOf_100_Percent_Pass

```
Benchmark                                                        Mode  Cnt   Score   Error  Units
InstanceOf_100_Percent_Pass.customException                      avgt   15  25.085 ± 0.445  ns/op
InstanceOf_100_Percent_Pass.customMessageWithMsgArgs             avgt   15  25.356 ± 0.467  ns/op
InstanceOf_100_Percent_Pass.customMessage_NoMsgArgs              avgt   15  25.275 ± 0.522  ns/op
InstanceOf_100_Percent_Pass.customMessage_NoMsgArgs_VarArgsNull  avgt   15  25.488 ± 0.542  ns/op
InstanceOf_100_Percent_Pass.handCoded_NoMsgArgs                  avgt   15  25.283 ± 0.358  ns/op
InstanceOf_100_Percent_Pass.handCoded_WithMsgArgs                avgt   15  25.260 ± 0.360  ns/op
InstanceOf_100_Percent_Pass.prefabMessage                        avgt   15  25.340 ± 0.316  ns/op
```

#### InstanceOf_099_Percent_Pass

```
Benchmark                                                        Mode  Cnt   Score   Error  Units
InstanceOf_099_Percent_Pass.customException                      avgt   15  26.623 ± 1.408  ns/op
InstanceOf_099_Percent_Pass.customMessage_NoMsgArgs              avgt   15  26.681 ± 1.777  ns/op
InstanceOf_099_Percent_Pass.customMessage_NoMsgArgs_VarArgsNull  avgt   15  26.306 ± 1.954  ns/op
InstanceOf_099_Percent_Pass.customMessage_WithMsgArgs            avgt   15  26.309 ± 1.071  ns/op
InstanceOf_099_Percent_Pass.handCoded_NoMsgArgs                  avgt   15  25.256 ± 0.434  ns/op
InstanceOf_099_Percent_Pass.handCoded_WithMsgArgs                avgt   15  26.832 ± 0.541  ns/op
InstanceOf_099_Percent_Pass.prefabMessage                        avgt   15  25.735 ± 0.667  ns/op
```

#### InstanceOf_050_Percent_Pass

```
Benchmark                                                       Mode  Cnt   Score   Error  Units
InstanceOf_050_Percent_Pass.customException                     avgt   15  37.474 ± 0.658  ns/op
InstanceOf_050_Percent_Pass.customMessageNoMsgArgs              avgt   15  38.951 ± 0.773  ns/op
InstanceOf_050_Percent_Pass.customMessageNoMsgArgs_VarArgsNull  avgt   15  37.919 ± 0.792  ns/op
InstanceOf_050_Percent_Pass.customMessage_WithMsgArgs           avgt   15  97.589 ± 8.929  ns/op
InstanceOf_050_Percent_Pass.handCoded_NoMsgArgs                 avgt   15  37.444 ± 0.911  ns/op
InstanceOf_050_Percent_Pass.handCoded_WithMsgArgs               avgt   15  94.689 ± 2.859  ns/op
InstanceOf_050_Percent_Pass.prefabMessage                       avgt   15  54.432 ± 7.180  ns/op
```

## Miscellaneous Benchmarks

### Performance of has()

OK, we know the trend now. No point in repeating this for each and every check within
the ```CommonChecks``` class. But note the intrinsic sluggishness of the instance-of
check **_relative_** to the null check and less-than check. It's almost 2.5 times as
slow for the "100_Percent_Pass" check. That is no surprise, of course. If the test
value's class is not referentially equal to the given class, instance-of must check
the superclass and interfaces of the test value's class. That may be why many
developers prefer

```java
if(obj.getClass() == Something.class)
```

over

```java
if(obj instanceof Something)
```

So wat about the check corresponding to the first pattern?

```java
// The hand-coded check:
if(obj.getClass() == Something.class){
    throw new IllegalArgumentException("obj has wrong type");
}

// Its straightforward translation into Klojang Check:
Check.that(obj.getClass()).is(sameAs(), Something.class);

// Not bad, but we can express this in a more idiomatic way:
Check.that(obj).has(type(), sameAs(), Something.class);
```

Sure enough, all of the above variants performance now reach parity with the null
check and the less-than check. But (for no good reason) we feared the idiomatic
variant might lag somewhat behind the hand-coded and the "straightforward" variants.
Quite the contrary, however:

```
Benchmark                             Mode  Cnt   Score   Error  Units
HasTypeEqualTo.handCoded              avgt   15  12.540 ± 0.036  ns/op
HasTypeEqualTo.arg_getClass_isSameAs  avgt   15  14.842 ± 0.070  ns/op
HasTypeEqualTo.arg_hasType_sameAs     avgt   15  12.588 ± 0.054  ns/op
```

(Of course we ran the benchmark a few times, but the result always looked like this
one.) We cannot currently explain this. All we can say (or rather conclude) is:
the JVM just has become really really good at lambdas &#8212; or dynamic invocation
in general.

