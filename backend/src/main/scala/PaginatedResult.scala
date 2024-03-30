case class PaginatedResult[T](
                               entities: Seq[T],
                               totalCount: Int
                             )
