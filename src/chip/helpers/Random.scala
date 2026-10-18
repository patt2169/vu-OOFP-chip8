package chip.helpers

case class Random(seed:Int) {
  def nextInt(max: Int): (Int, Random) = {
    val rng = new scala.util.Random(seed)
    val res = rng.nextInt(max)
    val newRandom = Random(seed+1)
    (res, newRandom)
  } 
}
