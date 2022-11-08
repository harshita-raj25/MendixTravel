ALTER TABLE "lets_travel$touristbookingdetails" DROP COLUMN "bookingtime";
DELETE FROM "mendixsystem$attribute" 
 WHERE "id" = 'b05d9d59-8b71-4c91-9349-0f3e2ae66452';
UPDATE "mendixsystem$version"
 SET "versionnumber" = '4.2', 
"lastsyncdate" = '20220727 13:13:05';
