package com.example.larry.miplayer;
/*
 * Only subclassed for the toString change. 
 * for the Loops for the SectionIndexer compiler
 * in the Adapters class
 */
public class AllSongHolder extends SongHolder{

	public AllSongHolder() {
	}
	
		@Override
		public String toString() {
			return super.getTitle();
		}
		
	
}