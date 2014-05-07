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

package name.heikoseeberger.akkamazing

import akka.actor.{ ActorLogging, Actor, Props }
import spray.json.DefaultJsonProtocol

object UserService {

  case object GetUsers

  object Users extends DefaultJsonProtocol {
    implicit val format = jsonFormat1(apply)
  }

  case class Users(names: Set[String])

  object SignUp extends DefaultJsonProtocol {
    implicit val format = jsonFormat1(apply)
  }

  case class SignUp(name: String)

  case class SignedUp(name: String)

  case class NameTaken(name: String)

  def props: Props =
    Props(new UserService)
}

class UserService extends Actor with ActorLogging {

  import UserService._

  var names = Set.empty[String]

  override def receive: Receive = {
    case GetUsers                            => sender() ! Users(names)
    case SignUp(name) if names contains name => sender() ! NameTaken(name)
    case SignUp(name)                        => signUp(name)
  }

  def signUp(name: String): Unit = {
    log.info("Signing up {}", name)
    names += name
    sender() ! SignedUp(name)
  }
}