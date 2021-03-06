package ru.org.codingteam.horta.actors.messenger

import akka.actor.{Actor, ActorLogging, ActorRef}
import ru.org.codingteam.horta.messages._
import ru.org.codingteam.horta.security.User
import ru.org.codingteam.horta.messages.UserMessage
import ru.org.codingteam.horta.messages.ProcessCommand

class PrivateHandler(val messenger: ActorRef) extends Actor with ActorLogging {
	def receive() = {
		case UserMessage(message) => {
			val jid = message.getFrom
			val text = message.getBody

			messenger ! ProcessCommand(User(Some(jid), None, None, None), text)
		}

		case GenerateCommand(jid, _, _) => {
			messenger ! SendChatMessage(jid, "Sorry, no.")
		}
	}
}
