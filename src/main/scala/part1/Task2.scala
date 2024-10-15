package part1

object Task2:
  def quadraticEquation(a: Float, b: Float, c: Float): (Boolean, Option[Double], Option[Double]) =
    val discriminant = b * b - 4.0 * a * c

    if (discriminant >= 0) {
      val x1 = (-b + math.sqrt(discriminant)) / (2.0 * a)
      val x2 = (-b - math.sqrt(discriminant)) / (2.0 * a)
      (true, Some(x1), Some(x2))
    } else (false, None, None)

  def quadratic(a: Float, b: Float, c: Float): Float => Float =
    (x: Float) => a * x * x + b * x + c
    