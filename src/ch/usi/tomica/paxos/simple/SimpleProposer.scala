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

import ch.usi.tomica.paxos.AbstractProposer
import scala.actors.remote.RemoteActor
import scala.actors.remote.RemoteActor._
import scala.actors.remote.Node
import scala.actors.Actor
import scala.actors.AbstractActor
import scala.collection.mutable.Queue
import scala.collection.mutable.HashMap
import ch.usi.tomica.paxos.Learner

/*@
 * Similar to the acceptors, the proposer must maintain a DB mapping the 
 * an instance/slot of paxos to the ballot, cmd and other relevant data like
 * the number of votes we have received. This should eventually go into BDB
 * so going to try to abstract these accesses to sensible method calls 
 */
class SimpleProposer(proposerId:Int, configFile:String) extends AbstractProposer {
  
	val slots = new HashMap[Long,SlotInfo]()
	
	RemoteActor.classLoader = getClass().getClassLoader()
	
	def connectToAcceptors() {
		val acc = new Queue[AbstractActor]()
		for (i <- 1 to acceptorCount) {
			val hostPort = opts("acceptor_" + i).split(":")
			val node = Node(hostPort(0), hostPort(1).toInt)
			println("Connecting to acceptor " + i + " at " + hostPort)
			val a = select(node, 'SimpleAcceptor)
			acc += a
		}
		acceptors = acc.toArray
	}
	def connectToLearners() {
		val lrn = new Queue[AbstractActor]()
		for (i <- 1 to learnerCount) {
			val hostPort = opts("learner_" + i).split(":")
			val node = Node(hostPort(0), hostPort(1).toInt)
			println("Connecting to learner " + i + " at " + hostPort)
			val l = select(node, 'SimpleLearner)
			lrn += l
		}
		learners = lrn.toArray
	}
	
	def handleProposal(cmd:String) {
		cRnd += 1
		val ballot = 1001
		val newSlot = new SlotInfo(ballot, cRnd, cmd,0,0)
		slots(cRnd) = newSlot
		latency = System.currentTimeMillis()
		for (a <- acceptors) {
			val p1a = new SlotTriple(ballot, cRnd, "")
			a ! Phase1A(p1a, proposerId)
		}
	}
	
	def handlePhase1(s : SlotTriple) {
	  
		val l = slots(s.slot)
		if (l.ballot == s.ballot) {
			l.phase1Votes += 1
			if (l.phase1Votes == 2) {
				if (s.cmd != "") l.cmd = s.cmd
				slots(s.slot) = l
				
				for (a <- acceptors) {
					if (DEBUG) println("2A sending b %d cmd %s".format(s.ballot, s.cmd))
					
					val r = new SlotTriple(l.ballot,l.slot,l.cmd)
					a ! Phase2A(r, proposerId)
				}
			}
		} 
	}
	
	def handlePhase2(s : SlotTriple) {
		val l = slots(s.slot)
		if (l.ballot == s.ballot) {
			l.phase2Votes += 1
			slots(s.slot) = l
			if (l.phase2Votes == 2) {
				latency = System.currentTimeMillis() - latency
				valuesDecided += 1
				if(latency < 10000)
					avgLatency += (latency - avgLatency) / valuesDecided
					
				// FIXME we need to ensure that we do not deliver messages out of order!
				// i.e. we can't really deliver at this point
				if (learnerCount > 0) {
					for (learner <- learners) {
						val toLearner = SlotTriple(l.ballot,l.slot,l.cmd)
								learner ! Phase3(toLearner)
					}
				}
		
			}
				
		}
	}
	
	def act() {
		readConfig(configFile)
		validateConfig()
		connectToAcceptors()
		connectToLearners()
		alive(proposerHosts(proposerId-1)._2)
		register('SimpleProposer, this)
		
		
		loop {
			receive {
			  
			case Proposal(cmd) =>
				if (DEBUG) println("Got Proposal payload " + cmd)
				handleProposal(cmd)

			case Phase1B(s, aId) =>
				if (DEBUG) println("Proposer got phase1B b %d from acc %d cmd %s"
						.format(s.ballot, aId, s.cmd))
				handlePhase1(s)

			case Phase2B(s, aId) =>
				if (DEBUG) println("Proposer got phase2B b %d from acc %d cmd %s"
						.format(s.ballot, aId, s.cmd))
			  	handlePhase2(s)

			case Stop =>
				println("Proposer " + proposerId + " stopping")
				exit()
			}
		}
	}
}

object ProposerApp {
  def main(args: Array[String]) : Unit = {
    if (args.length != 3) {
      println("usage: scala ProposerApp [proposerId] [conf file] [prop Batches]")
      sys.exit()
    }
    
	val p = new SimpleProposer(args(0).toInt, args(1)) 
//	val c = new SimpleLearner(args(0).toInt, args(1))
	p.start
	Thread.sleep(300)
	
	var propStr = ""
	for (i <- 1 to p.propStrLen) {
		propStr+= "0"
	}
	
	println("Starting proposer; launching %d batches of size %d every %d ms"
	    .format(args(2).toInt, p.propBatchSize, p.propBatchPause))
	
	Thread.sleep(300)
	
	for (j <- 1 to args(2).toInt) {
		for (i <- 1 to p.propBatchSize) {
			p ! Proposal(propStr)
		}
		Thread.sleep(p.propBatchPause)
	}
	
	println("Finished proposals; press enter to kill everything")
	var huy = readLine()
	p ! Stop
	
      } 
  }
