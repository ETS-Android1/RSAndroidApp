package de.bahnhoefe.deutschlands.bahnhofsfotos.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import de.bahnhoefe.deutschlands.bahnhofsfotos.BaseApplication;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pelzi on 17.09.16.
 * // todo Cache einbauen
 */
public class BahnhofsFotoFetchTask extends AsyncTask<Integer, Void, URL> {
    private final static String TAG = BahnhofsFotoFetchTask.class.getSimpleName();
    private final BitmapAvailableHandler handler;
    private final String DESCRIPTOR_URL_TEMPLATE = "https://railway-stations.org/bahnhoefe/%s/bhfnr/%d/bahnhofsfotos.json";
    private String license;
    private Uri authorReference;
    private String author;
    private String countryShortCode;
    SharedPreferences sharedPreferences;



    public BahnhofsFotoFetchTask(BitmapAvailableHandler handler, Context context) {
        this.handler = handler;
        sharedPreferences = context.getSharedPreferences("APP_PREF_FILE", Context.MODE_PRIVATE);
        countryShortCode = sharedPreferences.getString("APP_PREF_COUNTRY", BaseApplication.DEFAULT_COUNTRY);
    }

    private void initConfigurationParameters() {
        Log.d(TAG, "countryShortCode: " + countryShortCode);
    }


    @Override
    protected void onCancelled() {
        super.onCancelled();
        handler.onBitmapAvailable(null);
    }

    @Override
    protected void onPostExecute(URL url) {
        super.onPostExecute(url);
        if (url != null) {
            Log.i(TAG, "Fetching photo from " + url);
            // fetch bitmap asynchronously, call onBitmapAvailable if ready
            BitmapCache.getInstance().getFoto(handler, url);
        } else {
            handler.onBitmapAvailable(null);
        }
    }

    @Override
    protected void onPreExecute() {
        initConfigurationParameters();
    }

    @Override
    protected URL doInBackground(Integer... integers) {
        Integer bahnhofsNr = integers[0];
        InputStream is = null;
        try {
            Log.i(TAG, "Fetching Photo descriptor for station nr.: " + bahnhofsNr);
            URL descriptorUrl = new URL(String.format(DESCRIPTOR_URL_TEMPLATE, countryShortCode.toLowerCase(), bahnhofsNr));
            Log.d(TAG, "Attempting to fetch descriptor from URL " + descriptorUrl);
            HttpURLConnection httpConnection = (HttpURLConnection) descriptorUrl.openConnection();
            is = httpConnection.getInputStream();
            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String contentType = httpConnection.getContentType();
                if (!"application/json".equals(contentType)) {
                    Log.e(TAG, "Supplied URL does not appear to be a JSON resource (type=" + contentType + ")");
                } else {
                    BufferedReader in = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
                    String jsonString = "";
                    String nextLine = null;
                    do {
                        nextLine = in.readLine();
                        if (nextLine != null)
                            jsonString += "\n" + nextLine;
                    } while (nextLine != null);
                    JSONObject object = new JSONArray(jsonString).getJSONObject(0);
                    URL photoURL = new URL(object.getString("bahnhofsfoto"));
                    setAuthorReference(Uri.parse(object.getString("fotograf")));
                    setAuthor(object.getString("fotograf-title"));
                    setLicense(object.getString("lizenz"));
                    return photoURL;
                }
            } else {
                Log.e(TAG, "Error downloading descriptor: " + httpConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(TAG, "Could not download descriptor");
        } catch (JSONException e) {
            Log.wtf(TAG, "Parse error on returned json", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.w(TAG, "Could not close channel to photo download");
                }
            }
        }
        return null;
    }


    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public Uri getAuthorReference() {
        return authorReference;
    }

    public void setAuthorReference(Uri author) {
        this.authorReference = author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthor() {
        return author;
    }
}
