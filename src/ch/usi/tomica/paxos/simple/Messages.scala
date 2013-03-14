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
package ch.usi.tomica.paxos.simple

import scala.collection.mutable.Queue

// For lack of a better name... the triple we use for slot/instance state
case class SlotTriple(ballot:Int, slot:Long, cmd:String)
// Sent by Clients
case class Proposal(cmd: String)

// Sent by Proposer/Coordinator
case class Phase1A(s : SlotTriple, pId : Int)
case class Phase2A(s : SlotTriple, pId : Int)

// Sent by acceptors 
case class Phase1B(s : SlotTriple, aId : Int)
case class Phase2B(s : SlotTriple, aId : Int)

// Basically a notification for learner saying that slot was learned
case class Phase3(s : SlotTriple)


case class Stop
