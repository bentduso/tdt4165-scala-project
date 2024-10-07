package no.ntnu.tdt4165
package part1

object Task1:
  def generateArray(): Array[Int] =
    val arr = Array[Int](50)
    for i <- 1 to 50 do arr(i - 1) = i
    arr

  def sum(arr: Array[Int]): Int =
    var sum = 0
    for num <- arr do sum += num
    sum

  def recursiveSum(arr: Array[Int]): Int = arr match
    case Array() => 0
    case Array(head, tail*) => head + recursiveSum(tail.toArray)

  def fibonacci(n: BigInt): BigInt = n match
    case n if n == BigInt(0) => 0
    case n if n == BigInt(1) => 1
    case _ => fibonacci(n - 1) + fibonacci(n - 2)

