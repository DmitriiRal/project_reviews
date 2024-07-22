package Steam

case class SteamAppDetails(
                           name: String,
                           detailedDescription: String,
                           headerImage: String,
                           steamId: Long,
                           steamLink: String,
                           genres: List[String]
                          )
