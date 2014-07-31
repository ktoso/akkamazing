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

import akka.actor._

import scala.collection.breakOut

abstract class BaseApp {

  val Opt = """(\S+)=(\S+)""".r

  def main(args: Array[String]): Unit = {
    val opts = argsToOpts(args.toList)
    applySystemProperties(opts)
    val system = ActorSystem(s"akkamazing-system")
    run(system, opts)
    readLine("Hit ENTER to quit ...")
    system.shutdown()
    system.awaitTermination()
  }

  protected def run(system: ActorSystem, opts: Map[String, String]): Unit

  private def argsToOpts(args: Seq[String]): Map[String, String] =
    args.collect { case Opt(key, value) => key -> value }(breakOut)

  private def applySystemProperties(options: Map[String, String]): Unit =
    for ((key, value) <- options if key startsWith "-D")
      System.setProperty(key substring 2, value)
  
}
