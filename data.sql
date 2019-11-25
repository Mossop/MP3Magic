INSERT INTO Tracks
(artist,title,filename,album,genre,filesize,length,bitrate)
VALUES
('Queen','A Kind of Magic','','Classic Queen','rock',0,0,128),
('Queen','Bohemian Rhapsody','','Classic Queen','rock',0,0,128),
('Queen','Under Pressure','','Classic Queen','rock',0,0,128),
('Queen','Hammer to Fall','','Classic Queen','rock',0,0,128),
('Queen','Stone Cold Crazy','','Classic Queen','rock',0,0,128),
('Queen','One Year of Love','','Classic Queen','rock',0,0,128),
('Queen','Radio Ga Ga','','Classic Queen','rock',0,0,128),
('Queen','Im Going Slightly Mad','','Classic Queen','rock',0,0,128),
('Queen','I Want it All','','Classic Queen','rock',0,0,128),
('Queen','Tie Your Mother Down','','Classic Queen','rock',0,0,128),
('Queen','The Miracle','','Classic Queen','rock',0,0,128),
('Queen','These are the Days of our Lives','','Classic Queen','rock',0,0,128),
('Queen','One Vision','','Classic Queen','rock',0,0,128),
('Queen','Keep Yourself Alive','','Classic Queen','rock',0,0,128),
('Queen','Headlong','','Classic Queen','rock',0,0,128),
('Queen','Who Wants to Live Forever','','Classic Queen','rock',0,0,128),
('Queen','The Show Must go on','','Classic Queen','rock',0,0,128),
('The Corrs','Breathless','','In Blue','pop',0,0,128),
('The Corrs','Give me a Reason','','In Blue','pop',0,0,128),
('The Corrs','Somebody for Someone','','In Blue','pop',0,0,128),
('The Corrs','Say','','In Blue','pop',0,0,128),
('The Corrs','All the Love in the World','','In Blue','pop',0,0,128),
('The Corrs','Radio','','In Blue','pop',0,0,128),
('The Corrs','Irresistable','','In Blue','pop',0,0,128),
('The Corrs','One Night','','In Blue','pop',0,0,128),
('The Corrs','All in a Day','','In Blue','pop',0,0,128),
('The Corrs','At Your Side','','In Blue','pop',0,0,128),
('The Corrs','No More Cry','','In Blue','pop',0,0,128),
('The Corrs','Rain','','In Blue','pop',0,0,128),
('The Corrs','Give it All Up','','In Blue','pop',0,0,128),
('The Corrs','Hurt Before','','In Blue','pop',0,0,128),
('The Corrs','Rebel Heart','','In Blue','pop',0,0,128);

INSERT INTO Playlists
(name)
VALUES
('Classic Queen'),
('In Blue');

INSERT INTO Users
(name)
VALUES
('dave'),
('neil'),
('matt'),
('jon'),
('rich'),
('doug');

INSERT INTO PlaylistTracks
(trackartist,tracktitle,playlist,position)
VALUES
('Queen','A Kind of Magic','Classic Queen',1),
('Queen','Bohemian Rhapsody','Classic Queen',2),
('Queen','Under Pressure','Classic Queen',3),
('Queen','Hammer to Fall','Classic Queen',4),
('Queen','Stone Cold Crazy','Classic Queen',5),
('Queen','One Year of Love','Classic Queen',6),
('Queen','Radio Ga Ga','Classic Queen',7),
('Queen','I''m Going Slightly Mad','Classic Queen',8),
('Queen','I Want it All','Classic Queen',9),
('Queen','Tie Your Mother Down','Classic Queen',10),
('Queen','The Miracle','Classic Queen',11),
('Queen','These are the Days of our Lives','Classic Queen',12),
('Queen','One Vision','Classic Queen',13),
('Queen','Keep Yourself Alive','Classic Queen',14),
('Queen','Headlong','Classic Queen',15),
('Queen','Who Wants to Live Forever','Classic Queen',16),
('Queen','The Show Must go on','Classic Queen',17),
('The Corrs','Breathless','In Blue',1),
('The Corrs','Give me a Reason','In Blue',2),
('The Corrs','Somebody for Someone','In Blue',3),
('The Corrs','Say','In Blue',4),
('The Corrs','All the Love in the World','In Blue',5),
('The Corrs','Radio','In Blue',6),
('The Corrs','Irresistable','In Blue',7),
('The Corrs','One Night','In Blue',8),
('The Corrs','All in a Day','In Blue',9),
('The Corrs','At Your Side','In Blue',10),
('The Corrs','No More Cry','In Blue',11),
('The Corrs','Rain','In Blue',12),
('The Corrs','Give it All Up','In Blue',13),
('The Corrs','Hurt Before','In Blue',14),
('The Corrs','Rebel Heart','In Blue',15);

INSERT INTO UserPlaylists
(user,position,playlist)
VALUES
('dave',1,'Classic Queen'),
('dave',2,'In Blue');
