package akka

import akka.actor.{ActorSystem, ActorSystemImpl, DynamicAccess}
import akka.event.EventStream
import akka.remote.RemoteActorRefProvider
import legion.Legion

class LegionActorRefProvider(systemName: String,
                             settings: ActorSystem.Settings,
                             eventStream: EventStream,
                             dynamicAccess: DynamicAccess)
    extends RemoteActorRefProvider(systemName, settings, eventStream, dynamicAccess) {

  override def init(system: ActorSystemImpl): Unit = {
    super.init(system)
    Legion(system)
  }

}
