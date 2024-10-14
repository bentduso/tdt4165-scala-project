package no.ntnu.tdt4165
package part1

import java.util.concurrent.atomic.AtomicReference

object Task3:
  def createThread(fn: () => Unit): Thread = Thread(() => fn())

object ConcurrencyTroubles:
  private case class State(value1: Int, value2: Int)

  private val state = AtomicReference(State(1000, 0))
  private val sum = AtomicReference(0)

  private def moveOneUnit(): Unit =
    state.getAndUpdate { s =>
      if (s.value1 == 0) State(1000, 0)
      else State(s.value1 - 1, s.value2 + 1)
    }

  private def updateSum(): Unit =
    val currentState = state.get()
    sum.set(currentState.value1 + currentState.value2)

  private def execute(): Unit =
    while true do
      moveOneUnit()
      updateSum()
      Thread.sleep(100)

  @main def runThreads(): Unit =
    for (i <- 1 to 2) do
      val thread = Thread(() => execute())
      thread.start()

    while true do
      updateSum()
      val currentState = state.get()
      val currentSum = sum.get()
      println(s"$currentSum [${currentState.value1}] [${currentState.value2}]")
