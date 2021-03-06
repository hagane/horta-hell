package ru.org.codingteam.horta.actors.messenger

import akka.actor.{Props, ActorLogging, Actor}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import platonus.Network
import ru.org.codingteam.horta.messages._
import scala.concurrent.duration._
import scala.language.postfixOps
import java.util.{Calendar, Locale}
import scala.concurrent.Future
import ru.org.codingteam.horta.actors.LogParser

class RoomUser(val room: String, val nick: String) extends Actor with ActorLogging {

	import context.dispatcher

	implicit val timeout = Timeout(60 seconds)

	object Tick

	val cacheTime = 5 minutes

	var network: Option[Network] = None
	var lastMessage: Option[String] = None
	var lastNetworkTime: Option[Calendar] = None

	override def preStart() {
		context.system.scheduler.schedule(cacheTime, cacheTime, self, Tick)
	}

	def receive = {
		case SetNetwork(newNetwork) => {
			network = Some(newNetwork)
			lastNetworkTime = Some(Calendar.getInstance)
		}

		case UserPhrase(message) => {
			if (addPhrase(message)) {
				lastMessage = Some(message)
			}
		}

		case AddPhrase(phrase) => {
			addPhrase(phrase)
		}

		case GeneratePhrase(forNick, length, allCaps) => {
			getNetwork() map {
				case network => {
					val phrase = generatePhrase(network, length)
					GeneratedPhrase(forNick, if (allCaps) phrase.toUpperCase(Locale.ROOT) else phrase)
				}
			} pipeTo sender
		}

		case ReplaceRequest(from, to) => {
			lastMessage match {
				case Some(message) =>
					val newMessage = message.replace(from, to)
					lastMessage = Some(newMessage)
					sender ! ReplaceResponse(newMessage)
				case None => sender ! ReplaceResponse("No messages for you, sorry.")
			}
		}

		case CalculateDiff(forNick, nick1, nick2, roomUser2) => {
			getNetwork() map {
				case network => (roomUser2 ? CalculateDiffRequest(forNick, nick1, nick2, network))
			} pipeTo sender
		}

		case CalculateDiffRequest(forNick, nick1, nick2, network2) => {
			getNetwork() map {
				case network => {
					val diff = network.diff(network2)
					CalculateDiffResponse(forNick, nick1, nick2, diff)
				}
			} pipeTo sender
		}

		case Tick => {
			// Analyse and flush cache on tick.
			lastNetworkTime match {
				case Some(time) => {
					val msDiff = Calendar.getInstance.getTimeInMillis - time.getTimeInMillis
					if (msDiff > cacheTime.toMillis) {
						flushNetwork()
					}
				}

				case None =>
			}
		}
	}

	def getNetwork(): Future[Network] = {
		network match {
			case Some(network) => {
				lastNetworkTime = Some(Calendar.getInstance)
				Future.successful(network)
			}

			case None => {
				val parser = context.actorOf(Props[LogParser])
				val result = parser ? DoParsing(room, nick)
				result map {
					case network: Network =>
						self ! SetNetwork(network)
						network
				}
			}
		}
	}

	def flushNetwork() {
		if (!network.isEmpty) {
			log.info(s"Flushing Markov network cache of user $nick")
			network = None
		}
	}

	def addPhrase(phrase: String) = {
		if (!phrase.startsWith("$") && !phrase.startsWith("s/")) {
			network match {
				case Some(network) => network.addPhrase(phrase)
				case None => // will add later on log parse
			}
			true
		} else {
			false
		}
	}

	def generatePhrase(network: Network, length: Integer): String = {
		for (i <- 1 to 25) {
			val phrase = network.generate()
			if (phrase.split(" ").length >= length) {
				return phrase
			}
		}

		"Requested phrase was not found, sorry."
	}
}
