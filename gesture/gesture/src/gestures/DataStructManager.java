// Created by Nikhilesh on April 16 2008 at 18:08

package gestures;
import org.hibernate.Session;
import java.util.Date;
import java.util.List;
import java.util.Iterator;
import util.HibernateUtil;

public class DataStructManager
{
	public static void main(String[] args) 
	{
		DataStructManager mgr = new DataStructManager();
		double x = 1.1, y = 1.2, z = 1.3, totalG = 1.4, time = 1.5, dx = 1.6, dy = 1.7, dz = 1.8;
		String inactiveAxis = new String( "inactiveAxis" );
		if( args[0].equals( "store" ) )
			mgr.createAndStoreGesture( x, y, z, totalG, time, dx, dy, dz, inactiveAxis );
		else if (args[0].equals("list")) 
		{
			List gestures = mgr.listGestures();
			for ( Iterator i = gestures.iterator(); i.hasNext(); ) 
			{
				DataStruct gesture = ( DataStruct ) i.next();
				System.out.println( "x: " + gesture.getX() + "y: " + gesture.getY() + "z: " + gesture.getZ() +
					                "totalG: " + gesture.getTotalG() + "dx: " + gesture.getDx() + "dy: " + gesture.getDy() +
					                 "dz: " + gesture.getDz() + "time: " + gesture.getTimeStamp() + "inactiveAxis: " + gesture.getInactiveAxis() );
			}
		}
		HibernateUtil.getSessionFactory().close();
	}

	private void createAndStoreGesture( double x, double y, double z, double totalG, double time, double dx, double dy, double dz, String ia )
	{
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		DataStruct gesture = new DataStruct();
		gesture.setX( x );
		gesture.setY( y );
		gesture.setZ( z );
		gesture.setTotalG( totalG );
		gesture.setTimeStamp( time );
		gesture.setDx( dx );
		gesture.setDy( dy );
		gesture.setDz( dz );
		gesture.setInactiveAxis( ia );

		session.save( gesture );
		session.getTransaction().commit();
	}

	private List listGestures() 
	{
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		List result = session.createQuery( "from DataStruct" ).list();
		session.getTransaction().commit();
		return result;
	}
}
