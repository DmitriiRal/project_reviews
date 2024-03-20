package Steam

case class GameInfo(
                     name: String,
                     id: Long,
                     description: String,
                     steamId: Long,
                     steamLink: String,
                     gamePictureBig: String,
                     gamePictureSmall: String
                   )
