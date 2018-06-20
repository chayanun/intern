package database.driver

import com.github.tminglei.slickpg._

trait PgProfileBase extends ExPostgresProfile
  with PgArraySupport
  with PgHStoreSupport
  with PgJsonSupport
  with PgEnumSupport
  with PgDate2Support {
  override val pgjson = "jsonb"
  override val api = HelperAPI

  object HelperAPI extends API with ArrayImplicits
    with HStoreImplicits
    with JsonImplicits
    with DateTimeImplicits{}
}

object PgProfile extends PgProfileBase
