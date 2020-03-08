package legion.data

case class Path(private val segments: List[String]) {

  def /(segment: String): Path = Path(segment :: segments)

  override def toString: String =
    if (segments.isEmpty) "/"
    else segments.foldRight("") { case (segment, prefix) => s"$prefix/$segment" }

}

object Path {

  val root: Path = Path(Nil)

}
