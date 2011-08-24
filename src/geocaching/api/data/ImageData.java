package geocaching.api.data;

import geocaching.api.impl.live_geocaching_api.builder.JsonSerializable;
import google.gson.stream.JsonWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;

import com.arcao.geocaching4locus.util.Base64;
import com.arcao.geocaching4locus.util.Base64OutputStream;

public class ImageData implements JsonSerializable {
	private final String description;
	private final String mobileUrl;
	private final String name;
	private final String thumbUrl;
	private final String url;
	private byte[] imageData = null;
	private String fileName;
	
	public ImageData(String description, String mobileUrl, String name, String thumbUrl, String url) {
		this.description = description;
		this.mobileUrl = mobileUrl;
		this.name = name;
		this.thumbUrl = thumbUrl;
		this.url = url;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getMobileUrl() {
		return mobileUrl;
	}
	
	public String getName() {
		return name;
	}
	
	public String getThumbUrl() {
		return thumbUrl;
	}
	
	public String getUrl() {
		return url;
	}
	
	public static ImageData fromInputStream(String description, String name, String fileName, InputStream is) throws IOException {
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		Base64OutputStream b64os = new Base64OutputStream(bos, Base64.NO_CLOSE | Base64.NO_WRAP);
		
		int bytesReaded = 0;
		byte[] buffer = new byte[8192];
		while((bytesReaded = is.read(buffer)) != -1) {
			b64os.write(buffer, 0, bytesReaded);
		}
		b64os.flush();
		b64os.close();
		
		ImageData imageData = new ImageData(description, "", name, "", "");
		imageData.fileName = fileName;
		imageData.imageData = bos.toByteArray();
		
		return imageData;
	}
	
	public Bitmap getBitmap() {
		// TODO
		return null;
	}
	
	@Override
	public void writeJson(JsonWriter w) throws IOException {
		if (imageData == null)
			return;
		
		w.beginObject();
		w.name("FileCaption").value(name);
		w.name("FileDescription").value(description);
		w.name("FileName").value(fileName);
		w.name("base64ImageData").value(new String(imageData));
		w.endObject();
	}	
}
