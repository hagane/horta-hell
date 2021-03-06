package ru.org.codingteam.horta.messages

import akka.actor.ActorRef
import platonus.Network

abstract sealed class RoomUserMessage

case class SetNetwork(network: Network) extends RoomUserMessage

case class UserPhrase(phrase: String) extends RoomUserMessage

case class AddPhrase(phrase: String) extends RoomUserMessage

case class GeneratePhrase(forNick: String, length: Integer, allCaps: Boolean) extends RoomUserMessage

case class ReplaceRequest(from: String, to: String) extends RoomUserMessage

case class CalculateDiff(forNick: String, nick1: String, nick2: String, roomUser2: ActorRef) extends RoomUserMessage

case class CalculateDiffRequest(forNick: String, nick1: String, nick2: String, network1: Network)
	extends RoomUserMessage
