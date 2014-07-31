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

import akka.actor.{ PoisonPill, ReceiveTimeout, ActorLogging, Props }
import akka.contrib.pattern.ShardRegion
import akka.contrib.pattern.ShardRegion.Passivate
import akka.persistence.PersistentActor
import de.heikoseeberger.akkamazing.UserService._
import spray.json.DefaultJsonProtocol

import scala.concurrent.duration._

object UserService {

  object Shard {
    val name = "UserService"

    // id == shardId => one Actor takes care of a range of users
    // change this id to `name` to shard by initial letter, but keep 1 Actor per 1 user
    val idExtractor: ShardRegion.IdExtractor = {
      case m: SignUp => m.name.take(1).toUpperCase -> m
    }

    // sharding on first letter of user-name
    val shardResolver: ShardRegion.ShardResolver = {
      case SignUp(m) => m.take(1).toUpperCase
    }
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

class UserService extends PersistentActor with ActorLogging {

  log.info("Started UserService, shard: {}", self.path.name)

  // if no message received within 15 seconds, initialise passivation
  context setReceiveTimeout 15.seconds

  override def persistenceId: String =
    self.path.parent.name + "-" + self.path.name

  private var names = Set.empty[String]

  override def postStop() {
    super.postStop()
    log.info("Stopped UserService, shard: {}", self.path.name)
  }

  override def receiveCommand: Receive =
    handleCommand orElse handleTimeout

  def handleCommand: Receive = {
    case SignUp(name) if names contains name =>
      log.info("<< NameTaken({})", name)
      sender() ! SignUpResponse.NameTaken(name)

    case SignUp(name) =>
      log.info("<< SignedUp({})", name)
      persist(SignUpResponse.SignedUp(name))(handleSignedUpThenRespond)
  }

  def handleTimeout: Receive = {
    case ReceiveTimeout =>
      log.info("Initialise passivation of UserService, shard: {}", self.path.name)
      context.parent ! Passivate(stopMessage = PoisonPill)
  }

  override def receiveRecover: Receive = {
    case signedUp: SignUpResponse.SignedUp =>
      log.info("Recovery: SignedUp({})", signedUp.name)
      handleSignedUp(signedUp)
  }

  private def handleSignedUpThenRespond(signedUp: SignUpResponse.SignedUp): Unit = {
    handleSignedUp(signedUp)
    sender() ! signedUp
  }

  private def handleSignedUp(signedUp: SignUpResponse.SignedUp): Unit = {
    val SignUpResponse.SignedUp(name) = signedUp
    names += name
  }
}
