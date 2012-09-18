package menion.android.locus.addon.publiclib.util;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import menion.android.locus.addon.publiclib.geoData.PointsData;
import android.os.Parcel;
import android.os.Parcelable;

public class PointsDataOutputStream extends DataOutputStream {
	protected static final int FILE_VERSION = 1;	
	
	public PointsDataOutputStream(OutputStream os) throws IOException {
		super(new BufferedOutputStream(os));
		prepareHeader();
	}
	
	protected void prepareHeader() throws IOException {
		writeInt(FILE_VERSION);
	}
	
	/**
	 * Serialize and write a <code>PointsData</code> object to the underlying 
	 * output stream. Serializing is performed using Parcel object thus
	 * output is Android platform version depend. See {@link Parcel#marshall()} for more
	 * information.
	 *
	 * @param      pointsData a <code>PointsData</code> value to be written.
	 * @exception  IOException  if an I/O error occurs.
	 * @see        Parcelable#writeToParcel(Parcel, int)
	 * @see        Parcel#marshall()
	 */
	public void write(PointsData pointsData) throws IOException {
		// get byte array
		Parcel par = Parcel.obtain();
		pointsData.writeToParcel(par, 0);
		byte[] byteData = par.marshall();
		
		// write data
		writeInt(byteData.length);
		write(byteData);
	}
	
	/**
	 * Serialize and write a <code>PointsData</code> <code>Collection</code>
	 * to the underlying output stream. Serializing is performed using Parcel 
	 * object thus output is Android platform version depend. See 
	 * {@link Parcel#marshall()} for more information.
	 *
	 * @param      pointsDataCollection a <code>PointsData</code> <code>Collection</code> to be written.
	 * @exception  IOException  if an I/O error occurs.
	 * @see        Parcelable#writeToParcel(Parcel, int)
	 * @see        Parcel#marshall()
	 */
	public void write(Collection<PointsData> pointsDataCollection) throws IOException {
		for (PointsData pointsData : pointsDataCollection) {
			write(pointsData);
		}
	}
}
