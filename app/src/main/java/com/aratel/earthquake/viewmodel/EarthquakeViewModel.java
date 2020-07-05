package com.aratel.earthquake.viewmodel;

import android.app.Application;
import android.location.Location;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aratel.earthquake.Earthquake;
import com.aratel.earthquake.R;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class EarthquakeViewModel extends AndroidViewModel {

    private static final String TAG = "EarthquakeUpdate";

    //Create a getEarthquakes method that will check if our Earthquake List Live Data has been populated already,
    // and if not, will load the Earthquakes from the feed:
    private MutableLiveData<List<Earthquake>> earthquakes;

    public EarthquakeViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<Earthquake>> getEarthquakes() {
        if (earthquakes == null) {
            earthquakes = new MutableLiveData<List<Earthquake>>();
            loadEarthquakes();
        }

        return earthquakes;
    }

    // Asynchronously load the Earthquakes from the feed with xml pull parser
    // this must be done on background thread
    public void loadEarthquakes() {
        new AsyncTask<Void, Void, List<Earthquake>>() {

            @Override
            protected List<Earthquake> doInBackground(Void... voids) {
                //Result ArrayList of parsed earthquakes
                ArrayList<Earthquake> earthquakes = new ArrayList<>(0);

                // Get the XML
                URL url;
                try {
                    String quakeFeed = getApplication().getString(R.string.earthquake_feed);
                    url = new URL(quakeFeed);

                    URLConnection connection;
                    connection = url.openConnection();

                    HttpURLConnection httpConnection = (HttpURLConnection) connection;
                    int responseCode = httpConnection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream in = httpConnection.getInputStream();

                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        DocumentBuilder db = dbf.newDocumentBuilder();

                        // parse the earthquake feed
                        Document dom = db.parse(in);
                        Element docEle = dom.getDocumentElement();

                        // Get a list of each earthquake entry
                        NodeList nl = docEle.getElementsByTagName("entry");
                        if (nl != null && nl.getLength() > 0) {
                            for (int i = 0; i < nl.getLength(); i++) {
                                // check to see if our loading has been cancelled, in which
                                // case return what we have so far
                                if (isCancelled()) {
                                    Log.d(TAG, "Loading Cancelled");
                                    return earthquakes;
                                }
                                Element entry = (Element) nl.item(i);
                                Element id = (Element) entry.getElementsByTagName("id").item(0);
                                Element title = (Element) entry.getElementsByTagName("title").item(0);
                                Element g = (Element) entry.getElementsByTagName("georss:point").item(0);
                                Element when = (Element) entry.getElementsByTagName("updated").item(0);
                                Element link = (Element) entry.getElementsByTagName("link").item(0);

                                String idString = id.getFirstChild().getNodeValue();
                                String details = title.getFirstChild().getNodeValue();
                                String hostname = "http://earthquake.usgs.gov";
                                String linkString = hostname + link.getAttribute("href");
                                String point = g.getFirstChild().getNodeValue();
                                String dt = when.getFirstChild().getNodeValue();
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");
                                Date qdate = new GregorianCalendar(0, 0, 0).getTime();
                                try {
                                    qdate = sdf.parse(dt);
                                } catch (ParseException e) {
                                    Log.e(TAG, "Date parsing exception.", e);
                                }

                                String[] location = point.split(" ");
                                Location l = new Location("dummyGPS");
                                l.setLatitude(Double.parseDouble(location[0]));
                                l.setLongitude(Double.parseDouble(location[1]));

                                String magnitudeString = details.split(" ")[1];
                                int end = magnitudeString.length() - 1;
                                double magnitude = Double.parseDouble(magnitudeString.substring(0, end));

                                if (details.contains("-"))
                                    details = details.split("-")[1].trim();
                                else
                                    details = "";
                                final Earthquake earthquake = new Earthquake(idString, qdate, details, l, magnitude, linkString);

                                // add the new earthquake to our result array
                                earthquakes.add(earthquake);

                            }
                        }
                    }
                    httpConnection.disconnect();
                } catch (MalformedURLException e) {
                    Log.e(TAG, "MalformedException", e);
                } catch (IOException e) {
                    Log.e(TAG, "IOException", e);
                } catch (ParserConfigurationException e) {
                    Log.e(TAG, "Parser Configuration Exception", e);
                } catch (SAXException e) {
                    Log.e(TAG, "SAX Exception", e);
                }
                // return our result array
                return earthquakes;
            }

            @Override
            protected void onPostExecute(List<Earthquake> data) {
                // update the live data with the new list
                earthquakes.setValue(data);
            }
        }.execute();

    }

    private List<Earthquake> parseJson(InputStream in) throws IOException {
        // Create a new Json Reader to parse the input.
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            // Create an empty list of earthquakes.
            List<Earthquake> earthquakes = null;
            // The root node of the Earthquake JSON feed is an object that
            // we must parse.
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                // We are only interested in one sub-object: the array of
                // earthquakes labeled as features.
                if (name.equals("features")) {
                    earthquakes = readEarthquakeArray(reader);
                } else {
                    // We will ignore all other root level values and objects.
                    reader.skipValue();
                }
            }
            reader.endObject();
            return earthquakes;
        } finally {
            reader.close();
        }
    }

    // Traverse the array of earthquakes.
    private List<Earthquake> readEarthquakeArray(JsonReader reader) throws IOException {
        List<Earthquake> earthquakes = new ArrayList<Earthquake>();
        // The earthquake details are stored in an array.
        reader.beginArray();
        while (reader.hasNext()) {
            // Traverse the array, parsing each earthquake.
            earthquakes.add(readEarthquake(reader));
        }
        reader.endArray();
        return earthquakes;
    }

    // Parse each earthquake object within the earthquake array.
    public Earthquake readEarthquake(JsonReader reader) throws IOException {
        String id = null;
        Location location = null;
        Earthquake earthquakeProperties = null;
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("id")) {
                // The ID is stored as a value.
                id = reader.nextString();
            } else if (name.equals("geometry")) {
                // The location is stored as a geometry object
                // that must be parsed.
                location = readLocation(reader);
            } else if (name.equals("properties")) {
                // Most of the earthquake details are stored as a
                // properties object that must be parsed.
                earthquakeProperties = readEarthquakeProperties(reader);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        // Construct a new Earthquake based on the parsed details.
        return new Earthquake(id,
                earthquakeProperties.getDate(),
                earthquakeProperties.getDetails(),
                location,
                earthquakeProperties.getMagnitude(),
                earthquakeProperties.getLink());
    }

    // Parse the properties object for each earthquake object
    // within the earthquake array.
    public Earthquake readEarthquakeProperties(JsonReader reader) throws IOException {
        Date date = null;
        String details = null;
        double magnitude = -1;
        String link = null;
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("time")) {
                long time = reader.nextLong();
                date = new Date(time);
            } else if (name.equals("place")) {
                details = reader.nextString();
            } else if (name.equals("url")) {
                link = reader.nextString();
            } else if (name.equals("mag")) {
                magnitude = reader.nextDouble();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new Earthquake(null, date, details, null, magnitude, link);
    }

    // Parse the coordinates object to obtain a location.
    private Location readLocation(JsonReader reader) throws IOException {
        Location location = null;
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("coordinates")) {
                // The location coordinates are stored within an
                // array of doubles.
                List<Double> coords = readDoublesArray(reader);
                location = new Location("dummy");
                location.setLatitude(coords.get(0));
                location.setLongitude(coords.get(1));
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return location;
    }

    // Parse an array of doubles.
    public List<Double> readDoublesArray(JsonReader reader) throws IOException {
        List<Double> doubles = new ArrayList<Double>();

        reader.beginArray();
        while (reader.hasNext()) {
            doubles.add(reader.nextDouble());
        }
        reader.endArray();
        return doubles;
    }
}
