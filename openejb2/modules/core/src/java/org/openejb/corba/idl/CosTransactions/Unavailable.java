package org.openejb.corba.idl.CosTransactions;


/**
* org/apache/geronimo/interop/CosTransactions/Unavailable.java .
* Generated by the IDL-to-Java compiler (portable), version "3.1"
* from C:/dev/corba/geronimo/trunk/modules/interop/src/idl/CosTransactions.idl
* Saturday, March 12, 2005 1:30:01 PM EST
*/

public final class Unavailable extends org.omg.CORBA.UserException
{

  public Unavailable ()
  {
    super(UnavailableHelper.id());
  } // ctor


  public Unavailable (String $reason)
  {
    super(UnavailableHelper.id() + "  " + $reason);
  } // ctor

} // class Unavailable