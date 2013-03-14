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

import ch.usi.tomica.paxos.AbstractAcceptor
import scala.actors.remote.RemoteActor
import scala.actors.remote.RemoteActor._
import scala.actors.remote.Node
import scala.actors.Actor
import scala.actors.AbstractActor
import scala.collection.mutable.Queue
import scala.collection.mutable.HashMap
import com.sleepycat.je.EnvironmentConfig
import com.sleepycat.je.Environment
import com.sleepycat.persist.StoreConfig
import com.sleepycat.persist.EntityStore
import java.io.File

/*@
 * Each acceptor should maintain a map:
 * slot -> <slotInfo> 
 * that maintains the state of instances/slots that the acceptor has open
 * and the relevant <ballot, slot, cmd> triple
 * This structure should eventually be persisted to something like BDB
 */
class SimpleAcceptor(acceptorId: Int, configFile : String) extends AbstractAcceptor { 
	//	This is now a remote acceptor
	RemoteActor.classLoader = getClass().getClassLoader()
	// TODO For simplicity assume we just connect to one proposer for now
	var proposer:AbstractActor = null
	val slots = new HashMap[Long,SlotInfo]()
	
	def connectToProposers() {
		val pr = new Queue[AbstractActor]()
		// TODO Make this more flexible
		assert(proposerCount == 1)
		for (i <- 1 to proposerCount) {
			val hostPort = opts("proposer_" + i).split(":")
			val node = Node(hostPort(0), hostPort(1).toInt)
			val p = select(node, 'SimpleProposer)
			pr += p
		}
		proposers = pr.toArray
		proposer = proposers(0)
	}
	
	// TODO Move this out of this class and somewhere more appropriate
	def initBDB () {
	  
		/* Open the JE Environment. */
		val envConfig = new EnvironmentConfig()
		envConfig.setAllowCreate(true)
		envConfig.setTransactional(true)
		val env = new Environment(new File("/tmp/paxos/acc" + acceptorId), envConfig)

		/* Open the DPL Store. */
		val storeConfig = new StoreConfig()
		storeConfig.setAllowCreate(true)
		storeConfig.setTransactional(true)
		val store = new EntityStore(env, "Acceptor"+ acceptorId, storeConfig)

		/* The PrimaryIndex maps the Long primary key to Person. */
		val priIndex =
		store.getPrimaryIndex(classOf[java.lang.Long], classOf[SlotInfo])

		/* The SecondaryIndex maps the String secondary key to Person. */
//		val secIndex =
//		store.getSecondaryIndex(priIndex, classOf[java.lang.String], "name")
 
	}
	
	def initialize() {
		alive(acceptorHosts(acceptorId-1)._2)
		register('SimpleAcceptor, this)
	}
	
	def handlePhase1(s:SlotTriple) {
  
		var l:SlotInfo = null
		/* We handle three cases here:
		 * - we have something for this slot, and its ballot is smaller
		 * - we have something for this slot and it's a smaller or same ballot
		 * - we have nothing for this slot
		 */
		if (slots.contains(s.slot)) {
			l = slots(s.slot)
			if (s.ballot > l.ballot) {
				val newInst = SlotInfo(s.ballot, s.slot, s.cmd, 0, 0)
				slots(s.slot) = newInst
				l = newInst
			}
		} else  {
			l = new SlotInfo(s.ballot,s.slot,s.cmd,0,0)
			slots(s.slot) = l
		}
		
		val r = new SlotTriple(l.ballot, l.slot, l.cmd)
		proposer ! Phase1B(r, acceptorId)
	}
	
	def handlePhase2(s:SlotTriple) {
		var l:SlotInfo = null
		if (slots.contains(s.slot)) {
			l = slots(s.slot)
			if (s.ballot > l.ballot) {
				val newInst = SlotInfo(s.ballot, s.slot, s.cmd, 0, 0)
				slots(s.slot) = newInst
				l = newInst
			}
			
//			val r = new SlotTriple(l.ballot, l.slot, l.cmd)
			// Do we really need to send the cmd back if the ballot and slot match?
			val r = new SlotTriple(l.ballot, l.slot, "") 
			proposer ! Phase2B(r, acceptorId)
		  
		} else {
		  
			// do nothing; we should have something for this instance?
		  
		}
	}
	
	def act() {
		readConfig(configFile)
		validateConfig()
		initialize()
		connectToProposers()
		
		loop {
			receive {
			  
			case Phase1A(s, pId) =>
				if (DEBUG) println("aId %d Got Phase1A <b,s,cmd> %d %d ".
						format(acceptorId, s.ballot, s.slot))
						
				handlePhase1(s)

			case Phase2A(s, pId) =>
				if (DEBUG) println("aId %d Got Phase2A <b,s,cmd> %d, %d, %s".
				    format(acceptorId, s.ballot, s.slot, s.cmd))
				    
				handlePhase2(s)
				
			case Stop =>
				println("Acceptor " + acceptorId + " stopping")
				exit()
			}
		}
	}
}

object AcceptorApp {
  def main(args: Array[String]) : Unit = {
    if (args.length == 2) {
      println("Starting acceptor " + args(0) + ", press enter to exit")
      val a = new SimpleAcceptor(args(0).toInt, args(1))
      a.start
//      var asdf = readLine()
//      a ! Stop
	
    }
    else {
      println("usage: scala AcceptorApp [acceptorId] [conf file]")
    }
  }
}
