package legion

import akka.actor.{
  ActorSystem,
  ClassicActorSystemProvider,
  ExtendedActorSystem,
  Extension,
  ExtensionId,
  ExtensionIdProvider
}

class Legion(val system: ExtendedActorSystem) extends Extension {}

object Legion extends ExtensionId[Legion] with ExtensionIdProvider {
  override def get(system: ActorSystem): Legion = super.get(system)

  override def get(system: ClassicActorSystemProvider): Legion = super.get(system)

  override def lookup: Legion.type = Legion

  override def createExtension(system: ExtendedActorSystem): Legion = new Legion(system)
}
