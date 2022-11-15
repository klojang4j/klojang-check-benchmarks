# JMH Benchmarks for Klojang Check

JMH Benchmarks for some critical parts of the Klojang Check.

## How to run

- Clone this repository
- Run: mvn clean package
- Run: java -jar target/benchmarks.jar <name_of_test>

For example:

java -jar target/benchmarks.jar NotNull_100_Percent_Pass

## Test Setup

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
if(testValue = null){
    throw new IllegalArgumentException("arg must not be null");
}    
```

The Klojang Check counterparts to this check would look like this:

```java
// prefab message from Klojang Check
Check.that(testValue, "arg").is(notNull());
// custom message
Check.that(testValue).is(notNull(), "arg must not be null");
// custom exception
Check.that(arg).is(notNull(),
    () -> new IllegalArgumentException("arg must not be null"));
```

### No Stacktrace

After some preliminary tests, we decided to run the tests with JVM option
```-XX:-StackTraceInThrowable```. In other words, the JVM will not generate a stack
trace. That is not a realistic scenario because stacktrace generation is enabled by
default. However, if we do not specify this option, _all_ tests will at once run well
over 20 times slower. That's 2000%. That dwarves any subtlety in performance
differences between whatever variants we choose to measure. We would, in effect, be
testing the performance of stacktrace generation.

### Light-weight Checks

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
execution branch - where the value has failed to pass the test and an exception needs
to be thrown. Also note that the effect really only becomes pronounced if the check
keeps on rejecting values. That may mean:

- You have a DDOS attack
- A programmer calling your method is calling it the wrong way
- There was something wrong with the check itself

In all of these cases the relative sluggishness of the exception generation probably
is the least of your worries.

### Suppressing Message Parsing

The "VarArgsNull" benchmarks measure the effect of specifying null for the varargs
message arguments array. This is explicitly allowed. It signals to Klojang Check that
the message contains no message arguments and must be passed as-is to the exception.
It does help somewhat, but it only makes sense for applications that run with
stacktrace generation disabled &#8212; and then only if you expect to process a
awful amount of invalid/illegal values. Otherwise it is just silly.

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
LessThan_050_Percent_Pass.customMessage_WithMsgArgs            avgt   15   61.317 ±  3.286  ns/op
LessThan_050_Percent_Pass.handCoded_NoMsgArgs                  avgt   15   26.350 ±  2.546  ns/op
LessThan_050_Percent_Pass.handCoded_WithMsgArgs                avgt   15   77.585 ±  1.187  ns/op
LessThan_050_Percent_Pass.prefabMessage                        avgt   15   58.398 ±  0.777  ns/op
```

### Benchmarks for CommonChecks.instanceOf

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

Notice intrinsic sluggishness of the instance-of
check **_relative_** to the null check and less-than check. It's almost 2.5 times as
slow for the "100_Percent_Pass" check. No surprise there, of course. If the test
value's class is not _exactly_ equal to the given class, its type hierarchy must be 
visited. That may be why many developers prefer

```java
if(obj.getClass() == Something.class)
```

over

```java
if(obj instanceof Something)
```

So, what about the check corresponding to the reference comparison? We were 
especially interested how well the most _idiomatic_ expression of this check 
within Klojang Check would perform:

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

Sure enough, all of the above variants now reach performance parity with the null
check and the less-than check. But (for no good reason) we feared the idiomatic
variant might lag behind the hand-coded and the "straightforward" variants. Quite the
contrary, however:

```
Benchmark                             Mode  Cnt   Score   Error  Units
HasTypeEqualTo.handCoded              avgt   15  12.540 ± 0.036  ns/op
HasTypeEqualTo.arg_getClass_isSameAs  avgt   15  14.842 ± 0.070  ns/op
HasTypeEqualTo.arg_hasType_sameAs     avgt   15  12.588 ± 0.054  ns/op
```

We cannot currently explain this. All we can say (or rather conclude) is: the JVM
just has become really really good at lambdas &#8212; or dynamic invocation in
general.

### Syntactic Sugar
When composing tests, Klojang Check facilitates a syntax that feels very typical 
of Klojang Check, but that really is just syntactical sugar:
```java
// sweet:
Check.that(foo).is(notNull().andThat(bar, EQ(), "foo"));
// syntactical sugar for:
Check.that(foo).is(notNull().and(bar.equals("foo")));
```
But is it also just a waste of CPU cycles? Again &#8212; no!
```
ComposeSugarSyntax.bitter  avgt   15  11.981 ± 0.032  ns/op
ComposeSugarSyntax.sweet   avgt   15  12.009 ± 0.051  ns/op
```

### Message Interpolation By Itself

The "WithMsgArgs" benchmarks performed significantly worse than the benchmarks where
the error message was a constant string. Klojang Check's message interpolation
mechanism tends to run faster than ```String.format```, but in both cases the error
margin of the benchmark could be pretty dramatic (for reasons we don't understand and
haven't investigated). This benchmark isolates the message generation from the rest
of the check. In other words, it pits ```String.format```
against Klojang Check's message interpolation mechanism.
The ```klojangFormatWithBuiltInArgsOnly``` benchmark only uses message arguments like
```${arg}``` and ```${tag}```. ```klojangFormatWithUserArgsOnly``` only uses
positional arguments (```${0}```, ```${1}```,```${2}``` etc.), so most closely
resembles ```String.format```.

```
StringFormatting.klojangFormat_builtInArgsOnly  avgt   15  124.087 ± 3.662  ns/op
StringFormatting.klojangFormat_userArgsOnly     avgt   15  160.894 ± 3.938  ns/op
StringFormatting.stringFormat                   avgt   15  193.813 ± 2.922  ns/op
```


