package part2

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference
import scala.annotation.tailrec
import scala.collection.immutable.Map

/**
 * A banking system for managing multiple accounts and transactions between them.
 * Supports asynchronous transaction processing with automatic retry on failure,
 * up to a configurable retry limit.
 *
 * @param allowedAttempts maximum allowed retries for failed transactions
 */
class Bank(val allowedAttempts: Integer = 3):
  private val accountsRegistry = AtomicReference(Map[String, Account]())
  val transactionsPool = TransactionPool()
  val completedTransactions = TransactionPool()

  /**
   * Generates a unique account code with the format "ACC-XXXX", where X is an alphanumeric character.
   *
   * @return a string representing a unique account code.
   */
  private def generateAccountCode(): String =
    s"ACC-${UUID.randomUUID().toString.take(4).toUpperCase}"

  /**
   * Checks if there are any transactions currently being processed in the transaction pool.
   *
   * @return true if there are pending transactions, false otherwise.
   */
  def processing(): Boolean = !transactionsPool.isEmpty

  /**
   * Initiates a transfer between two accounts by adding a new transaction to the pool.
   *
   * @param from   the account code to transfer money from
   * @param to     the account code to transfer money to
   * @param amount the amount of money to transfer
   */
  def transfer(from: String, to: String, amount: Double): Unit =
    transactionsPool.add(Transaction(from, to, amount))

  /**
   * Processes all pending transactions in the transaction pool, retrying failed
   * transactions up to the allowed attempts. Uses multi-threading to handle
   * transactions concurrently.
   */
  def processTransactions(): Unit =
    transactionsPool.iterator
      .toList
      .filter(_.isPending)
      .map(processSingleTransaction)
      .foreach { worker =>
        worker.start()
        worker.join()
      }

    // Move succeeded transactions to completed pool
    transactionsPool.iterator
      .toList
      .filter(_.hasSucceeded)
      .foreach { t =>
        transactionsPool.remove(t)
        completedTransactions.add(t)
      }

    // Handle failed transactions
    transactionsPool.iterator
      .toList
      .filter(_.hasFailed)
      .foreach { t =>
        if (t.retries >= allowedAttempts) {
          transactionsPool.remove(t)
          completedTransactions.add(t)
        } else t.resetStatus()
      }

    if (processing()) {
      processTransactions()
    }

  /**
   * Processes a single transaction in a separate thread.
   *
   * @param t the transaction to process
   * @return a thread instance handling the transaction.
   */
  private def processSingleTransaction(t: Transaction): Thread =
    Thread(() => {
      val fromAccountOpt = accountsRegistry.get().get(t.from)
      val toAccountOpt = accountsRegistry.get.get(t.to)

      (fromAccountOpt, toAccountOpt) match
        case (Some(fromAccount), Some(toAccount)) =>
          fromAccount.withdraw(t.amount) match
            case Right(updatedFromAccount) =>
              toAccount.deposit(t.amount) match
                case Right(updatedToAccount) =>
                  updateAccountsRegistry(fromAccount.code,
                    updatedFromAccount,
                    toAccount.code,
                    updatedToAccount)
                  t.markSuccess()
                case Left(_) => t.markFailed()
            case Left(_) => t.markFailed()
        case _ => t.markFailed()
    })

  /**
   * Atomically updates the account registry with the new states following a successful transaction.
   *
   * @param fromCode           the source account code
   * @param updatedFromAccount the updated source account
   * @param toCode             the destination account code
   * @param updatedToAccount   the updated destination account
   */
  private def updateAccountsRegistry(fromCode: String,
                                     updatedFromAccount: Account,
                                     toCode: String,
                                     updatedToAccount: Account): Unit =
    accountsRegistry.updateAndGet { registry =>
      registry
        .updated(fromCode, updatedFromAccount)
        .updated(toCode, updatedToAccount)
    }

  /**
   * Creates a new account with the specified initial balance.
   * Generates a unique account code and adds the account to the registry.
   *
   * @param initialBalance the initial balance for the account
   * @return the unique account code for the newly created account.
   * @throws IllegalArgumentException if the initial balance is negative.
   */
  def createAccount(initialBalance: Double): String =
    if (initialBalance < 0) throw IllegalArgumentException("Initial balance cannot be negative")

    @tailrec
    def getUniqueAccountCode: String =
      val code = generateAccountCode()

      if (!accountsRegistry.get().contains(code)) code
      else getUniqueAccountCode

    val code = getUniqueAccountCode
    accountsRegistry.getAndUpdate { registry =>
      registry.updated(code, Account(code, initialBalance))
    }
    code

  /**
   * Retrieves an account from the registry by its code.
   *
   * @param code the account code to look up
   * @return `Some(Account)` if the account exists, `None` otherwise.
   */
  def getAccount(code: String): Option[Account] =
    accountsRegistry.get().get(code)
