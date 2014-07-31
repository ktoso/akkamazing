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

import akka.actor.{ ActorLogging, Props }
import akka.contrib.pattern.ShardRegion
import akka.persistence.PersistentActor
import spray.json.DefaultJsonProtocol

object UserService {

  // Sharding
  object Shard {
    val name = "UserService"

    val shardExtractor: ShardRegion.ShardResolver = {
      case m: ShardedBy => m.name.take(1)
    }

    val idExtractor: ShardRegion.IdExtractor = {
      case m: ShardedBy => (m.name, m)
    }
  }

  // Trait used by all commands sent via Cluster Sharding

  trait ShardedBy {
    def name: String
  }

  // SignUp

  object SignUp extends DefaultJsonProtocol {
    implicit val format = jsonFormat1(apply)
  }

  case class SignUp(name: String) extends ShardedBy

  sealed trait UserServiceResponse

  object UserServiceResponse {

    case class SignedUp(name: String) extends UserServiceResponse

    case class NameTaken(name: String) extends UserServiceResponse
  }

  // Props factories

  def props: Props =
    Props[UserService]
}

class UserService extends PersistentActor with ActorLogging {

  import de.heikoseeberger.akkamazing.UserService._

  override val persistenceId: String =
    "user-service"

  private var names = Set.empty[String]

  override def receiveCommand: Receive = {
    case SignUp(name) if names contains name => sender() ! UserServiceResponse.NameTaken(name)
    case SignUp(name)                        => persist(UserServiceResponse.SignedUp(name))(handleSignedUpThenRespond)
  }

  override def receiveRecover: Receive = {
    case signedUp: UserServiceResponse.SignedUp => handleSignedUp(signedUp)
  }

  private def handleSignedUpThenRespond(signedUp: UserServiceResponse.SignedUp): Unit = {
    handleSignedUp(signedUp)
    sender() ! signedUp
  }

  private def handleSignedUp(signedUp: UserServiceResponse.SignedUp): Unit = {
    val UserServiceResponse.SignedUp(name) = signedUp
    log.info("Signing up {}", name)
    names += name
  }
}
