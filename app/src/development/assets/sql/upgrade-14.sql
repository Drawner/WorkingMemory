-- Drop the table first
DROP TABLE IF EXISTS icons;

CREATE TABLE IF NOT EXISTS icons(
rowid integer primary key autoincrement
, resid integer not null
,FOREIGN KEY (resid) REFERENCES working(rowid)
);