package snake.logic

import engine.random.{RandomGenerator, ScalaRandomGen}
import snake.logic.GameLogic._

/** To implement Snake, complete the ``TODOs`` below.
 *
 * If you need additional files,
 * please also put them in the ``snake`` package.
 */
class GameLogic(val random: RandomGenerator,
                val gridDims : Dimensions) {
  

  private var direction : Direction = East()
  private var curGameState : GameState = GameState(initialSnake, 
                                                  newApple(initialSnake), 0, 
                                                  direction, false)
  private var reversing = false
  private var pastGameStates : List[GameState] = List()
    
  private def newApple(newSnake : List[Point]) : Option[Point] = {
    val freePoints : Seq[Point] = gridDims.allPointsInside.filter(point => !newSnake.contains(point))
    if (freePoints.nonEmpty) {
      Some(freePoints(random.randomInt(freePoints.size)))
    } else {
      None
    }
  }

  case class GameState(val snake : List[Point],
                  val apple : Option[Point],
                  val growthLeft : Int,
                  val directionLocal : Direction, // needed to forbid turning backwards
                  val gameOver : Boolean) {
    def move(direction: Direction, gridDims: Dimensions): GameState = {
      if (gameOver) { return this }
      val head = snake.head

      val newHead = direction match {
        case East()  => Point((head.x + 1) % gridDims.width, head.y)
        case West()  => Point((head.x - 1 + gridDims.width) % gridDims.width, head.y)
        case North() => Point(head.x, (head.y - 1 + gridDims.height) % gridDims.height)
        case South() => Point(head.x, (head.y + 1) % gridDims.height)
      }
      
      val grow = growthLeft > 0
      val newSnake = if (grow) { snake } 
                    else { snake.dropRight(1) }

      val ateApple = apple.contains(newHead)

      pastGameStates = curGameState :: pastGameStates
      GameState(
        snake = newHead :: newSnake,
        apple = if (ateApple) { newApple(newHead :: newSnake) }
                else { apple },
        growthLeft = growthLeft 
                    + (if (ateApple) { 3 } else { 0 })
                    - (if (grow) { 1 } else { 0 }),
        directionLocal = direction,
        gameOver = newSnake.contains(newHead)
      )
    }
  }


  def step(): Unit = {
    if (!reversing) { curGameState = curGameState.move(direction, gridDims) }
    else { decrementGameState() }
  }

  def decrementGameState(): Unit = {
    if (pastGameStates.nonEmpty) {
      curGameState = pastGameStates(0)
      pastGameStates = pastGameStates.drop(1)
      direction = curGameState.directionLocal
    }
  }

  def getCellType(p: Point): CellType = {
    if (p == curGameState.snake.head) {
      return SnakeHead(direction)
    } else if (curGameState.snake.contains(p)) {
      return SnakeBody()
    } else if (curGameState.apple.contains(p)) {
      return Apple()
    } else {
      return Empty()
    }
  }

  def changeDir(d: Direction): Unit = {
    if (d != curGameState.directionLocal.opposite) {
      direction = d
    }
  }

  def gameOver: Boolean = curGameState.gameOver
  

  def setReverse(r: Boolean): Unit = ( reversing = r )
}

/** GameLogic companion object */
object GameLogic {

  val initialSnake = List(Point(2, 0), Point(1, 0), Point(0, 0))

  val FramesPerSecond: Int = 5 // change this to increase/decrease speed of game

  val DrawSizeFactor = 1.0 // increase this to make the game bigger (for high-res screens)
  // or decrease to make game smaller

  // These are the dimensions used when playing the game.
  // When testing the game, other dimensions are passed to
  // the constructor of GameLogic.
  //
  // DO NOT USE the variable DefaultGridDims in your code!
  //
  // Doing so will cause tests which have different dimensions to FAIL!
  //
  // In your code only use gridDims.width and gridDims.height
  // do NOT use DefaultGridDims.width and DefaultGridDims.height
  val DefaultGridDims
    : Dimensions =
    Dimensions(width = 15, height = 15)  // you can adjust these values to play on a different sized board



}


