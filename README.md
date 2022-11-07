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

- Klojang Check generates both the exception message and the exception itself
- The client provides the exception message and Klojang Check generates the exception
- The client provides both the exception message and the exception itself

Each variant is again benchmarked for three scenarios: the argument _always_ passes
the test; it passes the test in 99% percent of the cases; it passes the test in 50%
of the cases.

The performance is compared with an equivalent "hand-coded" check that looks like
this:

```java
if(something_applies_to(argument)) {
    throw new IllegalArgumentException("an exception message");
}    
```

We deliberately tested only very light-weight checks, like the ```notNull()``` and
```lt()``` (less-than) checks. If we had picked the ```containsKey()``` check for
our benchmarks, for example, we would in effect be testing the performance of
HashMap (or whatever Map implementation we would have used for the occasion), which
obviously isn't what we were after.

Also, since this turns out to influence performance more than anything else, we
contrast plain, constant error messages with error messages that need to be
interpolated with message arguments. The benchmarks for "hand-coded" checks
use ```String.format``` while the benchmarks for Klojang Check use the message
interpolation mechanism used by Klojang Check.

In the ***WithEOM benchmarks shown in the test results the message arguments
(varargs) array was specified to be null. This is explicitly allowed. It signals to
Klojang Check that the message contains no message arguments and must be passed as-is
to the exception. As you can see, it does help somewhat, but only if the test
repetitively rejects any value thrown at it. Not recommended.

## Test Results

### Benchmarks for CommonChecks.notNull (null check)

#### NotNull_100_Percent_Pass

```
Benchmark                                               Mode  Cnt   Score   Error  Units
NotNull_100_Percent_Pass.customException                avgt    6  11.409 ± 0.103  ns/op
NotNull_100_Percent_Pass.customMessageNoMsgArgs         avgt    6  11.406 ± 0.114  ns/op
NotNull_100_Percent_Pass.customMessageWithMsgArgs       avgt    6  11.415 ± 0.049  ns/op
NotNull_100_Percent_Pass.handCoded                      avgt    6  11.424 ± 0.042  ns/op
NotNull_100_Percent_Pass.handCodedStringFormatErrMsg    avgt    6  11.430 ± 0.054  ns/op
NotNull_100_Percent_Pass.prefabMessage                  avgt    6  11.452 ± 0.187  ns/op
```

#### NotNull_099_Percent_Pass

```
Benchmark                                               Mode  Cnt   Score   Error  Units
NotNull_099_Percent_Pass.customException                avgt   90  14.884 ± 0.250  ns/op
NotNull_099_Percent_Pass.customMessageNoMsgArgs         avgt   90  15.578 ± 0.093  ns/op
NotNull_099_Percent_Pass.customMessageWithMsgArgs       avgt   90  16.248 ± 0.104  ns/op
NotNull_099_Percent_Pass.handCoded                      avgt   90  14.775 ± 0.178  ns/op
NotNull_099_Percent_Pass.handCodedStringFormatErrMsg    avgt   90  15.763 ± 0.146  ns/op
NotNull_099_Percent_Pass.prefabMessage                  avgt   90  15.040 ± 0.297  ns/op
```

#### NotNull_050_Percent_Pass

```
Benchmark                                               Mode  Cnt   Score   Error  Units
NotNull_050_Percent_Pass.customException                avgt   90  24.746 ± 0.105  ns/op
NotNull_050_Percent_Pass.customMessageNoMsgArgs         avgt   90  26.184 ± 0.127  ns/op
NotNull_050_Percent_Pass.customMessageNoMsgArgsWithEOM  avgt   90  24.651 ± 0.097  ns/op
NotNull_050_Percent_Pass.customMessageWithMsgArgs       avgt   90  63.345 ± 4.346  ns/op
NotNull_050_Percent_Pass.handCoded                      avgt   90  24.581 ± 0.110  ns/op
NotNull_050_Percent_Pass.handCodedStringFormatErrMsg    avgt   90  61.041 ± 0.294  ns/op
NotNull_050_Percent_Pass.prefabMessage                  avgt   90  28.526 ± 0.126  ns/op
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
