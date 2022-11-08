ALTER TABLE "lets_travel$touristbookingdetails" ADD "bookingtime" TIMESTAMP NULL;
INSERT INTO "mendixsystem$attribute" ("id", 
"entity_id", 
"attribute_name", 
"column_name", 
"type", 
"length", 
"default_value", 
"is_auto_number")
 VALUES ('b05d9d59-8b71-4c91-9349-0f3e2ae66452', 
'69cb2d1a-b3f6-4c68-a6d2-780fef77b8b2', 
'bookingtime', 
'bookingtime', 
20, 
0, 
'', 
false);
UPDATE "mendixsystem$version"
 SET "versionnumber" = '4.2', 
"lastsyncdate" = '20220727 12:15:52';
