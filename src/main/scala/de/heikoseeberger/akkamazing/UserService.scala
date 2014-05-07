/*
 * Copyright 2014 Heiko Seeberger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.heikoseeberger.akkamazing

import akka.actor.{ Actor, ActorLogging, Props }
import spray.json.DefaultJsonProtocol

object UserService {

  // GetUsers

  case object GetUsers

  sealed trait GetUsersResponse

  object GetUsersResponse {

    object Users extends DefaultJsonProtocol {
      implicit val format = jsonFormat1(apply)
    }

    case class Users(names: Set[String]) extends GetUsersResponse
  }

  // SignUp

  object SignUp extends DefaultJsonProtocol {
    implicit val format = jsonFormat1(apply)
  }

  case class SignUp(name: String)

  sealed trait SignUpResponse

  object SignUpResponse {

    case class SignedUp(name: String) extends SignUpResponse

    case class NameTaken(name: String) extends SignUpResponse
  }

  // Props factories

  def props: Props =
    Props(new UserService)
}

class UserService extends Actor with ActorLogging {

  import UserService._

  private var names = Set.empty[String]

  override def receive: Receive = {
    case GetUsers                            => sender() ! GetUsersResponse.Users(names)
    case SignUp(name) if names contains name => sender() ! SignUpResponse.NameTaken(name)
    case SignUp(name)                        => signUp(name)
  }

  private def signUp(name: String): Unit = {
    log.info("Signing up {}", name)
    names += name
    sender() ! SignUpResponse.SignedUp(name)
  }
}
