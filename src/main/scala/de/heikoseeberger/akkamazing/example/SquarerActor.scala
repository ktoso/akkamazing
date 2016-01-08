package de.heikoseeberger.akkamazing.example

import akka.actor._
import akka.remote.RemoteScope

class SquarerActor extends Actor with ActorLogging {

  override def postStop(): Unit = {
    log.info("Shutting down!!!!")
  }

  override def receive = {
    case SquareTask(id, number, _) =>
      log.info(s"${Thread.currentThread()} $id: $number^2 = ${number * number} for $sender")
      context.stop(self)
    case _ =>
      log.info(s"unknown message received")
  }
}

class TaskAllocatorActor extends Actor with ActorLogging {
  override def receive = {
    case task: SquareTask =>
      log.info(s"${Thread.currentThread()} received ${task} from ${sender}")

      val s = "akka.tcp://akkamazing-system@127.0.0.1:2555"
      val deploy = Deploy(scope = RemoteScope(AddressFromURIString(s)))
      val props = Props[SquarerActor].withDeploy(deploy)
      val actorRef = context.actorOf(props, task.id)
      actorRef ! task

    case _ =>
      println("Unknown Task")
  }
}

case class SquareTask(id: String, number: Int, any: Any)