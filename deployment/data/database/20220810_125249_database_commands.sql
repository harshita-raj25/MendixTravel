ALTER TABLE "lets_travel$tourist" DROP COLUMN "validateotp";
ALTER TABLE "lets_travel$tourist" DROP COLUMN "otp";
ALTER TABLE "lets_travel$tourist" ADD "validateotp" INT NULL;
ALTER TABLE "lets_travel$tourist" ADD "otp2" INT NULL;
UPDATE "lets_travel$tourist"
 SET "otp2" = 0;
DELETE FROM "mendixsystem$attribute" 
 WHERE "id" = '69e822ed-8c36-42db-8159-75a73a133afd';
DELETE FROM "mendixsystem$attribute" 
 WHERE "id" = 'b49539db-6be5-4a89-9b63-26ddaca3d2fc';
INSERT INTO "mendixsystem$attribute" ("id", 
"entity_id", 
"attribute_name", 
"column_name", 
"type", 
"length", 
"default_value", 
"is_auto_number")
 VALUES ('3e419bd9-4ace-47c1-a7f3-8d53a4bf7d97', 
'4856c549-d90b-4763-80c6-d806c7b2b4e2', 
'ValidateOtp', 
'validateotp', 
3, 
0, 
'', 
false);
INSERT INTO "mendixsystem$attribute" ("id", 
"entity_id", 
"attribute_name", 
"column_name", 
"type", 
"length", 
"default_value", 
"is_auto_number")
 VALUES ('a4f2879e-8486-4e57-95cf-cf695e5488d1', 
'4856c549-d90b-4763-80c6-d806c7b2b4e2', 
'OTP', 
'otp2', 
3, 
0, 
'0', 
false);
ALTER TABLE "lets_travel$guide" ADD "validateotp" INT NULL;
UPDATE "lets_travel$guide"
 SET "validateotp" = 0;
ALTER TABLE "lets_travel$guide" ADD "otp" INT NULL;
UPDATE "lets_travel$guide"
 SET "otp" = 0;
INSERT INTO "mendixsystem$attribute" ("id", 
"entity_id", 
"attribute_name", 
"column_name", 
"type", 
"length", 
"default_value", 
"is_auto_number")
 VALUES ('39ea7154-3f67-4de2-b426-a1bb09742015', 
'b5839496-0329-43a2-8d2a-4e7bccac5846', 
'OTP', 
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
 VALUES ('81c3490a-4144-486c-b110-56f3fc504855', 
'b5839496-0329-43a2-8d2a-4e7bccac5846', 
'ValidateOtp', 
'validateotp', 
3, 
0, 
'0', 
false);
UPDATE "mendixsystem$version"
 SET "versionnumber" = '4.2', 
"lastsyncdate" = '20220810 12:52:49';
