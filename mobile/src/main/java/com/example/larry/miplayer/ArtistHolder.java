package com.example.larry.miplayer;
/*
 * Only subclassed for the toString change. 
 * for the Loops for the SectionIndexer compiler
 * in the Adapters class
 */
class ArtistHolder extends SongHolder{

	@Override
	public String toString() {
		return super.getArtist().toString();
	}
	
}
