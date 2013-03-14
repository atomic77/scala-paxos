/*
    Scala-Paxos
    Copyright (C) 2013 Alex Tomic

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package ch.usi.tomica.paxos

import scala.actors.Actor

//abstract class AbstractAcceptor(aId: Int) extends Actor {
abstract class AbstractAcceptor extends AbstractPaxosProcess {
  val BUFF_SIZE = 1000
  val acceptorId:Int = 0
  var round: Int = 0
  var vRound: Int = 0
  var accepted = new Array[String](BUFF_SIZE)
}
