package com.example.larry.miplayer;

class SongHolder {
	private String artist, title, album, data, id, albumId, albumImage, artistId;
	private long duration;
	private boolean isPodcast, isMusic;

	SongHolder() {

	}

	public String getArtist() {
		return artist;
	}
	
	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	@Override
	public String toString() {
		return album.toString();
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAlbumId() {
		return albumId;
	}

	public void setAlbumId(String albumId) {
		this.albumId = albumId;
	}

	public boolean isPodcast() {
		return isPodcast;
	}

	public void setPodcast(boolean isPodcast) {
		this.isPodcast = isPodcast;
	}

	public boolean isMusic() {
		return isMusic;
	}

	public void setMusic(boolean isMusic) {
		this.isMusic = isMusic;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public String getAlbumImage() {
		return albumImage;
	}

	public void setAlbumImage(String albumImage) {
		this.albumImage = albumImage;
	}

	public String getArtistId() {
		return artistId;
	}

	public void setArtistId(String artistId) {
		this.artistId = artistId;
	}
}
