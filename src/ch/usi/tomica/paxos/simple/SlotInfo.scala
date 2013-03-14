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

import com.sleepycat.persist.model.Entity
import com.sleepycat.persist.model.PrimaryKey
import com.sleepycat.persist.model.SecondaryKey
import com.sleepycat.persist.model.Relationship

@Entity
case class SlotInfo ( ballot:Int, 
    @PrimaryKey slot:Long , 
//    @SecondaryKey(relate=Relationship.MANY_TO_ONE) 
    var cmd:String, 
    var phase1Votes:Int, 
    var phase2Votes:Int)
