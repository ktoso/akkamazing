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

import akka.actor.{ Actor, ActorLogging, ActorIdentity, Identify, Props, ReceiveTimeout, RootActorPath }
import akka.cluster.{ Cluster, Member }
import akka.cluster.ClusterEvent.{ InitialStateAsEvents, MemberUp }
import akka.persistence.journal.leveldb.SharedLeveldbJournal
import scala.concurrent.duration.DurationInt

object SharedJournalSetter {

  def props: Props =
    Props(new SharedJournalSetter)
}

/**
 * This actor must be created by all applications that want to use the shared journal,
 * e.g. in order to use persistence or cluster sharding.
 */
class SharedJournalSetter extends Actor with ActorLogging {

  override def preStart(): Unit =
    Cluster(context.system).subscribe(self, InitialStateAsEvents, classOf[MemberUp])

  override def receive: Receive =
    waiting

  override def postStop(): Unit =
    Cluster(context.system).unsubscribe(self)

  def waiting: Receive = {
    case MemberUp(member) if member hasRole "shared-journal" => onSharedJournalMemberUp(member)
  }

  def becomeIdentifying(): Unit = {
    context.setReceiveTimeout(10 seconds)
    context become identifying
  }

  def identifying: Receive = {
    case ActorIdentity(_, Some(sharedJournal)) =>
      SharedLeveldbJournal.setStore(sharedJournal, context.system)
      log.info("Succssfully set shared journal {}", sharedJournal)
      context.stop(self)
    case ActorIdentity(_, None) =>
      log.error("Can't identify shared journal!")
      context.stop(self)
    case ReceiveTimeout =>
      log.error("Timeout identifying shared journal!")
      context.stop(self)
  }

  def onSharedJournalMemberUp(member: Member): Unit = {
    val sharedJournal = context actorSelection (RootActorPath(member.address) / "user" / "shared-journal")
    sharedJournal ! Identify(None)
    becomeIdentifying()
  }
}
