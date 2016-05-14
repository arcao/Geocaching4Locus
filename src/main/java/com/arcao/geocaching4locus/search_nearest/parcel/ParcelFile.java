package com.arcao.geocaching4locus.search_nearest.parcel;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.File;
import java.net.URI;

public class ParcelFile extends File implements Parcelable {
  private static final long serialVersionUID = -3354942740907083967L;

  public ParcelFile(File dir, String name) {
    super(dir, name);
  }

  public ParcelFile(String path) {
    super(path);
  }

  public ParcelFile(String dirPath, String name) {
    super(dirPath, name);
  }

  public ParcelFile(URI uri) {
    super(uri);
  }

  public ParcelFile(File file) {
    this(file.getPath());
  }

  // PARCELABLE PART
  private ParcelFile(Parcel in) {
    super(in.readString());
  }

  public static final Parcelable.Creator<ParcelFile> CREATOR = new
      Parcelable.Creator<ParcelFile>() {
        public ParcelFile createFromParcel(Parcel in) {
          return new ParcelFile(in);
        }

        public ParcelFile[] newArray(int size) {
          return new ParcelFile[size];
        }
      };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(getPath());
  }
}
