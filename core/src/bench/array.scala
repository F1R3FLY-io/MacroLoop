package be.adamv.macroloop.bench

import org.openjdk.jmh.annotations.*

import java.util.concurrent.TimeUnit
import org.openjdk.jmh.infra.Blackhole

import be.adamv.macroloop.*


@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 250, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 250, timeUnit = TimeUnit.MILLISECONDS)
@Fork(2, warmups = 2)
@State(Scope.Benchmark)
class ArraySum:
  private val ar = Array.range(1, 10001)

  @Benchmark
  def baseline_const =
    50005000

  @Benchmark
  def default_sum =
    ar.sum

  @Benchmark
  def default_for =
    var k = 0
    for i <- ar do
      k += i
    k

  @Benchmark
  def default_while_it =
    var k = 0
    val it = ar.iterator
    while it.hasNext do
      k += it.next()
    k

  @Benchmark
  def default_while_index =
    var k = 0
    var i = 0
    while i < ar.length do
      k += ar(i)
      i += 1
    k

  @Benchmark
  def macro_while_index =
    var k = 0
    ArrayIndex.forEach(ar)(k += _)
    k

  @Benchmark
  def macro_while_index_unrolled_4 =
    var k = 0
    ArrayIndex.forEachUnrolledN(4)(ar)(k += _)
    k

  @Benchmark
  def macro_while_index_unrolled_32 =
    var k = 0
    ArrayIndex.forEachUnrolledN(32)(ar)(k += _)
    k

  @Benchmark
  def port_while_index =
    var k = 0
    port.ArrayIndex.forEach[Int, Unit](ar.asInstanceOf, k += _)
    k
end ArraySum


@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 250, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 250, timeUnit = TimeUnit.MILLISECONDS)
@Fork(2, warmups = 2)
@State(Scope.Benchmark)
class ArraySumJava:
  private val ar = Array.range(1, 10001).map(Int.box)

  @Benchmark
  def baseline_const =
    50005000

  @Benchmark
  def default_sum =
    ar.sum(using Numeric.IntIsIntegral.asInstanceOf)

  @Benchmark
  def default_for =
    var k = 0
    for i <- ar do
      k += i
    k

  @Benchmark
  def default_while_it =
    var k = 0
    val it = ar.iterator
    while it.hasNext do
      k += it.next()
    k

  @Benchmark
  def default_while_index =
    var k = 0
    var i = 0
    while i < ar.length do
      k += ar(i)
      i += 1
    k

  @Benchmark
  def macro_while_index =
    var k = 0
    ArrayIndex.forEach(ar)(k += _)
    k

  @Benchmark
  def macro_while_index_unrolled_4 =
    var k = 0
    ArrayIndex.forEachUnrolledN(4)(ar)(k += _)
    k

  @Benchmark
  def macro_while_index_unrolled_32 =
    var k = 0
    ArrayIndex.forEachUnrolledN(32)(ar)(k += _)
    k

  @Benchmark
  def port_while_index =
    var k = 0
    port.ArrayIndex.forEach[java.lang.Integer, Unit](ar, k += _)
    k
end ArraySumJava


@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 250, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 250, timeUnit = TimeUnit.MILLISECONDS)
@Fork(2, warmups = 2)
@State(Scope.Benchmark)
class ArrayForall:
  @Param(Array("10", "990", "-1"))
  var matching: Int = _

  private val ar = Array.range(1, 1001)

  @Benchmark
  def baseline_const =
    matching == -1

  @Benchmark
  def default_forall =
    ar.forall(_ != matching)

  @Benchmark
  def default_for: Boolean =
    for i <- ar do
      if i == matching then
        return false
    true

  @Benchmark
  def default_while_it_return: Boolean =
    val it = ar.iterator
    while it.hasNext do
      if it.next() == matching then
        return false
    true

  @Benchmark
  def macro_while_index_exception =
    ArrayIndex.forallException(ar)(_ != matching)

  @Benchmark
  def macro_while_index_condition =
    ArrayIndex.forallCondition(ar)(_ != matching)

  @Benchmark
  def port_while_index =
    var k = 0
    port.ArrayIndex.forall[Int](ar.asInstanceOf, _ != matching)
    k
end ArrayForall


@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 250, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 250, timeUnit = TimeUnit.MILLISECONDS)
@Fork(2, warmups = 2)
@State(Scope.Benchmark)
class ArrayForallMod:
  @Param(Array("57", "1977", "1000"))
  var matching: Int = _

  private val ar: Array[Int] = Array.tabulate(1000)(i => 2*i + i % 20)

  @Benchmark
  def baseline_const =
    matching == -1

  @Benchmark
  def default_forall =
    ar.forall(_ != matching)

  @Benchmark
  def default_for: Boolean =
    for i <- ar do
      if i == matching then
        return false
    true

  @Benchmark
  def default_while_it_return: Boolean =
    val it = ar.iterator
    while it.hasNext do
      if it.next() == matching then
        return false
    true

  @Benchmark
  def macro_while_index_exception =
    ArrayIndex.forallException(ar)(_ != matching)

  @Benchmark
  def macro_while_index_condition =
    ArrayIndex.forallCondition(ar)(_ != matching)

  @Benchmark
  def port_while_index =
    port.ArrayIndex.forall[Int](ar.asInstanceOf, _ != matching)
end ArrayForallMod


@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 250, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 250, timeUnit = TimeUnit.MILLISECONDS)
@Fork(2, warmups = 2)
@State(Scope.Benchmark)
class ArrayCopyParts:
  @Param(Array("small-single", "small-all", "large-few", "large-many"))
  var mode: String = _

  protected var elements: Int = _
  protected var divisions: Int = _
  protected var nested: Array[Array[Int]] = _
  protected var flat: Array[Int] = _

  @Setup
  def setup =
    mode match
      case "small-single" =>
        elements = 10
        divisions = 1
        nested = Array(Array.fill(10)(1))
        flat = Array.fill(10)(1)
      case "small-all" =>
        elements = 10
        divisions = 10
        nested = Array.fill(10)(Array(1))
        flat = Array.fill(10)(1)
      case "large-few" =>
        elements = 1000
        divisions = 10
        nested = Array.fill(10)(Array.fill(100)(1))
        flat = Array.fill(1000)(1)
      case "large-many" =>
        elements = 1000
        divisions = 100
        nested = Array.fill(100)(Array.fill(10)(1))
        flat = Array.fill(1000)(1)

  @Benchmark
  def flat_clone =
    flat.clone()

  @Benchmark
  def nested_clone =
    nested.map(_.clone())

  @Benchmark
  def to_flat_tabulate =
    val ar = new Array[Int](elements)
    val isize = elements/divisions
    var i = 0
    while i < divisions do
      var j = 0
      while j < isize do
        ar(i*isize + j) = nested(i)(j)
        j += 1
      i += 1
    ar

  @Benchmark
  def to_nested_tabulate =
    val ar = new Array[Array[Int]](divisions)
    val isize = elements/divisions
    var i = 0
    while i < divisions do
      var j = 0
      val iar = new Array[Int](isize)
      while j < isize do
        iar(j) = flat(i*isize + j)
        j += 1
      ar(i) = iar
      i += 1
    ar

  @Benchmark
  def to_flat_copy =
    val ar = new Array[Int](elements)
    val isize = elements/divisions
    var i = 0
    while i < divisions do
      Array.copy(nested(i), 0, ar, i*isize, isize)
      i += 1
    ar

  @Benchmark
  def to_nested_copy =
    val ar = new Array[Array[Int]](divisions)
    val isize = elements/divisions
    var i = 0
    while i < divisions do
      val iar = new Array[Int](isize)
      Array.copy(flat, i*isize, iar, 0, isize)
      ar(i) = iar
      i += 1
    ar
end ArrayCopyParts
