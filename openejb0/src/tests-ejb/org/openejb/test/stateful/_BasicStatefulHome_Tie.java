package org.openejb.test.stateful;

/**
 * Interface definition : BasicStatefulHome
 * 
 * @author OpenORB Compiler
 */

public class _BasicStatefulHome_Tie extends org.omg.PortableServer.Servant
		implements javax.rmi.CORBA.Tie
{

	static final String[] _ids_list =
	{
		"RMI:org.openejb.test.stateful.BasicStatefulHome:0000000000000000", 
		"RMI:javax.ejb.EJBHome:0000000000000000"
	};

	private org.omg.PortableServer.POA _poa;
	private byte [] _oid;

	public String[] _all_interfaces( org.omg.PortableServer.POA poa, byte [] oid )
	{
		_poa = poa;
		_oid = oid;
		return _ids_list;
	}

	//
	// Private reference to implementation object
	//
	private org.openejb.test.stateful.BasicStatefulHome target;

	//
	// Private reference to the ORB
	//
	private org.omg.CORBA_2_3.ORB _orb;

	//
	// Set target object
	//
	public void setTarget( java.rmi.Remote targ )
	{
		target = (BasicStatefulHome) targ;
	}

	//
	// Get target object
	//
	public java.rmi.Remote getTarget()
	{
		return target;
	}

	//
	// Returns an object reference for the target object
	//
	public org.omg.CORBA.Object thisObject()
	{
		return _this_object();
	}

	//
	// Deactivate the target object
	//
	public void deactivate()
	{
		try
		{
			_poa.deactivate_object( _oid );
		}
		catch ( org.omg.PortableServer.POAPackage.ObjectNotActive ex )
		{
		}
		catch ( org.omg.PortableServer.POAPackage.WrongPolicy ex )
		{
		}
		target = null;
	}

	//
	// Return the ORB
	//
	public org.omg.CORBA.ORB orb()
	{
		return _orb;
	}

	//
	// Set the ORB
	//
	public void orb( org.omg.CORBA.ORB orb )
	{
		_orb = ( org.omg.CORBA_2_3.ORB ) orb;
		_orb.set_delegate( this );
	}

	//
	// Invoke method ( for remote call )
	//
	public org.omg.CORBA.portable.OutputStream _invoke(String opName, org.omg.CORBA.portable.InputStream is, org.omg.CORBA.portable.ResponseHandler handler)
	{
		org.omg.CORBA_2_3.portable.InputStream _is = (org.omg.CORBA_2_3.portable.InputStream)is;
		org.omg.CORBA_2_3.portable.OutputStream _output = null;
		try
		{
			if ( opName.equals("create") )
			{
				String arg0_in = ( String )((org.omg.CORBA_2_3.portable.InputStream)_is).read_value(String.class);

				try
				{
					org.openejb.test.stateful.BasicStatefulObject _arg_result = target.create(arg0_in);

					_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
					javax.rmi.CORBA.Util.writeRemoteObject( _output, _arg_result );

				}
				catch ( javax.ejb.CreateException _exception )
				{
					String exid = "IDL:javax/ejb/CreateEx:1.0";
					_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createExceptionReply();
					_output.write_string(exid);
					_output.write_value(_exception);
				}
				return _output;
			}
			else
			if ( opName.equals("_get_EJBMetaData") )
			{
				javax.ejb.EJBMetaData arg = target.getEJBMetaData();
				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				_output.write_value((java.io.Serializable)arg,javax.ejb.EJBMetaData.class);
				return _output;
			}
			else
			if ( opName.equals("_get_homeHandle") )
			{
				javax.ejb.HomeHandle arg = target.getHomeHandle();
				_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();
				javax.rmi.CORBA.Util.writeAbstractObject( _output, arg );
				return _output;
			}
			else
			if ( opName.equals("remove__javax_ejb_Handle") )
			{
				javax.ejb.Handle arg0_in = ( javax.ejb.Handle ) javax.rmi.PortableRemoteObject.narrow(((org.omg.CORBA_2_3.portable.InputStream)_is).read_abstract_interface(), javax.ejb.Handle.class);

				try
				{
					target.remove(arg0_in);

					_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();

				}
				catch ( javax.ejb.RemoveException _exception )
				{
					String exid = "IDL:javax/ejb/RemoveEx:1.0";
					_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createExceptionReply();
					_output.write_string(exid);
					_output.write_value(_exception);
				}
				return _output;
			}
			else
			if ( opName.equals("remove__java_lang_Object") )
			{
				java.lang.Object arg0_in = javax.rmi.CORBA.Util.readAny(_is);

				try
				{
					target.remove(arg0_in);

					_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createReply();

				}
				catch ( javax.ejb.RemoveException _exception )
				{
					String exid = "IDL:javax/ejb/RemoveEx:1.0";
					_output = ( org.omg.CORBA_2_3.portable.OutputStream ) handler.createExceptionReply();
					_output.write_string(exid);
					_output.write_value(_exception);
				}
				return _output;
			}
			else
				throw new org.omg.CORBA.BAD_OPERATION();
		}
		catch ( org.omg.CORBA.SystemException ex )
		{
			throw ex;
		}
		catch ( Throwable ex )
		{
			throw new org.omg.CORBA.portable.UnknownException(ex);
		}
	}
}
