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
import scala.collection.mutable.HashMap
import scala.io.Source
import scala.actors.AbstractActor
import scala.collection.mutable.Queue

/**
 * Anything common to any type of process we will use (eg. reading config
 * files) will go in here
 * @author atomic
 *
 */
abstract class AbstractPaxosProcess extends Actor {
	val opts = new HashMap[String,String]()
	
	var DEBUG = false
	
	var proposerCount = -1
	var proposers:Array[AbstractActor] = null
	var proposerHosts:Array[(String,Int)] = null
	var propStrLen = 0
	var propBatchSize = 0
	var propBatchPause = 0
	
	var acceptorCount = -1
	var acceptors:Array[AbstractActor] = null
	var acceptorHosts:Array[(String,Int)] = null
	
	var learnerCount = -1
	var learners : Array[AbstractActor] = null
	var learnerHosts:Array[(String, Int)] = null
	
	var batchCount = 0
	
	var learnStatsInterval = 0
	/**
	 * Read any config parameters that are in the file; leave it up to the
	 * implementing classes to do implement validateConfigOptions
	 * @param filename
	 */
	def readConfig(filename : String) {
		for (line <- Source.fromFile(filename).getLines()) {
			val a = line.split("=")
			if (a.length == 2) {
			  opts(a(0).trim()) = a(1).trim()
			}
		}
		println("Read " + opts.size + " config options from " + filename)
			
	  
	}
	def readVariableProcs(prefix:String, count:Int) : Array[(String,Int)] = {
	  // TODO There is certainly a better way of doing this
//		val dest:Array[(String,Int)] = null
		val dest = new Queue[(String,Int)]()
		
		for (i <- 1 to count) {
			val hostPort = opts(prefix + "_" + i).split(":")
			dest += ((hostPort(0), hostPort(1).toInt))
			
		}
		return dest.toArray
	}
	
	def validateConfig () {
		try {
			
			DEBUG = opts("debug").toBoolean 
			proposerCount = opts("proposer_count").toInt
			acceptorCount = opts("acceptor_count").toInt
			learnerCount = opts("learner_count").toInt
		  
			batchCount = opts("batch_count").toInt
			DEBUG = opts("debug").toBoolean 
			propStrLen = opts("prop_str_len").toInt
			propBatchSize = opts("prop_batch_size").toInt
			propBatchPause = opts("prop_batch_pause").toInt
			
			
			learnStatsInterval = opts("learn_stats_interval").toInt
			
			proposerHosts = readVariableProcs("proposer", proposerCount)
			acceptorHosts = readVariableProcs("acceptor", acceptorCount)
			learnerHosts = readVariableProcs("learner", learnerCount)
			
		} catch {
		  case r:ArrayIndexOutOfBoundsException =>
		  case r:NoSuchElementException =>
		  	println("Config file problem! " + r.getMessage())
		  	sys.exit()
		}
	  
	}

}
