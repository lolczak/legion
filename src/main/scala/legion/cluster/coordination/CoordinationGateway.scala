package legion.cluster.coordination

import akka.actor.Actor
import org.apache.zookeeper.{WatchedEvent, Watcher, ZooKeeper}

class CoordinationGateway extends Actor with Watcher {

  private val zk = new ZooKeeper("localhost:2181", 10000, this)

  override def receive: Receive = ???

  override def process(event: WatchedEvent): Unit = {}
}
