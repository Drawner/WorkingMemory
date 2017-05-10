
-- Drop the table first
DROP TABLE IF EXISTS working;

-- TODO Timezone stored in VARCHAR seems a waste of space.
-- SQL Statement to create a new database.
-- Note: if the database names change, change the code in appModel.ToDoList()
CREATE TABLE IF NOT EXISTS working(
rowid integer primary key autoincrement
, ToDoItem VARCHAR
, ToDoKey VARCHAR
, ToDoDateTime VARCHAR
, ToDoDateTimeEpoch Long
, ToDoTimeZone VARCHAR
, ToDoReminderEpoch Long
, ToDoReminderChk integer default 0
, ToDoLEDColor integer default 0
, ToDoFired integer default 0
, ToDoSetAlarm integer default 1
, deleted integer default 0
);

-- Drop the table first
DROP TABLE IF EXISTS icons;

CREATE TABLE IF NOT EXISTS icons(
rowid integer primary key autoincrement
, resid integer not null
,FOREIGN KEY (resid) REFERENCES working(rowid)
);