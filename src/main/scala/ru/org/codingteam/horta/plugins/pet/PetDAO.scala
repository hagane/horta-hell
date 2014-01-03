package ru.org.codingteam.horta.plugins.pet

import ru.org.codingteam.horta.actors.database.DAO
import java.sql.Connection

class PetDAO extends DAO {
	override def directoryName = "pet"

  override def store(connection: Connection, id: Option[Any], obj: Any): Any = {
		id match {
			case Some(room) => {
				val roomName = room.asInstanceOf[String]
				val select = connection.prepareStatement("SELECT * FROM pet WHERE room = ?")
				try {
					select.setString(1, roomName)
					val resultSet = select.executeQuery()
					try {
						if (resultSet.next()) {
							update(connection, roomName, obj)
						} else {
							insert(connection, roomName, obj)
						}

						null
					} finally {
						resultSet.close()
					}
				} finally {
					select.close()
				}
			}

			case None => throw new IllegalArgumentException("id should not be None")
		}
	}

	override def read(connection: Connection, id: Any): Option[Any] = {
		val roomName = id.asInstanceOf[String]
		val select = connection.prepareStatement("SELECT * FROM pet WHERE room = ?")
		try {
			select.setString(1, roomName)
			val resultSet = select.executeQuery()
			try {
				if (resultSet.next()) {
					Some(
						PetStatus(
							resultSet.getString("nickname"),
							resultSet.getBoolean("alive"),
							resultSet.getInt("health"),
							resultSet.getInt("hunger")))
				} else {
					None
				}
			} finally {
				resultSet.close()
			}
		} finally {
			select.close()
		}
	}

	private def insert(connection: Connection, room: String, obj: Any) {
		obj match {
			case PetStatus(nickname, alive, health, hunger) => {
				val statement = connection.prepareStatement(
					"INSERT INTO pet (room, nickname, alive, health, hunger) VALUES (?, ?, ?, ?, ?)")
				try {
					statement.setString(1, room)
					statement.setString(2, nickname)
					statement.setBoolean(3, alive)
					statement.setInt(4, health)
					statement.setInt(5, hunger)
					statement.executeUpdate()
				} finally {
					statement.close()
				}
			}
		}
	}

	private def update(connection: Connection, room: String, obj: Any) {
		obj match {
			case PetStatus(nickname, alive, health, hunger) => {
				val statement = connection.prepareStatement(
					"UPDATE pet SET nickname = ?, alive = ?, health = ?, hunger = ? WHERE room = ?")
				try {
					statement.setString(1, nickname)
					statement.setBoolean(2, alive)
					statement.setInt(3, health)
					statement.setInt(4, hunger)
					statement.setString(5, room)
					statement.executeUpdate()
				} finally {
					statement.close()
				}
			}
		}
	}
}
