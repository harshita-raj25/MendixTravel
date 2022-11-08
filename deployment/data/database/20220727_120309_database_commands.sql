ALTER TABLE "lets_travel$cancelledbooking" RENAME TO "9dfc981ee03246f980bdc5343cf08fe2";
ALTER TABLE "lets_travel$cancelledbooking_touristbookingdetails" DROP CONSTRAINT "uniq_lets_travel$cancelledbooking_touristbookingdetails_lets_travel$cancelledbookingid";
DROP INDEX "idx_lets_travel$cancelledbooking_touristbookingdetails_lets_travel$touristbookingdetails_lets_travel$cancelledbooking";
ALTER TABLE "lets_travel$cancelledbooking_touristbookingdetails" RENAME TO "7b01ef50e5bb4231b9b93cff9d1e50d7";
DELETE FROM "mendixsystem$entity" 
 WHERE "id" = 'af2e45d0-59ac-47ce-9f9c-61168e636769';
DELETE FROM "mendixsystem$entityidentifier" 
 WHERE "id" = 'af2e45d0-59ac-47ce-9f9c-61168e636769';
DELETE FROM "mendixsystem$sequence" 
 WHERE "attribute_id" IN (SELECT "id"
 FROM "mendixsystem$attribute"
 WHERE "entity_id" = 'af2e45d0-59ac-47ce-9f9c-61168e636769');
DELETE FROM "mendixsystem$remote_primary_key" 
 WHERE "entity_id" = 'af2e45d0-59ac-47ce-9f9c-61168e636769';
DELETE FROM "mendixsystem$attribute" 
 WHERE "entity_id" = 'af2e45d0-59ac-47ce-9f9c-61168e636769';
DELETE FROM "mendixsystem$association" 
 WHERE "id" = 'c132807b-ddb9-44bc-9839-8273ec91e123';
DELETE FROM "mendixsystem$unique_constraint" 
 WHERE "name" = 'uniq_lets_travel$cancelledbooking_touristbookingdetails_lets_travel$cancelledbookingid' AND "column_id" = 'c6485f67-2ff1-3d1d-842d-73ad7d30da98';
DROP TABLE "9dfc981ee03246f980bdc5343cf08fe2";
DROP TABLE "7b01ef50e5bb4231b9b93cff9d1e50d7";
UPDATE "mendixsystem$version"
 SET "versionnumber" = '4.2', 
"lastsyncdate" = '20220727 12:03:09';
