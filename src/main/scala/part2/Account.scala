package part2

/**
 * Represents a bank account with a unique code and a balance.
 *
 * @param code    the unique code identifying the account
 * @param balance the initial balance of the account
 */
class Account(val code: String, val balance: Double) {
  /**
   * Withdraws an amount from the account. If the amount is negative,
   * or if it exceeds the available balance, it returns an error message. If successful,
   * it returns a new account instance with the updated balance.
   *
   * @param amount the amount to withdraw
   * @return either an error message as a `Left(String)`, or an updated account as `Right(Account)`.
   */
  def withdraw(amount: Double): Either[String, Account] =
    if (amount < 0) Left("Invalid amount: withdrawal amount must be positive")
    else if (amount > balance) Left(s"Insufficient funds: cannot withdraw $amount from balance $balance")
    else Right(Account(code, balance - amount))

  /**
   * Deposits an amount into the account. If the amount is negative, it returns an error
   * message. If successful, it returns a new account instance with the updated balance.
   *
   * @param amount the amount to deposit
   * @return either an error message as a `Left(String)`, or an updated account as `Right(Account)`.
   */
  def deposit(amount: Double): Either[String, Account] =
    if (amount < 0) Left("Invalid amount: deposit amount must be positive")
    else Right(Account(code, balance + amount))
}
