package com.traveler.traveljournal;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.util.Xml;


import androidx.core.content.FileProvider;

import com.traveler.traveljournal.model.TripLocation;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class GpxHelper {

    private static final String GPX_FILE_FOLDER = "GPXFiles";

    private final ArrayList<TripLocation> locations;
    private final Context context;

    public GpxHelper(Context context, ArrayList<TripLocation> locations) {
        this.context = context;
        this.locations = locations;
    }


    public Uri createGpxFile() {
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), GPX_FILE_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String filename = "gps_data_" + System.currentTimeMillis() + ".gpx";

        File file = new File(folder, filename);
        try {
            if (!file.createNewFile()) return null;
            FileOutputStream fos = new FileOutputStream(file);
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(fos, "UTF-8");
            serializer.startDocument(null, Boolean.TRUE);
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

            serializer.startTag(null, "gpx");
            serializer.attribute(null, "version", "1.1");
            serializer.attribute(null, "creator", context.getString(R.string.app_name));
            for (TripLocation location : locations) {
                serializer.startTag(null, "wpt");
                serializer.attribute(null, "latitude", String.valueOf(location.getLatitude()));
                serializer.attribute(null, "longitude", String.valueOf(location.getLongitude()));
                serializer.endTag(null, "wpt");
            }

            serializer.endTag(null, "gpx");
            serializer.endDocument();
            serializer.flush();
            fos.close();
            return FileProvider.getUriForFile(context, context.getPackageName() + ".provider",file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


}
