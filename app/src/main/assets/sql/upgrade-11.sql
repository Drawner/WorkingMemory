
ATTACH DATABASE "login.db" AS aDB;

-- Populate the new table
INSERT INTO working (
ToDoItem
, ToDoDateTime
, ToDoDateTimeEpoch
, ToDoTimeZone
, ToDoReminderEpoch
, ToDoReminderChk
, ToDoLEDColor
, ToDoFired
, deleted)
SELECT * FROM aDB.login;