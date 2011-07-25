/*
 * Copyright (C) 2010-2011, FuseSource Corp.  All rights reserved
 *
 *    http://fusesource.com
 *
 * The software in this package is published under the terms of the
 * CDDL license, a copy of which has been included with this distribution
 * in the license.txt file
 */

package org.fusesource.fabric.apollo.amqp.protocol.interfaces

import org.fusesource.fabric.apollo.amqp.codec.interfaces.AMQPFrame
import collection.mutable.Queue

abstract class Interceptor {

  private var _outgoing:Option[Interceptor] = None
  private var _incoming:Option[Interceptor] = None

  def outgoing = _outgoing.getOrElse(throw new RuntimeException("No outgoing interceptor exists at this end of chain"))
  def incoming = _incoming.getOrElse(throw new RuntimeException("No incoming interceptor exists at this end of chain"))

  val rm = () => {
    remove
  }

  def remove:Unit = {
    _outgoing match {
      case Some(out) =>
        _incoming match {
          case Some(in) =>
            in.outgoing = out
          case None =>
            out.incoming = null
        }
      case None =>

    }
    _incoming match {
      case Some(in) =>
        _outgoing match {
          case Some(out) =>
            out.incoming = in
          case None =>
            in.outgoing = null
        }
      case None =>
    }
    _outgoing = None
    _incoming = None
  }

  def connected:Boolean = !(_incoming == None && _outgoing == None)

  def outgoing_=(i:Interceptor):Unit = {
    if (i != null) {
      i._incoming = Option(this)
    }
    _outgoing = Option(i)
  }

  def incoming_=(i:Interceptor):Unit = {
    if (i != null) {
      i._outgoing = Option(this)
    }
    _incoming = Option(i)
  }

  def tail:Interceptor = {
    if (!connected || _incoming == None) {
      this
    } else {
      incoming.tail
    }
  }

  def head:Interceptor = {
    if (!connected || _outgoing == None) {
      this
    } else {
      outgoing.head
    }
  }

  def send(frame:AMQPFrame, tasks:Queue[() => Unit])

  def receive(frame:AMQPFrame, tasks:Queue[() => Unit])

}