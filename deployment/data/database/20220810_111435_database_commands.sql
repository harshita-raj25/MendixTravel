ALTER TABLE "lets_travel$tourist" ADD "otp" INT NULL;
UPDATE "lets_travel$tourist"
 SET "otp" = 0;
ALTER TABLE "lets_travel$tourist" ADD "validateotp" INT NULL;
UPDATE "lets_travel$tourist"
 SET "validateotp" = 0;
INSERT INTO "mendixsystem$attribute" ("id", 
"entity_id", 
"attribute_name", 
"column_name", 
"type", 
"length", 
"default_value", 
"is_auto_number")
 VALUES ('69e822ed-8c36-42db-8159-75a73a133afd', 
'4856c549-d90b-4763-80c6-d806c7b2b4e2', 
'Otp', 
'otp', 
3, 
0, 
'0', 
false);
INSERT INTO "mendixsystem$attribute" ("id", 
"entity_id", 
"attribute_name", 
"column_name", 
"type", 
"length", 
"default_value", 
"is_auto_number")
 VALUES ('b49539db-6be5-4a89-9b63-26ddaca3d2fc', 
'4856c549-d90b-4763-80c6-d806c7b2b4e2', 
'ValidateOtp', 
'validateotp', 
3, 
0, 
'0', 
false);
UPDATE "mendixsystem$version"
 SET "versionnumber" = '4.2', 
"lastsyncdate" = '20220810 11:14:35';
