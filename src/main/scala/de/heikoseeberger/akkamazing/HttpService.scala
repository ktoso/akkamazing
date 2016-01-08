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

import akka.actor.{ ActorPath, Props }
import akka.cluster.client.{ ClusterClientSettings, ClusterClient }
import akka.io.IO
import akka.pattern.ask
import spray.can.Http
import spray.http.StatusCodes
import spray.httpx.SprayJsonSupport
import spray.routing.{ HttpServiceActor, Route }

object HttpService {

  def props(hostname: String, port: Int): Props =
    Props(new HttpService(hostname, port))
}

class HttpService(hostname: String, port: Int) extends HttpServiceActor with SprayJsonSupport with SettingsActor {

  import context.dispatcher

  val initialContacts = Set(
    ActorPath.fromString("akka.tcp://akkamazing-system@127.0.0.1:2551/system/receptionist"),
    ActorPath.fromString("akka.tcp://akkamazing-system@127.0.0.1:2552/system/receptionist"))

  val client =
    context.actorOf(ClusterClient.props(ClusterClientSettings(context.system).withInitialContacts(initialContacts)))

  client ! ClusterClient.Send("/user/example", "wat!!!", false)
  client ! ClusterClient.Send("/user/example", "wat!!!", true)

  override def preStart(): Unit =
    IO(Http)(context.system) ! Http.Bind(self, hostname, port)

  override def receive: Receive =
    { case _ => }

}
