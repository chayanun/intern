package secure

//In database Make From Binary digit 001,011,111
sealed trait UserRole {
  val role: Int
}

case object NormalUserRole extends UserRole {
  val role: Int = 1
}

case object AdministratorRole extends UserRole {
  val role: Int = 2
}

object UserRole {
  // index all available roles
  val ALL_ROLES: List[Int] = List(NormalUserRole.role, AdministratorRole.role)

  //Make From Binary digit 001,011,111
  val USER_DESC_NORMAL_ID: Int = 1
  val USER_DESC_ADMINISTRATOR_ID: Int = 2

  val NORMAL_ROLE: String = "User"
  val ADMINISTRATOR_ROLE: String = "Administrator"

  def convertRoleToUI(roleId: Int): Set[String] = {
    extractPermission(roleId).map {
      case USER_DESC_NORMAL_ID => NORMAL_ROLE
      case USER_DESC_ADMINISTRATOR_ID => ADMINISTRATOR_ROLE
    }
  }

  def extractPermission(userRole: Int): Set[Int] = {
    val bs = userRole.toBinaryString.toList.reverse
    Set(bs.zipWithIndex.flatMap { x =>
      val (n, p) = x
      if (n == '1') Some(1 << p) else None
    }: _*)
  }
}
