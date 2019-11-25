DROP TABLE IF EXISTS Tracks;
CREATE TABLE Tracks (
	artist VARCHAR(20) NOT NULL,
	title VARCHAR(40) NOT NULL,
	filename VARCHAR(40),
	album VARCHAR(40),
	genre VARCHAR(20),
	filesize INT,
	length INT,
	bitrate INT,
	PRIMARY KEY (artist,title));

DROP TABLE IF EXISTS Playlists;
CREATE TABLE Playlists (
	name VARCHAR(20) NOT NULL,
	PRIMARY KEY (name));

DROP TABLE IF EXISTS Users;
CREATE TABLE Users (
	name VARCHAR(10) NOT NULL,
	PRIMARY KEY (name));

DROP TABLE IF EXISTS UserPlaylists;
CREATE TABLE UserPlaylists (
	user VARCHAR(10) NOT NULL,
	position INT NOT NULL,
	playlist VARCHAR(20),
	PRIMARY KEY (user,position));

DROP TABLE IF EXISTS PlaylistTracks;
CREATE TABLE PlaylistTracks (
	playlist VARCHAR(20) NOT NULL,
	position INT NOT NULL,
	trackartist VARCHAR(20),
	tracktitle VARCHAR(40),
	PRIMARY KEY (playlist,position));
