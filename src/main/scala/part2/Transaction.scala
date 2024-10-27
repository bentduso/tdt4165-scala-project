package part2

import scala.collection.mutable

/**
 * An enum representing the status of a transaction.
 */
enum TransactionStatus:
  case SUCCESS, PENDING, FAILED

/**
 * Represents a bank transaction between two accounts.
 *
 * @param from    the account code from which the account will be withdrawn
 * @param to      the account code to which the account will be deposited
 * @param amount  the amount to transfer
 * @param retries the number of retry attempts if the transaction fails
 */
class Transaction(val from: String, val to: String, val amount: Double, val retries: Int = 3):
  private var status = TransactionStatus.PENDING
  private var attempts = 0

  /**
   * Returns the current status of the transaction.
   *
   * @return the transaction status.
   */
  def getStatus: TransactionStatus = status

  /**
   * Checks if the transaction is still pending.
   *
   * @return true if the transaction is pending, otherwise false.
   */
  def isPending: Boolean = status == TransactionStatus.PENDING

  /**
   * Checks if the transaction has failed.
   *
   * @return true if the transaction has failed, otherwise false.
   */
  def hasFailed: Boolean = status == TransactionStatus.FAILED

  /**
   * Checks if the transaction has succeeded.
   *
   * @return true if the transaction has succeeded, otherwise false.
   */
  def hasSucceeded: Boolean = status == TransactionStatus.SUCCESS

  /** Marks the transaction as successful. */
  def markSuccess(): Unit = this.synchronized {
    status = TransactionStatus.SUCCESS
  }

  /** Marks the transaction as failed and increments the attempt counter. */
  def markFailed(): Unit = this.synchronized {
    status = TransactionStatus.FAILED
    attempts += 1
  }

  /** Resets the transaction status to pending if there are retry attempts remaining. */
  def resetStatus(): Unit = this.synchronized {
    if (attempts < retries) status = TransactionStatus.PENDING
  }

/** Represents a pool of transactions to be processed. */
class TransactionPool:
  private val transactions = mutable.Queue[Transaction]()

  /**
   * Removes a specified transaction from the pool if it exists.
   *
   * @param t the transaction to remove
   * @return true if the transaction was successfully removed, false otherwise.
   */
  def remove(t: Transaction): Boolean = this.synchronized {
    val initialSize = size
    transactions.removeAll(_ == t)
    initialSize != size
  }

  /**
   * Checks if the transaction pool is empty.
   *
   * @return true if the transaction pool is empty, false otherwise.
   */
  def isEmpty: Boolean = this.synchronized {
    transactions.isEmpty
  }

  /**
   * Returns the current size of the transaction pool.
   *
   * @return the size of the transaction pool.
   */
  def size: Integer = this.synchronized {
    transactions.size
  }

  /**
   * Adds a transaction to the pool.
   *
   * @param t the transaction to add
   * @return true, indicating that the transaction has been added.
   */
  def add(t: Transaction): Boolean = this.synchronized {
    transactions.enqueue(t)
    true
  }

  /**
   * Returns an iterator to traverse the transactions in the pool.
   *
   * @return an iterator over the transactions.
   */
  def iterator: Iterator[Transaction] = this.synchronized {
    transactions.iterator
  }
