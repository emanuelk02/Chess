package de.htwg.se.chess
package util

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._

class ObserverSpec extends AnyWordSpec {
  class TestObservable extends Observable {}
  class TestObserver(observable: TestObservable) extends Observer {
    val observed = observable
    var num = 0
    override def update: Unit = { num = num + 1 }
    override def updateOnError(message: String): Unit = print(
      "\n" + message + "\n"
    )
  }

  "An Observer" should {
    "be able to observe Observables and be notified of changes withing his observed object" in {
      val observable = new TestObservable()
      val observer1 = new TestObserver(observable)
      val observer2 = new TestObserver(observable)

      observer1.observed.add(observer1)
      observable.subscribers should contain(observer1)
      observer2.observed.add(observer2)
      observable.subscribers should contain(observer2)

      observable.remove(observer2)
      observable.subscribers should not contain (observer2)

      observable.notifyObservers

      observer1.num should be(1)
      observer2.num should be(0)
    }
  }
}
