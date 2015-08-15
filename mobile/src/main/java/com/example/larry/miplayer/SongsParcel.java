package com.example.larry.miplayer;

import org.json.JSONException;
import org.json.JSONObject;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
/*
 * General Parcel class where I bundle up a song or w/e and pass it on to the next activity
 */
public class SongsParcel implements Parcelable{

	// Constants
	final public static String TRACK_ID_KEY = "track_id_key";
	final public static String TRACK_ARTIST_KEY = "track_artist_key";
	final public static String TRACK_TITLE_KEY = "track_title_key";
	final public static String TRACK_ALBUM_KEY = "TRACK_ALBUM_KEY";
	final public static String TRACK_DATA_KEY = "TRACK_DATA_KEY";
	final public static String TRACK_ALBUM_ID_KEY = "TRACK_ALBUM_ID_KEY";
	final public static String TRACK_DURATION_KEY = "TRACK_DURATION_KEY";
	
	// fields
	//private final String id;
	private final String artist;
	//private final String title;
	private final String album;
	//private final String data;
	private final String albumId;
	private final String wRawJson;
	//private final long duration;

	public SongsParcel(JSONObject jsonObject) throws JSONException {
		wRawJson = jsonObject.toString();
		//id = jsonObject.getString(TRACK_ID_KEY);
		artist = jsonObject.getString(TRACK_ARTIST_KEY);
		//title = jsonObject.getString(TRACK_TITLE_KEY);
		album = jsonObject.getString(TRACK_ALBUM_KEY);
		//data = jsonObject.getString(TRACK_DATA_KEY);
		albumId = jsonObject.getString(TRACK_ALBUM_ID_KEY);
		//duration = jsonObject.getLong(TRACK_DURATION_KEY);
	}
	public String getArtist() {
		return artist;
	}
	/*public String getId() {
		return id;
	}

	

	public String getTitle() {
		return title;
	}


	public String getData() {
		return data;
	}

	

	public String getwRawJson() {
		return wRawJson;
	}
	
	public long getDuration() {
		return duration;
	}
*/
	public String getAlbum() {
		return album;
	}
	
	public String getAlbumId() {
		return albumId;
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(wRawJson);
	}
	


	public static Creator<SongsParcel> CREATOR = new Creator<SongsParcel>() {

		@Override
		public SongsParcel createFromParcel(Parcel source) {
			final String rawJson = source.readString();
			try{
				final JSONObject jsonObject = new JSONObject(rawJson);
				return new SongsParcel(jsonObject);
			}catch(JSONException e){
				Log.e("PROPERTY", "Failed to create Property from JSON "+ e.getMessage());
			}
			return null;
		}

		@Override
		public SongsParcel[] newArray(int size) {
			// TODO Auto-generated method stub
			return new SongsParcel[size];
		}
	};

}
