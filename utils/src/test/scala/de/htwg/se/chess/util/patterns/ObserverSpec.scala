/*                                                                                      *\
**     _________  ______________________                                                **
**    /  ___/  / /  /  ____/  ___/  ___/        2021 Emanuel Kupke & Marcel Biselli     **
**   /  /  /  /_/  /  /__  \  \  \  \           https://github.com/emanuelk02/Chess     **
**  /  /__/  __   /  /___ __\  \__\  \                                                  **
**  \    /__/ /__/______/______/\    /         Software Engineering | HTWG Constance    **
**   \__/                        \__/                                                   **
**                                                                                      **
\*                                                                                      */


package de.htwg.se.chess
package util
package patterns

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._


class TestObservable extends Observable {}
class TestObserver(observable: Observable) extends Observer:
    val observed = observable
    var num = 0
    override def update: Unit = { num = num + 1 }
    override def updateOnError(message: String): Unit = { num = num - 1 }

class ObserverSpec extends AnyWordSpec:
  "An Observer" should {
    "be able to observe Observables and be notified of changes withing his observed object" in {
      val observable = TestObservable()
      val observer1 = TestObserver(observable)
      val observer2 = TestObserver(observable)

      observer1.observed.add(observer1)
      observable.subscribers should contain(observer1)
      observer2.observed.add(observer2)
      observable.subscribers should contain(observer2)

      observable.notifyOnError("")
      
      observer1.num should be(-1)
      observer2.num should be(-1)
      
      observable.remove(observer2)
      observable.subscribers should not contain (observer2)

      observable.notifyObservers

      observer1.num should be(0)
      observer2.num should be(-1)
    }
  }
