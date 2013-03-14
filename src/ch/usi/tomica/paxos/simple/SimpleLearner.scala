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

import ch.usi.tomica.paxos.Learner
import ch.usi.tomica.paxos.AbstractPaxosProcess
import scala.actors.remote.RemoteActor
import scala.actors.remote.RemoteActor._

class SimpleLearner (learnerId:Int, configFile:String) 
	extends AbstractPaxosProcess with Learner {
  
	RemoteActor.classLoader = getClass().getClassLoader()
	def act() {
	  var valuesLearned = 0
	  var startTime:Long = 0
	  var maxInstance:Long = -1
	  var lastTime = System.currentTimeMillis()
	  
	  readConfig(configFile)
	  validateConfig()
		
	  alive(learnerHosts(learnerId-1)._2)
	  register('SimpleLearner, this)
	  
	  println("Learner starting at " + System.currentTimeMillis())
	  loop {
	    receive {
	      
	    case Phase3(s) => 
	      	if (DEBUG) println("Learner learned b,s,cmd <%d, %d, %s > at %d".format(
	      			s.ballot, s.slot, s.cmd, System.currentTimeMillis()))
	      	val lat = System.currentTimeMillis() - lastProposal
	      	avgLatency += (lat - avgLatency) / (valuesLearned+1)
	      	minLatency = Math.min(lat, minLatency)
	      	maxLatency = Math.max(lat, maxLatency)
	      	bytesLearned += s.cmd.size
	      	if (startTime == 0) startTime = System.currentTimeMillis()
	      	valuesLearned += 1
	      	
	      	maxInstance = Math.max(maxInstance, s.slot)
	      	if (valuesLearned % learnStatsInterval == 0)  {
			val curTime = System.currentTimeMillis()
			val intervalTime = curTime - lastTime
			lastTime = curTime
			val curRate = 1000.0 * (learnStatsInterval / (1.0*intervalTime))
	      		println(("Lrn slot: %d/%d @t: %d interval: %d tx/s:"
	      		    + " %.2f  bytes: %d ").format(
	      			maxInstance, valuesLearned, curTime, intervalTime, curRate,
	      			bytesLearned))
		}
	      	
	    case Stop =>
	      	val totalTime = System.currentTimeMillis() - startTime
	      	println("Client/Learner stopping")
	      	val propRate = valuesLearned / (totalTime/1000.0)
	      	val tput =  bytesLearned / (1024.0 * 1024.0) / totalTime 
	      	println("Client learned %d values , min/avg/max latency : %d, %d, %d , MB learned %.2f prop/s: %.2f, tput %.2f MB/s".format(
	      			valuesLearned, minLatency, avgLatency, maxLatency, 
	      			bytesLearned / (1024.0 * 1024.0), propRate, tput)) 
	      	exit()
	      
	    }
	  }
	}
   
		
		
}
object LearnerApp {
	def main(args: Array[String]) : Unit = {
		if (args.length != 2) {
			println("usage: scala LearnerApp [learnerId] [conf file]")
			exit()
		}
		val l = new SimpleLearner(args(0).toInt, args(1))

		l.start
  	}
}
