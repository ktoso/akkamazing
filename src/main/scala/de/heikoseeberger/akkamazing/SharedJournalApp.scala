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

import akka.actor.{ ActorSystem, Props }
import akka.persistence.journal.leveldb.{ SharedLeveldbJournal, SharedLeveldbStore }

/**
 * The shared journal is a single point of failure and must not be used in production.
 * This app must be running in order for persistence and cluster sharding to work.
 */
object SharedJournalApp extends BaseApp {

  override def run(system: ActorSystem, opts: Map[String, String]): Unit = {
    val sharedJournal = system.actorOf(Props(new SharedLeveldbStore), "shared-journal")
    SharedLeveldbJournal.setStore(sharedJournal, system)
  }
}
