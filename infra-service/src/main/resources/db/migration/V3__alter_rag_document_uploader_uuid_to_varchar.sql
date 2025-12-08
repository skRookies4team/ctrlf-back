-- Alter infra.rag_document.uploader_uuid to varchar(36)
ALTER TABLE "infra"."rag_document"
  ALTER COLUMN "uploader_uuid" TYPE varchar(36)
  USING "uploader_uuid"::varchar(36);

