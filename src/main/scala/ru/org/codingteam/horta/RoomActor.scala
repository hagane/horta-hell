package ru.org.codingteam.horta

import akka.actor.{Props, Actor, ActorLogging}
import messages.{UserMessage, Initialize, SendMessage}
import platonus.{Filesystem, Network}
import org.jivesoftware.smackx.muc.MultiUserChat

class RoomActor extends Actor with ActorLogging {
  var room: MultiUserChat = null
  var networks = Map[String, Network]()

  def receive = {
    case Initialize(muc) => room = muc
    case UserMessage(jid, message) => {
      val nick = nickByJid(jid)
      val network = networks.getOrElse(nick, networkByNick(nick))

      if (message == "$say") {
        val phrase = network.doGenerate()
        sender ! SendMessage(room, phrase)
      } else {
        network.addPhrase(message)
      }
    }
  }

  def nickByJid(jid: String) = jid.split('/')(1)
  def networkByNick(nick: String) = {
    log.info(s"Parsing log directory for $nick user.")
    Filesystem.scanDirectory(
      Configuration.logDirectory,
      nick,
      Configuration.logEncoding)
  }
}
