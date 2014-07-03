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

import akka.actor.Props
import akka.io.IO
import spray.can.Http
import spray.http.StatusCodes
import spray.routing.{ HttpServiceActor, Route }

object HttpService {

  def props(hostname: String, port: Int): Props =
    Props(new HttpService(hostname, port))
}

class HttpService(hostname: String, port: Int) extends HttpServiceActor {

  override def preStart(): Unit =
    IO(Http)(context.system) ! Http.Bind(self, hostname, port)

  override def receive: Receive =
    runRoute(apiRoute)

  private def apiRoute: Route =
    pathPrefix("api") {
      path("users") {
        get {
          complete {
            StatusCodes.OK
          }
        }
      }
    }
}
