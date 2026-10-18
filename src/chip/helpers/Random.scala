package chip.helpers

case class Random(state: Long) {
  def nextLong: (Long, Random) = {
    val nextState = state + 0x9E3779B97F4A7C15L

    var value = nextState
    value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L
    value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL
    value = value ^ (value >>> 31)

    (value, Random(nextState))
  }

  def nextInt(max: Int): (Int, Random) = {
    require(max > 0, "max must be positive")

    val mask = max.toLong - 1L
    var rng = this
    var bits = 0L
    var result = 0L
    var accepted = false

    while (!accepted) {
      val (value, nextRng) = rng.nextLong
      rng = nextRng
      bits = value >>> 1
      result = bits % max
      accepted = bits - result + mask >= 0L
    }

    (result.toInt, rng)
  }
}