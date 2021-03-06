package ru.org.codingteam.horta.actors

import akka.actor.{Actor, ActorLogging}
import java.io.File
import java.util.Scanner
import ru.org.codingteam.horta.Configuration
import ru.org.codingteam.horta.messages.{DoParsing, ParsedPhrase}
import scalax.file.Path
import platonus.Network

class LogParser extends Actor with ActorLogging {
	val regex = "^\\[.*?\\] \\* (.*?)(?: \\*|:) (.*?)$".r

	def receive = {
		case DoParsing(roomName, userName) => {
			val directory = Path.fromString(Configuration.logDirectory) / roomName
			val network = new Network()

			log.info(s"Reading directoty $directory")

			for (path <- directory.descendants(depth = 1)) {
				val filename = path.path

				val file = new File(filename)
				val scanner = new Scanner(file, Configuration.logEncoding).useDelimiter("\\r\\n")
				try {
					while (scanner.hasNext) {
						val string = scanner.next()
						string match {
							case regex(nick, message) if nick == userName => network.addPhrase(message)
							case _ =>
						}
					}
				} finally {
					scanner.close()
				}
			}

			log.info(s"Finished reading $directory")
			sender ! network
			context.stop(self)
		}
	}
}
