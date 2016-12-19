package com.arcao.geocaching4locus.error.exception;

import android.content.Intent;
import com.arcao.geocaching4locus.base.util.ParcelableUtil;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class IntendedException extends Exception {
  private static final long serialVersionUID = -6278705769679870918L;
  private transient Intent intent;

  public IntendedException(Throwable throwable, Intent intent) {
    super(throwable);
    this.intent = intent;
  }

  public Intent getIntent() {
    return intent;
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();

    byte[] data = ParcelableUtil.marshall(intent);

    out.writeInt(data.length);
    out.write(data);
  }
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();

    int len = in.readInt();
    byte[] data = new byte[len];

    if (in.read(data) != len)
      return;

    intent = ParcelableUtil.unmarshall(data, Intent.CREATOR);
  }
}
